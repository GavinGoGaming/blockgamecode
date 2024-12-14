/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.potion;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.resource.featuretoggle.ToggleableFeature;
import org.jetbrains.annotations.Nullable;

public class Potion
implements ToggleableFeature {
    public static final Codec<RegistryEntry<Potion>> CODEC = Registries.POTION.getEntryCodec();
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<Potion>> PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.POTION);
    @Nullable
    private final String baseName;
    private final List<StatusEffectInstance> effects;
    private FeatureSet requiredFeatures = FeatureFlags.VANILLA_FEATURES;

    public Potion(StatusEffectInstance ... effects) {
        this((String)null, effects);
    }

    public Potion(@Nullable String baseName, StatusEffectInstance ... effects) {
        this.baseName = baseName;
        this.effects = List.of(effects);
    }

    public Potion requires(FeatureFlag ... requiredFeatures) {
        this.requiredFeatures = FeatureFlags.FEATURE_MANAGER.featureSetOf(requiredFeatures);
        return this;
    }

    @Override
    public FeatureSet getRequiredFeatures() {
        return this.requiredFeatures;
    }

    public static String finishTranslationKey(Optional<RegistryEntry<Potion>> potion, String prefix) {
        String string2;
        if (potion.isPresent() && (string2 = potion.get().value().baseName) != null) {
            return prefix + string2;
        }
        string2 = potion.flatMap(RegistryEntry::getKey).map(key -> key.getValue().getPath()).orElse("empty");
        return prefix + string2;
    }

    public List<StatusEffectInstance> getEffects() {
        return this.effects;
    }

    public boolean hasInstantEffect() {
        if (!this.effects.isEmpty()) {
            for (StatusEffectInstance lv : this.effects) {
                if (!lv.getEffectType().value().isInstant()) continue;
                return true;
            }
        }
        return false;
    }
}

