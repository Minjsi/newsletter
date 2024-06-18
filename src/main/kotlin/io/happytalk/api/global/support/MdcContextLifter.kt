package io.happytalk.api.global.support

import org.reactivestreams.Subscription
import org.slf4j.MDC
import reactor.core.CoreSubscriber
import reactor.util.context.Context
import java.util.stream.Collectors

/**
 * Helper that copies the state of Reactor [Context] to MDC on the #onNext function.
 */
class MdcContextLifter<T>(private val coreSubscriber: CoreSubscriber<T>) : CoreSubscriber<T> {

    override fun onSubscribe(s: Subscription) {
        coreSubscriber.onSubscribe(s)
    }

    override fun onNext(t: T) {
        currentContext().copyToMdc()
        coreSubscriber.onNext(t)
    }

    override fun onError(t: Throwable?) {
        currentContext().copyToMdc()
        coreSubscriber.onError(t)
    }

    override fun onComplete() {
        coreSubscriber.onComplete()
    }

    override fun currentContext() = coreSubscriber.currentContext()

    /**
     * Extension function for the Reactor [Context]. Copies the current context to the MDC, if context is empty clears the MDC.
     * State of the MDC after calling this method should be same as Reactor [Context] state.
     * One thread-local access only.
     */
    private fun Context.copyToMdc() {
        if (!this.isEmpty) {
            val map: Map<String, String> = this.stream()
                .collect(Collectors.toMap({ e -> e.key.toString() }, { e -> e.value.toString() }))
            MDC.setContextMap(map)
        } else {
            MDC.clear()
        }
    }
}
