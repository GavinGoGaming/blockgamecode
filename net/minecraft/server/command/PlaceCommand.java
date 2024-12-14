/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Optional;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockMirrorArgumentType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockRotationArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.structure.processor.BlockRotStructureProcessor;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.structure.Structure;

public class PlaceCommand {
    private static final SimpleCommandExceptionType FEATURE_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.place.feature.failed"));
    private static final SimpleCommandExceptionType JIGSAW_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.place.jigsaw.failed"));
    private static final SimpleCommandExceptionType STRUCTURE_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.place.structure.failed"));
    private static final DynamicCommandExceptionType TEMPLATE_INVALID_EXCEPTION = new DynamicCommandExceptionType(id -> Text.stringifiedTranslatable("commands.place.template.invalid", id));
    private static final SimpleCommandExceptionType TEMPLATE_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.place.template.failed"));
    private static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder) -> {
        StructureTemplateManager lv = ((ServerCommandSource)context.getSource()).getWorld().getStructureTemplateManager();
        return CommandSource.suggestIdentifiers(lv.streamTemplates(), builder);
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("place").requires(source -> source.hasPermissionLevel(2))).then(CommandManager.literal("feature").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("feature", RegistryKeyArgumentType.registryKey(RegistryKeys.CONFIGURED_FEATURE)).executes(context -> PlaceCommand.executePlaceFeature((ServerCommandSource)context.getSource(), RegistryKeyArgumentType.getConfiguredFeatureEntry(context, "feature"), BlockPos.ofFloored(((ServerCommandSource)context.getSource()).getPosition())))).then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes(context -> PlaceCommand.executePlaceFeature((ServerCommandSource)context.getSource(), RegistryKeyArgumentType.getConfiguredFeatureEntry(context, "feature"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"))))))).then(CommandManager.literal("jigsaw").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("pool", RegistryKeyArgumentType.registryKey(RegistryKeys.TEMPLATE_POOL)).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("target", IdentifierArgumentType.identifier()).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("max_depth", IntegerArgumentType.integer(1, 20)).executes(context -> PlaceCommand.executePlaceJigsaw((ServerCommandSource)context.getSource(), RegistryKeyArgumentType.getStructurePoolEntry(context, "pool"), IdentifierArgumentType.getIdentifier(context, "target"), IntegerArgumentType.getInteger(context, "max_depth"), BlockPos.ofFloored(((ServerCommandSource)context.getSource()).getPosition())))).then(CommandManager.argument("position", BlockPosArgumentType.blockPos()).executes(context -> PlaceCommand.executePlaceJigsaw((ServerCommandSource)context.getSource(), RegistryKeyArgumentType.getStructurePoolEntry(context, "pool"), IdentifierArgumentType.getIdentifier(context, "target"), IntegerArgumentType.getInteger(context, "max_depth"), BlockPosArgumentType.getLoadedBlockPos(context, "position"))))))))).then(CommandManager.literal("structure").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("structure", RegistryKeyArgumentType.registryKey(RegistryKeys.STRUCTURE)).executes(context -> PlaceCommand.executePlaceStructure((ServerCommandSource)context.getSource(), RegistryKeyArgumentType.getStructureEntry(context, "structure"), BlockPos.ofFloored(((ServerCommandSource)context.getSource()).getPosition())))).then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes(context -> PlaceCommand.executePlaceStructure((ServerCommandSource)context.getSource(), RegistryKeyArgumentType.getStructureEntry(context, "structure"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"))))))).then(CommandManager.literal("template").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("template", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes(context -> PlaceCommand.executePlaceTemplate((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier(context, "template"), BlockPos.ofFloored(((ServerCommandSource)context.getSource()).getPosition()), BlockRotation.NONE, BlockMirror.NONE, 1.0f, 0))).then(((RequiredArgumentBuilder)CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes(context -> PlaceCommand.executePlaceTemplate((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier(context, "template"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), BlockRotation.NONE, BlockMirror.NONE, 1.0f, 0))).then(((RequiredArgumentBuilder)CommandManager.argument("rotation", BlockRotationArgumentType.blockRotation()).executes(context -> PlaceCommand.executePlaceTemplate((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier(context, "template"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), BlockRotationArgumentType.getBlockRotation(context, "rotation"), BlockMirror.NONE, 1.0f, 0))).then(((RequiredArgumentBuilder)CommandManager.argument("mirror", BlockMirrorArgumentType.blockMirror()).executes(context -> PlaceCommand.executePlaceTemplate((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier(context, "template"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), BlockRotationArgumentType.getBlockRotation(context, "rotation"), BlockMirrorArgumentType.getBlockMirror(context, "mirror"), 1.0f, 0))).then(((RequiredArgumentBuilder)CommandManager.argument("integrity", FloatArgumentType.floatArg(0.0f, 1.0f)).executes(context -> PlaceCommand.executePlaceTemplate((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier(context, "template"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), BlockRotationArgumentType.getBlockRotation(context, "rotation"), BlockMirrorArgumentType.getBlockMirror(context, "mirror"), FloatArgumentType.getFloat(context, "integrity"), 0))).then(CommandManager.argument("seed", IntegerArgumentType.integer()).executes(context -> PlaceCommand.executePlaceTemplate((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier(context, "template"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), BlockRotationArgumentType.getBlockRotation(context, "rotation"), BlockMirrorArgumentType.getBlockMirror(context, "mirror"), FloatArgumentType.getFloat(context, "integrity"), IntegerArgumentType.getInteger(context, "seed")))))))))));
    }

    public static int executePlaceFeature(ServerCommandSource source, RegistryEntry.Reference<ConfiguredFeature<?, ?>> feature, BlockPos pos) throws CommandSyntaxException {
        ServerWorld lv = source.getWorld();
        ConfiguredFeature<?, ?> lv2 = feature.value();
        ChunkPos lv3 = new ChunkPos(pos);
        PlaceCommand.throwOnUnloadedPos(lv, new ChunkPos(lv3.x - 1, lv3.z - 1), new ChunkPos(lv3.x + 1, lv3.z + 1));
        if (!lv2.generate(lv, lv.getChunkManager().getChunkGenerator(), lv.getRandom(), pos)) {
            throw FEATURE_FAILED_EXCEPTION.create();
        }
        String string = feature.registryKey().getValue().toString();
        source.sendFeedback(() -> Text.translatable("commands.place.feature.success", string, pos.getX(), pos.getY(), pos.getZ()), true);
        return 1;
    }

    public static int executePlaceJigsaw(ServerCommandSource source, RegistryEntry<StructurePool> structurePool, Identifier id, int maxDepth, BlockPos pos) throws CommandSyntaxException {
        ServerWorld lv = source.getWorld();
        ChunkPos lv2 = new ChunkPos(pos);
        PlaceCommand.throwOnUnloadedPos(lv, lv2, lv2);
        if (!StructurePoolBasedGenerator.generate(lv, structurePool, id, maxDepth, pos, false)) {
            throw JIGSAW_FAILED_EXCEPTION.create();
        }
        source.sendFeedback(() -> Text.translatable("commands.place.jigsaw.success", pos.getX(), pos.getY(), pos.getZ()), true);
        return 1;
    }

    public static int executePlaceStructure(ServerCommandSource source, RegistryEntry.Reference<Structure> structure, BlockPos pos) throws CommandSyntaxException {
        ServerWorld lv = source.getWorld();
        Structure lv2 = structure.value();
        ChunkGenerator lv3 = lv.getChunkManager().getChunkGenerator();
        StructureStart lv4 = lv2.createStructureStart(source.getRegistryManager(), lv3, lv3.getBiomeSource(), lv.getChunkManager().getNoiseConfig(), lv.getStructureTemplateManager(), lv.getSeed(), new ChunkPos(pos), 0, lv, biome -> true);
        if (!lv4.hasChildren()) {
            throw STRUCTURE_FAILED_EXCEPTION.create();
        }
        BlockBox lv5 = lv4.getBoundingBox();
        ChunkPos lv6 = new ChunkPos(ChunkSectionPos.getSectionCoord(lv5.getMinX()), ChunkSectionPos.getSectionCoord(lv5.getMinZ()));
        ChunkPos lv7 = new ChunkPos(ChunkSectionPos.getSectionCoord(lv5.getMaxX()), ChunkSectionPos.getSectionCoord(lv5.getMaxZ()));
        PlaceCommand.throwOnUnloadedPos(lv, lv6, lv7);
        ChunkPos.stream(lv6, lv7).forEach(chunkPos -> lv4.place(lv, lv.getStructureAccessor(), lv3, lv.getRandom(), new BlockBox(chunkPos.getStartX(), lv.getBottomY(), chunkPos.getStartZ(), chunkPos.getEndX(), lv.getTopY(), chunkPos.getEndZ()), (ChunkPos)chunkPos));
        String string = structure.registryKey().getValue().toString();
        source.sendFeedback(() -> Text.translatable("commands.place.structure.success", string, pos.getX(), pos.getY(), pos.getZ()), true);
        return 1;
    }

    public static int executePlaceTemplate(ServerCommandSource source, Identifier id, BlockPos pos, BlockRotation rotation, BlockMirror mirror, float integrity, int seed) throws CommandSyntaxException {
        boolean bl;
        Optional<StructureTemplate> optional;
        ServerWorld lv = source.getWorld();
        StructureTemplateManager lv2 = lv.getStructureTemplateManager();
        try {
            optional = lv2.getTemplate(id);
        } catch (InvalidIdentifierException lv3) {
            throw TEMPLATE_INVALID_EXCEPTION.create(id);
        }
        if (optional.isEmpty()) {
            throw TEMPLATE_INVALID_EXCEPTION.create(id);
        }
        StructureTemplate lv4 = optional.get();
        PlaceCommand.throwOnUnloadedPos(lv, new ChunkPos(pos), new ChunkPos(pos.add(lv4.getSize())));
        StructurePlacementData lv5 = new StructurePlacementData().setMirror(mirror).setRotation(rotation);
        if (integrity < 1.0f) {
            lv5.clearProcessors().addProcessor(new BlockRotStructureProcessor(integrity)).setRandom(StructureBlockBlockEntity.createRandom(seed));
        }
        if (!(bl = lv4.place(lv, pos, pos, lv5, StructureBlockBlockEntity.createRandom(seed), 2))) {
            throw TEMPLATE_FAILED_EXCEPTION.create();
        }
        source.sendFeedback(() -> Text.translatable("commands.place.template.success", Text.of(id), pos.getX(), pos.getY(), pos.getZ()), true);
        return 1;
    }

    private static void throwOnUnloadedPos(ServerWorld world, ChunkPos pos1, ChunkPos pos2) throws CommandSyntaxException {
        if (ChunkPos.stream(pos1, pos2).filter(pos -> !world.canSetBlock(pos.getStartPos())).findAny().isPresent()) {
            throw BlockPosArgumentType.UNLOADED_EXCEPTION.create();
        }
    }
}

