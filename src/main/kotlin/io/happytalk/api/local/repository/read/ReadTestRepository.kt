package io.happytalk.api.local.repository.read

import io.happytalk.api.local.entity.ExampleEntity
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface ReadTestRepository : R2dbcRepository<ExampleEntity, Long>
