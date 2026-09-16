package com.realworldmod;

import com.realworldmod.civil.CivilCourtHandler;
import com.realworldmod.civil.CivilCourtService;
import com.realworldmod.commerce.BlackjackService;
import com.realworldmod.commerce.RouletteUseHandler;
import com.realworldmod.commerce.SlotMachineUseHandler;
import com.realworldmod.commerce.net.BlackjackNetworking;
import com.realworldmod.crime.ArrestAccess;
import com.realworldmod.crime.ArrestHandler;
import com.realworldmod.crime.ArrestOutcome;
import com.realworldmod.crime.ArrestService;
import com.realworldmod.crime.CrimeAccess;
import com.realworldmod.crime.CrimeService;
import com.realworldmod.crime.LawEnforcementService;
import com.realworldmod.crime.PoliceSpawnHandler;
import com.realworldmod.crime.TrialAccess;
import com.realworldmod.crime.TrialService;
import com.realworldmod.crime.net.CrimeNetworking;
import com.realworldmod.economy.BankService;
import com.realworldmod.economy.JobService;
import com.realworldmod.economy.JobUseHandler;
import com.realworldmod.economy.net.BankNetworking;
import com.realworldmod.economy.net.TreasuryNetworking;
import com.realworldmod.init.ModBlocks;
import com.realworldmod.init.ModDataComponents;
import com.realworldmod.init.ModEntities;
import com.realworldmod.init.ModItemGroups;
import com.realworldmod.init.ModItems;
import com.realworldmod.medical.IllnessService;
import com.realworldmod.medical.LegInjuryEffect;
import com.realworldmod.medical.MedicineUseHandler;
import com.realworldmod.medical.PharmacyUseHandler;
import com.realworldmod.medical.WeatherIllnessEffect;
import com.realworldmod.npc.CitizenSpawnHandler;
import com.realworldmod.npc.NpcAccess;
import com.realworldmod.npc.NpcDatabase;
import com.realworldmod.npc.NpcScheduleManager;
import com.realworldmod.phone.PhoneUseHandler;
import com.realworldmod.property.ClaimRegistry;
import com.realworldmod.property.DeedUseHandler;
import com.realworldmod.property.PropertyAccess;
import com.realworldmod.property.PropertyProtection;
import com.realworldmod.property.PropertyService;
import com.realworldmod.property.PropertyTaxService;
import com.realworldmod.utilities.UtilityAccess;
import com.realworldmod.utilities.UtilityService;
import com.realworldmod.utilities.net.UtilityNetworking;
import com.realworldmod.underworld.NarcoticsHandler;
import com.realworldmod.underworld.NarcoticsService;
import com.realworldmod.vehicle.CarSpawnHandler;
import com.realworldmod.wildlife.DeerSpawnHandler;
import com.realworldmod.wildlife.GameWardenService;
import com.realworldmod.wildlife.GameWardenSpawnHandler;
import com.realworldmod.wildlife.HuntingLicenseService;
import com.realworldmod.wildlife.LicenseUseHandler;
import com.realworldmod.wildlife.PoachingAccess;
import com.realworldmod.wildlife.PoachingHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

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
    private final LawEnforcementService lawEnforcementService =
            new LawEnforcementService(crimeService, bankService);
    private final TrialService trialService = new TrialService();
    private final ArrestService arrestService = new ArrestService(crimeService, trialService);
    private final UtilityService utilityService = new UtilityService(bankService);
    private final IllnessService illnessService = new IllnessService();
    private final HuntingLicenseService huntingLicenseService = new HuntingLicenseService();
    private final GameWardenService gameWardenService = new GameWardenService(bankService);
    private final PropertyTaxService propertyTaxService =
            new PropertyTaxService(propertyService.registry(), bankService);
    private final BlackjackService blackjackService = new BlackjackService(bankService);
    private final CivilCourtService civilCourtService = new CivilCourtService(bankService);
    private final NarcoticsService narcoticsService = new NarcoticsService(bankService);

    @Override
    public void onInitialize() {
        LOGGER.info("[RealWorldMod] Initializing citizen simulation subsystem");

        ModDataComponents.register();
        ModItems.register();
        ModBlocks.register();
        ModEntities.register();
        ModItemGroups.register();
        PhoneUseHandler.register();
        PropertyAccess.set(propertyService.registry());
        CrimeAccess.set(lawEnforcementService);
        TrialAccess.set(trialService);
        ArrestAccess.set(arrestService);
        UtilityAccess.set(utilityService);
        new PropertyProtection(propertyService.registry(), lawEnforcementService).register();
        new DeedUseHandler(propertyService, bankService).register();
        BankNetworking.registerPayloadTypes();
        BankNetworking.registerServerReceiver(bankService);
        TreasuryNetworking.registerPayloadTypes();
        TreasuryNetworking.registerServerReceiver(bankService);
        CrimeNetworking.registerPayloadTypes();
        CrimeNetworking.registerServerReceiver(crimeService);
        UtilityNetworking.registerPayloadTypes();
        UtilityNetworking.registerServerReceivers(utilityService);
        new JobUseHandler(jobService).register();
        LegInjuryEffect.register();
        MedicineUseHandler.register();
        new PharmacyUseHandler(bankService).register();
        new LicenseUseHandler(bankService, huntingLicenseService).register();
        new SlotMachineUseHandler(bankService).register();
        new RouletteUseHandler(bankService).register();
        BlackjackNetworking.registerPayloadTypes();
        BlackjackNetworking.registerServerReceivers(blackjackService);
        new CivilCourtHandler(civilCourtService).register();
        new NarcoticsHandler(narcoticsService, lawEnforcementService).register();
        PoachingAccess.set(gameWardenService);
        new PoachingHandler(lawEnforcementService, huntingLicenseService, gameWardenService).register();
        CarSpawnHandler.register();
        CitizenSpawnHandler.register();
        DeerSpawnHandler.register();
        PoliceSpawnHandler.register();
        GameWardenSpawnHandler.register();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Path saveRoot = server.getSavePath(WorldSavePath.ROOT);

            Path npcDbPath = saveRoot.resolve("realworldmod").resolve("citizens.sqlite");
            npcDatabase = new NpcDatabase(npcDbPath);
            npcDatabase.open();
            scheduleManager = new NpcScheduleManager(npcDatabase);
            NpcAccess.set(npcDatabase);
            LOGGER.info("[RealWorldMod] Citizen database opened at {}", npcDbPath);

            Path claimDbPath = saveRoot.resolve("realworldmod").resolve("claims.sqlite");
            propertyService.open(claimDbPath);
            LOGGER.info("[RealWorldMod] Claim database opened at {} ({} claims loaded)",
                    claimDbPath, propertyService.registry().all().size());

            Path bankDbPath = saveRoot.resolve("realworldmod").resolve("bank.sqlite");
            bankService.open(bankDbPath);
            LOGGER.info("[RealWorldMod] Bank database opened at {}", bankDbPath);

            Path utilityDbPath = saveRoot.resolve("realworldmod").resolve("utilities.sqlite");
            utilityService.open(utilityDbPath);
            LOGGER.info("[RealWorldMod] Utility database opened at {}", utilityDbPath);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (scheduleManager != null) {
                long dayTime = server.getOverworld().getTimeOfDay();
                scheduleManager.tick(dayTime);
            }
            crimeService.tick(server.getOverworld().getTime());
            propertyTaxService.tick(server.getOverworld().getTime());
            civilCourtService.tick(server.getOverworld().getTime());

            List<ServerPlayerEntity> onlinePlayers = server.getPlayerManager().getPlayerList();
            List<UUID> disconnected = utilityService.tick(
                    server.getOverworld().getTime(), onlinePlayers.stream().map(ServerPlayerEntity::getUuid).toList());
            for (ServerPlayerEntity player : onlinePlayers) {
                if (disconnected.contains(player.getUuid())) {
                    player.sendMessage(Text.translatable("message.realworldmod.power_shutoff"), true);
                }
                WeatherIllnessEffect.check(illnessService, player);

                ArrestOutcome arrestOutcome = arrestService.tick(player.getUuid(), server.getOverworld().getTime());
                ArrestHandler.apply(arrestOutcome, server, player);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (npcDatabase != null) {
                npcDatabase.close();
            }
            propertyService.close();
            bankService.close();
            utilityService.close();
        });
    }
}
