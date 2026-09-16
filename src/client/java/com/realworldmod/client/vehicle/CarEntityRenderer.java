package com.realworldmod.client.vehicle;

import com.realworldmod.RealWorldMod;
import com.realworldmod.client.model.CarEntityModel;
import com.realworldmod.vehicle.CarEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

/**
 * Renders a {@link CarEntity} with a real {@link CarEntityModel} (chassis,
 * cabin, and four independently-spinning wheels) and a hand-painted
 * texture, replacing slice 19's scaled light-blue-concrete block
 * placeholder. {@code CarEntity} is a plain {@code Entity}, not a
 * {@code LivingEntity}, so — unlike the humanoid/deer renderers — this
 * extends {@code EntityRenderer} directly and drives the model manually
 * rather than through {@code LivingEntityRenderer}, including its own
 * yaw rotation (the same approach the old placeholder renderer used).
 * Still Minecraft's own cuboid-model style, not sculpted 3D art, and
 * still unverified without a running game client — see ROADMAP.md.
 */
public final class CarEntityRenderer extends EntityRenderer<CarEntity> {
    private static final Identifier TEXTURE = Identifier.of(RealWorldMod.MOD_ID, "textures/entity/car.png");

    private final CarEntityModel model;

    public CarEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.model = new CarEntityModel(CarEntityModel.createBodyLayer().createModel());
    }

    @Override
    public Identifier getTexture(CarEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(CarEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                        VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - yaw));
        model.setAngles(entity, 0.0f, 0.0f, entity.age + tickDelta, 0.0f, 0.0f);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(model.getLayer(getTexture(entity)));
        model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
