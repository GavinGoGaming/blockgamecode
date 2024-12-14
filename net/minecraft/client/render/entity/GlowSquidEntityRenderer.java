/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.SquidEntityRenderer;
import net.minecraft.client.render.entity.model.SquidEntityModel;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class GlowSquidEntityRenderer
extends SquidEntityRenderer<GlowSquidEntity> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/squid/glow_squid.png");

    public GlowSquidEntityRenderer(EntityRendererFactory.Context arg, SquidEntityModel<GlowSquidEntity> arg2) {
        super(arg, arg2);
    }

    @Override
    public Identifier getTexture(GlowSquidEntity arg) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLight(GlowSquidEntity arg, BlockPos arg2) {
        int i = (int)MathHelper.clampedLerp(0.0f, 15.0f, 1.0f - (float)arg.getDarkTicksRemaining() / 10.0f);
        if (i == 15) {
            return 15;
        }
        return Math.max(i, super.getBlockLight(arg, arg2));
    }
}

