/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.attribute;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Formatting;

public class EntityAttribute {
    public static final Codec<RegistryEntry<EntityAttribute>> CODEC = Registries.ATTRIBUTE.getEntryCodec();
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<EntityAttribute>> PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.ATTRIBUTE);
    private final double fallback;
    private boolean tracked;
    private final String translationKey;
    private Category category = Category.POSITIVE;

    protected EntityAttribute(String translationKey, double fallback) {
        this.fallback = fallback;
        this.translationKey = translationKey;
    }

    public double getDefaultValue() {
        return this.fallback;
    }

    public boolean isTracked() {
        return this.tracked;
    }

    public EntityAttribute setTracked(boolean tracked) {
        this.tracked = tracked;
        return this;
    }

    public EntityAttribute setCategory(Category category) {
        this.category = category;
        return this;
    }

    public double clamp(double value) {
        return value;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }

    public Formatting getFormatting(boolean addition) {
        return this.category.getFormatting(addition);
    }

    public static enum Category {
        POSITIVE,
        NEUTRAL,
        NEGATIVE;


        public Formatting getFormatting(boolean addition) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> {
                    if (addition) {
                        yield Formatting.BLUE;
                    }
                    yield Formatting.RED;
                }
                case 1 -> Formatting.GRAY;
                case 2 -> addition ? Formatting.RED : Formatting.BLUE;
            };
        }
    }
}

