package com.realworldmod.crime;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.EnumSet;

/**
 * Makes a {@link PoliceEntity} pursue the nearest sufficiently-wanted
 * player ({@link PoliceBehavior#shouldChase}), and — on actual contact
 * while the player is still at maximum wanted level
 * ({@link PoliceBehavior#canApprehend}) — apprehends them into a
 * {@link TrialService} trial rather than detaining them outright. This is
 * the "court/trial step before an automatic arrest becomes an adjudicated
 * one" from ROADMAP.md's priority list: {@link ArrestService} no longer
 * arrests anyone who hasn't actually been caught by an entity like this
 * one. Reads the live services through {@code CrimeAccess}/
 * {@code TrialAccess}/{@code ArrestAccess} since, like every other custom
 * {@code Goal} in this mod, the entity is constructed by
 * {@code EntityType.Builder} with no room for constructor injection.
 */
public final class ChaseWantedPlayerGoal extends Goal {
    private static final double CHASE_SPEED = 0.5;
    private static final double SEARCH_RADIUS = 16.0;

    private final PathAwareEntity police;
    private PlayerEntity target;

    public ChaseWantedPlayerGoal(PathAwareEntity police) {
        this.police = police;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        LawEnforcementService lawEnforcement = CrimeAccess.get();
        if (lawEnforcement == null) {
            return false;
        }
        PlayerEntity nearest = police.getWorld().getClosestPlayer(police, SEARCH_RADIUS);
        if (nearest == null || !PoliceBehavior.shouldChase(lawEnforcement.crimeService().getWantedLevel(nearest.getUuid()))) {
            return false;
        }
        target = nearest;
        return true;
    }

    @Override
    public void start() {
        police.getNavigation().startMovingTo(target, CHASE_SPEED);
    }

    @Override
    public boolean shouldContinue() {
        LawEnforcementService lawEnforcement = CrimeAccess.get();
        return target != null && target.isAlive() && lawEnforcement != null
                && PoliceBehavior.shouldChase(lawEnforcement.crimeService().getWantedLevel(target.getUuid()));
    }

    @Override
    public void tick() {
        if (police.getNavigation().isIdle()) {
            police.getNavigation().startMovingTo(target, CHASE_SPEED);
        }
        tryApprehend();
    }

    @Override
    public void stop() {
        target = null;
        police.getNavigation().stop();
    }

    private void tryApprehend() {
        LawEnforcementService lawEnforcement = CrimeAccess.get();
        ArrestService arrestService = ArrestAccess.get();
        TrialService trialService = TrialAccess.get();
        if (lawEnforcement == null || arrestService == null || trialService == null) {
            return;
        }
        int wantedLevel = lawEnforcement.crimeService().getWantedLevel(target.getUuid());
        if (!PoliceBehavior.canApprehend(wantedLevel, police.squaredDistanceTo(target))) {
            return;
        }
        if (arrestService.isDetained(target.getUuid()) || trialService.isOnTrial(target.getUuid())) {
            return;
        }
        trialService.beginTrial(target.getUuid(), police.getWorld().getTime());
        target.sendMessage(Text.translatable("message.realworldmod.apprehended"), false);
    }
}
