package main.java.model;

public class Player {
    private final String name;
    private final Hand hand;
    private int chips;

    public Player(String name, int chips) {
        this.name = name;
        this.chips = chips;
        this.hand = new Hand();
    }

    public String getName() { return name; }
    public Hand getHand() { return hand; }
    public int getChips() { return chips; }

    public void win(int amount) { chips += amount; }
    public void lose(int amount) { chips -= amount; }
}
