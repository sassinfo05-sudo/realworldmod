package com.realworldmod.vehicle;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * The first real, rideable wrapper around {@link VehiclePhysics} (Section
 * 4). Movement is server-authoritative and deliberately simple: no
 * client-side prediction/reconciliation, no suspension or tire-friction
 * variation by surface, no collision damage. See ROADMAP.md — this is the
 * highest-risk slice of the project so far because, unlike every earlier
 * Minecraft-side integration, its correctness (does it actually feel
 * driveable, is the model oriented/scaled right) cannot be verified by
 * inspecting bytecode or a generated refmap; it needs a running game
 * client.
 */
public class CarEntity extends Entity {
    /** Also the tank capacity — a car spawns with a full tank, see {@link #refuel}. */
    public static final double MAX_FUEL_LITERS = 100.0;
    private static final double STARTING_FUEL_LITERS = MAX_FUEL_LITERS;
    private static final float TURN_DEGREES_PER_TICK = 3.0f;
    private static final double GRAVITY_PER_TICK = 0.04;
    private static final double WHEEL_RADIUS_BLOCKS = 0.3;

    private static final TrackedData<Float> WHEEL_ROTATION =
            DataTracker.registerData(CarEntity.class, TrackedDataHandlerRegistry.FLOAT);

    private VehicleState vehicleState = VehicleState.atRestWithFuel(STARTING_FUEL_LITERS);

    public CarEntity(EntityType<? extends CarEntity> entityType, World world) {
        super(entityType, world);
    }

    public VehicleState vehicleState() {
        return vehicleState;
    }

    /** Adds fuel, capped at {@link #MAX_FUEL_LITERS} — see {@code vehicle.GasPumpUseHandler}. */
    public void refuel(double liters) {
        double newFuel = Math.min(MAX_FUEL_LITERS, vehicleState.fuelLiters() + liters);
        vehicleState = new VehicleState(vehicleState.speedBlocksPerTick(), newFuel);
    }

    /** Accumulated wheel-spin angle in radians, synced to the client for {@code CarEntityRenderer}. */
    public float wheelRotation() {
        return this.getDataTracker().get(WHEEL_ROTATION);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(WHEEL_ROTATION, 0.0f);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        double fuel = nbt.contains("Fuel") ? nbt.getDouble("Fuel") : STARTING_FUEL_LITERS;
        double speed = nbt.contains("Speed") ? nbt.getDouble("Speed") : 0.0;
        vehicleState = new VehicleState(speed, fuel);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putDouble("Fuel", vehicleState.fuelLiters());
        nbt.putDouble("Speed", vehicleState.speedBlocksPerTick());
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (this.getWorld().isClient || hand != Hand.MAIN_HAND) {
            return ActionResult.PASS;
        }
        if (player.isSneaking()) {
            player.sendMessage(Text.translatable("message.realworldmod.car_fuel_gauge",
                    String.format("%.1f", vehicleState.fuelLiters()), String.format("%.1f", MAX_FUEL_LITERS)), true);
            return ActionResult.SUCCESS;
        }
        if (this.getPassengerList().isEmpty()) {
            return player.startRiding(this) ? ActionResult.SUCCESS : ActionResult.PASS;
        }
        return ActionResult.PASS;
    }

    @Override
    public LivingEntity getControllingPassenger() {
        Entity passenger = this.getFirstPassenger();
        return passenger instanceof LivingEntity living ? living : null;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) {
            return;
        }

        LivingEntity controller = this.getControllingPassenger();
        double throttle = controller != null ? controller.forwardSpeed : 0.0;
        float steer = controller != null ? controller.sidewaysSpeed : 0.0f;

        vehicleState = VehiclePhysics.tick(vehicleState, throttle);

        float rotationDelta = (float) (vehicleState.speedBlocksPerTick() / WHEEL_RADIUS_BLOCKS);
        this.getDataTracker().set(WHEEL_ROTATION,
                (this.getDataTracker().get(WHEEL_ROTATION) + rotationDelta) % MathHelper.TAU);

        if (controller != null && vehicleState.speedBlocksPerTick() != 0) {
            this.setYaw(this.getYaw() - steer * TURN_DEGREES_PER_TICK);
            this.setBodyYaw(this.getYaw());
        }

        double speed = vehicleState.speedBlocksPerTick();
        double yawRadians = Math.toRadians(this.getYaw());
        double dx = -Math.sin(yawRadians) * speed;
        double dz = Math.cos(yawRadians) * speed;
        double dy = this.isOnGround() ? 0.0 : this.getVelocity().y - GRAVITY_PER_TICK;

        this.setVelocity(dx, dy, dz);
        this.move(MovementType.SELF, this.getVelocity());
    }
}
