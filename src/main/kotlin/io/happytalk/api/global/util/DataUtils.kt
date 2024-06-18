package io.happytalk.api.global.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.TreeMap

/**
 * 데이터 관련 유틸
 *
 * @author : LN
 * @since : 2022. 6. 29.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object DataUtils {

    val OBJECT_MAPPER = ObjectMapper()
    val PRETTY_OBJECT_MAPPER = ObjectMapper()
    val OBJECT_MAPPER_LOWER_CAMEL_CASE = ObjectMapper()
    val OBJECT_MAPPER_SNAKE_CASE = ObjectMapper()
    val COMPARE_OBJECT_MAPPER: ObjectMapper = JsonMapper.builder()
        .nodeFactory(JsonNodeFactoryForCompare())
        .build()

    init {
        val objectMappers: MutableList<ObjectMapper> = ArrayList(2)
        objectMappers.add(OBJECT_MAPPER)
        objectMappers.add(PRETTY_OBJECT_MAPPER)
        objectMappers.add(OBJECT_MAPPER_SNAKE_CASE)
        objectMappers.add(COMPARE_OBJECT_MAPPER)

        val javaTimeModule = JavaTimeModule()
        for (objectMapper in objectMappers) {
            with(objectMapper) {
                registerModule(javaTimeModule)
                setSerializationInclusion(JsonInclude.Include.NON_NULL)
                configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                registerModule(createKotlinModule())
            }
        }

        PRETTY_OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT)
        OBJECT_MAPPER_LOWER_CAMEL_CASE.propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE
        OBJECT_MAPPER_SNAKE_CASE.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }

    /**
     * 인자로 받은 JSON 을 객체로 변환
     *
     * @param json        JSON 데이터
     * @param resultClass 변환할 POJO 클래스
     * @return JSON 문자열을 객체로 반환. 에러 발생시 null 반환
     */
    fun <T> toObject(json: String, resultClass: Class<T>): T {
        return try {
            OBJECT_MAPPER.readValue(json, resultClass)
        } catch (e: JsonProcessingException) {
            throw RuntimeException("error occurred while parse json. [json: $json]", e)
        }
    }

    /**
     * 인자로 받은 JSON 을 객체로 변환
     *
     * @param json             JSON 데이터
     * @param resultClass      변환할 POJO 클래스
     * @param propertyCaseType 소스 JSON 의 Property Case Type [PropertyCaseType]
     * @return JSON 문자열을 객체로 반환. 에러 발생시 null 반환
     */
    fun <T> toObject(
        json: String, resultClass: Class<T>,
        propertyCaseType: PropertyCaseType,
    ): T {
        return try {
            when (propertyCaseType) {
                PropertyCaseType.SNAKE_CASE -> OBJECT_MAPPER_SNAKE_CASE.readValue(json, resultClass)
                PropertyCaseType.LOWER_CAMEL_CASE -> OBJECT_MAPPER_LOWER_CAMEL_CASE.readValue(json, resultClass)
            }
        } catch (e: JsonProcessingException) {
            throw RuntimeException("error occurred while parse json. [json: $json]", e)
        }
    }

    fun <T> toObject(jsonBytes: ByteArray, resultClass: Class<T>): T {
        return try {
            OBJECT_MAPPER.readValue(jsonBytes, resultClass)
        } catch (e: IOException) {
            throw RuntimeException(
                "error occurred while parse json. [json: ${
                    String(
                        jsonBytes,
                        StandardCharsets.UTF_8
                    )
                }]", e
            )
        }
    }

    /**
     * @param request     HTTP 요청 정보
     * @param resultClass 반환할 POJO 클래스
     * @return request 객체를 POJO 객체로 변환
     */
    fun <T> toObject(
        request: ServerHttpRequest,
        resultClass: Class<T>,
    ): T {
        val parameterMap = request.queryParams.toSingleValueMap()
        return OBJECT_MAPPER.convertValue(parameterMap, resultClass)
    }


    /**
     * @param plain plain BASE64 로 인코딩할 평문 문자열
     * @return 평문을 BASE64 로 인코딩 후 반환
     */
    fun encodeBase64(plain: String): String {
        return Base64.getEncoder().encodeToString(plain.toByteArray(StandardCharsets.UTF_8))
    }

    fun encodeBase64(bytes: ByteArray): String {
        return Base64.getEncoder().encodeToString(bytes)
    }

    /**
     * @param encoded BASE64 로 인코딩 된 문자열
     * @return 평문으로 디코딩 후 반환
     */
    fun decodeBase64(encoded: String): String {
        return String(Base64.getDecoder().decode(encoded.toByteArray()), StandardCharsets.UTF_8)
    }

    /**
     * @param obj MultiValueMap 으로 변환할 인스턴스
     * @return MultiValueMap 형태로 반환
     */
    fun toMultiValueMap(obj: Any): MultiValueMap<String, String> {
        val result: MultiValueMap<String, String> = LinkedMultiValueMap()
        val maps: Map<String, String> =
            OBJECT_MAPPER.convertValue(obj, object : TypeReference<Map<String, String>>() {})
        result.setAll(maps)
        return result
    }

    /**
     * @param value JSON 으로 반환할 인스턴스
     * @return JSON 형태의 문자열
     */
    fun toJson(value: Any): String {
        return try {
            OBJECT_MAPPER.writeValueAsString(value)
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }
    }

    /**
     * @param value JSON bytes 로 반환할 인스턴스
     * @return JSON 형태의 문자열
     */
    fun toJsonBytes(value: Any): ByteArray {
        return try {
            OBJECT_MAPPER.writeValueAsBytes(value)
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }
    }

    /**
     * `Experimental` 값 비교용 JSON 반환
     *
     * @param value JSON 으로 반환할 인스턴스
     * @return JSON 형태의 문자열
     */
    fun toJsonForCompare(value: Any): String {
        return try {
            // 정렬 위해 객체를 json 으로 변환 > 읽기(정렬) > 쓰기
            // TODO - LN : 성능 확인 및 개선
            val json = COMPARE_OBJECT_MAPPER.writeValueAsString(value)
            val jsonNode = COMPARE_OBJECT_MAPPER.readTree(json)
            COMPARE_OBJECT_MAPPER.writeValueAsString(jsonNode)
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }
    }

    /**
     * @param value JSON 으로 반환할 인스턴스
     * @return JSON 형태의 문자열
     */
    fun toJsonPretty(value: Any): String {
        return try {
            PRETTY_OBJECT_MAPPER.writeValueAsString(value)
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }
    }

    /**
     * Create kotlin module
     *
     * @return
     */
    fun createKotlinModule(): KotlinModule {

        return KotlinModule.Builder()
            .withReflectionCacheSize(512)
            .configure(KotlinFeature.NullToEmptyCollection, false)
            .configure(KotlinFeature.NullToEmptyMap, false)
            .configure(KotlinFeature.NullIsSameAsDefault, false)
            .configure(KotlinFeature.SingletonSupport, false)
            .configure(KotlinFeature.StrictNullChecks, false)
            .build()
    }

    /**
     * Property case type
     *
     * @constructor Create empty Property case type
     */
    enum class PropertyCaseType {
        LOWER_CAMEL_CASE, SNAKE_CASE
    }

    /**
     * Json node factory for compare
     *
     * @constructor Create empty Json node factory for compare
     */
    private class JsonNodeFactoryForCompare : JsonNodeFactory() {
        override fun objectNode(): ObjectNode {
            return ObjectNode(this, TreeMap())
        }
    }
}