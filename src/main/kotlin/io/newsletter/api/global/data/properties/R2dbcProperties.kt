package io.newsletter.api.global.data.properties

import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * [R2dbcAutoConfiguration] 을 exclude 하고 사용하기 위한 Config Class
 * - 여러개의 DB 를 사용하기 위함
 *
 * @author : LN
 * @since : 2023. 4. 7.
 */
@ConfigurationProperties("r2dbc")
data class R2dbcProperties(

    var profiles: MutableList<org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties> = ArrayList(),
) {

    /**
     * r2dbc > profiles > name 으로 R2dbcProperties 찾아서 반환
     *
     * @param name
     * @return
     */
    fun getByName(name: String): org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties {
        profiles.find { it.name == name }?.let {
            return it
        }
        throw IllegalArgumentException("R2dbcProperties not found by name: $name")
    }

}
