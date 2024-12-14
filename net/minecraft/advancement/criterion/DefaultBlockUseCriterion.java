/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.advancement.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.block.BlockState;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.entity.LootContextPredicateValidator;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class DefaultBlockUseCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, BlockPos pos) {
        ServerWorld lv = player.getServerWorld();
        BlockState lv2 = lv.getBlockState(pos);
        LootContextParameterSet lv3 = new LootContextParameterSet.Builder(lv).add(LootContextParameters.ORIGIN, pos.toCenterPos()).add(LootContextParameters.THIS_ENTITY, player).add(LootContextParameters.BLOCK_STATE, lv2).build(LootContextTypes.BLOCK_USE);
        LootContext lv4 = new LootContext.Builder(lv3).build(Optional.empty());
        this.trigger(player, (T conditions) -> conditions.test(lv4));
    }

    public record Conditions(Optional<LootContextPredicate> player, Optional<LootContextPredicate> location) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), LootContextPredicate.CODEC.optionalFieldOf("location").forGetter(Conditions::location)).apply((Applicative<Conditions, ?>)instance, Conditions::new));

        public boolean test(LootContext location) {
            return this.location.isEmpty() || this.location.get().test(location);
        }

        @Override
        public void validate(LootContextPredicateValidator validator) {
            AbstractCriterion.Conditions.super.validate(validator);
            this.location.ifPresent(location -> validator.validate((LootContextPredicate)location, LootContextTypes.BLOCK_USE, ".location"));
        }
    }
}

