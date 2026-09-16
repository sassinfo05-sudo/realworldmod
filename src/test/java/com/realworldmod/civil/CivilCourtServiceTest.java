package com.realworldmod.civil;

import com.realworldmod.economy.BankService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
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

        assertTrue(civilCourtService.contest(defendant, 10));
        assertFalse(civilCourtService.hasPendingCase(defendant));
    }

    @Test
    void contestingWithNoPendingCaseReturnsFalse() {
        assertFalse(civilCourtService.contest(UUID.randomUUID(), 10));
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
    void getCaseReturnsTheDefendantsPendingCase() {
        UUID plaintiff = UUID.randomUUID();
        UUID defendant = UUID.randomUUID();
        civilCourtService.fileClaim(plaintiff, defendant, 0);

        Optional<CivilCourtService.Case> result = civilCourtService.getCase(defendant);

        assertTrue(result.isPresent());
        assertEquals(plaintiff, result.get().plaintiffId());
        assertEquals(CivilCourtService.CLAIM_AMOUNT_CENTS, result.get().amountCents());
    }

    @Test
    void getCaseIsEmptyWithNoPendingCase() {
        assertTrue(civilCourtService.getCase(UUID.randomUUID()).isEmpty());
    }

    @Test
    void canFileANewClaimAgainstTheSameDefendantAfterTheirCaseCloses() {
        UUID defendant = UUID.randomUUID();
        civilCourtService.fileClaim(UUID.randomUUID(), defendant, 0);
        civilCourtService.contest(defendant, 10);

        assertTrue(civilCourtService.fileClaim(UUID.randomUUID(), defendant, 0));
    }

    @Test
    void historyIsEmptyBeforeAnyCaseResolves() {
        assertTrue(civilCourtService.getHistoryFor(UUID.randomUUID()).isEmpty());
    }

    @Test
    void contestingArchivesTheCaseAsContested() {
        UUID plaintiff = UUID.randomUUID();
        UUID defendant = UUID.randomUUID();
        civilCourtService.fileClaim(plaintiff, defendant, 0);

        civilCourtService.contest(defendant, 42);

        List<CivilCourtService.ArchivedCase> history = civilCourtService.getHistoryFor(defendant);
        assertEquals(1, history.size());
        assertEquals(plaintiff, history.get(0).plaintiffId());
        assertEquals(defendant, history.get(0).defendantId());
        assertEquals(42, history.get(0).resolvedTick());
        assertTrue(history.get(0).contested());
    }

    @Test
    void defaultJudgmentArchivesTheCaseAsNotContested() {
        UUID plaintiff = UUID.randomUUID();
        UUID defendant = UUID.randomUUID();
        bankService.deposit(defendant, 100_000);
        civilCourtService.fileClaim(plaintiff, defendant, 0);

        civilCourtService.tick(CivilCourtService.RESPONSE_WINDOW_TICKS);

        List<CivilCourtService.ArchivedCase> history = civilCourtService.getHistoryFor(defendant);
        assertEquals(1, history.size());
        assertFalse(history.get(0).contested());
    }

    @Test
    void historyIsVisibleToBothPlaintiffAndDefendant() {
        UUID plaintiff = UUID.randomUUID();
        UUID defendant = UUID.randomUUID();
        civilCourtService.fileClaim(plaintiff, defendant, 0);
        civilCourtService.contest(defendant, 10);

        assertEquals(1, civilCourtService.getHistoryFor(plaintiff).size());
        assertEquals(1, civilCourtService.getHistoryFor(defendant).size());
    }

    @Test
    void historyIsMostRecentlyResolvedFirst() {
        UUID player = UUID.randomUUID();
        UUID firstOpponent = UUID.randomUUID();
        UUID secondOpponent = UUID.randomUUID();
        civilCourtService.fileClaim(player, firstOpponent, 0);
        civilCourtService.contest(firstOpponent, 10);
        civilCourtService.fileClaim(player, secondOpponent, 20);
        civilCourtService.contest(secondOpponent, 30);

        List<CivilCourtService.ArchivedCase> history = civilCourtService.getHistoryFor(player);
        assertEquals(2, history.size());
        assertEquals(secondOpponent, history.get(0).defendantId());
        assertEquals(firstOpponent, history.get(1).defendantId());
    }

    @Test
    void historyDoesNotIncludeCasesTheGivenPlayerWasNotPartyTo() {
        civilCourtService.fileClaim(UUID.randomUUID(), UUID.randomUUID(), 0);
        assertTrue(civilCourtService.getHistoryFor(UUID.randomUUID()).isEmpty());
    }
}
