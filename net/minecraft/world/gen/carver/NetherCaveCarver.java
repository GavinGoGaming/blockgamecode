/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.carver.CarverContext;
import net.minecraft.world.gen.carver.CarvingMask;
import net.minecraft.world.gen.carver.CaveCarver;
import net.minecraft.world.gen.carver.CaveCarverConfig;
import net.minecraft.world.gen.chunk.AquiferSampler;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NetherCaveCarver
extends CaveCarver {
    public NetherCaveCarver(Codec<CaveCarverConfig> codec) {
        super(codec);
        this.carvableFluids = ImmutableSet.of(Fluids.LAVA, Fluids.WATER);
    }

    @Override
    protected int getMaxCaveCount() {
        return 10;
    }

    @Override
    protected float getTunnelSystemWidth(Random random) {
        return (random.nextFloat() * 2.0f + random.nextFloat()) * 2.0f;
    }

    @Override
    protected double getTunnelSystemHeightWidthRatio() {
        return 5.0;
    }

    @Override
    protected boolean carveAtPoint(CarverContext arg, CaveCarverConfig arg2, Chunk arg3, Function<BlockPos, RegistryEntry<Biome>> function, CarvingMask arg4, BlockPos.Mutable arg5, BlockPos.Mutable arg6, AquiferSampler arg7, MutableBoolean mutableBoolean) {
        if (this.canAlwaysCarveBlock(arg2, arg3.getBlockState(arg5))) {
            BlockState lv = arg5.getY() <= arg.getMinY() + 31 ? LAVA.getBlockState() : CAVE_AIR;
            arg3.setBlockState(arg5, lv, false);
            return true;
        }
        return false;
    }
}

