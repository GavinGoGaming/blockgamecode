/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.option;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Util;
import net.minecraft.util.thread.TaskExecutor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ServerList {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final TaskExecutor<Runnable> IO_EXECUTOR = TaskExecutor.create(Util.getMainWorkerExecutor(), "server-list-io");
    private static final int MAX_HIDDEN_ENTRIES = 16;
    private final MinecraftClient client;
    private final List<ServerInfo> servers = Lists.newArrayList();
    private final List<ServerInfo> hiddenServers = Lists.newArrayList();

    public ServerList(MinecraftClient client) {
        this.client = client;
    }

    public void loadFile() {
        try {
            this.servers.clear();
            this.hiddenServers.clear();
            NbtCompound lv = NbtIo.read(this.client.runDirectory.toPath().resolve("servers.dat"));
            if (lv == null) {
                return;
            }
            NbtList lv2 = lv.getList("servers", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < lv2.size(); ++i) {
                NbtCompound lv3 = lv2.getCompound(i);
                ServerInfo lv4 = ServerInfo.fromNbt(lv3);
                if (lv3.getBoolean("hidden")) {
                    this.hiddenServers.add(lv4);
                    continue;
                }
                this.servers.add(lv4);
            }
        } catch (Exception exception) {
            LOGGER.error("Couldn't load server list", exception);
        }
    }

    public void saveFile() {
        try {
            NbtCompound lv3;
            NbtList lv = new NbtList();
            for (ServerInfo lv2 : this.servers) {
                lv3 = lv2.toNbt();
                lv3.putBoolean("hidden", false);
                lv.add(lv3);
            }
            for (ServerInfo lv2 : this.hiddenServers) {
                lv3 = lv2.toNbt();
                lv3.putBoolean("hidden", true);
                lv.add(lv3);
            }
            NbtCompound lv4 = new NbtCompound();
            lv4.put("servers", lv);
            Path path = this.client.runDirectory.toPath();
            Path path2 = Files.createTempFile(path, "servers", ".dat", new FileAttribute[0]);
            NbtIo.write(lv4, path2);
            Path path3 = path.resolve("servers.dat_old");
            Path path4 = path.resolve("servers.dat");
            Util.backupAndReplace(path4, path2, path3);
        } catch (Exception exception) {
            LOGGER.error("Couldn't save server list", exception);
        }
    }

    public ServerInfo get(int index) {
        return this.servers.get(index);
    }

    @Nullable
    public ServerInfo get(String address) {
        for (ServerInfo lv : this.servers) {
            if (!lv.address.equals(address)) continue;
            return lv;
        }
        for (ServerInfo lv : this.hiddenServers) {
            if (!lv.address.equals(address)) continue;
            return lv;
        }
        return null;
    }

    @Nullable
    public ServerInfo tryUnhide(String address) {
        for (int i = 0; i < this.hiddenServers.size(); ++i) {
            ServerInfo lv = this.hiddenServers.get(i);
            if (!lv.address.equals(address)) continue;
            this.hiddenServers.remove(i);
            this.servers.add(lv);
            return lv;
        }
        return null;
    }

    public void remove(ServerInfo serverInfo) {
        if (!this.servers.remove(serverInfo)) {
            this.hiddenServers.remove(serverInfo);
        }
    }

    public void add(ServerInfo serverInfo, boolean hidden) {
        if (hidden) {
            this.hiddenServers.add(0, serverInfo);
            while (this.hiddenServers.size() > 16) {
                this.hiddenServers.remove(this.hiddenServers.size() - 1);
            }
        } else {
            this.servers.add(serverInfo);
        }
    }

    public int size() {
        return this.servers.size();
    }

    public void swapEntries(int index1, int index2) {
        ServerInfo lv = this.get(index1);
        this.servers.set(index1, this.get(index2));
        this.servers.set(index2, lv);
        this.saveFile();
    }

    public void set(int index, ServerInfo serverInfo) {
        this.servers.set(index, serverInfo);
    }

    private static boolean replace(ServerInfo serverInfo, List<ServerInfo> serverInfos) {
        for (int i = 0; i < serverInfos.size(); ++i) {
            ServerInfo lv = serverInfos.get(i);
            if (!lv.name.equals(serverInfo.name) || !lv.address.equals(serverInfo.address)) continue;
            serverInfos.set(i, serverInfo);
            return true;
        }
        return false;
    }

    public static void updateServerListEntry(ServerInfo serverInfo) {
        IO_EXECUTOR.send(() -> {
            ServerList lv = new ServerList(MinecraftClient.getInstance());
            lv.loadFile();
            if (!ServerList.replace(serverInfo, lv.servers)) {
                ServerList.replace(serverInfo, lv.hiddenServers);
            }
            lv.saveFile();
        });
    }
}

