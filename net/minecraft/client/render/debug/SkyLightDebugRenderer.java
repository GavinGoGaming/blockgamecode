/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.debug;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;

@Environment(value=EnvType.CLIENT)
public class SkyLightDebugRenderer
implements DebugRenderer.Renderer {
    private final MinecraftClient client;
    private static final int RANGE = 10;

    public SkyLightDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        ClientWorld lv = this.client.world;
        BlockPos lv2 = BlockPos.ofFloored(cameraX, cameraY, cameraZ);
        LongOpenHashSet longSet = new LongOpenHashSet();
        for (BlockPos lv3 : BlockPos.iterate(lv2.add(-10, -10, -10), lv2.add(10, 10, 10))) {
            int i = lv.getLightLevel(LightType.SKY, lv3);
            float g = (float)(15 - i) / 15.0f * 0.5f + 0.16f;
            int j = MathHelper.hsvToRgb(g, 0.9f, 0.9f);
            long l = ChunkSectionPos.fromBlockPos(lv3.asLong());
            if (longSet.add(l)) {
                DebugRenderer.drawString(matrices, vertexConsumers, lv.getChunkManager().getLightingProvider().displaySectionLevel(LightType.SKY, ChunkSectionPos.from(l)), ChunkSectionPos.getOffsetPos(ChunkSectionPos.unpackX(l), 8), ChunkSectionPos.getOffsetPos(ChunkSectionPos.unpackY(l), 8), ChunkSectionPos.getOffsetPos(ChunkSectionPos.unpackZ(l), 8), 0xFF0000, 0.3f);
            }
            if (i == 15) continue;
            DebugRenderer.drawString(matrices, vertexConsumers, String.valueOf(i), (double)lv3.getX() + 0.5, (double)lv3.getY() + 0.25, (double)lv3.getZ() + 0.5, j);
        }
    }
}

