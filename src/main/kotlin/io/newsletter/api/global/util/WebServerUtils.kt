package io.newsletter.api.global.util

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.resource.ResourceWebHandler
import org.springframework.web.server.ServerWebExchange

/**
 * @author : LN
 * @since : 2023. 4. 11
 */
object WebServerUtils {

    private val logger = KotlinLogging.logger { }

    /**
     * ServerWebExchange 에서 현재 요청에 대한 컨트롤러 정보를 추출하는 메서드
     *
     * @param exchange ServerWebExchange 객체
     * @param defaultValue
     * @return 컨트롤러 정보 문자열. 컨트롤러 정보를 가져올 수 없는 경우 defaultValue 반환.
     */
    fun getController(exchange: ServerWebExchange, defaultValue: String): String {

        try {
            val handlerAttr =
                exchange.attributes[HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE] ?: return defaultValue
            if (handlerAttr is HandlerMethod) {
                return handlerAttr.toString()
            }
            if (handlerAttr is ResourceWebHandler) {
                return handlerAttr.toString()
            }
        } catch (e: Exception) {
            logger.warn(e) { "error occurred while get handler." }
        }
        return defaultValue
    }
}
