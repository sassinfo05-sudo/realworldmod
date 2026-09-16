package com.realworldmod.wildlife;

import com.realworldmod.init.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ItemScatterer;

/**
 * Drops real {@code DEER_MEAT}/{@code DEER_HIDE} and records a
 * {@link WildlifePopulationService} death on any {@link DeerEntity}'s
 * death — regardless of what killed it (a player poacher, a hunting-
 * licensed player, or a {@link CoyoteEntity}) — closing "a killed deer
 * simply dies with no meat/hide drop or population-count consequence."
 * Deliberately separate from {@link PoachingHandler}, which only cares
 * about *player* kills without a license; this fires for every deer
 * death, the same "any death, not just a specific cause" scope
 * {@code medical.LegInjuryEffect} already applies to every living entity.
 */
public final class DeerDropHandler {
    private static final int MEAT_DROP_COUNT = 2;
    private static final int HIDE_DROP_COUNT = 1;

    private final WildlifePopulationService wildlifePopulationService;

    public DeerDropHandler(WildlifePopulationService wildlifePopulationService) {
        this.wildlifePopulationService = wildlifePopulationService;
    }

    public void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof DeerEntity)) {
                return;
            }

            ItemScatterer.spawn(entity.getWorld(), entity.getX(), entity.getY(), entity.getZ(),
                    new ItemStack(ModItems.DEER_MEAT, MEAT_DROP_COUNT));
            ItemScatterer.spawn(entity.getWorld(), entity.getX(), entity.getY(), entity.getZ(),
                    new ItemStack(ModItems.DEER_HIDE, HIDE_DROP_COUNT));
            wildlifePopulationService.recordDeerDeath();
        });
    }
}
