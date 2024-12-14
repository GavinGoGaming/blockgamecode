/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.session.report;

import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.report.ContextMessageCollector;
import net.minecraft.client.session.report.log.ChatLog;
import net.minecraft.client.session.report.log.ChatLogEntry;
import net.minecraft.client.session.report.log.ReceivedMessage;
import net.minecraft.network.message.MessageLink;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class MessagesListAdder {
    private final ChatLog log;
    private final ContextMessageCollector contextMessageCollector;
    private final Predicate<ReceivedMessage.ChatMessage> reportablePredicate;
    @Nullable
    private MessageLink link = null;
    private int maxLogIndex;
    private int foldedMessageCount;
    @Nullable
    private SignedMessage lastMessage;

    public MessagesListAdder(AbuseReportContext context, Predicate<ReceivedMessage.ChatMessage> reportablePredicate) {
        this.log = context.getChatLog();
        this.contextMessageCollector = new ContextMessageCollector(context.getSender().getLimits().leadingContextMessageCount());
        this.reportablePredicate = reportablePredicate;
        this.maxLogIndex = this.log.getMaxIndex();
    }

    public void add(int minAmount, MessagesList messages) {
        ChatLogEntry lv;
        int j = 0;
        while (j < minAmount && (lv = this.log.get(this.maxLogIndex)) != null) {
            ReceivedMessage.ChatMessage lv2;
            int k = this.maxLogIndex--;
            if (!(lv instanceof ReceivedMessage.ChatMessage) || (lv2 = (ReceivedMessage.ChatMessage)lv).message().equals(this.lastMessage)) continue;
            if (this.tryAdd(messages, lv2)) {
                if (this.foldedMessageCount > 0) {
                    messages.addText(Text.translatable("gui.chatSelection.fold", this.foldedMessageCount));
                    this.foldedMessageCount = 0;
                }
                messages.addMessage(k, lv2);
                ++j;
            } else {
                ++this.foldedMessageCount;
            }
            this.lastMessage = lv2.message();
        }
    }

    private boolean tryAdd(MessagesList messages, ReceivedMessage.ChatMessage message) {
        SignedMessage lv = message.message();
        boolean bl = this.contextMessageCollector.tryLink(lv);
        if (this.reportablePredicate.test(message)) {
            this.contextMessageCollector.add(lv);
            if (this.link != null && !this.link.linksTo(lv.link())) {
                messages.addText(Text.translatable("gui.chatSelection.join", message.profile().getName()).formatted(Formatting.YELLOW));
            }
            this.link = lv.link();
            return true;
        }
        return bl;
    }

    @Environment(value=EnvType.CLIENT)
    public static interface MessagesList {
        public void addMessage(int var1, ReceivedMessage.ChatMessage var2);

        public void addText(Text var1);
    }
}

