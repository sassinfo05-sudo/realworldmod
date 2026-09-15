package com.realworldmod;

import com.realworldmod.crime.CrimeAccess;
import com.realworldmod.crime.CrimeService;
import com.realworldmod.crime.net.CrimeNetworking;
import com.realworldmod.economy.BankService;
import com.realworldmod.economy.JobService;
import com.realworldmod.economy.JobUseHandler;
import com.realworldmod.economy.net.BankNetworking;
import com.realworldmod.init.ModBlocks;
import com.realworldmod.init.ModDataComponents;
import com.realworldmod.init.ModItemGroups;
import com.realworldmod.init.ModItems;
import com.realworldmod.medical.LegInjuryEffect;
import com.realworldmod.npc.NpcDatabase;
import com.realworldmod.npc.NpcScheduleManager;
import com.realworldmod.phone.PhoneUseHandler;
import com.realworldmod.property.ClaimRegistry;
import com.realworldmod.property.DeedUseHandler;
import com.realworldmod.property.PropertyAccess;
import com.realworldmod.property.PropertyProtection;
import com.realworldmod.property.PropertyService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Entry point for the RealWorld total-conversion mod. Wires together the
 * per-system services (citizens, property, banking, jobs, medical, crime)
 * built up slice by slice — see ROADMAP.md for what's implemented versus
 * the much larger remaining design.
 */
public final class RealWorldMod implements ModInitializer {
    public static final String MOD_ID = "realworldmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private NpcDatabase npcDatabase;
    private NpcScheduleManager scheduleManager;
    private final PropertyService propertyService = new PropertyService(new ClaimRegistry());
    private final BankService bankService = new BankService();
    private final JobService jobService = new JobService(bankService);
    private final CrimeService crimeService = new CrimeService();

    @Override
    public void onInitialize() {
        LOGGER.info("[RealWorldMod] Initializing citizen simulation subsystem");

        ModDataComponents.register();
        ModItems.register();
        ModBlocks.register();
        ModItemGroups.register();
        PhoneUseHandler.register();
        PropertyAccess.set(propertyService.registry());
        CrimeAccess.set(crimeService);
        new PropertyProtection(propertyService.registry(), crimeService).register();
        new DeedUseHandler(propertyService, bankService).register();
        BankNetworking.registerPayloadTypes();
        BankNetworking.registerServerReceiver(bankService);
        CrimeNetworking.registerPayloadTypes();
        CrimeNetworking.registerServerReceiver(crimeService);
        new JobUseHandler(jobService).register();
        LegInjuryEffect.register();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Path saveRoot = server.getSavePath(WorldSavePath.ROOT);

            Path npcDbPath = saveRoot.resolve("realworldmod").resolve("citizens.sqlite");
            npcDatabase = new NpcDatabase(npcDbPath);
            npcDatabase.open();
            scheduleManager = new NpcScheduleManager(npcDatabase);
            LOGGER.info("[RealWorldMod] Citizen database opened at {}", npcDbPath);

            Path claimDbPath = saveRoot.resolve("realworldmod").resolve("claims.sqlite");
            propertyService.open(claimDbPath);
            LOGGER.info("[RealWorldMod] Claim database opened at {} ({} claims loaded)",
                    claimDbPath, propertyService.registry().all().size());

            Path bankDbPath = saveRoot.resolve("realworldmod").resolve("bank.sqlite");
            bankService.open(bankDbPath);
            LOGGER.info("[RealWorldMod] Bank database opened at {}", bankDbPath);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (scheduleManager != null) {
                long dayTime = server.getOverworld().getTimeOfDay();
                scheduleManager.tick(dayTime);
            }
            crimeService.tick(server.getOverworld().getTime());
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (npcDatabase != null) {
                npcDatabase.close();
            }
            propertyService.close();
            bankService.close();
        });
    }
}
