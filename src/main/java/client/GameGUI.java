package client;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.net.URL;

public class GameGUI extends JFrame {

    public interface Sender { void send(String line); }

    private final Sender sender;

    private JButton hitButton, standButton;
    private JLabel statusLabel, turnLabel, roleLabel, chipLabel;

    private JPanel dealerPanel, p1Panel, p2Panel;
    private JPanel betPanel;

    private JTextArea chatArea;
    private JTextField chatInput;

    private JButton betAll, bet50, bet10, bet5, bet1;

    private String myRole = "UNKNOWN";
    private String turnRole = "UNKNOWN";

    // 카드 뒷면
    private ImageIcon backIcon;
    private JLabel dealerHiddenCardLabel = null;


    public GameGUI(Sender sender) {
        this.sender = sender;

        setTitle("블랙잭 온라인 (2인)");
        setSize(1000, 760);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ==================== 카드 뒷면 이미지 로딩 ====================
        URL backURL = getClass().getResource("/back.png");
        if (backURL != null) {
            backIcon = new ImageIcon(
                    new ImageIcon(backURL).getImage()
                            .getScaledInstance(90, 140, Image.SCALE_SMOOTH)
            );
        } else {
            System.out.println("[ERROR] back.png 를 찾을 수 없습니다!");
        }

        // ==================== 상단 UI ====================
        JPanel control = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));

        hitButton = new JButton("Hit");
        standButton = new JButton("Stand");
        statusLabel = new JLabel("대기중");
        turnLabel = new JLabel("턴: -");
        roleLabel = new JLabel("내 역할: -");
        chipLabel = new JLabel("칩: 100");

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

        // ==================== 카드 테이블 ====================
        JPanel table = new JPanel(new GridLayout(3, 1, 8, 8));
        dealerPanel = titledPanel("DEALER");
        p1Panel = titledPanel("PLAYER1");
        p2Panel = titledPanel("PLAYER2");

        table.add(dealerPanel);
        table.add(p1Panel);
        table.add(p2Panel);

        add(table, BorderLayout.CENTER);

        // ==================== 하단 전체 ====================
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // ==================== 배팅 패널 ====================
        betPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        betPanel.setBorder(BorderFactory.createTitledBorder("배팅"));

        betAll = new JButton("올인");
        bet50 = new JButton("50");
        bet10 = new JButton("10");
        bet5  = new JButton("5");
        bet1  = new JButton("1");

        betAll.addActionListener(e -> sendBet("ALL"));
        bet50.addActionListener(e -> sendBet("50"));
        bet10.addActionListener(e -> sendBet("10"));
        bet5.addActionListener(e -> sendBet("5"));
        bet1.addActionListener(e -> sendBet("1"));

        betPanel.add(betAll);
        betPanel.add(bet50);
        betPanel.add(bet10);
        betPanel.add(bet5);
        betPanel.add(bet1);

        bottomPanel.add(betPanel, BorderLayout.NORTH);

        // ==================== 채팅 ====================
        chatArea = new JTextArea(8, 60);
        chatArea.setEditable(false);

        chatInput = new JTextField();
        chatInput.addActionListener(e -> {
            String msg = chatInput.getText().trim();
            if (!msg.isEmpty()) {
                sender.send("CHAT:" + msg);
                appendMessage("[나] " + msg);
                chatInput.setText("");
            }
        });

        JPanel chatBox = new JPanel(new BorderLayout());
        chatBox.setBorder(BorderFactory.createTitledBorder("채팅"));
        chatBox.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatBox.add(chatInput, BorderLayout.SOUTH);

        bottomPanel.add(chatBox, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        // ==================== 버튼 이벤트 ====================
        hitButton.addActionListener(e -> {
            if (isMyTurn()) sender.send("GAME:HIT");
            else appendMessage("내 턴이 아닙니다.");
        });

        standButton.addActionListener(e -> {
            if (isMyTurn()) sender.send("GAME:STAND");
            else appendMessage("내 턴이 아닙니다.");
        });

        disableButtons();
        disableBetting(); // 초기에는 배팅 금지

        setVisible(true);
    }

    // ==================== 패널 생성 ====================
    private JPanel titledPanel(String title) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        p.setBackground(new Color(0, 80, 0));
        TitledBorder tb = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), title);
        tb.setTitleColor(Color.WHITE);
        p.setBorder(tb);
        return p;
    }

    private boolean isMyTurn() {
        return myRole.equalsIgnoreCase(turnRole);
    }

    public void setMyRole(String role) {
        this.myRole = role;
        roleLabel.setText("내 역할: " + role);
    }

    public void setTurn(String role) {
        this.turnRole = role;
        turnLabel.setText("턴: " + role);

        if (isMyTurn()) enableButtons();
        else disableButtons();
    }

    public void appendMessage(String msg) {
        chatArea.append(msg + "\n");
        chatArea.setCaretPosition(chatArea.getText().length());
    }

    // ==================== 카드 이미지 로딩 ====================
    private ImageIcon loadCardIcon(String suit, String rank) {
        String path = "/" + suit + "/" + rank + ".png";
        URL url = getClass().getResource(path);

        if (url == null) {
            appendMessage("[이미지 없음] " + path);
            return null;
        }

        Image img = new ImageIcon(url).getImage()
                .getScaledInstance(90, 140, Image.SCALE_SMOOTH);

        return new ImageIcon(img);
    }

    // ==================== 카드 표시 ====================
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

            // 딜러 카드 처리
            if (role.equals("DEALER")) {

                dealerHiddenCardLabel = null;

                for (int i = 0; i < cards.length; i++) {
                    String[] parts = cards[i].split("-");
                    String suit = parts[0];
                    String rank = parts[1];

                    // 두 번째 카드 뒷면
                    if (i == 1 && cards.length == 2) {
                        dealerHiddenCardLabel = new JLabel(backIcon);
                        target.add(dealerHiddenCardLabel);
                    } else {
                        target.add(new JLabel(loadCardIcon(suit, rank)));
                    }
                }

                // 전체 공개된 경우
                if (cards.length >= 2 && dealerHiddenCardLabel != null) {
                    String[] sr = cards[1].split("-");
                    dealerHiddenCardLabel.setIcon(loadCardIcon(sr[0], sr[1]));
                }

                target.revalidate();
                target.repaint();
                return;
            }

            // 플레이어 카드
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

    // ==================== 칩 UI 업데이트 ====================
    public void updateChips(String role, int amount) {
        if (role.equals(myRole)) {
            chipLabel.setText("칩: " + amount);
        }
    }

    // ==================== 배팅 전송 ====================
    private void sendBet(String value) {
        sender.send("BET:" + value);
        disableBetting();
    }

    // ==================== 배팅 활성화/비활성화 ====================
    public void enableBetting() {
        betAll.setEnabled(true);
        bet50.setEnabled(true);
        bet10.setEnabled(true);
        bet5.setEnabled(true);
        bet1.setEnabled(true);
        betPanel.setVisible(true);
    }

    public void disableBetting() {
        betAll.setEnabled(false);
        bet50.setEnabled(false);
        bet10.setEnabled(false);
        bet5.setEnabled(false);
        bet1.setEnabled(false);
    }

    private void enableButtons() {
        hitButton.setEnabled(true);
        standButton.setEnabled(true);
    }

    private void disableButtons() {
        hitButton.setEnabled(false);
        standButton.setEnabled(false);
    }
}
