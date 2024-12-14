/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.attribute;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class DefaultAttributeContainer {
    private final Map<RegistryEntry<EntityAttribute>, EntityAttributeInstance> instances;

    DefaultAttributeContainer(Map<RegistryEntry<EntityAttribute>, EntityAttributeInstance> instances) {
        this.instances = instances;
    }

    private EntityAttributeInstance require(RegistryEntry<EntityAttribute> attribute) {
        EntityAttributeInstance lv = this.instances.get(attribute);
        if (lv == null) {
            throw new IllegalArgumentException("Can't find attribute " + attribute.getIdAsString());
        }
        return lv;
    }

    public double getValue(RegistryEntry<EntityAttribute> attribute) {
        return this.require(attribute).getValue();
    }

    public double getBaseValue(RegistryEntry<EntityAttribute> attribute) {
        return this.require(attribute).getBaseValue();
    }

    public double getModifierValue(RegistryEntry<EntityAttribute> attribute, Identifier id) {
        EntityAttributeModifier lv = this.require(attribute).getModifier(id);
        if (lv == null) {
            throw new IllegalArgumentException("Can't find modifier " + String.valueOf(id) + " on attribute " + attribute.getIdAsString());
        }
        return lv.value();
    }

    @Nullable
    public EntityAttributeInstance createOverride(Consumer<EntityAttributeInstance> updateCallback, RegistryEntry<EntityAttribute> attribute) {
        EntityAttributeInstance lv = this.instances.get(attribute);
        if (lv == null) {
            return null;
        }
        EntityAttributeInstance lv2 = new EntityAttributeInstance(attribute, updateCallback);
        lv2.setFrom(lv);
        return lv2;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean has(RegistryEntry<EntityAttribute> attribute) {
        return this.instances.containsKey(attribute);
    }

    public boolean hasModifier(RegistryEntry<EntityAttribute> attribute, Identifier id) {
        EntityAttributeInstance lv = this.instances.get(attribute);
        return lv != null && lv.getModifier(id) != null;
    }

    public static class Builder {
        private final ImmutableMap.Builder<RegistryEntry<EntityAttribute>, EntityAttributeInstance> instances = ImmutableMap.builder();
        private boolean unmodifiable;

        private EntityAttributeInstance checkedAdd(RegistryEntry<EntityAttribute> attribute) {
            EntityAttributeInstance lv = new EntityAttributeInstance(attribute, attributex -> {
                if (this.unmodifiable) {
                    throw new UnsupportedOperationException("Tried to change value for default attribute instance: " + attribute.getIdAsString());
                }
            });
            this.instances.put(attribute, lv);
            return lv;
        }

        public Builder add(RegistryEntry<EntityAttribute> attribute) {
            this.checkedAdd(attribute);
            return this;
        }

        public Builder add(RegistryEntry<EntityAttribute> attribute, double baseValue) {
            EntityAttributeInstance lv = this.checkedAdd(attribute);
            lv.setBaseValue(baseValue);
            return this;
        }

        public DefaultAttributeContainer build() {
            this.unmodifiable = true;
            return new DefaultAttributeContainer(this.instances.buildKeepingLast());
        }
    }
}

