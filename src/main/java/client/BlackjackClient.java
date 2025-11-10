package client;

import javax.swing.*;
import java.io.*;
import java.net.*;

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
                if (msg.startsWith("CHAT:")) gui.appendMessage(msg.substring(5));
                else if (msg.startsWith("GAME:CARD:")) gui.applyCardMessage(msg);
                else if (msg.startsWith("GAME:TURN:")) gui.setTurn(msg.substring(10));
                else if (msg.startsWith("WAITING:")) gui.appendMessage(msg.substring(8));
                else gui.appendMessage(msg);
            }
        } catch (IOException ignored) {}
    }

    public static void main(String[] args) {
        String[] options = {"PLAYER1", "PLAYER2"};
        String role = (String) JOptionPane.showInputDialog(
                null, "역할 선택:", "블랙잭 클라이언트",
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (role != null)
            SwingUtilities.invokeLater(() -> new BlackjackClient("localhost", 5555, role));
    }
}
