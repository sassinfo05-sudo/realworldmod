package com.realworldmod.client.model;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

/**
 * A real, hand-built quadruped entity model for {@code DeerEntity} —
 * horizontal body, a forward-projecting head with a pair of antlers, and
 * four legs on a diagonal trot cycle (front-right paired with back-left,
 * front-left paired with back-right, the standard approximation every
 * vanilla quadruped mob model uses) — replacing the scaled block
 * placeholder from slice 22. Built the same verified-via-`javap`
 * {@code ModelPart} cuboid-hierarchy technique as
 * {@link HumanoidEntityModel}; still Minecraft's own blocky cuboid style,
 * not sculpted 3D art, and still unverified without a running game client.
 */
public final class DeerEntityModel<T extends LivingEntity> extends SinglePartEntityModel<T> {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart frontRightLeg;
    private final ModelPart frontLeftLeg;
    private final ModelPart backRightLeg;
    private final ModelPart backLeftLeg;

    public DeerEntityModel(ModelPart root) {
        super(RenderLayer::getEntityCutoutNoCull);
        this.root = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.frontRightLeg = root.getChild("front_right_leg");
        this.frontLeftLeg = root.getChild("front_left_leg");
        this.backRightLeg = root.getChild("back_right_leg");
        this.backLeftLeg = root.getChild("back_left_leg");
    }

    public static TexturedModelData createBodyLayer() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        root.addChild("body",
                ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -5.0f, -7.0f, 8.0f, 9.0f, 14.0f),
                ModelTransform.pivot(0.0f, 13.0f, 0.0f));

        ModelPartData head = root.addChild("head",
                ModelPartBuilder.create().uv(0, 24).cuboid(-2.5f, -3.0f, -7.0f, 5.0f, 5.0f, 7.0f),
                ModelTransform.pivot(0.0f, 9.0f, -7.0f));
        head.addChild("left_antler",
                ModelPartBuilder.create().uv(40, 24).cuboid(0.0f, -5.0f, 0.0f, 1.0f, 5.0f, 1.0f),
                ModelTransform.of(-2.0f, -3.0f, -2.0f, -0.3f, 0.0f, -0.4f));
        head.addChild("right_antler",
                ModelPartBuilder.create().uv(44, 24).cuboid(0.0f, -5.0f, 0.0f, 1.0f, 5.0f, 1.0f),
                ModelTransform.of(2.0f, -3.0f, -2.0f, -0.3f, 0.0f, 0.4f));

        root.addChild("front_right_leg",
                ModelPartBuilder.create().uv(28, 24).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 9.0f, 2.0f),
                ModelTransform.pivot(-3.0f, 17.0f, -5.0f));
        root.addChild("front_left_leg",
                ModelPartBuilder.create().uv(28, 24).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 9.0f, 2.0f),
                ModelTransform.pivot(3.0f, 17.0f, -5.0f));
        root.addChild("back_right_leg",
                ModelPartBuilder.create().uv(28, 24).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 9.0f, 2.0f),
                ModelTransform.pivot(-3.0f, 17.0f, 5.0f));
        root.addChild("back_left_leg",
                ModelPartBuilder.create().uv(28, 24).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 9.0f, 2.0f),
                ModelTransform.pivot(3.0f, 17.0f, 5.0f));

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float ageInTicks, float headYaw, float headPitch) {
        frontRightLeg.pitch = MathHelper.cos(limbAngle * 0.6662f) * 1.4f * limbDistance;
        backLeftLeg.pitch = MathHelper.cos(limbAngle * 0.6662f) * 1.4f * limbDistance;
        frontLeftLeg.pitch = MathHelper.cos(limbAngle * 0.6662f + (float) Math.PI) * 1.4f * limbDistance;
        backRightLeg.pitch = MathHelper.cos(limbAngle * 0.6662f + (float) Math.PI) * 1.4f * limbDistance;
    }
}
