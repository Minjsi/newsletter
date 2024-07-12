package io.newsletter.api.global.exception

open class HappytalkException : RuntimeException {

    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
