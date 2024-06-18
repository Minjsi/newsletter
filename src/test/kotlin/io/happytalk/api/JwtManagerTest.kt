//package io.happytalk.api
//
//import io.happytalk.api.global.data.JwtToken
//import io.happytalk.api.global.data.properties.JwtProperties
//import io.happytalk.api.global.support.JwtManager
//import io.happytalk.api.global.util.RsaUtils
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.runBlocking
//import org.junit.jupiter.api.*
//import org.junit.jupiter.api.Assertions.*
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import java.nio.file.Files
//import java.nio.file.Path
//import java.time.ZonedDateTime
//
///**
// * @author : LN
// * @since : 2022. 8. 2.
// */
//@SpringBootTest
//@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
//class JwtManagerTest(
//    @Autowired
//    private val jwtManager: JwtManager,
//
//    @Autowired
//    private val jwtProperties: JwtProperties,
//) {
//
//    private var orgPublicKey: String = ""
//    private var orgPrivateKey: String = ""
//    private var orgAccessTokenValidity: Long = 0
//    private var orgRefreshTokenValidity: Long = 0
//
//    @BeforeEach
//    fun setUp() = runBlocking {
//        orgAccessTokenValidity = jwtProperties.accessTokenValidity
//        orgRefreshTokenValidity = jwtProperties.refreshTokenValidity
//        if (!Files.isRegularFile(Path.of(jwtProperties.publicKey))
//            && !Files.isRegularFile(Path.of(jwtProperties.privateKey))
//        ) {
//            orgPublicKey = jwtProperties.publicKey
//            orgPrivateKey = jwtProperties.privateKey
//        }
//    }
//
//    @Test
//    @Order(1)
//    @DisplayName("public, private key 파일 생성 테스트")
//    fun test_generateKeyPairFiles() = runBlocking {
//
//        var publicKey: String = jwtProperties.publicKey
//        var privateKey: String = jwtProperties.privateKey
//        var publicKeyPath = Path.of(publicKey)
//        var privateKeyPath = Path.of(privateKey)
//        if (!Files.isRegularFile(publicKeyPath) && !Files.isRegularFile(privateKeyPath)) {
//            publicKey = "src/test/resources/test_publicKey.pem"
//            privateKey = "src/test/resources/test_privateKey.pem"
//            publicKeyPath = Path.of(publicKey)
//            privateKeyPath = Path.of(privateKey)
//            publicKeyPath.toFile().delete()
//            privateKeyPath.toFile().delete()
//            publicKeyPath.toFile().parentFile.mkdirs()
//            privateKeyPath.toFile().parentFile.mkdirs()
//        }
//
//        // When
//        RsaUtils.generateKeyPairFiles(publicKey, privateKey)
//
//        // Then
//        assertTrue(Files.exists(publicKeyPath))
//        assertTrue(Files.exists(privateKeyPath))
//    }
//
//    @Test
//    @Order(2)
//    @DisplayName("JwtToken 생성, 검증, 만료 테스트")
//    fun test_validateToken_withValidToken() = runBlocking {
//
//        // 토큰 생성
//        val username = "dev@happytalk.io"
//        var jwtToken = jwtManager.generateToken(username)
//        // 검증
//        validateJwtToken(username, jwtToken)
//        if (orgPublicKey.isNotBlank() && orgPrivateKey.isNotBlank()) {
//            jwtProperties.publicKey = orgPublicKey
//            jwtProperties.privateKey = orgPrivateKey
//            jwtManager.init()
//            jwtToken = jwtManager.generateToken(username)
//            validateJwtToken(username, jwtToken)
//        }
//
//        // 유효시간 3초
//        jwtProperties.accessTokenValidity = 3000
//        jwtProperties.refreshTokenValidity = 3000
//        jwtToken = jwtManager.generateToken(username)
//        val accessTokenExpiration: ZonedDateTime = jwtToken.accessTokenExpiresAt
//        val accessToken: String = jwtToken.accessToken
//        jwtProperties.accessTokenValidity = orgAccessTokenValidity
//        jwtProperties.refreshTokenValidity = orgRefreshTokenValidity
//
//        // 대기
//        delay(accessTokenExpiration.toInstant().toEpochMilli() - System.currentTimeMillis() + 100)
//
//        // 후 확인
//        assertFalse(jwtManager.validateToken(accessToken))
//    }
//
//    @Suppress("SameParameterValue")
//    private fun validateJwtToken(username: String, jwtToken: JwtToken) {
//
//        val accessToken: String = jwtToken.accessToken
//        assertTrue(jwtManager.validateToken(accessToken))
//        assertEquals(username, jwtManager.getUsernameFromToken(accessToken))
//    }
//
//}
