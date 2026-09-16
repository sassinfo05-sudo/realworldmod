package com.realworldmod.crime;

/** Maps a wanted level to the translation key describing it, kept separate so it's independently testable. */
public final class WantedLevelDescriptions {
    private WantedLevelDescriptions() {
    }

    public static String translationKeyFor(int wantedLevel) {
        if (wantedLevel <= 0) {
            return "gui.realworldmod.phone.crime.clean";
        }
        if (wantedLevel <= 2) {
            return "gui.realworldmod.phone.crime.minor";
        }
        if (wantedLevel <= 4) {
            return "gui.realworldmod.phone.crime.wanted";
        }
        return "gui.realworldmod.phone.crime.most_wanted";
    }
}
