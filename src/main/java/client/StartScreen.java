package client;

import javax.swing.*;
import java.awt.*;

public class StartScreen extends JFrame {

    public StartScreen() {
        setTitle("Blackjack Start");
        setSize(1100, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        //절대 경로로 배경 이미지 로드
        String bgPath = System.getProperty("user.dir") + "\\src\\main\\resources\\start.png";
        JLabel bgLabel = new JLabel();
        Image bg = new ImageIcon(bgPath).getImage().getScaledInstance(1100, 750, Image.SCALE_SMOOTH);
        bgLabel.setIcon(new ImageIcon(bg));
        bgLabel.setBounds(0, 0, 1100, 750);
        add(bgLabel);

        JButton p1 = new JButton("PLAYER 1");
        JButton p2 = new JButton("PLAYER 2");

        p1.setBounds(300, 600, 200, 50);
        p2.setBounds(600, 600, 200, 50);

        p1.setFont(new Font("Arial", Font.BOLD, 20));
        p2.setFont(new Font("Arial", Font.BOLD, 20));
        p1.setFocusPainted(false);
        p2.setFocusPainted(false);

        bgLabel.setLayout(null);
        bgLabel.add(p1);
        bgLabel.add(p2);

        p1.addActionListener(e -> {
            new BlackjackClient("localhost", 5555, "PLAYER1");
            dispose();
        });

        p2.addActionListener(e -> {
            new BlackjackClient("localhost", 5555, "PLAYER2");
            dispose();
        });

        setVisible(true);
    }
}