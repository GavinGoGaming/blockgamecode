/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EntityStatusS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, EntityStatusS2CPacket> CODEC = Packet.createCodec(EntityStatusS2CPacket::write, EntityStatusS2CPacket::new);
    private final int entityId;
    private final byte status;

    public EntityStatusS2CPacket(Entity entity, byte status) {
        this.entityId = entity.getId();
        this.status = status;
    }

    private EntityStatusS2CPacket(PacketByteBuf buf) {
        this.entityId = buf.readInt();
        this.status = buf.readByte();
    }

    private void write(PacketByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeByte(this.status);
    }

    @Override
    public PacketType<EntityStatusS2CPacket> getPacketId() {
        return PlayPackets.ENTITY_EVENT;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onEntityStatus(this);
    }

    @Nullable
    public Entity getEntity(World world) {
        return world.getEntityById(this.entityId);
    }

    public byte getStatus() {
        return this.status;
    }
}

