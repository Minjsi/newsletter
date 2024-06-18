package io.happytalk.api.local.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.ZonedDateTime

/**
 * @author : LN
 * @since : 2023. 3. 2.
 */
@Table("example")
data class ExampleEntity(

    @Id
    val id: Long,
    @Column
    val keyword: String?,
    @Column
    val data: String?,
    @Column
    val regDate: ZonedDateTime,
)
