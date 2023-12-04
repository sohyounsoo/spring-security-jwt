# spring-security-jwt

현재 개발중... API 추가 예정

Java 17, Spring Boot 3.0.8
Spring Security + JWT를 활용한 인증 인가 템플릿입니다.  
flyway DB 마이그레이션 도구 사용합니다.  
소프트웨어 아키텍처로 레이어드 아키텍처를 사용합니다.  


### 일반 유저 등록 API  
```java  
POST /api/v1/member  
```
request
```  
{
    username: String,
    password: String,
    nickname: String
}
```


### 계정 인증 API
```java
POST /api/v1/accounts/token
```
request
```  
{
    username: String,
    password: String
}
```


### 액세스 토큰, 리프레시 토큰 갱신 API
```java
PUT /api/v1/accounts/token
```
request
```  
{
    refreshToken: String
}
```


### 유저조회 API
```java
GET /api/v1/accounts/token
```
request
```  
-H "Authorization: Bearer ${ACCESS_TOKEN}"
```


### 일반 계정 탈퇴 API  
```java  
Delete /api/v1/member/{userName}/delete  
```
request
```  
-H "Authorization: Bearer ${ACCESS_TOKEN}"
```
