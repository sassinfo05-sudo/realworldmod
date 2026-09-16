package com.realworldmod.client.wildlife;

import com.realworldmod.RealWorldMod;
import com.realworldmod.client.model.DeerEntityModel;
import com.realworldmod.wildlife.DeerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.util.Identifier;

/**
 * Renders a {@link DeerEntity} with a real {@link DeerEntityModel}
 * (body/head/antlers/legs with a diagonal-trot walk cycle) and a
 * hand-painted texture, replacing slice 22's scaled brown-terracotta block
 * placeholder. Still Minecraft's own cuboid-model style, not sculpted 3D
 * art, and still unverified without a running game client — see
 * ROADMAP.md.
 */
public final class DeerEntityRenderer extends LivingEntityRenderer<DeerEntity, DeerEntityModel<DeerEntity>> {
    private static final Identifier TEXTURE = Identifier.of(RealWorldMod.MOD_ID, "textures/entity/deer.png");

    public DeerEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new DeerEntityModel<>(DeerEntityModel.createBodyLayer().createModel()), 0.5f);
    }

    @Override
    public Identifier getTexture(DeerEntity entity) {
        return TEXTURE;
    }
}
