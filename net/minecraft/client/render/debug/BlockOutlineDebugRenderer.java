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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class BlockOutlineDebugRenderer
implements DebugRenderer.Renderer {
    private final MinecraftClient client;

    public BlockOutlineDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        World lv = this.client.player.getWorld();
        BlockPos lv2 = BlockPos.ofFloored(cameraX, cameraY, cameraZ);
        for (BlockPos lv3 : BlockPos.iterate(lv2.add(-6, -6, -6), lv2.add(6, 6, 6))) {
            BlockState lv4 = lv.getBlockState(lv3);
            if (lv4.isOf(Blocks.AIR)) continue;
            VoxelShape lv5 = lv4.getOutlineShape(lv, lv3);
            for (Box lv6 : lv5.getBoundingBoxes()) {
                VertexConsumer lv8;
                Box lv7 = lv6.offset(lv3).expand(0.002);
                float g = (float)(lv7.minX - cameraX);
                float h = (float)(lv7.minY - cameraY);
                float i = (float)(lv7.minZ - cameraZ);
                float j = (float)(lv7.maxX - cameraX);
                float k = (float)(lv7.maxY - cameraY);
                float l = (float)(lv7.maxZ - cameraZ);
                int m = -2130771968;
                if (lv4.isSideSolidFullSquare(lv, lv3, Direction.WEST)) {
                    lv8 = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
                    lv8.vertex(matrix4f, g, h, i).color(-2130771968);
                    lv8.vertex(matrix4f, g, h, l).color(-2130771968);
                    lv8.vertex(matrix4f, g, k, i).color(-2130771968);
                    lv8.vertex(matrix4f, g, k, l).color(-2130771968);
                }
                if (lv4.isSideSolidFullSquare(lv, lv3, Direction.SOUTH)) {
                    lv8 = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
                    lv8.vertex(matrix4f, g, k, l).color(-2130771968);
                    lv8.vertex(matrix4f, g, h, l).color(-2130771968);
                    lv8.vertex(matrix4f, j, k, l).color(-2130771968);
                    lv8.vertex(matrix4f, j, h, l).color(-2130771968);
                }
                if (lv4.isSideSolidFullSquare(lv, lv3, Direction.EAST)) {
                    lv8 = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
                    lv8.vertex(matrix4f, j, h, l).color(-2130771968);
                    lv8.vertex(matrix4f, j, h, i).color(-2130771968);
                    lv8.vertex(matrix4f, j, k, l).color(-2130771968);
                    lv8.vertex(matrix4f, j, k, i).color(-2130771968);
                }
                if (lv4.isSideSolidFullSquare(lv, lv3, Direction.NORTH)) {
                    lv8 = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
                    lv8.vertex(matrix4f, j, k, i).color(-2130771968);
                    lv8.vertex(matrix4f, j, h, i).color(-2130771968);
                    lv8.vertex(matrix4f, g, k, i).color(-2130771968);
                    lv8.vertex(matrix4f, g, h, i).color(-2130771968);
                }
                if (lv4.isSideSolidFullSquare(lv, lv3, Direction.DOWN)) {
                    lv8 = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
                    lv8.vertex(matrix4f, g, h, i).color(-2130771968);
                    lv8.vertex(matrix4f, j, h, i).color(-2130771968);
                    lv8.vertex(matrix4f, g, h, l).color(-2130771968);
                    lv8.vertex(matrix4f, j, h, l).color(-2130771968);
                }
                if (!lv4.isSideSolidFullSquare(lv, lv3, Direction.UP)) continue;
                lv8 = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
                lv8.vertex(matrix4f, g, k, i).color(-2130771968);
                lv8.vertex(matrix4f, g, k, l).color(-2130771968);
                lv8.vertex(matrix4f, j, k, i).color(-2130771968);
                lv8.vertex(matrix4f, j, k, l).color(-2130771968);
            }
        }
    }
}

