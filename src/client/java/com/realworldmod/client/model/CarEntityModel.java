package com.realworldmod.client.model;

import com.realworldmod.vehicle.CarEntity;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;

/**
 * A real, hand-built vehicle model for {@code CarEntity} — a low chassis
 * with a raised cabin, plus four independently-spinning wheels — replacing
 * the scaled light-blue-concrete block placeholder from slice 19. Wheel
 * spin is driven by {@link CarEntity#wheelRotation()}, a real synced
 * `DataTracker` value accumulated server-side from the vehicle's actual
 * speed (radians per tick = speed / wheel radius), not a cosmetic-only
 * client guess. Built with the same verified-via-`javap` `ModelPart`
 * technique as {@link HumanoidEntityModel}/{@link DeerEntityModel}; {@code
 * CarEntity} is a plain {@code Entity} rather than a {@code LivingEntity},
 * so unlike those two this model is driven by a hand-written
 * {@code EntityRenderer} rather than {@code LivingEntityRenderer}.
 */
public final class CarEntityModel extends SinglePartEntityModel<CarEntity> {
    private final ModelPart root;
    private final ModelPart frontLeftWheel;
    private final ModelPart frontRightWheel;
    private final ModelPart backLeftWheel;
    private final ModelPart backRightWheel;

    public CarEntityModel(ModelPart root) {
        super(RenderLayer::getEntityCutoutNoCull);
        this.root = root;
        this.frontLeftWheel = root.getChild("front_left_wheel");
        this.frontRightWheel = root.getChild("front_right_wheel");
        this.backLeftWheel = root.getChild("back_left_wheel");
        this.backRightWheel = root.getChild("back_right_wheel");
    }

    public static TexturedModelData createBodyLayer() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        // Ground contact (the entity's actual world position) is model-space
        // y=0; everything above ground is negative Y, matching how a
        // hand-written (non-LivingEntityRenderer) renderer places geometry
        // directly relative to the entity's own anchor point, with no
        // biped-style "feet at y=24" translation applied for us.
        ModelPartData body = root.addChild("body",
                ModelPartBuilder.create().uv(0, 0).cuboid(-7.0f, -3.0f, -13.0f, 14.0f, 6.0f, 26.0f),
                ModelTransform.pivot(0.0f, -9.0f, 0.0f));
        body.addChild("cabin",
                ModelPartBuilder.create().uv(0, 46).cuboid(-5.0f, -8.0f, -7.0f, 10.0f, 5.0f, 14.0f),
                ModelTransform.NONE);

        root.addChild("front_left_wheel",
                ModelPartBuilder.create().uv(60, 0).cuboid(-1.0f, -3.0f, -3.0f, 2.0f, 6.0f, 6.0f),
                ModelTransform.pivot(8.0f, -3.0f, -9.0f));
        root.addChild("front_right_wheel",
                ModelPartBuilder.create().uv(60, 0).mirrored().cuboid(-1.0f, -3.0f, -3.0f, 2.0f, 6.0f, 6.0f),
                ModelTransform.pivot(-8.0f, -3.0f, -9.0f));
        root.addChild("back_left_wheel",
                ModelPartBuilder.create().uv(60, 0).cuboid(-1.0f, -3.0f, -3.0f, 2.0f, 6.0f, 6.0f),
                ModelTransform.pivot(8.0f, -3.0f, 9.0f));
        root.addChild("back_right_wheel",
                ModelPartBuilder.create().uv(60, 0).mirrored().cuboid(-1.0f, -3.0f, -3.0f, 2.0f, 6.0f, 6.0f),
                ModelTransform.pivot(-8.0f, -3.0f, 9.0f));

        return TexturedModelData.of(modelData, 128, 64);
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(CarEntity entity, float limbAngle, float limbDistance, float ageInTicks, float headYaw, float headPitch) {
        float rotation = entity.wheelRotation();
        frontLeftWheel.pitch = rotation;
        frontRightWheel.pitch = rotation;
        backLeftWheel.pitch = rotation;
        backRightWheel.pitch = rotation;
    }
}
