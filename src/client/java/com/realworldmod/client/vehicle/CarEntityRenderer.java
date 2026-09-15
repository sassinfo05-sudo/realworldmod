package com.realworldmod.client.vehicle;

import com.realworldmod.vehicle.CarEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

/**
 * Renders a {@link CarEntity} by reusing {@link BlockRenderManager}'s
 * existing "draw a block as a standalone entity" path (the same mechanism
 * {@code FallingBlockEntityRenderer} uses for sand/gravel) instead of
 * defining custom cuboid geometry — this is a placeholder appearance (a
 * vanilla concrete block scaled into a rough car silhouette), not real
 * art. Whether the scale/orientation actually looks right cannot be
 * verified without a running game client — see ROADMAP.md.
 */
public final class CarEntityRenderer extends EntityRenderer<CarEntity> {
    private static final BlockState PLACEHOLDER_BODY = Blocks.LIGHT_BLUE_CONCRETE.getDefaultState();
    private static final Identifier PLACEHOLDER_TEXTURE = Identifier.ofVanilla("textures/block/light_blue_concrete.png");

    private final BlockRenderManager blockRenderManager;

    public CarEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.blockRenderManager = context.getBlockRenderManager();
    }

    @Override
    public Identifier getTexture(CarEntity entity) {
        return PLACEHOLDER_TEXTURE;
    }

    @Override
    public void render(CarEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                        VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.translate(0.0, 0.25, 0.0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - yaw));
        matrices.scale(0.9f, 0.6f, 1.7f);
        matrices.translate(-0.5, 0.0, -0.5);
        blockRenderManager.renderBlockAsEntity(PLACEHOLDER_BODY, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
