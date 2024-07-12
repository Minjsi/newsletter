package io.happytalk.api.domain.user.controller

import io.happytalk.api.domain.user.data.dto.UserResponse
import io.happytalk.api.domain.user.service.UserService
import io.happytalk.api.global.document.OpenApiTags
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SecurityExceptionCheckController(
    private val userService: UserService
) {

    @Operation(
        tags = [OpenApiTags.AUTHENTICATION],
        summary = "SecurityExceptionHandler 테스트를 위한 컨트롤러"
    )
    @GetMapping("/check")
    suspend fun authLogin(
    ): UserResponse {
       return userService.selectUserByUsername("rm")

    }

}
