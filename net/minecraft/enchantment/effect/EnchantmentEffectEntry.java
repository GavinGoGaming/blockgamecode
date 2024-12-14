/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.util.ErrorReporter;

public record EnchantmentEffectEntry<T>(T effect, Optional<LootCondition> requirements) {
    public static Codec<LootCondition> createRequirementsCodec(LootContextType lootContextType) {
        return LootCondition.CODEC.validate(condition -> {
            ErrorReporter.Impl lv = new ErrorReporter.Impl();
            LootTableReporter lv2 = new LootTableReporter(lv, lootContextType);
            condition.validate(lv2);
            return lv.getErrorsAsString().map(errors -> DataResult.error(() -> "Validation error in enchantment effect condition: " + errors)).orElseGet(() -> DataResult.success(condition));
        });
    }

    public static <T> Codec<EnchantmentEffectEntry<T>> createCodec(Codec<T> effectCodec, LootContextType lootContextType) {
        return RecordCodecBuilder.create(instance -> instance.group(((MapCodec)effectCodec.fieldOf("effect")).forGetter(EnchantmentEffectEntry::effect), EnchantmentEffectEntry.createRequirementsCodec(lootContextType).optionalFieldOf("requirements").forGetter(EnchantmentEffectEntry::requirements)).apply((Applicative<EnchantmentEffectEntry, ?>)instance, EnchantmentEffectEntry::new));
    }

    public boolean test(LootContext context) {
        if (this.requirements.isEmpty()) {
            return true;
        }
        return this.requirements.get().test(context);
    }
}

