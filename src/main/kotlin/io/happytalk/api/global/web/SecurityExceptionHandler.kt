package io.happytalk.api.global.web

import io.happytalk.api.global.data.ErrorResponse
import io.happytalk.api.global.util.DataUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * @author : LN
 * @since : 2022. 8. 2.
 */
@Component
class SecurityExceptionHandler : ServerAccessDeniedHandler, ServerAuthenticationEntryPoint {

    private val logger = KotlinLogging.logger { }

    /**
     * @param exchange
     * @param denied
     * @return
     */
    override fun handle(exchange: ServerWebExchange, denied: AccessDeniedException): Mono<Void> {

        return securityErrorResponse(HttpStatus.FORBIDDEN, exchange, denied)
    }

    /**
     * @param exchange
     * @param ex
     * @return
     */
    override fun commence(exchange: ServerWebExchange, ex: AuthenticationException): Mono<Void> {

        val responseStatus =
            if (ex is AuthenticationCredentialsNotFoundException) {
                HttpStatus.UNAUTHORIZED
            } else {
                HttpStatus.FORBIDDEN
            }
        return securityErrorResponse(responseStatus, exchange, ex)
    }

    /**
     * @param responseCode
     * @param exchange
     * @param throwable
     * @return
     */
    private fun securityErrorResponse(
        responseCode: HttpStatus, exchange: ServerWebExchange,
        throwable: Throwable,
    ): Mono<Void> {

        if (logger.isDebugEnabled()) {
            logger.debug { "status: ${responseCode.value()}, errorMessage: ${throwable.message}" }
        }

        SecurityContextHolder.clearContext()

        val response = exchange.response
        response.headers.contentType = MediaType.APPLICATION_JSON
        response.statusCode = responseCode
        return response.writeWith(
            Flux.just(
                response.bufferFactory().wrap(
                    DataUtils.toJsonBytes(
                        ErrorResponse(responseCode.value()).apply {
                            success = false
                            errorMessage = throwable.message
                        }
                    )
                )
            )
        )
    }

}
