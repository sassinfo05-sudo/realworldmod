package com.realworldmod.crime;

/** What, if anything, changed about a player's arrest status this tick. */
public enum ArrestOutcome {
    NOT_ARRESTED,
    JUST_ARRESTED,
    STILL_DETAINED,
    JUST_RELEASED,
    ACQUITTED
}
