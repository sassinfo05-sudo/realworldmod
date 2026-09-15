package com.realworldmod.crime;

/** Outcome of a {@link TrialService} trial, decided by the wanted level a player is *still* carrying when it concludes. */
public enum TrialVerdict {
    GUILTY,
    NOT_GUILTY;

    public static TrialVerdict forWantedLevel(int wantedLevelAtVerdict) {
        return wantedLevelAtVerdict >= WantedLevelMath.MAX ? GUILTY : NOT_GUILTY;
    }
}
