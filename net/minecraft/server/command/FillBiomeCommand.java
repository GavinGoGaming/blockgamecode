/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.RegistryEntryPredicateArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.apache.commons.lang3.mutable.MutableInt;

public class FillBiomeCommand {
    public static final SimpleCommandExceptionType UNLOADED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.pos.unloaded"));
    private static final Dynamic2CommandExceptionType TOO_BIG_EXCEPTION = new Dynamic2CommandExceptionType((maximum, specified) -> Text.stringifiedTranslatable("commands.fillbiome.toobig", maximum, specified));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("fillbiome").requires(source -> source.hasPermissionLevel(2))).then(CommandManager.argument("from", BlockPosArgumentType.blockPos()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("to", BlockPosArgumentType.blockPos()).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("biome", RegistryEntryReferenceArgumentType.registryEntry(commandRegistryAccess, RegistryKeys.BIOME)).executes(context -> FillBiomeCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "from"), BlockPosArgumentType.getLoadedBlockPos(context, "to"), RegistryEntryReferenceArgumentType.getRegistryEntry(context, "biome", RegistryKeys.BIOME), arg -> true))).then(CommandManager.literal("replace").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("filter", RegistryEntryPredicateArgumentType.registryEntryPredicate(commandRegistryAccess, RegistryKeys.BIOME)).executes(context -> FillBiomeCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "from"), BlockPosArgumentType.getLoadedBlockPos(context, "to"), RegistryEntryReferenceArgumentType.getRegistryEntry(context, "biome", RegistryKeys.BIOME), RegistryEntryPredicateArgumentType.getRegistryEntryPredicate(context, "filter", RegistryKeys.BIOME)::test))))))));
    }

    private static int convertCoordinate(int coordinate) {
        return BiomeCoords.toBlock(BiomeCoords.fromBlock(coordinate));
    }

    private static BlockPos convertPos(BlockPos pos) {
        return new BlockPos(FillBiomeCommand.convertCoordinate(pos.getX()), FillBiomeCommand.convertCoordinate(pos.getY()), FillBiomeCommand.convertCoordinate(pos.getZ()));
    }

    private static BiomeSupplier createBiomeSupplier(MutableInt counter, Chunk chunk, BlockBox box, RegistryEntry<Biome> biome, Predicate<RegistryEntry<Biome>> filter) {
        return (x, y, z, noise) -> {
            int l = BiomeCoords.toBlock(x);
            int m = BiomeCoords.toBlock(y);
            int n = BiomeCoords.toBlock(z);
            RegistryEntry<Biome> lv = chunk.getBiomeForNoiseGen(x, y, z);
            if (box.contains(l, m, n) && filter.test(lv)) {
                counter.increment();
                return biome;
            }
            return lv;
        };
    }

    public static Either<Integer, CommandSyntaxException> fillBiome(ServerWorld world, BlockPos from, BlockPos to, RegistryEntry<Biome> biome) {
        return FillBiomeCommand.fillBiome(world, from, to, biome, biomex -> true, feedbackSupplier -> {});
    }

    public static Either<Integer, CommandSyntaxException> fillBiome(ServerWorld world, BlockPos from, BlockPos to, RegistryEntry<Biome> biome, Predicate<RegistryEntry<Biome>> filter, Consumer<Supplier<Text>> feedbackConsumer) {
        int j;
        BlockPos lv2;
        BlockPos lv = FillBiomeCommand.convertPos(from);
        BlockBox lv3 = BlockBox.create(lv, lv2 = FillBiomeCommand.convertPos(to));
        int i = lv3.getBlockCountX() * lv3.getBlockCountY() * lv3.getBlockCountZ();
        if (i > (j = world.getGameRules().getInt(GameRules.COMMAND_MODIFICATION_BLOCK_LIMIT))) {
            return Either.right(TOO_BIG_EXCEPTION.create(j, i));
        }
        ArrayList<Chunk> list = new ArrayList<Chunk>();
        for (int k = ChunkSectionPos.getSectionCoord(lv3.getMinZ()); k <= ChunkSectionPos.getSectionCoord(lv3.getMaxZ()); ++k) {
            for (int l = ChunkSectionPos.getSectionCoord(lv3.getMinX()); l <= ChunkSectionPos.getSectionCoord(lv3.getMaxX()); ++l) {
                Chunk lv4 = world.getChunk(l, k, ChunkStatus.FULL, false);
                if (lv4 == null) {
                    return Either.right(UNLOADED_EXCEPTION.create());
                }
                list.add(lv4);
            }
        }
        MutableInt mutableInt = new MutableInt(0);
        for (Chunk lv4 : list) {
            lv4.populateBiomes(FillBiomeCommand.createBiomeSupplier(mutableInt, lv4, lv3, biome, filter), world.getChunkManager().getNoiseConfig().getMultiNoiseSampler());
            lv4.setNeedsSaving(true);
        }
        world.getChunkManager().chunkLoadingManager.sendChunkBiomePackets(list);
        feedbackConsumer.accept(() -> Text.translatable("commands.fillbiome.success.count", mutableInt.getValue(), lv3.getMinX(), lv3.getMinY(), lv3.getMinZ(), lv3.getMaxX(), lv3.getMaxY(), lv3.getMaxZ()));
        return Either.left(mutableInt.getValue());
    }

    private static int execute(ServerCommandSource source, BlockPos from, BlockPos to, RegistryEntry.Reference<Biome> biome, Predicate<RegistryEntry<Biome>> filter) throws CommandSyntaxException {
        Either<Integer, CommandSyntaxException> either = FillBiomeCommand.fillBiome(source.getWorld(), from, to, biome, filter, feedbackSupplier -> source.sendFeedback((Supplier<Text>)feedbackSupplier, true));
        Optional<CommandSyntaxException> optional = either.right();
        if (optional.isPresent()) {
            throw optional.get();
        }
        return either.left().get();
    }
}

