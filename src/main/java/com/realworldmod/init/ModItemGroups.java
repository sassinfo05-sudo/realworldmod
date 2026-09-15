package com.realworldmod.init;

import com.realworldmod.RealWorldMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ModItemGroups {
    public static final ItemGroup ELECTRONICS = Registry.register(
            Registries.ITEM_GROUP,
            Identifier.of(RealWorldMod.MOD_ID, "electronics"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.SMARTPHONE))
                    .displayName(Text.translatable("itemGroup.realworldmod.electronics"))
                    .entries((context, entries) -> {
                        entries.add(ModItems.SMARTPHONE);
                        entries.add(ModItems.LAND_DEED);
                        entries.add(ModItems.MEDICINE);
                        entries.add(ModItems.CAR_KEY);
                        entries.add(ModItems.CITIZEN_SPAWNER);
                        entries.add(ModBlocks.CASH_REGISTER);
                        entries.add(ModBlocks.UTILITY_LAMP);
                        entries.add(ModBlocks.PHARMACY_COUNTER);
                        entries.add(ModBlocks.LICENSE_OFFICE);
                    })
                    .build());

    private ModItemGroups() {
    }

    public static void register() {
        // Classloading this class runs the static initializer above.
    }
}
