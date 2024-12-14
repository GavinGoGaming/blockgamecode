/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world;

import com.google.common.collect.AbstractIterator;
import java.util.function.BiFunction;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.CuboidBlockIterator;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import org.jetbrains.annotations.Nullable;

public class BlockCollisionSpliterator<T>
extends AbstractIterator<T> {
    private final Box box;
    private final ShapeContext context;
    private final CuboidBlockIterator blockIterator;
    private final BlockPos.Mutable pos;
    private final VoxelShape boxShape;
    private final CollisionView world;
    private final boolean forEntity;
    @Nullable
    private BlockView chunk;
    private long chunkPos;
    private final BiFunction<BlockPos.Mutable, VoxelShape, T> resultFunction;

    public BlockCollisionSpliterator(CollisionView world, @Nullable Entity entity, Box box, boolean forEntity, BiFunction<BlockPos.Mutable, VoxelShape, T> resultFunction) {
        this.context = entity == null ? ShapeContext.absent() : ShapeContext.of(entity);
        this.pos = new BlockPos.Mutable();
        this.boxShape = VoxelShapes.cuboid(box);
        this.world = world;
        this.box = box;
        this.forEntity = forEntity;
        this.resultFunction = resultFunction;
        int i = MathHelper.floor(box.minX - 1.0E-7) - 1;
        int j = MathHelper.floor(box.maxX + 1.0E-7) + 1;
        int k = MathHelper.floor(box.minY - 1.0E-7) - 1;
        int l = MathHelper.floor(box.maxY + 1.0E-7) + 1;
        int m = MathHelper.floor(box.minZ - 1.0E-7) - 1;
        int n = MathHelper.floor(box.maxZ + 1.0E-7) + 1;
        this.blockIterator = new CuboidBlockIterator(i, k, m, j, l, n);
    }

    @Nullable
    private BlockView getChunk(int x, int z) {
        BlockView lv;
        int k = ChunkSectionPos.getSectionCoord(x);
        int l = ChunkSectionPos.getSectionCoord(z);
        long m = ChunkPos.toLong(k, l);
        if (this.chunk != null && this.chunkPos == m) {
            return this.chunk;
        }
        this.chunk = lv = this.world.getChunkAsView(k, l);
        this.chunkPos = m;
        return lv;
    }

    @Override
    protected T computeNext() {
        while (this.blockIterator.step()) {
            BlockView lv;
            int i = this.blockIterator.getX();
            int j = this.blockIterator.getY();
            int k = this.blockIterator.getZ();
            int l = this.blockIterator.getEdgeCoordinatesCount();
            if (l == 3 || (lv = this.getChunk(i, k)) == null) continue;
            this.pos.set(i, j, k);
            BlockState lv2 = lv.getBlockState(this.pos);
            if (this.forEntity && !lv2.shouldSuffocate(lv, this.pos) || l == 1 && !lv2.exceedsCube() || l == 2 && !lv2.isOf(Blocks.MOVING_PISTON)) continue;
            VoxelShape lv3 = lv2.getCollisionShape(this.world, this.pos, this.context);
            if (lv3 == VoxelShapes.fullCube()) {
                if (!this.box.intersects(i, j, k, (double)i + 1.0, (double)j + 1.0, (double)k + 1.0)) continue;
                return this.resultFunction.apply(this.pos, lv3.offset(i, j, k));
            }
            VoxelShape lv4 = lv3.offset(i, j, k);
            if (lv4.isEmpty() || !VoxelShapes.matchesAnywhere(lv4, this.boxShape, BooleanBiFunction.AND)) continue;
            return this.resultFunction.apply(this.pos, lv4);
        }
        return this.endOfData();
    }
}

