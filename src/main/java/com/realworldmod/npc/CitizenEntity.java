package com.realworldmod.npc;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * A citizen as a real, visible, wandering entity in the world — Section
 * 2's NPCs given a body. This slice deliberately keeps the AI to vanilla's
 * off-the-shelf wander/look goals; the citizen's {@link NpcProfile} (its
 * name, job, current {@code DailyState}) still lives entirely in
 * {@link NpcDatabase}, keyed by this entity's own UUID — the "daily
 * schedule" from slice 1 does not yet drive this entity's *movement*
 * (walking to a real home/workplace position), only its dialogue. See
 * ROADMAP.md.
 */
public final class CitizenEntity extends PathAwareEntity {
    public CitizenEntity(EntityType<? extends CitizenEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new WanderAroundGoal(this, 0.6));
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
        this.goalSelector.add(3, new LookAroundGoal(this));
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (this.getWorld().isClient) {
            return ActionResult.SUCCESS;
        }
        NpcDatabase database = NpcAccess.get();
        if (database != null) {
            database.findById(this.getUuid())
                    .ifPresent(profile -> player.sendMessage(Text.literal(NpcDialogue.greeting(profile)), false));
        }
        return ActionResult.SUCCESS;
    }
}
