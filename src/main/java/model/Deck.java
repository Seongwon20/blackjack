package model;

import java.util.*;

/*
 * Deck
 * =================================================
 * [역할]
 * - 블랙잭 게임에서 사용하는 카드 덱(52장)을 관리하는 클래스
 *
 * [설계 의도]
 * - 카드 생성, 초기화, 셔플 기능을 한 객체에 캡슐화
 * - 게임 진행 중 카드 뽑기(draw) 기능 제공
 */
public class Deck {

    //현재 덱에 남아있는 카드 목록
    private final List<Card> cards = new ArrayList<>();

    /*
     * Deck 생성자
     * ---------------------------------------------
     * 1) 4가지 무늬 × 13가지 카드 = 총 52장 생성
     * 2) 블랙잭 규칙에 맞는 카드 점수 설정
     * 3) 카드 순서를 무작위로 섞음
     */
    public Deck() {

        //카드 무늬 배열
        String[] suits = {"spade", "heart", "diamond", "club"};

        //카드 숫자/문자 배열
        String[] ranks = {"A","2","3","4","5","6","7","8","9","10","J","Q","K"};

        //모든 무늬와 숫자를 조합하여 카드 생성
        for (String suit : suits) {
            for (String r : ranks) {

                //블랙잭 규칙에 따른 카드 점수 결정
                int value = switch (r) {
                    case "A" -> 11;                 //에이스는 기본 11점
                    case "J", "Q", "K" -> 10;       //그림 카드는 10점
                    default -> Integer.parseInt(r); //숫자 카드는 해당 숫자
                };

                //카드 객체 생성 후 덱에 추가
                cards.add(new Card(suit, r, value));
            }
        }

        //게임 시작 시 카드 순서를 무작위로 섞음
        Collections.shuffle(cards);
    }

    /*
     * draw()
     * ---------------------------------------------
     * 덱의 가장 위에 있는 카드 한 장을 뽑아 반환
     * (뽑은 카드는 덱에서 제거됨)
     */
    public Card draw() {
        return cards.remove(0);
    }
}
