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
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

@Environment(value=EnvType.CLIENT)
public class MobSpawnerBlockEntityRenderer
implements BlockEntityRenderer<MobSpawnerBlockEntity> {
    private final EntityRenderDispatcher entityRenderDispatcher;

    public MobSpawnerBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.entityRenderDispatcher = ctx.getEntityRenderDispatcher();
    }

    @Override
    public void render(MobSpawnerBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
        World lv = arg.getWorld();
        if (lv == null) {
            return;
        }
        MobSpawnerLogic lv2 = arg.getLogic();
        Entity lv3 = lv2.getRenderedEntity(lv, arg.getPos());
        if (lv3 != null) {
            MobSpawnerBlockEntityRenderer.render(f, arg2, arg3, i, lv3, this.entityRenderDispatcher, lv2.getLastRotation(), lv2.getRotation());
        }
    }

    public static void render(float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Entity entity, EntityRenderDispatcher entityRenderDispatcher, double lastRotation, double rotation) {
        matrices.push();
        matrices.translate(0.5f, 0.0f, 0.5f);
        float g = 0.53125f;
        float h = Math.max(entity.getWidth(), entity.getHeight());
        if ((double)h > 1.0) {
            g /= h;
        }
        matrices.translate(0.0f, 0.4f, 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)MathHelper.lerp((double)tickDelta, lastRotation, rotation) * 10.0f));
        matrices.translate(0.0f, -0.2f, 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-30.0f));
        matrices.scale(g, g, g);
        entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, tickDelta, matrices, vertexConsumers, light);
        matrices.pop();
    }
}

