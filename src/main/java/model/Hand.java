package model;

import java.util.ArrayList;
import java.util.List;

public class Hand {
    private final List<Card> cards = new ArrayList<>();

    public void addCard(Card c) {
        cards.add(c);
    }

    public List<Card> getCards() {
        return cards;
    }

    public void clear() {
        cards.clear();
    }

    // 점수 계산 (ACE 처리 포함)
    public int getValue() {
        int value = 0;
        int ace = 0;

        for (Card c : cards) {
            value += c.getValue();
            if (c.getRank().equals("A")) ace++;
        }

        while (value > 21 && ace > 0) {
            value -= 10;
            ace--;
        }

        return value;
    }
}
