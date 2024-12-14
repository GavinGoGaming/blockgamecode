/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.session.telemetry;

import com.mojang.authlib.minecraft.TelemetryEvent;
import com.mojang.authlib.minecraft.TelemetrySession;
import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.session.telemetry.PropertyMap;
import net.minecraft.client.session.telemetry.TelemetryEventType;

@Environment(value=EnvType.CLIENT)
public record SentTelemetryEvent(TelemetryEventType type, PropertyMap properties) {
    public static final Codec<SentTelemetryEvent> CODEC = TelemetryEventType.CODEC.dispatchStable(SentTelemetryEvent::type, TelemetryEventType::getCodec);

    public SentTelemetryEvent {
        arg2.keySet().forEach(property -> {
            if (!arg.hasProperty(property)) {
                throw new IllegalArgumentException("Property '" + property.id() + "' not expected for event: '" + arg.getId() + "'");
            }
        });
    }

    public TelemetryEvent createEvent(TelemetrySession session) {
        return this.type.createEvent(session, this.properties);
    }
}

