package com.realworldmod.economy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankServiceTest {
    private BankService service;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        service = new BankService();
        service.open(tempDir.resolve("bank.sqlite"));
    }

    @AfterEach
    void tearDown() {
        service.close();
    }

    @Test
    void newAccountStartsAtZero() {
        assertEquals(0, service.getBalance(UUID.randomUUID()));
    }

    @Test
    void depositIncreasesBalance() {
        UUID player = UUID.randomUUID();
        service.deposit(player, 500);
        assertEquals(500, service.getBalance(player));
    }

    @Test
    void withdrawSucceedsAndReducesBalance() {
        UUID player = UUID.randomUUID();
        service.deposit(player, 500);
        assertTrue(service.withdraw(player, 200).isPresent());
        assertEquals(300, service.getBalance(player));
    }

    @Test
    void withdrawFailsWithoutChangingBalanceWhenInsufficient() {
        UUID player = UUID.randomUUID();
        service.deposit(player, 100);
        assertFalse(service.withdraw(player, 200).isPresent());
        assertEquals(100, service.getBalance(player));
    }

    @Test
    void transferMovesMoneyBetweenAccounts() {
        UUID payer = UUID.randomUUID();
        UUID payee = UUID.randomUUID();
        service.deposit(payer, 1000);

        assertTrue(service.transfer(payer, payee, 400));
        assertEquals(600, service.getBalance(payer));
        assertEquals(400, service.getBalance(payee));
    }

    @Test
    void transferFailsAndChangesNothingWhenPayerLacksFunds() {
        UUID payer = UUID.randomUUID();
        UUID payee = UUID.randomUUID();
        service.deposit(payer, 100);

        assertFalse(service.transfer(payer, payee, 500));
        assertEquals(100, service.getBalance(payer));
        assertEquals(0, service.getBalance(payee));
    }

    @Test
    void remitSalesTaxDepositsIntoTheTreasuryAccount() {
        service.remitSalesTax(1500);
        assertEquals(SalesTax.taxCents(1500), service.getBalance(BankService.TREASURY_ACCOUNT_ID));
    }

    @Test
    void balancesArePersistedAndReloadable(@TempDir Path tempDir) {
        Path dbFile = tempDir.resolve("reload.sqlite");
        UUID player = UUID.randomUUID();

        BankService first = new BankService();
        first.open(dbFile);
        first.deposit(player, 750);
        first.close();

        BankService reopened = new BankService();
        reopened.open(dbFile);
        assertEquals(750, reopened.getBalance(player));
        reopened.close();
    }
}
