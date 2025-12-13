package model;

import java.util.ArrayList;
import java.util.List;

/*
 * Hand
 * =================================================
 * [역할]
 * - 플레이어 또는 딜러가 현재 들고 있는 카드 묶음(Hand)을 표현
 *
 * [설계 의도]
 * - 카드 추가, 초기화, 점수 계산 로직을 한 클래스에 집중
 * - 블랙잭 규칙(Ace 처리)을 내부에서 책임지도록 설계
 */
public class Hand {

    //현재 손패에 포함된 카드 목록
    private final List<Card> cards = new ArrayList<>();

    /*
     * 카드 한 장을 손패에 추가
     */
    public void addCard(Card c) {
        cards.add(c);
    }

    /*
     * 현재 손패의 카드 목록 반환
     * (카드 이미지 출력 등 GUI에서 사용)
     */
    public List<Card> getCards() {
        return cards;
    }

    /*
     * 손패 초기화
     * 라운드 종료 후 다음 라운드를 위해 카드 제거
     */
    public void clear() {
        cards.clear();
    }

    /*
     * getValue()
     * ------------------------------------------------
     * 블랙잭 점수 계산 메서드
     *
     * [규칙]
     * - 숫자 카드는 해당 숫자
     * - J, Q, K는 10점
     * - A(Ace)는 기본 11점으로 계산하되
     *   총합이 21을 초과하면 1점으로 조정
     */
    public int getValue() {
        int value = 0; //현재 점수 합
        int ace = 0;   //Ace 카드 개수

        //모든 카드 점수 합산
        for (Card c : cards) {
            value += c.getValue();
            if (c.getRank().equals("A")) ace++;
        }

        //점수가 21을 초과할 경우 에이스를 1로 조정
        while (value > 21 && ace > 0) {
            value -= 10; // Ace 하나를 11 → 1로 변경
            ace--;
        }

        return value;
    }
}
