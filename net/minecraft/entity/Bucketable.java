/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import java.util.Optional;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public interface Bucketable {
    public boolean isFromBucket();

    public void setFromBucket(boolean var1);

    public void copyDataToStack(ItemStack var1);

    public void copyDataFromNbt(NbtCompound var1);

    public ItemStack getBucketItem();

    public SoundEvent getBucketFillSound();

    @Deprecated
    public static void copyDataToStack(MobEntity entity, ItemStack stack) {
        stack.set(DataComponentTypes.CUSTOM_NAME, entity.getCustomName());
        NbtComponent.set(DataComponentTypes.BUCKET_ENTITY_DATA, stack, arg2 -> {
            if (entity.isAiDisabled()) {
                arg2.putBoolean("NoAI", entity.isAiDisabled());
            }
            if (entity.isSilent()) {
                arg2.putBoolean("Silent", entity.isSilent());
            }
            if (entity.hasNoGravity()) {
                arg2.putBoolean("NoGravity", entity.hasNoGravity());
            }
            if (entity.isGlowingLocal()) {
                arg2.putBoolean("Glowing", entity.isGlowingLocal());
            }
            if (entity.isInvulnerable()) {
                arg2.putBoolean("Invulnerable", entity.isInvulnerable());
            }
            arg2.putFloat("Health", entity.getHealth());
        });
    }

    @Deprecated
    public static void copyDataFromNbt(MobEntity entity, NbtCompound nbt) {
        if (nbt.contains("NoAI")) {
            entity.setAiDisabled(nbt.getBoolean("NoAI"));
        }
        if (nbt.contains("Silent")) {
            entity.setSilent(nbt.getBoolean("Silent"));
        }
        if (nbt.contains("NoGravity")) {
            entity.setNoGravity(nbt.getBoolean("NoGravity"));
        }
        if (nbt.contains("Glowing")) {
            entity.setGlowing(nbt.getBoolean("Glowing"));
        }
        if (nbt.contains("Invulnerable")) {
            entity.setInvulnerable(nbt.getBoolean("Invulnerable"));
        }
        if (nbt.contains("Health", NbtElement.NUMBER_TYPE)) {
            entity.setHealth(nbt.getFloat("Health"));
        }
    }

    public static <T extends LivingEntity> Optional<ActionResult> tryBucket(PlayerEntity player, Hand hand, T entity) {
        ItemStack lv = player.getStackInHand(hand);
        if (lv.getItem() == Items.WATER_BUCKET && entity.isAlive()) {
            entity.playSound(((Bucketable)((Object)entity)).getBucketFillSound(), 1.0f, 1.0f);
            ItemStack lv2 = ((Bucketable)((Object)entity)).getBucketItem();
            ((Bucketable)((Object)entity)).copyDataToStack(lv2);
            ItemStack lv3 = ItemUsage.exchangeStack(lv, player, lv2, false);
            player.setStackInHand(hand, lv3);
            World lv4 = entity.getWorld();
            if (!lv4.isClient) {
                Criteria.FILLED_BUCKET.trigger((ServerPlayerEntity)player, lv2);
            }
            entity.discard();
            return Optional.of(ActionResult.success(lv4.isClient));
        }
        return Optional.empty();
    }
}

