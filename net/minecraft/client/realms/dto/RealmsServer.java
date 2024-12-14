/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.realms.dto;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.realms.dto.PlayerInfo;
import net.minecraft.client.realms.dto.RealmsWorldOptions;
import net.minecraft.client.realms.dto.ValueObject;
import net.minecraft.client.realms.util.JsonUtils;
import net.minecraft.util.Util;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsServer
extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int NO_PARENT = -1;
    public long id;
    public String remoteSubscriptionId;
    public String name;
    public String description;
    public State state;
    public String owner;
    public UUID ownerUUID = Util.NIL_UUID;
    public List<PlayerInfo> players;
    public Map<Integer, RealmsWorldOptions> slots;
    public boolean expired;
    public boolean expiredTrial;
    public int daysLeft;
    public WorldType worldType;
    public int activeSlot;
    @Nullable
    public String minigameName;
    public int minigameId;
    public String minigameImage;
    public long parentWorldId = -1L;
    @Nullable
    public String parentWorldName;
    public String activeVersion = "";
    public Compatibility compatibility = Compatibility.UNVERIFIABLE;

    public String getDescription() {
        return this.description;
    }

    public String getName() {
        return this.name;
    }

    @Nullable
    public String getMinigameName() {
        return this.minigameName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static RealmsServer parse(JsonObject node) {
        RealmsServer lv = new RealmsServer();
        try {
            lv.id = JsonUtils.getLongOr("id", node, -1L);
            lv.remoteSubscriptionId = JsonUtils.getNullableStringOr("remoteSubscriptionId", node, null);
            lv.name = JsonUtils.getNullableStringOr("name", node, null);
            lv.description = JsonUtils.getNullableStringOr("motd", node, null);
            lv.state = RealmsServer.getState(JsonUtils.getNullableStringOr("state", node, State.CLOSED.name()));
            lv.owner = JsonUtils.getNullableStringOr("owner", node, null);
            if (node.get("players") != null && node.get("players").isJsonArray()) {
                lv.players = RealmsServer.parseInvited(node.get("players").getAsJsonArray());
                RealmsServer.sortInvited(lv);
            } else {
                lv.players = Lists.newArrayList();
            }
            lv.daysLeft = JsonUtils.getIntOr("daysLeft", node, 0);
            lv.expired = JsonUtils.getBooleanOr("expired", node, false);
            lv.expiredTrial = JsonUtils.getBooleanOr("expiredTrial", node, false);
            lv.worldType = RealmsServer.getWorldType(JsonUtils.getNullableStringOr("worldType", node, WorldType.NORMAL.name()));
            lv.ownerUUID = JsonUtils.getUuidOr("ownerUUID", node, Util.NIL_UUID);
            lv.slots = node.get("slots") != null && node.get("slots").isJsonArray() ? RealmsServer.parseSlots(node.get("slots").getAsJsonArray()) : RealmsServer.getEmptySlots();
            lv.minigameName = JsonUtils.getNullableStringOr("minigameName", node, null);
            lv.activeSlot = JsonUtils.getIntOr("activeSlot", node, -1);
            lv.minigameId = JsonUtils.getIntOr("minigameId", node, -1);
            lv.minigameImage = JsonUtils.getNullableStringOr("minigameImage", node, null);
            lv.parentWorldId = JsonUtils.getLongOr("parentWorldId", node, -1L);
            lv.parentWorldName = JsonUtils.getNullableStringOr("parentWorldName", node, null);
            lv.activeVersion = JsonUtils.getNullableStringOr("activeVersion", node, "");
            lv.compatibility = RealmsServer.getCompatibility(JsonUtils.getNullableStringOr("compatibility", node, Compatibility.UNVERIFIABLE.name()));
        } catch (Exception exception) {
            LOGGER.error("Could not parse McoServer: {}", (Object)exception.getMessage());
        }
        return lv;
    }

    private static void sortInvited(RealmsServer server) {
        server.players.sort((a, b) -> ComparisonChain.start().compareFalseFirst(b.isAccepted(), a.isAccepted()).compare((Comparable<?>)((Object)a.getName().toLowerCase(Locale.ROOT)), (Comparable<?>)((Object)b.getName().toLowerCase(Locale.ROOT))).result());
    }

    private static List<PlayerInfo> parseInvited(JsonArray jsonArray) {
        ArrayList<PlayerInfo> list = Lists.newArrayList();
        for (JsonElement jsonElement : jsonArray) {
            try {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                PlayerInfo lv = new PlayerInfo();
                lv.setName(JsonUtils.getNullableStringOr("name", jsonObject, null));
                lv.setUuid(JsonUtils.getUuidOr("uuid", jsonObject, Util.NIL_UUID));
                lv.setOperator(JsonUtils.getBooleanOr("operator", jsonObject, false));
                lv.setAccepted(JsonUtils.getBooleanOr("accepted", jsonObject, false));
                lv.setOnline(JsonUtils.getBooleanOr("online", jsonObject, false));
                list.add(lv);
            } catch (Exception exception) {}
        }
        return list;
    }

    private static Map<Integer, RealmsWorldOptions> parseSlots(JsonArray json) {
        HashMap<Integer, RealmsWorldOptions> map = Maps.newHashMap();
        for (JsonElement jsonElement : json) {
            try {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                JsonParser jsonParser = new JsonParser();
                JsonElement jsonElement2 = jsonParser.parse(jsonObject.get("options").getAsString());
                RealmsWorldOptions lv = jsonElement2 == null ? RealmsWorldOptions.getDefaults() : RealmsWorldOptions.parse(jsonElement2.getAsJsonObject());
                int i = JsonUtils.getIntOr("slotId", jsonObject, -1);
                map.put(i, lv);
            } catch (Exception exception) {}
        }
        for (int j = 1; j <= 3; ++j) {
            if (map.containsKey(j)) continue;
            map.put(j, RealmsWorldOptions.getEmptyDefaults());
        }
        return map;
    }

    private static Map<Integer, RealmsWorldOptions> getEmptySlots() {
        HashMap<Integer, RealmsWorldOptions> map = Maps.newHashMap();
        map.put(1, RealmsWorldOptions.getEmptyDefaults());
        map.put(2, RealmsWorldOptions.getEmptyDefaults());
        map.put(3, RealmsWorldOptions.getEmptyDefaults());
        return map;
    }

    public static RealmsServer parse(String json) {
        try {
            return RealmsServer.parse(new JsonParser().parse(json).getAsJsonObject());
        } catch (Exception exception) {
            LOGGER.error("Could not parse McoServer: {}", (Object)exception.getMessage());
            return new RealmsServer();
        }
    }

    private static State getState(String state) {
        try {
            return State.valueOf(state);
        } catch (Exception exception) {
            return State.CLOSED;
        }
    }

    private static WorldType getWorldType(String worldType) {
        try {
            return WorldType.valueOf(worldType);
        } catch (Exception exception) {
            return WorldType.NORMAL;
        }
    }

    public static Compatibility getCompatibility(@Nullable String compatibility) {
        try {
            return Compatibility.valueOf(compatibility);
        } catch (Exception exception) {
            return Compatibility.UNVERIFIABLE;
        }
    }

    public boolean isCompatible() {
        return this.compatibility.isCompatible();
    }

    public boolean needsUpgrade() {
        return this.compatibility.needsUpgrade();
    }

    public boolean needsDowngrade() {
        return this.compatibility.needsDowngrade();
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.id, this.name, this.description, this.state, this.owner, this.expired});
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        RealmsServer lv = (RealmsServer)o;
        return new EqualsBuilder().append(this.id, lv.id).append(this.name, lv.name).append(this.description, lv.description).append((Object)this.state, (Object)lv.state).append(this.owner, lv.owner).append(this.expired, lv.expired).append((Object)this.worldType, (Object)this.worldType).isEquals();
    }

    public RealmsServer clone() {
        RealmsServer lv = new RealmsServer();
        lv.id = this.id;
        lv.remoteSubscriptionId = this.remoteSubscriptionId;
        lv.name = this.name;
        lv.description = this.description;
        lv.state = this.state;
        lv.owner = this.owner;
        lv.players = this.players;
        lv.slots = this.cloneSlots(this.slots);
        lv.expired = this.expired;
        lv.expiredTrial = this.expiredTrial;
        lv.daysLeft = this.daysLeft;
        lv.worldType = this.worldType;
        lv.ownerUUID = this.ownerUUID;
        lv.minigameName = this.minigameName;
        lv.activeSlot = this.activeSlot;
        lv.minigameId = this.minigameId;
        lv.minigameImage = this.minigameImage;
        lv.parentWorldName = this.parentWorldName;
        lv.parentWorldId = this.parentWorldId;
        lv.activeVersion = this.activeVersion;
        lv.compatibility = this.compatibility;
        return lv;
    }

    public Map<Integer, RealmsWorldOptions> cloneSlots(Map<Integer, RealmsWorldOptions> slots) {
        HashMap<Integer, RealmsWorldOptions> map2 = Maps.newHashMap();
        for (Map.Entry<Integer, RealmsWorldOptions> entry : slots.entrySet()) {
            map2.put(entry.getKey(), entry.getValue().clone());
        }
        return map2;
    }

    public boolean hasParentWorld() {
        return this.parentWorldId != -1L;
    }

    public boolean isMinigame() {
        return this.worldType == WorldType.MINIGAME;
    }

    public String getWorldName(int slotId) {
        return this.name + " (" + this.slots.get(slotId).getSlotName(slotId) + ")";
    }

    public ServerInfo createServerInfo(String address) {
        return new ServerInfo(this.name, address, ServerInfo.ServerType.REALM);
    }

    public /* synthetic */ Object clone() throws CloneNotSupportedException {
        return this.clone();
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Compatibility {
        UNVERIFIABLE,
        INCOMPATIBLE,
        RELEASE_TYPE_INCOMPATIBLE,
        NEEDS_DOWNGRADE,
        NEEDS_UPGRADE,
        COMPATIBLE;


        public boolean isCompatible() {
            return this == COMPATIBLE;
        }

        public boolean needsUpgrade() {
            return this == NEEDS_UPGRADE;
        }

        public boolean needsDowngrade() {
            return this == NEEDS_DOWNGRADE;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum State {
        CLOSED,
        OPEN,
        UNINITIALIZED;

    }

    @Environment(value=EnvType.CLIENT)
    public static enum WorldType {
        NORMAL,
        MINIGAME,
        ADVENTUREMAP,
        EXPERIENCE,
        INSPIRATION;

    }

    @Environment(value=EnvType.CLIENT)
    public static class McoServerComparator
    implements Comparator<RealmsServer> {
        private final String refOwner;

        public McoServerComparator(String owner) {
            this.refOwner = owner;
        }

        @Override
        public int compare(RealmsServer arg, RealmsServer arg2) {
            return ComparisonChain.start().compareTrueFirst(arg.hasParentWorld(), arg2.hasParentWorld()).compareTrueFirst(arg.state == State.UNINITIALIZED, arg2.state == State.UNINITIALIZED).compareTrueFirst(arg.expiredTrial, arg2.expiredTrial).compareTrueFirst(arg.owner.equals(this.refOwner), arg2.owner.equals(this.refOwner)).compareFalseFirst(arg.expired, arg2.expired).compareTrueFirst(arg.state == State.OPEN, arg2.state == State.OPEN).compare(arg.id, arg2.id).result();
        }

        @Override
        public /* synthetic */ int compare(Object one, Object two) {
            return this.compare((RealmsServer)one, (RealmsServer)two);
        }
    }
}

