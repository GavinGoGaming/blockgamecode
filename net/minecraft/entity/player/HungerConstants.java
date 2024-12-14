/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.player;

public class HungerConstants {
    public static final int FULL_FOOD_LEVEL = 20;
    public static final float field_30705 = 20.0f;
    public static final float INITIAL_SATURATION_LEVEL = 5.0f;
    public static final float field_30707 = 2.5f;
    public static final float EXHAUSTION_UNIT = 4.0f;
    public static final int SLOW_HEALING_STARVING_INTERVAL = 80;
    public static final int FAST_HEALING_INTERVAL = 10;
    public static final int SLOW_HEALING_FOOD_LEVEL = 18;
    public static final int EXHAUSTION_PER_HITPOINT = 6;
    public static final int STARVING_FOOD_LEVEL = 0;
    public static final float field_30714 = 0.1f;
    public static final float field_30715 = 0.3f;
    public static final float field_30716 = 0.6f;
    public static final float field_30717 = 0.8f;
    public static final float field_30718 = 1.0f;
    public static final float field_30719 = 1.2f;
    public static final float field_30720 = 6.0f;
    public static final float field_30721 = 0.05f;
    public static final float field_30722 = 0.2f;
    public static final float field_30723 = 0.005f;
    public static final float field_30724 = 0.1f;
    public static final float field_30726 = 0.0f;
    public static final float field_30727 = 0.0f;
    public static final float field_30728 = 0.1f;
    public static final float field_30729 = 0.01f;

    public static float calculateSaturation(int nutrition, float saturationModifier) {
        return (float)nutrition * saturationModifier * 2.0f;
    }
}

