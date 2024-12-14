/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render.chunk;

import com.mojang.blaze3d.systems.VertexSorter;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.chunk.ChunkOcclusionData;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SectionBuilder {
    private final BlockRenderManager blockRenderManager;
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    public SectionBuilder(BlockRenderManager blockRenderManager, BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        this.blockRenderManager = blockRenderManager;
        this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
    }

    public RenderData build(ChunkSectionPos sectionPos, ChunkRendererRegion renderRegion, VertexSorter vertexSorter, BlockBufferAllocatorStorage allocatorStorage) {
        RenderData lv = new RenderData();
        BlockPos lv2 = sectionPos.getMinPos();
        BlockPos lv3 = lv2.add(15, 15, 15);
        ChunkOcclusionDataBuilder lv4 = new ChunkOcclusionDataBuilder();
        MatrixStack lv5 = new MatrixStack();
        BlockModelRenderer.enableBrightnessCache();
        Reference2ObjectArrayMap<RenderLayer, BufferBuilder> map = new Reference2ObjectArrayMap<RenderLayer, BufferBuilder>(RenderLayer.getBlockLayers().size());
        Random lv6 = Random.create();
        for (BlockPos blockPos : BlockPos.iterate(lv2, lv3)) {
            BufferBuilder lv12;
            RenderLayer lv11;
            FluidState lv10;
            BlockEntity lv9;
            BlockState lv8 = renderRegion.getBlockState(blockPos);
            if (lv8.isOpaqueFullCube(renderRegion, blockPos)) {
                lv4.markClosed(blockPos);
            }
            if (lv8.hasBlockEntity() && (lv9 = renderRegion.getBlockEntity(blockPos)) != null) {
                this.addBlockEntity(lv, lv9);
            }
            if (!(lv10 = lv8.getFluidState()).isEmpty()) {
                lv11 = RenderLayers.getFluidLayer(lv10);
                lv12 = this.beginBufferBuilding(map, allocatorStorage, lv11);
                this.blockRenderManager.renderFluid(blockPos, renderRegion, lv12, lv8, lv10);
            }
            if (lv8.getRenderType() != BlockRenderType.MODEL) continue;
            lv11 = RenderLayers.getBlockLayer(lv8);
            lv12 = this.beginBufferBuilding(map, allocatorStorage, lv11);
            lv5.push();
            lv5.translate(ChunkSectionPos.getLocalCoord(blockPos.getX()), ChunkSectionPos.getLocalCoord(blockPos.getY()), ChunkSectionPos.getLocalCoord(blockPos.getZ()));
            this.blockRenderManager.renderBlock(lv8, blockPos, renderRegion, lv5, lv12, true, lv6);
            lv5.pop();
        }
        for (Map.Entry entry : map.entrySet()) {
            RenderLayer lv13 = (RenderLayer)entry.getKey();
            BuiltBuffer lv14 = ((BufferBuilder)entry.getValue()).endNullable();
            if (lv14 == null) continue;
            if (lv13 == RenderLayer.getTranslucent()) {
                lv.translucencySortingData = lv14.sortQuads(allocatorStorage.get(RenderLayer.getTranslucent()), vertexSorter);
            }
            lv.buffers.put(lv13, lv14);
        }
        BlockModelRenderer.disableBrightnessCache();
        lv.chunkOcclusionData = lv4.build();
        return lv;
    }

    private BufferBuilder beginBufferBuilding(Map<RenderLayer, BufferBuilder> builders, BlockBufferAllocatorStorage allocatorStorage, RenderLayer layer) {
        BufferBuilder lv = builders.get(layer);
        if (lv == null) {
            BufferAllocator lv2 = allocatorStorage.get(layer);
            lv = new BufferBuilder(lv2, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
            builders.put(layer, lv);
        }
        return lv;
    }

    private <E extends BlockEntity> void addBlockEntity(RenderData data, E blockEntity) {
        BlockEntityRenderer<E> lv = this.blockEntityRenderDispatcher.get(blockEntity);
        if (lv != null) {
            data.blockEntities.add(blockEntity);
            if (lv.rendersOutsideBoundingBox(blockEntity)) {
                data.noCullingBlockEntities.add(blockEntity);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class RenderData {
        public final List<BlockEntity> noCullingBlockEntities = new ArrayList<BlockEntity>();
        public final List<BlockEntity> blockEntities = new ArrayList<BlockEntity>();
        public final Map<RenderLayer, BuiltBuffer> buffers = new Reference2ObjectArrayMap<RenderLayer, BuiltBuffer>();
        public ChunkOcclusionData chunkOcclusionData = new ChunkOcclusionData();
        @Nullable
        public BuiltBuffer.SortState translucencySortingData;

        public void close() {
            this.buffers.values().forEach(BuiltBuffer::close);
        }
    }
}

