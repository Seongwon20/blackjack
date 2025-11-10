package client;

import javax.swing.*;
import java.io.*;
import java.net.*;

/**
 * 같은 클래스를 두 번 실행해 2명이 접속합니다.
 * 서버가 접속 순서에 따라 ROLE(PLAYER1/PLAYER2)을 배정해 줍니다.
 */
public class BlackjackClient {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private GameGUI gui;

    public BlackjackClient(String host, int port) {
        try {
            socket = new Socket(host, port);
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream(), "CP949"));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "CP949"));

            gui = new GameGUI(msg -> send(msg)); // 서버로 보내는 콜백
            new Thread(this::listen).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "서버 연결 실패!\n" + e.getMessage(),
                    "연결 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void send(String line) {
        try {
            out.write(line + "\n");
            out.flush();
        } catch (IOException ignored) {}
    }

    private void listen() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                // 프로토콜 분기
                if (msg.startsWith("WAITING:")) {
                    gui.setWaiting(true, msg.substring(8));
                } else if (msg.startsWith("GAME:ROLE:")) {
                    gui.setMyRole(msg.substring("GAME:ROLE:".length()).trim()); // PLAYER1 / PLAYER2
                } else if (msg.equals("GAME:START")) {
                    gui.setWaiting(false, "게임이 시작되었습니다!");
                    gui.resetTable();
                } else if (msg.startsWith("GAME:CARD:")) {
                    // 예: GAME:CARD:DEALER:spade-K  / GAME:CARD:PLAYER1:heart-7
                    gui.applyCardMessage(msg);
                } else if (msg.startsWith("GAME:TURN:")) {
                    gui.setTurn(msg.substring("GAME:TURN:".length()).trim()); // PLAYER1/PLAYER2/DEALER
                } else if (msg.startsWith("GAME:RESULT:")) {
                    gui.appendMessage("[결과] " + msg.substring("GAME:RESULT:".length()));
                    gui.disablePlayButtons();
                } else if (msg.startsWith("CHAT:")) {
                    gui.appendMessage(msg.substring(5));
                } else {
                    gui.appendMessage(msg);
                }
            }
        } catch (IOException ignored) {
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BlackjackClient("localhost", 5555));
    }
}
