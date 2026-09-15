package com.realworldmod.client.phone;

import com.realworldmod.phone.PhoneApp;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;

/**
 * Maps each {@link PhoneApp} to the {@link Screen} that renders it. Kept
 * separate from the enum itself so {@code PhoneApp} stays usable from
 * common/server code with no client dependency; later slices add a case
 * here per new app (banking, real estate, BlockTube, dark web, ...).
 */
public final class ClientPhoneApps {
    private ClientPhoneApps() {
    }

    public static Screen createScreen(PhoneApp app, ItemStack phoneStack) {
        return switch (app) {
            case SETTINGS -> new SettingsAppScreen(phoneStack);
            case MESSAGES -> new MessagesAppScreen(phoneStack);
            case BANKING -> new BankingAppScreen(phoneStack);
            case CRIMINAL_RECORD -> new CriminalRecordAppScreen(phoneStack);
        };
    }
}
