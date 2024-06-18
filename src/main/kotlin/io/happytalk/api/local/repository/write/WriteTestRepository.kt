package io.happytalk.api.local.repository.write

import io.happytalk.api.local.entity.ExampleEntity
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface WriteTestRepository : R2dbcRepository<ExampleEntity, Long>
