package io.happytalk.api.global.support

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.Properties
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * MBI Secret Helper Class
 *
 * [암복호화 소스 출처](https://commonkb.blogspot.com/2015/12/andorid-php-aes256-endecode.html)
 *
 * @author : LN
 * @since : 2021. 9. 1.
 */
@Suppress("All")
class MbiSecret private constructor() {

    private val logger = KotlinLogging.logger { }

    companion object {

        private var mbiSecret: MbiSecret? = null
        fun instance(): MbiSecret {
            if (mbiSecret == null) {
                mbiSecret = MbiSecret()
                mbiSecret!!.init()
            }
            return mbiSecret!!
        }
    }

    private val syncObject = Any()
    private val encValuePrefix = "ENC("
    private val encValueSuffix = ")"
    private val encValuePrefixLength = encValuePrefix.length
    private val encValueSuffixLength = encValueSuffix.length

    private var ivParameterSpec: IvParameterSpec? = null
    private var secretKeySpec: SecretKeySpec? = null
    private var cipher: Cipher? = null
    private var initialized = false

    /**
     *
     */
    private fun init() {
        // REMIND - LN
        // PHP openssl_encrypt(값, "aes-256-cbc", 암호화키, OPENSSL_RAW_DATA, str_repeat(chr(0), 16)) 로 수행한 암호화 문자열
        // 복호화시 iv 값 때문에 삽질 ...
        val iv = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        try {
            val dotenv = Dotenv.instance()
            val secretFilePath = dotenv.get("MBI_SECRET_PATH", "/home/secret/mbi_secretkey.ini")
            val properties = Properties()
            FileInputStream(secretFilePath).use { fis -> properties.load(fis) }
            var key: String? = null
            val propertyNames = properties.stringPropertyNames()
            for (propertyName in propertyNames) {
                val propertyValue = properties.getProperty(propertyName)
                if (propertyName == "ENCRYPT_KEY_CONFIG") {
                    key = propertyValue
                    continue
                }
                // System ENV 가 아닌 Property 에 추가해서 사용
                System.setProperty(propertyName, propertyValue)
            }
            if (key == null) {
                throw RuntimeException("ENCRYPT_KEY_CONFIG is not defined.")
            }
            if (key.length > 32) {
                key = key.substring(0, 32)
            }
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            ivParameterSpec = IvParameterSpec(iv)
            secretKeySpec = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "AES")
            initialized = true
        } catch (e: Exception) {
            logger.error(e) { "error occurred while initializing" }
            initialized = false
        }
    }

    /**
     *
     */
    fun processReplace(text: String): String {
        if (!initialized) {
            throw RuntimeException("Initialization is required. (Initialization may have failed)")
        }
        val trimText = text.trim { it <= ' ' }
        if (trimText.isEmpty()) {
            return text
        }
        val builder = StringBuilder(trimText)
        decryptEnc(builder)
        return builder.toString()
    }

    /**
     *
     */
    fun encrypt(str: String): String {
        synchronized(syncObject) {
            return try {
                val strBytes = str.toByteArray(StandardCharsets.UTF_8)
                cipher!!.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
                Base64.getEncoder().encodeToString(cipher!!.doFinal(strBytes))
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

    /**
     *
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun decrypt(str: String?): String {
        synchronized(syncObject) {
            return try {
                val strBytes = Base64.getDecoder().decode(str)
                cipher!!.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
                String(cipher!!.doFinal(strBytes), StandardCharsets.UTF_8)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

    /**
     * 인자로 받은 text 에서 "ENC(암호화문자열)" 형태인 부분 모두 복호화 하여 반환
     */
    private fun decryptEnc(builder: StringBuilder) {
        var startIndex = builder.indexOf(encValuePrefix)
        if (startIndex < 0) {
            return
        }
        var endIndex: Int
        do {
            endIndex = builder.indexOf(encValueSuffix, startIndex + encValuePrefixLength)
            val encValue = builder.substring(startIndex + encValuePrefixLength, endIndex)
            val decValue = decrypt(encValue)
            endIndex += encValueSuffixLength
            builder.delete(startIndex, endIndex)
            builder.insert(startIndex, decValue)
            startIndex = endIndex - encValuePrefixLength + (decValue.length - encValue.length)
            startIndex = if (startIndex > -1) startIndex else 0
        } while (builder.indexOf(encValuePrefix, startIndex).also { startIndex = it } > -1)
    }

    /**
     *
     */
    fun isEncrypted(value: String): Boolean {
        return value.contains(encValuePrefix)
    }

}
