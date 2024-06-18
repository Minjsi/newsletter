package io.happytalk.api.local.controller

import io.happytalk.api.global.data.Principal
import io.happytalk.api.global.document.OpenApiConstants.JWT
import io.happytalk.api.global.document.OpenApiTags
import io.happytalk.api.local.data.JwtAuthResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * @author : LN
 * @since : 2023. 4. 17.
 */
@RestController
class JwtAuthController {

    @Operation(
        tags = [OpenApiTags.LOCAL_EXAMPLE],
        security = [SecurityRequirement(name = JWT)],
        summary = "JWT 인증 예제",
        description = "Spring Security 의 `ServerSecurityContextRepository` 인터페이스 사용한 JWT 인증 예제\n" +
                "* [OAuth2 Resource Server JWT](https://docs.spring.io/spring-security/reference/reactive/oauth2/resource-server/jwt.html) 사용 가능하지만 목적에 비해 구조가 복잡.\n" +
                "* 실제 어플리케이션 구현시 `SecurityContextRepository` 클래스의 `TODO` 내용들 확인 후 처리 필요.\n" +
                "* `JWT 생성 예제` 통해 만들어진 토큰을 Authentication 메뉴에서 SET 후 요청 가능."
    )
    @PatchMapping("/example/jwt-auth")
    @PreAuthorize("hasRole('ADMIN')")
    fun jwtAuth(
        @AuthenticationPrincipal
        principal: Principal,
    ): Mono<JwtAuthResponse> {

        return Mono.just(
            JwtAuthResponse(principal.claims)
        )
    }
}
