/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math;

import net.minecraft.util.math.Direction;

public class RotationCalculator {
    private final int max;
    private final int precision;
    private final float rotationPerDegrees;
    private final float degreesPerRotation;

    public RotationCalculator(int precision) {
        if (precision < 2) {
            throw new IllegalArgumentException("Precision cannot be less than 2 bits");
        }
        if (precision > 30) {
            throw new IllegalArgumentException("Precision cannot be greater than 30 bits");
        }
        int j = 1 << precision;
        this.max = j - 1;
        this.precision = precision;
        this.rotationPerDegrees = (float)j / 360.0f;
        this.degreesPerRotation = 360.0f / (float)j;
    }

    public boolean areRotationsParallel(int alpha, int beta) {
        int k = this.getMax() >> 1;
        return (alpha & k) == (beta & k);
    }

    public int toRotation(Direction direction) {
        if (direction.getAxis().isVertical()) {
            return 0;
        }
        int i = direction.getHorizontal();
        return i << this.precision - 2;
    }

    public int toRotation(float degrees) {
        return Math.round(degrees * this.rotationPerDegrees);
    }

    public int toClampedRotation(float degrees) {
        return this.clamp(this.toRotation(degrees));
    }

    public float toDegrees(int rotation) {
        return (float)rotation * this.degreesPerRotation;
    }

    public float toWrappedDegrees(int rotation) {
        float f = this.toDegrees(this.clamp(rotation));
        return f >= 180.0f ? f - 360.0f : f;
    }

    public int clamp(int rotationBits) {
        return rotationBits & this.max;
    }

    public int getMax() {
        return this.max;
    }
}

