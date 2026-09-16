package com.realworldmod.wildlife;

import com.realworldmod.economy.CurrencyFormatter;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.EnumSet;

/**
 * Makes a {@link GameWardenEntity} pursue the nearest player currently
 * flagged by {@link GameWardenService} (i.e. caught poaching within the
 * last {@link GameWardenService#ALERT_DURATION_TICKS}), and apprehends
 * them on contact — same shape as {@code crime.ChaseWantedPlayerGoal}, but
 * triggered by a poaching-specific alert rather than the general wanted
 * level, since {@code CrimeService} has no way to represent "wanted
 * specifically for poaching." Reads the live service through
 * {@link PoachingAccess} since, like every other custom {@code Goal} in
 * this mod, the entity is constructed by {@code EntityType.Builder} with
 * no room for constructor injection.
 */
public final class ChasePoacherGoal extends Goal {
    private static final double CHASE_SPEED = 0.5;
    private static final double SEARCH_RADIUS = 16.0;
    private static final double APPREHEND_DISTANCE_SQUARED = 2.5 * 2.5;

    private final PathAwareEntity warden;
    private PlayerEntity target;

    public ChasePoacherGoal(PathAwareEntity warden) {
        this.warden = warden;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        GameWardenService service = PoachingAccess.get();
        if (service == null) {
            return false;
        }
        PlayerEntity nearest = warden.getWorld().getClosestPlayer(warden, SEARCH_RADIUS);
        if (nearest == null || !service.isFlagged(nearest.getUuid(), warden.getWorld().getTime())) {
            return false;
        }
        target = nearest;
        return true;
    }

    @Override
    public void start() {
        warden.getNavigation().startMovingTo(target, CHASE_SPEED);
    }

    @Override
    public boolean shouldContinue() {
        GameWardenService service = PoachingAccess.get();
        return target != null && target.isAlive() && service != null
                && service.isFlagged(target.getUuid(), warden.getWorld().getTime());
    }

    @Override
    public void tick() {
        if (warden.getNavigation().isIdle()) {
            warden.getNavigation().startMovingTo(target, CHASE_SPEED);
        }
        tryApprehend();
    }

    @Override
    public void stop() {
        target = null;
        warden.getNavigation().stop();
    }

    private void tryApprehend() {
        GameWardenService service = PoachingAccess.get();
        if (service == null || warden.squaredDistanceTo(target) > APPREHEND_DISTANCE_SQUARED) {
            return;
        }
        boolean fined = service.apprehend(target.getUuid());
        if (fined) {
            target.sendMessage(Text.translatable("message.realworldmod.warden_fine_issued",
                    CurrencyFormatter.format(GameWardenService.APPREHENSION_FINE_CENTS)), true);
        } else {
            target.sendMessage(Text.translatable("message.realworldmod.warden_caught"), true);
        }
    }
}
