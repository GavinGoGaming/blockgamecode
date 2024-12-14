/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.event.listener;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.event.Vibrations;
import net.minecraft.world.event.listener.Vibration;
import org.apache.commons.lang3.tuple.Pair;

public class VibrationSelector {
    public static final Codec<VibrationSelector> CODEC = RecordCodecBuilder.create(instance -> instance.group(Vibration.CODEC.lenientOptionalFieldOf("event").forGetter(arg -> arg.current.map(Pair::getLeft)), ((MapCodec)Codec.LONG.fieldOf("tick")).forGetter(arg -> arg.current.map(Pair::getRight).orElse(-1L))).apply((Applicative<VibrationSelector, ?>)instance, VibrationSelector::new));
    private Optional<Pair<Vibration, Long>> current;

    public VibrationSelector(Optional<Vibration> vibration, long tick) {
        this.current = vibration.map(vibration2 -> Pair.of(vibration2, tick));
    }

    public VibrationSelector() {
        this.current = Optional.empty();
    }

    public void tryAccept(Vibration vibration, long tick) {
        if (this.shouldSelect(vibration, tick)) {
            this.current = Optional.of(Pair.of(vibration, tick));
        }
    }

    private boolean shouldSelect(Vibration vibration, long tick) {
        if (this.current.isEmpty()) {
            return true;
        }
        Pair<Vibration, Long> pair = this.current.get();
        long m = pair.getRight();
        if (tick != m) {
            return false;
        }
        Vibration lv = pair.getLeft();
        if (vibration.distance() < lv.distance()) {
            return true;
        }
        if (vibration.distance() > lv.distance()) {
            return false;
        }
        return Vibrations.getFrequency(vibration.gameEvent()) > Vibrations.getFrequency(lv.gameEvent());
    }

    public Optional<Vibration> getVibrationToTick(long currentTick) {
        if (this.current.isEmpty()) {
            return Optional.empty();
        }
        if (this.current.get().getRight() < currentTick) {
            return Optional.of(this.current.get().getLeft());
        }
        return Optional.empty();
    }

    public void clear() {
        this.current = Optional.empty();
    }
}

