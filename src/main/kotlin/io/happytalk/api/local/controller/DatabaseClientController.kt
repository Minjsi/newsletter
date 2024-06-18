package io.happytalk.api.local.controller

import io.happytalk.api.global.document.OpenApiTags
import io.happytalk.api.local.data.ExampleResponse
import io.happytalk.api.local.entity.ExampleEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.ZonedDateTime

/**
 * @author : LN
 * @since : 2023. 4. 6.
 */
@RestController
class DatabaseClientController(
    @Qualifier("readDatabaseClient")
    private val readDatabaseClient: DatabaseClient,
    @Qualifier("writeDatabaseClient")
    private val writeDatabaseClient: DatabaseClient,
    private val converter: MappingR2dbcConverter,
) {

    private val logger = KotlinLogging.logger { }

    @Operation(
        tags = [OpenApiTags.LOCAL_EXAMPLE],
        summary = "DatabaseClient 사용 예제",
        description = "`DatabaseClient` 사용한 DB 작업 예제 [문서](https://docs.spring.io/spring-framework/docs/6.0.7/reference/html/data-access.html#r2dbc-DatabaseClient)"
    )
    @GetMapping("/p/example/database-client")
    fun databaseClientTest(): Mono<ExampleResponse> {

        val fluxTestEntities: Flux<ExampleEntity> = readDatabaseClient
            .sql(
                """
        SELECT * FROM example WHERE id > :id
        """.trimIndent()
            )
            .bind("id", 0)
            .map { row, metadata -> converter.read(ExampleEntity::class.java, row, metadata) }
            .all()
            .flatMap { exampleEntity: ExampleEntity ->
                logger.info { "testEntity: $exampleEntity" }
                processEntity(exampleEntity)
            }.doOnError { throwable: Throwable ->
                logger.error(throwable) { "error occurred while DatabaseClient test." }
            }

        logger.info { "return response !!" }

        return fluxTestEntities.collectList().map {
            ExampleResponse(it)
        }
    }

    /**
     * @param exampleEntity
     * @return
     */
    private fun processEntity(exampleEntity: ExampleEntity): Mono<ExampleEntity> {

        val writeEntity = exampleEntity.copy(regDate = ZonedDateTime.now())

        val updateQuery = """
        UPDATE example
        SET reg_date = :regDate
        WHERE id = :id
        """.trimIndent()

        return readDatabaseClient
//        return writeDatabaseClient
            // read 커넥션으로 write 수행 테스트 > exception 발생
            //  > DB 계정 Privileges 에 따른 exception
            .sql(updateQuery)
            .bind("id", writeEntity.id)
            .bind("regDate", writeEntity.regDate.toLocalDateTime())
            .fetch()
            .rowsUpdated()
            .onErrorResume { throwable ->
                logger.error(throwable) { "error occurred while processEntity." }
                // write 커넥션으로 다시 수행
                writeDatabaseClient
                    .sql(updateQuery)
                    .bind("id", writeEntity.id)
                    .bind("regDate", writeEntity.regDate.toLocalDateTime())
                    .fetch()
                    .rowsUpdated()
            }
            .handle { it, sink ->
                if (it > 0) {
                    sink.next(writeEntity)
                } else {
                    sink.error(RuntimeException("failed to update data."))
                }
            }
    }

}
