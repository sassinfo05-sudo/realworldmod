package com.realworldmod.commerce;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.realworldmod.commerce.Card.Rank.ACE;
import static com.realworldmod.commerce.Card.Rank.EIGHT;
import static com.realworldmod.commerce.Card.Rank.FIVE;
import static com.realworldmod.commerce.Card.Rank.FOUR;
import static com.realworldmod.commerce.Card.Rank.JACK;
import static com.realworldmod.commerce.Card.Rank.KING;
import static com.realworldmod.commerce.Card.Rank.NINE;
import static com.realworldmod.commerce.Card.Rank.QUEEN;
import static com.realworldmod.commerce.Card.Rank.SEVEN;
import static com.realworldmod.commerce.Card.Rank.SIX;
import static com.realworldmod.commerce.Card.Rank.THREE;
import static com.realworldmod.commerce.Card.Rank.TWO;
import static com.realworldmod.commerce.Card.Suit.CLUBS;
import static com.realworldmod.commerce.Card.Suit.DIAMONDS;
import static com.realworldmod.commerce.Card.Suit.HEARTS;
import static com.realworldmod.commerce.Card.Suit.SPADES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreeCardPokerHandEvaluatorTest {
    @Test
    void detectsHighCard() {
        ThreeCardPokerHand hand = ThreeCardPokerHandEvaluator.evaluate(List.of(
                new Card(KING, HEARTS), new Card(SEVEN, CLUBS), new Card(TWO, SPADES)));
        assertEquals(ThreeCardPokerHandRank.HIGH_CARD, hand.rank());
        assertEquals(List.of(13, 7, 2), hand.tiebreakValuesDescending());
    }

    @Test
    void detectsPairWithKicker() {
        ThreeCardPokerHand hand = ThreeCardPokerHandEvaluator.evaluate(List.of(
                new Card(SEVEN, HEARTS), new Card(SEVEN, CLUBS), new Card(KING, SPADES)));
        assertEquals(ThreeCardPokerHandRank.PAIR, hand.rank());
        assertEquals(List.of(7, 13), hand.tiebreakValuesDescending());
    }

    @Test
    void detectsFlush() {
        ThreeCardPokerHand hand = ThreeCardPokerHandEvaluator.evaluate(List.of(
                new Card(KING, HEARTS), new Card(SEVEN, HEARTS), new Card(TWO, HEARTS)));
        assertEquals(ThreeCardPokerHandRank.FLUSH, hand.rank());
    }

    @Test
    void detectsStraight() {
        ThreeCardPokerHand hand = ThreeCardPokerHandEvaluator.evaluate(List.of(
                new Card(NINE, HEARTS), new Card(EIGHT, CLUBS), new Card(SEVEN, SPADES)));
        assertEquals(ThreeCardPokerHandRank.STRAIGHT, hand.rank());
        assertEquals(List.of(9), hand.tiebreakValuesDescending());
    }

    @Test
    void aceTwoThreeIsTheLowestStraightNotHighCard() {
        ThreeCardPokerHand hand = ThreeCardPokerHandEvaluator.evaluate(List.of(
                new Card(ACE, HEARTS), new Card(TWO, CLUBS), new Card(THREE, SPADES)));
        assertEquals(ThreeCardPokerHandRank.STRAIGHT, hand.rank());
        assertEquals(List.of(3), hand.tiebreakValuesDescending());
    }

    @Test
    void queenKingAceIsNotAStraightAroundTheCorner() {
        ThreeCardPokerHand hand = ThreeCardPokerHandEvaluator.evaluate(List.of(
                new Card(QUEEN, HEARTS), new Card(KING, CLUBS), new Card(ACE, SPADES)));
        assertEquals(ThreeCardPokerHandRank.STRAIGHT, hand.rank());
        assertEquals(List.of(14), hand.tiebreakValuesDescending());
    }

    @Test
    void detectsThreeOfAKind() {
        ThreeCardPokerHand hand = ThreeCardPokerHandEvaluator.evaluate(List.of(
                new Card(FOUR, HEARTS), new Card(FOUR, CLUBS), new Card(FOUR, SPADES)));
        assertEquals(ThreeCardPokerHandRank.THREE_OF_A_KIND, hand.rank());
        assertEquals(List.of(4), hand.tiebreakValuesDescending());
    }

    @Test
    void detectsStraightFlush() {
        ThreeCardPokerHand hand = ThreeCardPokerHandEvaluator.evaluate(List.of(
                new Card(NINE, HEARTS), new Card(EIGHT, HEARTS), new Card(SEVEN, HEARTS)));
        assertEquals(ThreeCardPokerHandRank.STRAIGHT_FLUSH, hand.rank());
    }

    @Test
    void straightOutranksFlushUnlikeFiveCardPoker() {
        ThreeCardPokerHand flush = ThreeCardPokerHandEvaluator.evaluate(List.of(
                new Card(TWO, HEARTS), new Card(SEVEN, HEARTS), new Card(JACK, HEARTS)));
        ThreeCardPokerHand straight = ThreeCardPokerHandEvaluator.evaluate(List.of(
                new Card(NINE, CLUBS), new Card(EIGHT, DIAMONDS), new Card(SEVEN, SPADES)));
        assertTrue(straight.compareTo(flush) > 0);
    }

    @Test
    void higherPairBeatsLowerPair() {
        ThreeCardPokerHand pairOfKings = ThreeCardPokerHandEvaluator.evaluate(List.of(
                new Card(KING, HEARTS), new Card(KING, CLUBS), new Card(TWO, SPADES)));
        ThreeCardPokerHand pairOfSevens = ThreeCardPokerHandEvaluator.evaluate(List.of(
                new Card(SEVEN, HEARTS), new Card(SEVEN, CLUBS), new Card(NINE, SPADES)));
        assertTrue(pairOfKings.compareTo(pairOfSevens) > 0);
    }

    @Test
    void samePairComparesByKicker() {
        ThreeCardPokerHand higherKicker = ThreeCardPokerHandEvaluator.evaluate(List.of(
                new Card(SEVEN, HEARTS), new Card(SEVEN, CLUBS), new Card(KING, SPADES)));
        ThreeCardPokerHand lowerKicker = ThreeCardPokerHandEvaluator.evaluate(List.of(
                new Card(SEVEN, DIAMONDS), new Card(SEVEN, SPADES), new Card(FIVE, HEARTS)));
        assertTrue(higherKicker.compareTo(lowerKicker) > 0);
    }

    @Test
    void identicalHighCardHandsAreEqual() {
        ThreeCardPokerHand first = ThreeCardPokerHandEvaluator.evaluate(List.of(
                new Card(KING, HEARTS), new Card(SEVEN, CLUBS), new Card(TWO, SPADES)));
        ThreeCardPokerHand second = ThreeCardPokerHandEvaluator.evaluate(List.of(
                new Card(KING, DIAMONDS), new Card(SEVEN, SPADES), new Card(TWO, HEARTS)));
        assertEquals(0, first.compareTo(second));
    }

    @Test
    void sixSevenEightIsAStraightWithHighCardEight() {
        ThreeCardPokerHand hand = ThreeCardPokerHandEvaluator.evaluate(List.of(
                new Card(SIX, HEARTS), new Card(SEVEN, CLUBS), new Card(EIGHT, SPADES)));
        assertEquals(ThreeCardPokerHandRank.STRAIGHT, hand.rank());
    }
}
