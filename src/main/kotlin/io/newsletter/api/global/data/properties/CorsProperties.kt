package io.newsletter.api.global.data.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author : LN
 * @since : 2023. 4. 13.
 */
@ConfigurationProperties("security.cors")
data class CorsProperties(

    /**
     * Preflight 요청의 캐시 시간 (단위: 초)
     */
    var maxAge: Long = 3600,

    /**
     * 허용 Origin 패턴 목록
     */
    var allowedOriginPatterns: List<String> = listOf("*"),
)
