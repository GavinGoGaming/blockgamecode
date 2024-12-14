/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.decoration;

import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.BlockAttachedEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;

public abstract class AbstractDecorationEntity
extends BlockAttachedEntity {
    protected static final Predicate<Entity> PREDICATE = entity -> entity instanceof AbstractDecorationEntity;
    protected Direction facing = Direction.SOUTH;

    protected AbstractDecorationEntity(EntityType<? extends AbstractDecorationEntity> arg, World arg2) {
        super((EntityType<? extends BlockAttachedEntity>)arg, arg2);
    }

    protected AbstractDecorationEntity(EntityType<? extends AbstractDecorationEntity> type, World world, BlockPos pos) {
        this(type, world);
        this.attachedBlockPos = pos;
    }

    protected void setFacing(Direction facing) {
        Objects.requireNonNull(facing);
        Validate.isTrue(facing.getAxis().isHorizontal());
        this.facing = facing;
        this.setYaw(this.facing.getHorizontal() * 90);
        this.prevYaw = this.getYaw();
        this.updateAttachmentPosition();
    }

    @Override
    protected final void updateAttachmentPosition() {
        if (this.facing == null) {
            return;
        }
        Box lv = this.calculateBoundingBox(this.attachedBlockPos, this.facing);
        Vec3d lv2 = lv.getCenter();
        this.setPos(lv2.x, lv2.y, lv2.z);
        this.setBoundingBox(lv);
    }

    protected abstract Box calculateBoundingBox(BlockPos var1, Direction var2);

    @Override
    public boolean canStayAttached() {
        if (!this.getWorld().isSpaceEmpty(this)) {
            return false;
        }
        boolean bl = BlockPos.stream(this.getAttachmentBox()).allMatch(pos -> {
            BlockState lv = this.getWorld().getBlockState((BlockPos)pos);
            return lv.isSolid() || AbstractRedstoneGateBlock.isRedstoneGate(lv);
        });
        if (!bl) {
            return false;
        }
        return this.getWorld().getOtherEntities(this, this.getBoundingBox(), PREDICATE).isEmpty();
    }

    protected Box getAttachmentBox() {
        return this.getBoundingBox().offset(this.facing.getUnitVector().mul(-0.5f)).contract(1.0E-7);
    }

    @Override
    public Direction getHorizontalFacing() {
        return this.facing;
    }

    public abstract void onPlace();

    @Override
    public ItemEntity dropStack(ItemStack stack, float yOffset) {
        ItemEntity lv = new ItemEntity(this.getWorld(), this.getX() + (double)((float)this.facing.getOffsetX() * 0.15f), this.getY() + (double)yOffset, this.getZ() + (double)((float)this.facing.getOffsetZ() * 0.15f), stack);
        lv.setToDefaultPickupDelay();
        this.getWorld().spawnEntity(lv);
        return lv;
    }

    @Override
    public float applyRotation(BlockRotation rotation) {
        if (this.facing.getAxis() != Direction.Axis.Y) {
            switch (rotation) {
                case CLOCKWISE_180: {
                    this.facing = this.facing.getOpposite();
                    break;
                }
                case COUNTERCLOCKWISE_90: {
                    this.facing = this.facing.rotateYCounterclockwise();
                    break;
                }
                case CLOCKWISE_90: {
                    this.facing = this.facing.rotateYClockwise();
                    break;
                }
            }
        }
        float f = MathHelper.wrapDegrees(this.getYaw());
        return switch (rotation) {
            case BlockRotation.CLOCKWISE_180 -> f + 180.0f;
            case BlockRotation.COUNTERCLOCKWISE_90 -> f + 90.0f;
            case BlockRotation.CLOCKWISE_90 -> f + 270.0f;
            default -> f;
        };
    }

    @Override
    public float applyMirror(BlockMirror mirror) {
        return this.applyRotation(mirror.getRotation(this.facing));
    }
}

