package model;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Card {
    private final String suit;   // spade/heart/diamond/club
    private final String rank;   // A,2..10,J,Q,K
    private final int value;

    // 이미지는 클라이언트에서 경로로 로드하므로 여기선 보관만
    public Card(String suit, String rank, int value) {
        this.suit = suit;
        this.rank = rank;
        this.value = value;
    }

    public String getSuit() { return suit; }
    public String getRank() { return rank; }
    public int getValue() { return value; }
}
