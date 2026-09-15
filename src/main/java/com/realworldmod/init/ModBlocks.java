package com.realworldmod.init;

import com.realworldmod.RealWorldMod;
import com.realworldmod.utilities.UtilityLampBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlocks {
    /** A job site: interacting with it (empty hand) pays a wage — see {@code JobUseHandler}. */
    public static final Block CASH_REGISTER = register("cash_register",
            new Block(AbstractBlock.Settings.create().strength(3.5f).requiresTool()));

    /** A light that actually goes dark when its claim owner's power is disconnected — see {@code UtilityLampBlock}. */
    public static final Block UTILITY_LAMP = register("utility_lamp",
            new UtilityLampBlock(AbstractBlock.Settings.create()
                    .strength(0.3f)
                    .luminance(state -> state.get(UtilityLampBlock.LIT) ? 15 : 0)));

    /** Sells {@code MEDICINE} for real money — see {@code PharmacyUseHandler}. */
    public static final Block PHARMACY_COUNTER = register("pharmacy_counter",
            new Block(AbstractBlock.Settings.create().strength(3.5f).requiresTool()));

    /** Sells hunting licenses — see {@code LicenseUseHandler}. */
    public static final Block LICENSE_OFFICE = register("license_office",
            new Block(AbstractBlock.Settings.create().strength(3.5f).requiresTool()));

    private ModBlocks() {
    }

    private static Block register(String path, Block block) {
        Identifier id = Identifier.of(RealWorldMod.MOD_ID, path);
        Block registeredBlock = Registry.register(Registries.BLOCK, id, block);
        Registry.register(Registries.ITEM, id, new BlockItem(registeredBlock, new Item.Settings()));
        return registeredBlock;
    }

    public static void register() {
        // Classloading this class runs the static initializer above.
    }
}
