package com.realworldmod.client.crime;

import com.realworldmod.crime.PoliceEntity;
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
 * Renders a {@link PoliceEntity} the same way {@code CitizenEntityRenderer}
 * and {@code DeerEntityRenderer} render theirs: reusing
 * {@link BlockRenderManager}'s block-as-entity path scaled to a rough
 * person-shaped box, in a darker blue than {@code CarEntityRenderer}'s
 * light-blue car to read as a distinct "uniform." A placeholder
 * appearance, not real art — see ROADMAP.md. Also unverified without a
 * running game client.
 */
public final class PoliceEntityRenderer extends EntityRenderer<PoliceEntity> {
    private static final BlockState PLACEHOLDER_BODY = Blocks.BLUE_CONCRETE.getDefaultState();
    private static final Identifier PLACEHOLDER_TEXTURE = Identifier.ofVanilla("textures/block/blue_concrete.png");

    private final BlockRenderManager blockRenderManager;

    public PoliceEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.blockRenderManager = context.getBlockRenderManager();
    }

    @Override
    public Identifier getTexture(PoliceEntity entity) {
        return PLACEHOLDER_TEXTURE;
    }

    @Override
    public void render(PoliceEntity entity, float yaw, float tickDelta, MatrixStack matrices,
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
