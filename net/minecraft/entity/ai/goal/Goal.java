/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.util.math.MathHelper;

public abstract class Goal {
    private final EnumSet<Control> controls = EnumSet.noneOf(Control.class);

    public abstract boolean canStart();

    public boolean shouldContinue() {
        return this.canStart();
    }

    public boolean canStop() {
        return true;
    }

    public void start() {
    }

    public void stop() {
    }

    public boolean shouldRunEveryTick() {
        return false;
    }

    public void tick() {
    }

    public void setControls(EnumSet<Control> controls) {
        this.controls.clear();
        this.controls.addAll(controls);
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }

    public EnumSet<Control> getControls() {
        return this.controls;
    }

    protected int getTickCount(int ticks) {
        return this.shouldRunEveryTick() ? ticks : Goal.toGoalTicks(ticks);
    }

    protected static int toGoalTicks(int serverTicks) {
        return MathHelper.ceilDiv(serverTicks, 2);
    }

    public static enum Control {
        MOVE,
        LOOK,
        JUMP,
        TARGET;

    }
}

