package io.happytalk.api.global.support

import io.github.oshai.kotlinlogging.KotlinLogging
import io.r2dbc.proxy.core.QueryExecutionInfo
import io.r2dbc.proxy.listener.ProxyExecutionListener
import io.r2dbc.proxy.support.QueryExecutionInfoFormatter
import org.jooq.TableRecord
import java.util.Collections

/**
 * https://r2dbc.io/2021/04/14/r2dbc-proxy-tips-query-logging
 *
 * @author : LN
 * @since : 2023. 3. 2.
 */
class R2dbcLoggingListener : ProxyExecutionListener {

    companion object {
        private const val DO_NOT_DEBUG_KEY = "DO_NOT_DEBUG"
        private val bindingParamRegex = Regex(":\\w+")
        private val r2dbcLoggingEnable = KotlinLogging.logger(R2dbcLoggingListener::class.java.name).isDebugEnabled()
        private val loggingExcludeSqlSet = Collections.synchronizedSet(mutableSetOf<String>())

        fun addExcludeSql(sql: String) {
            if (!r2dbcLoggingEnable) {
                return
            }
            val sqlForExcludeCheck = sql.replace(bindingParamRegex, "?")
            if (loggingExcludeSqlSet.contains(sqlForExcludeCheck)) {
                return
            }
            loggingExcludeSqlSet.add(sqlForExcludeCheck)
        }
    }

    private val logger = KotlinLogging.logger { }

    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private val formatter = QueryExecutionInfoFormatter()
        .addConsumer { queryExecutionInfo, stringBuilder -> stringBuilder.append("Execute") }
        .showQuery()
        .addConsumer { queryExecutionInfo, stringBuilder -> stringBuilder.append("/") }
        .showBindings()

    /**
     * 쿼리 실행 전 SQL 로깅
     */
    override fun beforeQuery(execInfo: QueryExecutionInfo) {
        logger.debug { formatter.format(execInfo) }
    }

    /**
     * Result Row 하나마다 로깅
     */
    override fun eachQueryResult(execInfo: QueryExecutionInfo) {
        execInfo.currentMappedResult?.let { result ->
            // Result Row 하나마다 로깅
            logger.debug {
                "${execInfo.currentResultCount} / ${convertResultForLogging(result)}"
            }
        }
    }

    /**
     * Query 결과를 로깅하기 위한 변환
     */
    private fun convertResultForLogging(result: Any): String {

        return if (result is TableRecord<*>) {
            result.fields().joinToString(",") { field ->
                "${field.name}=${result.getValue(field)}"
            }
        } else {
            result.toString()
        }
    }
}
