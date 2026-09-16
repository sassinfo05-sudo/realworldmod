package com.realworldmod.wildlife;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;

/**
 * Real, distinct wildlife (Section 8) — replacing the previous "any vanilla
 * passive animal counts as game" stand-in with an actual entity that has
 * its own behavior: it flees from an approaching player on proximity alone
 * ({@link FleeFromPlayerGoal}, unlike vanilla passive mobs which only flee
 * once hit), flees a nearby {@link CoyoteEntity} the same way
 * ({@link FleeFromPredatorGoal}, since slice 42's predator AI), and drifts
 * toward other nearby deer ({@link HerdWithOthersGoal}) rather than
 * wandering solo. {@link com.realworldmod.wildlife.PoachingHandler} now
 * only reacts to killing *this* entity, not arbitrary livestock — see
 * ROADMAP.md for the zoo/migration gaps still open.
 */
public final class DeerEntity extends PathAwareEntity {
    public DeerEntity(EntityType<? extends DeerEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new FleeFromPredatorGoal(this));
        this.goalSelector.add(2, new FleeFromPlayerGoal(this));
        this.goalSelector.add(3, new HerdWithOthersGoal(this));
        this.goalSelector.add(4, new WanderAroundGoal(this, 0.7));
        this.goalSelector.add(5, new LookAroundGoal(this));
    }
}
