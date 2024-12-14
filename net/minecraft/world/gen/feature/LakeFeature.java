/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

@Deprecated
public class LakeFeature
extends Feature<Config> {
    private static final BlockState CAVE_AIR = Blocks.CAVE_AIR.getDefaultState();

    public LakeFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<Config> context) {
        int t;
        int s;
        BlockPos lv = context.getOrigin();
        StructureWorldAccess lv2 = context.getWorld();
        Random lv3 = context.getRandom();
        Config lv4 = context.getConfig();
        if (lv.getY() <= lv2.getBottomY() + 4) {
            return false;
        }
        lv = lv.down(4);
        boolean[] bls = new boolean[2048];
        int i = lv3.nextInt(4) + 4;
        for (int j = 0; j < i; ++j) {
            double d = lv3.nextDouble() * 6.0 + 3.0;
            double e = lv3.nextDouble() * 4.0 + 2.0;
            double f = lv3.nextDouble() * 6.0 + 3.0;
            double g = lv3.nextDouble() * (16.0 - d - 2.0) + 1.0 + d / 2.0;
            double h = lv3.nextDouble() * (8.0 - e - 4.0) + 2.0 + e / 2.0;
            double k = lv3.nextDouble() * (16.0 - f - 2.0) + 1.0 + f / 2.0;
            for (int l = 1; l < 15; ++l) {
                for (int m = 1; m < 15; ++m) {
                    for (int n = 1; n < 7; ++n) {
                        double o = ((double)l - g) / (d / 2.0);
                        double p = ((double)n - h) / (e / 2.0);
                        double q = ((double)m - k) / (f / 2.0);
                        double r = o * o + p * p + q * q;
                        if (!(r < 1.0)) continue;
                        bls[(l * 16 + m) * 8 + n] = true;
                    }
                }
            }
        }
        BlockState lv5 = lv4.fluid().get(lv3, lv);
        for (s = 0; s < 16; ++s) {
            for (t = 0; t < 16; ++t) {
                for (int u = 0; u < 8; ++u) {
                    boolean bl;
                    boolean bl2 = bl = !bls[(s * 16 + t) * 8 + u] && (s < 15 && bls[((s + 1) * 16 + t) * 8 + u] || s > 0 && bls[((s - 1) * 16 + t) * 8 + u] || t < 15 && bls[(s * 16 + t + 1) * 8 + u] || t > 0 && bls[(s * 16 + (t - 1)) * 8 + u] || u < 7 && bls[(s * 16 + t) * 8 + u + 1] || u > 0 && bls[(s * 16 + t) * 8 + (u - 1)]);
                    if (!bl) continue;
                    BlockState lv6 = lv2.getBlockState(lv.add(s, u, t));
                    if (u >= 4 && lv6.isLiquid()) {
                        return false;
                    }
                    if (u >= 4 || lv6.isSolid() || lv2.getBlockState(lv.add(s, u, t)) == lv5) continue;
                    return false;
                }
            }
        }
        for (s = 0; s < 16; ++s) {
            for (t = 0; t < 16; ++t) {
                for (int u = 0; u < 8; ++u) {
                    BlockPos lv7;
                    if (!bls[(s * 16 + t) * 8 + u] || !this.canReplace(lv2.getBlockState(lv7 = lv.add(s, u, t)))) continue;
                    boolean bl2 = u >= 4;
                    lv2.setBlockState(lv7, bl2 ? CAVE_AIR : lv5, Block.NOTIFY_LISTENERS);
                    if (!bl2) continue;
                    lv2.scheduleBlockTick(lv7, CAVE_AIR.getBlock(), 0);
                    this.markBlocksAboveForPostProcessing(lv2, lv7);
                }
            }
        }
        BlockState lv8 = lv4.barrier().get(lv3, lv);
        if (!lv8.isAir()) {
            for (t = 0; t < 16; ++t) {
                for (int u = 0; u < 16; ++u) {
                    for (int v = 0; v < 8; ++v) {
                        BlockState lv9;
                        boolean bl2;
                        boolean bl = bl2 = !bls[(t * 16 + u) * 8 + v] && (t < 15 && bls[((t + 1) * 16 + u) * 8 + v] || t > 0 && bls[((t - 1) * 16 + u) * 8 + v] || u < 15 && bls[(t * 16 + u + 1) * 8 + v] || u > 0 && bls[(t * 16 + (u - 1)) * 8 + v] || v < 7 && bls[(t * 16 + u) * 8 + v + 1] || v > 0 && bls[(t * 16 + u) * 8 + (v - 1)]);
                        if (!bl2 || v >= 4 && lv3.nextInt(2) == 0 || !(lv9 = lv2.getBlockState(lv.add(t, v, u))).isSolid() || lv9.isIn(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE)) continue;
                        BlockPos lv10 = lv.add(t, v, u);
                        lv2.setBlockState(lv10, lv8, Block.NOTIFY_LISTENERS);
                        this.markBlocksAboveForPostProcessing(lv2, lv10);
                    }
                }
            }
        }
        if (lv5.getFluidState().isIn(FluidTags.WATER)) {
            for (t = 0; t < 16; ++t) {
                for (int u = 0; u < 16; ++u) {
                    int v = 4;
                    BlockPos lv11 = lv.add(t, 4, u);
                    if (!lv2.getBiome(lv11).value().canSetIce(lv2, lv11, false) || !this.canReplace(lv2.getBlockState(lv11))) continue;
                    lv2.setBlockState(lv11, Blocks.ICE.getDefaultState(), Block.NOTIFY_LISTENERS);
                }
            }
        }
        return true;
    }

    private boolean canReplace(BlockState state) {
        return !state.isIn(BlockTags.FEATURES_CANNOT_REPLACE);
    }

    public record Config(BlockStateProvider fluid, BlockStateProvider barrier) implements FeatureConfig
    {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockStateProvider.TYPE_CODEC.fieldOf("fluid")).forGetter(Config::fluid), ((MapCodec)BlockStateProvider.TYPE_CODEC.fieldOf("barrier")).forGetter(Config::barrier)).apply((Applicative<Config, ?>)instance, Config::new));
    }
}

