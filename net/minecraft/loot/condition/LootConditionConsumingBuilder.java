/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.condition;

import java.util.function.Function;
import net.minecraft.loot.condition.LootCondition;

public interface LootConditionConsumingBuilder<T extends LootConditionConsumingBuilder<T>> {
    public T conditionally(LootCondition.Builder var1);

    default public <E> T conditionally(Iterable<E> conditions, Function<E, LootCondition.Builder> toBuilderFunction) {
        T lv = this.getThisConditionConsumingBuilder();
        for (E object : conditions) {
            lv = lv.conditionally(toBuilderFunction.apply(object));
        }
        return lv;
    }

    public T getThisConditionConsumingBuilder();
}

