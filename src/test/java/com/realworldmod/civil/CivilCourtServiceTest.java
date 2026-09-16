package com.realworldmod.civil;

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

class CivilCourtServiceTest {
    private BankService bankService;
    private CivilCourtService civilCourtService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        bankService = new BankService();
        bankService.open(tempDir.resolve("bank.sqlite"));
        civilCourtService = new CivilCourtService(bankService);
    }

    @AfterEach
    void tearDown() {
        bankService.close();
    }

    @Test
    void filingAClaimAgainstAFreeDefendantSucceeds() {
        UUID plaintiff = UUID.randomUUID();
        UUID defendant = UUID.randomUUID();

        assertTrue(civilCourtService.fileClaim(plaintiff, defendant, 0));
        assertTrue(civilCourtService.hasPendingCase(defendant));
    }

    @Test
    void cannotFileAClaimAgainstYourself() {
        UUID player = UUID.randomUUID();
        assertFalse(civilCourtService.fileClaim(player, player, 0));
    }

    @Test
    void cannotFileASecondClaimAgainstTheSameDefendant() {
        UUID defendant = UUID.randomUUID();
        civilCourtService.fileClaim(UUID.randomUUID(), defendant, 0);

        assertFalse(civilCourtService.fileClaim(UUID.randomUUID(), defendant, 0));
    }

    @Test
    void contestingDismissesThePendingCase() {
        UUID plaintiff = UUID.randomUUID();
        UUID defendant = UUID.randomUUID();
        civilCourtService.fileClaim(plaintiff, defendant, 0);

        assertTrue(civilCourtService.contest(defendant));
        assertFalse(civilCourtService.hasPendingCase(defendant));
    }

    @Test
    void contestingWithNoPendingCaseReturnsFalse() {
        assertFalse(civilCourtService.contest(UUID.randomUUID()));
    }

    @Test
    void tickDoesNothingBeforeTheDeadline() {
        UUID plaintiff = UUID.randomUUID();
        UUID defendant = UUID.randomUUID();
        bankService.deposit(defendant, 100_000);
        civilCourtService.fileClaim(plaintiff, defendant, 0);

        civilCourtService.tick(CivilCourtService.RESPONSE_WINDOW_TICKS - 1);

        assertTrue(civilCourtService.hasPendingCase(defendant));
        assertEquals(100_000, bankService.getBalance(defendant));
    }

    @Test
    void defaultJudgmentTransfersMoneyOnceTheDeadlinePasses() {
        UUID plaintiff = UUID.randomUUID();
        UUID defendant = UUID.randomUUID();
        bankService.deposit(defendant, 100_000);
        civilCourtService.fileClaim(plaintiff, defendant, 0);

        civilCourtService.tick(CivilCourtService.RESPONSE_WINDOW_TICKS);

        assertFalse(civilCourtService.hasPendingCase(defendant));
        assertEquals(100_000 - CivilCourtService.CLAIM_AMOUNT_CENTS, bankService.getBalance(defendant));
        assertEquals(CivilCourtService.CLAIM_AMOUNT_CENTS, bankService.getBalance(plaintiff));
    }

    @Test
    void defaultJudgmentClosesTheCaseEvenIfTheDefendantCannotPay() {
        UUID plaintiff = UUID.randomUUID();
        UUID defendant = UUID.randomUUID();
        civilCourtService.fileClaim(plaintiff, defendant, 0);

        civilCourtService.tick(CivilCourtService.RESPONSE_WINDOW_TICKS);

        assertFalse(civilCourtService.hasPendingCase(defendant));
        assertEquals(0, bankService.getBalance(plaintiff));
    }

    @Test
    void canFileANewClaimAgainstTheSameDefendantAfterTheirCaseCloses() {
        UUID defendant = UUID.randomUUID();
        civilCourtService.fileClaim(UUID.randomUUID(), defendant, 0);
        civilCourtService.contest(defendant);

        assertTrue(civilCourtService.fileClaim(UUID.randomUUID(), defendant, 0));
    }
}
