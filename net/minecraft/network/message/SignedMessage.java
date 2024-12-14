/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.message;

import com.google.common.primitives.Ints;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.SignatureException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.encryption.SignatureUpdatable;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.network.message.FilterMask;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageLink;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public record SignedMessage(MessageLink link, @Nullable MessageSignatureData signature, MessageBody signedBody, @Nullable Text unsignedContent, FilterMask filterMask) {
    public static final MapCodec<SignedMessage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)MessageLink.CODEC.fieldOf("link")).forGetter(SignedMessage::link), MessageSignatureData.CODEC.optionalFieldOf("signature").forGetter(message -> Optional.ofNullable(message.signature)), MessageBody.CODEC.forGetter(SignedMessage::signedBody), TextCodecs.CODEC.optionalFieldOf("unsigned_content").forGetter(message -> Optional.ofNullable(message.unsignedContent)), FilterMask.CODEC.optionalFieldOf("filter_mask", FilterMask.PASS_THROUGH).forGetter(SignedMessage::filterMask)).apply((Applicative<SignedMessage, ?>)instance, (link, signature, signedBody, unsignedContent, filterMask) -> new SignedMessage((MessageLink)link, signature.orElse(null), (MessageBody)signedBody, unsignedContent.orElse(null), (FilterMask)filterMask)));
    private static final UUID NIL_UUID = Util.NIL_UUID;
    public static final Duration SERVERBOUND_TIME_TO_LIVE = Duration.ofMinutes(5L);
    public static final Duration CLIENTBOUND_TIME_TO_LIVE = SERVERBOUND_TIME_TO_LIVE.plus(Duration.ofMinutes(2L));

    public static SignedMessage ofUnsigned(String content) {
        return SignedMessage.ofUnsigned(NIL_UUID, content);
    }

    public static SignedMessage ofUnsigned(UUID sender, String content) {
        MessageBody lv = MessageBody.ofUnsigned(content);
        MessageLink lv2 = MessageLink.of(sender);
        return new SignedMessage(lv2, null, lv, null, FilterMask.PASS_THROUGH);
    }

    public SignedMessage withUnsignedContent(Text unsignedContent) {
        Text lv = !unsignedContent.equals(Text.literal(this.getSignedContent())) ? unsignedContent : null;
        return new SignedMessage(this.link, this.signature, this.signedBody, lv, this.filterMask);
    }

    public SignedMessage withoutUnsigned() {
        if (this.unsignedContent != null) {
            return new SignedMessage(this.link, this.signature, this.signedBody, null, this.filterMask);
        }
        return this;
    }

    public SignedMessage withFilterMask(FilterMask filterMask) {
        if (this.filterMask.equals(filterMask)) {
            return this;
        }
        return new SignedMessage(this.link, this.signature, this.signedBody, this.unsignedContent, filterMask);
    }

    public SignedMessage withFilterMaskEnabled(boolean enabled) {
        return this.withFilterMask(enabled ? this.filterMask : FilterMask.PASS_THROUGH);
    }

    public SignedMessage stripSignature() {
        MessageBody lv = MessageBody.ofUnsigned(this.getSignedContent());
        MessageLink lv2 = MessageLink.of(this.getSender());
        return new SignedMessage(lv2, null, lv, this.unsignedContent, this.filterMask);
    }

    public static void update(SignatureUpdatable.SignatureUpdater updater, MessageLink link, MessageBody body) throws SignatureException {
        updater.update(Ints.toByteArray(1));
        link.update(updater);
        body.update(updater);
    }

    public boolean verify(SignatureVerifier verifier) {
        return this.signature != null && this.signature.verify(verifier, updater -> SignedMessage.update(updater, this.link, this.signedBody));
    }

    public String getSignedContent() {
        return this.signedBody.content();
    }

    public Text getContent() {
        return Objects.requireNonNullElseGet(this.unsignedContent, () -> Text.literal(this.getSignedContent()));
    }

    public Instant getTimestamp() {
        return this.signedBody.timestamp();
    }

    public long getSalt() {
        return this.signedBody.salt();
    }

    public boolean isExpiredOnServer(Instant currentTime) {
        return currentTime.isAfter(this.getTimestamp().plus(SERVERBOUND_TIME_TO_LIVE));
    }

    public boolean isExpiredOnClient(Instant currentTime) {
        return currentTime.isAfter(this.getTimestamp().plus(CLIENTBOUND_TIME_TO_LIVE));
    }

    public UUID getSender() {
        return this.link.sender();
    }

    public boolean isSenderMissing() {
        return this.getSender().equals(NIL_UUID);
    }

    public boolean hasSignature() {
        return this.signature != null;
    }

    public boolean canVerifyFrom(UUID sender) {
        return this.hasSignature() && this.link.sender().equals(sender);
    }

    public boolean isFullyFiltered() {
        return this.filterMask.isFullyFiltered();
    }

    @Nullable
    public MessageSignatureData signature() {
        return this.signature;
    }

    @Nullable
    public Text unsignedContent() {
        return this.unsignedContent;
    }
}

