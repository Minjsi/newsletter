package io.newsletter.api.global.exception

import io.happytalk.api.global.data.BaseResponse.Code

open class BaseResponseException : HappytalkException {

    var code: Code? = null

    constructor(code: Code) : super() {
        this.code = code
    }

    constructor(code: Code, message: String) : super(message) {
        this.code = code
    }

    constructor(code: Code, message: String, cause: Throwable) : super(message, cause) {
        this.code = code
    }
}
