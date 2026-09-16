package com.realworldmod.wildlife;

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
import net.minecraft.world.World;

/**
 * A real, visible game-warden NPC (Section 8) — the "warden NPCs are
 * simulated only as an automatic fine, not an agent" gap, closed the same
 * way slice 23 closed the equivalent gap for police: a
 * {@link ChasePoacherGoal} pursues a player {@link GameWardenService} has
 * flagged for poaching and apprehends them on contact, instead of the fine
 * from {@link PoachingHandler} being the entire consequence.
 */
public final class GameWardenEntity extends PathAwareEntity {
    public GameWardenEntity(EntityType<? extends GameWardenEntity> entityType, World world) {
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
        this.goalSelector.add(1, new ChasePoacherGoal(this));
        this.goalSelector.add(2, new WanderAroundGoal(this, 0.6));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(4, new LookAroundGoal(this));
    }
}
