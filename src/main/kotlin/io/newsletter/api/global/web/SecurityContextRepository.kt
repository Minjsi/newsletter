package io.newsletter.api.global.web

import io.happytalk.api.global.data.Principal
import io.happytalk.api.global.support.JwtManager
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * Spring Security > OAuth2 Resource Server 사용할 수 있으나 구조가 불필요하게 복잡 하다고 판단
 *
 * 아래 내용들을 여기서 처리
 *
 * - Bearer 헤더 파싱
 * - JWT 토큰 검증
 * - 사용자 검증
 * - 사용자 권한 부여 후 SecurityContext 반환
 *
 * @author : LN
 * @since : 2023. 4. 17.
 */
@Component
class SecurityContextRepository(
    private val jwtManager: JwtManager,
) : ServerSecurityContextRepository {

    private val logger = KotlinLogging.logger { }

    private val authorizationPrefix = "Bearer "

    // TODO - : JWT 토큰에 인증 정보(권한 등) 을 넣고 검증할 필요가 없다면 굳이 필요 없음
    private val authoritiesKey = "authorities"

    /**
     * @param serverWebExchange
     * @return
     */
    override fun load(serverWebExchange: ServerWebExchange): Mono<SecurityContext> {

        val authorizationHeader = serverWebExchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        if (authorizationHeader == null || !authorizationHeader.startsWith(authorizationPrefix)) {
            return Mono.empty()
        }
        val token = authorizationHeader.replaceFirst(authorizationPrefix.toRegex(), "")

        return authenticate(token)
            .map { authentication: Authentication ->
                SecurityContextImpl(
                    authentication
                )
            }
    }

    /**
     * @param serverWebExchange
     * @param securityContext
     * @return
     */
    override fun save(serverWebExchange: ServerWebExchange, securityContext: SecurityContext): Mono<Void> {

        if (logger.isDebugEnabled()) {
            logger.warn { "Not support method" }
        }
        return Mono.empty()
    }

    /**
     * @param token
     * @return
     */
    private fun authenticate(token: String): Mono<Authentication> {

        try {
            val claims: Claims = jwtManager.parseToken(token)
            val username = claims.subject

            // TODO - JWT 토큰 정보 통해 실제 DB 에 존재하는지, 토큰의 Role 이 DB 와 동일한지 와 같은 사용자 검증 처리
            //      - 최소한 외부 IO 에 대한 처리는 비동기로 수행
            //      - 아래 ROLE 은 예시로 넣은 값
            logger.warn { "Implements here. example authorities add to : [username: $username]" }
            claims[authoritiesKey] = "ROLE_USER, ROLE_ADMIN"

            val authoritiesClaim = claims[authoritiesKey]
            val authorities: Collection<GrantedAuthority> = if (authoritiesClaim == null) {
                AuthorityUtils.NO_AUTHORITIES
            } else {
                AuthorityUtils.commaSeparatedStringToAuthorityList(
                    authoritiesClaim.toString()
                )
            }

            // principal, authorities 를 SecurityContext 에 넘겨주는 역할이 "목적"
            // > 클래스 명 따윈 신경 끄자
            val authentication = UsernamePasswordAuthenticationToken(
                // > 꼭 Principal 일 필요는 없다. 변경이 필요하다 판단되면 고민하지 말고 바꾸자
                Principal(
                    claims = claims
                ),
                token,
                authorities
            )
            SecurityContextHolder.getContext().authentication = authentication

            return Mono.just(authentication)
        } catch (e: Exception) {
            logger.warn { "authentication failed: [token: $token, error: ${e.message}]" }
            return Mono.empty()
        }
    }
}
