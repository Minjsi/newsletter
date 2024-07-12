package io.newsletter.api.global.data.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author : LN
 * @since : 2023. 4. 13.
 */
@ConfigurationProperties("security.jwt")
data class JwtProperties(

    /**
     * RSA 비대칭 키 암호화를 위한 private key 경로
     */
    var privateKey: String = "",

    /**
     * RSA 비대칭 키 암호화를 위한 public key 경로
     */
    var publicKey: String = "",

    /**
     * 토큰 발급자
     */
    var issuer: String = "",

    /**
     * Access Token 의 유효 시간 (단위: 밀리초)
     */
    var accessTokenValidity: Long = 0,

    /**
     * Refresh Token 의 유효 시간 (단위: 밀리초)
     */
    var refreshTokenValidity: Long = 0,
)
