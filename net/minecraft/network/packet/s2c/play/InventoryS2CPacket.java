/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.util.collection.DefaultedList;

public class InventoryS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, InventoryS2CPacket> CODEC = Packet.createCodec(InventoryS2CPacket::write, InventoryS2CPacket::new);
    private final int syncId;
    private final int revision;
    private final List<ItemStack> contents;
    private final ItemStack cursorStack;

    public InventoryS2CPacket(int syncId, int revision, DefaultedList<ItemStack> contents, ItemStack cursorStack) {
        this.syncId = syncId;
        this.revision = revision;
        this.contents = DefaultedList.ofSize(contents.size(), ItemStack.EMPTY);
        for (int k = 0; k < contents.size(); ++k) {
            this.contents.set(k, contents.get(k).copy());
        }
        this.cursorStack = cursorStack.copy();
    }

    private InventoryS2CPacket(RegistryByteBuf buf) {
        this.syncId = buf.readUnsignedByte();
        this.revision = buf.readVarInt();
        this.contents = (List)ItemStack.OPTIONAL_LIST_PACKET_CODEC.decode(buf);
        this.cursorStack = (ItemStack)ItemStack.OPTIONAL_PACKET_CODEC.decode(buf);
    }

    private void write(RegistryByteBuf buf) {
        buf.writeByte(this.syncId);
        buf.writeVarInt(this.revision);
        ItemStack.OPTIONAL_LIST_PACKET_CODEC.encode(buf, this.contents);
        ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, this.cursorStack);
    }

    @Override
    public PacketType<InventoryS2CPacket> getPacketId() {
        return PlayPackets.CONTAINER_SET_CONTENT;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onInventory(this);
    }

    public int getSyncId() {
        return this.syncId;
    }

    public List<ItemStack> getContents() {
        return this.contents;
    }

    public ItemStack getCursorStack() {
        return this.cursorStack;
    }

    public int getRevision() {
        return this.revision;
    }
}

