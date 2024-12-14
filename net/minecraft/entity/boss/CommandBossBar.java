/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.boss;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class CommandBossBar
extends ServerBossBar {
    private final Identifier id;
    private final Set<UUID> playerUuids = Sets.newHashSet();
    private int value;
    private int maxValue = 100;

    public CommandBossBar(Identifier id, Text displayName) {
        super(displayName, BossBar.Color.WHITE, BossBar.Style.PROGRESS);
        this.id = id;
        this.setPercent(0.0f);
    }

    public Identifier getId() {
        return this.id;
    }

    @Override
    public void addPlayer(ServerPlayerEntity player) {
        super.addPlayer(player);
        this.playerUuids.add(player.getUuid());
    }

    public void addPlayer(UUID uuid) {
        this.playerUuids.add(uuid);
    }

    @Override
    public void removePlayer(ServerPlayerEntity player) {
        super.removePlayer(player);
        this.playerUuids.remove(player.getUuid());
    }

    @Override
    public void clearPlayers() {
        super.clearPlayers();
        this.playerUuids.clear();
    }

    public int getValue() {
        return this.value;
    }

    public int getMaxValue() {
        return this.maxValue;
    }

    public void setValue(int value) {
        this.value = value;
        this.setPercent(MathHelper.clamp((float)value / (float)this.maxValue, 0.0f, 1.0f));
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        this.setPercent(MathHelper.clamp((float)this.value / (float)maxValue, 0.0f, 1.0f));
    }

    public final Text toHoverableText() {
        return Texts.bracketed(this.getName()).styled(style -> style.withColor(this.getColor().getTextFormat()).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(this.getId().toString()))).withInsertion(this.getId().toString()));
    }

    public boolean addPlayers(Collection<ServerPlayerEntity> players) {
        boolean bl;
        HashSet<UUID> set = Sets.newHashSet();
        HashSet<ServerPlayerEntity> set2 = Sets.newHashSet();
        for (UUID uUID : this.playerUuids) {
            bl = false;
            for (ServerPlayerEntity lv : players) {
                if (!lv.getUuid().equals(uUID)) continue;
                bl = true;
                break;
            }
            if (bl) continue;
            set.add(uUID);
        }
        for (ServerPlayerEntity lv2 : players) {
            bl = false;
            for (UUID uUID2 : this.playerUuids) {
                if (!lv2.getUuid().equals(uUID2)) continue;
                bl = true;
                break;
            }
            if (bl) continue;
            set2.add(lv2);
        }
        for (UUID uUID : set) {
            for (ServerPlayerEntity lv3 : this.getPlayers()) {
                if (!lv3.getUuid().equals(uUID)) continue;
                this.removePlayer(lv3);
                break;
            }
            this.playerUuids.remove(uUID);
        }
        for (ServerPlayerEntity lv2 : set2) {
            this.addPlayer(lv2);
        }
        return !set.isEmpty() || !set2.isEmpty();
    }

    public NbtCompound toNbt(RegistryWrapper.WrapperLookup arg) {
        NbtCompound lv = new NbtCompound();
        lv.putString("Name", Text.Serialization.toJsonString(this.name, arg));
        lv.putBoolean("Visible", this.isVisible());
        lv.putInt("Value", this.value);
        lv.putInt("Max", this.maxValue);
        lv.putString("Color", this.getColor().getName());
        lv.putString("Overlay", this.getStyle().getName());
        lv.putBoolean("DarkenScreen", this.shouldDarkenSky());
        lv.putBoolean("PlayBossMusic", this.hasDragonMusic());
        lv.putBoolean("CreateWorldFog", this.shouldThickenFog());
        NbtList lv2 = new NbtList();
        for (UUID uUID : this.playerUuids) {
            lv2.add(NbtHelper.fromUuid(uUID));
        }
        lv.put("Players", lv2);
        return lv;
    }

    public static CommandBossBar fromNbt(NbtCompound nbt, Identifier id, RegistryWrapper.WrapperLookup arg3) {
        CommandBossBar lv = new CommandBossBar(id, Text.Serialization.fromJson(nbt.getString("Name"), arg3));
        lv.setVisible(nbt.getBoolean("Visible"));
        lv.setValue(nbt.getInt("Value"));
        lv.setMaxValue(nbt.getInt("Max"));
        lv.setColor(BossBar.Color.byName(nbt.getString("Color")));
        lv.setStyle(BossBar.Style.byName(nbt.getString("Overlay")));
        lv.setDarkenSky(nbt.getBoolean("DarkenScreen"));
        lv.setDragonMusic(nbt.getBoolean("PlayBossMusic"));
        lv.setThickenFog(nbt.getBoolean("CreateWorldFog"));
        NbtList lv2 = nbt.getList("Players", NbtElement.INT_ARRAY_TYPE);
        for (NbtElement lv3 : lv2) {
            lv.addPlayer(NbtHelper.toUuid(lv3));
        }
        return lv;
    }

    public void onPlayerConnect(ServerPlayerEntity player) {
        if (this.playerUuids.contains(player.getUuid())) {
            this.addPlayer(player);
        }
    }

    public void onPlayerDisconnect(ServerPlayerEntity player) {
        super.removePlayer(player);
    }
}

