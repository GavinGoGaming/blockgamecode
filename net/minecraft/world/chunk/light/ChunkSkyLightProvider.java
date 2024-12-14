/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 *  org.jetbrains.annotations.VisibleForTesting
 */
package net.minecraft.world.chunk.light;

import java.util.Objects;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.world.chunk.light.ChunkSkyLight;
import net.minecraft.world.chunk.light.LightSourceView;
import net.minecraft.world.chunk.light.SkyLightStorage;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public final class ChunkSkyLightProvider
extends ChunkLightProvider<SkyLightStorage.Data, SkyLightStorage> {
    private static final long field_44743 = ChunkLightProvider.class_8531.packWithAllDirectionsSet(15);
    private static final long field_44744 = ChunkLightProvider.class_8531.packWithOneDirectionCleared(15, Direction.UP);
    private static final long field_44745 = ChunkLightProvider.class_8531.method_51574(15, false, Direction.UP);
    private final BlockPos.Mutable field_44746 = new BlockPos.Mutable();
    private final ChunkSkyLight field_44747;

    public ChunkSkyLightProvider(ChunkProvider chunkProvider) {
        this(chunkProvider, new SkyLightStorage(chunkProvider));
    }

    @VisibleForTesting
    protected ChunkSkyLightProvider(ChunkProvider chunkProvider, SkyLightStorage lightStorage) {
        super(chunkProvider, lightStorage);
        this.field_44747 = new ChunkSkyLight(chunkProvider.getWorld());
    }

    private static boolean method_51584(int i) {
        return i == 15;
    }

    private int method_51585(int x, int z, int k) {
        ChunkSkyLight lv = this.method_51589(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z));
        if (lv == null) {
            return k;
        }
        return lv.get(ChunkSectionPos.getLocalCoord(x), ChunkSectionPos.getLocalCoord(z));
    }

    @Nullable
    private ChunkSkyLight method_51589(int chunkX, int chunkZ) {
        LightSourceView lv = this.chunkProvider.getChunk(chunkX, chunkZ);
        return lv != null ? lv.getChunkSkyLight() : null;
    }

    @Override
    protected void method_51529(long l) {
        boolean bl;
        int n;
        int i = BlockPos.unpackLongX(l);
        int j = BlockPos.unpackLongY(l);
        int k = BlockPos.unpackLongZ(l);
        long m = ChunkSectionPos.fromBlockPos(l);
        int n2 = n = ((SkyLightStorage)this.lightStorage).isSectionInEnabledColumn(m) ? this.method_51585(i, k, Integer.MAX_VALUE) : Integer.MAX_VALUE;
        if (n != Integer.MAX_VALUE) {
            this.method_51590(i, k, n);
        }
        if (!((SkyLightStorage)this.lightStorage).hasSection(m)) {
            return;
        }
        boolean bl2 = bl = j >= n;
        if (bl) {
            this.method_51565(l, field_44744);
            this.method_51566(l, field_44745);
        } else {
            int o = ((SkyLightStorage)this.lightStorage).get(l);
            if (o > 0) {
                ((SkyLightStorage)this.lightStorage).set(l, 0);
                this.method_51565(l, ChunkLightProvider.class_8531.packWithAllDirectionsSet(o));
            } else {
                this.method_51565(l, field_44731);
            }
        }
    }

    private void method_51590(int i, int j, int k) {
        int l = ChunkSectionPos.getBlockCoord(((SkyLightStorage)this.lightStorage).getMinSectionY());
        this.method_51586(i, j, k, l);
        this.method_51591(i, j, k, l);
    }

    private void method_51586(int x, int z, int k, int l) {
        if (k <= l) {
            return;
        }
        int m = ChunkSectionPos.getSectionCoord(x);
        int n = ChunkSectionPos.getSectionCoord(z);
        int o = k - 1;
        int p = ChunkSectionPos.getSectionCoord(o);
        while (((SkyLightStorage)this.lightStorage).isAboveMinHeight(p)) {
            if (((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.asLong(m, p, n))) {
                int q = ChunkSectionPos.getBlockCoord(p);
                int r = q + 15;
                for (int s = Math.min(r, o); s >= q; --s) {
                    long t = BlockPos.asLong(x, s, z);
                    if (!ChunkSkyLightProvider.method_51584(((SkyLightStorage)this.lightStorage).get(t))) {
                        return;
                    }
                    ((SkyLightStorage)this.lightStorage).set(t, 0);
                    this.method_51565(t, s == k - 1 ? field_44743 : field_44744);
                }
            }
            --p;
        }
    }

    private void method_51591(int i, int j, int k, int l) {
        int m = ChunkSectionPos.getSectionCoord(i);
        int n = ChunkSectionPos.getSectionCoord(j);
        int o = Math.max(Math.max(this.method_51585(i - 1, j, Integer.MIN_VALUE), this.method_51585(i + 1, j, Integer.MIN_VALUE)), Math.max(this.method_51585(i, j - 1, Integer.MIN_VALUE), this.method_51585(i, j + 1, Integer.MIN_VALUE)));
        int p = Math.max(k, l);
        long q = ChunkSectionPos.asLong(m, ChunkSectionPos.getSectionCoord(p), n);
        while (!((SkyLightStorage)this.lightStorage).isAtOrAboveTopmostSection(q)) {
            if (((SkyLightStorage)this.lightStorage).hasSection(q)) {
                int r = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(q));
                int s = r + 15;
                for (int t = Math.max(r, p); t <= s; ++t) {
                    long u = BlockPos.asLong(i, t, j);
                    if (ChunkSkyLightProvider.method_51584(((SkyLightStorage)this.lightStorage).get(u))) {
                        return;
                    }
                    ((SkyLightStorage)this.lightStorage).set(u, 15);
                    if (t >= o && t != k) continue;
                    this.method_51566(u, field_44745);
                }
            }
            q = ChunkSectionPos.offset(q, Direction.UP);
        }
    }

    @Override
    protected void method_51531(long l, long m, int i) {
        BlockState lv = null;
        int j = this.getNumberOfSectionsBelowPos(l);
        for (Direction lv2 : DIRECTIONS) {
            int k;
            int o;
            long n;
            if (!ChunkLightProvider.class_8531.isDirectionBitSet(m, lv2) || !((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.fromBlockPos(n = BlockPos.offset(l, lv2))) || (o = i - 1) <= (k = ((SkyLightStorage)this.lightStorage).get(n))) continue;
            this.field_44746.set(n);
            BlockState lv3 = this.getStateForLighting(this.field_44746);
            int p = i - this.getOpacity(lv3, this.field_44746);
            if (p <= k) continue;
            if (lv == null) {
                BlockState blockState = lv = ChunkLightProvider.class_8531.isTrivial(m) ? Blocks.AIR.getDefaultState() : this.getStateForLighting(this.field_44746.set(l));
            }
            if (this.shapesCoverFullCube(l, lv, n, lv3, lv2)) continue;
            ((SkyLightStorage)this.lightStorage).set(n, p);
            if (p > 1) {
                this.method_51566(n, ChunkLightProvider.class_8531.method_51574(p, ChunkSkyLightProvider.isTrivialForLighting(lv3), lv2.getOpposite()));
            }
            this.method_51587(n, lv2, p, true, j);
        }
    }

    @Override
    protected void method_51530(long l, long m) {
        int i = this.getNumberOfSectionsBelowPos(l);
        int j = ChunkLightProvider.class_8531.getLightLevel(m);
        for (Direction lv : DIRECTIONS) {
            int k;
            long n;
            if (!ChunkLightProvider.class_8531.isDirectionBitSet(m, lv) || !((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.fromBlockPos(n = BlockPos.offset(l, lv))) || (k = ((SkyLightStorage)this.lightStorage).get(n)) == 0) continue;
            if (k <= j - 1) {
                ((SkyLightStorage)this.lightStorage).set(n, 0);
                this.method_51565(n, ChunkLightProvider.class_8531.packWithOneDirectionCleared(k, lv.getOpposite()));
                this.method_51587(n, lv, k, false, i);
                continue;
            }
            this.method_51566(n, ChunkLightProvider.class_8531.method_51579(k, false, lv.getOpposite()));
        }
    }

    private int getNumberOfSectionsBelowPos(long blockPos) {
        int i = BlockPos.unpackLongY(blockPos);
        int j = ChunkSectionPos.getLocalCoord(i);
        if (j != 0) {
            return 0;
        }
        int k = BlockPos.unpackLongX(blockPos);
        int m = BlockPos.unpackLongZ(blockPos);
        int n = ChunkSectionPos.getLocalCoord(k);
        int o = ChunkSectionPos.getLocalCoord(m);
        if (n == 0 || n == 15 || o == 0 || o == 15) {
            int p = ChunkSectionPos.getSectionCoord(k);
            int q = ChunkSectionPos.getSectionCoord(i);
            int r = ChunkSectionPos.getSectionCoord(m);
            int s = 0;
            while (!((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.asLong(p, q - s - 1, r)) && ((SkyLightStorage)this.lightStorage).isAboveMinHeight(q - s - 1)) {
                ++s;
            }
            return s;
        }
        return 0;
    }

    private void method_51587(long blockPos, Direction direction, int lightLevel, boolean bl, int j) {
        if (j == 0) {
            return;
        }
        int k = BlockPos.unpackLongX(blockPos);
        int m = BlockPos.unpackLongZ(blockPos);
        if (!ChunkSkyLightProvider.exitsChunkXZ(direction, ChunkSectionPos.getLocalCoord(k), ChunkSectionPos.getLocalCoord(m))) {
            return;
        }
        int n = BlockPos.unpackLongY(blockPos);
        int o = ChunkSectionPos.getSectionCoord(k);
        int p = ChunkSectionPos.getSectionCoord(m);
        int q = ChunkSectionPos.getSectionCoord(n) - 1;
        int r = q - j + 1;
        while (q >= r) {
            if (!((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.asLong(o, q, p))) {
                --q;
                continue;
            }
            int s = ChunkSectionPos.getBlockCoord(q);
            for (int t = 15; t >= 0; --t) {
                long u = BlockPos.asLong(k, s + t, m);
                if (bl) {
                    ((SkyLightStorage)this.lightStorage).set(u, lightLevel);
                    if (lightLevel <= 1) continue;
                    this.method_51566(u, ChunkLightProvider.class_8531.method_51574(lightLevel, true, direction.getOpposite()));
                    continue;
                }
                ((SkyLightStorage)this.lightStorage).set(u, 0);
                this.method_51565(u, ChunkLightProvider.class_8531.packWithOneDirectionCleared(lightLevel, direction.getOpposite()));
            }
            --q;
        }
    }

    private static boolean exitsChunkXZ(Direction direction, int localX, int localZ) {
        return switch (direction) {
            case Direction.NORTH -> {
                if (localZ == 15) {
                    yield true;
                }
                yield false;
            }
            case Direction.SOUTH -> {
                if (localZ == 0) {
                    yield true;
                }
                yield false;
            }
            case Direction.WEST -> {
                if (localX == 15) {
                    yield true;
                }
                yield false;
            }
            case Direction.EAST -> {
                if (localX == 0) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    @Override
    public void setColumnEnabled(ChunkPos pos, boolean retainData) {
        super.setColumnEnabled(pos, retainData);
        if (retainData) {
            ChunkSkyLight lv = Objects.requireNonNullElse(this.method_51589(pos.x, pos.z), this.field_44747);
            int i = lv.getMaxSurfaceY() - 1;
            int j = ChunkSectionPos.getSectionCoord(i) + 1;
            long l = ChunkSectionPos.withZeroY(pos.x, pos.z);
            int k = ((SkyLightStorage)this.lightStorage).getTopSectionForColumn(l);
            int m = Math.max(((SkyLightStorage)this.lightStorage).getMinSectionY(), j);
            for (int n = k - 1; n >= m; --n) {
                ChunkNibbleArray lv2 = ((SkyLightStorage)this.lightStorage).method_51547(ChunkSectionPos.asLong(pos.x, n, pos.z));
                if (lv2 == null || !lv2.isUninitialized()) continue;
                lv2.clear(15);
            }
        }
    }

    @Override
    public void propagateLight(ChunkPos chunkPos) {
        long l = ChunkSectionPos.withZeroY(chunkPos.x, chunkPos.z);
        ((SkyLightStorage)this.lightStorage).setColumnEnabled(l, true);
        ChunkSkyLight lv = Objects.requireNonNullElse(this.method_51589(chunkPos.x, chunkPos.z), this.field_44747);
        ChunkSkyLight lv2 = Objects.requireNonNullElse(this.method_51589(chunkPos.x, chunkPos.z - 1), this.field_44747);
        ChunkSkyLight lv3 = Objects.requireNonNullElse(this.method_51589(chunkPos.x, chunkPos.z + 1), this.field_44747);
        ChunkSkyLight lv4 = Objects.requireNonNullElse(this.method_51589(chunkPos.x - 1, chunkPos.z), this.field_44747);
        ChunkSkyLight lv5 = Objects.requireNonNullElse(this.method_51589(chunkPos.x + 1, chunkPos.z), this.field_44747);
        int i = ((SkyLightStorage)this.lightStorage).getTopSectionForColumn(l);
        int j = ((SkyLightStorage)this.lightStorage).getMinSectionY();
        int k = ChunkSectionPos.getBlockCoord(chunkPos.x);
        int m = ChunkSectionPos.getBlockCoord(chunkPos.z);
        for (int n = i - 1; n >= j; --n) {
            long o = ChunkSectionPos.asLong(chunkPos.x, n, chunkPos.z);
            ChunkNibbleArray lv6 = ((SkyLightStorage)this.lightStorage).method_51547(o);
            if (lv6 == null) continue;
            int p = ChunkSectionPos.getBlockCoord(n);
            int q = p + 15;
            boolean bl = false;
            for (int r = 0; r < 16; ++r) {
                for (int s = 0; s < 16; ++s) {
                    int t = lv.get(s, r);
                    if (t > q) continue;
                    int u = r == 0 ? lv2.get(s, 15) : lv.get(s, r - 1);
                    int v = r == 15 ? lv3.get(s, 0) : lv.get(s, r + 1);
                    int w = s == 0 ? lv4.get(15, r) : lv.get(s - 1, r);
                    int x = s == 15 ? lv5.get(0, r) : lv.get(s + 1, r);
                    int y = Math.max(Math.max(u, v), Math.max(w, x));
                    for (int z = q; z >= Math.max(p, t); --z) {
                        lv6.set(s, ChunkSectionPos.getLocalCoord(z), r, 15);
                        if (z != t && z >= y) continue;
                        long aa = BlockPos.asLong(k + s, z, m + r);
                        this.method_51566(aa, ChunkLightProvider.class_8531.method_51578(z == t, z < u, z < v, z < w, z < x));
                    }
                    if (t >= p) continue;
                    bl = true;
                }
            }
            if (!bl) break;
        }
    }
}

