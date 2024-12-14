/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.network.message;

import com.google.common.collect.Queues;
import com.mojang.authlib.GameProfile;
import java.time.Instant;
import java.util.Deque;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.message.MessageTrustStatus;
import net.minecraft.client.session.report.log.ChatLog;
import net.minecraft.client.session.report.log.ReceivedMessage;
import net.minecraft.network.message.FilterMask;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class MessageHandler {
    private static final Text VALIDATION_ERROR_TEXT = Text.translatable("chat.validation_error").formatted(Formatting.RED, Formatting.ITALIC);
    private final MinecraftClient client;
    private final Deque<ProcessableMessage> delayedMessages = Queues.newArrayDeque();
    private long chatDelay;
    private long lastProcessTime;

    public MessageHandler(MinecraftClient client) {
        this.client = client;
    }

    public void processDelayedMessages() {
        if (this.chatDelay == 0L) {
            return;
        }
        if (Util.getMeasuringTimeMs() >= this.lastProcessTime + this.chatDelay) {
            ProcessableMessage lv = this.delayedMessages.poll();
            while (lv != null && !lv.accept()) {
                lv = this.delayedMessages.poll();
            }
        }
    }

    public void setChatDelay(double chatDelay) {
        long l = (long)(chatDelay * 1000.0);
        if (l == 0L && this.chatDelay > 0L) {
            this.delayedMessages.forEach(ProcessableMessage::accept);
            this.delayedMessages.clear();
        }
        this.chatDelay = l;
    }

    public void process() {
        this.delayedMessages.remove().accept();
    }

    public long getUnprocessedMessageCount() {
        return this.delayedMessages.size();
    }

    public void processAll() {
        this.delayedMessages.forEach(ProcessableMessage::accept);
        this.delayedMessages.clear();
    }

    public boolean removeDelayedMessage(MessageSignatureData signature) {
        return this.delayedMessages.removeIf(message -> signature.equals(message.signature()));
    }

    private boolean shouldDelay() {
        return this.chatDelay > 0L && Util.getMeasuringTimeMs() < this.lastProcessTime + this.chatDelay;
    }

    private void process(@Nullable MessageSignatureData signature, BooleanSupplier processor) {
        if (this.shouldDelay()) {
            this.delayedMessages.add(new ProcessableMessage(signature, processor));
        } else {
            processor.getAsBoolean();
        }
    }

    public void onChatMessage(SignedMessage message, GameProfile sender, MessageType.Parameters params) {
        boolean bl = this.client.options.getOnlyShowSecureChat().getValue();
        SignedMessage lv = bl ? message.withoutUnsigned() : message;
        Text lv2 = params.applyChatDecoration(lv.getContent());
        Instant instant = Instant.now();
        this.process(message.signature(), () -> {
            boolean bl2 = this.processChatMessageInternal(params, message, lv2, sender, bl, instant);
            ClientPlayNetworkHandler lv = this.client.getNetworkHandler();
            if (lv != null) {
                lv.acknowledge(message, bl2);
            }
            return bl2;
        });
    }

    public void onUnverifiedMessage(UUID sender, MessageType.Parameters parameters) {
        this.process(null, () -> {
            if (this.client.shouldBlockMessages(sender)) {
                return false;
            }
            Text lv = parameters.applyChatDecoration(VALIDATION_ERROR_TEXT);
            this.client.inGameHud.getChatHud().addMessage(lv, null, MessageIndicator.chatError());
            this.lastProcessTime = Util.getMeasuringTimeMs();
            return true;
        });
    }

    public void onProfilelessMessage(Text content, MessageType.Parameters params) {
        Instant instant = Instant.now();
        this.process(null, () -> {
            Text lv = params.applyChatDecoration(content);
            this.client.inGameHud.getChatHud().addMessage(lv);
            this.narrate(params, content);
            this.addToChatLog(lv, instant);
            this.lastProcessTime = Util.getMeasuringTimeMs();
            return true;
        });
    }

    private boolean processChatMessageInternal(MessageType.Parameters params, SignedMessage message, Text decorated, GameProfile sender, boolean onlyShowSecureChat, Instant receptionTimestamp) {
        MessageTrustStatus lv = this.getStatus(message, decorated, receptionTimestamp);
        if (onlyShowSecureChat && lv.isInsecure()) {
            return false;
        }
        if (this.client.shouldBlockMessages(message.getSender()) || message.isFullyFiltered()) {
            return false;
        }
        MessageIndicator lv2 = lv.createIndicator(message);
        MessageSignatureData lv3 = message.signature();
        FilterMask lv4 = message.filterMask();
        if (lv4.isPassThrough()) {
            this.client.inGameHud.getChatHud().addMessage(decorated, lv3, lv2);
            this.narrate(params, message.getContent());
        } else {
            Text lv5 = lv4.getFilteredText(message.getSignedContent());
            if (lv5 != null) {
                this.client.inGameHud.getChatHud().addMessage(params.applyChatDecoration(lv5), lv3, lv2);
                this.narrate(params, lv5);
            }
        }
        this.addToChatLog(message, params, sender, lv);
        this.lastProcessTime = Util.getMeasuringTimeMs();
        return true;
    }

    private void narrate(MessageType.Parameters params, Text message) {
        this.client.getNarratorManager().narrateChatMessage(params.applyNarrationDecoration(message));
    }

    private MessageTrustStatus getStatus(SignedMessage message, Text decorated, Instant receptionTimestamp) {
        if (this.isAlwaysTrusted(message.getSender())) {
            return MessageTrustStatus.SECURE;
        }
        return MessageTrustStatus.getStatus(message, decorated, receptionTimestamp);
    }

    private void addToChatLog(SignedMessage message, MessageType.Parameters params, GameProfile sender, MessageTrustStatus trustStatus) {
        ChatLog lv = this.client.getAbuseReportContext().getChatLog();
        lv.add(ReceivedMessage.of(sender, message, trustStatus));
    }

    private void addToChatLog(Text message, Instant timestamp) {
        ChatLog lv = this.client.getAbuseReportContext().getChatLog();
        lv.add(ReceivedMessage.of(message, timestamp));
    }

    public void onGameMessage(Text message, boolean overlay) {
        if (this.client.options.getHideMatchedNames().getValue().booleanValue() && this.client.shouldBlockMessages(this.extractSender(message))) {
            return;
        }
        if (overlay) {
            this.client.inGameHud.setOverlayMessage(message, false);
        } else {
            this.client.inGameHud.getChatHud().addMessage(message);
            this.addToChatLog(message, Instant.now());
        }
        this.client.getNarratorManager().narrateSystemMessage(message);
    }

    private UUID extractSender(Text text) {
        String string = TextVisitFactory.removeFormattingCodes(text);
        String string2 = StringUtils.substringBetween(string, "<", ">");
        if (string2 == null) {
            return Util.NIL_UUID;
        }
        return this.client.getSocialInteractionsManager().getUuid(string2);
    }

    private boolean isAlwaysTrusted(UUID sender) {
        if (this.client.isInSingleplayer() && this.client.player != null) {
            UUID uUID2 = this.client.player.getGameProfile().getId();
            return uUID2.equals(sender);
        }
        return false;
    }

    @Environment(value=EnvType.CLIENT)
    record ProcessableMessage(@Nullable MessageSignatureData signature, BooleanSupplier handler) {
        public boolean accept() {
            return this.handler.getAsBoolean();
        }

        @Nullable
        public MessageSignatureData signature() {
            return this.signature;
        }
    }
}

