package io.newsletter.api.global.support

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanNameGenerator

/**
 * Spring Bean 이름을 패키지명 까지 포함하기 위한 Generator
 *
 * @author : LN
 * @since : 2023. 4. 7.
 */
class FullBeanNameGenerator : BeanNameGenerator {
    override fun generateBeanName(
        definition: BeanDefinition,
        registry: BeanDefinitionRegistry,
    ): String {
        return definition.beanClassName!!
    }
}
