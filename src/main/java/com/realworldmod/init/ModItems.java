package com.realworldmod.init;

import com.realworldmod.RealWorldMod;
import com.realworldmod.phone.PhoneBattery;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModItems {
    public static final Item SMARTPHONE = register("smartphone", new Item(new Item.Settings()
            .maxCount(1)
            .component(ModDataComponents.PHONE_BATTERY, PhoneBattery.MAX_LEVEL)));

    public static final Item LAND_DEED = register("land_deed", new Item(new Item.Settings().maxCount(16)));

    public static final Item MEDICINE = register("medicine", new Item(new Item.Settings().maxCount(16)));

    public static final Item CAR_KEY = register("car_key", new Item(new Item.Settings().maxCount(1)));

    public static final Item CITIZEN_SPAWNER = register("citizen_spawner", new Item(new Item.Settings().maxCount(16)));

    public static final Item DEER_SPAWNER = register("deer_spawner", new Item(new Item.Settings().maxCount(16)));

    public static final Item POLICE_SPAWNER = register("police_spawner", new Item(new Item.Settings().maxCount(16)));

    private ModItems() {
    }

    private static Item register(String path, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(RealWorldMod.MOD_ID, path), item);
    }

    public static void register() {
        // Classloading this class runs the static initializers above.
    }
}
