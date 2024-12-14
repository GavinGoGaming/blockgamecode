/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.message;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.BitSet;
import java.util.Objects;
import net.minecraft.network.message.AcknowledgedMessage;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.message.MessageSignatureData;
import org.jetbrains.annotations.Nullable;

public class LastSeenMessagesCollector {
    private final AcknowledgedMessage[] acknowledgedMessages;
    private int nextIndex;
    private int messageCount;
    @Nullable
    private MessageSignatureData lastAdded;

    public LastSeenMessagesCollector(int size) {
        this.acknowledgedMessages = new AcknowledgedMessage[size];
    }

    public boolean add(MessageSignatureData signature, boolean displayed) {
        if (Objects.equals(signature, this.lastAdded)) {
            return false;
        }
        this.lastAdded = signature;
        this.add(displayed ? new AcknowledgedMessage(signature, true) : null);
        return true;
    }

    private void add(@Nullable AcknowledgedMessage message) {
        int i = this.nextIndex;
        this.nextIndex = (i + 1) % this.acknowledgedMessages.length;
        ++this.messageCount;
        this.acknowledgedMessages[i] = message;
    }

    public void remove(MessageSignatureData signature) {
        for (int i = 0; i < this.acknowledgedMessages.length; ++i) {
            AcknowledgedMessage lv = this.acknowledgedMessages[i];
            if (lv == null || !lv.pending() || !signature.equals(lv.signature())) continue;
            this.acknowledgedMessages[i] = null;
            break;
        }
    }

    public int resetMessageCount() {
        int i = this.messageCount;
        this.messageCount = 0;
        return i;
    }

    public LastSeenMessages collect() {
        int i = this.resetMessageCount();
        BitSet bitSet = new BitSet(this.acknowledgedMessages.length);
        ObjectArrayList<MessageSignatureData> objectList = new ObjectArrayList<MessageSignatureData>(this.acknowledgedMessages.length);
        for (int j = 0; j < this.acknowledgedMessages.length; ++j) {
            int k = (this.nextIndex + j) % this.acknowledgedMessages.length;
            AcknowledgedMessage lv = this.acknowledgedMessages[k];
            if (lv == null) continue;
            bitSet.set(j, true);
            objectList.add(lv.signature());
            this.acknowledgedMessages[k] = lv.unmarkAsPending();
        }
        LastSeenMessageList lv2 = new LastSeenMessageList(objectList);
        LastSeenMessageList.Acknowledgment lv3 = new LastSeenMessageList.Acknowledgment(i, bitSet);
        return new LastSeenMessages(lv2, lv3);
    }

    public int getMessageCount() {
        return this.messageCount;
    }

    public record LastSeenMessages(LastSeenMessageList lastSeen, LastSeenMessageList.Acknowledgment update) {
    }
}

