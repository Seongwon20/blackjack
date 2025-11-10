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
        dealerHand.addCard(deck.draw());
        dealerHand.addCard(deck.draw());

        // 첫 턴
        broadcast("GAME:START");

        // 딜러는 첫 번째 카드만 공개
        broadcast("GAME:CARD:DEALER:" + formatCard(dealerHand.getCards().get(0)));
        broadcast("GAME:CARD:HIDDEN"); // 딜러의 두 번째 카드는 숨김 처리

        // 플레이어 카드 전송
        broadcast("GAME:CARD:PLAYER1:" + formatCards(p1.getHand()));
        broadcast("GAME:CARD:PLAYER2:" + formatCards(p2.getHand()));

        // 턴 시작
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

    /** ✅ 딜러 카드 공개 */
    private void revealDealerCards() {
        broadcast("CHAT:[SYSTEM] 딜러 카드 공개!");
        broadcast("GAME:CARD:DEALER:" + formatCards(dealerHand));

        while (dealerHand.getValue() < 17) {
            dealerHand.addCard(deck.draw());
            broadcast("GAME:CARD:DEALER:" + formatCards(dealerHand));
        }

        int dealerValue = dealerHand.getValue();
        if (dealerValue > 21) {
            broadcast("CHAT:[SYSTEM] 딜러 버스트! (" + dealerValue + ")");
        } else {
            broadcast("CHAT:[SYSTEM] 딜러 최종 점수: " + dealerValue);
        }

        broadcastResults();
    }

    /** ✅ 결과 표시 */
    private void broadcastResults() {
        int d = dealerHand.getValue();
        int v1 = p1.getHand().getValue();
        int v2 = p2.getHand().getValue();

        broadcast("CHAT:[RESULT] 딜러(" + d + ")  P1(" + v1 + ")  P2(" + v2 + ")");
    }

    // ===== 클라이언트 스레드 =====
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

        /** ✅ 턴 넘김 */
        private void nextTurn() {
            turn++;
            if (turn == 3) {
                revealDealerCards();
            } else if (turn == 2) {
                broadcast("GAME:TURN:PLAYER2");
            }
        }

        void send(String msg) {
            try {
                out.write(msg + "\n");
                out.flush();
            } catch (IOException ignored) {}
        }
    }
}
