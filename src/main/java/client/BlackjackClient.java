package main.java.client;

import javax.swing.*;
import java.io.*;
import java.net.*;

public class BlackjackClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private GameGUI gui;

    public BlackjackClient(String host, int port) {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            gui = new GameGUI(out);
            new Thread(this::listen).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "서버 연결 실패!");
        }
    }

    private void listen() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                gui.appendMessage(msg);
            }
        } catch (IOException ignored) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BlackjackClient("localhost", 5555));
    }
}
