package client;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class GameGUI extends JFrame {

    public interface Sender { void send(String line); }

    //서버로 메시지를 전송하기 위한 콜백 객체
    private final Sender sender;

    //게임 진행 버튼 (Hit / Stand)
    private JButton hitButton, standButton;

    //배팅 버튼들 (칩 단위 배팅)
    private JButton betAll, bet50, bet10, bet5, bet1, betDone;

    //게임 상태 표시 라벨들
    private JLabel statusLabel, turnLabel, roleLabel, chipLabel;

    //카드가 표시되는 영역 패널
    private JPanel dealerPanel, p1Panel, p2Panel;

    //배팅 버튼 영역 패널
    private JPanel betPanel;

    //채팅 출력 영역과 입력 필드
    private JTextArea chatArea;
    private JTextField chatInput;

    //자신의 역할(PLAYER1 / PLAYER2)
    private String myRole = "UNKNOWN";

    //현재 턴이 누구인지
    private String turnRole = "UNKNOWN";

    //카드 뒷면 이미지 아이콘
    private ImageIcon backIcon;

    //딜러의 숨겨진 카드 표현용 라벨
    private JLabel dealerHiddenCardLabel = null;

    //화면 중앙에 결과를 크게 표시하기 위한 메시지 라벨
    private JLabel centerMessage;

    public GameGUI(Sender sender) {
        this.sender = sender;

        setTitle("블랙잭 온라인 (2인)");
        setSize(1000, 760);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());


        centerMessage = new JLabel("", SwingConstants.CENTER);
        centerMessage.setFont(new Font("Tmoney RoundWind ExtraBold", Font.BOLD, 48));
        centerMessage.setForeground(Color.RED);
        centerMessage.setVisible(false);
        getLayeredPane().add(centerMessage, JLayeredPane.PALETTE_LAYER);

        //창 크기 변경 시 중앙 메시지 위치 재조정
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                centerMessage.setBounds(
                        0,
                        getHeight() / 2 - 150,
                        getWidth(),
                        100
                );
            }
        });

        //카드 뒷면 이미지 로드
        String backPath = System.getProperty("user.dir") + "\\src\\main\\resources\\back.png";
        backIcon = new ImageIcon(
                new ImageIcon(backPath).getImage().getScaledInstance(90, 140, Image.SCALE_SMOOTH)
        );

        // 상단 컨트롤바
        JPanel control = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));

        //게임 액션 버튼
        hitButton = new JButton("Hit");
        standButton = new JButton("Stand");

        //상태 표시용 라벨
        statusLabel = new JLabel("대기중");
        turnLabel = new JLabel("턴: -");
        roleLabel = new JLabel("내 역할: -");
        chipLabel = new JLabel("칩: 100");

        //컨트롤바 구성
        control.add(hitButton);
        control.add(standButton);
        control.add(new JLabel(" | "));
        control.add(roleLabel);
        control.add(new JLabel(" | "));
        control.add(turnLabel);
        control.add(new JLabel(" | "));
        control.add(chipLabel);
        control.add(new JLabel(" | "));
        control.add(statusLabel);

        add(control, BorderLayout.NORTH);

        //카드 테이블 공간
        //딜러 / PLAYER1 / PLAYER2 카드 영역
        JPanel table = new JPanel(new GridLayout(3, 1, 8, 8));
        dealerPanel = titledPanel("DEALER");
        p1Panel = titledPanel("PLAYER1");
        p2Panel = titledPanel("PLAYER2");

        table.add(dealerPanel);
        table.add(p1Panel);
        table.add(p2Panel);

        add(table, BorderLayout.CENTER);

        //하단 배팅 + 채팅
        JPanel bottomPanel = new JPanel(new BorderLayout());

        //배팅 패널
        betPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        betPanel.setBorder(BorderFactory.createTitledBorder("배팅"));

        //배팅 버튼 생성
        betAll = createChipButton("chip_allin.png", "ALL");
        bet50 = createChipButton("chip50.png", "50");
        bet10 = createChipButton("chip10.png", "10");
        bet5  = createChipButton("chip5.png", "5");
        bet1  = createChipButton("chip1.png", "1");

        //배팅 완료 버튼 (서버에 배팅 종료 신호 전송)
        betDone = new JButton("배팅 완료");
        betDone.setPreferredSize(new Dimension(120, 60));
        betDone.addActionListener(e -> sender.send("BET:DONE"));

        //배팅 패널 구성
        betPanel.add(betAll);
        betPanel.add(bet50);
        betPanel.add(bet10);
        betPanel.add(bet5);
        betPanel.add(bet1);
        betPanel.add(betDone);

        bottomPanel.add(betPanel, BorderLayout.NORTH);

        // 채팅 UI
        chatArea = new JTextArea(8, 60);
        chatArea.setEditable(false);
        chatInput = new JTextField();

        //Enter 입력 시 채팅 메시지 서버 전송
        chatInput.addActionListener(e -> {
            String msg = chatInput.getText().trim();
            if (!msg.isEmpty()) {
                sender.send("CHAT:" + msg);
                chatInput.setText("");
            }
        });

        JPanel chatBox = new JPanel(new BorderLayout());
        chatBox.setBorder(BorderFactory.createTitledBorder("채팅"));
        chatBox.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatBox.add(chatInput, BorderLayout.SOUTH);

        bottomPanel.add(chatBox, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        //버튼 기능
        //Hit 버튼: 내 턴일 때만 서버로 요청
        hitButton.addActionListener(e -> {
            if (isMyTurn()) sender.send("GAME:HIT");
            else appendMessage("내 턴이 아닙니다.");
        });

        //Stand 버튼: 내 턴일 때만 서버로 요청
        standButton.addActionListener(e -> {
            if (isMyTurn()) sender.send("GAME:STAND");
            else appendMessage("내 턴이 아닙니다.");
        });

        //초기에는 버튼 비활성화
        disableButtons();
        setVisible(true);
    }

    // 중앙 메시지 출력 (승/패 결과 표시)
    public void showCenterMessage(String msg) {
        centerMessage.setText(msg);
        centerMessage.setVisible(true);

        //5초 후 자동으로 메시지 제거
        Timer t = new Timer(5000, e -> centerMessage.setVisible(false));
        t.setRepeats(false);
        t.start();
    }

    // 칩 버튼 생성 + 눌림 효과
    private JButton createChipButton(String fileName, String value) {
        String path = System.getProperty("user.dir") +
                "\\src\\main\\resources\\chips\\" + fileName;

        ImageIcon icon = new ImageIcon(
                new ImageIcon(path).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH)
        );

        JButton btn = new JButton(icon);
        btn.setPreferredSize(new Dimension(70, 70));

        //버튼 배경 제거 (이미지만 표시)
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);

        //버튼 눌림 시 어둡게 보이는 효과
        btn.getModel().addChangeListener(e -> {
            ButtonModel m = btn.getModel();
            if (m.isPressed()) {
                btn.setIcon(makeDarkerIcon(icon, 0.75f));
            } else {
                btn.setIcon(icon);
            }
        });

        //배팅 값 서버 전송
        btn.addActionListener(e -> sendBet(value));
        return btn;
    }

    //이미지 어둡게 처리
    private ImageIcon makeDarkerIcon(ImageIcon original, float alpha) {
        Image img = original.getImage();
        int w = img.getWidth(null);
        int h = img.getHeight(null);

        BufferedImage darker = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = darker.createGraphics();

        g2.drawImage(img, 0, 0, null);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1f - alpha));
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, w, h);

        g2.dispose();
        return new ImageIcon(darker);
    }

    // 기본 UI 구성용 패널 생성
    private JPanel titledPanel(String title) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        p.setBackground(new Color(0, 80, 0));
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), title);
        tb.setTitleColor(Color.WHITE);
        p.setBorder(tb);
        return p;
    }

    //현재 턴이 나인지 확인
    private boolean isMyTurn() {
        return myRole.equalsIgnoreCase(turnRole);
    }

    //내 역할 설정
    public void setMyRole(String role) {
        this.myRole = role;
        roleLabel.setText("내 역할: " + role);
    }

    //턴 정보 설정
    public void setTurn(String role) {
        this.turnRole = role;
        turnLabel.setText("턴: " + role);

        if (isMyTurn()) enableButtons();
        else disableButtons();
    }

    //채팅 및 시스템 메시지 출력
    public void appendMessage(String msg) {
        chatArea.append(msg + "\n");
        chatArea.setCaretPosition(chatArea.getText().length());

        //메시지는 중앙에도 출력
        if (msg.startsWith("[RESULT]")) {
            showCenterMessage(msg.replace("[RESULT]", "").trim());
        }
    }

    //카드 이미지 로드
    private ImageIcon loadCardIcon(String suit, String rank) {
        String path = System.getProperty("user.dir") +
                "\\src\\main\\resources\\" + suit + "\\" + rank + ".png";
        Image img = new ImageIcon(path).getImage().getScaledInstance(90, 140, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    //테이블 초기화 (새 라운드 시작 시)
    public void resetTable() {
        dealerPanel.removeAll();
        p1Panel.removeAll();
        p2Panel.removeAll();

        dealerPanel.revalidate();
        p1Panel.revalidate();
        p2Panel.revalidate();
        dealerPanel.repaint();
        p1Panel.repaint();
        p2Panel.repaint();

        dealerHiddenCardLabel = null;

        statusLabel.setText("배팅 대기중");
        turnLabel.setText("턴: -");
        turnRole = "";

        appendMessage("============== [새 라운드] ==============");
    }

    //서버로부터 받은 카드 정보를 화면에 표시
    public void applyCardMessage(String line) {
        try {
            String[] a = line.split(":");
            if (a.length < 4) return;

            String role = a[2];
            String[] cards = a[3].split(",");

            JPanel target = switch (role) {
                case "DEALER" -> dealerPanel;
                case "PLAYER1" -> p1Panel;
                default -> p2Panel;
            };

            target.removeAll();

            for (String c : cards) {
                String[] sr = c.split("-");
                target.add(new JLabel(loadCardIcon(sr[0], sr[1])));
            }

            target.revalidate();
            target.repaint();

        } catch (Exception e) {
            appendMessage("[오류] 카드 표시 실패: " + e.getMessage());
        }
    }

    //칩 정보 갱신 (내 칩만 표시)
    public void updateChips(String role, int amount) {
        if (role.equals(myRole)) chipLabel.setText("칩: " + amount);
    }

    //배팅 메시지 서버 전송
    private void sendBet(String value) {
        sender.send("BET:" + value);
    }

    //배팅 버튼 활성화
    public void enableBetting() {
        betAll.setEnabled(true);
        bet50.setEnabled(true);
        bet10.setEnabled(true);
        bet5.setEnabled(true);
        bet1.setEnabled(true);
        betDone.setEnabled(true);
    }

    //Hit / Stand 버튼 활성화
    private void enableButtons() {
        hitButton.setEnabled(true);
        standButton.setEnabled(true);
    }

    //Hit / Stand 버튼 비활성화
    private void disableButtons() {
        hitButton.setEnabled(false);
        standButton.setEnabled(false);
    }
}
