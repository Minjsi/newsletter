package io.happytalk.api.local.data

import io.happytalk.api.global.document.Description
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import kotlin.time.DurationUnit

/**
 * @author : LN
 * @since : 2023. 4. 13.
 */
data class CoroutineRequest(

    @field:Schema(description = "작업당 지연 시간", requiredMode = RequiredMode.REQUIRED, example = "2")
    val delay: Int,

    @field:Schema(description = "지연 시간값의 단위", requiredMode = RequiredMode.REQUIRED, example = "SECONDS")
    val timeUnit: TimeUnit = TimeUnit.SECONDS,

    @field:Schema(description = "지연 작업 반복 횟수. 입력하지 않으면 기본값: 100", example = "100")
    val loop: Int? = 100,
) {

    /**
     * delay 시간 단위
     */
    @Suppress("unused")
    enum class TimeUnit(
        val durationUnit: DurationUnit,
    ) {
        @Description("밀리초")
        MILLISECONDS(DurationUnit.MILLISECONDS),

        @Description("초")
        SECONDS(DurationUnit.SECONDS),

        @Description("분")
        MINUTES(DurationUnit.MINUTES),
    }
}
