/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.message;

import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public interface SentMessage {
    public Text content();

    public void send(ServerPlayerEntity var1, boolean var2, MessageType.Parameters var3);

    public static SentMessage of(SignedMessage message) {
        if (message.isSenderMissing()) {
            return new Profileless(message.getContent());
        }
        return new Chat(message);
    }

    public record Profileless(Text content) implements SentMessage
    {
        @Override
        public void send(ServerPlayerEntity sender, boolean filterMaskEnabled, MessageType.Parameters params) {
            sender.networkHandler.sendProfilelessChatMessage(this.content, params);
        }
    }

    public record Chat(SignedMessage message) implements SentMessage
    {
        @Override
        public Text content() {
            return this.message.getContent();
        }

        @Override
        public void send(ServerPlayerEntity sender, boolean filterMaskEnabled, MessageType.Parameters params) {
            SignedMessage lv = this.message.withFilterMaskEnabled(filterMaskEnabled);
            if (!lv.isFullyFiltered()) {
                sender.networkHandler.sendChatMessage(lv, params);
            }
        }
    }
}

