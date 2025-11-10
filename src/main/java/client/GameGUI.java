package client;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;

public class GameGUI extends JFrame {

    public interface Sender { void send(String line); }
    private final Sender sender;

    private JButton hitButton, standButton;
    private JLabel waitLabel, turnLabel, roleLabel;
    private JPanel dealerPanel, p1Panel, p2Panel;
    private JTextArea chatArea;
    private JTextField chatInput;

    private String myRole = "UNKNOWN";
    private String turnRole = "UNKNOWN";

    public GameGUI(Sender sender) {
        this.sender = sender;
        setTitle("블랙잭 온라인 (2인)");
        setSize(1000, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 상단
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        hitButton = new JButton("Hit");
        standButton = new JButton("Stand");
        roleLabel = new JLabel("내 역할: -");
        turnLabel = new JLabel("턴: -");
        waitLabel = new JLabel("대기 중…");
        top.add(hitButton); top.add(standButton);
        top.add(new JLabel(" | ")); top.add(roleLabel);
        top.add(new JLabel(" | ")); top.add(turnLabel);
        top.add(new JLabel(" | ")); top.add(waitLabel);
        add(top, BorderLayout.NORTH);

        // 카드 테이블
        dealerPanel = makePanel("DEALER");
        p1Panel = makePanel("PLAYER1");
        p2Panel = makePanel("PLAYER2");

        JPanel table = new JPanel(new GridLayout(3, 1));
        table.add(dealerPanel); table.add(p1Panel); table.add(p2Panel);
        add(table, BorderLayout.CENTER);

        // 채팅
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

        // 버튼 이벤트
        hitButton.addActionListener(e -> {
            if (isMyTurn()) sender.send("GAME:HIT");
            else appendMessage("내 턴이 아닙니다.");
        });
        standButton.addActionListener(e -> {
            if (isMyTurn()) sender.send("GAME:STAND");
            else appendMessage("내 턴이 아닙니다.");
        });

        disablePlay();
        setVisible(true);
    }

    private JPanel makePanel(String title) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        p.setBackground(new Color(0, 80, 0));
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), title);
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
        if (isMyTurn()) enablePlay(); else disablePlay();
    }

    public void setWaiting(boolean waiting) {
        waitLabel.setText(waiting ? "플레이어 기다리는 중…" : "게임 진행 중");
        waitLabel.setForeground(waiting ? Color.GRAY : new Color(0, 200, 0));
    }

    public void appendMessage(String msg) {
        chatArea.append(msg + "\n");
        chatArea.setCaretPosition(chatArea.getText().length());
    }

    public void disablePlay() { hitButton.setEnabled(false); standButton.setEnabled(false); }
    public void enablePlay() { hitButton.setEnabled(true); standButton.setEnabled(true); }

    /** 서버로부터 오는 카드 메시지 적용 */
    public void applyCardMessage(String line) {
        try {
            if (line.equals("GAME:CARD:HIDDEN")) {
                // 딜러의 뒷면 카드
                String base = System.getProperty("user.dir") + File.separator +
                        "src" + File.separator + "main" + File.separator + "resources";
                String backPath = base + File.separator + "back.png";
                Image img = new ImageIcon(backPath).getImage().getScaledInstance(90, 140, Image.SCALE_SMOOTH);
                dealerPanel.add(new JLabel(new ImageIcon(img)));
                dealerPanel.revalidate(); dealerPanel.repaint();
                return;
            }

            String[] a = line.split(":");
            if (a.length < 4) return;
            String role = a[2];
            String[] cards = a[3].split(",");

            JPanel target;
            if (role.equalsIgnoreCase("DEALER")) target = dealerPanel;
            else if (role.equalsIgnoreCase("PLAYER1")) target = p1Panel;
            else target = p2Panel;

            target.removeAll();
            for (String s : cards) {
                String[] sr = s.split("-");
                if (sr.length != 2) continue;
                String suit = sr[0], rank = sr[1];
                String base = System.getProperty("user.dir") + File.separator +
                        "src" + File.separator + "main" + File.separator + "resources";
                String path = base + File.separator + suit + File.separator + rank + ".png";
                Image img = new ImageIcon(path).getImage().getScaledInstance(90, 140, Image.SCALE_SMOOTH);
                target.add(new JLabel(new ImageIcon(img)));
            }
            target.revalidate();
            target.repaint();
        } catch (Exception e) {
            appendMessage("[오류] 카드 표시 실패: " + e.getMessage());
        }
    }
}
