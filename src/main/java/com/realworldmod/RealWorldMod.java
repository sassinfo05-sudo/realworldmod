package com.realworldmod;

import com.realworldmod.npc.NpcDatabase;
import com.realworldmod.npc.NpcScheduleManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Entry point for the RealWorld total-conversion mod.
 *
 * This is slice 1 of the design: a persistent, SQLite-backed citizen
 * population driven by a deterministic daily-schedule state machine.
 * Later slices (transit, economy, in-game internet, etc.) hook into the
 * same {@link NpcDatabase} and server tick lifecycle established here.
 */
public final class RealWorldMod implements ModInitializer {
    public static final String MOD_ID = "realworldmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private NpcDatabase npcDatabase;
    private NpcScheduleManager scheduleManager;

    @Override
    public void onInitialize() {
        LOGGER.info("[RealWorldMod] Initializing citizen simulation subsystem");

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Path saveRoot = server.getSavePath(WorldSavePath.ROOT);
            Path dbPath = saveRoot.resolve("realworldmod").resolve("citizens.sqlite");
            npcDatabase = new NpcDatabase(dbPath);
            npcDatabase.open();
            scheduleManager = new NpcScheduleManager(npcDatabase);
            LOGGER.info("[RealWorldMod] Citizen database opened at {}", dbPath);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (scheduleManager != null) {
                long dayTime = server.getOverworld().getTimeOfDay();
                scheduleManager.tick(dayTime);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (npcDatabase != null) {
                npcDatabase.close();
            }
        });
    }
}
