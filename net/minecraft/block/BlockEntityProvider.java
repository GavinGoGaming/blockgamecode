/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;

public interface BlockEntityProvider {
    @Nullable
    public BlockEntity createBlockEntity(BlockPos var1, BlockState var2);

    @Nullable
    default public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    @Nullable
    default public <T extends BlockEntity> GameEventListener getGameEventListener(ServerWorld world, T blockEntity) {
        if (blockEntity instanceof GameEventListener.Holder) {
            GameEventListener.Holder lv = (GameEventListener.Holder)((Object)blockEntity);
            return lv.getEventListener();
        }
        return null;
    }
}

