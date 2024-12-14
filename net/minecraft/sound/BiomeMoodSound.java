/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.sound;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class BiomeMoodSound {
    public static final Codec<BiomeMoodSound> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)SoundEvent.ENTRY_CODEC.fieldOf("sound")).forGetter(sound -> sound.sound), ((MapCodec)Codec.INT.fieldOf("tick_delay")).forGetter(sound -> sound.cultivationTicks), ((MapCodec)Codec.INT.fieldOf("block_search_extent")).forGetter(sound -> sound.spawnRange), ((MapCodec)Codec.DOUBLE.fieldOf("offset")).forGetter(sound -> sound.extraDistance)).apply((Applicative<BiomeMoodSound, ?>)instance, BiomeMoodSound::new));
    public static final BiomeMoodSound CAVE = new BiomeMoodSound(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0);
    private final RegistryEntry<SoundEvent> sound;
    private final int cultivationTicks;
    private final int spawnRange;
    private final double extraDistance;

    public BiomeMoodSound(RegistryEntry<SoundEvent> sound, int cultivationTicks, int spawnRange, double extraDistance) {
        this.sound = sound;
        this.cultivationTicks = cultivationTicks;
        this.spawnRange = spawnRange;
        this.extraDistance = extraDistance;
    }

    public RegistryEntry<SoundEvent> getSound() {
        return this.sound;
    }

    public int getCultivationTicks() {
        return this.cultivationTicks;
    }

    public int getSpawnRange() {
        return this.spawnRange;
    }

    public double getExtraDistance() {
        return this.extraDistance;
    }
}

