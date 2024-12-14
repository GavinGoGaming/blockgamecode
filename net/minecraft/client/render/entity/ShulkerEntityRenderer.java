/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.ShulkerHeadFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ShulkerEntityRenderer
extends MobEntityRenderer<ShulkerEntity, ShulkerEntityModel<ShulkerEntity>> {
    private static final Identifier TEXTURE = TexturedRenderLayers.SHULKER_TEXTURE_ID.getTextureId().withPath(string -> "textures/" + string + ".png");
    private static final Identifier[] COLORED_TEXTURES = (Identifier[])TexturedRenderLayers.COLORED_SHULKER_BOXES_TEXTURES.stream().map(spriteId -> spriteId.getTextureId().withPath(string -> "textures/" + string + ".png")).toArray(Identifier[]::new);

    public ShulkerEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new ShulkerEntityModel(arg.getPart(EntityModelLayers.SHULKER)), 0.0f);
        this.addFeature(new ShulkerHeadFeatureRenderer(this));
    }

    @Override
    public Vec3d getPositionOffset(ShulkerEntity arg, float f) {
        return arg.getRenderPositionOffset(f).orElse(super.getPositionOffset(arg, f)).multiply(arg.getScale());
    }

    @Override
    public boolean shouldRender(ShulkerEntity arg, Frustum arg2, double d, double e, double f) {
        if (super.shouldRender(arg, arg2, d, e, f)) {
            return true;
        }
        return arg.getRenderPositionOffset(0.0f).filter(renderPositionOffset -> {
            EntityType<?> lv = arg.getType();
            float f = lv.getHeight() / 2.0f;
            float g = lv.getWidth() / 2.0f;
            Vec3d lv2 = Vec3d.ofBottomCenter(arg.getBlockPos());
            return arg2.isVisible(new Box(renderPositionOffset.x, renderPositionOffset.y + (double)f, renderPositionOffset.z, lv2.x, lv2.y + (double)f, lv2.z).expand(g, f, g));
        }).isPresent();
    }

    @Override
    public Identifier getTexture(ShulkerEntity arg) {
        return ShulkerEntityRenderer.getTexture(arg.getColor());
    }

    public static Identifier getTexture(@Nullable DyeColor shulkerColor) {
        if (shulkerColor == null) {
            return TEXTURE;
        }
        return COLORED_TEXTURES[shulkerColor.getId()];
    }

    @Override
    protected void setupTransforms(ShulkerEntity arg, MatrixStack arg2, float f, float g, float h, float i) {
        super.setupTransforms(arg, arg2, f, g + 180.0f, h, i);
        arg2.multiply(arg.getAttachedFace().getOpposite().getRotationQuaternion(), 0.0f, 0.5f, 0.0f);
    }
}

