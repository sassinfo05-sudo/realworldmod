package com.realworldmod.crime;

/** Result of recording a crime: the player's new wanted level, and whether a fine was actually withdrawn. */
public record OffenseOutcome(int wantedLevel, boolean fined) {
}
