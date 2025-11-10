package client;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;

public class GameGUI extends JFrame {

    public interface Sender { void send(String line); }

    private final Sender sender;
    private JButton hitButton, standButton;
    private JLabel statusLabel, turnLabel, roleLabel;
    private JPanel dealerPanel, p1Panel, p2Panel;
    private JTextArea chatArea;
    private JTextField chatInput;
    private String myRole = "UNKNOWN", turnRole = "UNKNOWN";

    public GameGUI(Sender sender) {
        this.sender = sender;
        setTitle("블랙잭 온라인 (2인)");
        setSize(1000, 760);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== 상단 =====
        JPanel control = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        hitButton = new JButton("Hit");
        standButton = new JButton("Stand");
        statusLabel = new JLabel("대기중");
        turnLabel = new JLabel("턴: -");
        roleLabel = new JLabel("내 역할: -");
        control.add(hitButton); control.add(standButton);
        control.add(new JLabel(" | ")); control.add(roleLabel);
        control.add(new JLabel(" | ")); control.add(turnLabel);
        control.add(new JLabel(" | ")); control.add(statusLabel);
        add(control, BorderLayout.NORTH);

        // ===== 카드 테이블 =====
        JPanel table = new JPanel(new GridLayout(3, 1, 8, 8));
        dealerPanel = titledPanel("DEALER");
        p1Panel = titledPanel("PLAYER1");
        p2Panel = titledPanel("PLAYER2");
        table.add(dealerPanel); table.add(p1Panel); table.add(p2Panel);
        add(table, BorderLayout.CENTER);

        // ===== 채팅 =====
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
        add(chatBox, BorderLayout.SOUTH);

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

    private JPanel titledPanel(String title) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        p.setBackground(new Color(0, 80, 0));
        TitledBorder tb = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), title);
        tb.setTitleColor(Color.WHITE);
        p.setBorder(tb);
        return p;
    }

    private boolean isMyTurn() { return myRole.equalsIgnoreCase(turnRole); }

    public void setMyRole(String role) {
        this.myRole = role;
        roleLabel.setText("내 역할: " + role);
    }

    public void setTurn(String role) {
        this.turnRole = role;
        turnLabel.setText("턴: " + role);
        if (isMyTurn()) enableButtons(); else disableButtons();
    }

    public void appendMessage(String msg) {
        chatArea.append(msg + "\n");
        chatArea.setCaretPosition(chatArea.getText().length());
    }

    public void applyCardMessage(String line) {
        try {
            String[] a = line.split(":");
            if (a.length < 4) return;
            String role = a[2].trim();
            String[] cards = a[3].split(",");
            JPanel target = switch (role) {
                case "DEALER" -> dealerPanel;
                case "PLAYER1" -> p1Panel;
                default -> p2Panel;
            };
            target.removeAll();
            for (String c : cards) {
                String[] sr = c.split("-");
                String suit = sr[0], rank = sr[1];
                String path = System.getProperty("user.dir") + "\\src\\main\\resources\\" + suit + "\\" + rank + ".png";
                Image img = new ImageIcon(path).getImage().getScaledInstance(90, 140, Image.SCALE_SMOOTH);
                target.add(new JLabel(new ImageIcon(img)));
            }
            target.revalidate();
            target.repaint();
        } catch (Exception e) {
            appendMessage("[오류] 카드 표시 실패: " + e.getMessage());
        }
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
