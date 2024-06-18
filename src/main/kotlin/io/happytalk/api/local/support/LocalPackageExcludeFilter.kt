package io.happytalk.api.local.support

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.type.classreading.MetadataReader
import org.springframework.core.type.classreading.MetadataReaderFactory
import org.springframework.core.type.filter.TypeFilter

/**
 * Spring Boot 프로필이 "local" 일때만 사용되는 패키지를 ComponentScan 시 제외하기 위한 Filter
 *
 * @author : LN
 * @since : 2023. 4. 11.
 */
class LocalPackageExcludeFilter : TypeFilter {

    private val logger = KotlinLogging.logger { }
    override fun match(
        metadataReader: MetadataReader,
        metadataReaderFactory: MetadataReaderFactory,
    ): Boolean {
        val className = metadataReader.annotationMetadata.className
        val result = className.startsWith("io.happytalk.api.local")
        if (logger.isTraceEnabled()) {
            if (result) {
                logger.trace { "exclude > $className" }
            } else {
                logger.trace { "include > $className" }
            }
        }
        return result
    }
}
