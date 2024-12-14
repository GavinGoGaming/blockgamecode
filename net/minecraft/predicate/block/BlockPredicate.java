/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.predicate.block;

import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockPredicate
implements Predicate<BlockState> {
    private final Block block;

    public BlockPredicate(Block block) {
        this.block = block;
    }

    public static BlockPredicate make(Block block) {
        return new BlockPredicate(block);
    }

    @Override
    public boolean test(@Nullable BlockState arg) {
        return arg != null && arg.isOf(this.block);
    }

    @Override
    public /* synthetic */ boolean test(@Nullable Object context) {
        return this.test((BlockState)context);
    }
}

