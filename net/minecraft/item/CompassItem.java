/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import java.util.Optional;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CompassItem
extends Item {
    public CompassItem(Item.Settings arg) {
        super(arg);
    }

    @Nullable
    public static GlobalPos createSpawnPos(World world) {
        return world.getDimension().natural() ? GlobalPos.create(world.getRegistryKey(), world.getSpawnPos()) : null;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return stack.contains(DataComponentTypes.LODESTONE_TRACKER) || super.hasGlint(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world instanceof ServerWorld) {
            LodestoneTrackerComponent lv3;
            ServerWorld lv = (ServerWorld)world;
            LodestoneTrackerComponent lv2 = stack.get(DataComponentTypes.LODESTONE_TRACKER);
            if (lv2 != null && (lv3 = lv2.forWorld(lv)) != lv2) {
                stack.set(DataComponentTypes.LODESTONE_TRACKER, lv3);
            }
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos lv = context.getBlockPos();
        World lv2 = context.getWorld();
        if (lv2.getBlockState(lv).isOf(Blocks.LODESTONE)) {
            lv2.playSound(null, lv, SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 1.0f, 1.0f);
            PlayerEntity lv3 = context.getPlayer();
            ItemStack lv4 = context.getStack();
            boolean bl = !lv3.isInCreativeMode() && lv4.getCount() == 1;
            LodestoneTrackerComponent lv5 = new LodestoneTrackerComponent(Optional.of(GlobalPos.create(lv2.getRegistryKey(), lv)), true);
            if (bl) {
                lv4.set(DataComponentTypes.LODESTONE_TRACKER, lv5);
            } else {
                ItemStack lv6 = lv4.copyComponentsToNewStack(Items.COMPASS, 1);
                lv4.decrementUnlessCreative(1, lv3);
                lv6.set(DataComponentTypes.LODESTONE_TRACKER, lv5);
                if (!lv3.getInventory().insertStack(lv6)) {
                    lv3.dropItem(lv6, false);
                }
            }
            return ActionResult.success(lv2.isClient);
        }
        return super.useOnBlock(context);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return stack.contains(DataComponentTypes.LODESTONE_TRACKER) ? "item.minecraft.lodestone_compass" : super.getTranslationKey(stack);
    }
}

