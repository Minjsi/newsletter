package io.newsletter.api.global.web

import io.happytalk.api.global.util.DataUtils
import io.happytalk.api.global.util.WebServerUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.reactivestreams.Publisher
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.server.PathContainer
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.http.server.reactive.ServerHttpResponseDecorator
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebExchangeDecorator
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.springframework.web.util.pattern.PathPattern
import org.springframework.web.util.pattern.PathPatternParser
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.charset.StandardCharsets
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicLong

/**
 * Request, Response 로깅 인터셉터 [출처](https://egkatzioura.com/2021/06/21/keeping-track-of-requests-and-responses-on-spring-webflux/) 참고 후 수정
 *
 * @author : LN
 * @since : 2023. 4. 11.
 */
class LoggingWebFilter(
    private val requestMappingHandlerMapping: RequestMappingHandlerMapping,
) : WebFilter {

    private val logger = KotlinLogging.logger { }

    private var excludePathPatterns: List<PathPattern>? = null

    private var excludeResponseBodyLoggingPathPatterns: List<PathPattern>? = null

    private var onlyTracePathPatterns: List<PathPattern>? = null

    init {
        if (logger.isDebugEnabled()) {

            val parser = PathPatternParser.defaultInstance

            // 로깅 제외 PathPattern
            val excludePathList: LinkedList<PathPattern> = LinkedList()
            excludePathList.add(parser.parse("/swagger-ui/**"))
            excludePathList.add(parser.parse("/static/doc/**"))
            this.excludePathPatterns = excludePathList

            // Body 로깅 제외 PathPattern
            val excludeBodyResponseLogPathList: LinkedList<PathPattern> = LinkedList()
            excludeBodyResponseLogPathList.add(parser.parse("/docs"))
            excludeBodyResponseLogPathList.add(parser.parse("/static/**"))
            this.excludeResponseBodyLoggingPathPatterns = excludeBodyResponseLogPathList

            // LoggingWebFilter 로깅 레벨이 trace 인 경우에만 로깅할 PathPattern
            val onlyTracePathList: LinkedList<PathPattern> = LinkedList()
            this.onlyTracePathPatterns = onlyTracePathList
        }
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {

        if (!logger.isDebugEnabled()) {
            return chain.filter(exchange)
        }

        if (!logger.isTraceEnabled() && exchange.request.method == HttpMethod.OPTIONS) {
            // OPTIONS Method (preflight) 는 trace 레벨 일때만 로깅
            return chain.filter(exchange)
        }

        val requestPath = exchange.request.path
        val path = requestPath.value()
        val contextPath = requestPath.contextPath().value()
        val pathWithoutContext = path.substring(path.indexOf(contextPath) + contextPath.length)

        if (match(excludePathPatterns!!, pathWithoutContext)) {
            return chain.filter(exchange)
        }

        if (!logger.isTraceEnabled() && match(onlyTracePathPatterns!!, pathWithoutContext)) {
            return chain.filter(exchange)
        }

        initExchange(exchange)
        val controller = WebServerUtils.getController(exchange, "")
        val captureResponseBody = !match(excludeResponseBodyLoggingPathPatterns!!, pathWithoutContext)
        val loggingDecorator = LoggingDecorator(exchange, captureResponseBody)

        return chain.filter(loggingDecorator).doOnRequest { _: Long ->
            val request: LoggingRequest = loggingDecorator.request
            val uri = request.uri
            val url = if (uri.rawQuery == null) {
                uri.rawPath
            } else {
                uri.rawPath + "?" + uri.rawQuery
            }
            val remoteAddress = request.remoteAddress?.address
            if (!logger.isTraceEnabled()) {
                logger.debug { "[${remoteAddress?.hostAddress}] ${request.method.name()} \"${url}\" $controller" }
            } else {
                logger.trace {
                    "[${remoteAddress?.hostAddress}] ${request.method.name()} \"${url}\" $controller, HEADER: ${
                        getHeader(request.headers)
                    }"
                }
            }
        }.doOnSuccess { _: Void? ->
            val response: LoggingResponse = loggingDecorator.response
            var fullBody = response.getFullBody()
            fullBody = if (fullBody.isEmpty()) "" else " BODY: $fullBody"
            if (!logger.isTraceEnabled()) {
                logger.debug { " > ${response.statusCode.value()}$fullBody" }
            } else {
                logger.trace { " > ${response.statusCode.value()}$fullBody, HEADER: ${getHeader(response.headers)}" }
            }
        }
    }

    /**
     * [참고](https://stackoverflow.com/a/72351644)
     *
     * @param exchange
     */
    @Suppress("ReactiveStreamsUnusedPublisher")
    private fun initExchange(exchange: ServerWebExchange) {

        // if not to call - then exchange.attributes will be empty
        requestMappingHandlerMapping.getHandler(exchange)
    }

    /**
     * @param httpHeaders
     * @return
     */
    private fun getHeader(httpHeaders: HttpHeaders): String {
        val headerMap: MutableMap<String, Any?> = HashMap()
        for (key in httpHeaders.keys) {
            val headerValues = httpHeaders[key]
            if (headerValues != null && headerValues.size > 1) {
                headerMap[key] = headerValues
            } else {
                headerMap[key] = httpHeaders.getFirst(key)
            }
        }
        return DataUtils.toJson(headerMap)
    }

    /**
     * @param pathList
     * @param checkPath
     * @return
     */
    private fun match(pathList: List<PathPattern>, checkPath: String): Boolean {
        for (excludePath in pathList) {
            if (excludePath.matches(PathContainer.parsePath(checkPath))) {
                return true
            }
        }
        return false
    }

    /**
     * @param dataBuffer
     * @return
     */
    private fun toByteBuffer(dataBuffer: DataBuffer): ByteBuffer {

        val byteBuffer = ByteBuffer.allocate(dataBuffer.readableByteCount())
        dataBuffer.toByteBuffer(byteBuffer)
        return byteBuffer
    }

    /**
     * Logging decorator
     */
    inner class LoggingDecorator(exchange: ServerWebExchange, captureResponseBody: Boolean) :
        ServerWebExchangeDecorator(exchange) {

        private var loggingRequest: LoggingRequest
        private var loggingResponse: LoggingResponse

        init {
            loggingRequest = LoggingRequest(exchange.request)
            loggingResponse = LoggingResponse(exchange.response, captureResponseBody)
        }

        /**
         * @return
         */
        override fun getRequest(): LoggingRequest {
            return loggingRequest
        }

        /**
         * @return
         */
        override fun getResponse(): LoggingResponse {
            return loggingResponse
        }
    }

    /**
     * Logging request
     */
    inner class LoggingRequest(delegate: ServerHttpRequest) : ServerHttpRequestDecorator(delegate) {

        private var remainBytes: AtomicLong? = null
        private var fullBody: StringBuilder? = null

        init {
            val contentLength = delegate.headers.contentLength
            if (contentLength > -1) {
                remainBytes = AtomicLong(contentLength)
                fullBody = StringBuilder()
            }
        }

        /**
         * @return
         */
        override fun getBody(): Flux<DataBuffer> {

            return super.getBody().publishOn(Schedulers.boundedElastic()).doOnNext { dataBuffer: DataBuffer ->
                try {
                    ByteArrayOutputStream().use { stream ->
                        Channels.newChannel(stream).write(toByteBuffer(dataBuffer))
                        val streamBytes = stream.toByteArray()
                        if (streamBytes.isNotEmpty()) {
                            val body = String(stream.toByteArray(), StandardCharsets.UTF_8)
                            fullBody!!.append(body)
                            if (remainBytes!!.addAndGet((-1 * streamBytes.size).toLong()) <= 0) {
                                // Content-Length 헤더값 확인해서 한번만 로깅
                                if (!logger.isTraceEnabled()) {
                                    logger.debug { " < BODY: ${fullBody.toString()}" }
                                } else {
                                    logger.trace { " < BODY: ${fullBody.toString()}" }
                                }
                            }
                        }
                    }
                } catch (e: IOException) {
                    logger.warn(e) { "error occurred while get request body." }
                }
            }
        }
    }

    /**
     * Logging response
     */
    inner class LoggingResponse(delegate: ServerHttpResponse, captureResponseBody: Boolean) :
        ServerHttpResponseDecorator(delegate) {

        private var fullBody: StringBuilder?

        init {
            fullBody = if (captureResponseBody) {
                StringBuilder()
            } else {
                null
            }
        }

        /**
         * @param body
         * @return
         */
        override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {

            if (fullBody != null) {
                val buffer = Flux.from(body)
                return super.writeWith(buffer.doOnNext(this::capture))
            }
            return super.writeWith(body)
        }

        /**
         * @param buffer
         */
        private fun capture(buffer: DataBuffer) {

            if (fullBody != null) {
                fullBody!!.append(StandardCharsets.UTF_8.decode(toByteBuffer(buffer)))
            }
        }

        /**
         * @return
         */
        fun getFullBody(): String {

            return if (fullBody != null) {
                fullBody.toString()
            } else {
                ""
            }
        }
    }
}
