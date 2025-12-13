package model;

import javax.swing.*;
import java.awt.*;

/*
 * Card
 * =================================================
 * [역할]
 * - 블랙잭 게임에서 사용되는 카드 한 장을 표현하는 모델 클래스
 *
 * [설계 의도]
 * - 카드의 상태(무늬, 숫자, 점수)를 하나의 객체로 관리
 * - GUI, 네트워크, 게임 로직과 분리된 순수 데이터 객체(Model)
 */
public class Card {

    //카드 무늬 (spade, heart, diamond, club)
    private final String suit;

    //카드 숫자 또는 문자 (A, 2~10, J, Q, K)
    private final String rank;

    //블랙잭 점수 계산에 사용되는 값
    //(A=11, J/Q/K=10, 숫자카드는 해당 숫자)
    private final int value;

    /*
     * Card 생성자
     * ---------------------------------------------
     * suit  : 카드 무늬
     * rank  : 카드 숫자/문자
     * value : 블랙잭 계산용 점수
     */
    public Card(String suit, String rank, int value) {
        this.suit = suit;
        this.rank = rank;
        this.value = value;
    }

    //카드 무늬 반환
    public String getSuit() { return suit; }

    //카드 숫자/문자 반환
    public String getRank() { return rank; }

    //카드 점수 반환
    public int getValue() { return value; }
}
