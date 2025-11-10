package server;

import model.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class BlackjackServer {
    private static final int PORT = 5555;
    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private Deck deck;
    private Hand dealerHand;
    private Player p1, p2;
    private boolean gameStarted = false;
    private int turn = 1; // 1=PLAYER1, 2=PLAYER2, 3=딜러 턴

    public static void main(String[] args) {
        new BlackjackServer().startServer();
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("서버 실행 중... 포트 " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler client = new ClientHandler(socket);
                clients.add(client);
                new Thread(client).start();
            }
        } catch (IOException e) {
            System.out.println("[서버 오류] " + e.getMessage());
        }
    }

    private void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler c : clients) c.send(message);
        }
    }

    /** ✅ 게임 시작 */
    private void startNewRound() {
        if (clients.size() < 2 || gameStarted) return;
        gameStarted = true;
        System.out.println("두 명 접속됨 → 게임 시작!");

        deck = new Deck();
        dealerHand = new Hand();
        p1 = new Player("PLAYER1", 1000);
        p2 = new Player("PLAYER2", 1000);

        // 초기 분배
        p1.getHand().addCard(deck.draw());
        p1.getHand().addCard(deck.draw());
        p2.getHand().addCard(deck.draw());
        p2.getHand().addCard(deck.draw());
        dealerHand.addCard(deck.draw()); // 공개 1장
        dealerHand.addCard(deck.draw()); // 비공개 1장

        broadcast("GAME:START");
        broadcast("GAME:CARD:DEALER:" + formatCard(dealerHand.getCards().get(0))); // 첫 장만 공개
        broadcast("GAME:CARD:PLAYER1:" + formatCards(p1.getHand()));
        broadcast("GAME:CARD:PLAYER2:" + formatCards(p2.getHand()));

        broadcast("GAME:TURN:PLAYER1");
    }

    private String formatCard(Card c) {
        return c.getSuit() + "-" + c.getRank();
    }

    private String formatCards(Hand hand) {
        List<String> list = new ArrayList<>();
        for (Card c : hand.getCards()) list.add(formatCard(c));
        return String.join(",", list);
    }

    /** ✅ 딜러 턴 (플레이어 턴 종료 후 실행) */
    private void revealDealerCards() {
        broadcast("CHAT:[SYSTEM] 딜러 카드 공개!");

        // 숨긴 카드 포함해서 순차적으로 공개
        for (int i = 0; i < dealerHand.getCards().size(); i++) {
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            broadcast("GAME:CARD:DEALER:" + formatCards(dealerHand));
        }

        // 17 미만이면 1초 간격으로 드로우
        while (dealerHand.getValue() < 17) {
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            Card newCard = deck.draw();
            dealerHand.addCard(newCard);
            broadcast("GAME:CARD:DEALER:" + formatCards(dealerHand));

            int dealerValue = dealerHand.getValue();
            if (dealerValue > 21) {
                broadcast("CHAT:[SYSTEM] 딜러 버스트! (" + dealerValue + ")");
                broadcastResults();
                resetGame();
                return;
            }
        }

        int dealerValue = dealerHand.getValue();
        broadcast("CHAT:[SYSTEM] 딜러 최종 점수: " + dealerValue);
        broadcastResults();
        resetGame();
    }

    /** ✅ 승패 판단 로직 (딜러 버스트 문구 추가됨) */
    private void broadcastResults() {
        int d = dealerHand.getValue();
        int v1 = p1.getHand().getValue();
        int v2 = p2.getHand().getValue();

        String dealerText = (d > 21) ? (d + ", 버스트") : String.valueOf(d);

        String r1, r2;
        if (v1 > 21) r1 = "패배 (버스트)";
        else if (d > 21 || v1 > d) r1 = "승리!";
        else if (v1 == d) r1 = "무승부";
        else r1 = "패배";

        if (v2 > 21) r2 = "패배 (버스트)";
        else if (d > 21 || v2 > d) r2 = "승리!";
        else if (v2 == d) r2 = "무승부";
        else r2 = "패배";

        broadcast("CHAT:[RESULT] 딜러(" + dealerText + ")  P1(" + v1 + " → " + r1 + ")  P2(" + v2 + " → " + r2 + ")");
    }

    /** ✅ 다음 라운드를 위해 초기화 */
    private void resetGame() {
        gameStarted = false;
        turn = 1;
    }

    // ===================== 클라이언트 스레드 =====================
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

                send("WAITING:상대 플레이어를 기다리는 중…");

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("MODE:")) {
                        role = line.substring(5).trim();
                        System.out.println(role + " 접속됨");
                        checkPlayersAndStart();
                    } else if (line.startsWith("CHAT:")) {
                        broadcast("CHAT:[" + role + "] " + line.substring(5));
                    } else if (line.startsWith("GAME:HIT")) {
                        handleHit(role);
                    } else if (line.startsWith("GAME:STAND")) {
                        handleStand(role);
                    }
                }
            } catch (IOException e) {
                System.out.println("클라이언트 종료: " + e.getMessage());
            } finally {
                clients.remove(this);
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        private void checkPlayersAndStart() {
            long cnt = clients.stream().filter(c -> c.role.startsWith("PLAYER")).count();
            if (cnt >= 2) startNewRound();
            else broadcast("WAITING:상대 플레이어를 기다리는 중…");
        }

        private void handleHit(String role) {
            Player cur = role.equals("PLAYER1") ? p1 : p2;
            cur.getHand().addCard(deck.draw());
            broadcast("GAME:CARD:" + role + ":" + formatCards(cur.getHand()));
            if (cur.getHand().getValue() > 21) {
                broadcast("CHAT:[" + role + "] 버스트!");
                nextTurn();
            }
        }

        private void handleStand(String role) {
            broadcast("CHAT:[" + role + "] Stand!");
            nextTurn();
        }

        private void nextTurn() {
            turn++;
            if (turn == 2) broadcast("GAME:TURN:PLAYER2");
            else if (turn == 3) revealDealerCards();
        }

        void send(String msg) {
            try {
                out.write(msg + "\n");
                out.flush();
            } catch (IOException ignored) {}
        }
    }
}
