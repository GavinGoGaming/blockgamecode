/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.structure.JigsawJunction;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.StructureTerrainAdaptation;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

public class StructureWeightSampler
implements DensityFunctionTypes.Beardifying {
    public static final int INDEX_OFFSET = 12;
    private static final int EDGE_LENGTH = 24;
    private static final float[] STRUCTURE_WEIGHT_TABLE = Util.make(new float[13824], array -> {
        for (int i = 0; i < 24; ++i) {
            for (int j = 0; j < 24; ++j) {
                for (int k = 0; k < 24; ++k) {
                    array[i * 24 * 24 + j * 24 + k] = (float)StructureWeightSampler.calculateStructureWeight(j - 12, k - 12, i - 12);
                }
            }
        }
    });
    private final ObjectListIterator<Piece> pieceIterator;
    private final ObjectListIterator<JigsawJunction> junctionIterator;

    public static StructureWeightSampler createStructureWeightSampler(StructureAccessor world, ChunkPos pos) {
        int i = pos.getStartX();
        int j = pos.getStartZ();
        ObjectArrayList objectList = new ObjectArrayList(10);
        ObjectArrayList objectList2 = new ObjectArrayList(32);
        world.getStructureStarts(pos, structure -> structure.getTerrainAdaptation() != StructureTerrainAdaptation.NONE).forEach(start -> {
            StructureTerrainAdaptation lv = start.getStructure().getTerrainAdaptation();
            for (StructurePiece lv2 : start.getChildren()) {
                if (!lv2.intersectsChunk(pos, 12)) continue;
                if (lv2 instanceof PoolStructurePiece) {
                    PoolStructurePiece lv3 = (PoolStructurePiece)lv2;
                    StructurePool.Projection lv4 = lv3.getPoolElement().getProjection();
                    if (lv4 == StructurePool.Projection.RIGID) {
                        objectList.add(new Piece(lv3.getBoundingBox(), lv, lv3.getGroundLevelDelta()));
                    }
                    for (JigsawJunction lv5 : lv3.getJunctions()) {
                        int k = lv5.getSourceX();
                        int l = lv5.getSourceZ();
                        if (k <= i - 12 || l <= j - 12 || k >= i + 15 + 12 || l >= j + 15 + 12) continue;
                        objectList2.add(lv5);
                    }
                    continue;
                }
                objectList.add(new Piece(lv2.getBoundingBox(), lv, 0));
            }
        });
        return new StructureWeightSampler((ObjectListIterator<Piece>)objectList.iterator(), (ObjectListIterator<JigsawJunction>)objectList2.iterator());
    }

    @VisibleForTesting
    public StructureWeightSampler(ObjectListIterator<Piece> pieceIterator, ObjectListIterator<JigsawJunction> junctionIterator) {
        this.pieceIterator = pieceIterator;
        this.junctionIterator = junctionIterator;
    }

    @Override
    public double sample(DensityFunction.NoisePos pos) {
        int m;
        int l;
        int i = pos.blockX();
        int j = pos.blockY();
        int k = pos.blockZ();
        double d = 0.0;
        while (this.pieceIterator.hasNext()) {
            Piece lv = (Piece)this.pieceIterator.next();
            BlockBox lv2 = lv.box();
            l = lv.groundLevelDelta();
            m = Math.max(0, Math.max(lv2.getMinX() - i, i - lv2.getMaxX()));
            int n = Math.max(0, Math.max(lv2.getMinZ() - k, k - lv2.getMaxZ()));
            int o = lv2.getMinY() + l;
            int p = j - o;
            int q = switch (lv.terrainAdjustment()) {
                default -> throw new MatchException(null, null);
                case StructureTerrainAdaptation.NONE -> 0;
                case StructureTerrainAdaptation.BURY, StructureTerrainAdaptation.BEARD_THIN -> p;
                case StructureTerrainAdaptation.BEARD_BOX -> Math.max(0, Math.max(o - j, j - lv2.getMaxY()));
                case StructureTerrainAdaptation.ENCAPSULATE -> Math.max(0, Math.max(lv2.getMinY() - j, j - lv2.getMaxY()));
            };
            d += (switch (lv.terrainAdjustment()) {
                default -> throw new MatchException(null, null);
                case StructureTerrainAdaptation.NONE -> 0.0;
                case StructureTerrainAdaptation.BURY -> StructureWeightSampler.getMagnitudeWeight(m, (double)q / 2.0, n);
                case StructureTerrainAdaptation.BEARD_THIN, StructureTerrainAdaptation.BEARD_BOX -> StructureWeightSampler.getStructureWeight(m, q, n, p) * 0.8;
                case StructureTerrainAdaptation.ENCAPSULATE -> StructureWeightSampler.getMagnitudeWeight((double)m / 2.0, (double)q / 2.0, (double)n / 2.0) * 0.8;
            });
        }
        this.pieceIterator.back(Integer.MAX_VALUE);
        while (this.junctionIterator.hasNext()) {
            JigsawJunction lv3 = (JigsawJunction)this.junctionIterator.next();
            int r = i - lv3.getSourceX();
            l = j - lv3.getSourceGroundY();
            m = k - lv3.getSourceZ();
            d += StructureWeightSampler.getStructureWeight(r, l, m, l) * 0.4;
        }
        this.junctionIterator.back(Integer.MAX_VALUE);
        return d;
    }

    @Override
    public double minValue() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double maxValue() {
        return Double.POSITIVE_INFINITY;
    }

    private static double getMagnitudeWeight(double x, double y, double z) {
        double g = MathHelper.magnitude(x, y, z);
        return MathHelper.clampedMap(g, 0.0, 6.0, 1.0, 0.0);
    }

    private static double getStructureWeight(int x, int y, int z, int yy) {
        int m = x + 12;
        int n = y + 12;
        int o = z + 12;
        if (!(StructureWeightSampler.indexInBounds(m) && StructureWeightSampler.indexInBounds(n) && StructureWeightSampler.indexInBounds(o))) {
            return 0.0;
        }
        double d = (double)yy + 0.5;
        double e = MathHelper.squaredMagnitude(x, d, z);
        double f = -d * MathHelper.fastInverseSqrt(e / 2.0) / 2.0;
        return f * (double)STRUCTURE_WEIGHT_TABLE[o * 24 * 24 + m * 24 + n];
    }

    private static boolean indexInBounds(int i) {
        return i >= 0 && i < 24;
    }

    private static double calculateStructureWeight(int x, int y, int z) {
        return StructureWeightSampler.structureWeight(x, (double)y + 0.5, z);
    }

    private static double structureWeight(int x, double y, int z) {
        double e = MathHelper.squaredMagnitude(x, y, z);
        double f = Math.pow(Math.E, -e / 16.0);
        return f;
    }

    @VisibleForTesting
    public record Piece(BlockBox box, StructureTerrainAdaptation terrainAdjustment, int groundLevelDelta) {
    }
}

