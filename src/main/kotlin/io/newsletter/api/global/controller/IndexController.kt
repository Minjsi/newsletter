package io.newsletter.api.global.controller

import io.happytalk.api.global.data.IndexResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.TreeMap

/**
 * @author : LN
 * @since : 2023. 4. 6.
 */
@RestController
class IndexController {

    @Operation(hidden = true)
    @GetMapping("/", "/health_check")
    fun index(
        request: ServerHttpRequest,
        @RequestParam(name = "show_headers", required = false)
        showHeaders: Boolean,
    ): Mono<IndexResponse> {

        val indexResponse = IndexResponse()
        indexResponse.message = "Welcome to MBI API Server :)"

        // show_headers 파라미터가 true 일 경우, 모든 요청 헤더값을 응답에 포함시킨다.
        if (showHeaders) {
            val requestHeaders: MutableMap<String, String> = TreeMap()
            request.headers.forEach {
                requestHeaders[it.key] = it.value[0]
            }
            val remoteAddress = request.remoteAddress?.address
            val localAddress = request.localAddress?.address
            indexResponse.requestHeaders = requestHeaders
            indexResponse.remoteAddr = remoteAddress?.hostAddress
            indexResponse.localAddr = localAddress?.hostAddress
        }

        return Mono.just(indexResponse)
    }

}
