package io.newsletter.api.global.util

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher

/**
 * RSA 암호화 관련 유틸리티 클래스입니다.
 * - 작성 당시 기준 암호화 되지 않은 private key 를 사용할 수 있습니다.
 * - openssl 커맨드로 아래와 같이 PEM 형식의 공개키와 개인키를 생성할 수 있고, pkcs8 형식의 개인키를 사용할 수 있습니다.
 *
 * ```shell
 * openssl genrsa -out private_key.pem 2048
 * openssl pkcs8 -topk8 -nocrypt -in private_key.pem -outform PEM -out private_key_pkcs8.pem
 * openssl rsa -in private_key.pem -pubout -outform PEM -out public_key.pem
 * ```
 *
 * @author : LN
 * @since : 2023. 2. 24.
 */
@Suppress("unused")
object RsaUtils {


    /**
     * RSA 알고리즘을 사용하는 KeyPair 를 생성합니다.
     */
    private const val RSA = "RSA"

    /**
     * 생성될 KeyPair 의 길이입니다.
     */
    private const val KEY_SIZE = 2048

    /**
     * PEM 파일에서 공개키와 개인키의 헤더와 푸터를 제거하기 위한 정규식 패턴입니다.
     *
     *
     * 이 패턴은 PEM 파일에서 "-----BEGIN" 또는 "-----END"로 시작하고, 그 뒤에 하이픈 ('-')이 연속해서 나오고, 다시 뒤따라오는 문자열이 하이픈으로
     * 끝나는 문자열 패턴을 찾습니다.
     */
    private const val PEM_HEADER_FOOTER_PATTERN = "(-+BEGIN [^-]+-+\\r?\\n|\\r?\\n-+END [^-]+-+\\r?\\n?)"

    /**
     * 지정된 경로에서 PEM 형식의 RSA Private Key 파일을 읽어들여 PrivateKey 객체를 생성합니다.
     *
     * @param privateKeyFile Private Key 파일 경로
     * @return PrivateKey 객체
     */
    fun getPrivateKey(privateKeyFile: String): PrivateKey {
        return try {
            val privateKeyBytes = readKeyFile(privateKeyFile)
            val privateKeySpec = PKCS8EncodedKeySpec(privateKeyBytes)
            val keyFactory = KeyFactory.getInstance(RSA)
            keyFactory.generatePrivate(privateKeySpec)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidKeySpecException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 지정된 경로에서 PEM 형식의 RSA Public Key 파일을 읽어들여 PublicKey 객체를 생성합니다.
     *
     * @param publicKeyFile Public Key 파일 경로
     * @return PublicKey 객체
     */
    fun getPublicKey(publicKeyFile: String): PublicKey {
        return try {
            val publicKeyBytes = readKeyFile(publicKeyFile)
            val publicKeySpec = X509EncodedKeySpec(publicKeyBytes)
            val keyFactory = KeyFactory.getInstance(RSA)
            keyFactory.generatePublic(publicKeySpec)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidKeySpecException) {
            throw RuntimeException(e)
        }
    }

    /**
     * RSA KeyPair 생성
     *
     * @param keySize 생성할 KeyPair 의 비트 수
     * @return 생성된 RSA KeyPair
     * @throws NoSuchAlgorithmException 비대칭키 생성에 실패한 경우
     */
    @Throws(NoSuchAlgorithmException::class)
    fun generateKeyPair(keySize: Int): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(keySize)
        return keyPairGenerator.generateKeyPair()
    }

    /**
     * RSA 공개키로 문자열 암호화
     *
     * @param publicKey 공개키
     * @param plainText 암호화할 문자열
     * @return 암호화된 문자열
     * @throws Exception 암호화 실패 시 예외 발생
     */
    @Throws(Exception::class)
    fun encrypt(publicKey: PublicKey?, plainText: String): String {
        val cipher = Cipher.getInstance(RSA)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val bytePlainText = plainText.toByteArray(StandardCharsets.UTF_8)
        val encryptedBytes = cipher.doFinal(bytePlainText)
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    /**
     * RSA 개인키로 문자열 복호화
     *
     * @param privateKey    개인키
     * @param encryptedText 복호화할 암호화된 문자열
     * @return 복호화된 문자열
     * @throws Exception 복호화 실패 시 예외 발생
     */
    @Throws(Exception::class)
    fun decrypt(privateKey: PrivateKey, encryptedText: String): String {
        val cipher = Cipher.getInstance(RSA)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val byteEncryptedText = Base64.getDecoder().decode(encryptedText)
        val decryptedBytes = cipher.doFinal(byteEncryptedText)
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }

    /**
     * 지정된 경로에 public key 와 private key 를 생성하고 저장합니다.
     *
     * @param publicKeyFile  생성된 public key 를 저장할 경로
     * @param privateKeyFile 생성된 private key 를 저장할 경로
     * @throws NoSuchAlgorithmException 지정된 알고리즘을 찾을 수 없을 경우 발생
     * @throws IOException              파일을 쓰기 위한 예외
     */
    @Throws(NoSuchAlgorithmException::class, IOException::class)
    fun generateKeyPairFiles(publicKeyFile: String, privateKeyFile: String) {
        val generator = KeyPairGenerator.getInstance(RSA)
        generator.initialize(KEY_SIZE)
        val keyPair = generator.generateKeyPair()
        val publicKey = keyPair.public
        val privateKey = keyPair.private
        writeKeyToFile(publicKeyFile, publicKey.encoded)
        writeKeyToFile(privateKeyFile, privateKey.encoded)
    }

    /**
     * 지정된 경로에 key bytes 를 파일로 저장합니다.
     *
     * @param path     파일을 저장할 경로
     * @param keyBytes key bytes
     * @throws IOException 파일을 쓰기 위한 예외
     */
    @Throws(IOException::class)
    private fun writeKeyToFile(path: String, keyBytes: ByteArray) {
        FileOutputStream(path).use { fos ->
            val encodedKey = Base64.getEncoder().encodeToString(keyBytes)
            fos.write(encodedKey.toByteArray())
        }
    }

    /**
     * 지정된 경로의 파일을 byte 배열로 읽어옵니다.<br></br> 파일이 존재하지 않을 경우 파일 경로를 byte 배열로 변환하여 반환합니다.
     *
     * @param keyFile 읽어올 파일 경로
     * @return 파일 내용을 담은 byte 배열
     * @throws IOException 파일을 읽어오는 중 예외가 발생한 경우
     */
    @Throws(IOException::class)
    fun readKeyFile(keyFile: String): ByteArray {
        var pem: String = if (File(keyFile).exists()) {
            Files.readString(Paths.get(keyFile))
        } else {
            keyFile
        }
        pem = pem.replace(PEM_HEADER_FOOTER_PATTERN.toRegex(), "").replace("\\s".toRegex(), "")
        return Base64.getDecoder().decode(pem)
    }

}
