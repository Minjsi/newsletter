package io.happytalk.api

import io.happytalk.api.global.support.FullBeanNameGenerator
import io.happytalk.api.global.util.LogUtils
import io.happytalk.api.local.support.LocalPackageExcludeFilter
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration
import org.springframework.boot.context.TypeExcludeFilter
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.context.annotation.FilterType

/**
 * @author : LN
 * @since : 2023. 3. 2.
 */
@ComponentScan(
    excludeFilters = [
        Filter(
            type = FilterType.CUSTOM,
            classes = [
                TypeExcludeFilter::class,
                AutoConfigurationExcludeFilter::class,
                LocalPackageExcludeFilter::class
            ]
        )
    ],
    nameGenerator = FullBeanNameGenerator::class
)

@ConfigurationPropertiesScan(
    basePackages = [
        "io.happytalk"
    ]
)

@EnableAutoConfiguration(
    exclude = [
        R2dbcAutoConfiguration::class,
        ReactiveUserDetailsServiceAutoConfiguration::class
    ]
)

@SpringBootConfiguration
class HappytalkBackendApplication

fun main(args: Array<String>) {

    LogUtils.systemPropertiesLogging()

    // Thank you for using jOOQ 메시지 제거
    System.setProperty("org.jooq.no-logo", "true")
    // jOOQ tip of the day 메시지 제거
    System.setProperty("org.jooq.no-tips", "true")

    // Spring Boot Application 실행
    runApplication<HappytalkBackendApplication>(*args)
}
