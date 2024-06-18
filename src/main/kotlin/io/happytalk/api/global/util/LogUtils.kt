package io.happytalk.api.global.util

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.lang3.time.DateUtils
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter

/**
 * 로깅 관련 유틸
 *
 * @author : LN
 * @since : 2017. 6. 27.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object LogUtils {

    const val LOG_TITLE_LINE = "============================================================="

    private val logger = KotlinLogging.logger { }

    private val LOG_FORMAT_NUMBER_POSTFIX = arrayOf("st", "nd", "rd", "th")

    /**
     * 인자로 받은 숫자를 1st, 2nd, 3rd, 4th 같이 접미어를 붙여서 반환
     */
    fun getLogFormatNumber(number: Int): String {

        return number.toString() + if (number < 4) LOG_FORMAT_NUMBER_POSTFIX[number - 1] else LOG_FORMAT_NUMBER_POSTFIX[3]
    }

    /**
     * 인자로 받은 ms 단위의 시간값을 문자열 형태로 변경해서 반환<br></br> 예) 2 days 6 hours 28 minutes 59 seconds
     */
    fun timeToLogFormat(timeMillis: Long): String {

        val days: Long
        val hours: Long
        val minutes: Long
        val seconds: Long
        var milliseconds = timeMillis
        days = milliseconds / DateUtils.MILLIS_PER_DAY
        milliseconds -= days * DateUtils.MILLIS_PER_DAY
        hours = milliseconds / DateUtils.MILLIS_PER_HOUR
        milliseconds -= hours * DateUtils.MILLIS_PER_HOUR
        minutes = milliseconds / DateUtils.MILLIS_PER_MINUTE
        milliseconds -= minutes * DateUtils.MILLIS_PER_MINUTE
        seconds = milliseconds / DateUtils.MILLIS_PER_SECOND
        milliseconds -= seconds * DateUtils.MILLIS_PER_SECOND
        val result = StringBuilder()
        if (days > 0) {
            result.append(days).append(if (days > 1) " days " else "day ")
        }
        if (hours > 0) {
            result.append(hours).append(if (hours > 1) " hours " else " hour ")
        }
        if (minutes > 0) {
            result.append(minutes).append(if (minutes > 1) " minutes " else " minute ")
        }
        if (seconds > 0) {
            result.append(seconds)
            if (milliseconds > 0) {
                result.append(".").append(milliseconds)
            }
            result.append(if (seconds > 1 || milliseconds > 1) " seconds" else " second")
        } else {
            if (milliseconds > 0) {
                result.append(0).append(".").append(if (milliseconds > 1) " seconds" else " second")
            }
        }
        return result.toString().trim()
    }

    /**
     * Carriage Return, Line Feed 값을 로그에서 한 라인으로 볼 수 있는 형태로 변환해서 반환
     */
    fun replaceCRLFForLogging(messages: Any): String {

        val result = StringBuilder()
        if (messages.javaClass.isArray) {
            val arr = messages as Array<*>
            for (message in arr) {
                result.append(replaceCRLFForLogging(message.toString()))
            }
        } else {
            result.append(replaceCRLFForLogging(messages.toString()))
        }
        return result.toString()
    }

    /**
     * Carriage Return, Line Feed 값을 로그에서 한 라인으로 볼 수 있는 형태로 변환해서 반환
     */
    fun replaceCRLFForLogging(message: String): String {

        return message.replace("\r\n".toRegex(), "<CRLF>").replace("\n".toRegex(), "<CR>")
            .replace("\n".toRegex(), "<LF>")
    }

    /**
     * 인자로 받은 Throwable 의 suppressed 확인 해 로그 형태로 반환
     *
     * @param throwable           suppressed stack trace 확인할 throwable
     * @param maxPrintTraceCount  출력 최대 count
     * @param maxSearchTraceCount 검색 최대 count
     * @param includeClassPrefix  trace 로그에서 포함할 클래스 Prefix
     */
    fun getTraceLog(
        throwable: Throwable, maxPrintTraceCount: Int,
        maxSearchTraceCount: Int, vararg includeClassPrefix: String?,
    ): String {

        var suppressed = throwable.suppressed
        if (suppressed.isEmpty()) {
            suppressed = arrayOfNulls(1)
            suppressed[0] = throwable
        }
        var searchCount = 0
        var printCount = 0
        val builder = StringBuilder()
        for (t in suppressed) {
            val stackTraceElements = t!!.stackTrace
            for (s in stackTraceElements) {
                searchCount += 1
                var include = false
                var className = s.className
                for (prefix in includeClassPrefix) {
                    if (className.startsWith(prefix!!)) {
                        include = true
                        break
                    }
                }
                if (!include) {
                    continue
                }
                className = className.substring(className.lastIndexOf('.') + 1)
                if (className.contains("CGLIB$$")) {
                    continue
                }
                printCount += 1
                if (printCount > 1) {
                    builder.append(" < ")
                }
                builder.append(className).append(":").append(s.lineNumber)
                if (searchCount == maxSearchTraceCount || printCount == maxPrintTraceCount) {
                    break
                }
            }
            if (searchCount == maxSearchTraceCount || printCount == maxPrintTraceCount) {
                break
            }
        }
        return builder.toString()
    }

    /**
     * @see getTraceLog(Throwable, int, int, String...)
     */
    fun getTraceLog(throwable: Throwable): String {
        return getTraceLog(throwable, 5, 15, "com.heythere", "io.heythere")
    }

    /**
     * 인자로 받은 throwable 을 trace 문자열 형태로 반환
     */
    fun getPrintStackTraceLog(throwable: Throwable): String {
        var sWriter: StringWriter? = null
        var pWriter: PrintWriter? = null
        return try {
            sWriter = StringWriter(4000)
            pWriter = PrintWriter(sWriter)
            pWriter.println(throwable.message)
            throwable.printStackTrace(pWriter)
            sWriter.toString()
        } finally {
            if (sWriter != null) {
                try {
                    sWriter.close()
                } catch (ioe: IOException) {
                    // ignore
                }
            }
            pWriter?.close()
        }
    }

    /**
     * 모든 System Properties 를 정렬해서 로깅
     */
    fun systemPropertiesLogging() {

        // System Properties 로깅
        if (logger.isDebugEnabled()) {
            logger.debug { LOG_TITLE_LINE }
            logger.debug { " System Properties..." }
            logger.debug { LOG_TITLE_LINE }
        } else {
            logger.info { "System Properties..." }
        }
        var keyList: List<Any> = ArrayList(System.getProperties().keys)
        keyList = keyList.sortedWith(Comparator.comparing { obj: Any -> obj.toString() })
        for (key in keyList) {
            val strKey = key.toString()
            var value = System.getProperty(strKey)
            if (value.isEmpty() || strKey.startsWith("rebel.")) {
                continue
            } else if ("line.separator" == strKey) {
                val lsArray = value.toByteArray()
                val vb = StringBuilder()
                for (ls in lsArray) {
                    if (vb.isNotEmpty()) {
                        vb.append(", ")
                    }
                    vb.append(ls.toInt())
                }
                value = vb.toString()
            }
            logger.info { " > $strKey = [$value]" }
        }
    }
}
