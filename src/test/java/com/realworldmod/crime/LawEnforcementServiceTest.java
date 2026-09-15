package com.realworldmod.crime;

import com.realworldmod.economy.BankService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LawEnforcementServiceTest {
    private BankService bankService;
    private LawEnforcementService lawEnforcementService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        bankService = new BankService();
        bankService.open(tempDir.resolve("bank.sqlite"));
        lawEnforcementService = new LawEnforcementService(new CrimeService(), bankService);
    }

    @AfterEach
    void tearDown() {
        bankService.close();
    }

    @Test
    void offenseBelowThresholdDoesNotFine() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 100_000);

        OffenseOutcome outcome = lawEnforcementService.recordOffense(player, 1);

        assertEquals(1, outcome.wantedLevel());
        assertFalse(outcome.fined());
        assertEquals(100_000, bankService.getBalance(player));
    }

    @Test
    void offenseAtThresholdFinesWhenAffordable() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 100_000);

        OffenseOutcome outcome = lawEnforcementService.recordOffense(player, LawEnforcementService.FINE_THRESHOLD);

        assertTrue(outcome.fined());
        assertEquals(100_000 - LawEnforcementService.FINE_CENTS, bankService.getBalance(player));
    }

    @Test
    void offenseAtThresholdWithoutFundsIsNotFinedAndDoesNotThrow() {
        UUID player = UUID.randomUUID();

        OffenseOutcome outcome = lawEnforcementService.recordOffense(player, LawEnforcementService.FINE_THRESHOLD);

        assertFalse(outcome.fined());
        assertEquals(0, bankService.getBalance(player));
    }
}
