package io.happytalk.api.local.controller

import io.happytalk.api.global.document.OpenApiTags
import io.happytalk.api.local.data.ExampleResponse
import io.happytalk.api.local.entity.ExampleEntity
import io.happytalk.api.local.repository.read.ReadTestRepository
import io.happytalk.api.local.repository.write.WriteTestRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.ZonedDateTime

/**
 * @author : LN
 * @since : 2023. 4. 6.
 */
@RestController
class SpringDataController(
    private val readTestRepository: ReadTestRepository,
    private val writeTestRepository: WriteTestRepository,
) {

    private val logger = KotlinLogging.logger { }

    @Operation(
        tags = [OpenApiTags.LOCAL_EXAMPLE],
        summary = "R2dbcRepository 사용 예제",
        description = "`R2dbcRepository` 사용한 DB 작업 예제 [문서](https://docs.spring.io/spring-data/r2dbc/docs/3.1.1/reference/html/#r2dbc.repositories.usage)"
    )
    @DeleteMapping("/p/example/r2dbc-repository")
    fun springDataRepositoryTest(): Mono<ExampleResponse> {

        val fluxTestEntities: Flux<ExampleEntity> = readTestRepository
            .findAll()
//            .findById(1).flux()
            .flatMap { exampleEntity: ExampleEntity ->
                logger.info { "testEntity: $exampleEntity" }
                processEntity(exampleEntity)
            }.doOnError { throwable: Throwable ->
                logger.error(throwable) { "error occurred while SpringData test." }
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

        return readTestRepository
//        return writeTestRepository
            // read 커넥션으로 write 수행 테스트 > exception 발생
            //  > DB 계정 Privileges 에 따른 exception
            .save(writeEntity)
            .onErrorResume { throwable: Throwable ->
                logger.error(throwable) { "error occurred while processEntity." }
                // write 커넥션으로 다시 수행
                writeTestRepository.save(writeEntity)
            }
    }

}
