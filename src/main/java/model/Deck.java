package model;

import java.util.*;

public class Deck {
    private final List<Card> cards = new ArrayList<>();

    public Deck() {
        String[] suits = {"spade", "heart", "diamond", "club"};
        String[] ranks = {"A","2","3","4","5","6","7","8","9","10","J","Q","K"};

        for (String suit : suits) {
            for (String r : ranks) {
                int value = switch (r) {
                    case "A" -> 11;
                    case "J", "Q", "K" -> 10;
                    default -> Integer.parseInt(r);
                };

                // 확장자 처리 (J/Q/K는 png, 나머지는 jpg)
                String ext = (r.equals("J") || r.equals("Q") || r.equals("K")) ? ".png" : ".jpg";

                // 경로 예: /main.resources/하트/A.png
                String path = "/" + suit + "/" + r + ext;


                cards.add(new Card(suit, r, value, path));
            }
        }
        Collections.shuffle(cards);
    }

    public Card draw() {
        return cards.remove(0);
    }
}
