package io.happytalk.api.global.data.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author : LN
 * @since : 2023. 4. 12.
 */
@ConfigurationProperties("general")
data class GeneralProperties(

    var domain: String = "localhost",
)
