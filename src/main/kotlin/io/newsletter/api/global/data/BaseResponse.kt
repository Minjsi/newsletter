package io.newsletter.api.global.data

import io.happytalk.api.global.document.Description
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import java.time.ZonedDateTime

/**
 * @author : LN
 * @since : 2023. 3. 2.
 */
open class BaseResponse<E : Enum<*>> {

    @Schema(description = "요청 성공, 실패 여부", requiredMode = RequiredMode.REQUIRED)
    var success: Boolean = true

    @Schema(description = "응답 일시", requiredMode = RequiredMode.REQUIRED)
    var timestamp: ZonedDateTime = ZonedDateTime.now()

    @Schema(description = "응답 메시지")
    var message: String? = null

    @Schema(description = "에러 코드")
    var errorCode: E? = null

    @Schema(description = "에러 메시지")
    var errorMessage: String? = null

    /**
     * Error code
     */
    enum class Code {

        @Description("요청 파라미터 검증 실패")
        VALIDATION_FAILED,

        @Description("분류되지 않은 에러")
        NOT_CLASSIFIED,

        @Description("의도되지 않은 에러 (예외)")
        EXCEPTION
    }
}
