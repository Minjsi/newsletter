package io.newsletter.api.global.support

import io.happytalk.api.global.data.JwtToken
import io.happytalk.api.global.data.properties.JwtProperties
import io.happytalk.api.global.util.RsaUtils
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Component
import java.security.PrivateKey
import java.security.PublicKey
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.function.Function

/**
 * JWT Access Token 과 Refresh Token 을 생성 및 검증하는 클래스입니다.
 *
 * @author : LN
 * @since : 2023. 2. 24.
 */
@Component
@Suppress("unused")
class JwtManager(
    private val jwtProperties: JwtProperties,
) {

    private val zoneId: ZoneId = ZoneId.systemDefault()

    private var privateKey: PrivateKey? = null
    private var publicKey: PublicKey? = null

    init {
        init()
    }

    /**
     * [JwtProperties] PrivateKey, PublicKey 초기화
     */
    final fun init() {

        privateKey = RsaUtils.getPrivateKey(jwtProperties.privateKey)
        publicKey = RsaUtils.getPublicKey(jwtProperties.publicKey)
    }

    /**
     * JWT Token 을 생성합니다.
     *
     * @param username JWT Payload 에 포함할 사용자 이름
     * @return 생성된 JWT Token 과 만료 시간, 생성 시간을 포함하는 JwtToken 객체
     */
    fun generateToken(username: String): JwtToken {

        return generateToken(
            username,
            jwtProperties.accessTokenValidity,
            jwtProperties.refreshTokenValidity
        )
    }

    /**
     * JWT Token 을 생성합니다.
     *
     * @param username             JWT Payload 에 포함할 사용자 이름
     * @param accessTokenValidity  Access Token 만료 시간 (millisecond)
     * @param refreshTokenValidity Refresh Token 만료 시간 (millisecond)
     * @return 생성된 JWT Token 과 만료 시간, 생성 시간을 포함하는 JwtToken 객체
     */
    fun generateToken(username: String, accessTokenValidity: Long, refreshTokenValidity: Long): JwtToken {

        val now = Date()
        val accessTokenExpiration = Date(now.time + accessTokenValidity)
        val refreshTokenExpiration = Date(now.time + refreshTokenValidity)
        val claims: MutableMap<String, Any> = HashMap()
        claims[Claims.SUBJECT] = username
        claims[Claims.ISSUER] = jwtProperties.issuer
        claims[Claims.ISSUED_AT] = now
        claims[Claims.EXPIRATION] = accessTokenExpiration
        val accessToken = buildToken(claims)
        claims[Claims.EXPIRATION] = refreshTokenExpiration
        val refreshToken = buildToken(claims)

        return JwtToken(
            issuedAt = ZonedDateTime.ofInstant(now.toInstant(), zoneId),
            accessToken = accessToken,
            accessTokenExpiresAt = ZonedDateTime.ofInstant(accessTokenExpiration.toInstant(), zoneId),
            refreshToken = refreshToken,
            refreshTokenExpiresAt = ZonedDateTime.ofInstant(refreshTokenExpiration.toInstant(), zoneId)
        )
    }

    /**
     * JWT 토큰을 검증합니다.
     *
     * @param token JWT Access Token 또는 Refresh Token
     * @return JWT 토큰이 유효하면 true, 그렇지 않으면 false
     */
    fun validateToken(token: String): Boolean {

        return try {
            parseToken(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * JWT Token 에서 사용자 이름(sub) 정보를 추출합니다.
     *
     * @param token JWT Access Token
     * @return 사용자 이름(sub)
     */
    fun getUsernameFromToken(token: String): String {

        return extractClaim(token) { claims: Claims -> claims.subject }
    }

    /**
     * JWT Access Token 에서 Access Token 만료 시각 정보(exp)를 추출합니다.
     *
     * @param token JWT Access Token
     * @return Access Token 만료 시각
     */
    fun getExpirationFromToken(token: String): ZonedDateTime {

        val expiration = extractClaim(token) { claims: Claims -> claims.expiration }
        return ZonedDateTime.ofInstant(expiration.toInstant(), zoneId)
    }

    /**
     * JWT Token 에서 특정 클레임 정보를 추출합니다.
     *
     * @param T 클레임 정보 타입
     * @param token JWT Token
     * @param claimsExtractor 추출할 클레임 정보를 포함
     * @param claimsExtractor 추출할 클레임 정보를 포함한 Function 객체
     * @return 추출된 클레임 정보
     */
    fun <T> extractClaim(token: String, claimsExtractor: Function<Claims, T>): T {

        val claims: Claims = parseToken(token)
        return claimsExtractor.apply(claims)
    }

    /**
     * JWT Token 을 검증하고, 토큰의 클레임 정보를 반환합니다.
     *
     * @param token JWT Token
     * @return JWT Token 의 클레임 정보
     */
    fun parseToken(token: String): Claims {

        return Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    /**
     * JWT Token 을 생성합니다.
     *
     * @param claims JWT Payload 에 포함할 클레임 정보
     * @return 생성된 JWT Token
     */
    private fun buildToken(claims: Map<String, Any?>): String {

        return Jwts.builder()
            .signWith(privateKey, Jwts.SIG.RS256)
            .claims(claims)
            .compact()
    }
}
