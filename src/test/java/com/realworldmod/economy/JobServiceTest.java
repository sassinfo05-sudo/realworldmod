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

class JobServiceTest {
    private BankService bankService;
    private JobService jobService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        bankService = new BankService();
        bankService.open(tempDir.resolve("bank.sqlite"));
        jobService = new JobService(bankService);
    }

    @AfterEach
    void tearDown() {
        bankService.close();
    }

    @Test
    void firstShiftAlwaysPaysOut() {
        UUID player = UUID.randomUUID();
        assertTrue(jobService.tryWorkShift(player, 0));
        assertEquals(JobService.WAGE_CENTS, bankService.getBalance(player));
    }

    @Test
    void secondShiftWithinCooldownIsRefused() {
        UUID player = UUID.randomUUID();
        jobService.tryWorkShift(player, 0);

        assertFalse(jobService.tryWorkShift(player, 10));
        assertEquals(JobService.WAGE_CENTS, bankService.getBalance(player));
    }

    @Test
    void shiftAfterCooldownExpiresPaysAgain() {
        UUID player = UUID.randomUUID();
        jobService.tryWorkShift(player, 0);

        assertTrue(jobService.tryWorkShift(player, JobService.COOLDOWN_TICKS));
        assertEquals(JobService.WAGE_CENTS * 2, bankService.getBalance(player));
    }

    @Test
    void ticksRemainingCountsDownToZero() {
        UUID player = UUID.randomUUID();
        jobService.tryWorkShift(player, 0);

        assertEquals(JobService.COOLDOWN_TICKS - 50, jobService.ticksRemaining(player, 50));
        assertEquals(0, jobService.ticksRemaining(player, JobService.COOLDOWN_TICKS + 100));
    }

    @Test
    void differentPlayersHaveIndependentCooldowns() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        jobService.tryWorkShift(first, 0);

        assertTrue(jobService.tryWorkShift(second, 5));
    }
}
