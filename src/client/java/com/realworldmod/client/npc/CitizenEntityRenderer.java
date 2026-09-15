package com.realworldmod.client.npc;

import com.realworldmod.RealWorldMod;
import com.realworldmod.client.model.HumanoidEntityModel;
import com.realworldmod.npc.CitizenEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

/**
 * Renders a {@link CitizenEntity} with a real {@link HumanoidEntityModel}
 * (head/body/arms/legs with a walk cycle and head tracking) and a
 * hand-painted 64x64 skin-style texture, replacing slice 20's "scaled
 * vanilla concrete block" placeholder. Still not hand-crafted 3D art, and
 * still unverified without a running game client — see ROADMAP.md.
 */
public final class CitizenEntityRenderer extends LivingEntityRenderer<CitizenEntity, HumanoidEntityModel<CitizenEntity>> {
    private static final Identifier TEXTURE = Identifier.of(RealWorldMod.MOD_ID, "textures/entity/citizen.png");

    public CitizenEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new HumanoidEntityModel<>(HumanoidEntityModel.createBodyLayer().createModel()), 0.5f);
    }

    @Override
    public Identifier getTexture(CitizenEntity entity) {
        return TEXTURE;
    }
}
