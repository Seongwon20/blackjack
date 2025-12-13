# 0. Getting Started (시작하기)
```bash
서버: java -cp out\classes server.BlackjackServer

클라이언트: java -cp out\classes client.BlackjackClient
```


<br/>
<br/>

# 1. Project Overview (프로젝트 개요)
- 프로젝트 이름: BlackJack Online(블랙잭 온라인)
- 프로젝트 설명: 채팅을 기반으로 하는 블랙잭 게임

<br/>
<br/>

# 2. Team Members (팀원 및 팀 소개)
| 구자혁 | 진성원 |
|:------:|:------:|
| <img src="https://github.com/user-attachments/assets/c1c2b1e3-656d-4712-98ab-a15e91efa2da" alt="구자혁" width="150"> | <img src="https://github.com/user-attachments/assets/39d82e3c-2f52-4224-abf6-68fe169d82ca" alt="진성원" width="150"> |
| [GitHub](https://github.com/JaHyeok2002) | [GitHub](https://github.com/Seongwon20) |

<br/>
<br/>

# 3. Key Features (주요 기능)
- **플레이어 선택**:
  - 시작화면에서 플레이어1, 플레이어2를 선택 할 수 있다.
- **배팅 기능**:
  - All-In, 50, 10, 5, 1 칩을 통해 사용자가 원하는 만큼을 배팅 할 수 있다.
- **HIT/STAND**:
  - 카드를 받고 더 받을 지, 그만 받을 지 선택 할 수 있다.
- **채팅기능**:
  - 게임 중 언제라도 채팅 기능을 통해 상대방과 소통 할 수 있다.



<br/>
<br/>

# 4. Tasks & Responsibilities (작업 및 역할 분담)
|  |  |  |
|-----------------|-----------------|-----------------|
| 구자혁    |  <img src="https://github.com/user-attachments/assets/c1c2b1e3-656d-4712-98ab-a15e91efa2da" alt="구자혁" width="100"> | <ul><li>서버 로직 개발</li><li>카드/덱/모델 클래스 구현</li></ul>     |
| 진성원   |  <img src="https://github.com/user-attachments/assets/39d82e3c-2f52-4224-abf6-68fe169d82ca" alt="진성원" width="100">| <ul><li>ClientGUI 개발</li><li>StartScreen 개발</li><li>카드 렌더링 및 전체 UI 개발</li></ul> |


<br/>

# 5. 프로젝트 구조 
```plaintext
src
└── main
    ├── java
    │   ├── client
    │   │   ├── BlackjackClient.java
    │   │   ├── GameGUI.java
    │   │   └── StartScreen.java
    │   │
    │   ├── model
    │   │   ├── Card.java
    │   │   ├── Deck.java
    │   │   ├── Hand.java
    │   │   └── Player.java
    │   │
    │   └── server
    │       └── BlackjackServer.java
    │
    └── resources
        ├── chips/
        ├── club/
        ├── diamond/
        ├── heart/
        ├── spade/
        └── start.png
```

<br/>

# 6. 실행 화면 사진
<p align="center">
  <img width="1085" height="743" alt="Image" src="https://github.com/user-attachments/assets/09091358-ec5e-4c4d-b893-a167ce663c44" />
  <img width="1918" height="1030" alt="Image" src="https://github.com/user-attachments/assets/435279fe-f2d9-46f3-a45f-bf067c8dcaf3" />
</p>
