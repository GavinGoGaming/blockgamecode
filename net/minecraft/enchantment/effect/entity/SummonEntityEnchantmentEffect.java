/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public record SummonEntityEnchantmentEffect(RegistryEntryList<EntityType<?>> entityTypes, boolean joinTeam) implements EnchantmentEntityEffect
{
    public static final MapCodec<SummonEntityEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)RegistryCodecs.entryList(RegistryKeys.ENTITY_TYPE).fieldOf("entity")).forGetter(SummonEntityEnchantmentEffect::entityTypes), Codec.BOOL.optionalFieldOf("join_team", false).forGetter(SummonEntityEnchantmentEffect::joinTeam)).apply((Applicative<SummonEntityEnchantmentEffect, ?>)instance, SummonEntityEnchantmentEffect::new));

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        BlockPos lv = BlockPos.ofFloored(pos);
        if (!World.isValid(lv)) {
            return;
        }
        Optional<RegistryEntry<EntityType<?>>> optional = this.entityTypes().getRandom(world.getRandom());
        if (optional.isEmpty()) {
            return;
        }
        Object lv2 = optional.get().value().spawn(world, lv, SpawnReason.TRIGGERED);
        if (lv2 == null) {
            return;
        }
        if (lv2 instanceof LightningEntity) {
            LightningEntity lv3 = (LightningEntity)lv2;
            LivingEntity livingEntity = context.owner();
            if (livingEntity instanceof ServerPlayerEntity) {
                ServerPlayerEntity lv4 = (ServerPlayerEntity)livingEntity;
                lv3.setChanneler(lv4);
            }
        }
        if (this.joinTeam && user.getScoreboardTeam() != null) {
            world.getScoreboard().addScoreHolderToTeam(((Entity)lv2).getNameForScoreboard(), user.getScoreboardTeam());
        }
        ((Entity)lv2).refreshPositionAndAngles(pos.x, pos.y, pos.z, ((Entity)lv2).getYaw(), ((Entity)lv2).getPitch());
    }

    public MapCodec<SummonEntityEnchantmentEffect> getCodec() {
        return CODEC;
    }
}

