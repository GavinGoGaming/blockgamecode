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
import net.minecraft.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.block.spawner.TrialSpawnerData;
import net.minecraft.block.spawner.TrialSpawnerLogic;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.MobSpawnerBlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

@Environment(value=EnvType.CLIENT)
public class TrialSpawnerBlockEntityRenderer
implements BlockEntityRenderer<TrialSpawnerBlockEntity> {
    private final EntityRenderDispatcher entityRenderDispatcher;

    public TrialSpawnerBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.entityRenderDispatcher = context.getEntityRenderDispatcher();
    }

    @Override
    public void render(TrialSpawnerBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
        World lv = arg.getWorld();
        if (lv == null) {
            return;
        }
        TrialSpawnerLogic lv2 = arg.getSpawner();
        TrialSpawnerData lv3 = lv2.getData();
        Entity lv4 = lv3.setDisplayEntity(lv2, lv, lv2.getSpawnerState());
        if (lv4 != null) {
            MobSpawnerBlockEntityRenderer.render(f, arg2, arg3, i, lv4, this.entityRenderDispatcher, lv3.getLastDisplayEntityRotation(), lv3.getDisplayEntityRotation());
        }
    }
}

