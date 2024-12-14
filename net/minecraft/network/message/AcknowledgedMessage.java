/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.message;

import net.minecraft.network.message.MessageSignatureData;

public record AcknowledgedMessage(MessageSignatureData signature, boolean pending) {
    public AcknowledgedMessage unmarkAsPending() {
        return this.pending ? new AcknowledgedMessage(this.signature, false) : this;
    }
}

