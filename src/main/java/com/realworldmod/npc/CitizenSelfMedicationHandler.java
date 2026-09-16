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
 * <p>Deliberately simplified relative to a player's own treatment: a
 * citizen doesn't need to actually walk to a {@code PHARMACY_COUNTER}
 * structure first, since the mod has no NPC pathfinding-to-a-shop
 * behavior yet — this models "calls in a house visit" rather than a
 * shopping trip. See ROADMAP.md.
 */
public final class CitizenSelfMedicationHandler {
    private static final List<RegistryEntry<StatusEffect>> CURABLE_EFFECTS =
            List.of(StatusEffects.NAUSEA, StatusEffects.WEAKNESS, StatusEffects.SLOWNESS);

    private CitizenSelfMedicationHandler() {
    }

    /** Checks whether {@code citizen} is sick/injured and can afford treatment, and cures it if so. */
    public static void tryTreat(BankService bankService, CitizenEntity citizen) {
        boolean needsTreatment = CURABLE_EFFECTS.stream().anyMatch(citizen::hasStatusEffect);
        if (!needsTreatment) {
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
