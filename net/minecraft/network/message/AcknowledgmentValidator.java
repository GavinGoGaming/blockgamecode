/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.message;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Optional;
import net.minecraft.network.message.AcknowledgedMessage;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.message.MessageSignatureData;
import org.jetbrains.annotations.Nullable;

public class AcknowledgmentValidator {
    private final int size;
    private final ObjectList<AcknowledgedMessage> messages = new ObjectArrayList<AcknowledgedMessage>();
    @Nullable
    private MessageSignatureData lastSignature;

    public AcknowledgmentValidator(int size) {
        this.size = size;
        for (int j = 0; j < size; ++j) {
            this.messages.add(null);
        }
    }

    public void addPending(MessageSignatureData signature) {
        if (!signature.equals(this.lastSignature)) {
            this.messages.add(new AcknowledgedMessage(signature, true));
            this.lastSignature = signature;
        }
    }

    public int getMessageCount() {
        return this.messages.size();
    }

    public boolean removeUntil(int index) {
        int j = this.messages.size() - this.size;
        if (index >= 0 && index <= j) {
            this.messages.removeElements(0, index);
            return true;
        }
        return false;
    }

    public Optional<LastSeenMessageList> validate(LastSeenMessageList.Acknowledgment acknowledgment) {
        if (!this.removeUntil(acknowledgment.offset())) {
            return Optional.empty();
        }
        ObjectArrayList<MessageSignatureData> objectList = new ObjectArrayList<MessageSignatureData>(acknowledgment.acknowledged().cardinality());
        if (acknowledgment.acknowledged().length() > this.size) {
            return Optional.empty();
        }
        for (int i = 0; i < this.size; ++i) {
            boolean bl = acknowledgment.acknowledged().get(i);
            AcknowledgedMessage lv = (AcknowledgedMessage)this.messages.get(i);
            if (bl) {
                if (lv == null) {
                    return Optional.empty();
                }
                this.messages.set(i, lv.unmarkAsPending());
                objectList.add(lv.signature());
                continue;
            }
            if (lv != null && !lv.pending()) {
                return Optional.empty();
            }
            this.messages.set(i, null);
        }
        return Optional.of(new LastSeenMessageList(objectList));
    }
}

