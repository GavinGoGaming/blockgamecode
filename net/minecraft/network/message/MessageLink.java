/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.message;

import com.google.common.primitives.Ints;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.SignatureException;
import java.util.UUID;
import net.minecraft.network.encryption.SignatureUpdatable;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;

public record MessageLink(int index, UUID sender, UUID sessionId) {
    public static final Codec<MessageLink> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codecs.NONNEGATIVE_INT.fieldOf("index")).forGetter(MessageLink::index), ((MapCodec)Uuids.INT_STREAM_CODEC.fieldOf("sender")).forGetter(MessageLink::sender), ((MapCodec)Uuids.INT_STREAM_CODEC.fieldOf("session_id")).forGetter(MessageLink::sessionId)).apply((Applicative<MessageLink, ?>)instance, MessageLink::new));

    public static MessageLink of(UUID sender) {
        return MessageLink.of(sender, Util.NIL_UUID);
    }

    public static MessageLink of(UUID sender, UUID sessionId) {
        return new MessageLink(0, sender, sessionId);
    }

    public void update(SignatureUpdatable.SignatureUpdater updater) throws SignatureException {
        updater.update(Uuids.toByteArray(this.sender));
        updater.update(Uuids.toByteArray(this.sessionId));
        updater.update(Ints.toByteArray(this.index));
    }

    public boolean linksTo(MessageLink preceding) {
        return this.index > preceding.index() && this.sender.equals(preceding.sender()) && this.sessionId.equals(preceding.sessionId());
    }

    @Nullable
    public MessageLink next() {
        if (this.index == Integer.MAX_VALUE) {
            return null;
        }
        return new MessageLink(this.index + 1, this.sender, this.sessionId);
    }
}

