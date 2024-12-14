/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.network;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public class OtherClientPlayerEntity
extends AbstractClientPlayerEntity {
    private Vec3d clientVelocity = Vec3d.ZERO;
    private int velocityLerpDivisor;

    public OtherClientPlayerEntity(ClientWorld arg, GameProfile gameProfile) {
        super(arg, gameProfile);
        this.noClip = true;
    }

    @Override
    public boolean shouldRender(double distance) {
        double e = this.getBoundingBox().getAverageSideLength() * 10.0;
        if (Double.isNaN(e)) {
            e = 1.0;
        }
        return distance < (e *= 64.0 * OtherClientPlayerEntity.getRenderDistanceMultiplier()) * e;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        this.updateLimbs(false);
    }

    @Override
    public void tickMovement() {
        if (this.bodyTrackingIncrements > 0) {
            this.lerpPosAndRotation(this.bodyTrackingIncrements, this.serverX, this.serverY, this.serverZ, this.serverYaw, this.serverPitch);
            --this.bodyTrackingIncrements;
        }
        if (this.headTrackingIncrements > 0) {
            this.lerpHeadYaw(this.headTrackingIncrements, this.serverHeadYaw);
            --this.headTrackingIncrements;
        }
        if (this.velocityLerpDivisor > 0) {
            this.addVelocityInternal(new Vec3d((this.clientVelocity.x - this.getVelocity().x) / (double)this.velocityLerpDivisor, (this.clientVelocity.y - this.getVelocity().y) / (double)this.velocityLerpDivisor, (this.clientVelocity.z - this.getVelocity().z) / (double)this.velocityLerpDivisor));
            --this.velocityLerpDivisor;
        }
        this.prevStrideDistance = this.strideDistance;
        this.tickHandSwing();
        float f = !this.isOnGround() || this.isDead() ? 0.0f : (float)Math.min(0.1, this.getVelocity().horizontalLength());
        this.strideDistance += (f - this.strideDistance) * 0.4f;
        this.getWorld().getProfiler().push("push");
        this.tickCramming();
        this.getWorld().getProfiler().pop();
    }

    @Override
    public void setVelocityClient(double x, double y, double z) {
        this.clientVelocity = new Vec3d(x, y, z);
        this.velocityLerpDivisor = this.getType().getTrackTickInterval() + 1;
    }

    @Override
    protected void updatePose() {
    }

    @Override
    public void sendMessage(Text message) {
        MinecraftClient lv = MinecraftClient.getInstance();
        lv.inGameHud.getChatHud().addMessage(message);
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        this.resetPosition();
    }
}

