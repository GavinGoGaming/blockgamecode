/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public record ReplaceDiskEnchantmentEffect(EnchantmentLevelBasedValue radius, EnchantmentLevelBasedValue height, Vec3i offset, Optional<BlockPredicate> predicate, BlockStateProvider blockState, Optional<RegistryEntry<GameEvent>> triggerGameEvent) implements EnchantmentEntityEffect
{
    public static final MapCodec<ReplaceDiskEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)EnchantmentLevelBasedValue.CODEC.fieldOf("radius")).forGetter(ReplaceDiskEnchantmentEffect::radius), ((MapCodec)EnchantmentLevelBasedValue.CODEC.fieldOf("height")).forGetter(ReplaceDiskEnchantmentEffect::height), Vec3i.CODEC.optionalFieldOf("offset", Vec3i.ZERO).forGetter(ReplaceDiskEnchantmentEffect::offset), BlockPredicate.BASE_CODEC.optionalFieldOf("predicate").forGetter(ReplaceDiskEnchantmentEffect::predicate), ((MapCodec)BlockStateProvider.TYPE_CODEC.fieldOf("block_state")).forGetter(ReplaceDiskEnchantmentEffect::blockState), GameEvent.CODEC.optionalFieldOf("trigger_game_event").forGetter(ReplaceDiskEnchantmentEffect::triggerGameEvent)).apply((Applicative<ReplaceDiskEnchantmentEffect, ?>)instance, ReplaceDiskEnchantmentEffect::new));

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        BlockPos lv = BlockPos.ofFloored(pos).add(this.offset);
        Random lv2 = user.getRandom();
        int j = (int)this.radius.getValue(level);
        int k = (int)this.height.getValue(level);
        for (BlockPos lv3 : BlockPos.iterate(lv.add(-j, 0, -j), lv.add(j, Math.min(k - 1, 0), j))) {
            if (!(lv3.getSquaredDistanceFromCenter(pos.getX(), (double)lv3.getY() + 0.5, pos.getZ()) < (double)MathHelper.square(j)) || !this.predicate.map(predicate -> predicate.test(world, lv3)).orElse(true).booleanValue() || !world.setBlockState(lv3, this.blockState.get(lv2, lv3))) continue;
            this.triggerGameEvent.ifPresent(gameEvent -> world.emitGameEvent(user, (RegistryEntry<GameEvent>)gameEvent, lv3));
        }
    }

    public MapCodec<ReplaceDiskEnchantmentEffect> getCodec() {
        return CODEC;
    }
}

