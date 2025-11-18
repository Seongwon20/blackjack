package server;

import model.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class BlackjackServer {

    private static final int PORT = 5555;

    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    private Player p1, p2;
    private Deck deck;
    private Hand dealerHand;

    private int betCount = 0;      // 두 플레이어 모두 배팅하면 2가 됨
    private boolean roundInProgress = false;

    public static void main(String[] args) {
        new BlackjackServer().startServer();
    }

    // --------------------- 서버 시작 ---------------------
    public void startServer() {
        System.out.println("==== Blackjack Server 실행됨 ====");

        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = server.accept();
                ClientHandler ch = new ClientHandler(socket);
                clients.add(ch);
                new Thread(ch).start();
            }
        } catch (IOException e) {
            System.out.println("서버 오류: " + e.getMessage());
        }
    }

    // --------------------- 브로드캐스트 ---------------------
    private void broadcast(String msg) {
        synchronized (clients) {
            for (ClientHandler c : clients) c.send(msg);
        }
    }

    // --------------------- 라운드 초기화 ---------------------
    private void startNewRound() {

        roundInProgress = false;
        betCount = 0;

        deck = new Deck();
        dealerHand = new Hand();

        p1.getHand().getCards().clear();
        p2.getHand().getCards().clear();

        p1.resetBet();
        p2.resetBet();

        broadcast("CHAT:[SYSTEM] 새로운 라운드를 시작합니다. 배팅을 해주세요.");
        broadcast("INFO:BETTING");
    }

    // --------------------- 첫 카드 분배 ---------------------
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

        // 딜러 첫 장만 공개
        Card open = dealerHand.getCards().get(0);
        broadcast("GAME:CARD:DEALER:" + open.getSuit() + "-" + open.getRank());

        broadcast("GAME:TURN:PLAYER1");
    }

    // --------------------- 카드 포맷 ---------------------
    private String formatCards(Hand h) {
        List<String> list = new ArrayList<>();
        for (Card c : h.getCards()) list.add(c.getSuit() + "-" + c.getRank());
        return String.join(",", list);
    }

    // --------------------- 칩 상태 업데이트 ---------------------
    private void updateChips() {
        broadcast("CHIPS:P1:" + p1.getChips());
        broadcast("CHIPS:P2:" + p2.getChips());
    }

    // --------------------- 딜러 턴 ---------------------
    private void dealerTurn() {

        broadcast("CHAT:[SYSTEM] 딜러 턴 시작.");
        broadcast("GAME:CARD:DEALER:" + formatCards(dealerHand));

        while (dealerHand.getValue() < 17) {
            try { Thread.sleep(900); } catch (Exception ignored) {}
            dealerHand.addCard(deck.draw());
            broadcast("GAME:CARD:DEALER:" + formatCards(dealerHand));
        }

        evaluateResults();
    }

    // --------------------- 승패 판정 ---------------------
    private void evaluateResults() {

        int d = dealerHand.getValue();
        int v1 = p1.getHand().getValue();
        int v2 = p2.getHand().getValue();

        String r1 = resultText(v1, d);
        String r2 = resultText(v2, d);

        broadcast("CHAT:[RESULT] 딜러(" + d +
                ") | P1(" + v1 + ":" + r1 +
                ") | P2(" + v2 + ":" + r2 + ")");

        applyChipResults(p1, v1, d);
        applyChipResults(p2, v2, d);

        updateChips();

        if (p1.getChips() <= 0 || p2.getChips() <= 0) {
            broadcast("CHAT:[SYSTEM] 누군가 칩이 0개라 게임 종료.");
            return;
        }

        try { Thread.sleep(2000); } catch (Exception ignored) {}

        startNewRound();
    }

    private String resultText(int v, int d) {
        if (v > 21) return "버스트";
        if (d > 21) return "승리";
        if (v > d) return "승리";
        if (v == d) return "무승부";
        return "패배";
    }

    // --------------------- 칩 정산 ---------------------
    private void applyChipResults(Player p, int v, int d) {

        int bet = p.getBetAmount();

        // 무승부 → 배팅금 반환
        if (v == d) {
            p.winChips(bet);
            return;
        }

        // 버스트 → 배팅 잃음
        if (v > 21) {
            p.loseChips(bet);
            return;
        }

        // 딜러 버스트 → 승리
        if (d > 21) {
            p.winChips(bet);
            return;
        }

        // 블랙잭 3:2 지급
        if (v == 21 && p.getHand().getCards().size() == 2) {
            p.winChips((int)(bet * 1.5));
            return;
        }

        // 일반 승리
        if (v > d) {
            p.winChips(bet);
        } else {
            p.loseChips(bet);
        }
    }

    // --------------------- 클라이언트 핸들러 ---------------------
    private class ClientHandler implements Runnable {

        private Socket socket;
        private BufferedReader in;
        private BufferedWriter out;

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

                    // 역할 설정
                    if (line.startsWith("MODE:")) {
                        role = line.substring(5);

                        if (role.equals("PLAYER1")) p1 = new Player("PLAYER1");
                        if (role.equals("PLAYER2")) p2 = new Player("PLAYER2");

                        if (p1 != null && p2 != null) {
                            broadcast("CHAT:[SYSTEM] 두 플레이어 연결됨. 배팅 시작!");
                            startNewRound();
                        }
                    }

                    // 채팅
                    else if (line.startsWith("CHAT:")) {
                        broadcast("CHAT:[" + role + "] " + line.substring(5));
                    }

                    // --------------------- 배팅 ---------------------
                    else if (line.startsWith("BET:")) {
                        handleBet(role, line.substring(4));
                    }

                    // --------------------- HIT ---------------------
                    else if (line.startsWith("GAME:HIT")) {
                        handleHit(role);
                    }

                    // --------------------- STAND ---------------------
                    else if (line.startsWith("GAME:STAND")) {
                        handleStand(role);
                    }

                }

            } catch (IOException e) {
                System.out.println("클라이언트 종료: " + role);
            }

            clients.remove(this);
        }

        // --------------------- 배팅 처리 ---------------------
        private void handleBet(String role, String amount) {

            Player cur = (role.equals("PLAYER1") ? p1 : p2);

            int chips = cur.getChips();
            int bet = amount.equals("ALL") ? chips : Integer.parseInt(amount);

            if (bet <= 0 || bet > chips) {
                send("CHAT:[SYSTEM] 잘못된 배팅입니다.");
                return;
            }

            cur.setBetAmount(bet);
            betCount++;

            broadcast("CHAT:[" + role + "] " + bet + "칩 배팅");

            // **두 플레이어 모두 배팅함 → 라운드 시작**
            if (betCount == 2) {

                try { Thread.sleep(600); } catch (Exception ignored) {}

                dealInitialCards();
            }
        }

        // --------------------- HIT ---------------------
        private void handleHit(String role) {

            if (!roundInProgress) return;

            Player cur = role.equals("PLAYER1") ? p1 : p2;

            cur.getHand().addCard(deck.draw());
            broadcast("GAME:CARD:" + role + ":" + formatCards(cur.getHand()));

            // 버스트 처리
            if (cur.getHand().getValue() > 21) {
                broadcast("CHAT:[" + role + "] 버스트!");

                if (role.equals("PLAYER1")) {
                    broadcast("GAME:TURN:PLAYER2");
                } else {
                    dealerTurn();
                }
            }
        }

        // --------------------- STAND ---------------------
        private void handleStand(String role) {
            broadcast("CHAT:[" + role + "] Stand");

            if (role.equals("PLAYER1")) {
                broadcast("GAME:TURN:PLAYER2");
            } else {
                dealerTurn();
            }
        }

        // --------------------- 단일 전송 ---------------------
        private void send(String msg) {
            try {
                out.write(msg + "\n");
                out.flush();
            } catch (IOException ignored) {}
        }
    }
}
