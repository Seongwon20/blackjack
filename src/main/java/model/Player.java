package model;

public class Player {

    private final String name;
    private final Hand hand = new Hand();

    private int chips = 100;     // 기본 칩
    private int betAmount = 0;   // 라운드 베팅 칩

    public Player(String name) {
        this.name = name;
    }

    // -------------------- Getter --------------------
    public String getName() { return name; }
    public Hand getHand() { return hand; }

    public int getChips() { return chips; }
    public int getBetAmount() { return betAmount; }

    // -------------------- Setter --------------------
    public void setBetAmount(int betAmount) {
        this.betAmount = betAmount;
    }

    // -------------------- 칩 증가/감소 --------------------
    public void winChips(int amount) {
        this.chips += amount;
    }

    public void loseChips(int amount) {
        this.chips -= amount;
        if (this.chips < 0) this.chips = 0; // 칩이 음수가 되면 0으로
    }

    // -------------------- 베팅 초기화 --------------------
    public void resetBet() {
        this.betAmount = 0;
    }
}