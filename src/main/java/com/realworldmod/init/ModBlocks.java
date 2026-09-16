package com.realworldmod.init;

import com.realworldmod.RealWorldMod;
import com.realworldmod.utilities.UtilityLampBlock;
import com.realworldmod.utilities.WaterOutletBlock;
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

    /** A real casino game with a real payout table — see {@code SlotMachineUseHandler}. */
    public static final Block SLOT_MACHINE = register("slot_machine",
            new Block(AbstractBlock.Settings.create().strength(3.5f).requiresTool()));

    /** A second real casino game — see {@code RouletteUseHandler}. */
    public static final Block ROULETTE_TABLE = register("roulette_table",
            new Block(AbstractBlock.Settings.create().strength(3.5f).requiresTool()));

    /** A third real casino game with a full interactive UI — see {@code BlackjackService}. */
    public static final Block BLACKJACK_TABLE = register("blackjack_table",
            new Block(AbstractBlock.Settings.create().strength(3.5f).requiresTool()));

    /** A fourth real casino game (real Ante/Play Three Card Poker) — see {@code ThreeCardPokerService}. */
    public static final Block POKER_TABLE = register("poker_table",
            new Block(AbstractBlock.Settings.create().strength(3.5f).requiresTool()));

    /** Files/contests small-claims civil cases — see {@code CivilCourtHandler}. */
    public static final Block COURTHOUSE = register("courthouse",
            new Block(AbstractBlock.Settings.create().strength(3.5f).requiresTool()));

    /** Section 7's underworld/narcotics gap, reduced to a cook/deal loop — see {@code NarcoticsHandler}. */
    public static final Block NARCOTICS_LAB = register("narcotics_lab",
            new Block(AbstractBlock.Settings.create().strength(3.5f).requiresTool()));

    /** Sells {@code ALCOHOL} and {@code CIGARETTE} for real money — see {@code LiquorStoreUseHandler}. */
    public static final Block LIQUOR_STORE = register("liquor_store",
            new Block(AbstractBlock.Settings.create().strength(3.5f).requiresTool()));

    /** Refuels the rider's parked car for real money — see {@code vehicle.GasPumpUseHandler}. */
    public static final Block GAS_PUMP = register("gas_pump",
            new Block(AbstractBlock.Settings.create().strength(3.5f).requiresTool()));

    /** A sink whose flowing state actually reflects water billing — see {@code utilities.WaterOutletBlock}. */
    public static final Block WATER_OUTLET = register("water_outlet",
            new WaterOutletBlock(AbstractBlock.Settings.create().strength(3.5f).requiresTool()));

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
