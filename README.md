# 🚀 [DateManagerBackend] 백엔드 API

>위치 데이터를 기반으로 맞춤형 데이트 코스를 추천하고 생성해 주는 백엔드 API 서버

---

## 🛠 기술 스택

- **Language & Framework:** Java 21, Spring Boot 4.x
- **Database** Oracle 18c XE
- **ORM:** Spring Data JPA
- **API Docs:** Swagger (OpenAPI 3.0)
- **Deployment:** 추후 입력

---


## 🏗 아키텍처 및 ERD

### System Architecture
![Architecture Diagram](이미지_링크_또는_다이어그램)

### ERD
![ERD 이미지 링크](이미지_링크)

---

## 🚀 Getting Started

### Prerequisites
- IntelliJ IDEA
- Java 21 이상
- OracleDB 18c XE 이상

### 환경변수 설정 가이드
하나하나 윈도우 환경변수에 추가하기에는 번거롭기 때문에 
외부 라이브러리를 의존성에 추가하여 
.env파일에 application.yaml에 넣을 값을 정리하고자 합니다.

**1.의존성 추가**
build.gradle dependency에 dotenv-java 라이브러리를 추가합니다
``` implementation 'io.github.cdimascio:dotenv-java:3.0.0' ```
이후 src폴더와 동일 위치에 .env파일을 생성하여 .env.example에 적혀있는 양식대로 본인에게 맞는 값을 입력합니다.

### REST API Documentation
![Swagger-UI]((http://localhost:8080/swagger-ui/index.html))

### 권장 개발 환경
- IDE:IntelliJ IDEA(ultimate)