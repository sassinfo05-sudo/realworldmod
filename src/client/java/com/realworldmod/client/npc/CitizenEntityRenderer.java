package com.realworldmod.client.npc;

import com.realworldmod.npc.CitizenEntity;
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
 * Renders a {@link CitizenEntity} the same way {@code CarEntityRenderer}
 * renders a car: reusing {@link BlockRenderManager}'s block-as-entity path
 * instead of a real humanoid model, scaled to a rough person-shaped box. A
 * placeholder appearance, not real art — see ROADMAP.md. Also unverified
 * without a running game client.
 */
public final class CitizenEntityRenderer extends EntityRenderer<CitizenEntity> {
    private static final BlockState PLACEHOLDER_BODY = Blocks.LIGHT_GRAY_CONCRETE.getDefaultState();
    private static final Identifier PLACEHOLDER_TEXTURE = Identifier.ofVanilla("textures/block/light_gray_concrete.png");

    private final BlockRenderManager blockRenderManager;

    public CitizenEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.blockRenderManager = context.getBlockRenderManager();
    }

    @Override
    public Identifier getTexture(CitizenEntity entity) {
        return PLACEHOLDER_TEXTURE;
    }

    @Override
    public void render(CitizenEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                        VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - yaw));
        matrices.scale(0.5f, 1.8f, 0.3f);
        matrices.translate(-0.5, 0.0, -0.5);
        blockRenderManager.renderBlockAsEntity(PLACEHOLDER_BODY, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
