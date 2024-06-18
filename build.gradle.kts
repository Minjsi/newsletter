@file:Suppress("SpellCheckingInspection")

import nu.studer.gradle.jooq.JooqEdition
import nu.studer.gradle.jooq.JooqGenerate
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jooq.meta.jaxb.GeneratedSerialVersionUID
import org.jooq.meta.jaxb.Logging
import org.jooq.meta.jaxb.OnError
import org.jooq.meta.jaxb.Property

plugins {
    val kotlinVersion = System.getProperty("kotlinVersion")
    val springBootVersion = System.getProperty("springBootVersion")

    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
//    kotlin("plugin.serialization") version kotlinVersion

    id("org.springframework.boot") version springBootVersion
    id("io.spring.dependency-management") version "1.1.0"
    id("nu.studer.jooq") version "8.2.1"
}

group = "io.happytalk"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

val kotlinVersion = System.getProperty("kotlinVersion")!!
val springBootVersion = System.getProperty("springBootVersion")!!
val coroutineVersion = project.properties["coroutineVersion"]
val lombokVersion = project.properties["lombokVersion"]
val reactorVersion = project.properties["reactorVersion"]
val jooqVersion = project.properties["jooqVersion"]

configurations {
    all {
        // R2DBC 사용 > spring-boot-starter-jdbc 제외
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-jdbc")
        // log4j 관련 라이브러리 제외
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

sourceSets {
    named("main") {
        java {
            srcDirs(
                layout.buildDirectory.dir("src/main").get().asFile,
                layout.buildDirectory.dir("generated/jooq").get().asFile
            )
        }
    }
}

dependencies {

    // Java 코드에서 Lombok 사용 위해 추가
    // Known-issue: Kotlin Code 에서 @Data 등 Lombok 사용한 Java Class 의 필드 접근시 오류 발생
    //  > Cannot access 'fieldName': it is private in 'JavaClass'
    //  > getter/setter 등 Java <-> Kotlin 사이에 같이 사용해야 하는 Java Class 의 경우 Lombok 제거
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    // Spring @ConfigurationProperties > spring-configuration-metadata.json 생성
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    // Kotlin data class 에서 @ConfigurationProperties 어노테이션 사용하기 위해
    kapt("org.springframework.boot:spring-boot-configuration-processor")

    // Language, Core Framework
    implementation("org.jetbrains:annotations:24.0.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:$coroutineVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.projectreactor:reactor-core:$reactorVersion")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.2")
    implementation("org.yaml:snakeyaml:2.2")

    // Logger
    implementation("io.github.oshai:kotlin-logging:5.1.0")

    // Data Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")

    // R2DBC
    implementation("com.github.jasync-sql:jasync-r2dbc-mysql:2.2.4") {
        exclude(group = "io.github.oshai", module = "kotlin-logging")
        exclude(group = "io.github.oshai", module = "kotlin-logging-jvm")
    }
    implementation("io.r2dbc:r2dbc-proxy:1.1.2.RELEASE")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.3")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.3")

    // 기타
    implementation("com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.5")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
    implementation("me.paulschwarz:spring-dotenv:4.0.0")

    // jOOQ
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.jooq:jooq:$jooqVersion")
    // jOOQ 코드 생성시에만 JDBC 사용 (실제 어플리케이션 에서는 R2DBC 사용됨)
    jooqGenerator("mysql:mysql-connector-java:8.0.32")

    // OpenAPI Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.2.0")

    // Test
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test:6.1.5")
    testImplementation("org.springframework.amqp:spring-rabbit-test:3.0.10")
    testImplementation("io.projectreactor:reactor-test:$reactorVersion")
}

kapt {
    // kapt 플러그인 추가 후 아래 옵션을 사용하지 않으면 Java 코드에서 @Slf4j 어노테이션 등이 제대로 빌드되지 않음
    keepJavacAnnotationProcessors = true
}

jooq {

    version.set("$jooqVersion")  // default (can be omitted)
    edition.set(JooqEdition.OSS)  // default (can be omitted)

    configurations {
        create("main") {  // name of the jOOQ configuration
            generateSchemaSourceOnCompilation.set(true)  // default (can be omitted)

            jooqConfiguration.apply {

                logging = Logging.WARN
                onError = OnError.FAIL

                // 빌드 파이프라인에서 접근할 수 있는 DB 필요
                // > 때문에 개발 DB 와 운영 DB 스키마 차이가 발생하면 빌드단계에서 부터 failure
                // > 배포 후 runtime 시 failture 발생하는것 보다 안전
                jdbc.apply {
                    driver = "com.mysql.cj.jdbc.Driver"
                    url = "jdbc:mysql://127.0.0.1:3306/example_db"
                    user = "root"
                    password = "1234"
                    properties.add(Property().apply {
                        key = "ssl"
                        value = "true"
                    })
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.mysql.MySQLDatabase"
                        inputSchema = "example_db"
                        includes = ".*"
                        excludes = "test_.* | temp_.*"
                        withUnsignedTypes(false)
                    }
                    generate.apply {
                        generatedSerialVersionUID = GeneratedSerialVersionUID.CONSTANT
                        isJavaTimeTypes = true
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "io.happytalk.jooq"
                        directory = "src/generated/jooq"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}

tasks.bootRun {
    systemProperty("spring.profiles.active", "local")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// participate in incremental builds and build caching
tasks.named<JooqGenerate>("generateJooq") {
    allInputsDeclared.set(true)
}
