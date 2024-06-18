package io.happytalk.api.local.data

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * @author : LN
 * @since : 2023. 4. 17.
 */
data class JwtGenerateRequest(

    @field:NotBlank
    @field:Email
    @field:Schema(
        description = "sub(subject) claim 에 들어갈 값",
        example = "dev@happytalk.io",
        required = true
    )
    val username: String,
)
