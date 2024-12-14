/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render.chunk;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
class RenderedChunk {
    private final Map<BlockPos, BlockEntity> blockEntities;
    @Nullable
    private final List<PalettedContainer<BlockState>> blockStateContainers;
    private final boolean debugWorld;
    private final WorldChunk chunk;

    RenderedChunk(WorldChunk chunk) {
        this.chunk = chunk;
        this.debugWorld = chunk.getWorld().isDebugWorld();
        this.blockEntities = ImmutableMap.copyOf(chunk.getBlockEntities());
        if (chunk instanceof EmptyChunk) {
            this.blockStateContainers = null;
        } else {
            ChunkSection[] lvs = chunk.getSectionArray();
            this.blockStateContainers = new ArrayList<PalettedContainer<BlockState>>(lvs.length);
            for (ChunkSection lv : lvs) {
                this.blockStateContainers.add(lv.isEmpty() ? null : lv.getBlockStateContainer().copy());
            }
        }
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        return this.blockEntities.get(pos);
    }

    public BlockState getBlockState(BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        if (this.debugWorld) {
            BlockState lv = null;
            if (j == 60) {
                lv = Blocks.BARRIER.getDefaultState();
            }
            if (j == 70) {
                lv = DebugChunkGenerator.getBlockState(i, k);
            }
            return lv == null ? Blocks.AIR.getDefaultState() : lv;
        }
        if (this.blockStateContainers == null) {
            return Blocks.AIR.getDefaultState();
        }
        try {
            PalettedContainer<BlockState> lv2;
            int l = this.chunk.getSectionIndex(j);
            if (l >= 0 && l < this.blockStateContainers.size() && (lv2 = this.blockStateContainers.get(l)) != null) {
                return lv2.get(i & 0xF, j & 0xF, k & 0xF);
            }
            return Blocks.AIR.getDefaultState();
        } catch (Throwable throwable) {
            CrashReport lv3 = CrashReport.create(throwable, "Getting block state");
            CrashReportSection lv4 = lv3.addElement("Block being got");
            lv4.add("Location", () -> CrashReportSection.createPositionString((HeightLimitView)this.chunk, i, j, k));
            throw new CrashException(lv3);
        }
    }
}

