/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ParrotEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.ParrotEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

@Environment(value=EnvType.CLIENT)
public class ShoulderParrotFeatureRenderer<T extends PlayerEntity>
extends FeatureRenderer<T, PlayerEntityModel<T>> {
    private final ParrotEntityModel model;

    public ShoulderParrotFeatureRenderer(FeatureRendererContext<T, PlayerEntityModel<T>> context, EntityModelLoader loader) {
        super(context);
        this.model = new ParrotEntityModel(loader.getModelPart(EntityModelLayers.PARROT));
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, T arg3, float f, float g, float h, float j, float k, float l) {
        this.renderShoulderParrot(arg, arg2, i, arg3, f, g, k, l, true);
        this.renderShoulderParrot(arg, arg2, i, arg3, f, g, k, l, false);
    }

    private void renderShoulderParrot(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T player, float limbAngle, float limbDistance, float headYaw, float headPitch, boolean leftShoulder) {
        NbtCompound lv = leftShoulder ? ((PlayerEntity)player).getShoulderEntityLeft() : ((PlayerEntity)player).getShoulderEntityRight();
        EntityType.get(lv.getString("id")).filter(type -> type == EntityType.PARROT).ifPresent(type -> {
            matrices.push();
            matrices.translate(leftShoulder ? 0.4f : -0.4f, player.isInSneakingPose() ? -1.3f : -1.5f, 0.0f);
            ParrotEntity.Variant lv = ParrotEntity.Variant.byIndex(lv.getInt("Variant"));
            VertexConsumer lv2 = vertexConsumers.getBuffer(this.model.getLayer(ParrotEntityRenderer.getTexture(lv)));
            this.model.poseOnShoulder(matrices, lv2, light, OverlayTexture.DEFAULT_UV, limbAngle, limbDistance, headYaw, headPitch, arg2.age);
            matrices.pop();
        });
    }
}

