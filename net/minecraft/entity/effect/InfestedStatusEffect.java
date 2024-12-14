/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.effect;

import java.util.function.ToIntFunction;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.joml.Vector3f;

class InfestedStatusEffect
extends StatusEffect {
    private final float silverfishChance;
    private final ToIntFunction<Random> silverfishCountFunction;

    protected InfestedStatusEffect(StatusEffectCategory category, int color, float silverfishChance, ToIntFunction<Random> silverfishCountFunction) {
        super(category, color, ParticleTypes.INFESTED);
        this.silverfishChance = silverfishChance;
        this.silverfishCountFunction = silverfishCountFunction;
    }

    @Override
    public void onEntityDamage(LivingEntity entity, int amplifier, DamageSource source, float amount) {
        if (entity.getRandom().nextFloat() <= this.silverfishChance) {
            int j = this.silverfishCountFunction.applyAsInt(entity.getRandom());
            for (int k = 0; k < j; ++k) {
                this.spawnSilverfish(entity.getWorld(), entity, entity.getX(), entity.getY() + (double)entity.getHeight() / 2.0, entity.getZ());
            }
        }
    }

    private void spawnSilverfish(World world, LivingEntity entity, double x, double y, double z) {
        SilverfishEntity lv = EntityType.SILVERFISH.create(world);
        if (lv == null) {
            return;
        }
        Random lv2 = entity.getRandom();
        float g = 1.5707964f;
        float h = MathHelper.nextBetween(lv2, -1.5707964f, 1.5707964f);
        Vector3f vector3f = entity.getRotationVector().toVector3f().mul(0.3f).mul(1.0f, 1.5f, 1.0f).rotateY(h);
        lv.refreshPositionAndAngles(x, y, z, world.getRandom().nextFloat() * 360.0f, 0.0f);
        lv.setVelocity(new Vec3d(vector3f));
        world.spawnEntity(lv);
        lv.playSoundIfNotSilent(SoundEvents.ENTITY_SILVERFISH_HURT);
    }
}

