package io.newsletter.api.global.support

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.DotenvEntry
import me.paulschwarz.springdotenv.DotenvPropertyLoader

/**
 * Spring 과 상관없이 Dotenv 인스턴스를 싱글톤으로 관리하기 위한 클래스
 *
 * 시간될 때 [DotenvPropertyLoader] (spring-dotenv) 제거
 *
 * @author : LN
 * @since : 2023. 3. 16.
 */
class Dotenv {

    companion object {

        private var dotenv: Dotenv? = null

        fun instance(): Dotenv {

            if (dotenv == null) {
                val builder = Dotenv.configure()
                dotenv = builder.ignoreIfMissing().load()
            }
            return dotenv!!
        }
    }

    operator fun get(key: String?): String {
        return instance()[key]
    }

    operator fun get(key: String?, defaultValue: String?): String {
        return instance()[key, defaultValue]
    }

    fun entries(): Set<DotenvEntry> {
        return instance().entries()
    }

    fun entries(filter: Dotenv.Filter?): Set<DotenvEntry> {
        return instance().entries(filter)
    }

}
