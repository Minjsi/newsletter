package io.newsletter.api.global.config

import io.happytalk.api.global.data.properties.R2dbcProperties
import io.happytalk.api.global.support.R2dbcManager
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.support.DefaultTransactionDefinition


/**
 * 개별 R2DBC 설정, Bean 등록 클래스
 * - R2dbcAutoConfiguration exclude 후 수동 설정
 * - [org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration] 일부
 *
 * @since : 2023. 3. 2.
 */
@Configuration
@EnableR2dbcRepositories(
    basePackages = [
        "io.happytalk.api.global.repository.cud",
    ],
    entityOperationsRef = "cudEntityOperations"
)
class CudR2dbcConfig(private val r2dbcProperties: R2dbcProperties) {

    @Bean("cudConnectionFactory")
    fun connectionFactory(): ConnectionFactory {
        return R2dbcManager.connectionFactory(r2dbcProperties.getByName("mobile_cs"))
    }

    @Bean("cudDatabaseClient")
    fun databaseClient(
        @Qualifier("cudConnectionFactory") connectionFactory: ConnectionFactory,
    ): DatabaseClient {
        return DatabaseClient.create(connectionFactory)
    }

    @Bean("cudEntityOperations")
    fun r2dbcEntityOperations(
        @Qualifier("cudDatabaseClient") databaseClient: DatabaseClient,
    ): R2dbcEntityOperations {
        return R2dbcManager.entityOperations(databaseClient)
    }

    @Bean("cudEntityTemplate")
    fun r2dbcEntityTemplate(
        @Qualifier("cudEntityOperations") r2dbcEntityOperations: R2dbcEntityOperations,
    ): R2dbcEntityTemplate {
        return r2dbcEntityOperations as R2dbcEntityTemplate
    }

    @Bean("cudTransactionManager")
    fun r2dbcTransactionManager(
        @Qualifier("cudConnectionFactory") connectionFactory: ConnectionFactory,
    ): R2dbcTransactionManager {
        return R2dbcTransactionManager(connectionFactory)
    }

    /**
     * [Using the TransactionalOperator](https://docs.spring.io/spring-framework/docs/6.0.8/reference/html/data-access.html#tx-prog-operator)
     *
     * @param transactionManager
     * @return
     */
    @Bean("cudTransactionalOperator")
    fun transactionalOperator(
        @Qualifier("cudTransactionManager") transactionManager: R2dbcTransactionManager,
    ): TransactionalOperator {
        val definition = DefaultTransactionDefinition()
        definition.timeout = 30
        return TransactionalOperator.create(transactionManager, definition)
    }
}
