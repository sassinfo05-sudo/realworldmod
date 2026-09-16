package com.realworldmod.utilities;

import com.realworldmod.economy.BankService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaterServiceTest {
    private BankService bankService;
    private WaterService waterService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        bankService = new BankService();
        bankService.open(tempDir.resolve("bank.sqlite"));
        waterService = new WaterService(bankService);
        waterService.open(tempDir.resolve("water.sqlite"));
    }

    @AfterEach
    void tearDown() {
        bankService.close();
        waterService.close();
    }

    @Test
    void newPlayerStartsConnected() {
        assertTrue(waterService.getState(UUID.randomUUID()).connected());
    }

    @Test
    void billingWithSufficientFundsStaysConnected() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 5000);

        List<UUID> disconnected = waterService.tick(0, List.of(player));

        assertTrue(disconnected.isEmpty());
        assertTrue(waterService.getState(player).connected());
        assertEquals(5000 - WaterService.BILL_CENTS, bankService.getBalance(player));
    }

    @Test
    void billingWithoutFundsDisconnectsAndReportsIt() {
        UUID player = UUID.randomUUID();

        List<UUID> disconnected = waterService.tick(0, List.of(player));

        assertEquals(List.of(player), disconnected);
        WaterState state = waterService.getState(player);
        assertFalse(state.connected());
        assertEquals(WaterService.BILL_CENTS, state.unpaidCents());
    }

    @Test
    void tickOnlyBillsOncePerInterval() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 100_000);

        waterService.tick(0, List.of(player));
        waterService.tick(100, List.of(player));

        assertEquals(100_000 - WaterService.BILL_CENTS, bankService.getBalance(player));
    }

    @Test
    void secondBillingCycleAfterDisconnectAddsToDebt() {
        UUID player = UUID.randomUUID();

        waterService.tick(0, List.of(player));
        waterService.tick(WaterService.BILLING_INTERVAL_TICKS, List.of(player));

        assertEquals(WaterService.BILL_CENTS * 2, waterService.getState(player).unpaidCents());
    }

    @Test
    void payNowClearsDebtAndReconnectsWhenAffordable() {
        UUID player = UUID.randomUUID();
        waterService.tick(0, List.of(player));
        bankService.deposit(player, 5000);

        assertTrue(waterService.payNow(player));
        WaterState state = waterService.getState(player);
        assertTrue(state.connected());
        assertEquals(0, state.unpaidCents());
    }

    @Test
    void payNowFailsAndLeavesDebtWhenNotAffordable() {
        UUID player = UUID.randomUUID();
        waterService.tick(0, List.of(player));

        assertFalse(waterService.payNow(player));
        assertEquals(WaterService.BILL_CENTS, waterService.getState(player).unpaidCents());
    }

    @Test
    void stateIsPersistedAndReloadable(@TempDir Path tempDir) {
        Path bankDbFile = tempDir.resolve("bank2.sqlite");
        Path waterDbFile = tempDir.resolve("water2.sqlite");
        UUID player = UUID.randomUUID();

        BankService bank = new BankService();
        bank.open(bankDbFile);
        WaterService first = new WaterService(bank);
        first.open(waterDbFile);
        first.tick(0, List.of(player));
        first.close();
        bank.close();

        BankService reopenedBank = new BankService();
        reopenedBank.open(bankDbFile);
        WaterService reopened = new WaterService(reopenedBank);
        reopened.open(waterDbFile);
        assertFalse(reopened.getState(player).connected());
        assertEquals(WaterService.BILL_CENTS, reopened.getState(player).unpaidCents());
        reopened.close();
        reopenedBank.close();
    }
}
