package com.realworldmod.npc;

import com.realworldmod.economy.BankService;
import com.realworldmod.medical.PharmacyUseHandler;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.List;

/**
 * Closes "a sick citizen has no way to recover early since it never
 * spends its own income on medicine" (a gap this file has repeated since
 * slice 46). Each tick, a {@link CitizenEntity} carrying any of the same
 * curable effects {@code medical.MedicineUseHandler} treats for a player
 * checks whether it can afford {@link PharmacyUseHandler#MEDICINE_PRICE_CENTS}
 * out of its own {@link BankService} balance (the same wage slice 47 gave
 * it) and, if so, pays for it — remitting the same municipal sales-tax
 * cut a real pharmacy purchase would — and clears the effects immediately.
 *
 * <p>As of slice 61, this is no longer a "house call": a citizen must
 * actually be standing next to a real {@code PHARMACY_COUNTER} (the same
 * one {@link WalkToPharmacyGoal} walks it towards) before it can be
 * treated, closing the "no NPC pathfinding-to-a-shop behavior" gap this
 * class's own javadoc used to call out. A citizen with no pharmacy within
 * {@link PharmacyLocator#ARRIVAL_RADIUS} simply stays sick, the same real
 * limitation a player without one nearby would face.
 */
public final class CitizenSelfMedicationHandler {
    private static final List<RegistryEntry<StatusEffect>> CURABLE_EFFECTS =
            List.of(StatusEffects.NAUSEA, StatusEffects.WEAKNESS, StatusEffects.SLOWNESS);

    private CitizenSelfMedicationHandler() {
    }

    /** Checks whether {@code citizen} is sick/injured, standing at a real pharmacy counter, and can afford treatment, and cures it if so. */
    public static void tryTreat(BankService bankService, CitizenEntity citizen) {
        boolean needsTreatment = CURABLE_EFFECTS.stream().anyMatch(citizen::hasStatusEffect);
        if (!needsTreatment) {
            return;
        }
        boolean atPharmacy = PharmacyLocator.findNearest(citizen.getWorld(), citizen.getBlockPos(),
                PharmacyLocator.ARRIVAL_RADIUS, PharmacyLocator.ARRIVAL_VERTICAL_RADIUS).isPresent();
        if (!atPharmacy) {
            return;
        }
        if (bankService.withdraw(citizen.getUuid(), PharmacyUseHandler.MEDICINE_PRICE_CENTS).isEmpty()) {
            return;
        }

        bankService.remitSalesTax(PharmacyUseHandler.MEDICINE_PRICE_CENTS);
        for (RegistryEntry<StatusEffect> effect : CURABLE_EFFECTS) {
            citizen.removeStatusEffect(effect);
        }
    }
}
