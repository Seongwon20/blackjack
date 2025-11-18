package client;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class StartScreen extends JFrame {

    public StartScreen() {

        setTitle("Blackjack Start");
        setSize(1100, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null); // 절대 좌표 배치

        // =====================================
        //  배경 이미지 불러오기
        // =====================================
        URL bgURL = getClass().getResource("/start.png");  // 너가 보내준 이미지를 start.png로 저장
        JLabel bgLabel = new JLabel();

        if (bgURL != null) {
            Image bg = new ImageIcon(bgURL).getImage().getScaledInstance(1100, 750, Image.SCALE_SMOOTH);
            bgLabel.setIcon(new ImageIcon(bg));
        }

        bgLabel.setBounds(0, 0, 1100, 750);
        add(bgLabel);

        // =====================================
        //  버튼 2개 (Player1, Player2)
        // =====================================
        JButton p1 = new JButton("PLAYER 1");
        JButton p2 = new JButton("PLAYER 2");

        p1.setBounds(300, 600, 200, 50);
        p2.setBounds(600, 600, 200, 50);

        p1.setFont(new Font("Arial", Font.BOLD, 20));
        p2.setFont(new Font("Arial", Font.BOLD, 20));

        p1.setFocusPainted(false);
        p2.setFocusPainted(false);

        // 버튼 위로 올리기
        bgLabel.setLayout(null);
        bgLabel.add(p1);
        bgLabel.add(p2);

        // =====================================
        // 버튼 클릭 → BlackjackClient 실행
        // =====================================
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
