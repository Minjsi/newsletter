package io.happytalk.api.global.support

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.proxy.ProxyConnectionFactory
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.CustomConversions
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.dialect.DialectResolver
import org.springframework.data.r2dbc.dialect.R2dbcDialect
import org.springframework.r2dbc.core.DatabaseClient
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 *
 * @author : LN
 * @since : 2023. 3. 2.
 */
object R2dbcManager {

    private val converters: MutableList<Converter<*, *>> = ArrayList()

    init {
        converters.add(LocalDateTimeToZonedDateTimeReadingConverter())
        converters.add(ZonedDateTimeToLocalDateTimeWritingConverter())
    }

    /**
     *
     * @param r2dbcProperties
     * @return
     */
    fun connectionFactory(r2dbcProperties: R2dbcProperties): ConnectionFactory {

        // ConnectionFactory
        val connectionFactory = ConnectionFactories.get(
            ConnectionFactoryOptions.parse(r2dbcProperties.url)
                .mutate()
                .option(ConnectionFactoryOptions.USER, r2dbcProperties.username)
                .option(ConnectionFactoryOptions.PASSWORD, r2dbcProperties.password)
                .build()
        )

        // ConnectionPool
        val pool = r2dbcProperties.pool
        val connectionPool = ConnectionPool(
            ConnectionPoolConfiguration.builder(connectionFactory)
                .maxIdleTime(pool.maxIdleTime)
                .maxLifeTime(pool.maxLifeTime)
                .maxAcquireTime(pool.maxAcquireTime)
                .maxCreateConnectionTime(pool.maxCreateConnectionTime)
                .initialSize(pool.initialSize)
                .maxSize(pool.maxSize)
                .validationQuery(pool.validationQuery)
                .validationDepth(pool.validationDepth)
                .build()
        )

        // ProxyConnectionFactory
        return ProxyConnectionFactory.builder(connectionPool)
            .listener(R2dbcLoggingListener())
            .build()
    }

    /**
     *
     * @param databaseClient
     * @return
     */
    fun entityOperations(databaseClient: DatabaseClient): R2dbcEntityOperations {

        val dialect = DialectResolver.getDialect(databaseClient.connectionFactory)
        return R2dbcEntityTemplate(databaseClient, DefaultReactiveDataAccessStrategy(dialect, converters))
    }

    /**
     *
     * @param dialect
     * @return
     */
    fun customConversions(dialect: R2dbcDialect): R2dbcCustomConversions {

        val converters: MutableList<Any> = ArrayList(dialect.converters)
        converters.addAll(R2dbcCustomConversions.STORE_CONVERTERS)
        return R2dbcCustomConversions(
            CustomConversions.StoreConversions.of(dialect.simpleTypeHolder, converters), this.converters
        )
    }

    /**
     *
     */
    @ReadingConverter
    internal class LocalDateTimeToZonedDateTimeReadingConverter : Converter<LocalDateTime, ZonedDateTime> {

        private val zoneId = ZoneId.systemDefault()

        override fun convert(source: LocalDateTime): ZonedDateTime {
            return source.atZone(zoneId)
        }
    }

    /**
     *
     */
    @WritingConverter
    internal class ZonedDateTimeToLocalDateTimeWritingConverter : Converter<ZonedDateTime, LocalDateTime> {

        override fun convert(source: ZonedDateTime): LocalDateTime {
            return source.toLocalDateTime()
        }
    }
}
