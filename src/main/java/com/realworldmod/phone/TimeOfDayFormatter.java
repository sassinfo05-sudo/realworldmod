package com.realworldmod.phone;

/**
 * Converts a Minecraft world "time of day" tick count into a 12-hour clock
 * string for display on the phone's lock screen. Vanilla's time-of-day is
 * 24000 ticks per day, with tick 0 corresponding to 06:00.
 */
public final class TimeOfDayFormatter {
    private static final long TICKS_PER_DAY = 24000L;
    private static final long MINUTES_PER_DAY = 24L * 60L;
    private static final int DAY_START_HOUR_OFFSET = 6;

    private TimeOfDayFormatter() {
    }

    public static String format(long timeOfDay) {
        long normalizedTicks = ((timeOfDay % TICKS_PER_DAY) + TICKS_PER_DAY) % TICKS_PER_DAY;
        long minutesSinceDayStart = (normalizedTicks * MINUTES_PER_DAY) / TICKS_PER_DAY;
        long minutesOfDay = (minutesSinceDayStart + (long) DAY_START_HOUR_OFFSET * 60L) % MINUTES_PER_DAY;
        int hour24 = (int) (minutesOfDay / 60L);
        int minute = (int) (minutesOfDay % 60L);

        String meridiem = hour24 < 12 ? "AM" : "PM";
        int hour12 = hour24 % 12;
        if (hour12 == 0) {
            hour12 = 12;
        }
        return String.format("%d:%02d %s", hour12, minute, meridiem);
    }
}
