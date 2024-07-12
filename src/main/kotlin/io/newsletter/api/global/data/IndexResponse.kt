package io.newsletter.api.global.data

/**
 * @author : LN
 * @since : 2023. 3. 2.
 */
class IndexResponse : DefaultResponse() {

    var remoteAddr: String? = null
    var localAddr: String? = null
    var requestHeaders: Map<String, String>? = null
}
