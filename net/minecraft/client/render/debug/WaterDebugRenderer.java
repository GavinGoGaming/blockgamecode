/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.debug;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Colors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

@Environment(value=EnvType.CLIENT)
public class WaterDebugRenderer
implements DebugRenderer.Renderer {
    private final MinecraftClient client;

    public WaterDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        FluidState lv4;
        BlockPos lv = this.client.player.getBlockPos();
        World lv2 = this.client.player.getWorld();
        for (BlockPos lv3 : BlockPos.iterate(lv.add(-10, -10, -10), lv.add(10, 10, 10))) {
            lv4 = lv2.getFluidState(lv3);
            if (!lv4.isIn(FluidTags.WATER)) continue;
            double g = (float)lv3.getY() + lv4.getHeight(lv2, lv3);
            DebugRenderer.drawBox(matrices, vertexConsumers, new Box((float)lv3.getX() + 0.01f, (float)lv3.getY() + 0.01f, (float)lv3.getZ() + 0.01f, (float)lv3.getX() + 0.99f, g, (float)lv3.getZ() + 0.99f).offset(-cameraX, -cameraY, -cameraZ), 0.0f, 1.0f, 0.0f, 0.15f);
        }
        for (BlockPos lv3 : BlockPos.iterate(lv.add(-10, -10, -10), lv.add(10, 10, 10))) {
            lv4 = lv2.getFluidState(lv3);
            if (!lv4.isIn(FluidTags.WATER)) continue;
            DebugRenderer.drawString(matrices, vertexConsumers, String.valueOf(lv4.getLevel()), (double)lv3.getX() + 0.5, (float)lv3.getY() + lv4.getHeight(lv2, lv3), (double)lv3.getZ() + 0.5, Colors.BLACK);
        }
    }
}

