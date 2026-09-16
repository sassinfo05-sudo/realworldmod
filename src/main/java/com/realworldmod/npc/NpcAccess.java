package com.realworldmod.npc;

/**
 * Static holder exposing the live {@link NpcDatabase} to
 * {@code CitizenEntity}, whose instances are created by Minecraft's own
 * entity-spawning machinery outside the mod's constructor-injected object
 * graph — same pattern as {@code property.PropertyAccess}. Set from
 * {@code RealWorldMod}'s {@code SERVER_STARTING} handler, since (unlike
 * the other {@code *Access} holders) the database itself isn't created
 * until then.
 */
public final class NpcAccess {
    private static volatile NpcDatabase npcDatabase;

    private NpcAccess() {
    }

    public static void set(NpcDatabase database) {
        npcDatabase = database;
    }

    public static NpcDatabase get() {
        return npcDatabase;
    }
}
