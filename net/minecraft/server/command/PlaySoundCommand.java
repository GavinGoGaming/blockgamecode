/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.List;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class PlaySoundCommand {
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.playsound.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder requiredArgumentBuilder = (RequiredArgumentBuilder)CommandManager.argument("sound", IdentifierArgumentType.identifier()).suggests(SuggestionProviders.AVAILABLE_SOUNDS).executes(context -> PlaySoundCommand.execute((ServerCommandSource)context.getSource(), PlaySoundCommand.toList(((ServerCommandSource)context.getSource()).getPlayer()), IdentifierArgumentType.getIdentifier(context, "sound"), SoundCategory.MASTER, ((ServerCommandSource)context.getSource()).getPosition(), 1.0f, 1.0f, 0.0f));
        for (SoundCategory lv : SoundCategory.values()) {
            requiredArgumentBuilder.then(PlaySoundCommand.makeArgumentsForCategory(lv));
        }
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("playsound").requires(source -> source.hasPermissionLevel(2))).then(requiredArgumentBuilder));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> makeArgumentsForCategory(SoundCategory category) {
        return (LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal(category.getName()).executes(context -> PlaySoundCommand.execute((ServerCommandSource)context.getSource(), PlaySoundCommand.toList(((ServerCommandSource)context.getSource()).getPlayer()), IdentifierArgumentType.getIdentifier(context, "sound"), category, ((ServerCommandSource)context.getSource()).getPosition(), 1.0f, 1.0f, 0.0f))).then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).executes(context -> PlaySoundCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IdentifierArgumentType.getIdentifier(context, "sound"), category, ((ServerCommandSource)context.getSource()).getPosition(), 1.0f, 1.0f, 0.0f))).then(((RequiredArgumentBuilder)CommandManager.argument("pos", Vec3ArgumentType.vec3()).executes(context -> PlaySoundCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IdentifierArgumentType.getIdentifier(context, "sound"), category, Vec3ArgumentType.getVec3(context, "pos"), 1.0f, 1.0f, 0.0f))).then(((RequiredArgumentBuilder)CommandManager.argument("volume", FloatArgumentType.floatArg(0.0f)).executes(context -> PlaySoundCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IdentifierArgumentType.getIdentifier(context, "sound"), category, Vec3ArgumentType.getVec3(context, "pos"), context.getArgument("volume", Float.class).floatValue(), 1.0f, 0.0f))).then(((RequiredArgumentBuilder)CommandManager.argument("pitch", FloatArgumentType.floatArg(0.0f, 2.0f)).executes(context -> PlaySoundCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IdentifierArgumentType.getIdentifier(context, "sound"), category, Vec3ArgumentType.getVec3(context, "pos"), context.getArgument("volume", Float.class).floatValue(), context.getArgument("pitch", Float.class).floatValue(), 0.0f))).then(CommandManager.argument("minVolume", FloatArgumentType.floatArg(0.0f, 1.0f)).executes(context -> PlaySoundCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IdentifierArgumentType.getIdentifier(context, "sound"), category, Vec3ArgumentType.getVec3(context, "pos"), context.getArgument("volume", Float.class).floatValue(), context.getArgument("pitch", Float.class).floatValue(), context.getArgument("minVolume", Float.class).floatValue())))))));
    }

    private static Collection<ServerPlayerEntity> toList(@Nullable ServerPlayerEntity player) {
        return player != null ? List.of(player) : List.of();
    }

    private static int execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Identifier sound, SoundCategory category, Vec3d pos, float volume, float pitch, float minVolume) throws CommandSyntaxException {
        RegistryEntry<SoundEvent> lv = RegistryEntry.of(SoundEvent.of(sound));
        double d = MathHelper.square(lv.value().getDistanceToTravel(volume));
        int i = 0;
        long l = source.getWorld().getRandom().nextLong();
        for (ServerPlayerEntity lv2 : targets) {
            double e = pos.x - lv2.getX();
            double j = pos.y - lv2.getY();
            double k = pos.z - lv2.getZ();
            double m = e * e + j * j + k * k;
            Vec3d lv3 = pos;
            float n = volume;
            if (m > d) {
                if (minVolume <= 0.0f) continue;
                double o = Math.sqrt(m);
                lv3 = new Vec3d(lv2.getX() + e / o * 2.0, lv2.getY() + j / o * 2.0, lv2.getZ() + k / o * 2.0);
                n = minVolume;
            }
            lv2.networkHandler.sendPacket(new PlaySoundS2CPacket(lv, category, lv3.getX(), lv3.getY(), lv3.getZ(), n, pitch, l));
            ++i;
        }
        if (i == 0) {
            throw FAILED_EXCEPTION.create();
        }
        if (targets.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.playsound.success.single", Text.of(sound), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.playsound.success.multiple", Text.of(sound), targets.size()), true);
        }
        return i;
    }
}

