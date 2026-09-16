package com.realworldmod.client.wildlife;

import com.realworldmod.RealWorldMod;
import com.realworldmod.client.model.HumanoidEntityModel;
import com.realworldmod.wildlife.GameWardenEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.util.Identifier;

/**
 * Renders a {@link GameWardenEntity} with the same real
 * {@link HumanoidEntityModel} {@code CitizenEntityRenderer}/
 * {@code PoliceEntityRenderer} use, with its own ranger-uniform texture.
 * Still not hand-crafted 3D art, and still unverified without a running
 * game client — see ROADMAP.md.
 */
public final class GameWardenEntityRenderer extends LivingEntityRenderer<GameWardenEntity, HumanoidEntityModel<GameWardenEntity>> {
    private static final Identifier TEXTURE = Identifier.of(RealWorldMod.MOD_ID, "textures/entity/game_warden.png");

    public GameWardenEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new HumanoidEntityModel<>(HumanoidEntityModel.createBodyLayer().createModel()), 0.5f);
    }

    @Override
    public Identifier getTexture(GameWardenEntity entity) {
        return TEXTURE;
    }
}
