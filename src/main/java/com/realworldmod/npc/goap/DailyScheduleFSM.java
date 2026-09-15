package com.realworldmod.npc.goap;

/**
 * Deterministic rule-tree ("GOAP-lite") transition function for an NPC's
 * daily routine. Pure and side-effect free so it can be unit tested without
 * a running Minecraft server.
 *
 * <p>Hours are in-game hours in the range [0, 24). NPCs are parameterized by
 * a {@code workStartHour}/{@code workEndHour} pair pulled from their
 * workplace shift, so not every citizen keeps identical hours.
 */
public final class DailyScheduleFSM {

    private DailyScheduleFSM() {
    }

    /** Ticks near a state boundary get a few in-game minutes of "in transit" to avoid instant teleport-like behavior. */
    private static final int COMMUTE_WINDOW_HOURS = 1;

    public static DailyState nextState(DailyState current, int hourOfDay, int workStartHour, int workEndHour) {
        boolean isWorkTime = isWithinShift(hourOfDay, workStartHour, workEndHour);
        boolean isCommuteToWorkWindow = isWithinShift(hourOfDay, workStartHour - COMMUTE_WINDOW_HOURS, workStartHour);
        boolean isCommuteHomeWindow = isWithinShift(hourOfDay, workEndHour, workEndHour + COMMUTE_WINDOW_HOURS);
        boolean isNight = hourOfDay >= 23 || hourOfDay < 6;

        return switch (current) {
            case SLEEPING -> isNight ? DailyState.SLEEPING : DailyState.WAKING;
            case WAKING -> isCommuteToWorkWindow ? DailyState.COMMUTING_TO_WORK : DailyState.LEISURE;
            case COMMUTING_TO_WORK -> isWorkTime ? DailyState.WORKING : DailyState.COMMUTING_TO_WORK;
            case WORKING -> isCommuteHomeWindow || !isWorkTime ? DailyState.COMMUTING_HOME : DailyState.WORKING;
            case COMMUTING_HOME -> isNight ? DailyState.SLEEPING : DailyState.LEISURE;
            case LEISURE -> {
                if (isNight) {
                    yield DailyState.SLEEPING;
                } else if (isCommuteToWorkWindow) {
                    yield DailyState.COMMUTING_TO_WORK;
                } else {
                    yield DailyState.LEISURE;
                }
            }
        };
    }

    private static boolean isWithinShift(int hour, int start, int end) {
        int normalizedHour = ((hour % 24) + 24) % 24;
        int normalizedStart = ((start % 24) + 24) % 24;
        int normalizedEnd = ((end % 24) + 24) % 24;
        if (normalizedStart == normalizedEnd) {
            return false;
        }
        if (normalizedStart < normalizedEnd) {
            return normalizedHour >= normalizedStart && normalizedHour < normalizedEnd;
        }
        // Shift wraps past midnight (e.g. night-shift factory workers).
        return normalizedHour >= normalizedStart || normalizedHour < normalizedEnd;
    }
}
