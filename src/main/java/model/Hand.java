package model;

import java.util.ArrayList;
import java.util.List;

public class Hand {
    private final List<Card> cards = new ArrayList<>();

    public void addCard(Card card) { cards.add(card); }
    public List<Card> getCards() { return cards; }

    public int getValue() {
        int value = 0, ace = 0;
        for (Card c : cards) {
            value += c.getValue();
            if (c.getRank().equals("A")) ace++;
        }
        while (value > 21 && ace > 0) { value -= 10; ace--; }
        return value;
    }
}
