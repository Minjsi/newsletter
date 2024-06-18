package io.happytalk.api.global.exception

import io.happytalk.api.global.data.BaseResponse.Code.VALIDATION_FAILED

class ValidationFailedException : BaseResponseException {
    constructor() : super(VALIDATION_FAILED)
    constructor(message: String) : super(VALIDATION_FAILED, message)
    constructor(message: String, cause: Throwable) : super(VALIDATION_FAILED, message, cause)
}