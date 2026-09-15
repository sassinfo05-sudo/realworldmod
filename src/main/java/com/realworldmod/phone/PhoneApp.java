package com.realworldmod.phone;

/**
 * Registry of apps installable on an in-game smartphone (Section 3 of the
 * design doc). This enum only carries the app's identity/display name; the
 * client module maps each entry to the {@code Screen} that renders it, so
 * this class stays usable from server-side/common code and from unit tests
 * with no Minecraft client dependency.
 */
public enum PhoneApp {
    SETTINGS("Settings"),
    MESSAGES("Messages"),
    BANKING("Banking");

    private final String displayName;

    PhoneApp(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
