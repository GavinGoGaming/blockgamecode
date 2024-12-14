/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.player;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class PlayerAbilities {
    public boolean invulnerable;
    public boolean flying;
    public boolean allowFlying;
    public boolean creativeMode;
    public boolean allowModifyWorld = true;
    private float flySpeed = 0.05f;
    private float walkSpeed = 0.1f;

    public void writeNbt(NbtCompound nbt) {
        NbtCompound lv = new NbtCompound();
        lv.putBoolean("invulnerable", this.invulnerable);
        lv.putBoolean("flying", this.flying);
        lv.putBoolean("mayfly", this.allowFlying);
        lv.putBoolean("instabuild", this.creativeMode);
        lv.putBoolean("mayBuild", this.allowModifyWorld);
        lv.putFloat("flySpeed", this.flySpeed);
        lv.putFloat("walkSpeed", this.walkSpeed);
        nbt.put("abilities", lv);
    }

    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("abilities", NbtElement.COMPOUND_TYPE)) {
            NbtCompound lv = nbt.getCompound("abilities");
            this.invulnerable = lv.getBoolean("invulnerable");
            this.flying = lv.getBoolean("flying");
            this.allowFlying = lv.getBoolean("mayfly");
            this.creativeMode = lv.getBoolean("instabuild");
            if (lv.contains("flySpeed", NbtElement.NUMBER_TYPE)) {
                this.flySpeed = lv.getFloat("flySpeed");
                this.walkSpeed = lv.getFloat("walkSpeed");
            }
            if (lv.contains("mayBuild", NbtElement.BYTE_TYPE)) {
                this.allowModifyWorld = lv.getBoolean("mayBuild");
            }
        }
    }

    public float getFlySpeed() {
        return this.flySpeed;
    }

    public void setFlySpeed(float flySpeed) {
        this.flySpeed = flySpeed;
    }

    public float getWalkSpeed() {
        return this.walkSpeed;
    }

    public void setWalkSpeed(float walkSpeed) {
        this.walkSpeed = walkSpeed;
    }
}

