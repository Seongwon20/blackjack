package server;

import model.*;
import java.io.*;
import java.net.*;
import java.util.*;

/*
 * BlackjackServer
 * =================================================
 * [역할]
 * - 블랙잭 게임의 서버 역할
 * - 클라이언트 접속 관리
 * - 게임 라운드 진행, 턴 제어, 카드 분배, 승패 판정 담당
 *
 * [서버 구조]
 * - ServerSocket으로 클라이언트 접속 대기
 * - 각 클라이언트는 ClientHandler 스레드로 처리
 * - 모든 게임 로직은 서버에서 단일 기준으로 관리
 */
public class BlackjackServer {

    //서버 포트 번호
    private static final int PORT = 5555;

    //접속 중인 클라이언트 목록
    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    //PLAYER1, PLAYER2 객체
    private Player p1, p2;

    //카드 덱
    private Deck deck;

    //딜러의 손패
    private Hand dealerHand;

    //현재 라운드 진행 여부
    private boolean roundInProgress = false;

    //각 플레이어의 배팅 완료 여부
    //두 값이 모두 true가 되면 카드 분배 시작
    private boolean p1Done = false;
    private boolean p2Done = false;

    //서버 시작 지점
    public static void main(String[] args) {
        new BlackjackServer().startServer();
    }

    /*
     * startServer()
     * -------------------------------------------------
     * ServerSocket 생성 후 클라이언트 접속을 지속적으로 대기
     */
    public void startServer() {
        System.out.println("==== Blackjack Server 실행됨 ====");

        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                //클라이언트 접속 수락
                Socket socket = server.accept();

                //클라이언트 전용 핸들러 생성
                ClientHandler ch = new ClientHandler(socket);
                clients.add(ch);

                //각 클라이언트를 개별 스레드로 처리
                new Thread(ch).start();
            }
        } catch (IOException e) {
            System.out.println("서버 오류: " + e.getMessage());
        }
    }

    /*
     * broadcast()
     * -------------------------------------------------
     * 모든 클라이언트에게 메시지를 전송
     */
    private void broadcast(String msg) {
        synchronized (clients) {
            for (ClientHandler c : clients) c.send(msg);
        }
    }

    /*
     * startNewRound()
     * -------------------------------------------------
     * 새 라운드 초기화
     * - 상태 변수 초기화
     * - 카드 덱 재생성
     * - 손패 및 베팅 초기화
     */
    private void startNewRound() {

        roundInProgress = false;

        p1Done = false;
        p2Done = false;

        deck = new Deck();
        dealerHand = new Hand();

        p1.getHand().clear();
        p2.getHand().clear();

        p1.resetBet();
        p2.resetBet();

        broadcast("GAME:RESET");
        broadcast("CHAT:[SYSTEM] 새로운 라운드를 시작합니다. 배팅을 해주세요.");
        broadcast("INFO:BETTING");
    }

    /*
     * dealInitialCards()
     * -------------------------------------------------
     * 초기 카드 분배
     * - 각 플레이어 2장
     * - 딜러 2장 (1장은 공개)
     * - PLAYER1부터 턴 시작
     */
    private void dealInitialCards() {
        roundInProgress = true;

        p1.getHand().addCard(deck.draw());
        p1.getHand().addCard(deck.draw());

        p2.getHand().addCard(deck.draw());
        p2.getHand().addCard(deck.draw());

        dealerHand.addCard(deck.draw());
        dealerHand.addCard(deck.draw());

        broadcast("GAME:CARD:PLAYER1:" + formatCards(p1.getHand()));
        broadcast("GAME:CARD:PLAYER2:" + formatCards(p2.getHand()));

        //딜러의 첫 카드만 공개
        Card open = dealerHand.getCards().get(0);
        broadcast("GAME:CARD:DEALER:" + open.getSuit() + "-" + open.getRank());

        broadcast("GAME:TURN:PLAYER1");
    }

    /*
     * formatCards()
     * -------------------------------------------------
     * Hand 객체를 "suit-rank,suit-rank" 문자열로 변환
     * 클라이언트 카드 표시용
     */
    private String formatCards(Hand h) {
        List<String> list = new ArrayList<>();
        for (Card c : h.getCards()) list.add(c.getSuit() + "-" + c.getRank());
        return String.join(",", list);
    }

    /*
     * updateChips()
     * -------------------------------------------------
     * 모든 클라이언트에 현재 칩 정보 전송
     */
    private void updateChips() {
        broadcast("CHIPS:P1:" + p1.getChips());
        broadcast("CHIPS:P2:" + p2.getChips());
    }

    /*
     * dealerTurn()
     * -------------------------------------------------
     * 딜러의 턴 진행
     * - 점수가 17 미만이면 카드 추가
     * - 종료 후 승패 판정
     */
    private void dealerTurn() {

        broadcast("GAME:TURN:DEALER");

        broadcast("CHAT:[SYSTEM] 딜러 턴 시작.");
        broadcast("GAME:CARD:DEALER:" + formatCards(dealerHand));

        while (dealerHand.getValue() < 17) {
            try { Thread.sleep(900); } catch (Exception ignored) {}
            dealerHand.addCard(deck.draw());
            broadcast("GAME:CARD:DEALER:" + formatCards(dealerHand));
        }

        evaluateResults();
    }

    /*
     * evaluateResults()
     * -------------------------------------------------
     * 딜러와 각 플레이어의 점수를 비교하여 결과 판정
     */
    private void evaluateResults() {

        int d = dealerHand.getValue();
        int v1 = p1.getHand().getValue();
        int v2 = p2.getHand().getValue();

        broadcast("CHAT:[RESULT] 딜러(" + d +
                ") | P1(" + v1 + ") | P2(" + v2 + ")");

        applyChipResults(p1, v1, d);
        applyChipResults(p2, v2, d);

        updateChips();

        //칩이 0인 플레이어가 있으면 게임 종료
        if (p1.getChips() <= 0 || p2.getChips() <= 0) {
            broadcast("CHAT:[SYSTEM] 누군가 칩이 0개라 게임 종료.");
            return;
        }

        try { Thread.sleep(6000); } catch (Exception ignored) {}

        startNewRound();
    }

    /*
     * applyChipResults()
     * -------------------------------------------------
     * 단일 플레이어의 승패 결과에 따른 칩 계산
     */
    private void applyChipResults(Player p, int v, int d) {

        int bet = p.getBetAmount();

        //무승부: 베팅 금액 반환
        if (v == d) {
            p.winChips(bet);
            return;
        }

        //플레이어 버스트
        if (v > 21) return;

        //딜러 버스트
        if (d > 21) {
            p.winChips(bet * 2);
            return;
        }

        //블랙잭 (2장 21)
        if (v == 21 && p.getHand().getCards().size() == 2) {
            p.winChips((int)(bet * 2.5));
            return;
        }

        //일반 승리
        if (v > d) {
            p.winChips(bet * 2);
        }
    }

    /*
     * ClientHandler
     * =================================================
     * 각 클라이언트와의 통신을 담당하는 내부 클래스
     * Runnable을 구현하여 스레드로 실행됨
     */
    private class ClientHandler implements Runnable {

        private Socket socket;
        private BufferedReader in;
        private BufferedWriter out;

        //클라이언트 역할
        private String role = "UNKNOWN";

        ClientHandler(Socket socket) { this.socket = socket; }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

                send("WAITING:상대 플레이어 연결을 기다리는 중...");

                String line;
                while ((line = in.readLine()) != null) {

                    //역할 설정 메시지
                    if (line.startsWith("MODE:")) {
                        role = line.substring(5);

                        if (role.equals("PLAYER1")) p1 = new Player("PLAYER1");
                        if (role.equals("PLAYER2")) p2 = new Player("PLAYER2");

                        if (p1 != null && p2 != null) {
                            broadcast("CHAT:[SYSTEM] 두 플레이어 연결됨. 배팅 시작!");
                            startNewRound();
                        }
                    }
                    //채팅 메시지
                    else if (line.startsWith("CHAT:")) {
                        broadcast("CHAT:[" + role + "] " + line.substring(5));
                    }
                    //배팅 완료 신호
                    else if (line.equals("BET:DONE")) {
                        handleBetDone(role);
                    }
                    //배팅 금액 처리
                    else if (line.startsWith("BET:")) {
                        handleBet(role, line.substring(4));
                    }
                    //Hit 요청
                    else if (line.startsWith("GAME:HIT")) {
                        handleHit(role);
                    }
                    //Stand 요청
                    else if (line.startsWith("GAME:STAND")) {
                        handleStand(role);
                    }
                }
            } catch (IOException e) {
                System.out.println("클라이언트 종료: " + role);
            }
            clients.remove(this);
        }

        /*
         * 배팅 완료 처리
         * 두 플레이어 모두 완료 시 카드 분배 시작
         */
        private void handleBetDone(String role) {
            if (role.equals("PLAYER1")) p1Done = true;
            else p2Done = true;

            broadcast("CHAT:[" + role + "] 배팅 완료");

            if (p1Done && p2Done && !roundInProgress) {
                try { Thread.sleep(600); } catch (Exception ignored) {}
                dealInitialCards();
            }
        }

       
        //배팅 금액 처리
        private void handleBet(String role, String amount) {
            Player cur = (role.equals("PLAYER1") ? p1 : p2);

            int chips = cur.getChips();
            int bet = amount.equals("ALL") ? chips : Integer.parseInt(amount);

            if (chips < bet) {
                send("CHAT:[SYSTEM] 칩이 부족합니다.");
                return;
            }

            cur.loseChips(bet);
            cur.setBetAmount(cur.getBetAmount() + bet);

            broadcast("CHAT:[" + role + "] +" + bet + "칩 (총 배팅 " + cur.getBetAmount() + ")");
            updateChips();
        }

        //Hit 처리
        private void handleHit(String role) {
            if (!roundInProgress) return;

            Player cur = role.equals("PLAYER1") ? p1 : p2;

            cur.getHand().addCard(deck.draw());
            broadcast("GAME:CARD:" + role + ":" + formatCards(cur.getHand()));

            if (cur.getHand().getValue() > 21) {
                broadcast("CHAT:[" + role + "] 버스트!");
                if (role.equals("PLAYER1")) broadcast("GAME:TURN:PLAYER2");
                else dealerTurn();
            }
        }

        //Stand 처리
        private void handleStand(String role) {
            broadcast("CHAT:[" + role + "] Stand");
            if (role.equals("PLAYER1")) broadcast("GAME:TURN:PLAYER2");
            else dealerTurn();
        }

        //단일 클라이언트 메시지 전송
        private void send(String msg) {
            try {
                out.write(msg + "\n");
                out.flush();
            } catch (IOException ignored) {}
        }
    }
}
