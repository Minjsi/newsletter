package io.happytalk.api.local.data

import io.happytalk.api.global.data.BaseResponse
import io.happytalk.api.global.document.Description
import io.happytalk.api.local.data.CoroutineResponse.ErrorCode
import io.swagger.v3.oas.annotations.media.Schema
import java.time.ZonedDateTime

/**
 * @author : LN
 * @since : 2023. 4. 13.
 */
class CoroutineResponse : BaseResponse<ErrorCode>() {

    @Schema(description = "전체 작업 경과 시간")
    var totalDurationMillis: Long? = null

    @Schema(description = "작업별 결과 목록")
    var taskResults: List<CoroutineTaskResult>? = null

    /**
     * Error code
     * - BaseResponse 의 errorCode 외 별도 에러코드 사용할 수 있음을 보여주기 위한 예제
     */
    @Suppress("unused")
    enum class ErrorCode {

        @Description("응답별 에러코드 예제 1")
        ERROR_CODE_BY_REQUEST_001,

        @Description("응답별 에러코드 예제 2")
        ERROR_CODE_BY_REQUEST_002,
    }

    /**
     * Coroutine task result
     *
     * @property jobSequence
     * @property startTime
     * @property endTime
     * @property durationMillis
     * @constructor Create empty Coroutine task result
     */
    @Schema(description = "작업 결과")
    data class CoroutineTaskResult(
        @field:Schema(description = "작업 번호")
        val jobSequence: Int,
        @field:Schema(description = "작업 시작 시간")
        val startTime: ZonedDateTime,
        @field:Schema(description = "작업 종료 시간")
        val endTime: ZonedDateTime,
        @field:Schema(description = "작업 소요 시간 (ms)")
        val durationMillis: Long,
        @field:Schema(description = "작업 Thread 이름")
        val threadName: String,
    )
}
