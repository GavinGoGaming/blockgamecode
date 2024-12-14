/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.attribute;

import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class AttributeContainer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<RegistryEntry<EntityAttribute>, EntityAttributeInstance> custom = new Object2ObjectOpenHashMap<RegistryEntry<EntityAttribute>, EntityAttributeInstance>();
    private final Set<EntityAttributeInstance> tracked = new ObjectOpenHashSet<EntityAttributeInstance>();
    private final Set<EntityAttributeInstance> pendingUpdate = new ObjectOpenHashSet<EntityAttributeInstance>();
    private final DefaultAttributeContainer fallback;

    public AttributeContainer(DefaultAttributeContainer defaultAttributes) {
        this.fallback = defaultAttributes;
    }

    private void updateTrackedStatus(EntityAttributeInstance instance) {
        this.pendingUpdate.add(instance);
        if (instance.getAttribute().value().isTracked()) {
            this.tracked.add(instance);
        }
    }

    public Set<EntityAttributeInstance> getTracked() {
        return this.tracked;
    }

    public Set<EntityAttributeInstance> getPendingUpdate() {
        return this.pendingUpdate;
    }

    public Collection<EntityAttributeInstance> getAttributesToSend() {
        return this.custom.values().stream().filter(attribute -> attribute.getAttribute().value().isTracked()).collect(Collectors.toList());
    }

    @Nullable
    public EntityAttributeInstance getCustomInstance(RegistryEntry<EntityAttribute> attribute2) {
        return this.custom.computeIfAbsent(attribute2, attribute -> this.fallback.createOverride(this::updateTrackedStatus, (RegistryEntry<EntityAttribute>)attribute));
    }

    public boolean hasAttribute(RegistryEntry<EntityAttribute> attribute) {
        return this.custom.get(attribute) != null || this.fallback.has(attribute);
    }

    public boolean hasModifierForAttribute(RegistryEntry<EntityAttribute> attribute, Identifier id) {
        EntityAttributeInstance lv = this.custom.get(attribute);
        return lv != null ? lv.getModifier(id) != null : this.fallback.hasModifier(attribute, id);
    }

    public double getValue(RegistryEntry<EntityAttribute> attribute) {
        EntityAttributeInstance lv = this.custom.get(attribute);
        return lv != null ? lv.getValue() : this.fallback.getValue(attribute);
    }

    public double getBaseValue(RegistryEntry<EntityAttribute> attribute) {
        EntityAttributeInstance lv = this.custom.get(attribute);
        return lv != null ? lv.getBaseValue() : this.fallback.getBaseValue(attribute);
    }

    public double getModifierValue(RegistryEntry<EntityAttribute> attribute, Identifier id) {
        EntityAttributeInstance lv = this.custom.get(attribute);
        return lv != null ? lv.getModifier(id).value() : this.fallback.getModifierValue(attribute, id);
    }

    public void addTemporaryModifiers(Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> modifiersMap) {
        modifiersMap.forEach((attribute, modifier) -> {
            EntityAttributeInstance lv = this.getCustomInstance((RegistryEntry<EntityAttribute>)attribute);
            if (lv != null) {
                lv.removeModifier(modifier.id());
                lv.addTemporaryModifier((EntityAttributeModifier)modifier);
            }
        });
    }

    public void removeModifiers(Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> modifiersMap) {
        modifiersMap.asMap().forEach((attribute, modifiers) -> {
            EntityAttributeInstance lv = this.custom.get(attribute);
            if (lv != null) {
                modifiers.forEach(modifier -> lv.removeModifier(modifier.id()));
            }
        });
    }

    public void setFrom(AttributeContainer other) {
        other.custom.values().forEach(attributeInstance -> {
            EntityAttributeInstance lv = this.getCustomInstance(attributeInstance.getAttribute());
            if (lv != null) {
                lv.setFrom((EntityAttributeInstance)attributeInstance);
            }
        });
    }

    public void setBaseFrom(AttributeContainer other) {
        other.custom.values().forEach(attributeInstance -> {
            EntityAttributeInstance lv = this.getCustomInstance(attributeInstance.getAttribute());
            if (lv != null) {
                lv.setBaseValue(attributeInstance.getBaseValue());
            }
        });
    }

    public NbtList toNbt() {
        NbtList lv = new NbtList();
        for (EntityAttributeInstance lv2 : this.custom.values()) {
            lv.add(lv2.toNbt());
        }
        return lv;
    }

    public void readNbt(NbtList nbt) {
        for (int i = 0; i < nbt.size(); ++i) {
            NbtCompound lv = nbt.getCompound(i);
            String string = lv.getString("id");
            Identifier lv2 = Identifier.tryParse(string);
            if (lv2 != null) {
                Util.ifPresentOrElse(Registries.ATTRIBUTE.getEntry(lv2), attribute -> {
                    EntityAttributeInstance lv = this.getCustomInstance((RegistryEntry<EntityAttribute>)attribute);
                    if (lv != null) {
                        lv.readNbt(lv);
                    }
                }, () -> LOGGER.warn("Ignoring unknown attribute '{}'", (Object)lv2));
                continue;
            }
            LOGGER.warn("Ignoring malformed attribute '{}'", (Object)string);
        }
    }
}

