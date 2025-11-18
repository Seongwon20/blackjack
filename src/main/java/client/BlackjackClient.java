package client;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class BlackjackClient {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private GameGUI gui;

    public BlackjackClient(String host, int port, String role) {
        try {
            socket = new Socket(host, port);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            gui = new GameGUI(line -> out.println(line));
            gui.setMyRole(role);

            out.println("MODE:" + role);

            new Thread(this::listen).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "서버 연결 실패!\n" + e.getMessage());
        }
    }

    private void listen() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {

                if (msg.startsWith("CHAT:")) {
                    gui.appendMessage(msg.substring(5));
                }
                //게임 리셋 신호 처리
                else if (msg.equals("GAME:RESET")) {
                    gui.resetTable();
                }
                else if (msg.startsWith("GAME:CARD:")) {
                    gui.applyCardMessage(msg);
                }
                else if (msg.startsWith("GAME:TURN:")) {
                    gui.setTurn(msg.substring(10));
                }
                else if (msg.startsWith("CHIPS:P1:")) {
                    gui.updateChips("PLAYER1", Integer.parseInt(msg.substring(9)));
                }
                else if (msg.startsWith("CHIPS:P2:")) {
                    gui.updateChips("PLAYER2", Integer.parseInt(msg.substring(9)));
                }
                else if (msg.equals("INFO:BETTING")) {
                    gui.enableBetting();
                }
                else if (msg.startsWith("WAITING:")) {
                    gui.appendMessage(msg.substring(8));
                }
                else if (msg.startsWith("INFO:새 라운드")) {
                    gui.appendMessage(msg.substring(5));
                }
                else {
                    gui.appendMessage(msg);
                }
            }
        } catch (IOException ignored) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StartScreen::new);
    }
}