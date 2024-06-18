package io.happytalk.api.local.controller

import io.happytalk.api.global.document.OpenApiTags
import io.happytalk.api.local.data.CoroutineRequest
import io.happytalk.api.local.data.CoroutineResponse
import io.happytalk.api.local.data.CoroutineResponse.CoroutineTaskResult
import io.happytalk.api.local.repository.read.CoroutineReadTestRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.ZonedDateTime
import kotlin.system.measureTimeMillis
import kotlin.time.Duration
import kotlin.time.toDuration

@RestController
class CoroutineController(
    private val coroutineReadTestRepository: CoroutineReadTestRepository,
) {

    private val logger = KotlinLogging.logger { }

    @Operation(
        tags = [OpenApiTags.LOCAL_EXAMPLE],
        summary = "Coroutine 사용 예제",
        description = "`Kotlin` `Coroutine` 을 사용한 비동기 작업 예시\n" +
                "* 일련의 작업(우리가 제공하는 API endpoint 등) 에서" +
                " `delay` (`timeUnit`) 만큼의 시간동안 `지연이 발생하는 작업`(DB 조회, API 호출 등)이" +
                " `loop` 만큼 반복된다고 가정했을때 소요된 `총 시간` 확인.\n" +
                "* 만약 `동기` 방식이면 `시간 * 반복 횟수` 만큼의 시간이 걸려야 한다.\n" +
                "* `비동기` 방식이기 때문에 지연 작업들 중 가장 오래걸린 작업의 시간(+a)이 총 소요시간이 됨을 확인할 수 있다.\n" +
                "* 결험이 없다면 익숙치 않은 코드 스타일인 [Reactor](https://projectreactor.io/docs/core/release/reference/)" +
                " Mono, Flux, map, flatMap... 등을 학습하지 않아도 `Node.js` 의 `Promise` `async` `await` 와 비슷한 개념 & 스타일로 코딩 가능.\n" +
                "  * 신규 프로젝트에 Kotlin 을 선택한 이유 중 하나."
    )
    @PostMapping("/p/example/coroutine")
    suspend fun coroutineTest(
        @RequestBody coroutineRequest: CoroutineRequest,
    ): CoroutineResponse = withContext(MDCContext()) { // CoroutineScope
        // 시작 시간 마킹
        val startTime = ZonedDateTime.now()
        logger.info { "- Starting... [delay: ${coroutineRequest.delay}, timeUnit: ${coroutineRequest.timeUnit}, loop: ${coroutineRequest.loop}]" }

        val delayDuration = coroutineRequest.delay.toDuration(coroutineRequest.timeUnit.durationUnit)
        val asyncResultList: MutableList<Deferred<CoroutineTaskResult>> = ArrayList()

        // 지연 작업 정의 > async
        for (i in 1..coroutineRequest.loop!!) {
            val asyncResult: Deferred<CoroutineTaskResult> = async(Dispatchers.IO) {
                delayedTask(i, delayDuration)
            }
            asyncResultList.add(asyncResult)
        }

        // 지연 작업 실행 및 결과 반환 > await
        //  > 단순 CoroutineScope 안 비동기 작업 예시용 코드
        //  > 실제로는 여러개의 suspend 함수들을 async 로 실행후 결과 취합하는 부분에서 await 하여 사용하게 됨
        val taskResults = mutableListOf<CoroutineTaskResult>()
        asyncResultList.awaitAll().forEach {
            taskResults.add(it)
        }
        val exampleRecordCount = async(Dispatchers.IO) {
            coroutineReadTestRepository.count()
        }.await()

        // 종료 시간 마킹
        val endTime = ZonedDateTime.now()
        val totalTimeMillis = endTime.toInstant().toEpochMilli() - startTime.toInstant().toEpochMilli()
        logger.info { "- Finished delayed task(s). [totalTimeMillis: $totalTimeMillis, taskResults: $taskResults, exampleRecordCount: $exampleRecordCount]" }

        val response = CoroutineResponse().apply {
            this.totalDurationMillis = totalTimeMillis
            this.taskResults = taskResults
            message =
                "${coroutineRequest.delay} ${coroutineRequest.timeUnit} delay 작업을 ${coroutineRequest.loop}번 반복 했으니 " +
                        "'동기' 방식 이라면 총 ${delayDuration.inWholeMilliseconds * coroutineRequest.loop}+ ms 가 소요되어야 함. " +
                        "실제 소요 시간: $totalTimeMillis ms. "
        }
        response
    }

    /**
     * Delay test
     *
     * @param jobSequence
     * @param duration
     */
    private suspend fun delayedTask(jobSequence: Int, duration: Duration): CoroutineTaskResult {

        val startTime = ZonedDateTime.now()
        logger.warn { " > [$jobSequence] delayedTask: start" }

        val durationMillis = measureTimeMillis { delay(duration) }

        val endTime = ZonedDateTime.now()
        logger.warn { " > [$jobSequence] delayedTask: end" }

        return CoroutineTaskResult(
            jobSequence,
            startTime,
            endTime,
            durationMillis,
            Thread.currentThread().name
        )
    }

}
