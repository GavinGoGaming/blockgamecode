/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.SingleStateFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class IcebergFeature
extends Feature<SingleStateFeatureConfig> {
    public IcebergFeature(Codec<SingleStateFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<SingleStateFeatureConfig> context) {
        boolean bl3;
        int s;
        int r;
        int q;
        int p;
        int l;
        BlockPos lv = context.getOrigin();
        StructureWorldAccess lv2 = context.getWorld();
        lv = new BlockPos(lv.getX(), context.getGenerator().getSeaLevel(), lv.getZ());
        Random lv3 = context.getRandom();
        boolean bl = lv3.nextDouble() > 0.7;
        BlockState lv4 = context.getConfig().state;
        double d = lv3.nextDouble() * 2.0 * Math.PI;
        int i = 11 - lv3.nextInt(5);
        int j = 3 + lv3.nextInt(3);
        boolean bl2 = lv3.nextDouble() > 0.7;
        int k = 11;
        int n = l = bl2 ? lv3.nextInt(6) + 6 : lv3.nextInt(15) + 3;
        if (!bl2 && lv3.nextDouble() > 0.9) {
            l += lv3.nextInt(19) + 7;
        }
        int m = Math.min(l + lv3.nextInt(11), 18);
        int n2 = Math.min(l + lv3.nextInt(7) - lv3.nextInt(5), 11);
        int o = bl2 ? i : 11;
        for (p = -o; p < o; ++p) {
            for (q = -o; q < o; ++q) {
                for (r = 0; r < l; ++r) {
                    int n3 = s = bl2 ? this.method_13417(r, l, n2) : this.method_13419(lv3, r, l, n2);
                    if (!bl2 && p >= s) continue;
                    this.placeAt(lv2, lv3, lv, l, p, r, q, s, o, bl2, j, d, bl, lv4);
                }
            }
        }
        this.method_13418(lv2, lv, n2, l, bl2, i);
        for (p = -o; p < o; ++p) {
            for (q = -o; q < o; ++q) {
                for (r = -1; r > -m; --r) {
                    s = bl2 ? MathHelper.ceil((float)o * (1.0f - (float)Math.pow(r, 2.0) / ((float)m * 8.0f))) : o;
                    int t = this.method_13427(lv3, -r, m, n2);
                    if (p >= t) continue;
                    this.placeAt(lv2, lv3, lv, m, p, r, q, t, s, bl2, j, d, bl, lv4);
                }
            }
        }
        boolean bl4 = bl2 ? lv3.nextDouble() > 0.1 : (bl3 = lv3.nextDouble() > 0.7);
        if (bl3) {
            this.method_13428(lv3, lv2, n2, l, lv, bl2, i, d, j);
        }
        return true;
    }

    private void method_13428(Random random, WorldAccess world, int i, int j, BlockPos pos, boolean bl, int k, double d, int l) {
        int r;
        int q;
        int m = random.nextBoolean() ? -1 : 1;
        int n = random.nextBoolean() ? -1 : 1;
        int o = random.nextInt(Math.max(i / 2 - 2, 1));
        if (random.nextBoolean()) {
            o = i / 2 + 1 - random.nextInt(Math.max(i - i / 2 - 1, 1));
        }
        int p = random.nextInt(Math.max(i / 2 - 2, 1));
        if (random.nextBoolean()) {
            p = i / 2 + 1 - random.nextInt(Math.max(i - i / 2 - 1, 1));
        }
        if (bl) {
            o = p = random.nextInt(Math.max(k - 5, 1));
        }
        BlockPos lv = new BlockPos(m * o, 0, n * p);
        double e = bl ? d + 1.5707963267948966 : random.nextDouble() * 2.0 * Math.PI;
        for (q = 0; q < j - 3; ++q) {
            r = this.method_13419(random, q, j, i);
            this.method_13415(r, q, pos, world, false, e, lv, k, l);
        }
        for (q = -1; q > -j + random.nextInt(5); --q) {
            r = this.method_13427(random, -q, j, i);
            this.method_13415(r, q, pos, world, true, e, lv, k, l);
        }
    }

    private void method_13415(int i, int y, BlockPos pos, WorldAccess world, boolean placeWater, double d, BlockPos arg3, int k, int l) {
        int m = i + 1 + k / 3;
        int n = Math.min(i - 3, 3) + l / 2 - 1;
        for (int o = -m; o < m; ++o) {
            for (int p = -m; p < m; ++p) {
                BlockPos lv;
                BlockState lv2;
                double e = this.getDistance(o, p, arg3, m, n, d);
                if (!(e < 0.0) || !IcebergFeature.isSnowOrIce(lv2 = world.getBlockState(lv = pos.add(o, y, p))) && !lv2.isOf(Blocks.SNOW_BLOCK)) continue;
                if (placeWater) {
                    this.setBlockState(world, lv, Blocks.WATER.getDefaultState());
                    continue;
                }
                this.setBlockState(world, lv, Blocks.AIR.getDefaultState());
                this.clearSnowAbove(world, lv);
            }
        }
    }

    private void clearSnowAbove(WorldAccess world, BlockPos pos) {
        if (world.getBlockState(pos.up()).isOf(Blocks.SNOW)) {
            this.setBlockState(world, pos.up(), Blocks.AIR.getDefaultState());
        }
    }

    private void placeAt(WorldAccess world, Random random, BlockPos pos, int height, int offsetX, int offsetY, int offsetZ, int m, int n, boolean bl, int o, double randomSine, boolean placeSnow, BlockState state) {
        double e;
        double d = e = bl ? this.getDistance(offsetX, offsetZ, BlockPos.ORIGIN, n, this.decreaseValueNearTop(offsetY, height, o), randomSine) : this.method_13421(offsetX, offsetZ, BlockPos.ORIGIN, m, random);
        if (e < 0.0) {
            double f;
            BlockPos lv = pos.add(offsetX, offsetY, offsetZ);
            double d2 = f = bl ? -0.5 : (double)(-6 - random.nextInt(3));
            if (e > f && random.nextDouble() > 0.9) {
                return;
            }
            this.placeBlockOrSnow(lv, world, random, height - offsetY, height, bl, placeSnow, state);
        }
    }

    private void placeBlockOrSnow(BlockPos pos, WorldAccess world, Random random, int heightRemaining, int height, boolean lessSnow, boolean placeSnow, BlockState state) {
        BlockState lv = world.getBlockState(pos);
        if (lv.isAir() || lv.isOf(Blocks.SNOW_BLOCK) || lv.isOf(Blocks.ICE) || lv.isOf(Blocks.WATER)) {
            int k;
            boolean bl3 = !lessSnow || random.nextDouble() > 0.05;
            int n = k = lessSnow ? 3 : 2;
            if (placeSnow && !lv.isOf(Blocks.WATER) && (double)heightRemaining <= (double)random.nextInt(Math.max(1, height / k)) + (double)height * 0.6 && bl3) {
                this.setBlockState(world, pos, Blocks.SNOW_BLOCK.getDefaultState());
            } else {
                this.setBlockState(world, pos, state);
            }
        }
    }

    private int decreaseValueNearTop(int y, int height, int value) {
        int l = value;
        if (y > 0 && height - y <= 3) {
            l -= 4 - (height - y);
        }
        return l;
    }

    private double method_13421(int x, int z, BlockPos pos, int k, Random random) {
        float f = 10.0f * MathHelper.clamp(random.nextFloat(), 0.2f, 0.8f) / (float)k;
        return (double)f + Math.pow(x - pos.getX(), 2.0) + Math.pow(z - pos.getZ(), 2.0) - Math.pow(k, 2.0);
    }

    private double getDistance(int x, int z, BlockPos pos, int divisor1, int divisor2, double randomSine) {
        return Math.pow(((double)(x - pos.getX()) * Math.cos(randomSine) - (double)(z - pos.getZ()) * Math.sin(randomSine)) / (double)divisor1, 2.0) + Math.pow(((double)(x - pos.getX()) * Math.sin(randomSine) + (double)(z - pos.getZ()) * Math.cos(randomSine)) / (double)divisor2, 2.0) - 1.0;
    }

    private int method_13419(Random random, int y, int height, int factor) {
        float f = 3.5f - random.nextFloat();
        float g = (1.0f - (float)Math.pow(y, 2.0) / ((float)height * f)) * (float)factor;
        if (height > 15 + random.nextInt(5)) {
            int l = y < 3 + random.nextInt(6) ? y / 2 : y;
            g = (1.0f - (float)l / ((float)height * f * 0.4f)) * (float)factor;
        }
        return MathHelper.ceil(g / 2.0f);
    }

    private int method_13417(int y, int height, int factor) {
        float f = 1.0f;
        float g = (1.0f - (float)Math.pow(y, 2.0) / ((float)height * 1.0f)) * (float)factor;
        return MathHelper.ceil(g / 2.0f);
    }

    private int method_13427(Random random, int y, int height, int factor) {
        float f = 1.0f + random.nextFloat() / 2.0f;
        float g = (1.0f - (float)y / ((float)height * f)) * (float)factor;
        return MathHelper.ceil(g / 2.0f);
    }

    private static boolean isSnowOrIce(BlockState state) {
        return state.isOf(Blocks.PACKED_ICE) || state.isOf(Blocks.SNOW_BLOCK) || state.isOf(Blocks.BLUE_ICE);
    }

    private boolean isAirBelow(BlockView world, BlockPos pos) {
        return world.getBlockState(pos.down()).isAir();
    }

    private void method_13418(WorldAccess world, BlockPos pos, int i, int height, boolean bl, int k) {
        int l = bl ? k : i / 2;
        for (int m = -l; m <= l; ++m) {
            for (int n = -l; n <= l; ++n) {
                for (int o = 0; o <= height; ++o) {
                    BlockPos lv = pos.add(m, o, n);
                    BlockState lv2 = world.getBlockState(lv);
                    if (!IcebergFeature.isSnowOrIce(lv2) && !lv2.isOf(Blocks.SNOW)) continue;
                    if (this.isAirBelow(world, lv)) {
                        this.setBlockState(world, lv, Blocks.AIR.getDefaultState());
                        this.setBlockState(world, lv.up(), Blocks.AIR.getDefaultState());
                        continue;
                    }
                    if (!IcebergFeature.isSnowOrIce(lv2)) continue;
                    BlockState[] lvs = new BlockState[]{world.getBlockState(lv.west()), world.getBlockState(lv.east()), world.getBlockState(lv.north()), world.getBlockState(lv.south())};
                    int p = 0;
                    for (BlockState lv3 : lvs) {
                        if (IcebergFeature.isSnowOrIce(lv3)) continue;
                        ++p;
                    }
                    if (p < 3) continue;
                    this.setBlockState(world, lv, Blocks.AIR.getDefaultState());
                }
            }
        }
    }
}

