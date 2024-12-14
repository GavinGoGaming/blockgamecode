/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.MathHelper;

public class EulerAngle {
    public static final PacketCodec<ByteBuf, EulerAngle> PACKET_CODEC = new PacketCodec<ByteBuf, EulerAngle>(){

        @Override
        public EulerAngle decode(ByteBuf byteBuf) {
            return new EulerAngle(byteBuf.readFloat(), byteBuf.readFloat(), byteBuf.readFloat());
        }

        @Override
        public void encode(ByteBuf byteBuf, EulerAngle arg) {
            byteBuf.writeFloat(arg.pitch);
            byteBuf.writeFloat(arg.yaw);
            byteBuf.writeFloat(arg.roll);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (EulerAngle)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    protected final float pitch;
    protected final float yaw;
    protected final float roll;

    public EulerAngle(float pitch, float yaw, float roll) {
        this.pitch = Float.isInfinite(pitch) || Float.isNaN(pitch) ? 0.0f : pitch % 360.0f;
        this.yaw = Float.isInfinite(yaw) || Float.isNaN(yaw) ? 0.0f : yaw % 360.0f;
        this.roll = Float.isInfinite(roll) || Float.isNaN(roll) ? 0.0f : roll % 360.0f;
    }

    public EulerAngle(NbtList serialized) {
        this(serialized.getFloat(0), serialized.getFloat(1), serialized.getFloat(2));
    }

    public NbtList toNbt() {
        NbtList lv = new NbtList();
        lv.add(NbtFloat.of(this.pitch));
        lv.add(NbtFloat.of(this.yaw));
        lv.add(NbtFloat.of(this.roll));
        return lv;
    }

    public boolean equals(Object o) {
        if (!(o instanceof EulerAngle)) {
            return false;
        }
        EulerAngle lv = (EulerAngle)o;
        return this.pitch == lv.pitch && this.yaw == lv.yaw && this.roll == lv.roll;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getRoll() {
        return this.roll;
    }

    public float getWrappedPitch() {
        return MathHelper.wrapDegrees(this.pitch);
    }

    public float getWrappedYaw() {
        return MathHelper.wrapDegrees(this.yaw);
    }

    public float getWrappedRoll() {
        return MathHelper.wrapDegrees(this.roll);
    }
}

