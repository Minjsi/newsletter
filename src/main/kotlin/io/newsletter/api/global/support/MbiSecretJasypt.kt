package io.newsletter.api.global.support

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector
import com.ulisesbocchio.jasyptspringboot.detector.DefaultPropertyDetector
import org.jasypt.encryption.StringEncryptor

/**
 * [Jasypt](https://github.com/ulisesbocchio/jasypt-spring-boot) [DefaultPropertyDetector], Encryptor 대체
 *
 * - [DefaultPropertyDetector] 는 startWith(prefix) 로 암호화 여부를 판단.
 *     - 하나의 Property 에 여러 ENC() 사용 불가능
 * - contains(prefix) 로 암호화 여부 판단 후 그에 맞게 복호화
 *
 * @author : LN
 * @since : 2023. 4. 19.
 */
class MbiSecretJasypt : EncryptablePropertyDetector, StringEncryptor {

    private val mbiSecret = MbiSecret.instance()

    override fun isEncrypted(property: String): Boolean {

        return mbiSecret.isEncrypted(property)
    }

    override fun unwrapEncryptedValue(property: String): String {

        return property
    }

    override fun encrypt(message: String): String {

        return mbiSecret.encrypt(message)
    }

    override fun decrypt(encryptedMessage: String): String {

        return mbiSecret.processReplace(encryptedMessage)
    }
}