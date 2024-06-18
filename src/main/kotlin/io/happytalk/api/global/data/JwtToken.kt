package io.happytalk.api.global.data

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import java.time.ZonedDateTime

/**
 * JWT Access Token 또는 Refresh Token 정보를 담은 클래스
 *
 * @author : LN
 * @since : 2023. 2. 24.
 */
data class JwtToken(

    @field:Schema(
        requiredMode = RequiredMode.REQUIRED,
        description = "API 호출시 필요한 액세스 토큰"
    )
    val accessToken: String,

    @field:Schema(
        requiredMode = RequiredMode.REQUIRED,
        description = "액세스 토큰 발급 일시"
    )
    val issuedAt: ZonedDateTime,

    @field:Schema(
        requiredMode = RequiredMode.REQUIRED,
        description = "액세스 토큰 만료 일시"
    )
    val accessTokenExpiresAt: ZonedDateTime,

    @field:Schema(
        requiredMode = RequiredMode.REQUIRED,
        description = "액세스 토큰을 재발급 할 수 있는 토큰"
    )
    val refreshToken: String,

    @field:Schema(
        requiredMode = RequiredMode.REQUIRED,
        description = "리프레시 토큰 만료 일시"
    )
    val refreshTokenExpiresAt: ZonedDateTime,
)
