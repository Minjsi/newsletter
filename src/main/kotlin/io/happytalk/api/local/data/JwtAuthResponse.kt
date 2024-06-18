package io.happytalk.api.local.data

import io.happytalk.api.global.data.DefaultResponse
import io.jsonwebtoken.Claims
import io.swagger.v3.oas.annotations.media.Schema

/**
 * @author : LN
 * @since : 2023. 4. 17.
 */
data class JwtAuthResponse(
    @field:Schema(description = "JWT 인증에 사용된 토큰의 claim 정보")
    val claims: Claims,
) : DefaultResponse()
