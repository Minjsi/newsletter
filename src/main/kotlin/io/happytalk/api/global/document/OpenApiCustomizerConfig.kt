package io.happytalk.api.global.document

import io.happytalk.api.global.data.BaseResponse
import com.fasterxml.jackson.databind.type.SimpleType
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.oas.models.media.Schema
import org.springdoc.core.customizers.PropertyCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author : LN
 * @since : 2023. 2. 16.
 */
@Suppress("UNCHECKED_CAST", "TYPE_MISMATCH_WARNING")
@Configuration
class OpenApiCustomizerConfig {

    private val logger = KotlinLogging.logger { }

    @Bean
    fun propertyCustomizer(): PropertyCustomizer? {
        return PropertyCustomizer { schema: Schema<*>, annotatedType: AnnotatedType ->
            try {
                if (annotatedType.type !is SimpleType) {
                    return@PropertyCustomizer schema
                }

                val simpleType = annotatedType.type as SimpleType
                if (!simpleType.isEnumImplType) {
                    return@PropertyCustomizer schema
                }

                logger.debug {
                    "${annotatedType.parent.name}:${annotatedType.propertyName} enum schema description customizing"
                }

                // Enum 타입 커스텀 어노테이션 @Description 을 문서에 적용
                val descriptionBuilder = StringBuilder()
                val propertyDescription = schema.description
                if (!propertyDescription.isNullOrEmpty()) {
                    descriptionBuilder.append(propertyDescription)
                }
                descriptionBuilder.append("\n").append(
                    """
                    |Name|Description|
                    |----|-----------|
                    """.trimIndent()
                ).append("\n")
                val enumClass: Class<*> = simpleType.rawClass
                appendEnumDescription(enumClass, descriptionBuilder)
                val parentClass = enumClass.enclosingClass
                if (parentClass != null) {
                    val superClass = parentClass.superclass
                    if (superClass == BaseResponse::class.java) {
                        // BaseResponse 상속받은 경우 BaseResponse 의 ErrorCode 를 문서에 추가
                        appendEnumDescription(BaseResponse.Code::class.java, descriptionBuilder)
                        val enumList: ArrayList<String>?
                        if (schema.enum != null) {
                            enumList = schema.enum as ArrayList<String>?
                        } else {
                            enumList = ArrayList()
                            schema.enum = enumList
                        }
                        val baseCodes: Array<BaseResponse.Code> = BaseResponse.Code.values()
                        for (baseErrorCode in baseCodes) {
                            val baseErrorCodeName: String = baseErrorCode.name
                            enumList!!.add(baseErrorCodeName)
                        }
                    }
                }

                // 완성한 description 적용
                descriptionBuilder.append("\n")
                schema.setDescription(descriptionBuilder.toString())
            } catch (e: Exception) {
                logger.error(e) { "error occurred while customize spring-doc Schema." }
                return@PropertyCustomizer schema
            }
            schema
        }
    }

    /**
     * [Description] 사용한 enumClass 필드 설명을 descriptionBuilder 애 추가
     */
    private fun appendEnumDescription(enumClass: Class<*>, descriptionBuilder: StringBuilder) {
        val enumConstants = enumClass.enumConstants as Array<Enum<*>>
        for (enumConstant in enumConstants) {
            val enumConstantName = enumConstant.name
            val description: Description? = try {
                enumClass.getField(enumConstantName).getAnnotation(Description::class.java)
            } catch (e: NoSuchFieldException) {
                null
            }
            val descriptionValue = description?.value ?: ""
            descriptionBuilder
                .append("|").append(enumConstantName)
                .append("|").append(descriptionValue).append("|")
                .append("\n")
        }
    }
}
