/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk.light;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.light.BlockLightStorage;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.world.chunk.light.LightSourceView;

public final class ChunkBlockLightProvider
extends ChunkLightProvider<BlockLightStorage.Data, BlockLightStorage> {
    private final BlockPos.Mutable mutablePos = new BlockPos.Mutable();

    public ChunkBlockLightProvider(ChunkProvider chunkProvider) {
        this(chunkProvider, new BlockLightStorage(chunkProvider));
    }

    @VisibleForTesting
    public ChunkBlockLightProvider(ChunkProvider chunkProvider, BlockLightStorage blockLightStorage) {
        super(chunkProvider, blockLightStorage);
    }

    @Override
    protected void method_51529(long l) {
        int j;
        long m = ChunkSectionPos.fromBlockPos(l);
        if (!((BlockLightStorage)this.lightStorage).hasSection(m)) {
            return;
        }
        BlockState lv = this.getStateForLighting(this.mutablePos.set(l));
        int i = this.getLightSourceLuminance(l, lv);
        if (i < (j = ((BlockLightStorage)this.lightStorage).get(l))) {
            ((BlockLightStorage)this.lightStorage).set(l, 0);
            this.method_51565(l, ChunkLightProvider.class_8531.packWithAllDirectionsSet(j));
        } else {
            this.method_51565(l, field_44731);
        }
        if (i > 0) {
            this.method_51566(l, ChunkLightProvider.class_8531.method_51573(i, ChunkBlockLightProvider.isTrivialForLighting(lv)));
        }
    }

    @Override
    protected void method_51531(long l, long m, int i) {
        BlockState lv = null;
        for (Direction lv2 : DIRECTIONS) {
            int j;
            int k;
            long n;
            if (!ChunkLightProvider.class_8531.isDirectionBitSet(m, lv2) || !((BlockLightStorage)this.lightStorage).hasSection(ChunkSectionPos.fromBlockPos(n = BlockPos.offset(l, lv2))) || (k = i - 1) <= (j = ((BlockLightStorage)this.lightStorage).get(n))) continue;
            this.mutablePos.set(n);
            BlockState lv3 = this.getStateForLighting(this.mutablePos);
            int o = i - this.getOpacity(lv3, this.mutablePos);
            if (o <= j) continue;
            if (lv == null) {
                BlockState blockState = lv = ChunkLightProvider.class_8531.isTrivial(m) ? Blocks.AIR.getDefaultState() : this.getStateForLighting(this.mutablePos.set(l));
            }
            if (this.shapesCoverFullCube(l, lv, n, lv3, lv2)) continue;
            ((BlockLightStorage)this.lightStorage).set(n, o);
            if (o <= 1) continue;
            this.method_51566(n, ChunkLightProvider.class_8531.method_51574(o, ChunkBlockLightProvider.isTrivialForLighting(lv3), lv2.getOpposite()));
        }
    }

    @Override
    protected void method_51530(long l, long m) {
        int i = ChunkLightProvider.class_8531.getLightLevel(m);
        for (Direction lv : DIRECTIONS) {
            int j;
            long n;
            if (!ChunkLightProvider.class_8531.isDirectionBitSet(m, lv) || !((BlockLightStorage)this.lightStorage).hasSection(ChunkSectionPos.fromBlockPos(n = BlockPos.offset(l, lv))) || (j = ((BlockLightStorage)this.lightStorage).get(n)) == 0) continue;
            if (j <= i - 1) {
                BlockState lv2 = this.getStateForLighting(this.mutablePos.set(n));
                int k = this.getLightSourceLuminance(n, lv2);
                ((BlockLightStorage)this.lightStorage).set(n, 0);
                if (k < j) {
                    this.method_51565(n, ChunkLightProvider.class_8531.packWithOneDirectionCleared(j, lv.getOpposite()));
                }
                if (k <= 0) continue;
                this.method_51566(n, ChunkLightProvider.class_8531.method_51573(k, ChunkBlockLightProvider.isTrivialForLighting(lv2)));
                continue;
            }
            this.method_51566(n, ChunkLightProvider.class_8531.method_51579(j, false, lv.getOpposite()));
        }
    }

    private int getLightSourceLuminance(long blockPos, BlockState blockState) {
        int i = blockState.getLuminance();
        if (i > 0 && ((BlockLightStorage)this.lightStorage).isSectionInEnabledColumn(ChunkSectionPos.fromBlockPos(blockPos))) {
            return i;
        }
        return 0;
    }

    @Override
    public void propagateLight(ChunkPos chunkPos) {
        this.setColumnEnabled(chunkPos, true);
        LightSourceView lv = this.chunkProvider.getChunk(chunkPos.x, chunkPos.z);
        if (lv != null) {
            lv.forEachLightSource((blockPos, blockState) -> {
                int i = blockState.getLuminance();
                this.method_51566(blockPos.asLong(), ChunkLightProvider.class_8531.method_51573(i, ChunkBlockLightProvider.isTrivialForLighting(blockState)));
            });
        }
    }
}

