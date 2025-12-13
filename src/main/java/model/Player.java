package model;

/*
 * Player
 * =================================================
 * [역할]
 * - 블랙잭 게임에 참여하는 플레이어의 상태를 관리하는 클래스
 *
 * [관리 대상]
 * - 플레이어 이름
 * - 손패(Hand)
 * - 보유 칩 수
 * - 현재 라운드의 베팅 금액
 */
public class Player {

    //플레이어 식별용 이름
    private final String name;

    //플레이어가 현재 보유한 카드 묶음
    private final Hand hand = new Hand();

    //플레이어가 보유한 총 칩 수
    private int chips = 100;     //기본 칩

    //현재 라운드에서 베팅한 칩 수
    private int betAmount = 0;   //라운드 베팅 칩

    /*
     * Player 생성자
     * ---------------------------------------------
     * name : 플레이어 이름(식별자)
     */
    public Player(String name) {
        this.name = name;
    }

    //플레이어 이름 반환
    public String getName() { return name; }

    //플레이어의 손패 반환
    public Hand getHand() { return hand; }

    //현재 보유 칩 수 반환
    public int getChips() { return chips; }

    //현재 라운드 베팅 금액 반환
    public int getBetAmount() { return betAmount; }

    //베팅 금액 설정
    public void setBetAmount(int betAmount) {
        this.betAmount = betAmount;
    }

    //승리 시 칩 증가
    public void winChips(int amount) {
        this.chips += amount;
    }

    //패배 시 칩 감소
    public void loseChips(int amount) {
        this.chips -= amount;
        if (this.chips < 0) this.chips = 0; // 칩이 음수가 되면 0으로
    }

    //라운드 종료 후 베팅 금액 초기화
    public void resetBet() {
        this.betAmount = 0;
    }
}
