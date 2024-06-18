package io.happytalk.api.global.config

import io.happytalk.api.global.data.properties.CorsProperties
import io.happytalk.api.global.web.LoggingWebFilter
import io.happytalk.api.global.web.SecurityContextRepository
import io.happytalk.api.global.web.SecurityExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping

/**
 * @author : LN
 * @since : 2022. 8. 2.
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {

    /**
     * Security web filter chain
     * @param http
     * @return
     */
    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity,
        requestMappingHandlerMapping: RequestMappingHandlerMapping,
        securityContextRepository: SecurityContextRepository,
        securityExceptionHandler: SecurityExceptionHandler,
    ): SecurityWebFilterChain {

        return http
            .securityContextRepository(securityContextRepository)
            .addFilterAfter(LoggingWebFilter(requestMappingHandlerMapping), SecurityWebFiltersOrder.LAST)
            .authorizeExchange {
                it.pathMatchers(
                    // index
                    "", "/", "/health_check", "/favicon.ico",

                    // p: public > 인증 없이 접근할 수 있는 endpoint
                    "/p/**",

                    // Static Resource 서빙 위한 static-path-pattern
                    "/static/**",

                    // springdoc-openapi
                    "/docs", "/swagger-ui/**",
                ).permitAll()
                    .anyExchange().authenticated()
            }
            .exceptionHandling {
                it.accessDeniedHandler(securityExceptionHandler)
                it.authenticationEntryPoint(securityExceptionHandler)
            }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .csrf { it.disable() }
            .build()
    }

    /**
     * Cors configuration source
     *
     * @return
     */
    @Bean
    fun corsConfigurationSource(
        corsProperties: CorsProperties,
    ): CorsConfigurationSource {

        val corsConfig = CorsConfiguration().apply {
            addAllowedMethod(HttpMethod.DELETE)
            addAllowedMethod(HttpMethod.GET)
            addAllowedMethod(HttpMethod.PATCH)
            addAllowedMethod(HttpMethod.POST)
            addAllowedMethod(HttpMethod.PUT)
            allowCredentials = true
            allowedOriginPatterns = corsProperties.allowedOriginPatterns
            maxAge = corsProperties.maxAge
            applyPermitDefaultValues()
        }
        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", corsConfig)
        }
        return source
    }

    /**
     * Deprecated 된 해시함수 알고리즘 제외
     * - [PasswordEncoderFactories.createDelegatingPasswordEncoder] 참고
     *
     * @return scrypt 를 기본으로 하는 DelegatingPasswordEncoder 반환
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder {

        /*
        val encoders: MutableMap<String, PasswordEncoder> = HashMap()
        encoders["bcrypt"] = BCryptPasswordEncoder()
        encoders["pbkdf2"] = Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8()
        encoders["scrypt"] = SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8()
        encoders["argon2"] = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()
        return DelegatingPasswordEncoder("scrypt", encoders)
         */
        return BCryptPasswordEncoder()
    }

}
