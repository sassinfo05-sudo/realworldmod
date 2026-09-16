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

class UtilityServiceTest {
    private BankService bankService;
    private UtilityService utilityService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        bankService = new BankService();
        bankService.open(tempDir.resolve("bank.sqlite"));
        utilityService = new UtilityService(bankService);
        utilityService.open(tempDir.resolve("utilities.sqlite"));
    }

    @AfterEach
    void tearDown() {
        bankService.close();
        utilityService.close();
    }

    @Test
    void newPlayerStartsConnected() {
        assertTrue(utilityService.getState(UUID.randomUUID()).powerConnected());
    }

    @Test
    void billingWithSufficientFundsStaysConnected() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 5000);

        List<UUID> disconnected = utilityService.tick(0, List.of(player));

        assertTrue(disconnected.isEmpty());
        assertTrue(utilityService.getState(player).powerConnected());
        assertEquals(5000 - UtilityService.BILL_CENTS, bankService.getBalance(player));
    }

    @Test
    void billingWithoutFundsDisconnectsAndReportsIt() {
        UUID player = UUID.randomUUID();

        List<UUID> disconnected = utilityService.tick(0, List.of(player));

        assertEquals(List.of(player), disconnected);
        UtilityState state = utilityService.getState(player);
        assertFalse(state.powerConnected());
        assertEquals(UtilityService.BILL_CENTS, state.unpaidCents());
    }

    @Test
    void tickOnlyBillsOncePerInterval() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 100_000);

        utilityService.tick(0, List.of(player));
        utilityService.tick(100, List.of(player));

        assertEquals(100_000 - UtilityService.BILL_CENTS, bankService.getBalance(player));
    }

    @Test
    void secondBillingCycleAfterDisconnectAddsToDebt() {
        UUID player = UUID.randomUUID();

        utilityService.tick(0, List.of(player));
        utilityService.tick(UtilityService.BILLING_INTERVAL_TICKS, List.of(player));

        assertEquals(UtilityService.BILL_CENTS * 2, utilityService.getState(player).unpaidCents());
    }

    @Test
    void payNowClearsDebtAndReconnectsWhenAffordable() {
        UUID player = UUID.randomUUID();
        utilityService.tick(0, List.of(player));
        bankService.deposit(player, 5000);

        assertTrue(utilityService.payNow(player));
        UtilityState state = utilityService.getState(player);
        assertTrue(state.powerConnected());
        assertEquals(0, state.unpaidCents());
    }

    @Test
    void payNowFailsAndLeavesDebtWhenNotAffordable() {
        UUID player = UUID.randomUUID();
        utilityService.tick(0, List.of(player));

        assertFalse(utilityService.payNow(player));
        assertEquals(UtilityService.BILL_CENTS, utilityService.getState(player).unpaidCents());
    }

    @Test
    void stateIsPersistedAndReloadable(@TempDir Path tempDir) {
        Path bankDbFile = tempDir.resolve("bank2.sqlite");
        Path utilityDbFile = tempDir.resolve("utilities2.sqlite");
        UUID player = UUID.randomUUID();

        BankService bank = new BankService();
        bank.open(bankDbFile);
        UtilityService first = new UtilityService(bank);
        first.open(utilityDbFile);
        first.tick(0, List.of(player));
        first.close();
        bank.close();

        BankService reopenedBank = new BankService();
        reopenedBank.open(bankDbFile);
        UtilityService reopened = new UtilityService(reopenedBank);
        reopened.open(utilityDbFile);
        assertFalse(reopened.getState(player).powerConnected());
        assertEquals(UtilityService.BILL_CENTS, reopened.getState(player).unpaidCents());
        reopened.close();
        reopenedBank.close();
    }
}
