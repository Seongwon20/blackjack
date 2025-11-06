package main.java.model;

import javax.swing.*;
import java.awt.*;

public class Card {
    private final String suit;   // 문양(스페이드, 다이아, 클로버, 하트)
    private final String rank;   // A,2,...,K
    private final int value;
    private final Image image;

    public Card(String suit, String rank, int value, String imagePath) {
        this.suit = suit;
        this.rank = rank;
        this.value = value;
        this.image = new ImageIcon(getClass().getResource(imagePath))
                .getImage().getScaledInstance(90, 140, Image.SCALE_SMOOTH);
    }

    public String getSuit() { return suit; }
    public String getRank() { return rank; }
    public int getValue() { return value; }
    public Image getImage() { return image; }
}
