package io.happytalk.api.local.controller

import io.happytalk.api.global.document.OpenApiTags
import io.happytalk.api.global.support.JwtManager
import io.happytalk.api.local.data.JwtGenerateRequest
import io.happytalk.api.local.data.JwtGenerateResponse
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * @author : LN
 * @since : 2023. 4. 17.
 */
@RestController
class JwtGenerateController(
    private val jwtManager: JwtManager,
) {

    @Operation(
        tags = [OpenApiTags.LOCAL_EXAMPLE],
        summary = "JWT 생성 예제",
        description = "JWT 토큰 생성 예제"
    )
    @PatchMapping("/p/example/jwt-generate")
    fun jwtCreate(
        @Valid
        @RequestBody
        request: JwtGenerateRequest,
    ): Mono<JwtGenerateResponse> {

        val jwtToken = jwtManager.generateToken(request.username)

        return Mono.just(
            JwtGenerateResponse(jwtToken)
        )
    }
}
