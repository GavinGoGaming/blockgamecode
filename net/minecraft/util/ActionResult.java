/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

public enum ActionResult {
    SUCCESS,
    SUCCESS_NO_ITEM_USED,
    CONSUME,
    CONSUME_PARTIAL,
    PASS,
    FAIL;


    public boolean isAccepted() {
        return this == SUCCESS || this == CONSUME || this == CONSUME_PARTIAL || this == SUCCESS_NO_ITEM_USED;
    }

    public boolean shouldSwingHand() {
        return this == SUCCESS || this == SUCCESS_NO_ITEM_USED;
    }

    public boolean shouldIncrementStat() {
        return this == SUCCESS || this == CONSUME;
    }

    public static ActionResult success(boolean swingHand) {
        return swingHand ? SUCCESS : CONSUME;
    }
}

