/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.component.ComponentMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.tick.WorldTickScheduler;
import org.jetbrains.annotations.Nullable;

public class CloneCommand {
    private static final SimpleCommandExceptionType OVERLAP_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.clone.overlap"));
    private static final Dynamic2CommandExceptionType TOO_BIG_EXCEPTION = new Dynamic2CommandExceptionType((maxCount, count) -> Text.stringifiedTranslatable("commands.clone.toobig", maxCount, count));
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.clone.failed"));
    public static final Predicate<CachedBlockPosition> IS_AIR_PREDICATE = pos -> !pos.getBlockState().isAir();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("clone").requires(source -> source.hasPermissionLevel(2))).then(CloneCommand.createSourceArgs(commandRegistryAccess, context -> ((ServerCommandSource)context.getSource()).getWorld()))).then(CommandManager.literal("from").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("sourceDimension", DimensionArgumentType.dimension()).then(CloneCommand.createSourceArgs(commandRegistryAccess, context -> DimensionArgumentType.getDimensionArgument(context, "sourceDimension"))))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> createSourceArgs(CommandRegistryAccess commandRegistryAccess, ArgumentGetter<CommandContext<ServerCommandSource>, ServerWorld> worldGetter) {
        return CommandManager.argument("begin", BlockPosArgumentType.blockPos()).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("end", BlockPosArgumentType.blockPos()).then(CloneCommand.createDestinationArgs(commandRegistryAccess, worldGetter, context -> ((ServerCommandSource)context.getSource()).getWorld()))).then(CommandManager.literal("to").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("targetDimension", DimensionArgumentType.dimension()).then(CloneCommand.createDestinationArgs(commandRegistryAccess, worldGetter, context -> DimensionArgumentType.getDimensionArgument(context, "targetDimension"))))));
    }

    private static DimensionalPos createDimensionalPos(CommandContext<ServerCommandSource> context, ServerWorld world, String name) throws CommandSyntaxException {
        BlockPos lv = BlockPosArgumentType.getLoadedBlockPos(context, world, name);
        return new DimensionalPos(world, lv);
    }

    private static ArgumentBuilder<ServerCommandSource, ?> createDestinationArgs(CommandRegistryAccess commandRegistryAccess, ArgumentGetter<CommandContext<ServerCommandSource>, ServerWorld> sourceWorldGetter, ArgumentGetter<CommandContext<ServerCommandSource>, ServerWorld> targetWorldGetter) {
        ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> lv = context -> CloneCommand.createDimensionalPos(context, (ServerWorld)sourceWorldGetter.apply((CommandContext<ServerCommandSource>)context), "begin");
        ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> lv2 = context -> CloneCommand.createDimensionalPos(context, (ServerWorld)sourceWorldGetter.apply((CommandContext<ServerCommandSource>)context), "end");
        ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> lv3 = context -> CloneCommand.createDimensionalPos(context, (ServerWorld)targetWorldGetter.apply((CommandContext<ServerCommandSource>)context), "destination");
        return ((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("destination", BlockPosArgumentType.blockPos()).executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), (DimensionalPos)lv.apply(context), (DimensionalPos)lv2.apply(context), (DimensionalPos)lv3.apply(context), pos -> true, Mode.NORMAL))).then(CloneCommand.createModeArgs(lv, lv2, lv3, context -> arg -> true, CommandManager.literal("replace").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), (DimensionalPos)lv.apply(context), (DimensionalPos)lv2.apply(context), (DimensionalPos)lv3.apply(context), pos -> true, Mode.NORMAL))))).then(CloneCommand.createModeArgs(lv, lv2, lv3, context -> IS_AIR_PREDICATE, CommandManager.literal("masked").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), (DimensionalPos)lv.apply(context), (DimensionalPos)lv2.apply(context), (DimensionalPos)lv3.apply(context), IS_AIR_PREDICATE, Mode.NORMAL))))).then(CommandManager.literal("filtered").then(CloneCommand.createModeArgs(lv, lv2, lv3, context -> BlockPredicateArgumentType.getBlockPredicate(context, "filter"), CommandManager.argument("filter", BlockPredicateArgumentType.blockPredicate(commandRegistryAccess)).executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), (DimensionalPos)lv.apply(context), (DimensionalPos)lv2.apply(context), (DimensionalPos)lv3.apply(context), BlockPredicateArgumentType.getBlockPredicate(context, "filter"), Mode.NORMAL)))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> createModeArgs(ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> beginPosGetter, ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> endPosGetter, ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> destinationPosGetter, ArgumentGetter<CommandContext<ServerCommandSource>, Predicate<CachedBlockPosition>> filterGetter, ArgumentBuilder<ServerCommandSource, ?> builder) {
        return ((ArgumentBuilder)((ArgumentBuilder)builder.then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("force").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), (DimensionalPos)beginPosGetter.apply(context), (DimensionalPos)endPosGetter.apply(context), (DimensionalPos)destinationPosGetter.apply(context), (Predicate)filterGetter.apply(context), Mode.FORCE)))).then(CommandManager.literal("move").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), (DimensionalPos)beginPosGetter.apply(context), (DimensionalPos)endPosGetter.apply(context), (DimensionalPos)destinationPosGetter.apply(context), (Predicate)filterGetter.apply(context), Mode.MOVE)))).then(CommandManager.literal("normal").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), (DimensionalPos)beginPosGetter.apply(context), (DimensionalPos)endPosGetter.apply(context), (DimensionalPos)destinationPosGetter.apply(context), (Predicate)filterGetter.apply(context), Mode.NORMAL)));
    }

    private static int execute(ServerCommandSource source, DimensionalPos begin, DimensionalPos end, DimensionalPos destination, Predicate<CachedBlockPosition> filter, Mode mode) throws CommandSyntaxException {
        int j;
        BlockPos lv = begin.position();
        BlockPos lv2 = end.position();
        BlockBox lv3 = BlockBox.create(lv, lv2);
        BlockPos lv4 = destination.position();
        BlockPos lv5 = lv4.add(lv3.getDimensions());
        BlockBox lv6 = BlockBox.create(lv4, lv5);
        ServerWorld lv7 = begin.dimension();
        ServerWorld lv8 = destination.dimension();
        if (!mode.allowsOverlap() && lv7 == lv8 && lv6.intersects(lv3)) {
            throw OVERLAP_EXCEPTION.create();
        }
        int i = lv3.getBlockCountX() * lv3.getBlockCountY() * lv3.getBlockCountZ();
        if (i > (j = source.getWorld().getGameRules().getInt(GameRules.COMMAND_MODIFICATION_BLOCK_LIMIT))) {
            throw TOO_BIG_EXCEPTION.create(j, i);
        }
        if (!lv7.isRegionLoaded(lv, lv2) || !lv8.isRegionLoaded(lv4, lv5)) {
            throw BlockPosArgumentType.UNLOADED_EXCEPTION.create();
        }
        ArrayList<BlockInfo> list = Lists.newArrayList();
        ArrayList<BlockInfo> list2 = Lists.newArrayList();
        ArrayList<BlockInfo> list3 = Lists.newArrayList();
        LinkedList<BlockPos> deque = Lists.newLinkedList();
        BlockPos lv9 = new BlockPos(lv6.getMinX() - lv3.getMinX(), lv6.getMinY() - lv3.getMinY(), lv6.getMinZ() - lv3.getMinZ());
        for (int k = lv3.getMinZ(); k <= lv3.getMaxZ(); ++k) {
            for (int l = lv3.getMinY(); l <= lv3.getMaxY(); ++l) {
                for (int m = lv3.getMinX(); m <= lv3.getMaxX(); ++m) {
                    BlockPos lv10 = new BlockPos(m, l, k);
                    BlockPos lv11 = lv10.add(lv9);
                    CachedBlockPosition lv12 = new CachedBlockPosition(lv7, lv10, false);
                    BlockState lv13 = lv12.getBlockState();
                    if (!filter.test(lv12)) continue;
                    BlockEntity lv14 = lv7.getBlockEntity(lv10);
                    if (lv14 != null) {
                        BlockEntityInfo lv15 = new BlockEntityInfo(lv14.createComponentlessNbt(source.getRegistryManager()), lv14.getComponents());
                        list2.add(new BlockInfo(lv11, lv13, lv15));
                        deque.addLast(lv10);
                        continue;
                    }
                    if (lv13.isOpaqueFullCube(lv7, lv10) || lv13.isFullCube(lv7, lv10)) {
                        list.add(new BlockInfo(lv11, lv13, null));
                        deque.addLast(lv10);
                        continue;
                    }
                    list3.add(new BlockInfo(lv11, lv13, null));
                    deque.addFirst(lv10);
                }
            }
        }
        if (mode == Mode.MOVE) {
            for (BlockPos lv16 : deque) {
                BlockEntity lv17 = lv7.getBlockEntity(lv16);
                Clearable.clear(lv17);
                lv7.setBlockState(lv16, Blocks.BARRIER.getDefaultState(), Block.NOTIFY_LISTENERS);
            }
            for (BlockPos lv16 : deque) {
                lv7.setBlockState(lv16, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            }
        }
        ArrayList<BlockInfo> list4 = Lists.newArrayList();
        list4.addAll(list);
        list4.addAll(list2);
        list4.addAll(list3);
        List list5 = Lists.reverse(list4);
        for (Iterator lv18 : list5) {
            BlockEntity lv19 = lv8.getBlockEntity(((BlockInfo)((Object)lv18)).pos);
            Clearable.clear(lv19);
            lv8.setBlockState(((BlockInfo)((Object)lv18)).pos, Blocks.BARRIER.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
        int m = 0;
        for (BlockInfo lv20 : list4) {
            if (!lv8.setBlockState(lv20.pos, lv20.state, Block.NOTIFY_LISTENERS)) continue;
            ++m;
        }
        for (BlockInfo lv20 : list2) {
            BlockEntity lv21 = lv8.getBlockEntity(lv20.pos);
            if (lv20.blockEntityInfo != null && lv21 != null) {
                lv21.readComponentlessNbt(lv20.blockEntityInfo.nbt, lv8.getRegistryManager());
                lv21.setComponents(lv20.blockEntityInfo.components);
                lv21.markDirty();
            }
            lv8.setBlockState(lv20.pos, lv20.state, Block.NOTIFY_LISTENERS);
        }
        for (BlockInfo lv20 : list5) {
            lv8.updateNeighbors(lv20.pos, lv20.state.getBlock());
        }
        ((WorldTickScheduler)lv8.getBlockTickScheduler()).scheduleTicks(lv7.getBlockTickScheduler(), lv3, lv9);
        if (m == 0) {
            throw FAILED_EXCEPTION.create();
        }
        int n = m;
        source.sendFeedback(() -> Text.translatable("commands.clone.success", n), true);
        return m;
    }

    @FunctionalInterface
    static interface ArgumentGetter<T, R> {
        public R apply(T var1) throws CommandSyntaxException;
    }

    record DimensionalPos(ServerWorld dimension, BlockPos position) {
    }

    static enum Mode {
        FORCE(true),
        MOVE(true),
        NORMAL(false);

        private final boolean allowsOverlap;

        private Mode(boolean allowsOverlap) {
            this.allowsOverlap = allowsOverlap;
        }

        public boolean allowsOverlap() {
            return this.allowsOverlap;
        }
    }

    record BlockEntityInfo(NbtCompound nbt, ComponentMap components) {
    }

    record BlockInfo(BlockPos pos, BlockState state, @Nullable BlockEntityInfo blockEntityInfo) {
        @Nullable
        public BlockEntityInfo blockEntityInfo() {
            return this.blockEntityInfo;
        }
    }
}

