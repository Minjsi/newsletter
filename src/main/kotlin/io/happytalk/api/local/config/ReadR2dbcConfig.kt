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
    basePackages = ["io.happytalk.api.local.repository.read"],
    entityOperationsRef = "readEntityOperations"
)
class ReadR2dbcConfig(private val r2dbcProperties: R2dbcProperties) {

    @Bean("readConnectionFactory")
    fun connectionFactory(): ConnectionFactory {
        return R2dbcManager.connectionFactory(r2dbcProperties.getByName("example_read"))
    }

    @Bean("readTransactionManager")
    fun r2dbcTransactionManager(
        @Qualifier("readConnectionFactory") connectionFactory: ConnectionFactory,
    ): R2dbcTransactionManager {
        return R2dbcTransactionManager(connectionFactory)
    }

    @Bean("readDatabaseClient")
    fun databaseClient(
        @Qualifier("readConnectionFactory") connectionFactory: ConnectionFactory,
    ): DatabaseClient {
        return DatabaseClient.create(connectionFactory)
    }

    @Bean("readEntityOperations")
    fun r2dbcEntityOperations(
        @Qualifier("readDatabaseClient") databaseClient: DatabaseClient,
    ): R2dbcEntityOperations {
        return R2dbcManager.entityOperations(databaseClient)
    }

    @Bean("readEntityTemplate")
    fun r2dbcEntityTemplate(
        @Qualifier("readEntityOperations") r2dbcEntityOperations: R2dbcEntityOperations,
    ): R2dbcEntityTemplate {
        return r2dbcEntityOperations as R2dbcEntityTemplate
    }

    // jOOQ
    @Bean("readDSLContext")
    fun readDSLContext(
        @Qualifier("readConnectionFactory") connectionFactory: ConnectionFactory,
    ): DSLContext {
        return JooqManager.dslContext(connectionFactory)
    }

}
