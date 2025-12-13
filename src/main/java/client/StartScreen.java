package client;

import javax.swing.*;
import java.awt.*;

public class StartScreen extends JFrame {
    
    public StartScreen() {
        setTitle("Blackjack Start");
        setSize(1100, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //절대 좌표 기반 배치를 사용하기 위해 LayoutManager 제거
        setLayout(null);

        //절대 경로로 배경 이미지 로드
        //게임 시작 화면의 분위기를 연출하기 위한 UI 요소
        String bgPath = System.getProperty("user.dir") + "\\src\\main\\resources\\start.png";
        JLabel bgLabel = new JLabel();
        Image bg = new ImageIcon(bgPath).getImage().getScaledInstance(1100, 750, Image.SCALE_SMOOTH);
        bgLabel.setIcon(new ImageIcon(bg));
        bgLabel.setBounds(0, 0, 1100, 750);
        add(bgLabel);

        //PLAYER1 / PLAYER2 선택 버튼 생성
        JButton p1 = new JButton("PLAYER 1");
        JButton p2 = new JButton("PLAYER 2");

        //버튼 위치 및 크기 지정
        p1.setBounds(300, 600, 200, 50);
        p2.setBounds(600, 600, 200, 50);

        //버튼 글꼴 설정
        p1.setFont(new Font("Arial", Font.BOLD, 20));
        p2.setFont(new Font("Arial", Font.BOLD, 20));

        //버튼 포커스 테두리 제거
        p1.setFocusPainted(false);
        p2.setFocusPainted(false);

        //배경 이미지 위에 버튼을 올리기 위해 레이아웃 제거
        bgLabel.setLayout(null);
        bgLabel.add(p1);
        bgLabel.add(p2);

        //PLAYER1 선택 시
        //PLAYER1 역할로 클라이언트 생성
        p1.addActionListener(e -> {
            new BlackjackClient("localhost", 5555, "PLAYER1");
            dispose();
        });

        //PLAYER2 선택 시
        //PLAYER2 역할로 클라이언트 생성
        p2.addActionListener(e -> {
            new BlackjackClient("localhost", 5555, "PLAYER2");
            dispose();
        });

        //시작 화면 표시
        setVisible(true);
    }
}
