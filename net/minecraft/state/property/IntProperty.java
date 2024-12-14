/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.state.property;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import net.minecraft.state.property.Property;

public class IntProperty
extends Property<Integer> {
    private final ImmutableSet<Integer> values;
    private final int min;
    private final int max;

    protected IntProperty(String name, int min, int max) {
        super(name, Integer.class);
        if (min < 0) {
            throw new IllegalArgumentException("Min value of " + name + " must be 0 or greater");
        }
        if (max <= min) {
            throw new IllegalArgumentException("Max value of " + name + " must be greater than min (" + min + ")");
        }
        this.min = min;
        this.max = max;
        HashSet<Integer> set = Sets.newHashSet();
        for (int k = min; k <= max; ++k) {
            set.add(k);
        }
        this.values = ImmutableSet.copyOf(set);
    }

    @Override
    public Collection<Integer> getValues() {
        return this.values;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof IntProperty) {
            IntProperty lv = (IntProperty)object;
            if (super.equals(object)) {
                return this.values.equals(lv.values);
            }
        }
        return false;
    }

    @Override
    public int computeHashCode() {
        return 31 * super.computeHashCode() + this.values.hashCode();
    }

    public static IntProperty of(String name, int min, int max) {
        return new IntProperty(name, min, max);
    }

    @Override
    public Optional<Integer> parse(String name) {
        try {
            Integer integer = Integer.valueOf(name);
            return integer >= this.min && integer <= this.max ? Optional.of(integer) : Optional.empty();
        } catch (NumberFormatException numberFormatException) {
            return Optional.empty();
        }
    }

    @Override
    public String name(Integer integer) {
        return integer.toString();
    }
}

