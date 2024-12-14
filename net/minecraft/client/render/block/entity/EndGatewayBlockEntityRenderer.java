/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.EndPortalBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class EndGatewayBlockEntityRenderer
extends EndPortalBlockEntityRenderer<EndGatewayBlockEntity> {
    private static final Identifier BEAM_TEXTURE = Identifier.ofVanilla("textures/entity/end_gateway_beam.png");

    public EndGatewayBlockEntityRenderer(BlockEntityRendererFactory.Context arg) {
        super(arg);
    }

    @Override
    public void render(EndGatewayBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
        if (arg.isRecentlyGenerated() || arg.needsCooldownBeforeTeleporting()) {
            float g = arg.isRecentlyGenerated() ? arg.getRecentlyGeneratedBeamHeight(f) : arg.getCooldownBeamHeight(f);
            double d = arg.isRecentlyGenerated() ? (double)arg.getWorld().getTopY() : 50.0;
            g = MathHelper.sin(g * (float)Math.PI);
            int k = MathHelper.floor((double)g * d);
            int l = arg.isRecentlyGenerated() ? DyeColor.MAGENTA.getEntityColor() : DyeColor.PURPLE.getEntityColor();
            long m = arg.getWorld().getTime();
            BeaconBlockEntityRenderer.renderBeam(arg2, arg3, BEAM_TEXTURE, f, g, m, -k, k * 2, l, 0.15f, 0.175f);
        }
        super.render(arg, f, arg2, arg3, i, j);
    }

    @Override
    protected float getTopYOffset() {
        return 1.0f;
    }

    @Override
    protected float getBottomYOffset() {
        return 0.0f;
    }

    @Override
    protected RenderLayer getLayer() {
        return RenderLayer.getEndGateway();
    }

    @Override
    public int getRenderDistance() {
        return 256;
    }
}

