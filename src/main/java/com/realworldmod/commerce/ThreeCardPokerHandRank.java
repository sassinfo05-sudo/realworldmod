package com.realworldmod.commerce;

/**
 * Three Card Poker's real hand-strength order, ordinal ascending — notably
 * different from standard five-card poker: a straight outranks a flush
 * here, because with only three cards a straight is genuinely rarer than
 * a flush (the opposite of five-card poker, where flushes are the rarer
 * of the two — the real rule, not a simplification).
 */
public enum ThreeCardPokerHandRank {
    HIGH_CARD, PAIR, FLUSH, STRAIGHT, THREE_OF_A_KIND, STRAIGHT_FLUSH
}
