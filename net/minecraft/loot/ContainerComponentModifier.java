/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;

public interface ContainerComponentModifier<T> {
    public ComponentType<T> getComponentType();

    public T getDefault();

    public T create(T var1, Stream<ItemStack> var2);

    public Stream<ItemStack> stream(T var1);

    default public void apply(ItemStack stack, T component, Stream<ItemStack> contents) {
        T object2 = stack.getOrDefault(this.getComponentType(), component);
        T object3 = this.create(object2, contents);
        stack.set(this.getComponentType(), object3);
    }

    default public void apply(ItemStack stack, Stream<ItemStack> contents) {
        this.apply(stack, this.getDefault(), contents);
    }

    default public void apply(ItemStack stack, UnaryOperator<ItemStack> contentsOperator) {
        T object = stack.get(this.getComponentType());
        if (object != null) {
            UnaryOperator unaryOperator2 = contentStack -> {
                if (contentStack.isEmpty()) {
                    return contentStack;
                }
                ItemStack lv = (ItemStack)contentsOperator.apply((ItemStack)contentStack);
                lv.capCount(lv.getMaxCount());
                return lv;
            };
            this.apply(stack, this.stream(object).map(unaryOperator2));
        }
    }
}

