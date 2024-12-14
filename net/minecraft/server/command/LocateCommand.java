/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.google.common.base.Stopwatch;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.time.Duration;
import java.util.Optional;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryPredicateArgumentType;
import net.minecraft.command.argument.RegistryPredicateArgumentType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.slf4j.Logger;

public class LocateCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DynamicCommandExceptionType STRUCTURE_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(id -> Text.stringifiedTranslatable("commands.locate.structure.not_found", id));
    private static final DynamicCommandExceptionType STRUCTURE_INVALID_EXCEPTION = new DynamicCommandExceptionType(id -> Text.stringifiedTranslatable("commands.locate.structure.invalid", id));
    private static final DynamicCommandExceptionType BIOME_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(id -> Text.stringifiedTranslatable("commands.locate.biome.not_found", id));
    private static final DynamicCommandExceptionType POI_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(id -> Text.stringifiedTranslatable("commands.locate.poi.not_found", id));
    private static final int LOCATE_STRUCTURE_RADIUS = 100;
    private static final int LOCATE_BIOME_RADIUS = 6400;
    private static final int LOCATE_BIOME_HORIZONTAL_BLOCK_CHECK_INTERVAL = 32;
    private static final int LOCATE_BIOME_VERTICAL_BLOCK_CHECK_INTERVAL = 64;
    private static final int LOCATE_POI_RADIUS = 256;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("locate").requires(source -> source.hasPermissionLevel(2))).then(CommandManager.literal("structure").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("structure", RegistryPredicateArgumentType.registryPredicate(RegistryKeys.STRUCTURE)).executes(context -> LocateCommand.executeLocateStructure((ServerCommandSource)context.getSource(), RegistryPredicateArgumentType.getPredicate(context, "structure", RegistryKeys.STRUCTURE, STRUCTURE_INVALID_EXCEPTION)))))).then(CommandManager.literal("biome").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("biome", RegistryEntryPredicateArgumentType.registryEntryPredicate(registryAccess, RegistryKeys.BIOME)).executes(context -> LocateCommand.executeLocateBiome((ServerCommandSource)context.getSource(), RegistryEntryPredicateArgumentType.getRegistryEntryPredicate(context, "biome", RegistryKeys.BIOME)))))).then(CommandManager.literal("poi").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("poi", RegistryEntryPredicateArgumentType.registryEntryPredicate(registryAccess, RegistryKeys.POINT_OF_INTEREST_TYPE)).executes(context -> LocateCommand.executeLocatePoi((ServerCommandSource)context.getSource(), RegistryEntryPredicateArgumentType.getRegistryEntryPredicate(context, "poi", RegistryKeys.POINT_OF_INTEREST_TYPE))))));
    }

    private static Optional<? extends RegistryEntryList.ListBacked<Structure>> getStructureListForPredicate(RegistryPredicateArgumentType.RegistryPredicate<Structure> predicate, Registry<Structure> structureRegistry) {
        return predicate.getKey().map(key -> structureRegistry.getEntry((RegistryKey<Structure>)key).map(entry -> RegistryEntryList.of(entry)), structureRegistry::getEntryList);
    }

    private static int executeLocateStructure(ServerCommandSource source, RegistryPredicateArgumentType.RegistryPredicate<Structure> predicate) throws CommandSyntaxException {
        Registry<Structure> lv = source.getWorld().getRegistryManager().get(RegistryKeys.STRUCTURE);
        RegistryEntryList lv2 = LocateCommand.getStructureListForPredicate(predicate, lv).orElseThrow(() -> STRUCTURE_INVALID_EXCEPTION.create(predicate.asString()));
        BlockPos lv3 = BlockPos.ofFloored(source.getPosition());
        ServerWorld lv4 = source.getWorld();
        Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
        Pair<BlockPos, RegistryEntry<Structure>> pair = lv4.getChunkManager().getChunkGenerator().locateStructure(lv4, lv2, lv3, 100, false);
        stopwatch.stop();
        if (pair == null) {
            throw STRUCTURE_NOT_FOUND_EXCEPTION.create(predicate.asString());
        }
        return LocateCommand.sendCoordinates(source, predicate, lv3, pair, "commands.locate.structure.success", false, stopwatch.elapsed());
    }

    private static int executeLocateBiome(ServerCommandSource source, RegistryEntryPredicateArgumentType.EntryPredicate<Biome> predicate) throws CommandSyntaxException {
        BlockPos lv = BlockPos.ofFloored(source.getPosition());
        Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
        Pair<BlockPos, RegistryEntry<Biome>> pair = source.getWorld().locateBiome(predicate, lv, 6400, 32, 64);
        stopwatch.stop();
        if (pair == null) {
            throw BIOME_NOT_FOUND_EXCEPTION.create(predicate.asString());
        }
        return LocateCommand.sendCoordinates(source, predicate, lv, pair, "commands.locate.biome.success", true, stopwatch.elapsed());
    }

    private static int executeLocatePoi(ServerCommandSource source, RegistryEntryPredicateArgumentType.EntryPredicate<PointOfInterestType> predicate) throws CommandSyntaxException {
        BlockPos lv = BlockPos.ofFloored(source.getPosition());
        ServerWorld lv2 = source.getWorld();
        Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
        Optional<Pair<RegistryEntry<PointOfInterestType>, BlockPos>> optional = lv2.getPointOfInterestStorage().getNearestTypeAndPosition(predicate, lv, 256, PointOfInterestStorage.OccupationStatus.ANY);
        stopwatch.stop();
        if (optional.isEmpty()) {
            throw POI_NOT_FOUND_EXCEPTION.create(predicate.asString());
        }
        return LocateCommand.sendCoordinates(source, predicate, lv, optional.get().swap(), "commands.locate.poi.success", false, stopwatch.elapsed());
    }

    public static int sendCoordinates(ServerCommandSource source, RegistryEntryPredicateArgumentType.EntryPredicate<?> predicate, BlockPos currentPos, Pair<BlockPos, ? extends RegistryEntry<?>> result, String successMessage, boolean includeY, Duration timeTaken) {
        String string2 = predicate.getEntry().map(entry -> predicate.asString(), tag -> predicate.asString() + " (" + ((RegistryEntry)result.getSecond()).getIdAsString() + ")");
        return LocateCommand.sendCoordinates(source, currentPos, result, successMessage, includeY, string2, timeTaken);
    }

    public static int sendCoordinates(ServerCommandSource source, RegistryPredicateArgumentType.RegistryPredicate<?> structure, BlockPos currentPos, Pair<BlockPos, ? extends RegistryEntry<?>> result, String successMessage, boolean includeY, Duration timeTaken) {
        String string2 = structure.getKey().map(key -> key.getValue().toString(), key -> "#" + String.valueOf(key.id()) + " (" + ((RegistryEntry)result.getSecond()).getIdAsString() + ")");
        return LocateCommand.sendCoordinates(source, currentPos, result, successMessage, includeY, string2, timeTaken);
    }

    private static int sendCoordinates(ServerCommandSource source, BlockPos currentPos, Pair<BlockPos, ? extends RegistryEntry<?>> result, String successMessage, boolean includeY, String entryString, Duration timeTaken) {
        BlockPos lv = result.getFirst();
        int i = includeY ? MathHelper.floor(MathHelper.sqrt((float)currentPos.getSquaredDistance(lv))) : MathHelper.floor(LocateCommand.getDistance(currentPos.getX(), currentPos.getZ(), lv.getX(), lv.getZ()));
        String string3 = includeY ? String.valueOf(lv.getY()) : "~";
        MutableText lv2 = Texts.bracketed(Text.translatable("chat.coordinates", lv.getX(), string3, lv.getZ())).styled(style -> style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + lv.getX() + " " + string3 + " " + lv.getZ())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.coordinates.tooltip"))));
        source.sendFeedback(() -> Text.translatable(successMessage, entryString, lv2, i), false);
        LOGGER.info("Locating element " + entryString + " took " + timeTaken.toMillis() + " ms");
        return i;
    }

    private static float getDistance(int x1, int y1, int x2, int y2) {
        int m = x2 - x1;
        int n = y2 - y1;
        return MathHelper.sqrt(m * m + n * n);
    }
}

