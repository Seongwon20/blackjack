package client;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class BlackjackClient {

    //서버와의 네트워크 연결을 담당하는 소켓
    private Socket socket;

    //서버로부터 문자열 메시지를 읽기 위한 입력 스트림
    private BufferedReader in;

    //서버로 문자열 메시지를 전송하기 위한 출력 스트림
    private PrintWriter out;

    //게임 화면 GUI 객체
    private GameGUI gui;

    /*
     host : 서버 주소
     port : 서버 포트 번호
     role : PLAYER1 또는 PLAYER2*/
    public BlackjackClient(String host, int port, String role) {
        try {
            //서버와 TCP 소켓 연결
            socket = new Socket(host, port);

            //수업 시간에 적용시켜봤던 한글 깨짐 방지 코드
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            //GameGUI 생성
            //GUI에서 발생한 이벤트는 Sender 인터페이스를 통해 서버로 전달됨
            gui = new GameGUI(line -> out.println(line));
            gui.setMyRole(role);

            //서버에 자신의 역할 정보 전송
            out.println("MODE:" + role);

            //서버 메시지를 계속 수신하는 스레드 시작
            new Thread(this::listen).start();

        } catch (IOException e) {
            //서버 연결 실패 시 출력 메시지
            JOptionPane.showMessageDialog(null, "서버 연결 실패!\n" + e.getMessage());
        }
    }

    //서버로부터 메시지를 지속적으로 수신
    private void listen() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                //채팅 메시지 수신
                if (msg.startsWith("CHAT:")) {
                    gui.appendMessage(msg.substring(5));
                }
                //게임 초기화(새 라운드 시작)
                else if (msg.equals("GAME:RESET")) {
                    gui.resetTable();
                }
                //카드 정보 수신
                else if (msg.startsWith("GAME:CARD:")) {
                    gui.applyCardMessage(msg);
                }
                //현재 턴 정보 수신
                else if (msg.startsWith("GAME:TURN:")) {
                    gui.setTurn(msg.substring(10));
                }
                //PLAYER1 칩 정보 갱신
                else if (msg.startsWith("CHIPS:P1:")) {
                    gui.updateChips("PLAYER1", Integer.parseInt(msg.substring(9)));
                }
                //PLAYER2 칩 정보 갱신
                else if (msg.startsWith("CHIPS:P2:")) {
                    gui.updateChips("PLAYER2", Integer.parseInt(msg.substring(9)));
                }
                //배팅 가능 상태 알림
                else if (msg.equals("INFO:BETTING")) {
                    gui.enableBetting();
                }
                //대기 상태 메시지
                else if (msg.startsWith("WAITING:")) {
                    gui.appendMessage(msg.substring(8));
                }
                //새 라운드 안내 메시지
                else if (msg.startsWith("INFO:새 라운드")) {
                    gui.appendMessage(msg.substring(5));
                }
                //그 외 메시지
                else {
                    gui.appendMessage(msg);
                }
            }
        } catch (IOException ignored) {}
    }

    //프로그램 시작 지점
    public static void main(String[] args) {
        SwingUtilities.invokeLater(StartScreen::new);
    }
}
