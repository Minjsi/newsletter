package io.happytalk.api.local.config

import io.happytalk.api.global.data.properties.R2dbcProperties
import io.happytalk.api.global.support.JooqManager
import io.happytalk.api.global.support.R2dbcManager
import io.r2dbc.spi.ConnectionFactory
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.core.DatabaseClient


/**
 * R2DBC 설정 클래스
 * - R2dbcAutoConfiguration exclude 후 수동 설정
 *
 * @author : LN
 * @since : 2023. 3. 2.
 */
@Configuration
@EnableR2dbcRepositories(
    basePackages = ["io.happytalk.api.local.repository.write"],
    entityOperationsRef = "writeEntityOperations"
)
class WriteR2dbcConfig(private val r2dbcProperties: R2dbcProperties) {

    @Bean("writeConnectionFactory")
    fun connectionFactory(): ConnectionFactory {
        return R2dbcManager.connectionFactory(r2dbcProperties.getByName("example_write"))
    }

    @Bean("writeDatabaseClient")
    fun databaseClient(
        @Qualifier("writeConnectionFactory") connectionFactory: ConnectionFactory,
    ): DatabaseClient {
        return DatabaseClient.create(connectionFactory)
    }

    @Bean("writeTransactionManager")
    fun r2dbcTransactionManager(
        @Qualifier("writeConnectionFactory") connectionFactory: ConnectionFactory,
    ): R2dbcTransactionManager {
        return R2dbcTransactionManager(connectionFactory)
    }

    @Bean("writeEntityOperations")
    fun r2dbcEntityOperations(
        @Qualifier("writeDatabaseClient") databaseClient: DatabaseClient,
    ): R2dbcEntityOperations {
        return R2dbcManager.entityOperations(databaseClient)
    }

    @Bean("writeEntityTemplate")
    fun r2dbcEntityTemplate(
        @Qualifier("writeEntityOperations") r2dbcEntityOperations: R2dbcEntityOperations,
    ): R2dbcEntityTemplate {
        return r2dbcEntityOperations as R2dbcEntityTemplate
    }

    // jOOQ
    @Bean("writeDSLContext")
    fun writeDSLContext(
        @Qualifier("writeConnectionFactory") connectionFactory: ConnectionFactory,
    ): DSLContext {
        return JooqManager.dslContext(connectionFactory)
    }
}
