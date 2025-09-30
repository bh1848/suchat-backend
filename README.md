# Suchat Backend (랜덤 채팅 서비스)

## 📌 프로젝트 개요
**Suchat**은 실시간 랜덤 채팅 서비스를 제공하는 플랫폼으로,  
사용자 인증, 회원 관리, 보안 기능을 갖춘 백엔드 시스템입니다.  

이 프로젝트는 **Spring Boot 기반 REST API**와 **JWT 인증**을 적용하여,  
안전하고 신뢰성 있는 실시간 채팅 환경을 제공합니다.

---

## 🛠 Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot
- **Database**: MySQL
- **Security**: Spring Security, JWT
- **Auth**: Email 인증 (토큰 기반)
- **Build Tool**: Gradle
- **Others**: JPA, REST API

---

## ⚙️ 아키텍처 & 주요 기능
- **Controller**: REST API 제공 (회원가입, 로그인, 인증 요청)  
- **Service**: 비즈니스 로직 처리 (회원 관리, 이메일 인증, 토큰 검증)  
- **Repository**: JPA 기반 데이터 접근  
- **Security**: Spring Security + JWT 기반 인증/인가  
- **EmailAuth**: 이메일 인증 토큰 발급 및 만료 처리 (스케줄러 포함)  
- **Response Wrapper**: 일관성 있는 API 응답 구조 제공  

### 주요 기능
- **회원 관리**
  - 회원가입, 로그인, 회원 정보 관리
- **JWT 인증**
  - Access Token / Refresh Token 발급 및 검증
- **이메일 인증**
  - 회원가입 시 이메일 토큰 발급 → 검증 완료 후 활성화
  - 토큰 만료 자동 처리 (스케줄러)
- **공통 응답 처리**
  - 단일/리스트/폼 응답 일원화

---

## 👤 My Role (방혁)
본 프로젝트에서 저는 **보안, 인증, 회원 관리 로직**을 집중적으로 개발했습니다.

- **인증·보안**
  - **JWT 기반 인증·인가 로직 구현** (Spring Security)  
  - Access Token / Refresh Token 설계 및 적용  
- **회원 관리**
  - 회원가입 / 로그인 API 및 서비스 로직 개발  
  - DTO 설계 (SignUpRequest, SignInResponse 등)  
- **이메일 인증**
  - 이메일 인증 토큰 발급 및 검증 로직 구현  
  - 만료 토큰 자동 삭제 스케줄러 작성  
- **공통 응답 구조**
  - Response Wrapper 설계 (Single, List, FormResponse)  
