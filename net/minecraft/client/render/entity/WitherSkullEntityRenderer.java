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
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SkullEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class WitherSkullEntityRenderer
extends EntityRenderer<WitherSkullEntity> {
    private static final Identifier INVULNERABLE_TEXTURE = Identifier.ofVanilla("textures/entity/wither/wither_invulnerable.png");
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/wither/wither.png");
    private final SkullEntityModel model;

    public WitherSkullEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
        this.model = new SkullEntityModel(arg.getPart(EntityModelLayers.WITHER_SKULL));
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 35).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.NONE);
        return TexturedModelData.of(lv, 64, 64);
    }

    @Override
    protected int getBlockLight(WitherSkullEntity arg, BlockPos arg2) {
        return 15;
    }

    @Override
    public void render(WitherSkullEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        arg2.push();
        arg2.scale(-1.0f, -1.0f, 1.0f);
        float h = MathHelper.lerpAngleDegrees(g, arg.prevYaw, arg.getYaw());
        float j = MathHelper.lerp(g, arg.prevPitch, arg.getPitch());
        VertexConsumer lv = arg3.getBuffer(this.model.getLayer(this.getTexture(arg)));
        this.model.setHeadRotation(0.0f, h, j);
        this.model.render(arg2, lv, i, OverlayTexture.DEFAULT_UV);
        arg2.pop();
        super.render(arg, f, g, arg2, arg3, i);
    }

    @Override
    public Identifier getTexture(WitherSkullEntity arg) {
        return arg.isCharged() ? INVULNERABLE_TEXTURE : TEXTURE;
    }
}

