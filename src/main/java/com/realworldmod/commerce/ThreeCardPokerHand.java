package com.realworldmod.commerce;

import java.util.List;

/**
 * An evaluated three-card hand: its {@link ThreeCardPokerHandRank} plus
 * enough tiebreak values (highest first) to compare two hands of the same
 * rank — e.g. two pairs compare the pair's value then the kicker, two
 * straights compare their high card. Only ever compared against another
 * hand produced by {@link ThreeCardPokerHandEvaluator#evaluate}, so the
 * tiebreak lists are always the same length within a given rank.
 */
public record ThreeCardPokerHand(ThreeCardPokerHandRank rank, List<Integer> tiebreakValuesDescending)
        implements Comparable<ThreeCardPokerHand> {
    @Override
    public int compareTo(ThreeCardPokerHand other) {
        int rankComparison = rank.compareTo(other.rank);
        if (rankComparison != 0) {
            return rankComparison;
        }
        for (int i = 0; i < tiebreakValuesDescending.size(); i++) {
            int comparison = tiebreakValuesDescending.get(i) - other.tiebreakValuesDescending.get(i);
            if (comparison != 0) {
                return comparison;
            }
        }
        return 0;
    }
}
