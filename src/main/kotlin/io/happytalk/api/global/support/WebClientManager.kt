package io.happytalk.api.global.support

import io.github.oshai.kotlinlogging.KotlinLogging
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.Connection
import reactor.netty.http.client.HttpClient

/**
 * WebClient 생성, 로깅 관련 공통로직 수행 Manager
 *
 * @author : LN
 * @since : 2023. 4. 17.
 */
@Component
class WebClientManager {

    private val logger = KotlinLogging.logger { }

    /**
     * @param baseUrl 스키마 포함된 사용할 Base URL
     * @return connect, read, write timeout 을 10초로 설정한 [WebClient] 반환
     */
    fun create(baseUrl: String): WebClient {

        return create(baseUrl, 10, 10, 10)
    }

    /**
     * @param baseUrl 스키마 포함된 사용할 Base URL
     * @param connectTimeout 단위 초
     * @param readTimeout 단위 초
     * @param writeTimeout 단위 초
     * @return [WebClient]
     */
    fun create(baseUrl: String, connectTimeout: Int, readTimeout: Int, writeTimeout: Int): WebClient {

        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout * 1000)
            .doOnConnected { conn: Connection ->
                conn
                    .addHandlerLast(ReadTimeoutHandler(readTimeout))
                    .addHandlerLast(WriteTimeoutHandler(writeTimeout))
            }
        val connector = ReactorClientHttpConnector(httpClient)
        val exchangeStrategies = ExchangeStrategies.builder()
            .codecs { config: ClientCodecConfigurer ->
                config.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)
            }
            .build()

        val builder = WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(connector)
            .filter { req: ClientRequest, next: ExchangeFunction ->
                next.exchange(
                    ClientRequest.from(req)
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "MBI-WebClient/1.0.0")
                        .build()
                )
            }

        if (logger.isDebugEnabled()) {
            builder
                .filter(ExchangeFilterFunction.ofRequestProcessor { clientRequest: ClientRequest ->
                    if (logger.isDebugEnabled()) {
                        logger.debug { "[C] > ${clientRequest.method()} ${clientRequest.url()}" }
                        if (logger.isTraceEnabled()) {
                            clientRequest.headers().forEach { name: String, values: List<String> ->
                                values.forEach { value ->
                                    logger.debug { " > $name : $value" }
                                }
                            }
                        }
                    }
                    Mono.just(clientRequest)
                })
                .filter(ExchangeFilterFunction.ofResponseProcessor { clientResponse: ClientResponse ->
                    if (logger.isDebugEnabled()) {
                        logger.debug { "[C] < ${clientResponse.statusCode().value()}" }
                        if (logger.isTraceEnabled()) {
                            clientResponse.headers().asHttpHeaders().forEach { name: String, values: List<String> ->
                                values.forEach { value ->
                                    logger.trace { "$name : $value" }
                                }
                            }
                        }
                    }
                    Mono.just(clientResponse)
                })
        }

        return builder
            .exchangeStrategies(exchangeStrategies)
            .build()
    }
}
