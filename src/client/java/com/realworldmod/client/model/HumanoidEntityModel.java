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
 * A real, hand-built humanoid entity model — proper head/body/arm/leg
 * geometry with a walk-cycle and head-tracking animation — replacing the
 * "scaled vanilla concrete block" placeholder every entity in the mod used
 * through slice 23. Shared by {@code CitizenEntity} and {@code PoliceEntity}
 * (both plain humanoid bipeds); each gets its own texture rather than its
 * own model. Not a hand-sculpted 3D asset — it's built the same way vanilla
 * builds its own mob models (a cuboid-hierarchy {@link ModelPart} tree) —
 * but it is a genuine shaped body with real limb animation, a real step up
 * from a single textured box. Visual correctness is still unverified
 * without a running game client — see ROADMAP.md.
 */
public final class HumanoidEntityModel<T extends LivingEntity> extends SinglePartEntityModel<T> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public HumanoidEntityModel(ModelPart root) {
        super(RenderLayer::getEntityCutoutNoCull);
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    /** Standard 64x64 biped layout: head, torso, two arms, two legs. */
    public static TexturedModelData createBodyLayer() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        root.addChild("head",
                ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f),
                ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        root.addChild("body",
                ModelPartBuilder.create().uv(16, 16).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f),
                ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        root.addChild("right_arm",
                ModelPartBuilder.create().uv(40, 16).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        root.addChild("left_arm",
                ModelPartBuilder.create().uv(40, 16).mirrored().cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        root.addChild("right_leg",
                ModelPartBuilder.create().uv(0, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        root.addChild("left_leg",
                ModelPartBuilder.create().uv(0, 16).mirrored().cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float ageInTicks, float headYaw, float headPitch) {
        head.yaw = headYaw * (MathHelper.PI / 180f);
        head.pitch = headPitch * (MathHelper.PI / 180f);

        rightArm.pitch = MathHelper.cos(limbAngle * 0.6662f) * 2.0f * limbDistance * 0.5f;
        leftArm.pitch = MathHelper.cos(limbAngle * 0.6662f + (float) Math.PI) * 2.0f * limbDistance * 0.5f;
        rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662f) * 1.4f * limbDistance;
        leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662f + (float) Math.PI) * 1.4f * limbDistance;
    }
}
