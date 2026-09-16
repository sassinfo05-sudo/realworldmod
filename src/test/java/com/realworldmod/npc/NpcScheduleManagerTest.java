package com.realworldmod.npc;

import com.realworldmod.economy.BankService;
import com.realworldmod.economy.IncomeTax;
import com.realworldmod.npc.goap.DailyState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class NpcScheduleManagerTest {
    private static final long TICKS_PER_HOUR = 24000L / 24L;

    private NpcDatabase database;
    private BankService bankService;
    private NpcScheduleManager scheduleManager;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        database = new NpcDatabase(tempDir.resolve("citizens.sqlite"));
        database.open();
        bankService = new BankService();
        bankService.open(tempDir.resolve("bank.sqlite"));
        scheduleManager = new NpcScheduleManager(database, bankService);
    }

    @AfterEach
    void tearDown() {
        database.close();
        bankService.close();
    }

    @Test
    void transitioningIntoWorkingPaysTheCitizensWageAfterTax() {
        UUID citizenId = UUID.randomUUID();
        database.upsert(new NpcProfile(citizenId, "Alex Rivera", "Home", "Work",
                150_000L, 9, 17, 0, 64, 0, 24, 64, 0, DailyState.COMMUTING_TO_WORK));

        scheduleManager.tick(9 * TICKS_PER_HOUR);

        long expectedTax = IncomeTax.taxCents(150_000L);
        assertEquals(150_000L - expectedTax, bankService.getBalance(citizenId));
        assertEquals(DailyState.WORKING, database.findById(citizenId).orElseThrow().currentState());
    }

    @Test
    void theWithheldTaxReachesTheTreasury() {
        UUID citizenId = UUID.randomUUID();
        database.upsert(new NpcProfile(citizenId, "Jordan Lee", "Home", "Work",
                150_000L, 9, 17, 0, 64, 0, 24, 64, 0, DailyState.COMMUTING_TO_WORK));

        scheduleManager.tick(9 * TICKS_PER_HOUR);

        long expectedTax = IncomeTax.taxCents(150_000L);
        assertEquals(expectedTax, bankService.getBalance(BankService.TREASURY_ACCOUNT_ID));
    }

    @Test
    void transitioningToAnyOtherStateDoesNotPay() {
        UUID citizenId = UUID.randomUUID();
        database.upsert(new NpcProfile(citizenId, "Sam Chen", "Home", "Work",
                150_000L, 9, 17, 0, 64, 0, 24, 64, 0, DailyState.SLEEPING));

        // Hour 7 is within SLEEPING's night window's end but not yet the commute window; expect WAKING, not WORKING.
        scheduleManager.tick(7 * TICKS_PER_HOUR);

        assertNotEquals(DailyState.WORKING, database.findById(citizenId).orElseThrow().currentState());
        assertEquals(0, bankService.getBalance(citizenId));
    }

    @Test
    void remainingInWorkingDoesNotPayAgain() {
        UUID citizenId = UUID.randomUUID();
        database.upsert(new NpcProfile(citizenId, "Morgan Diaz", "Home", "Work",
                150_000L, 9, 17, 0, 64, 0, 24, 64, 0, DailyState.COMMUTING_TO_WORK));

        scheduleManager.tick(9 * TICKS_PER_HOUR);
        long balanceAfterFirstPay = bankService.getBalance(citizenId);
        scheduleManager.tick(10 * TICKS_PER_HOUR);

        assertEquals(balanceAfterFirstPay, bankService.getBalance(citizenId));
    }

    @Test
    void differentCitizensAreCreditedIndependently() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        database.upsert(new NpcProfile(first, "Casey Kim", "Home", "Work",
                100_000L, 9, 17, 0, 64, 0, 24, 64, 0, DailyState.COMMUTING_TO_WORK));
        database.upsert(new NpcProfile(second, "Taylor Brooks", "Home", "Work",
                200_000L, 9, 17, 0, 64, 0, 24, 64, 0, DailyState.COMMUTING_TO_WORK));

        scheduleManager.tick(9 * TICKS_PER_HOUR);

        assertEquals(100_000L - IncomeTax.taxCents(100_000L), bankService.getBalance(first));
        assertEquals(200_000L - IncomeTax.taxCents(200_000L), bankService.getBalance(second));
    }
}
