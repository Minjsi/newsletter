package io.happytalk.api.local.controller

import io.happytalk.api.global.document.OpenApiTags
import io.happytalk.api.global.support.WebClientManager
import io.happytalk.api.local.data.JwtGenerateRequest
import io.happytalk.api.local.data.JwtGenerateResponse
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * @author : LN
 * @since : 2023. 4. 17.
 */
@RestController
class WebclientController(
    private val serverProperties: ServerProperties,
    private val webClientManager: WebClientManager,
) {

    @Operation(
        tags = [OpenApiTags.LOCAL_EXAMPLE],
        summary = "WebClient 사용 예제",
        description = "`WebClient` 사용한 DB 작업 예제 [문서](https://docs.spring.io/spring-boot/docs/3.0.5/reference/html/io.html#io.rest-client.webclient)\n" +
                "* JWT 생성 예제 API 를 WebClient 로 호출 후 결과 반환"
    )
    @PatchMapping("/p/example/webclient")
    fun jwtCreate(
        @Valid
        @RequestBody
        request: JwtGenerateRequest,
    ): Mono<JwtGenerateResponse> {

        val webClient = webClientManager.create("http://127.0.0.1:${serverProperties.port}")

        return webClient
            .patch()
            .uri("/p/example/jwt-generate")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(JwtGenerateResponse::class.java)
    }
}
