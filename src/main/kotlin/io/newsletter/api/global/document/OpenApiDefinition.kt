package io.newsletter.api.global.document

import io.happytalk.api.global.document.OpenApiConstants.JWT
import io.happytalk.api.global.document.OpenApiTags.AUTHENTICATION
import io.happytalk.api.global.document.OpenApiTags.LOCAL_EXAMPLE
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.security.SecuritySchemes
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders


/**
 * @author : LN
 * @since : 2022. 8. 2.
 */
@SecuritySchemes(
    SecurityScheme(
        name = JWT,
        type = SecuritySchemeType.HTTP,
        paramName = HttpHeaders.AUTHORIZATION,
        `in` = SecuritySchemeIn.HEADER,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "* 엑세스 토큰 발급을 통해 얻은 `accessToken` 값을 넣어 사용할 수 있습니다.\n" +
                "* <b>Authorization</b> 헤더에 <b>Bearer header.payload.secret</b> 형태의 값이 추가됩니다."
    )
)
@Configuration
class OpenApiDefinition(
    private val env: Environment,
    @Value("\${general.domain}")
    private val domain: String,
    @Value("\${server.port}")
    private val port: Int,
    @Value("\${spring.webflux.base-path}")
    private val basePath: String,
) {


    @Bean
    fun openAPI(): OpenAPI? {
        val activeProfiles: Array<String> = env.activeProfiles
        val activeProfile = if (activeProfiles.isEmpty()) {
            "default"
        } else {
            activeProfiles[0]
        }
        val activeApiServer = "http://${domain}:${port}${basePath}"
        val openAPI = OpenAPI()

        // Heythere API
        openAPI
            .info(
                Info()
                    .title("MBI OpenAPI definition")
                    .version("v1.0")
                    .contact(
                        Contact()
                            .email("dev@happytalk.io")
                            .name("Development Team")
                    )
            )

        // API Servers
        openAPI
            .addServersItem(
                Server()
                    .url(activeApiServer)
                    .description(activeProfile)
            )
        /*
        when (activeProfile) {
            "docker", "local" -> openAPI.addServersItem(
                Server()
                    .url(activeApiServer.replaceFirst("local", "dev"))
                    .description("dev")
            )

            "dev" -> openAPI.addServersItem(
                Server()
                    .url(activeApiServer.replaceFirst("dev", "local"))
                    .description("local")
            )
        }
         */

        // Tags
        openAPI
            .addTagsItem(Tag().name(AUTHENTICATION).description("인증"))
            .addTagsItem(Tag().name(LOCAL_EXAMPLE).description("로컬 예제 API"))

        return openAPI
    }
}
