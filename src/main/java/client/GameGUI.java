package client;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class GameGUI extends JFrame {

    public interface Sender { void send(String line); }

    private final Sender sender;

    private JButton hitButton, standButton;
    private JButton betAll, bet50, bet10, bet5, bet1, betDone;

    private JLabel statusLabel, turnLabel, roleLabel, chipLabel;

    private JPanel dealerPanel, p1Panel, p2Panel;
    private JPanel betPanel;

    private JTextArea chatArea;
    private JTextField chatInput;

    private String myRole = "UNKNOWN";
    private String turnRole = "UNKNOWN";

    private ImageIcon backIcon;
    private JLabel dealerHiddenCardLabel = null;

    // 화면 중앙 메시지
    private JLabel centerMessage;

    public GameGUI(Sender sender) {
        this.sender = sender;

        setTitle("블랙잭 온라인 (2인)");
        setSize(1000, 760);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // =============================
        // 중앙 메시지 Overlay
        // =============================
        centerMessage = new JLabel("", SwingConstants.CENTER);
        centerMessage.setFont(new Font("Tmoney RoundWind ExtraBold", Font.BOLD, 48));
        centerMessage.setForeground(Color.RED);
        centerMessage.setVisible(false);
        getLayeredPane().add(centerMessage, JLayeredPane.PALETTE_LAYER);

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

        // 카드 뒷면 이미지
        String backPath = System.getProperty("user.dir") + "\\src\\main\\resources\\back.png";
        backIcon = new ImageIcon(
                new ImageIcon(backPath).getImage().getScaledInstance(90, 140, Image.SCALE_SMOOTH)
        );

        // =============================
        // 상단 컨트롤바
        // =============================
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

        // =============================
        // 카드 테이블 공간
        // =============================
        JPanel table = new JPanel(new GridLayout(3, 1, 8, 8));
        dealerPanel = titledPanel("DEALER");
        p1Panel = titledPanel("PLAYER1");
        p2Panel = titledPanel("PLAYER2");

        table.add(dealerPanel);
        table.add(p1Panel);
        table.add(p2Panel);

        add(table, BorderLayout.CENTER);

        // =============================
        // 하단 배팅 + 채팅
        // =============================
        JPanel bottomPanel = new JPanel(new BorderLayout());

        betPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        betPanel.setBorder(BorderFactory.createTitledBorder("배팅"));

        betAll = createChipButton("chip_allin.png", "ALL");
        bet50 = createChipButton("chip50.png", "50");
        bet10 = createChipButton("chip10.png", "10");
        bet5  = createChipButton("chip5.png", "5");
        bet1  = createChipButton("chip1.png", "1");

        // ★ 추가: 배팅 완료 버튼
        betDone = new JButton("배팅 완료");
        betDone.setPreferredSize(new Dimension(120, 60));
        betDone.addActionListener(e -> sender.send("BET:DONE"));

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

        // 버튼 기능
        hitButton.addActionListener(e -> {
            if (isMyTurn()) sender.send("GAME:HIT");
            else appendMessage("내 턴이 아닙니다.");
        });

        standButton.addActionListener(e -> {
            if (isMyTurn()) sender.send("GAME:STAND");
            else appendMessage("내 턴이 아닙니다.");
        });

        disableButtons();
        setVisible(true);
    }

    // =========================================
    // 중앙 메시지 출력
    // =========================================
    public void showCenterMessage(String msg) {
        centerMessage.setText(msg);
        centerMessage.setVisible(true);

        Timer t = new Timer(5000, e -> centerMessage.setVisible(false));
        t.setRepeats(false);
        t.start();
    }

    // =========================================
    // 칩 버튼 생성 + 눌림 효과
    // =========================================
    private JButton createChipButton(String fileName, String value) {
        String path = System.getProperty("user.dir") +
                "\\src\\main\\resources\\chips\\" + fileName;

        ImageIcon icon = new ImageIcon(
                new ImageIcon(path).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH)
        );

        JButton btn = new JButton(icon);
        btn.setPreferredSize(new Dimension(70, 70));

        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);

        btn.getModel().addChangeListener(e -> {
            ButtonModel m = btn.getModel();
            if (m.isPressed()) {
                btn.setIcon(makeDarkerIcon(icon, 0.75f));
            } else {
                btn.setIcon(icon);
            }
        });

        btn.addActionListener(e -> sendBet(value));
        return btn;
    }

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

    // =========================================
    // 기본 UI
    // =========================================
    private JPanel titledPanel(String title) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        p.setBackground(new Color(0, 80, 0));
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), title);
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

        // [RESULT] 메시지는 중앙에도 표시
        if (msg.startsWith("[RESULT]")) {
            showCenterMessage(msg.replace("[RESULT]", "").trim());
        }
    }

    // 카드 이미지
    private ImageIcon loadCardIcon(String suit, String rank) {
        String path = System.getProperty("user.dir") +
                "\\src\\main\\resources\\" + suit + "\\" + rank + ".png";
        Image img = new ImageIcon(path).getImage().getScaledInstance(90, 140, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    // 테이블 초기화
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

    // 카드 표시
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

    // 칩 갱신
    public void updateChips(String role, int amount) {
        if (role.equals(myRole)) chipLabel.setText("칩: " + amount);
    }

    private void sendBet(String value) {
        sender.send("BET:" + value);
    }

    public void enableBetting() {
        betAll.setEnabled(true);
        bet50.setEnabled(true);
        bet10.setEnabled(true);
        bet5.setEnabled(true);
        bet1.setEnabled(true);
        betDone.setEnabled(true);
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
