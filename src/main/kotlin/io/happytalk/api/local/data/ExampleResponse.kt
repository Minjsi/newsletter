package io.happytalk.api.local.data

import io.happytalk.api.global.data.DefaultResponse
import io.happytalk.api.local.entity.ExampleEntity

/**
 * @author : LN
 * @since : 2023. 3. 2.
 */
class ExampleResponse(
    val data: List<ExampleEntity>,
) : DefaultResponse()
