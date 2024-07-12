package io.newsletter.api.global.repository

import io.happytalk.api.global.config.R2dbcMySqlConverterConfig
import io.happytalk.api.global.exception.ValidationFailedException
import io.happytalk.api.global.support.R2dbcLoggingListener
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.RowsFetchSpec
import org.springframework.r2dbc.core.UpdatedRowsFetchSpec
import org.springframework.stereotype.Component
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * @since : 2023. 5. 26.
 */
@Component
@Scope("prototype")
class BaseRepository {

    /**
     * [R2dbcMySqlConverterConfig.r2dbcConverter]
     */
    @Autowired
    private lateinit var converter: MappingR2dbcConverter

    /**
     * [R2dbcLoggingListener] 통해 로깅하지 않을 쿼리
     * GenericExecuteSpec extended
     *
     * @param sql
     */
    @Suppress("SqlSourceToSinkFlow")
    protected fun DatabaseClient.sqlNotDebugLogging(
        sql: String,
    ): DatabaseClient.GenericExecuteSpec {
        R2dbcLoggingListener.addExcludeSql(sql)
        return this.sql(sql)
    }

    /**
     * nullable value 를 바인딩 한다.
     * GenericExecuteSpec extended
     *
     * @param name
     * @param value
     * @param type
     */
    protected fun DatabaseClient.GenericExecuteSpec.bindNullable(
        name: String,
        value: Any?,
        type: Class<*>,
    ): DatabaseClient.GenericExecuteSpec {
        return if (value != null) {
            this.bind(name, value)
        } else {
            this.bindNull(name, type)
        }
    }

    /**
     *
     * @param name
     * @param dateTime
     * @return
     */
    protected fun DatabaseClient.GenericExecuteSpec.bindDateTime(
        name: String,
        dateTime: ZonedDateTime?,
    ): DatabaseClient.GenericExecuteSpec {
        return if (dateTime != null) {
            val zoneId = ZoneId.systemDefault()
            val defaultZonedDateTime = dateTime.withZoneSameInstant(zoneId)
            val localDateTime = defaultZonedDateTime.toLocalDateTime()

            this.bind(name, localDateTime)
        } else {
            this.bindNull(name, ZonedDateTime::class.java)
        }
    }

    /**
     * [Row], [RowMetadata] 를 targetType 형태로 변환
     *
     * @param R
     * @param targetType
     * @return
     */
    protected fun <R> DatabaseClient.GenericExecuteSpec.mapTo(targetType: Class<R>): RowsFetchSpec<R> {
        return this.map { row, rowMetadata ->
            converter.read(targetType, row, rowMetadata)
        }
    }

    /**
     * [org.springframework.r2dbc.core.awaitRowsUpdated] 과 동일한 기능 수행 후 값 검증
     *
     * @param expectValues 기대하는 값
     * @param messageOrThrowable 문자열로 반환하면 [ValidationFailedException] 으로 처리
     * @return 기대하는 값이 아닌경우 throw, 기대하는 값인경우 rowsUpdated 값 반환
     */
    protected suspend fun UpdatedRowsFetchSpec.awaitRowsUpdated(
        vararg expectValues: Long,
        messageOrThrowable: (rowsUpdated: Long) -> Any,
    ): Long {
        val rowsUpdated = rowsUpdated().awaitSingle()
        var expect = false
        expectValues.forEach { expectValue ->
            if (rowsUpdated == expectValue) {
                expect = true
                return@forEach
            }
        }
        // 의도했던 값일경우
        if (expect) {
            return rowsUpdated
        }
        // 의도하지 않았던 값일경우 Exception throw
        val mt = messageOrThrowable(rowsUpdated)
        if (mt is String) {
            throw ValidationFailedException(mt)
        }
        if (mt is Throwable) {
            throw mt
        }
        throw ValidationFailedException(mt.toString())
    }

    /**
     * 이 함수 미정의시 인자 없는 상태로 awaitRowsUpdated() 호출시
     * 의도치 않게 awaitRowsUpdated(vararg expectValues: Long) 호출되기 때문에 정의
     *
     * @return [org.springframework.r2dbc.core.awaitRowsUpdated]
     */
    protected suspend fun UpdatedRowsFetchSpec.awaitRowsUpdated(): Long {
        return rowsUpdated().awaitSingle()
    }
}
