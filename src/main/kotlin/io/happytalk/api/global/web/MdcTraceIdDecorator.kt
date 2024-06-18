package io.happytalk.api.global.web

import org.slf4j.MDC
import org.springframework.http.server.reactive.HttpHandler
import org.springframework.http.server.reactive.HttpHandlerDecoratorFactory
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import reactor.util.context.Context

/**
 * MCD Trace ID 관련 Decorator
 * - [출처](https://mjin1220.tistory.com/52) 참고 후 수정
 *
 * @author : LN
 * @since : 2022. 8. 24.
 */
@Component
class MdcTraceIdDecorator : HttpHandlerDecoratorFactory {

    val mdcTraceIdKey = "mcd.traceId"

    /**
     * @param httpHandler
     * @return
     */
    override fun apply(httpHandler: HttpHandler?): HttpHandler {

        return HttpHandler { request: ServerHttpRequest?, response: ServerHttpResponse? ->
            httpHandler!!.handle(
                request!!, response!!
            ).contextWrite { _ ->
                val traceId: String = getTraceId(request)
                MDC.put(mdcTraceIdKey, traceId)
                Context.of(mdcTraceIdKey, traceId)
            }
        }
    }

    /**
     * @param request
     * @return
     */
    private fun getTraceId(request: ServerHttpRequest): String {

        return request.id
    }
}
