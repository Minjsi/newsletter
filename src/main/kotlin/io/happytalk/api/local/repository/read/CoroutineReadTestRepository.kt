package io.happytalk.api.local.repository.read

import io.happytalk.api.local.entity.ExampleEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface CoroutineReadTestRepository : CoroutineCrudRepository<ExampleEntity, Long>
