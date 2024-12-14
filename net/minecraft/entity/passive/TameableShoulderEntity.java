/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public abstract class TameableShoulderEntity
extends TameableEntity {
    private static final int READY_TO_SIT_COOLDOWN = 100;
    private int ticks;

    protected TameableShoulderEntity(EntityType<? extends TameableShoulderEntity> arg, World arg2) {
        super((EntityType<? extends TameableEntity>)arg, arg2);
    }

    public boolean mountOnto(ServerPlayerEntity player) {
        NbtCompound lv = new NbtCompound();
        lv.putString("id", this.getSavedEntityId());
        this.writeNbt(lv);
        if (player.addShoulderEntity(lv)) {
            this.discard();
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        ++this.ticks;
        super.tick();
    }

    public boolean isReadyToSitOnPlayer() {
        return this.ticks > 100;
    }
}

