/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.command.argument;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class BlockStateArgument
implements Predicate<CachedBlockPosition> {
    private final BlockState state;
    private final Set<Property<?>> properties;
    @Nullable
    private final NbtCompound data;

    public BlockStateArgument(BlockState state, Set<Property<?>> properties, @Nullable NbtCompound data) {
        this.state = state;
        this.properties = properties;
        this.data = data;
    }

    public BlockState getBlockState() {
        return this.state;
    }

    public Set<Property<?>> getProperties() {
        return this.properties;
    }

    @Override
    public boolean test(CachedBlockPosition arg) {
        BlockState lv = arg.getBlockState();
        if (!lv.isOf(this.state.getBlock())) {
            return false;
        }
        for (Property<?> lv2 : this.properties) {
            if (lv.get(lv2) == this.state.get(lv2)) continue;
            return false;
        }
        if (this.data != null) {
            BlockEntity lv3 = arg.getBlockEntity();
            return lv3 != null && NbtHelper.matches(this.data, lv3.createNbtWithIdentifyingData(arg.getWorld().getRegistryManager()), true);
        }
        return true;
    }

    public boolean test(ServerWorld world, BlockPos pos) {
        return this.test(new CachedBlockPosition(world, pos, false));
    }

    public boolean setBlockState(ServerWorld world, BlockPos pos, int flags) {
        BlockEntity lv2;
        BlockState lv = Block.postProcessState(this.state, world, pos);
        if (lv.isAir()) {
            lv = this.state;
        }
        if (!world.setBlockState(pos, lv, flags)) {
            return false;
        }
        if (this.data != null && (lv2 = world.getBlockEntity(pos)) != null) {
            lv2.read(this.data, world.getRegistryManager());
        }
        return true;
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((CachedBlockPosition)context);
    }
}

