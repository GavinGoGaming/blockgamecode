/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.enchantment;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.effect.EnchantmentEffectTarget;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.enchantment.provider.EnchantmentProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.collection.Weighting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

public class EnchantmentHelper {
    public static int getLevel(RegistryEntry<Enchantment> enchantment, ItemStack stack) {
        ItemEnchantmentsComponent lv = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        return lv.getLevel(enchantment);
    }

    public static ItemEnchantmentsComponent apply(ItemStack stack, java.util.function.Consumer<ItemEnchantmentsComponent.Builder> applier) {
        ComponentType<ItemEnchantmentsComponent> lv = EnchantmentHelper.getEnchantmentsComponentType(stack);
        ItemEnchantmentsComponent lv2 = stack.get(lv);
        if (lv2 == null) {
            return ItemEnchantmentsComponent.DEFAULT;
        }
        ItemEnchantmentsComponent.Builder lv3 = new ItemEnchantmentsComponent.Builder(lv2);
        applier.accept(lv3);
        ItemEnchantmentsComponent lv4 = lv3.build();
        stack.set(lv, lv4);
        return lv4;
    }

    public static boolean canHaveEnchantments(ItemStack stack) {
        return stack.contains(EnchantmentHelper.getEnchantmentsComponentType(stack));
    }

    public static void set(ItemStack stack, ItemEnchantmentsComponent enchantments) {
        stack.set(EnchantmentHelper.getEnchantmentsComponentType(stack), enchantments);
    }

    public static ItemEnchantmentsComponent getEnchantments(ItemStack stack) {
        return stack.getOrDefault(EnchantmentHelper.getEnchantmentsComponentType(stack), ItemEnchantmentsComponent.DEFAULT);
    }

    private static ComponentType<ItemEnchantmentsComponent> getEnchantmentsComponentType(ItemStack stack) {
        return stack.isOf(Items.ENCHANTED_BOOK) ? DataComponentTypes.STORED_ENCHANTMENTS : DataComponentTypes.ENCHANTMENTS;
    }

    public static boolean hasEnchantments(ItemStack stack) {
        return !stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT).isEmpty() || !stack.getOrDefault(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT).isEmpty();
    }

    public static int getItemDamage(ServerWorld world, ItemStack stack, int baseItemDamage) {
        MutableFloat mutableFloat = new MutableFloat(baseItemDamage);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyItemDamage(world, level, stack, mutableFloat));
        return mutableFloat.intValue();
    }

    public static int getAmmoUse(ServerWorld world, ItemStack rangedWeaponStack, ItemStack projectileStack, int baseAmmoUse) {
        MutableFloat mutableFloat = new MutableFloat(baseAmmoUse);
        EnchantmentHelper.forEachEnchantment(rangedWeaponStack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyAmmoUse(world, level, projectileStack, mutableFloat));
        return mutableFloat.intValue();
    }

    public static int getBlockExperience(ServerWorld world, ItemStack stack, int baseBlockExperience) {
        MutableFloat mutableFloat = new MutableFloat(baseBlockExperience);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyBlockExperience(world, level, stack, mutableFloat));
        return mutableFloat.intValue();
    }

    public static int getMobExperience(ServerWorld world, @Nullable Entity attacker, Entity mob, int baseMobExperience) {
        if (attacker instanceof LivingEntity) {
            LivingEntity lv = (LivingEntity)attacker;
            MutableFloat mutableFloat = new MutableFloat(baseMobExperience);
            EnchantmentHelper.forEachEnchantment(lv, (RegistryEntry<Enchantment> enchantment, int level, EnchantmentEffectContext context) -> ((Enchantment)enchantment.value()).modifyMobExperience(world, level, context.stack(), mob, mutableFloat));
            return mutableFloat.intValue();
        }
        return baseMobExperience;
    }

    private static void forEachEnchantment(ItemStack stack, Consumer consumer) {
        ItemEnchantmentsComponent lv = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : lv.getEnchantmentEntries()) {
            consumer.accept((RegistryEntry)entry.getKey(), entry.getIntValue());
        }
    }

    private static void forEachEnchantment(ItemStack stack, EquipmentSlot slot, LivingEntity entity, ContextAwareConsumer contextAwareConsumer) {
        if (stack.isEmpty()) {
            return;
        }
        ItemEnchantmentsComponent lv = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (lv == null || lv.isEmpty()) {
            return;
        }
        EnchantmentEffectContext lv2 = new EnchantmentEffectContext(stack, slot, entity);
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : lv.getEnchantmentEntries()) {
            RegistryEntry lv3 = (RegistryEntry)entry.getKey();
            if (!((Enchantment)lv3.value()).slotMatches(slot)) continue;
            contextAwareConsumer.accept(lv3, entry.getIntValue(), lv2);
        }
    }

    private static void forEachEnchantment(LivingEntity entity, ContextAwareConsumer contextAwareConsumer) {
        for (EquipmentSlot lv : EquipmentSlot.values()) {
            EnchantmentHelper.forEachEnchantment(entity.getEquippedStack(lv), lv, entity, contextAwareConsumer);
        }
    }

    public static boolean isInvulnerableTo(ServerWorld world, LivingEntity user, DamageSource damageSource) {
        MutableBoolean mutableBoolean = new MutableBoolean();
        EnchantmentHelper.forEachEnchantment(user, (RegistryEntry<Enchantment> enchantment, int level, EnchantmentEffectContext context) -> mutableBoolean.setValue(mutableBoolean.isTrue() || ((Enchantment)enchantment.value()).hasDamageImmunityTo(world, level, user, damageSource)));
        return mutableBoolean.isTrue();
    }

    public static float getProtectionAmount(ServerWorld world, LivingEntity user, DamageSource damageSource) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.forEachEnchantment(user, (RegistryEntry<Enchantment> enchantment, int level, EnchantmentEffectContext context) -> ((Enchantment)enchantment.value()).modifyDamageProtection(world, level, context.stack(), user, damageSource, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static float getDamage(ServerWorld world, ItemStack stack, Entity target, DamageSource damageSource, float baseDamage) {
        MutableFloat mutableFloat = new MutableFloat(baseDamage);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyDamage(world, level, stack, target, damageSource, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static float getSmashDamagePerFallenBlock(ServerWorld world, ItemStack stack, Entity target, DamageSource damageSource, float baseSmashDamagePerFallenBlock) {
        MutableFloat mutableFloat = new MutableFloat(baseSmashDamagePerFallenBlock);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifySmashDamagePerFallenBlock(world, level, stack, target, damageSource, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static float getArmorEffectiveness(ServerWorld world, ItemStack stack, Entity user, DamageSource damageSource, float baseArmorEffectiveness) {
        MutableFloat mutableFloat = new MutableFloat(baseArmorEffectiveness);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyArmorEffectiveness(world, level, stack, user, damageSource, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static float modifyKnockback(ServerWorld world, ItemStack stack, Entity target, DamageSource damageSource, float baseKnockback) {
        MutableFloat mutableFloat = new MutableFloat(baseKnockback);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyKnockback(world, level, stack, target, damageSource, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static void onTargetDamaged(ServerWorld world, Entity target, DamageSource damageSource) {
        Entity entity = damageSource.getAttacker();
        if (entity instanceof LivingEntity) {
            LivingEntity lv = (LivingEntity)entity;
            EnchantmentHelper.onTargetDamaged(world, target, damageSource, lv.getWeaponStack());
        } else {
            EnchantmentHelper.onTargetDamaged(world, target, damageSource, null);
        }
    }

    public static void onTargetDamaged(ServerWorld world, Entity target, DamageSource damageSource, @Nullable ItemStack weapon) {
        Entity entity;
        LivingEntity lv;
        if (target instanceof LivingEntity) {
            lv = (LivingEntity)target;
            EnchantmentHelper.forEachEnchantment(lv, (RegistryEntry<Enchantment> enchantment, int level, EnchantmentEffectContext context) -> ((Enchantment)enchantment.value()).onTargetDamaged(world, level, context, EnchantmentEffectTarget.VICTIM, target, damageSource));
        }
        if (weapon != null && (entity = damageSource.getAttacker()) instanceof LivingEntity) {
            lv = (LivingEntity)entity;
            EnchantmentHelper.forEachEnchantment(weapon, EquipmentSlot.MAINHAND, lv, (enchantment, level, context) -> ((Enchantment)enchantment.value()).onTargetDamaged(world, level, context, EnchantmentEffectTarget.ATTACKER, target, damageSource));
        }
    }

    public static void applyLocationBasedEffects(ServerWorld world, LivingEntity user) {
        EnchantmentHelper.forEachEnchantment(user, (RegistryEntry<Enchantment> enchantment, int level, EnchantmentEffectContext context) -> ((Enchantment)enchantment.value()).applyLocationBasedEffects(world, level, context, user));
    }

    public static void applyLocationBasedEffects(ServerWorld world, ItemStack stack, LivingEntity user, EquipmentSlot slot) {
        EnchantmentHelper.forEachEnchantment(stack, slot, user, (enchantment, level, context) -> ((Enchantment)enchantment.value()).applyLocationBasedEffects(world, level, context, user));
    }

    public static void removeLocationBasedEffects(LivingEntity user) {
        EnchantmentHelper.forEachEnchantment(user, (RegistryEntry<Enchantment> enchantment, int level, EnchantmentEffectContext context) -> ((Enchantment)enchantment.value()).removeLocationBasedEffects(level, context, user));
    }

    public static void removeLocationBasedEffects(ItemStack stack, LivingEntity user, EquipmentSlot slot) {
        EnchantmentHelper.forEachEnchantment(stack, slot, user, (enchantment, level, context) -> ((Enchantment)enchantment.value()).removeLocationBasedEffects(level, context, user));
    }

    public static void onTick(ServerWorld world, LivingEntity user) {
        EnchantmentHelper.forEachEnchantment(user, (RegistryEntry<Enchantment> enchantment, int level, EnchantmentEffectContext context) -> ((Enchantment)enchantment.value()).onTick(world, level, context, user));
    }

    public static int getEquipmentLevel(RegistryEntry<Enchantment> enchantment, LivingEntity entity) {
        Collection<ItemStack> iterable = enchantment.value().getEquipment(entity).values();
        int i = 0;
        for (ItemStack lv : iterable) {
            int j = EnchantmentHelper.getLevel(enchantment, lv);
            if (j <= i) continue;
            i = j;
        }
        return i;
    }

    public static int getProjectileCount(ServerWorld world, ItemStack stack, Entity user, int baseProjectileCount) {
        MutableFloat mutableFloat = new MutableFloat(baseProjectileCount);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyProjectileCount(world, level, stack, user, mutableFloat));
        return Math.max(0, mutableFloat.intValue());
    }

    public static float getProjectileSpread(ServerWorld world, ItemStack stack, Entity user, float baseProjectileSpread) {
        MutableFloat mutableFloat = new MutableFloat(baseProjectileSpread);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyProjectileSpread(world, level, stack, user, mutableFloat));
        return Math.max(0.0f, mutableFloat.floatValue());
    }

    public static int getProjectilePiercing(ServerWorld world, ItemStack weaponStack, ItemStack projectileStack) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.forEachEnchantment(weaponStack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyProjectilePiercing(world, level, projectileStack, mutableFloat));
        return Math.max(0, mutableFloat.intValue());
    }

    public static void onProjectileSpawned(ServerWorld world, ItemStack weaponStack, PersistentProjectileEntity projectileEntity, java.util.function.Consumer<Item> onBreak) {
        LivingEntity lv;
        Entity entity = projectileEntity.getOwner();
        LivingEntity lv2 = entity instanceof LivingEntity ? (lv = (LivingEntity)entity) : null;
        EnchantmentEffectContext lv3 = new EnchantmentEffectContext(weaponStack, null, lv2, onBreak);
        EnchantmentHelper.forEachEnchantment(weaponStack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).onProjectileSpawned(world, level, lv3, projectileEntity));
    }

    public static void onHitBlock(ServerWorld world, ItemStack stack, @Nullable LivingEntity user, Entity enchantedEntity, @Nullable EquipmentSlot slot, Vec3d pos, BlockState state, java.util.function.Consumer<Item> onBreak) {
        EnchantmentEffectContext lv = new EnchantmentEffectContext(stack, slot, user, onBreak);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).onHitBlock(world, level, lv, enchantedEntity, pos, state));
    }

    public static int getRepairWithXp(ServerWorld world, ItemStack stack, int baseRepairWithXp) {
        MutableFloat mutableFloat = new MutableFloat(baseRepairWithXp);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyRepairWithXp(world, level, stack, mutableFloat));
        return Math.max(0, mutableFloat.intValue());
    }

    public static float getEquipmentDropChance(ServerWorld world, LivingEntity attacker, DamageSource damageSource, float baseEquipmentDropChance) {
        MutableFloat mutableFloat = new MutableFloat(baseEquipmentDropChance);
        Random lv = attacker.getRandom();
        EnchantmentHelper.forEachEnchantment(attacker, (RegistryEntry<Enchantment> enchantment, int level, EnchantmentEffectContext context) -> {
            LootContext lv = Enchantment.createEnchantedDamageLootContext(world, level, attacker, damageSource);
            ((Enchantment)enchantment.value()).getEffect(EnchantmentEffectComponentTypes.EQUIPMENT_DROPS).forEach(effect -> {
                if (effect.enchanted() == EnchantmentEffectTarget.VICTIM && effect.affected() == EnchantmentEffectTarget.VICTIM && effect.test(lv)) {
                    mutableFloat.setValue(((EnchantmentValueEffect)effect.effect()).apply(level, lv, mutableFloat.floatValue()));
                }
            });
        });
        Entity lv2 = damageSource.getAttacker();
        if (lv2 instanceof LivingEntity) {
            LivingEntity lv3 = (LivingEntity)lv2;
            EnchantmentHelper.forEachEnchantment(lv3, (RegistryEntry<Enchantment> enchantment, int level, EnchantmentEffectContext context) -> {
                LootContext lv = Enchantment.createEnchantedDamageLootContext(world, level, attacker, damageSource);
                ((Enchantment)enchantment.value()).getEffect(EnchantmentEffectComponentTypes.EQUIPMENT_DROPS).forEach(effect -> {
                    if (effect.enchanted() == EnchantmentEffectTarget.ATTACKER && effect.affected() == EnchantmentEffectTarget.VICTIM && effect.test(lv)) {
                        mutableFloat.setValue(((EnchantmentValueEffect)effect.effect()).apply(level, lv, mutableFloat.floatValue()));
                    }
                });
            });
        }
        return mutableFloat.floatValue();
    }

    public static void applyAttributeModifiers(ItemStack stack, AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer) {
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).getEffect(EnchantmentEffectComponentTypes.ATTRIBUTES).forEach(effect -> {
            if (((Enchantment)enchantment.value()).definition().slots().contains(slot)) {
                attributeModifierConsumer.accept(effect.attribute(), effect.createAttributeModifier(level, slot));
            }
        }));
    }

    public static void applyAttributeModifiers(ItemStack stack, EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer) {
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).getEffect(EnchantmentEffectComponentTypes.ATTRIBUTES).forEach(effect -> {
            if (((Enchantment)enchantment.value()).slotMatches(slot)) {
                attributeModifierConsumer.accept(effect.attribute(), effect.createAttributeModifier(level, slot));
            }
        }));
    }

    public static int getFishingLuckBonus(ServerWorld world, ItemStack stack, Entity user) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyFishingLuckBonus(world, level, stack, user, mutableFloat));
        return Math.max(0, mutableFloat.intValue());
    }

    public static float getFishingTimeReduction(ServerWorld world, ItemStack stack, Entity user) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyFishingTimeReduction(world, level, stack, user, mutableFloat));
        return Math.max(0.0f, mutableFloat.floatValue());
    }

    public static int getTridentReturnAcceleration(ServerWorld world, ItemStack stack, Entity user) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyTridentReturnAcceleration(world, level, stack, user, mutableFloat));
        return Math.max(0, mutableFloat.intValue());
    }

    public static float getCrossbowChargeTime(ItemStack stack, LivingEntity user, float baseCrossbowChargeTime) {
        MutableFloat mutableFloat = new MutableFloat(baseCrossbowChargeTime);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyCrossbowChargeTime(user.getRandom(), level, mutableFloat));
        return Math.max(0.0f, mutableFloat.floatValue());
    }

    public static float getTridentSpinAttackStrength(ItemStack stack, LivingEntity user) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> ((Enchantment)enchantment.value()).modifyTridentSpinAttackStrength(user.getRandom(), level, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static boolean hasAnyEnchantmentsIn(ItemStack stack, TagKey<Enchantment> tag) {
        ItemEnchantmentsComponent lv = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : lv.getEnchantmentEntries()) {
            RegistryEntry lv2 = (RegistryEntry)entry.getKey();
            if (!lv2.isIn(tag)) continue;
            return true;
        }
        return false;
    }

    public static boolean hasAnyEnchantmentsWith(ItemStack stack, ComponentType<?> componentType) {
        MutableBoolean mutableBoolean = new MutableBoolean(false);
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> {
            if (((Enchantment)enchantment.value()).effects().contains(componentType)) {
                mutableBoolean.setTrue();
            }
        });
        return mutableBoolean.booleanValue();
    }

    public static <T> Optional<T> getEffect(ItemStack stack, ComponentType<List<T>> componentType) {
        Pair<List<T>, Integer> pair = EnchantmentHelper.getEffectListAndLevel(stack, componentType);
        if (pair != null) {
            List<T> list = pair.getFirst();
            int i = pair.getSecond();
            return Optional.of(list.get(Math.min(i, list.size()) - 1));
        }
        return Optional.empty();
    }

    @Nullable
    public static <T> Pair<T, Integer> getEffectListAndLevel(ItemStack stack, ComponentType<T> componentType) {
        MutableObject mutableObject = new MutableObject();
        EnchantmentHelper.forEachEnchantment(stack, (RegistryEntry<Enchantment> enchantment, int level) -> {
            Object object;
            if ((mutableObject.getValue() == null || (Integer)((Pair)mutableObject.getValue()).getSecond() < level) && (object = ((Enchantment)enchantment.value()).effects().get(componentType)) != null) {
                mutableObject.setValue(Pair.of(object, level));
            }
        });
        return (Pair)mutableObject.getValue();
    }

    public static Optional<EnchantmentEffectContext> chooseEquipmentWith(ComponentType<?> componentType, LivingEntity entity, Predicate<ItemStack> stackPredicate) {
        ArrayList<EnchantmentEffectContext> list = new ArrayList<EnchantmentEffectContext>();
        for (EquipmentSlot lv : EquipmentSlot.values()) {
            ItemStack lv2 = entity.getEquippedStack(lv);
            if (!stackPredicate.test(lv2)) continue;
            ItemEnchantmentsComponent lv3 = lv2.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : lv3.getEnchantmentEntries()) {
                RegistryEntry lv4 = (RegistryEntry)entry.getKey();
                if (!((Enchantment)lv4.value()).effects().contains(componentType) || !((Enchantment)lv4.value()).slotMatches(lv)) continue;
                list.add(new EnchantmentEffectContext(lv2, lv, entity));
            }
        }
        return Util.getRandomOrEmpty(list, entity.getRandom());
    }

    public static int calculateRequiredExperienceLevel(Random random, int slotIndex, int bookshelfCount, ItemStack stack) {
        Item lv = stack.getItem();
        int k = lv.getEnchantability();
        if (k <= 0) {
            return 0;
        }
        if (bookshelfCount > 15) {
            bookshelfCount = 15;
        }
        int l = random.nextInt(8) + 1 + (bookshelfCount >> 1) + random.nextInt(bookshelfCount + 1);
        if (slotIndex == 0) {
            return Math.max(l / 3, 1);
        }
        if (slotIndex == 1) {
            return l * 2 / 3 + 1;
        }
        return Math.max(l, bookshelfCount * 2);
    }

    public static ItemStack enchant(Random random, ItemStack stack, int level, DynamicRegistryManager dynamicRegistryManager, Optional<? extends RegistryEntryList<Enchantment>> enchantments) {
        return EnchantmentHelper.enchant(random, stack, level, enchantments.map(RegistryEntryList::stream).orElseGet(() -> dynamicRegistryManager.get(RegistryKeys.ENCHANTMENT).streamEntries().map(arg -> arg)));
    }

    public static ItemStack enchant(Random random, ItemStack stack, int level, Stream<RegistryEntry<Enchantment>> possibleEnchantments) {
        List<EnchantmentLevelEntry> list = EnchantmentHelper.generateEnchantments(random, stack, level, possibleEnchantments);
        if (stack.isOf(Items.BOOK)) {
            stack = new ItemStack(Items.ENCHANTED_BOOK);
        }
        for (EnchantmentLevelEntry lv : list) {
            stack.addEnchantment(lv.enchantment, lv.level);
        }
        return stack;
    }

    public static List<EnchantmentLevelEntry> generateEnchantments(Random random, ItemStack stack, int level, Stream<RegistryEntry<Enchantment>> possibleEnchantments) {
        ArrayList<EnchantmentLevelEntry> list = Lists.newArrayList();
        Item lv = stack.getItem();
        int j = lv.getEnchantability();
        if (j <= 0) {
            return list;
        }
        level += 1 + random.nextInt(j / 4 + 1) + random.nextInt(j / 4 + 1);
        float f = (random.nextFloat() + random.nextFloat() - 1.0f) * 0.15f;
        List<EnchantmentLevelEntry> list2 = EnchantmentHelper.getPossibleEntries(level = MathHelper.clamp(Math.round((float)level + (float)level * f), 1, Integer.MAX_VALUE), stack, possibleEnchantments);
        if (!list2.isEmpty()) {
            Weighting.getRandom(random, list2).ifPresent(list::add);
            while (random.nextInt(50) <= level) {
                if (!list.isEmpty()) {
                    EnchantmentHelper.removeConflicts(list2, Util.getLast(list));
                }
                if (list2.isEmpty()) break;
                Weighting.getRandom(random, list2).ifPresent(list::add);
                level /= 2;
            }
        }
        return list;
    }

    public static void removeConflicts(List<EnchantmentLevelEntry> possibleEntries, EnchantmentLevelEntry pickedEntry) {
        possibleEntries.removeIf(entry -> !Enchantment.canBeCombined(arg.enchantment, entry.enchantment));
    }

    public static boolean isCompatible(Collection<RegistryEntry<Enchantment>> existing, RegistryEntry<Enchantment> candidate) {
        for (RegistryEntry<Enchantment> lv : existing) {
            if (Enchantment.canBeCombined(lv, candidate)) continue;
            return false;
        }
        return true;
    }

    public static List<EnchantmentLevelEntry> getPossibleEntries(int level, ItemStack stack, Stream<RegistryEntry<Enchantment>> possibleEnchantments) {
        ArrayList<EnchantmentLevelEntry> list = Lists.newArrayList();
        boolean bl = stack.isOf(Items.BOOK);
        possibleEnchantments.filter(enchantment -> ((Enchantment)enchantment.value()).isPrimaryItem(stack) || bl).forEach(enchantmentx -> {
            Enchantment lv = (Enchantment)enchantmentx.value();
            for (int j = lv.getMaxLevel(); j >= lv.getMinLevel(); --j) {
                if (level < lv.getMinPower(j) || level > lv.getMaxPower(j)) continue;
                list.add(new EnchantmentLevelEntry((RegistryEntry<Enchantment>)enchantmentx, j));
                break;
            }
        });
        return list;
    }

    public static void applyEnchantmentProvider(ItemStack stack, DynamicRegistryManager registryManager, RegistryKey<EnchantmentProvider> providerKey, LocalDifficulty localDifficulty, Random random) {
        EnchantmentProvider lv = registryManager.get(RegistryKeys.ENCHANTMENT_PROVIDER).get(providerKey);
        if (lv != null) {
            EnchantmentHelper.apply(stack, componentBuilder -> lv.provideEnchantments(stack, (ItemEnchantmentsComponent.Builder)componentBuilder, random, localDifficulty));
        }
    }

    @FunctionalInterface
    static interface Consumer {
        public void accept(RegistryEntry<Enchantment> var1, int var2);
    }

    @FunctionalInterface
    static interface ContextAwareConsumer {
        public void accept(RegistryEntry<Enchantment> var1, int var2, EnchantmentEffectContext var3);
    }
}

