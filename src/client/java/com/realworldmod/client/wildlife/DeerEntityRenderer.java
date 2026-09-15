package com.realworldmod.client.wildlife;

import com.realworldmod.wildlife.DeerEntity;
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
 * Renders a {@link DeerEntity} the same way {@code CitizenEntityRenderer}
 * and {@code CarEntityRenderer} render their entities: reusing
 * {@link BlockRenderManager}'s block-as-entity path instead of a real
 * model, scaled to a rough four-legged-animal box. A placeholder
 * appearance, not real art — see ROADMAP.md. Also unverified without a
 * running game client.
 */
public final class DeerEntityRenderer extends EntityRenderer<DeerEntity> {
    private static final BlockState PLACEHOLDER_BODY = Blocks.BROWN_TERRACOTTA.getDefaultState();
    private static final Identifier PLACEHOLDER_TEXTURE = Identifier.ofVanilla("textures/block/brown_terracotta.png");

    private final BlockRenderManager blockRenderManager;

    public DeerEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.blockRenderManager = context.getBlockRenderManager();
    }

    @Override
    public Identifier getTexture(DeerEntity entity) {
        return PLACEHOLDER_TEXTURE;
    }

    @Override
    public void render(DeerEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                        VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - yaw));
        matrices.scale(0.7f, 0.9f, 1.1f);
        matrices.translate(-0.5, 0.0, -0.5);
        blockRenderManager.renderBlockAsEntity(PLACEHOLDER_BODY, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
