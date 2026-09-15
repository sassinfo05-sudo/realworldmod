package com.realworldmod.npc;

import com.realworldmod.RealWorldMod;
import com.realworldmod.npc.goap.DailyScheduleFSM;
import com.realworldmod.npc.goap.DailyState;

import java.util.List;

/**
 * Drives every loaded citizen's {@link DailyScheduleFSM} from the server's
 * day/time clock and persists any resulting state changes.
 *
 * <p>Minecraft's day cycle is 24000 ticks; we convert that to an in-game
 * hour-of-day (0-23) and only re-evaluate NPCs once per in-game "hour" worth
 * of ticks to avoid needless SQLite writes every server tick.
 */
public final class NpcScheduleManager {
    private static final long TICKS_PER_MINECRAFT_DAY = 24000L;
    private static final long TICKS_PER_HOUR = TICKS_PER_MINECRAFT_DAY / 24L;

    private final NpcDatabase database;
    private long lastEvaluatedHourBucket = -1;

    public NpcScheduleManager(NpcDatabase database) {
        this.database = database;
    }

    public void tick(long worldTimeOfDay) {
        long hourBucket = worldTimeOfDay / TICKS_PER_HOUR;
        if (hourBucket == lastEvaluatedHourBucket) {
            return;
        }
        lastEvaluatedHourBucket = hourBucket;

        int hourOfDay = (int) (hourBucket % 24L);
        List<NpcProfile> citizens = database.findAll();
        for (NpcProfile citizen : citizens) {
            DailyState previous = citizen.currentState();
            DailyState next = DailyScheduleFSM.nextState(
                    previous, hourOfDay, citizen.workStartHour(), citizen.workEndHour());
            if (next != previous) {
                citizen.setCurrentState(next);
                database.updateState(citizen.id(), next);
                RealWorldMod.LOGGER.debug("[RealWorldMod] {} transitioned {} -> {} at hour {}",
                        citizen.name(), previous, next, hourOfDay);
            }
        }
    }
}
