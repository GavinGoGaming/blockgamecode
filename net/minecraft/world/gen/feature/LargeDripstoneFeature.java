/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.LargeDripstoneFeatureConfig;
import net.minecraft.world.gen.feature.util.CaveSurface;
import net.minecraft.world.gen.feature.util.DripstoneHelper;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.jetbrains.annotations.Nullable;

public class LargeDripstoneFeature
extends Feature<LargeDripstoneFeatureConfig> {
    public LargeDripstoneFeature(Codec<LargeDripstoneFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<LargeDripstoneFeatureConfig> context) {
        StructureWorldAccess lv = context.getWorld();
        BlockPos lv2 = context.getOrigin();
        LargeDripstoneFeatureConfig lv3 = context.getConfig();
        Random lv4 = context.getRandom();
        if (!DripstoneHelper.canGenerate(lv, lv2)) {
            return false;
        }
        Optional<CaveSurface> optional = CaveSurface.create(lv, lv2, lv3.floorToCeilingSearchRange, DripstoneHelper::canGenerate, DripstoneHelper::canReplaceOrLava);
        if (optional.isEmpty() || !(optional.get() instanceof CaveSurface.Bounded)) {
            return false;
        }
        CaveSurface.Bounded lv5 = (CaveSurface.Bounded)optional.get();
        if (lv5.getHeight() < 4) {
            return false;
        }
        int i = (int)((float)lv5.getHeight() * lv3.maxColumnRadiusToCaveHeightRatio);
        int j = MathHelper.clamp(i, lv3.columnRadius.getMin(), lv3.columnRadius.getMax());
        int k = MathHelper.nextBetween(lv4, lv3.columnRadius.getMin(), j);
        DripstoneGenerator lv6 = LargeDripstoneFeature.createGenerator(lv2.withY(lv5.getCeiling() - 1), false, lv4, k, lv3.stalactiteBluntness, lv3.heightScale);
        DripstoneGenerator lv7 = LargeDripstoneFeature.createGenerator(lv2.withY(lv5.getFloor() + 1), true, lv4, k, lv3.stalagmiteBluntness, lv3.heightScale);
        WindModifier lv8 = lv6.generateWind(lv3) && lv7.generateWind(lv3) ? new WindModifier(lv2.getY(), lv4, lv3.windSpeed) : WindModifier.create();
        boolean bl = lv6.canGenerate(lv, lv8);
        boolean bl2 = lv7.canGenerate(lv, lv8);
        if (bl) {
            lv6.generate(lv, lv4, lv8);
        }
        if (bl2) {
            lv7.generate(lv, lv4, lv8);
        }
        return true;
    }

    private static DripstoneGenerator createGenerator(BlockPos pos, boolean isStalagmite, Random arg2, int scale, FloatProvider bluntness, FloatProvider heightScale) {
        return new DripstoneGenerator(pos, isStalagmite, scale, bluntness.get(arg2), heightScale.get(arg2));
    }

    private void testGeneration(StructureWorldAccess world, BlockPos pos, CaveSurface.Bounded surface, WindModifier wind) {
        world.setBlockState(wind.modify(pos.withY(surface.getCeiling() - 1)), Blocks.DIAMOND_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS);
        world.setBlockState(wind.modify(pos.withY(surface.getFloor() + 1)), Blocks.GOLD_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS);
        BlockPos.Mutable lv = pos.withY(surface.getFloor() + 2).mutableCopy();
        while (lv.getY() < surface.getCeiling() - 1) {
            BlockPos lv2 = wind.modify(lv);
            if (DripstoneHelper.canGenerate(world, lv2) || world.getBlockState(lv2).isOf(Blocks.DRIPSTONE_BLOCK)) {
                world.setBlockState(lv2, Blocks.CREEPER_HEAD.getDefaultState(), Block.NOTIFY_LISTENERS);
            }
            lv.move(Direction.UP);
        }
    }

    static final class DripstoneGenerator {
        private BlockPos pos;
        private final boolean isStalagmite;
        private int scale;
        private final double bluntness;
        private final double heightScale;

        DripstoneGenerator(BlockPos pos, boolean isStalagmite, int scale, double bluntness, double heightScale) {
            this.pos = pos;
            this.isStalagmite = isStalagmite;
            this.scale = scale;
            this.bluntness = bluntness;
            this.heightScale = heightScale;
        }

        private int getBaseScale() {
            return this.scale(0.0f);
        }

        private int getBottomY() {
            if (this.isStalagmite) {
                return this.pos.getY();
            }
            return this.pos.getY() - this.getBaseScale();
        }

        private int getTopY() {
            if (!this.isStalagmite) {
                return this.pos.getY();
            }
            return this.pos.getY() + this.getBaseScale();
        }

        boolean canGenerate(StructureWorldAccess world, WindModifier wind) {
            while (this.scale > 1) {
                BlockPos.Mutable lv = this.pos.mutableCopy();
                int i = Math.min(10, this.getBaseScale());
                for (int j = 0; j < i; ++j) {
                    if (world.getBlockState(lv).isOf(Blocks.LAVA)) {
                        return false;
                    }
                    if (DripstoneHelper.canGenerateBase(world, wind.modify(lv), this.scale)) {
                        this.pos = lv;
                        return true;
                    }
                    lv.move(this.isStalagmite ? Direction.DOWN : Direction.UP);
                }
                this.scale /= 2;
            }
            return false;
        }

        private int scale(float height) {
            return (int)DripstoneHelper.scaleHeightFromRadius(height, this.scale, this.heightScale, this.bluntness);
        }

        void generate(StructureWorldAccess world, Random random, WindModifier wind) {
            for (int i = -this.scale; i <= this.scale; ++i) {
                block1: for (int j = -this.scale; j <= this.scale; ++j) {
                    int k;
                    float f = MathHelper.sqrt(i * i + j * j);
                    if (f > (float)this.scale || (k = this.scale(f)) <= 0) continue;
                    if ((double)random.nextFloat() < 0.2) {
                        k = (int)((float)k * MathHelper.nextBetween(random, 0.8f, 1.0f));
                    }
                    BlockPos.Mutable lv = this.pos.add(i, 0, j).mutableCopy();
                    boolean bl = false;
                    int l = this.isStalagmite ? world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, lv.getX(), lv.getZ()) : Integer.MAX_VALUE;
                    for (int m = 0; m < k && lv.getY() < l; ++m) {
                        BlockPos lv2 = wind.modify(lv);
                        if (DripstoneHelper.canGenerateOrLava(world, lv2)) {
                            bl = true;
                            Block lv3 = Blocks.DRIPSTONE_BLOCK;
                            world.setBlockState(lv2, lv3.getDefaultState(), Block.NOTIFY_LISTENERS);
                        } else if (bl && world.getBlockState(lv2).isIn(BlockTags.BASE_STONE_OVERWORLD)) continue block1;
                        lv.move(this.isStalagmite ? Direction.UP : Direction.DOWN);
                    }
                }
            }
        }

        boolean generateWind(LargeDripstoneFeatureConfig config) {
            return this.scale >= config.minRadiusForWind && this.bluntness >= (double)config.minBluntnessForWind;
        }
    }

    static final class WindModifier {
        private final int y;
        @Nullable
        private final Vec3d wind;

        WindModifier(int y, Random random, FloatProvider wind) {
            this.y = y;
            float f = wind.get(random);
            float g = MathHelper.nextBetween(random, 0.0f, (float)Math.PI);
            this.wind = new Vec3d(MathHelper.cos(g) * f, 0.0, MathHelper.sin(g) * f);
        }

        private WindModifier() {
            this.y = 0;
            this.wind = null;
        }

        static WindModifier create() {
            return new WindModifier();
        }

        BlockPos modify(BlockPos pos) {
            if (this.wind == null) {
                return pos;
            }
            int i = this.y - pos.getY();
            Vec3d lv = this.wind.multiply(i);
            return pos.add(MathHelper.floor(lv.x), 0, MathHelper.floor(lv.z));
        }
    }
}

