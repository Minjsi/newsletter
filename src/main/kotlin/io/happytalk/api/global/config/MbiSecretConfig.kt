package io.happytalk.api.global.config

import io.happytalk.api.global.support.MbiSecretJasypt
import io.github.cdimascio.dotenv.Dotenv
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author : LN
 * @since : 2023. 4. 19.
 */
@Configuration
class MbiSecretConfig {

    private val mbiSecretJasypt = MbiSecretJasypt()

    @Bean("secretKeyEnvJasypt")
    fun secretKeyEnvDetector(): MbiSecretJasypt {

        return mbiSecretJasypt
    }

    @Bean
    fun dotenv(): Dotenv {

        return io.happytalk.api.global.support.Dotenv.instance()
    }
}
