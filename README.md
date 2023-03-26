# Peoples
> 스터디원 출결, 아직도 엑셀로 관리하시나요? 이제 피플즈가 도와드릴게요!

![피플즈_배너](https://user-images.githubusercontent.com/87797716/226782373-32471849-1493-4aff-ba67-576cdd5318a3.png)

## 개발 목표
### 1. 스터디 만들기? 어렵지 않아요~~
- 스터디를 하고 싶은데 찾을 수가 없었던 적이 있으신가요? 
- 그렇지만 만들기에는 부담스러우셨죠?
- 원하는 스터디가 없을 때 직접 만드는 것도 망설이지 않도록, 스터디 관리를 도와주는 **비서**가 되어드릴게요.

### 2. 스터디에 관한 모든 것, 관리해줄게!
- 스터디룸 예약 서비스 등 스터디를 할 때 필요한 모든 것을 해결해줄 수 있는 어플이 되는게 피플즈의 목표에요.

## 개발 환경
- Spring boot / gradle / Jpa
- mySql
- Intellij / dataGrip

## 배포 서버
- AWS EC2(Ubuntu 20.04), S3, RDS
- Elastic Ip, Route 53, Certificate manager, Application Load balancer

## ERD
![image](https://user-images.githubusercontent.com/87797716/227778610-c5ec0b8c-702b-4beb-b368-9bb23064ccf4.png)


## 주요 기능
### 1. JWT Token
- HTTP는 stateless 상태이므로, Request마다 유저를 식별하고 인증과 인가를 해야 한다.
- 쿠키에 세션 id를 발급하여 매 요청마다 브라우저의 쿠키를 검증하여 세션 아이디를 통해 사용자를 인증하는 쿠키와 세션 방식에는 클라이언트가 인증 정보를 책임져야한다는 보안성에서 취약하게 된다.
- 위와 더불어 native app 에서는 session 사용이 불가능하여 Jwt Token을 도입했다.
- Token 탈취를 방지하기 위해 만료기간이 짧은 access token과 상대적으로 만료기간이 긴 refresh token 두 형태로 발급한다.
- jwt token을 이용한 인증/인가 플로우는 다음과 같다.
> 1. 클라이언트에서 사용자가 로그인 요청
> 2. 서버에서 db에 저장된 회원 조회
> 3. 서버는 Access Token 과 Refresh Token 발급하여 response Header에 Token을 담아서 클라이언트에 응답
> 4. 이후 모든 요청에서 클라이언트는 request Header에 Token을 담아서 요청
> 5. 서버에서는 Token 검증하고 통과된 요청에게만 응답
> 6. 만약 서버에서 token 검증 단계에서 access token 이 만료되었다면 클라이언트에게 access token이 만료되었다는 응답을 보냄
> 7. 클라이언트는 token 만료 응답을 확인 후, request Header에 access token과 refresh token을 담아 Token 재발급 요청
> 8. 서버는 Refresh Token 검증 후, access token 과 refresh token을 재발급하여 response Header에 token을 담아 클라이언트로 응답
> 9. 클라이언트는 새로운 토큰을 담아서 재요청

- 인증과 인가가 필요한 요청을 필터링하기위해 OncePerRequestFilter를 상속받는 JwtAuthenticationProcessingFilter 추가
> 1. OncePerRequestFilter는 요청당 단 한번의 필터 실행을 보장하는 기본 filter이다.
> 2. 해당 필터를 상속받은 JwtAuthenticationProcessingFilter 에서 인증과 인가가 필요한 요청을 필터링하여 jwt token 검증 단계를 거친다.

## 진행하며 겪은 문제점 및 해결
### 1. response 무한재귀
> jpa를 사용하면서 양방향 연관관계 맵핑하여 사용하면서 발생한 문제이다.
> 1. 양방향 참조된 Entity를 Controller에서 return하게 되면, Entity가 참조하고 있는 객체는 지연 로딩되고, 로딩된 객체는 또다시 참조하고 있는 객체를 호출하게 된다.
> 2. 서로 참조하는 객체를 계속 호출하면서 무한재귀 문제가 발생하였다.
> 3. 이를 해결하기 위해서 몇가지 방법 ( Json으로 직렬화할 속성 무시, 직렬화 방향 설정, 식별키로 구분하여 더이상 참조되지 않게 하기, DTO 사용하기 ) 중 클라이언트에서도 편리한 작업환경을 제공하기 위해서 DTO를 이용하는 방법으로 해결하였다.

## 아쉬운 점
- 목표는 앱을 실제 배포하여 서비스를 잠깐이나마 운영해보는 것이었으나, 클라이언트 단 개발이 늦어지면서 자연스럽게 사이드 프로젝트가 종료되었다.
> 실제 서비스를 운영하면서 생기는 문제점들을 경험해볼 수 있는 좋은 기회였는데 아쉬움이 많이 남는다.
> 하지만 실제 배포를 목표로 api server를 직접 구현해보는 과정에서 보안설정과 HTTPS 적용 등을 다뤄볼 수 있어서 좋았다.

# API
https://app.gitbook.com/o/7Qa6miUFzBp1ZKjBL4b5/s/E57Z6epCcPhNFUfUEU0p/
