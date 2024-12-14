/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public class EntityEquipmentUpdateS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, EntityEquipmentUpdateS2CPacket> CODEC = Packet.createCodec(EntityEquipmentUpdateS2CPacket::write, EntityEquipmentUpdateS2CPacket::new);
    private static final byte field_33342 = -128;
    private final int entityId;
    private final List<Pair<EquipmentSlot, ItemStack>> equipmentList;

    public EntityEquipmentUpdateS2CPacket(int entityId, List<Pair<EquipmentSlot, ItemStack>> equipmentList) {
        this.entityId = entityId;
        this.equipmentList = equipmentList;
    }

    private EntityEquipmentUpdateS2CPacket(RegistryByteBuf buf) {
        byte i;
        this.entityId = buf.readVarInt();
        EquipmentSlot[] lvs = EquipmentSlot.values();
        this.equipmentList = Lists.newArrayList();
        do {
            i = buf.readByte();
            EquipmentSlot lv = lvs[i & 0x7F];
            ItemStack lv2 = (ItemStack)ItemStack.OPTIONAL_PACKET_CODEC.decode(buf);
            this.equipmentList.add(Pair.of(lv, lv2));
        } while ((i & 0xFFFFFF80) != 0);
    }

    private void write(RegistryByteBuf buf) {
        buf.writeVarInt(this.entityId);
        int i = this.equipmentList.size();
        for (int j = 0; j < i; ++j) {
            Pair<EquipmentSlot, ItemStack> pair = this.equipmentList.get(j);
            EquipmentSlot lv = pair.getFirst();
            boolean bl = j != i - 1;
            int k = lv.ordinal();
            buf.writeByte(bl ? k | 0xFFFFFF80 : k);
            ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, pair.getSecond());
        }
    }

    @Override
    public PacketType<EntityEquipmentUpdateS2CPacket> getPacketId() {
        return PlayPackets.SET_EQUIPMENT;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onEntityEquipmentUpdate(this);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public List<Pair<EquipmentSlot, ItemStack>> getEquipmentList() {
        return this.equipmentList;
    }
}

