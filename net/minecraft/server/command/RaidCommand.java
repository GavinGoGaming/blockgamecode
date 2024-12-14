/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Set;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.raid.Raid;
import net.minecraft.village.raid.RaidManager;
import org.jetbrains.annotations.Nullable;

public class RaidCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("raid").requires(source -> source.hasPermissionLevel(3))).then(CommandManager.literal("start").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("omenlvl", IntegerArgumentType.integer(0)).executes(context -> RaidCommand.executeStart((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger(context, "omenlvl")))))).then(CommandManager.literal("stop").executes(context -> RaidCommand.executeStop((ServerCommandSource)context.getSource())))).then(CommandManager.literal("check").executes(context -> RaidCommand.executeCheck((ServerCommandSource)context.getSource())))).then(CommandManager.literal("sound").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("type", TextArgumentType.text(registryAccess)).executes(context -> RaidCommand.executeSound((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "type")))))).then(CommandManager.literal("spawnleader").executes(context -> RaidCommand.executeSpawnLeader((ServerCommandSource)context.getSource())))).then(CommandManager.literal("setomen").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("level", IntegerArgumentType.integer(0)).executes(context -> RaidCommand.executeSetOmen((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger(context, "level")))))).then(CommandManager.literal("glow").executes(context -> RaidCommand.executeGlow((ServerCommandSource)context.getSource()))));
    }

    private static int executeGlow(ServerCommandSource source) throws CommandSyntaxException {
        Raid lv = RaidCommand.getRaid(source.getPlayerOrThrow());
        if (lv != null) {
            Set<RaiderEntity> set = lv.getAllRaiders();
            for (RaiderEntity lv2 : set) {
                lv2.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 1000, 1));
            }
        }
        return 1;
    }

    private static int executeSetOmen(ServerCommandSource source, int level) throws CommandSyntaxException {
        Raid lv = RaidCommand.getRaid(source.getPlayerOrThrow());
        if (lv != null) {
            int j = lv.getMaxAcceptableBadOmenLevel();
            if (level > j) {
                source.sendError(Text.literal("Sorry, the max raid omen level you can set is " + j));
            } else {
                int k = lv.getBadOmenLevel();
                lv.setBadOmenLevel(level);
                source.sendFeedback(() -> Text.literal("Changed village's raid omen level from " + k + " to " + level), false);
            }
        } else {
            source.sendError(Text.literal("No raid found here"));
        }
        return 1;
    }

    private static int executeSpawnLeader(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Spawned a raid captain"), false);
        RaiderEntity lv = EntityType.PILLAGER.create(source.getWorld());
        if (lv == null) {
            source.sendError(Text.literal("Pillager failed to spawn"));
            return 0;
        }
        lv.setPatrolLeader(true);
        lv.equipStack(EquipmentSlot.HEAD, Raid.getOminousBanner(source.getRegistryManager().getWrapperOrThrow(RegistryKeys.BANNER_PATTERN)));
        lv.setPosition(source.getPosition().x, source.getPosition().y, source.getPosition().z);
        lv.initialize(source.getWorld(), source.getWorld().getLocalDifficulty(BlockPos.ofFloored(source.getPosition())), SpawnReason.COMMAND, null);
        source.getWorld().spawnEntityAndPassengers(lv);
        return 1;
    }

    private static int executeSound(ServerCommandSource source, @Nullable Text type) {
        if (type != null && type.getString().equals("local")) {
            ServerWorld lv = source.getWorld();
            Vec3d lv2 = source.getPosition().add(5.0, 0.0, 0.0);
            lv.playSound(null, lv2.x, lv2.y, lv2.z, SoundEvents.EVENT_RAID_HORN, SoundCategory.NEUTRAL, 2.0f, 1.0f, lv.random.nextLong());
        }
        return 1;
    }

    private static int executeStart(ServerCommandSource source, int level) throws CommandSyntaxException {
        ServerPlayerEntity lv = source.getPlayerOrThrow();
        BlockPos lv2 = lv.getBlockPos();
        if (lv.getServerWorld().hasRaidAt(lv2)) {
            source.sendError(Text.literal("Raid already started close by"));
            return -1;
        }
        RaidManager lv3 = lv.getServerWorld().getRaidManager();
        Raid lv4 = lv3.startRaid(lv, lv.getBlockPos());
        if (lv4 != null) {
            lv4.setBadOmenLevel(level);
            lv3.markDirty();
            source.sendFeedback(() -> Text.literal("Created a raid in your local village"), false);
        } else {
            source.sendError(Text.literal("Failed to create a raid in your local village"));
        }
        return 1;
    }

    private static int executeStop(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity lv = source.getPlayerOrThrow();
        BlockPos lv2 = lv.getBlockPos();
        Raid lv3 = lv.getServerWorld().getRaidAt(lv2);
        if (lv3 != null) {
            lv3.invalidate();
            source.sendFeedback(() -> Text.literal("Stopped raid"), false);
            return 1;
        }
        source.sendError(Text.literal("No raid here"));
        return -1;
    }

    private static int executeCheck(ServerCommandSource source) throws CommandSyntaxException {
        Raid lv = RaidCommand.getRaid(source.getPlayerOrThrow());
        if (lv != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Found a started raid! ");
            source.sendFeedback(() -> Text.literal(stringBuilder.toString()), false);
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Num groups spawned: ");
            stringBuilder2.append(lv.getGroupsSpawned());
            stringBuilder2.append(" Raid omen level: ");
            stringBuilder2.append(lv.getBadOmenLevel());
            stringBuilder2.append(" Num mobs: ");
            stringBuilder2.append(lv.getRaiderCount());
            stringBuilder2.append(" Raid health: ");
            stringBuilder2.append(lv.getCurrentRaiderHealth());
            stringBuilder2.append(" / ");
            stringBuilder2.append(lv.getTotalHealth());
            source.sendFeedback(() -> Text.literal(stringBuilder2.toString()), false);
            return 1;
        }
        source.sendError(Text.literal("Found no started raids"));
        return 0;
    }

    @Nullable
    private static Raid getRaid(ServerPlayerEntity player) {
        return player.getServerWorld().getRaidAt(player.getBlockPos());
    }
}

