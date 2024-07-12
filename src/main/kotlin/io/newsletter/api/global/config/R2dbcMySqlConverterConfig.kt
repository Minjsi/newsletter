package io.newsletter.api.global.config

import io.happytalk.api.global.support.R2dbcManager
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.domain.EntityScanner
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter
import org.springframework.data.r2dbc.dialect.MySqlDialect
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext
import org.springframework.data.relational.RelationalManagedTypes
import org.springframework.data.relational.core.mapping.DefaultNamingStrategy
import org.springframework.data.relational.core.mapping.NamingStrategy
import org.springframework.data.relational.core.mapping.Table

/**
 * Spring Data Repository 가 아닌 DatabaseClient 에서 Entity Mapping 시 사용할 MappingR2dbcConverter Bean 설정
 * - R2dbcAutoConfiguration exclude 후 수동 설정
 * - [org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration] 일부 취합
 * - ZonedDateTime 관련 컨버터 추가 [R2dbcManager.customConversions] 참고
 *
 * @since : 2023. 5. 15.
 */
@Configuration
class R2dbcMySqlConverterConfig {

    private val dialect = MySqlDialect.INSTANCE

    @Bean
    fun r2dbcConverter(
        applicationContext: ApplicationContext,
        namingStrategy: ObjectProvider<NamingStrategy>,
    ): MappingR2dbcConverter {

        val relationalManagedTypes = RelationalManagedTypes.fromIterable(
            EntityScanner(applicationContext).scan(Table::class.java)
        )
        val r2dbcCustomConversions = R2dbcManager.customConversions(dialect)
        val r2dbcMappingContext = R2dbcMappingContext(
            namingStrategy.getIfAvailable { DefaultNamingStrategy.INSTANCE }
        )
        r2dbcMappingContext.setSimpleTypeHolder(r2dbcCustomConversions.simpleTypeHolder)
        r2dbcMappingContext.setManagedTypes(relationalManagedTypes)

        return MappingR2dbcConverter(r2dbcMappingContext, r2dbcCustomConversions)
    }

}
