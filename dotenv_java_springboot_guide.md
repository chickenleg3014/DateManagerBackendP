# Spring Boot에서 dotenv-java 라이브러리 사용 가이드

Spring Boot 프로젝트에서 환경변수나 민감 정보(API 키, DB 접속 정보 등)를 안전하고 편리하게 관리하기 위해 `.env` 파일을 활용할 때 **`dotenv-java`** 라이브러리를 사용합니다.

스프링 부트 실행 시점에 `.env` 파일의 변수를 **System Property**로 등록하여 `application.yml`이나 `@Value`에서 사용할 수 있도록 설정하는 종합 가이드입니다.

---

## 1. 의존성 추가 (Dependency)

프로젝트의 빌드 도구에 맞춰 의존성을 추가합니다.

### Gradle (`build.gradle`)
```groovy
dependencies {
    // 최신 버전 확인 후 사용
    implementation 'io.github.cdimascio:dotenv-java:3.0.0'
}
```

### Maven (`pom.xml`)
```xml
<dependency>
    <groupId>io.github.cdimascio</groupId>
    <artifactId>dotenv-java</artifactId>
    <version>3.0.0</version>
</dependency>
```

---

## 2. `.env` 파일 작성

프로젝트의 **최상위 루트 경로**(src 폴더와 동일한 위치)에 `.env` 파일을 생성합니다.

> ⚠️ **주의:** `.env` 파일에는 보안 민감 정보가 포함되므로 **`.gitignore`에 반드시 등록**하여 Git 레포지토리에 올라가지 않도록 설정해야 합니다.

### `.env` 작성 예시
```properties
SERVER_PORT=8080
DB_URL=jdbc:mysql://localhost:3306/mydb
DB_USERNAME=root
DB_PASSWORD=secret_password!
JWT_SECRET=my_super_secret_jwt_key_123456
```

### `.gitignore` 추가
```gitignore
# .env 파일 보안 예외 처리
.env
```

---

## 3. Spring Boot 시작 시점에 `.env` 로드하기

Spring Boot가 `application.yml`을 읽기 **전에** `.env` 파일의 변수를 System Property로 등록해 주어야 설정 파일에서 자유롭게 가져다 쓸 수 있습니다.

### Main 클래스 설정 (`DemoApplication.java`)

```java
package com.example.demo;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        // 1. .env 파일 로드 (DotenvBuilder 설정)
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing() // .env 파일이 없어도 에러를 발생시키지 않음
                .load();

        // 2. 읽어온 환경변수를 System Property로 등록
        dotenv.entries().forEach(entry -> 
            System.setProperty(entry.getKey(), entry.getValue())
        );

        // 3. Spring Boot 애플리케이션 실행
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

---

## 4. `ignoreIfMissing()` 옵션 상세 안내

### 위치
`Dotenv.configure()`와 `.load()` 사이에 체이닝(Chaining) 방식으로 호출합니다.

```java
Dotenv dotenv = Dotenv.configure()
        .ignoreIfMissing() // <--- 이 위치
        .load();
```

### 역할 및 필요성
- **미사용 시:** 루트 경로에 `.env` 파일이 없으면 `DotenvException` 오류를 내며 애플리케이션 실행이 중단됩니다.
- **사용 시:** `.env` 파일이 없어도 에러 없이 넘어갑니다. 
- **활용 시나리오:** 로컬 환경에서는 `.env` 파일을 사용하고, Docker/AWS/Kubernetes 등 운영 환경에서는 실제 OS 시스템 환경변수를 주입받아 사용할 때 유용합니다.

---

## 5. 설정 파일 및 코드에서 활용하기

System Property로 등록된 값은 표준 Spring 표현식인 `${...}`로 참조할 수 있습니다.

### `application.yml`에서 참조

```yaml
server:
  port: ${SERVER_PORT:8080} # .env의 SERVER_PORT 사용 (기본값 8080)

spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

jwt:
  secret: ${JWT_SECRET}
```

### 자바 코드에서 참조 (`@Value`)

```java
package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public void printSecret() {
        System.out.println("Loaded JWT Secret: " + jwtSecret);
    }
}
```

---

## 💡 실무 적용 팁

1. **`.env.example` 파일 공유**
   * `.env` 파일 자체는 Git에 올리지 않되, 프로젝트 구성원 공유용으로 키 이름만 정의된 `.env.example` 파일을 커밋해 두는 것이 권장됩니다.
2. **배포 환경 구분**
   * **로컬 개발:** `.env` 파일 생성 후 로컬용 설정값 적용
   * **운영/CI/CD:** `.ignoreIfMissing()`을 적용한 상태에서 CI/CD 파이프라인이나 컨테이너 실행 시 OS 환경변수(Environment Variables)로 직접 주입
