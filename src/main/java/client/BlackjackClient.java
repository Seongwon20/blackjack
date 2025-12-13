package client;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

/**
 * BlackjackClient
 * - 서버와 TCP 소켓으로 연결되는 클라이언트 네트워크 클래스
 * - GUI ↔ 서버 사이의 메시지 중계 역할
 */
public class BlackjackClient {

    //서버와의 소켓 통신 객체
    private Socket socket;

    //서버로부터 문자열 메시지를 읽기 위한 입력 스트림
    private BufferedReader in;

    // 서버로 문자열 메시지를 보내기 위한 출력 스트림
    private PrintWriter out;

    // 게임 화면(GUI)
    private GameGUI gui;

    /**
     * 클라이언트 생성자
     * @param host 서버 주소
     * @param port 서버 포트
     * @param role PLAYER1 / PLAYER2 역할
     */
    public BlackjackClient(String host, int port, String role) {
        try {
            //서버와 소켓 연결
            socket = new Socket(host, port);

            //수업 시간에 사용한 한글 깨짐 방지 코드
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            //GUI 생성 → GUI에서 발생한 이벤트는 서버로 전송
            gui = new GameGUI(line -> out.println(line));
            gui.setMyRole(role);

            //서버에 내 역할(PLAYER1 / PLAYER2) 전송
            out.println("MODE:" + role);

            //서버 메시지를 지속적으로 수신하는 스레드 시작
            new Thread(this::listen).start();

        } catch (IOException e) {
            //서버 연결 실패 시 사용자에게 알림
            JOptionPane.showMessageDialog(null, "서버 연결 실패!\n" + e.getMessage());
        }
    }

    /*서버로부터 메시지를 지속적으로 수신하는 메서드*/
    private void listen() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {

                //채팅 메시지 처리
                if (msg.startsWith("CHAT:")) {
                    gui.appendMessage(msg.substring(5));
                }
                //게임 리셋 신호 처리
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
                //PLAYER1 칩 갱신
                else if (msg.startsWith("CHIPS:P1:")) {
                    gui.updateChips("PLAYER1", Integer.parseInt(msg.substring(9)));
                }
                //PLAYER2 칩 갱신
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
                //새 라운드 안내
                else if (msg.startsWith("INFO:새 라운드")) {
                    gui.appendMessage(msg.substring(5));
                }
                //기타 메시지
                else {
                    gui.appendMessage(msg);
                }
            }
        } catch (IOException ignored) {}
    }

    /*프로그램 시작 지점, Swing 스레드에서 시작 화면 실행*/
    public static void main(String[] args) {
        SwingUtilities.invokeLater(StartScreen::new);
    }
}
