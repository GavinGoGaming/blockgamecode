/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.boss;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class BossBarManager {
    private final Map<Identifier, CommandBossBar> commandBossBars = Maps.newHashMap();

    @Nullable
    public CommandBossBar get(Identifier id) {
        return this.commandBossBars.get(id);
    }

    public CommandBossBar add(Identifier id, Text displayName) {
        CommandBossBar lv = new CommandBossBar(id, displayName);
        this.commandBossBars.put(id, lv);
        return lv;
    }

    public void remove(CommandBossBar bossBar) {
        this.commandBossBars.remove(bossBar.getId());
    }

    public Collection<Identifier> getIds() {
        return this.commandBossBars.keySet();
    }

    public Collection<CommandBossBar> getAll() {
        return this.commandBossBars.values();
    }

    public NbtCompound toNbt(RegistryWrapper.WrapperLookup arg) {
        NbtCompound lv = new NbtCompound();
        for (CommandBossBar lv2 : this.commandBossBars.values()) {
            lv.put(lv2.getId().toString(), lv2.toNbt(arg));
        }
        return lv;
    }

    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup arg2) {
        for (String string : nbt.getKeys()) {
            Identifier lv = Identifier.of(string);
            this.commandBossBars.put(lv, CommandBossBar.fromNbt(nbt.getCompound(string), lv, arg2));
        }
    }

    public void onPlayerConnect(ServerPlayerEntity player) {
        for (CommandBossBar lv : this.commandBossBars.values()) {
            lv.onPlayerConnect(player);
        }
    }

    public void onPlayerDisconnect(ServerPlayerEntity player) {
        for (CommandBossBar lv : this.commandBossBars.values()) {
            lv.onPlayerDisconnect(player);
        }
    }
}

