/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.network;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import net.minecraft.util.PngMetadata;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ServerInfo {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_FAVICON_SIZE = 1024;
    public String name;
    public String address;
    public Text playerCountLabel;
    public Text label;
    @Nullable
    public ServerMetadata.Players players;
    public long ping;
    public int protocolVersion = SharedConstants.getGameVersion().getProtocolVersion();
    public Text version = Text.literal(SharedConstants.getGameVersion().getName());
    public List<Text> playerListSummary = Collections.emptyList();
    private ResourcePackPolicy resourcePackPolicy = ResourcePackPolicy.PROMPT;
    @Nullable
    private byte[] favicon;
    private ServerType serverType;
    private Status status = Status.INITIAL;

    public ServerInfo(String name, String address, ServerType serverType) {
        this.name = name;
        this.address = address;
        this.serverType = serverType;
    }

    public NbtCompound toNbt() {
        NbtCompound lv = new NbtCompound();
        lv.putString("name", this.name);
        lv.putString("ip", this.address);
        if (this.favicon != null) {
            lv.putString("icon", Base64.getEncoder().encodeToString(this.favicon));
        }
        if (this.resourcePackPolicy == ResourcePackPolicy.ENABLED) {
            lv.putBoolean("acceptTextures", true);
        } else if (this.resourcePackPolicy == ResourcePackPolicy.DISABLED) {
            lv.putBoolean("acceptTextures", false);
        }
        return lv;
    }

    public ResourcePackPolicy getResourcePackPolicy() {
        return this.resourcePackPolicy;
    }

    public void setResourcePackPolicy(ResourcePackPolicy resourcePackPolicy) {
        this.resourcePackPolicy = resourcePackPolicy;
    }

    public static ServerInfo fromNbt(NbtCompound root) {
        ServerInfo lv = new ServerInfo(root.getString("name"), root.getString("ip"), ServerType.OTHER);
        if (root.contains("icon", NbtElement.STRING_TYPE)) {
            try {
                byte[] bs = Base64.getDecoder().decode(root.getString("icon"));
                lv.setFavicon(ServerInfo.validateFavicon(bs));
            } catch (IllegalArgumentException illegalArgumentException) {
                LOGGER.warn("Malformed base64 server icon", illegalArgumentException);
            }
        }
        if (root.contains("acceptTextures", NbtElement.BYTE_TYPE)) {
            if (root.getBoolean("acceptTextures")) {
                lv.setResourcePackPolicy(ResourcePackPolicy.ENABLED);
            } else {
                lv.setResourcePackPolicy(ResourcePackPolicy.DISABLED);
            }
        } else {
            lv.setResourcePackPolicy(ResourcePackPolicy.PROMPT);
        }
        return lv;
    }

    @Nullable
    public byte[] getFavicon() {
        return this.favicon;
    }

    public void setFavicon(@Nullable byte[] favicon) {
        this.favicon = favicon;
    }

    public boolean isLocal() {
        return this.serverType == ServerType.LAN;
    }

    public boolean isRealm() {
        return this.serverType == ServerType.REALM;
    }

    public ServerType getServerType() {
        return this.serverType;
    }

    public void copyFrom(ServerInfo serverInfo) {
        this.address = serverInfo.address;
        this.name = serverInfo.name;
        this.favicon = serverInfo.favicon;
    }

    public void copyWithSettingsFrom(ServerInfo serverInfo) {
        this.copyFrom(serverInfo);
        this.setResourcePackPolicy(serverInfo.getResourcePackPolicy());
        this.serverType = serverInfo.serverType;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Nullable
    public static byte[] validateFavicon(@Nullable byte[] favicon) {
        if (favicon != null) {
            try {
                PngMetadata lv = PngMetadata.fromBytes(favicon);
                if (lv.width() <= 1024 && lv.height() <= 1024) {
                    return favicon;
                }
            } catch (IOException iOException) {
                LOGGER.warn("Failed to decode server icon", iOException);
            }
        }
        return null;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum ResourcePackPolicy {
        ENABLED("enabled"),
        DISABLED("disabled"),
        PROMPT("prompt");

        private final Text name;

        private ResourcePackPolicy(String name) {
            this.name = Text.translatable("addServer.resourcePack." + name);
        }

        public Text getName() {
            return this.name;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Status {
        INITIAL,
        PINGING,
        UNREACHABLE,
        INCOMPATIBLE,
        SUCCESSFUL;

    }

    @Environment(value=EnvType.CLIENT)
    public static enum ServerType {
        LAN,
        REALM,
        OTHER;

    }
}

