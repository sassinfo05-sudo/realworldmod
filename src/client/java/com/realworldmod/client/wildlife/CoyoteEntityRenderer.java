package com.realworldmod.client.wildlife;

import com.realworldmod.RealWorldMod;
import com.realworldmod.client.model.DeerEntityModel;
import com.realworldmod.wildlife.CoyoteEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.util.Identifier;

/**
 * Renders a {@link CoyoteEntity} with the same real {@link DeerEntityModel}
 * quadruped rig {@code DeerEntityRenderer} uses (a similarly-shaped
 * four-legged animal), scaled down and given its own coyote texture,
 * rather than hand-building a second near-identical cuboid hierarchy —
 * the same model-reuse pattern {@code GameWardenEntityRenderer} already
 * established for {@code HumanoidEntityModel}. Still unverified without a
 * running game client — see ROADMAP.md.
 */
public final class CoyoteEntityRenderer extends LivingEntityRenderer<CoyoteEntity, DeerEntityModel<CoyoteEntity>> {
    private static final Identifier TEXTURE = Identifier.of(RealWorldMod.MOD_ID, "textures/entity/coyote.png");

    public CoyoteEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new DeerEntityModel<>(DeerEntityModel.createBodyLayer().createModel()), 0.35f);
        this.shadowRadius = 0.4f;
    }

    @Override
    public Identifier getTexture(CoyoteEntity entity) {
        return TEXTURE;
    }
}
