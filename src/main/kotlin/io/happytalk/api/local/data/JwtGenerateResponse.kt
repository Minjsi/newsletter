package io.happytalk.api.local.data

import io.happytalk.api.global.data.DefaultResponse
import io.happytalk.api.global.data.JwtToken
import io.swagger.v3.oas.annotations.media.Schema

/**
 * @author : LN
 * @since : 2023. 4. 17.
 */
data class JwtGenerateResponse(
    @field:Schema(description = "생성한 JWT 토큰 정보")
    val jwtToken: JwtToken,
) : DefaultResponse()
