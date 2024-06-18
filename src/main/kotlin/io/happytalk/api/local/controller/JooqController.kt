package io.happytalk.api.local.controller

import io.happytalk.api.global.document.OpenApiTags
import io.happytalk.jooq.tables.references.EXAMPLE
import io.happytalk.api.local.data.ExampleResponse
import io.happytalk.api.local.entity.ExampleEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.ZonedDateTime

/**
 * @author : LN
 * @since : 2023. 4. 6.
 */
@RestController
class JooqController(
    @Qualifier("readDSLContext")
    private val readDSLContext: DSLContext,
    @Qualifier("writeDSLContext")
    private val writeDSLContext: DSLContext,
) {

    private val logger = KotlinLogging.logger { }

    @Operation(
        tags = [OpenApiTags.LOCAL_EXAMPLE],
        summary = "jOOQ DSLContext 사용 예제",
        description = "`jOOQ` `DSLContext` 사용한 DB 작업 예제 [문서](https://blog.jooq.org/reactive-sql-with-jooq-3-15-and-r2dbc/)"
    )
    @PutMapping("/p/example/jooq")
    fun jooqDSLContextTest(): Mono<ExampleResponse> {

        val fluxTestEntities: Flux<ExampleEntity> = Flux.from(
            readDSLContext
                .selectFrom(EXAMPLE)
//        .where(TEST.ID.eq(2))
        ).map { record: Record ->
            record.into(ExampleEntity::class.java)
        }.flatMap { exampleEntity: ExampleEntity ->
            logger.info { "testEntity: $exampleEntity" }
            processEntity(exampleEntity)
        }.doOnError { throwable: Throwable ->
            logger.error(throwable) { "error occurred while jOOQ test." }
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

        val writeEntity: ExampleEntity = exampleEntity.copy(regDate = ZonedDateTime.now())

        return Mono.from(
//            writeDSLContext
            readDSLContext
                // read 커넥션으로 write 수행 테스트 > exception 발생
                //  > DB 계정 Privileges 에 따른 exception
                .update(EXAMPLE)
                .set(EXAMPLE.REG_DATE, writeEntity.regDate.toLocalDateTime())
                .where(EXAMPLE.ID.eq(1))
        ).onErrorResume { throwable ->
            logger.error(throwable) { "error occurred while processEntity." }
            Mono.from(
                // write 커넥션으로 다시 수행
                writeDSLContext
                    .update(EXAMPLE)
                    .set(EXAMPLE.REG_DATE, writeEntity.regDate.toLocalDateTime())
                    .where(EXAMPLE.ID.eq(1))
            )
        }.map {
            if (it == 1) {
                writeEntity
            } else {
                exampleEntity
            }
        }
    }

}
