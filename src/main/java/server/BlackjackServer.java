package main.java.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class BlackjackServer {
    private static final int PORT = 5555;
    private static final List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("서버 실행 중... 포트: " + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(socket);
            clients.add(handler);
            new Thread(handler).start();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String playerName;

        public ClientHandler(Socket socket) { this.socket = socket; }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("이름을 입력하세요:");
                playerName = in.readLine();
                broadcast("[시스템] " + playerName + "님이 입장했습니다.");

                String msg;
                while ((msg = in.readLine()) != null) {
                    if (msg.equalsIgnoreCase("exit")) break;
                    if (msg.startsWith("CHAT:")) {
                        broadcast(playerName + ": " + msg.substring(5));
                    } else if (msg.startsWith("GAME:")) {
                        broadcast("[게임 이벤트] " + msg.substring(5));
                    }
                }

            } catch (IOException e) {
                System.out.println("클라이언트 연결 종료");
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
                clients.remove(this);
                broadcast("[시스템] " + playerName + "님이 퇴장했습니다.");
            }
        }

        private void broadcast(String message) {
            for (ClientHandler client : clients) {
                client.out.println(message);
            }
        }
    }
}
