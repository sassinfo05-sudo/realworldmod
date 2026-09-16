package com.realworldmod.crime;

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
 * A real, visible police NPC (Section 7) — the "warden/police as an agent"
 * gap called out since slice 10's wanted-level tracker went in, and the
 * physical half of the court/trial flow this slice builds:
 * {@link ChaseWantedPlayerGoal} pursues sufficiently-wanted players and
 * hands off to {@link TrialService} on contact, rather than
 * {@link ArrestService} detaining someone the moment an invisible timer
 * says their wanted level is maxed out.
 */
public final class PoliceEntity extends PathAwareEntity {
    public PoliceEntity(EntityType<? extends PoliceEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.32);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new ChaseWantedPlayerGoal(this));
        this.goalSelector.add(2, new WanderAroundGoal(this, 0.6));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(4, new LookAroundGoal(this));
    }
}
