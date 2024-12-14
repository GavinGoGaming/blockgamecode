/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class DamageUtil {
    public static final float field_29962 = 20.0f;
    public static final float field_29963 = 25.0f;
    public static final float field_29964 = 2.0f;
    public static final float field_29965 = 0.2f;
    private static final int field_29966 = 4;

    public static float getDamageLeft(LivingEntity armorWearer, float damageAmount, DamageSource damageSource, float armor, float armorToughness) {
        float l;
        World world;
        float i = 2.0f + armorToughness / 4.0f;
        float j = MathHelper.clamp(armor - damageAmount / i, armor * 0.2f, 20.0f);
        float k = j / 25.0f;
        ItemStack lv = damageSource.getWeaponStack();
        if (lv != null && (world = armorWearer.getWorld()) instanceof ServerWorld) {
            ServerWorld lv2 = (ServerWorld)world;
            l = MathHelper.clamp(EnchantmentHelper.getArmorEffectiveness(lv2, lv, armorWearer, damageSource, k), 0.0f, 1.0f);
        } else {
            l = k;
        }
        float m = 1.0f - l;
        return damageAmount * m;
    }

    public static float getInflictedDamage(float damageDealt, float protection) {
        float h = MathHelper.clamp(protection, 0.0f, 20.0f);
        return damageDealt * (1.0f - h / 25.0f);
    }
}

