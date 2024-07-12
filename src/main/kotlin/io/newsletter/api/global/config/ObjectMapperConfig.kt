package io.newsletter.api.global.config

import io.happytalk.api.global.util.DataUtils
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Spring Web 에서 사용되는 기본 ObjectMapper 설정
 */
@Configuration
class ObjectMapperConfig {

    @Bean
    fun objectMapperBuilderCustomizer(): Jackson2ObjectMapperBuilderCustomizer {

        return Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder
                .featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .modulesToInstall(DataUtils.createKotlinModule())
        }
    }
}
