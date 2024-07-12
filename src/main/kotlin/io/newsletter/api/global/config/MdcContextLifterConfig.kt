package io.newsletter.api.global.config

import io.happytalk.api.global.support.MdcContextLifter
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Hooks
import reactor.core.publisher.Operators

/**
 * MCD Trace ID 관련 설정
 * - [1차 출처](https://github.com/archie-swif/webflux-mdc)
 * - [2차 출처](https://techblog.woowahan.com/2667)
 * - [3차 출처](https://mjin1220.tistory.com/52)
 *
 * @author : LN
 * @since : 2022. 4. 10.
 */
@Configuration
class MdcContextLifterConfig {

    val mdcContextReactorKey: String = io.newsletter.api.global.config.MdcContextLifterConfig::class.java.name

    @PostConstruct
    fun contextOperatorHook() {
        Hooks.onEachOperator(
            mdcContextReactorKey,
            Operators.lift { _, subscriber -> MdcContextLifter(subscriber) }
        )
    }

    @PreDestroy
    fun cleanupHook() {
        Hooks.resetOnEachOperator(mdcContextReactorKey)
    }
}
