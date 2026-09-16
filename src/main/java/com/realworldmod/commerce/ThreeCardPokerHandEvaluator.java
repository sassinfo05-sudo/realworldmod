package com.realworldmod.commerce;

import java.util.Comparator;
import java.util.List;

/**
 * Pure three-card hand evaluation, kept separate from {@link ThreeCardPokerGame}
 * so the hand-strength rules are unit testable on their own. Recognizes the
 * "wheel" (Ace-2-3) as the lowest straight, the standard real-world Three
 * Card Poker rule.
 */
public final class ThreeCardPokerHandEvaluator {
    private static final List<Integer> WHEEL_VALUES_DESCENDING = List.of(14, 3, 2);

    private ThreeCardPokerHandEvaluator() {
    }

    public static ThreeCardPokerHand evaluate(List<Card> cards) {
        List<Integer> values = cards.stream()
                .map(card -> card.rank().value())
                .sorted(Comparator.reverseOrder())
                .toList();
        boolean flush = cards.stream().map(Card::suit).distinct().count() == 1;
        boolean straight = isStraight(values);
        boolean threeOfAKind = values.get(0).equals(values.get(1)) && values.get(1).equals(values.get(2));
        boolean pair = !threeOfAKind && (values.get(0).equals(values.get(1)) || values.get(1).equals(values.get(2)));

        if (straight && flush) {
            return new ThreeCardPokerHand(ThreeCardPokerHandRank.STRAIGHT_FLUSH, List.of(straightHighValue(values)));
        }
        if (threeOfAKind) {
            return new ThreeCardPokerHand(ThreeCardPokerHandRank.THREE_OF_A_KIND, List.of(values.get(0)));
        }
        if (straight) {
            return new ThreeCardPokerHand(ThreeCardPokerHandRank.STRAIGHT, List.of(straightHighValue(values)));
        }
        if (flush) {
            return new ThreeCardPokerHand(ThreeCardPokerHandRank.FLUSH, values);
        }
        if (pair) {
            int pairValue = values.get(0).equals(values.get(1)) ? values.get(0) : values.get(1);
            int kicker = values.get(0).equals(values.get(1)) ? values.get(2) : values.get(0);
            return new ThreeCardPokerHand(ThreeCardPokerHandRank.PAIR, List.of(pairValue, kicker));
        }
        return new ThreeCardPokerHand(ThreeCardPokerHandRank.HIGH_CARD, values);
    }

    private static boolean isStraight(List<Integer> valuesDescending) {
        if (valuesDescending.equals(WHEEL_VALUES_DESCENDING)) {
            return true;
        }
        return valuesDescending.get(0) - valuesDescending.get(1) == 1
                && valuesDescending.get(1) - valuesDescending.get(2) == 1;
    }

    private static int straightHighValue(List<Integer> valuesDescending) {
        return valuesDescending.equals(WHEEL_VALUES_DESCENDING) ? 3 : valuesDescending.get(0);
    }
}
