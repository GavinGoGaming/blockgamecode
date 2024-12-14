/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StringIdentifiable;

public enum EquipmentSlot implements StringIdentifiable
{
    MAINHAND(Type.HAND, 0, 0, "mainhand"),
    OFFHAND(Type.HAND, 1, 5, "offhand"),
    FEET(Type.HUMANOID_ARMOR, 0, 1, 1, "feet"),
    LEGS(Type.HUMANOID_ARMOR, 1, 1, 2, "legs"),
    CHEST(Type.HUMANOID_ARMOR, 2, 1, 3, "chest"),
    HEAD(Type.HUMANOID_ARMOR, 3, 1, 4, "head"),
    BODY(Type.ANIMAL_ARMOR, 0, 1, 6, "body");

    public static final int NO_MAX_COUNT = 0;
    public static final StringIdentifiable.EnumCodec<EquipmentSlot> CODEC;
    private final Type type;
    private final int entityId;
    private final int maxCount;
    private final int armorStandId;
    private final String name;

    private EquipmentSlot(Type type, int entityId, int maxCount, int armorStandId, String name) {
        this.type = type;
        this.entityId = entityId;
        this.maxCount = maxCount;
        this.armorStandId = armorStandId;
        this.name = name;
    }

    private EquipmentSlot(Type type, int entityId, int armorStandId, String name) {
        this(type, entityId, 0, armorStandId, name);
    }

    public Type getType() {
        return this.type;
    }

    public int getEntitySlotId() {
        return this.entityId;
    }

    public int getOffsetEntitySlotId(int offset) {
        return offset + this.entityId;
    }

    public ItemStack split(ItemStack stack) {
        return this.maxCount > 0 ? stack.split(this.maxCount) : stack;
    }

    public int getArmorStandSlotId() {
        return this.armorStandId;
    }

    public String getName() {
        return this.name;
    }

    public boolean isArmorSlot() {
        return this.type == Type.HUMANOID_ARMOR || this.type == Type.ANIMAL_ARMOR;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public static EquipmentSlot byName(String name) {
        EquipmentSlot lv = CODEC.byId(name);
        if (lv != null) {
            return lv;
        }
        throw new IllegalArgumentException("Invalid slot '" + name + "'");
    }

    static {
        CODEC = StringIdentifiable.createCodec(EquipmentSlot::values);
    }

    public static enum Type {
        HAND,
        HUMANOID_ARMOR,
        ANIMAL_ARMOR;

    }
}

