package com.realworldmod.npc;

import com.realworldmod.init.ModEntities;
import com.realworldmod.init.ModItems;
import com.realworldmod.npc.goap.DailyState;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Random;

/**
 * Right-clicking any block with a {@code CITIZEN_SPAWNER} in hand spawns a
 * {@link CitizenEntity} on top of it and creates the matching
 * {@link NpcProfile} row (keyed by the new entity's own UUID) so
 * {@code NpcScheduleManager} picks it up on the very next tick.
 *
 * <p>Reads the live database through {@link NpcAccess} rather than taking
 * it as a constructor dependency: this handler is registered exactly once
 * at mod init, but the database itself is recreated every time a world is
 * (re)loaded via {@code SERVER_STARTING} — registering the event handler
 * again on every load would duplicate it.
 */
public final class CitizenSpawnHandler {
    private static final List<String> NAME_POOL = List.of(
            "Alex Rivera", "Jordan Lee", "Sam Chen", "Morgan Diaz", "Casey Kim",
            "Taylor Brooks", "Riley Nguyen", "Jamie Patel");
    private static final Random RANDOM = new Random();

    private CitizenSpawnHandler() {
    }

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            if (!player.getStackInHand(hand).isOf(ModItems.CITIZEN_SPAWNER)) {
                return ActionResult.PASS;
            }
            NpcDatabase npcDatabase = NpcAccess.get();
            if (npcDatabase == null) {
                return ActionResult.PASS;
            }

            BlockPos spawnPos = hitResult.getBlockPos().up();
            CitizenEntity citizen = new CitizenEntity(ModEntities.CITIZEN, world);
            citizen.refreshPositionAndAngles(
                    spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getYaw(), 0.0f);

            String name = NAME_POOL.get(RANDOM.nextInt(NAME_POOL.size()));
            citizen.setCustomName(Text.literal(name));
            citizen.setCustomNameVisible(true);
            ((ServerWorld) world).spawnEntity(citizen);

            npcDatabase.upsert(new NpcProfile(citizen.getUuid(), name, "Unassigned", "Unassigned",
                    150_000L, 9, 17, DailyState.SLEEPING));
            return ActionResult.SUCCESS;
        });
    }
}
