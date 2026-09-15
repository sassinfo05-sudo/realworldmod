package com.realworldmod.economy;

/** Formats an integer cents amount as a dollar string, e.g. {@code 12345 -> "$123.45"}. */
public final class CurrencyFormatter {
    private CurrencyFormatter() {
    }

    public static String format(long cents) {
        long sign = cents < 0 ? -1 : 1;
        long absCents = Math.abs(cents);
        return String.format("%s$%d.%02d", sign < 0 ? "-" : "", absCents / 100, absCents % 100);
    }
}
