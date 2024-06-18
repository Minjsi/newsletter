package io.happytalk.api.global.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Spring Boot 프로필이 "local" 일때만 사용하기 위한 Config
 * [io.happytalk.api.local]
 *
 * @author : LN
 * @since : 2023. 4. 11.
 */
@ComponentScan(basePackages = ["io.happytalk.api.local"])
@EntityScan(basePackages = ["io.happytalk.api.local.entity"])
@Configuration
@Profile("local")
class LocalProfileConfig
