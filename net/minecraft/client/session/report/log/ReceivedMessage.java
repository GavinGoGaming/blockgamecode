/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.session.report.log;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.message.MessageTrustStatus;
import net.minecraft.client.session.report.log.ChatLogEntry;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.Codecs;

@Environment(value=EnvType.CLIENT)
public interface ReceivedMessage
extends ChatLogEntry {
    public static ChatMessage of(GameProfile gameProfile, SignedMessage message, MessageTrustStatus trustStatus) {
        return new ChatMessage(gameProfile, message, trustStatus);
    }

    public static GameMessage of(Text message, Instant timestamp) {
        return new GameMessage(message, timestamp);
    }

    public Text getContent();

    default public Text getNarration() {
        return this.getContent();
    }

    public boolean isSentFrom(UUID var1);

    @Environment(value=EnvType.CLIENT)
    public record ChatMessage(GameProfile profile, SignedMessage message, MessageTrustStatus trustStatus) implements ReceivedMessage
    {
        public static final MapCodec<ChatMessage> CHAT_MESSAGE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codecs.GAME_PROFILE_WITH_PROPERTIES.fieldOf("profile")).forGetter(ChatMessage::profile), SignedMessage.CODEC.forGetter(ChatMessage::message), MessageTrustStatus.CODEC.optionalFieldOf("trust_level", MessageTrustStatus.SECURE).forGetter(ChatMessage::trustStatus)).apply((Applicative<ChatMessage, ?>)instance, ChatMessage::new));
        private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

        @Override
        public Text getContent() {
            if (!this.message.filterMask().isPassThrough()) {
                Text lv = this.message.filterMask().getFilteredText(this.message.getSignedContent());
                return lv != null ? lv : Text.empty();
            }
            return this.message.getContent();
        }

        @Override
        public Text getNarration() {
            Text lv = this.getContent();
            Text lv2 = this.getFormattedTimestamp();
            return Text.translatable("gui.chatSelection.message.narrate", this.profile.getName(), lv, lv2);
        }

        public Text getHeadingText() {
            Text lv = this.getFormattedTimestamp();
            return Text.translatable("gui.chatSelection.heading", this.profile.getName(), lv);
        }

        private Text getFormattedTimestamp() {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(this.message.getTimestamp(), ZoneOffset.systemDefault());
            return Text.literal(localDateTime.format(DATE_TIME_FORMATTER)).formatted(Formatting.ITALIC, Formatting.GRAY);
        }

        @Override
        public boolean isSentFrom(UUID uuid) {
            return this.message.canVerifyFrom(uuid);
        }

        public UUID getSenderUuid() {
            return this.profile.getId();
        }

        @Override
        public ChatLogEntry.Type getType() {
            return ChatLogEntry.Type.PLAYER;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record GameMessage(Text message, Instant timestamp) implements ReceivedMessage
    {
        public static final MapCodec<GameMessage> GAME_MESSAGE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)TextCodecs.CODEC.fieldOf("message")).forGetter(GameMessage::message), ((MapCodec)Codecs.INSTANT.fieldOf("time_stamp")).forGetter(GameMessage::timestamp)).apply((Applicative<GameMessage, ?>)instance, GameMessage::new));

        @Override
        public Text getContent() {
            return this.message;
        }

        @Override
        public boolean isSentFrom(UUID uuid) {
            return false;
        }

        @Override
        public ChatLogEntry.Type getType() {
            return ChatLogEntry.Type.SYSTEM;
        }
    }
}

