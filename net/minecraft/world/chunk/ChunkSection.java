/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ReadableContainer;

public class ChunkSection {
    public static final int field_31406 = 16;
    public static final int field_31407 = 16;
    public static final int field_31408 = 4096;
    public static final int field_34555 = 2;
    private short nonEmptyBlockCount;
    private short randomTickableBlockCount;
    private short nonEmptyFluidCount;
    private final PalettedContainer<BlockState> blockStateContainer;
    private ReadableContainer<RegistryEntry<Biome>> biomeContainer;

    public ChunkSection(PalettedContainer<BlockState> blockStateContainer, ReadableContainer<RegistryEntry<Biome>> biomeContainer) {
        this.blockStateContainer = blockStateContainer;
        this.biomeContainer = biomeContainer;
        this.calculateCounts();
    }

    public ChunkSection(Registry<Biome> biomeRegistry) {
        this.blockStateContainer = new PalettedContainer<BlockState>(Block.STATE_IDS, Blocks.AIR.getDefaultState(), PalettedContainer.PaletteProvider.BLOCK_STATE);
        this.biomeContainer = new PalettedContainer<RegistryEntry.Reference<Biome>>(biomeRegistry.getIndexedEntries(), biomeRegistry.entryOf(BiomeKeys.PLAINS), PalettedContainer.PaletteProvider.BIOME);
    }

    public BlockState getBlockState(int x, int y, int z) {
        return this.blockStateContainer.get(x, y, z);
    }

    public FluidState getFluidState(int x, int y, int z) {
        return this.blockStateContainer.get(x, y, z).getFluidState();
    }

    public void lock() {
        this.blockStateContainer.lock();
    }

    public void unlock() {
        this.blockStateContainer.unlock();
    }

    public BlockState setBlockState(int x, int y, int z, BlockState state) {
        return this.setBlockState(x, y, z, state, true);
    }

    public BlockState setBlockState(int x, int y, int z, BlockState state, boolean lock) {
        BlockState lv = lock ? this.blockStateContainer.swap(x, y, z, state) : this.blockStateContainer.swapUnsafe(x, y, z, state);
        FluidState lv2 = lv.getFluidState();
        FluidState lv3 = state.getFluidState();
        if (!lv.isAir()) {
            this.nonEmptyBlockCount = (short)(this.nonEmptyBlockCount - 1);
            if (lv.hasRandomTicks()) {
                this.randomTickableBlockCount = (short)(this.randomTickableBlockCount - 1);
            }
        }
        if (!lv2.isEmpty()) {
            this.nonEmptyFluidCount = (short)(this.nonEmptyFluidCount - 1);
        }
        if (!state.isAir()) {
            this.nonEmptyBlockCount = (short)(this.nonEmptyBlockCount + 1);
            if (state.hasRandomTicks()) {
                this.randomTickableBlockCount = (short)(this.randomTickableBlockCount + 1);
            }
        }
        if (!lv3.isEmpty()) {
            this.nonEmptyFluidCount = (short)(this.nonEmptyFluidCount + 1);
        }
        return lv;
    }

    public boolean isEmpty() {
        return this.nonEmptyBlockCount == 0;
    }

    public boolean hasRandomTicks() {
        return this.hasRandomBlockTicks() || this.hasRandomFluidTicks();
    }

    public boolean hasRandomBlockTicks() {
        return this.randomTickableBlockCount > 0;
    }

    public boolean hasRandomFluidTicks() {
        return this.nonEmptyFluidCount > 0;
    }

    public void calculateCounts() {
        class BlockStateCounter
        implements PalettedContainer.Counter<BlockState> {
            public int nonEmptyBlockCount;
            public int randomTickableBlockCount;
            public int nonEmptyFluidCount;

            BlockStateCounter(ChunkSection arg) {
            }

            @Override
            public void accept(BlockState arg, int i) {
                FluidState lv = arg.getFluidState();
                if (!arg.isAir()) {
                    this.nonEmptyBlockCount += i;
                    if (arg.hasRandomTicks()) {
                        this.randomTickableBlockCount += i;
                    }
                }
                if (!lv.isEmpty()) {
                    this.nonEmptyBlockCount += i;
                    if (lv.hasRandomTicks()) {
                        this.nonEmptyFluidCount += i;
                    }
                }
            }

            @Override
            public /* synthetic */ void accept(Object object, int i) {
                this.accept((BlockState)object, i);
            }
        }
        BlockStateCounter lv = new BlockStateCounter(this);
        this.blockStateContainer.count(lv);
        this.nonEmptyBlockCount = (short)lv.nonEmptyBlockCount;
        this.randomTickableBlockCount = (short)lv.randomTickableBlockCount;
        this.nonEmptyFluidCount = (short)lv.nonEmptyFluidCount;
    }

    public PalettedContainer<BlockState> getBlockStateContainer() {
        return this.blockStateContainer;
    }

    public ReadableContainer<RegistryEntry<Biome>> getBiomeContainer() {
        return this.biomeContainer;
    }

    public void readDataPacket(PacketByteBuf buf) {
        this.nonEmptyBlockCount = buf.readShort();
        this.blockStateContainer.readPacket(buf);
        PalettedContainer<RegistryEntry<Biome>> lv = this.biomeContainer.slice();
        lv.readPacket(buf);
        this.biomeContainer = lv;
    }

    public void readBiomePacket(PacketByteBuf buf) {
        PalettedContainer<RegistryEntry<Biome>> lv = this.biomeContainer.slice();
        lv.readPacket(buf);
        this.biomeContainer = lv;
    }

    public void toPacket(PacketByteBuf buf) {
        buf.writeShort(this.nonEmptyBlockCount);
        this.blockStateContainer.writePacket(buf);
        this.biomeContainer.writePacket(buf);
    }

    public int getPacketSize() {
        return 2 + this.blockStateContainer.getPacketSize() + this.biomeContainer.getPacketSize();
    }

    public boolean hasAny(Predicate<BlockState> predicate) {
        return this.blockStateContainer.hasAny(predicate);
    }

    public RegistryEntry<Biome> getBiome(int x, int y, int z) {
        return this.biomeContainer.get(x, y, z);
    }

    public void populateBiomes(BiomeSupplier biomeSupplier, MultiNoiseUtil.MultiNoiseSampler sampler, int x, int y, int z) {
        PalettedContainer<RegistryEntry<Biome>> lv = this.biomeContainer.slice();
        int l = 4;
        for (int m = 0; m < 4; ++m) {
            for (int n = 0; n < 4; ++n) {
                for (int o = 0; o < 4; ++o) {
                    lv.swapUnsafe(m, n, o, biomeSupplier.getBiome(x + m, y + n, z + o, sampler));
                }
            }
        }
        this.biomeContainer = lv;
    }
}

