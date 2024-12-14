/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.util.math.Vec3d;

public class ExperienceOrbSpawnS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, ExperienceOrbSpawnS2CPacket> CODEC = Packet.createCodec(ExperienceOrbSpawnS2CPacket::write, ExperienceOrbSpawnS2CPacket::new);
    private final int entityId;
    private final double x;
    private final double y;
    private final double z;
    private final int experience;

    public ExperienceOrbSpawnS2CPacket(ExperienceOrbEntity orb, EntityTrackerEntry entry) {
        this.entityId = orb.getId();
        Vec3d lv = entry.getPos();
        this.x = lv.getX();
        this.y = lv.getY();
        this.z = lv.getZ();
        this.experience = orb.getExperienceAmount();
    }

    private ExperienceOrbSpawnS2CPacket(PacketByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.experience = buf.readShort();
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeShort(this.experience);
    }

    @Override
    public PacketType<ExperienceOrbSpawnS2CPacket> getPacketId() {
        return PlayPackets.ADD_EXPERIENCE_ORB;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onExperienceOrbSpawn(this);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public int getExperience() {
        return this.experience;
    }
}

