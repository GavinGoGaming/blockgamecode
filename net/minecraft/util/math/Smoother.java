/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math;

import net.minecraft.util.math.MathHelper;

public class Smoother {
    private double actualSum;
    private double smoothedSum;
    private double movementLatency;

    public double smooth(double original, double smoother) {
        this.actualSum += original;
        double f = this.actualSum - this.smoothedSum;
        double g = MathHelper.lerp(0.5, this.movementLatency, f);
        double h = Math.signum(f);
        if (h * f > h * this.movementLatency) {
            f = g;
        }
        this.movementLatency = g;
        this.smoothedSum += f * smoother;
        return f * smoother;
    }

    public void clear() {
        this.actualSum = 0.0;
        this.smoothedSum = 0.0;
        this.movementLatency = 0.0;
    }
}

