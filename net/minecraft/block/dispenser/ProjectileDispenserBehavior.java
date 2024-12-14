/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.dispenser;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ProjectileItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;

public class ProjectileDispenserBehavior
extends ItemDispenserBehavior {
    private final ProjectileItem projectile;
    private final ProjectileItem.Settings projectileSettings;

    public ProjectileDispenserBehavior(Item item) {
        if (!(item instanceof ProjectileItem)) {
            throw new IllegalArgumentException(String.valueOf(item) + " not instance of " + ProjectileItem.class.getSimpleName());
        }
        ProjectileItem lv = (ProjectileItem)((Object)item);
        this.projectile = lv;
        this.projectileSettings = lv.getProjectileSettings();
    }

    @Override
    public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        ServerWorld lv = pointer.world();
        Direction lv2 = pointer.state().get(DispenserBlock.FACING);
        Position lv3 = this.projectileSettings.positionFunction().getDispensePosition(pointer, lv2);
        ProjectileEntity lv4 = this.projectile.createEntity(lv, lv3, stack, lv2);
        this.projectile.initializeProjectile(lv4, lv2.getOffsetX(), lv2.getOffsetY(), lv2.getOffsetZ(), this.projectileSettings.power(), this.projectileSettings.uncertainty());
        lv.spawnEntity(lv4);
        stack.decrement(1);
        return stack;
    }

    @Override
    protected void playSound(BlockPointer pointer) {
        pointer.world().syncWorldEvent(this.projectileSettings.overrideDispenseEvent().orElse(1002), pointer.pos(), 0);
    }
}

