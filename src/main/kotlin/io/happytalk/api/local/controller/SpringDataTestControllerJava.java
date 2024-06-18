package io.happytalk.api.local.controller;

import io.happytalk.api.local.entity.ExampleEntity;
import io.happytalk.api.local.data.ExampleResponse;
import io.happytalk.api.local.repository.read.ReadTestRepository;
import io.happytalk.api.local.repository.write.WriteTestRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;

/**
 * {@link SpringDataController} JAVA 버전 비교 테스트용 컨트롤러
 *
 * @author : LN
 * @since : 2023. 3. 2.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SpringDataTestControllerJava {

    private final ReadTestRepository readTestRepository;
    private final WriteTestRepository writeTestRepository;

    @Operation(hidden = true)
    @GetMapping("/spring-data/java")
    public Mono<ExampleResponse> springDataRepositoryTest() {

        Flux<ExampleEntity> fluxTestEntities = readTestRepository
                .findAll()
//                .findById(1).flux()
                .flatMap(exampleEntity -> {
                    log.info("testEntity: {}", exampleEntity);
                    return processEntity(exampleEntity);
                })
                .doOnError(throwable ->
                        log.error("error occurred while SpringDataJava.", throwable)
                );

        log.info("return response !!");

        return fluxTestEntities
                .collectList()
                .map(ExampleResponse::new);
    }

    /**
     *
     */
    private Mono<ExampleEntity> processEntity(ExampleEntity exampleEntity) {

        ExampleEntity writeEntity = new ExampleEntity(
                exampleEntity.getId(),
                exampleEntity.getKeyword(),
                exampleEntity.getData(),
                ZonedDateTime.now()
        );

        return readTestRepository
//        return writeTestRepository
                .save(writeEntity)
                // read 커넥션으로 write 수행 테스트 > exception 발생
                //  > DB 계정 Privileges 에 따른 exception
                .onErrorResume(throwable -> {
                    log.error("error occurred while processEntity.", throwable);
                    // write 커넥션으로 다시 수행
                    return writeTestRepository.save(writeEntity);
                });
    }

}
