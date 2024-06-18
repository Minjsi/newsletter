# spring-boot-3.x-template

`Spring WebFlux` 환경에서 보편적으로 사용하는(보통은 항상) 기본적인 프로젝트 구성 템플릿

---

### 주요 스택

|        구분         |                                                                                              사용 프레임워크 / 라이브러리                                                                                              |
|:-----------------:|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|     Language      |                                                                                     [Kotlin](https://kotlinlang.org/)                                                                                      |
|  Core Framework   |                                                             [Spring Boot](https://docs.spring.io/spring-boot/docs/3.1.1/reference/htmlsingle/)                                                             |
|        Web        |                                                       [Webflux](https://docs.spring.io/spring-framework/docs/6.0.8/reference/html/web-reactive.html)                                                       |
|  RDB Connection   |                                                                                         [R2DBC](https://r2dbc.io/)                                                                                         |
|   RDB Operation   | [Spring Core Data Access](https://docs.spring.io/spring-framework/docs/6.0.8/reference/html/data-access.html)<br/>[Spring Data R2DBC](https://docs.spring.io/spring-data/r2dbc/docs/3.1.1/reference/html/) |
| API Documentation |                                                               [OpenAPI 3](https://springdoc.org/v2/)<br/>[RapiDoc](https://rapidocweb.com/)                                                                |

### 선택적 스택

|             구분              |       사용 프레임워크 / 라이브러리        |
|:---------------------------:|:-----------------------------:|
| SQL Builder / RDB Operation | [jOOQ](https://www.jooq.org/) |

---

### 코드 스타일

* IntelliJ
  * Settings > Editor > Code Style > Import Schema
  * `MBI Code Style` [mbi-code-style.xml](mbi-code-style.xml) 적용

---

### 템플릿 주요 구성 요소

* MbiSecret, MbiSecretJasypt
  * MBI 에서 사용하는 설정 암복호화 설정 파일 로드 및 application.yml 에 적용
  * System 환경변수 `MBI_SECRET_PATH` 에 설정된 파일 로딩

---

### 템플릿 예제, API, RapiDoc 사용 방법

* .env 파일 셋팅
  * [.env.example](.env.example) 파일을 .env 이름으로 복사
    * DB 접속정보 127.0.0.1:3306 / example_db / read, write 계정 정보 입력되어있는 상태
* 예제 DB 스키마 생성
  * [example_init.sql](./src/main/resources/schema/example_init.sql) 파일을 참고하여 DB, Account, Table 생성
  * 템플릿 최초 기준 jOOQ Code generation 이 [build.gradle.kts](build.gradle.kts) 에 포함되어있기 때문에 example_init.sql 에 해당되는 DB 접속 정보
    필수
* 빌드
  * JDK 17 로 셋팅
  * `./gradlew build`
  * 또는 IDE 에서 Gradle > build > build 수행
* 실행
  * `./gradlew bootRun`
  * bootRun 기본 Active Profile 은 `local` 로 셋팅해놓은 상태
  * 또는 IDE 에서 [HappytalkBackendApplication.run.xml](.run/HappytalkBackendApplicationKt.run.xml) 사용해서 Run/Debug
* 정상 구동 확인
  * http://localhost:8080
* OpenAPI RapiDoc 확인
  * http://localhost:8080/static/doc/rapidoc.html
    * Local Example 하위 API 통해 기본적인 예제 확인

---

### 템플릿 프로젝트 실 사용 전 정리

* 사용하지 않는 선택적 스펙 삭제
  * jOOQ
    * jOOQ [자동생성 소스](./src/generated/jooq) ./src/generated/jooq 디렉토리 삭제
    * 프로젝트 root 디렉토리에서 jooq 키워드로 검색해서 관련된 부분들 삭제
* Module 이름 수정
  * [settings.gradle.kts](settings.gradle.kts) 파일에서 `rootProject.name` 을 사용할 프로젝트 명으로 수정
    * repository 명으로 수정
  * 만약 IDE 로 이미 열었던 상태라면 창 닫고 .idea 디렉토리 삭제 후 다시 오픈
* Root Package 수정
  * [io.happytalk.](./src/main/kotlin/co/happytalk/boot3) 에 커서 두고 `Shift` + `F6` (Refactor > Rename) > io.happytalk. 를 원하는 패키지로
    수정
    * 예) io.happytalk. > me.myname.myapp
  * [프로젝트 root](.) 에 커서를 두고 `Ctrl` + `Shift` + `R` (Replace in Path) > io.happytalk. 를 원하는 패키지로 수정
    * 예) io.happytalk. > me.myname.myapp
  * [build.gradle.kts](build.gradle.kts) > group = "co.happytalk" 를 변경한 패키지명에 맞게 변경
    * 예) co.happytalk > me.myname
* Main Class 명 수정
  * [Boot3Application.kt](./src/main/kotlin/co/happytalk/boot3/Boot3Application.kt) 에 커서 두고 `Shift` + `F6` (Refactor >
    Rename) > 원하는 클래스명으로 수정
    * 예) Boot3Application.kt > MyApplication.kt

---

### Kotlin + Java 문제 해결

* [개발환경] Spring Configuration 메타 데이터 생성
  * `@ConfigurationProperties` annotation 사용하는 [application.yml](./src/main/resources/application.yml) Mapping 용도의
    data class 들의 필드들을 final (val) 로 지정할
    경우 [spring-boot-configuration-processor](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-configuration-metadata.html#configuration-metadata-annotation-processor)
    annotationProcessor 에서 생성되는 `spring-configuration-metadata.json` 파일의 `properties` 가 제대로 생성되지 않는 문제
    * 해결: data class 의 필드들을 non-final (var) 로 지정

* Lombok 사용
  * Kotlin Code 에서 @Data 등 Lombok 사용한 Java Class 의 필드 접근시 오류 발생
    ```text
    Cannot access 'fieldName': it is private in 'JavaClass'
    ```
* `getter`, `setter` 등 Java <-> Kotlin 사이에 같이 사용해야 하는 Java Class 의 경우 Lombok 제거
