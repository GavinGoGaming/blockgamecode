/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import net.minecraft.world.WorldView;
import net.minecraft.world.dimension.DimensionType;

public interface LunarWorldView
extends WorldView {
    public long getLunarTime();

    default public float getMoonSize() {
        return DimensionType.MOON_SIZES[this.getDimension().getMoonPhase(this.getLunarTime())];
    }

    default public float getSkyAngle(float tickDelta) {
        return this.getDimension().getSkyAngle(this.getLunarTime());
    }

    default public int getMoonPhase() {
        return this.getDimension().getMoonPhase(this.getLunarTime());
    }
}

