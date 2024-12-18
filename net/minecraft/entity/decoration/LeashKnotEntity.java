/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.decoration;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.BlockAttachedEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.LeadItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class LeashKnotEntity
extends BlockAttachedEntity {
    public static final double field_30455 = 0.375;

    public LeashKnotEntity(EntityType<? extends LeashKnotEntity> arg, World arg2) {
        super((EntityType<? extends BlockAttachedEntity>)arg, arg2);
    }

    public LeashKnotEntity(World world, BlockPos pos) {
        super(EntityType.LEASH_KNOT, world, pos);
        this.setPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    protected void updateAttachmentPosition() {
        this.setPos((double)this.attachedBlockPos.getX() + 0.5, (double)this.attachedBlockPos.getY() + 0.375, (double)this.attachedBlockPos.getZ() + 0.5);
        double d = (double)this.getType().getWidth() / 2.0;
        double e = this.getType().getHeight();
        this.setBoundingBox(new Box(this.getX() - d, this.getY(), this.getZ() - d, this.getX() + d, this.getY() + e, this.getZ() + d));
    }

    @Override
    public boolean shouldRender(double distance) {
        return distance < 1024.0;
    }

    @Override
    public void onBreak(@Nullable Entity breaker) {
        this.playSound(SoundEvents.ENTITY_LEASH_KNOT_BREAK, 1.0f, 1.0f);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (this.getWorld().isClient) {
            return ActionResult.SUCCESS;
        }
        boolean bl = false;
        List<Leashable> list = LeadItem.collectLeashablesAround(this.getWorld(), this.getAttachedBlockPos(), entity -> {
            Entity lv = entity.getLeashHolder();
            return lv == player || lv == this;
        });
        for (Leashable lv : list) {
            if (lv.getLeashHolder() != player) continue;
            lv.attachLeash(this, true);
            bl = true;
        }
        boolean bl2 = false;
        if (!bl) {
            this.discard();
            if (player.getAbilities().creativeMode) {
                for (Leashable lv2 : list) {
                    if (!lv2.isLeashed() || lv2.getLeashHolder() != this) continue;
                    lv2.detachLeash(true, false);
                    bl2 = true;
                }
            }
        }
        if (bl || bl2) {
            this.emitGameEvent(GameEvent.BLOCK_ATTACH, player);
        }
        return ActionResult.CONSUME;
    }

    @Override
    public boolean canStayAttached() {
        return this.getWorld().getBlockState(this.attachedBlockPos).isIn(BlockTags.FENCES);
    }

    public static LeashKnotEntity getOrCreate(World world, BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        List<LeashKnotEntity> list = world.getNonSpectatingEntities(LeashKnotEntity.class, new Box((double)i - 1.0, (double)j - 1.0, (double)k - 1.0, (double)i + 1.0, (double)j + 1.0, (double)k + 1.0));
        for (LeashKnotEntity lv : list) {
            if (!lv.getAttachedBlockPos().equals(pos)) continue;
            return lv;
        }
        LeashKnotEntity lv2 = new LeashKnotEntity(world, pos);
        world.spawnEntity(lv2);
        return lv2;
    }

    public void onPlace() {
        this.playSound(SoundEvents.ENTITY_LEASH_KNOT_PLACE, 1.0f, 1.0f);
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        return new EntitySpawnS2CPacket((Entity)this, 0, this.getAttachedBlockPos());
    }

    @Override
    public Vec3d getLeashPos(float delta) {
        return this.getLerpedPos(delta).add(0.0, 0.2, 0.0);
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(Items.LEAD);
    }
}

