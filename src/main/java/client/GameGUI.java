package main.java.client;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import main.java.model.*;

public class GameGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField chatInput;
    private JButton hitButton, standButton, restartButton;
    private JPanel playerPanel, dealerPanel;
    private JLabel statusLabel, chipLabel;
    private PrintWriter out;
    private Deck deck;
    private Hand playerHand, dealerHand;
    private int chips = 1000;

    public GameGUI(PrintWriter out) {
        this.out = out;
        setTitle("블랙잭 온라인");
        setSize(900, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 카드 영역
        dealerPanel = new JPanel();
        dealerPanel.setBackground(new Color(0, 80, 0));
        playerPanel = new JPanel();
        playerPanel.setBackground(new Color(0, 80, 0));

        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        centerPanel.add(dealerPanel);
        centerPanel.add(playerPanel);
        add(centerPanel, BorderLayout.CENTER);

        // 버튼/상태
        JPanel controlPanel = new JPanel();
        hitButton = new JButton("Hit");
        standButton = new JButton("Stand");
        restartButton = new JButton("Restart");
        chipLabel = new JLabel("💰 칩: " + chips);
        statusLabel = new JLabel("게임 시작!");
        controlPanel.add(hitButton);
        controlPanel.add(standButton);
        controlPanel.add(restartButton);
        controlPanel.add(chipLabel);
        controlPanel.add(statusLabel);
        add(controlPanel, BorderLayout.NORTH);

        // 채팅창
        chatArea = new JTextArea(10, 40);
        chatArea.setEditable(false);
        chatInput = new JTextField();
        chatInput.addActionListener(e -> sendChat());

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatPanel.add(chatInput, BorderLayout.SOUTH);
        add(chatPanel, BorderLayout.SOUTH);

        // 이벤트 연결
        hitButton.addActionListener(e -> hit());
        standButton.addActionListener(e -> stand());
        restartButton.addActionListener(e -> restart());

        restart();
        setVisible(true);
    }

    private void sendChat() {
        String msg = chatInput.getText().trim();
        if (!msg.isEmpty()) {
            out.println("CHAT:" + msg);
            chatInput.setText("");
        }
    }

    private void hit() {
        playerHand.addCard(deck.draw());
        refresh();
        if (playerHand.getValue() > 21) {
            statusLabel.setText("버스트! 패배했습니다.");
            chips -= 100;
            chipLabel.setText("💰 칩: " + chips);
            disablePlay();
        }
    }

    private void stand() {
        while (dealerHand.getValue() < 17) {
            dealerHand.addCard(deck.draw());
        }
        refresh();
        int playerValue = playerHand.getValue();
        int dealerValue = dealerHand.getValue();
        String result;
        if (dealerValue > 21 || playerValue > dealerValue) {
            result = "승리!";
            chips += 100;
        } else if (playerValue == dealerValue) {
            result = "무승부!";
        } else {
            result = "패배!";
            chips -= 100;
        }
        statusLabel.setText(result);
        chipLabel.setText("💰 칩: " + chips);
        disablePlay();
    }

    private void restart() {
        deck = new Deck();
        playerHand = new Hand();
        dealerHand = new Hand();
        playerHand.addCard(deck.draw());
        playerHand.addCard(deck.draw());
        dealerHand.addCard(deck.draw());
        dealerHand.addCard(deck.draw());
        refresh();
        statusLabel.setText("새 게임 시작!");
        hitButton.setEnabled(true);
        standButton.setEnabled(true);
    }

    private void disablePlay() {
        hitButton.setEnabled(false);
        standButton.setEnabled(false);
    }

    private void refresh() {
        dealerPanel.removeAll();
        playerPanel.removeAll();
        for (Card c : dealerHand.getCards())
            dealerPanel.add(new JLabel(new ImageIcon(c.getImage())));
        for (Card c : playerHand.getCards())
            playerPanel.add(new JLabel(new ImageIcon(c.getImage())));
        dealerPanel.revalidate();
        playerPanel.revalidate();
        dealerPanel.repaint();
        playerPanel.repaint();
    }

    public void appendMessage(String msg) {
        chatArea.append(msg + "\n");
    }
}
