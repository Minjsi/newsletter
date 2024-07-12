package io.newsletter.api.global.web

import io.happytalk.api.global.data.BaseResponse.Code
import io.happytalk.api.global.data.ErrorResponse
import io.happytalk.api.global.util.LogUtils
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.ValidationException
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.SearchStrategy
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.core.codec.DecodingException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.util.StringUtils
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.result.view.ViewResolver
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.net.URI
import java.util.stream.Collectors

/**
 * @author : LN
 * @since : 2022. 8. 23.
 */
@Configuration
class ExceptionHandler(
    private val serverProperties: ServerProperties,
) {

    private val logger = KotlinLogging.logger { }

    /**
     * 전역 WEB 예외 핸들러
     * @return
     */
    @Bean
    @Order(-1)
    @ConditionalOnMissingBean(value = [ErrorWebExceptionHandler::class], search = SearchStrategy.CURRENT)
    fun errorWebExceptionHandler(
        errorAttributes: ErrorAttributes,
        webProperties: WebProperties,
        viewResolvers: ObjectProvider<ViewResolver>,
        serverCodecConfigurer: ServerCodecConfigurer,
        applicationContext: ApplicationContext,
    ): ErrorWebExceptionHandler {

        val exceptionHandler: DefaultErrorWebExceptionHandler = object : DefaultErrorWebExceptionHandler(
            errorAttributes, webProperties.resources, serverProperties.error,
            applicationContext
        ) {
            override fun renderErrorResponse(request: ServerRequest): Mono<ServerResponse> {

                val errorPropertiesMap: Map<String, Any> = getErrorAttributes(
                    request,
                    ErrorAttributeOptions.defaults()
                )
                var statusCode: Int
                var httpStatus: HttpStatus
                try {
                    statusCode = errorPropertiesMap["status"] as Int
                    httpStatus = HttpStatus.valueOf(statusCode)
                } catch (e: Exception) {
                    logger.warn { "could not get 'status' from errorAttributes. ${errorPropertiesMap["status"]}" }
                    httpStatus = HttpStatus.INTERNAL_SERVER_ERROR
                    statusCode = httpStatus.value()
                }

                val throwable: Throwable = getError(request)
                val code: Code? = getErrorCode(throwable)
                val errorMessage = if (code === Code.EXCEPTION) throwable.message else null
                val methodName: String = request.method().name()
                val requestURI: URI = request.uri()
                if (code === Code.EXCEPTION) {
                    logger.error {
                        "$methodName \"$requestURI\" $statusCode : ${LogUtils.getPrintStackTraceLog(throwable)}"
                    }
                } else {
                    logger.warn { "$methodName \"$requestURI\" $statusCode : ${throwable.message}" }
                }

                return ServerResponse.status(httpStatus)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                        BodyInserters.fromValue(
                            ErrorResponse(statusCode).apply {
                                success = false
                                this.errorCode = code
                                this.errorMessage = errorMessage
                            }
                        )
                    )
            }

            /**
             * @param throwable
             * @return
             */
            private fun getErrorCode(throwable: Throwable): Code? {

                if (throwable is ResponseStatusException) {
                    if (throwable.statusCode.value() == 404) {
                        return null
                    }
                }

                if (throwable is ValidationException) {
                    return Code.VALIDATION_FAILED
                }

                if (throwable is WebExchangeBindException) {
                    val reason: String? = throwable.reason
                    if ("Validation failure" == reason) {
                        return Code.VALIDATION_FAILED
                    }
                }

                val cause1 = throwable.cause
                if (cause1 is DecodingException) {
                    val message = cause1.message
                    if (StringUtils.hasText(message)
                        && message!!.contains("not one of the values accepted for Enum class")
                    ) {
                        return Code.VALIDATION_FAILED
                    }
                    val cause2 = cause1.cause
                    if (cause2 is InvalidFormatException) {
                        if (!StringUtils.hasText(cause2.value.toString())) {
                            return Code.VALIDATION_FAILED
                        }
                    }
                }

                return Code.EXCEPTION
            }

            /**
             * @param request
             * @param response
             * @param throwable
             */
            override fun logError(request: ServerRequest, response: ServerResponse, throwable: Throwable) {
                // do nothing
            }

            /**
             * @param errorAttributes
             * @return
             */
            override fun getRoutingFunction(errorAttributes: ErrorAttributes): RouterFunction<ServerResponse> {
                return RouterFunctions.route(
                    RequestPredicates.all()
                ) { request: ServerRequest -> renderErrorResponse(request) }
            }

            /**
             * @param request
             * @param options
             * @return
             */
            override fun getErrorAttributes(
                request: ServerRequest,
                options: ErrorAttributeOptions,
            ): MutableMap<String, Any> {
                return super.getErrorAttributes(request, options)
            }
        }

        exceptionHandler.setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()))
        exceptionHandler.setMessageWriters(serverCodecConfigurer.writers)
        exceptionHandler.setMessageReaders(serverCodecConfigurer.readers)
        return exceptionHandler
    }
}
