package io.happytalk.api.global.document

/**
 * @author : LN
 * @since : 2022. 8. 2.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Description(

    val value: String = "",
)
