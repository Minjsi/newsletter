package io.happytalk.api.global.support

import io.r2dbc.spi.ConnectionFactory
import org.jooq.ConverterProvider
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.DefaultConfiguration
import org.jooq.impl.DefaultConverterProvider
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 *
 *
 * @author : LN
 * @since : 2023. 6. 9.
 */
object JooqManager {

    private val converterProvider = CustomConverterProvider()

    /**
     *
     * @param connectionFactory
     * @return
     */
    fun dslContext(connectionFactory: ConnectionFactory): DSLContext {

        val configuration = DefaultConfiguration()
        val converterProvider = converterProvider
        configuration.set(connectionFactory)
        configuration.set(SQLDialect.MYSQL)
        configuration.set(converterProvider)
        return DSL.using(configuration)
    }

    /**
     *
     */
    internal class ZonedDateTimeConverter : org.jooq.Converter<LocalDateTime, ZonedDateTime> {

        override fun from(databaseObject: LocalDateTime): ZonedDateTime? {
            return databaseObject.atZone(ZoneId.systemDefault())
        }

        override fun to(userObject: ZonedDateTime): LocalDateTime {
            return userObject.toLocalDateTime()
        }

        override fun fromType(): Class<LocalDateTime> {
            return LocalDateTime::class.java
        }

        override fun toType(): Class<ZonedDateTime> {
            return ZonedDateTime::class.java
        }
    }

    /**
     *
     */
    internal class CustomConverterProvider : ConverterProvider {

        private val defaultConverterProvider = DefaultConverterProvider()
        private val zonedDateTimeConverter = ZonedDateTimeConverter()

        @Suppress("UNCHECKED_CAST")
        override fun <T : Any?, U : Any?> provide(tType: Class<T>, uType: Class<U>?): org.jooq.Converter<T, U>? {
            if (tType == LocalDateTime::class.java && uType == ZonedDateTime::class.java) {
                return zonedDateTimeConverter as org.jooq.Converter<T, U>
            }
            return defaultConverterProvider.provide(tType, uType)
        }
    }
}
