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
 * Section 8's first real predator — until this slice, wildlife AI only
 * ever fled or herded; nothing stalked or hunted anything.
 * {@link HuntDeerGoal} finds and chases the nearest {@link DeerEntity},
 * dealing real damage on contact rather than just following it around.
 */
public final class CoyoteEntity extends PathAwareEntity {
    public CoyoteEntity(EntityType<? extends CoyoteEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new HuntDeerGoal(this));
        this.goalSelector.add(2, new WanderAroundGoal(this, 0.6));
        this.goalSelector.add(3, new LookAroundGoal(this));
    }
}
