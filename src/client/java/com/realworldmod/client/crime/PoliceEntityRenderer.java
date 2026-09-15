package com.realworldmod.client.crime;

import com.realworldmod.RealWorldMod;
import com.realworldmod.client.model.HumanoidEntityModel;
import com.realworldmod.crime.PoliceEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

/**
 * Renders a {@link PoliceEntity} with the same real {@link
 * HumanoidEntityModel} {@code CitizenEntityRenderer} uses (head/body/arms/
 * legs with a walk cycle and head tracking), with its own uniform texture,
 * replacing slice 23's "scaled vanilla concrete block" placeholder. Still
 * not hand-crafted 3D art, and still unverified without a running game
 * client — see ROADMAP.md.
 */
public final class PoliceEntityRenderer extends LivingEntityRenderer<PoliceEntity, HumanoidEntityModel<PoliceEntity>> {
    private static final Identifier TEXTURE = Identifier.of(RealWorldMod.MOD_ID, "textures/entity/police.png");

    public PoliceEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new HumanoidEntityModel<>(HumanoidEntityModel.createBodyLayer().createModel()), 0.5f);
    }

    @Override
    public Identifier getTexture(PoliceEntity entity) {
        return TEXTURE;
    }
}
