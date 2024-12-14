/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class ArmorItem
extends Item
implements Equipment {
    public static final DispenserBehavior DISPENSER_BEHAVIOR = new ItemDispenserBehavior(){

        @Override
        protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            return ArmorItem.dispenseArmor(pointer, stack) ? stack : super.dispenseSilently(pointer, stack);
        }
    };
    protected final Type type;
    protected final RegistryEntry<ArmorMaterial> material;
    private final Supplier<AttributeModifiersComponent> attributeModifiers;

    public static boolean dispenseArmor(BlockPointer pointer, ItemStack armor) {
        BlockPos lv = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
        List<Entity> list = pointer.world().getEntitiesByClass(LivingEntity.class, new Box(lv), EntityPredicates.EXCEPT_SPECTATOR.and(new EntityPredicates.Equipable(armor)));
        if (list.isEmpty()) {
            return false;
        }
        LivingEntity lv2 = (LivingEntity)list.get(0);
        EquipmentSlot lv3 = lv2.getPreferredEquipmentSlot(armor);
        ItemStack lv4 = armor.split(1);
        lv2.equipStack(lv3, lv4);
        if (lv2 instanceof MobEntity) {
            ((MobEntity)lv2).setEquipmentDropChance(lv3, 2.0f);
            ((MobEntity)lv2).setPersistent();
        }
        return true;
    }

    public ArmorItem(RegistryEntry<ArmorMaterial> material, Type type, Item.Settings settings) {
        super(settings);
        this.material = material;
        this.type = type;
        DispenserBlock.registerBehavior(this, DISPENSER_BEHAVIOR);
        this.attributeModifiers = Suppliers.memoize(() -> {
            int i = ((ArmorMaterial)material.value()).getProtection(type);
            float f = ((ArmorMaterial)material.value()).toughness();
            AttributeModifiersComponent.Builder lv = AttributeModifiersComponent.builder();
            AttributeModifierSlot lv2 = AttributeModifierSlot.forEquipmentSlot(type.getEquipmentSlot());
            Identifier lv3 = Identifier.ofVanilla("armor." + type.getName());
            lv.add(EntityAttributes.GENERIC_ARMOR, new EntityAttributeModifier(lv3, i, EntityAttributeModifier.Operation.ADD_VALUE), lv2);
            lv.add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, new EntityAttributeModifier(lv3, f, EntityAttributeModifier.Operation.ADD_VALUE), lv2);
            float g = ((ArmorMaterial)material.value()).knockbackResistance();
            if (g > 0.0f) {
                lv.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, new EntityAttributeModifier(lv3, g, EntityAttributeModifier.Operation.ADD_VALUE), lv2);
            }
            return lv.build();
        });
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public int getEnchantability() {
        return this.material.value().enchantability();
    }

    public RegistryEntry<ArmorMaterial> getMaterial() {
        return this.material;
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return this.material.value().repairIngredient().get().test(ingredient) || super.canRepair(stack, ingredient);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return this.equipAndSwap(this, world, user, hand);
    }

    @Override
    public AttributeModifiersComponent getAttributeModifiers() {
        return this.attributeModifiers.get();
    }

    public int getProtection() {
        return this.material.value().getProtection(this.type);
    }

    public float getToughness() {
        return this.material.value().toughness();
    }

    @Override
    public EquipmentSlot getSlotType() {
        return this.type.getEquipmentSlot();
    }

    @Override
    public RegistryEntry<SoundEvent> getEquipSound() {
        return this.getMaterial().value().equipSound();
    }

    public static enum Type implements StringIdentifiable
    {
        HELMET(EquipmentSlot.HEAD, 11, "helmet"),
        CHESTPLATE(EquipmentSlot.CHEST, 16, "chestplate"),
        LEGGINGS(EquipmentSlot.LEGS, 15, "leggings"),
        BOOTS(EquipmentSlot.FEET, 13, "boots"),
        BODY(EquipmentSlot.BODY, 16, "body");

        public static final Codec<Type> CODEC;
        private final EquipmentSlot equipmentSlot;
        private final String name;
        private final int baseMaxDamage;

        private Type(EquipmentSlot equipmentSlot, int baseMaxDamage, String name) {
            this.equipmentSlot = equipmentSlot;
            this.name = name;
            this.baseMaxDamage = baseMaxDamage;
        }

        public int getMaxDamage(int multiplier) {
            return this.baseMaxDamage * multiplier;
        }

        public EquipmentSlot getEquipmentSlot() {
            return this.equipmentSlot;
        }

        public String getName() {
            return this.name;
        }

        public boolean isTrimmable() {
            return this == HELMET || this == CHESTPLATE || this == LEGGINGS || this == BOOTS;
        }

        @Override
        public String asString() {
            return this.name;
        }

        static {
            CODEC = StringIdentifiable.createBasicCodec(Type::values);
        }
    }
}

