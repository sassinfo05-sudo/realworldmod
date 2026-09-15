package com.realworldmod.npc.goap;

/** Coarse-grained phases of an NPC's 24-hour autonomous routine. */
public enum DailyState {
    SLEEPING,
    WAKING,
    COMMUTING_TO_WORK,
    WORKING,
    COMMUTING_HOME,
    LEISURE
}
