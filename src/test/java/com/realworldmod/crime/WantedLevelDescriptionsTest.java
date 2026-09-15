package com.realworldmod.crime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WantedLevelDescriptionsTest {
    @Test
    void zeroIsClean() {
        assertEquals("gui.realworldmod.phone.crime.clean", WantedLevelDescriptions.translationKeyFor(0));
    }

    @Test
    void oneOrTwoIsMinor() {
        assertEquals("gui.realworldmod.phone.crime.minor", WantedLevelDescriptions.translationKeyFor(1));
        assertEquals("gui.realworldmod.phone.crime.minor", WantedLevelDescriptions.translationKeyFor(2));
    }

    @Test
    void threeOrFourIsWanted() {
        assertEquals("gui.realworldmod.phone.crime.wanted", WantedLevelDescriptions.translationKeyFor(3));
        assertEquals("gui.realworldmod.phone.crime.wanted", WantedLevelDescriptions.translationKeyFor(4));
    }

    @Test
    void fiveIsMostWanted() {
        assertEquals("gui.realworldmod.phone.crime.most_wanted", WantedLevelDescriptions.translationKeyFor(5));
    }
}
