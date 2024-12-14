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
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public abstract class StuckObjectsFeatureRenderer<T extends LivingEntity, M extends PlayerEntityModel<T>>
extends FeatureRenderer<T, M> {
    public StuckObjectsFeatureRenderer(LivingEntityRenderer<T, M> entityRenderer) {
        super(entityRenderer);
    }

    protected abstract int getObjectCount(T var1);

    protected abstract void renderObject(MatrixStack var1, VertexConsumerProvider var2, int var3, Entity var4, float var5, float var6, float var7, float var8);

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, T arg3, float f, float g, float h, float j, float k, float l) {
        int m = this.getObjectCount(arg3);
        Random lv = Random.create(((Entity)arg3).getId());
        if (m <= 0) {
            return;
        }
        for (int n = 0; n < m; ++n) {
            arg.push();
            ModelPart lv2 = ((PlayerEntityModel)this.getContextModel()).getRandomPart(lv);
            ModelPart.Cuboid lv3 = lv2.getRandomCuboid(lv);
            lv2.rotate(arg);
            float o = lv.nextFloat();
            float p = lv.nextFloat();
            float q = lv.nextFloat();
            float r = MathHelper.lerp(o, lv3.minX, lv3.maxX) / 16.0f;
            float s = MathHelper.lerp(p, lv3.minY, lv3.maxY) / 16.0f;
            float t = MathHelper.lerp(q, lv3.minZ, lv3.maxZ) / 16.0f;
            arg.translate(r, s, t);
            o = -1.0f * (o * 2.0f - 1.0f);
            p = -1.0f * (p * 2.0f - 1.0f);
            q = -1.0f * (q * 2.0f - 1.0f);
            this.renderObject(arg, arg2, i, (Entity)arg3, o, p, q, h);
            arg.pop();
        }
    }
}

