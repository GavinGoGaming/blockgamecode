/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.advancement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementDisplays;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.CriterionProgress;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SelectAdvancementTabS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PathUtil;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class PlayerAdvancementTracker {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final PlayerManager playerManager;
    private final Path filePath;
    private AdvancementManager advancementManager;
    private final Map<AdvancementEntry, AdvancementProgress> progress = new LinkedHashMap<AdvancementEntry, AdvancementProgress>();
    private final Set<AdvancementEntry> visibleAdvancements = new HashSet<AdvancementEntry>();
    private final Set<AdvancementEntry> progressUpdates = new HashSet<AdvancementEntry>();
    private final Set<PlacedAdvancement> updatedRoots = new HashSet<PlacedAdvancement>();
    private ServerPlayerEntity owner;
    @Nullable
    private AdvancementEntry currentDisplayTab;
    private boolean dirty = true;
    private final Codec<ProgressMap> progressMapCodec;

    public PlayerAdvancementTracker(DataFixer dataFixer, PlayerManager playerManager, ServerAdvancementLoader advancementLoader, Path filePath, ServerPlayerEntity owner) {
        this.playerManager = playerManager;
        this.filePath = filePath;
        this.owner = owner;
        this.advancementManager = advancementLoader.getManager();
        int i = 1343;
        this.progressMapCodec = DataFixTypes.ADVANCEMENTS.createDataFixingCodec(ProgressMap.CODEC, dataFixer, 1343);
        this.load(advancementLoader);
    }

    public void setOwner(ServerPlayerEntity owner) {
        this.owner = owner;
    }

    public void clearCriteria() {
        for (Criterion criterion : Registries.CRITERION) {
            criterion.endTracking(this);
        }
    }

    public void reload(ServerAdvancementLoader advancementLoader) {
        this.clearCriteria();
        this.progress.clear();
        this.visibleAdvancements.clear();
        this.updatedRoots.clear();
        this.progressUpdates.clear();
        this.dirty = true;
        this.currentDisplayTab = null;
        this.advancementManager = advancementLoader.getManager();
        this.load(advancementLoader);
    }

    private void beginTrackingAllAdvancements(ServerAdvancementLoader advancementLoader) {
        for (AdvancementEntry lv : advancementLoader.getAdvancements()) {
            this.beginTracking(lv);
        }
    }

    private void rewardEmptyAdvancements(ServerAdvancementLoader advancementLoader) {
        for (AdvancementEntry lv : advancementLoader.getAdvancements()) {
            Advancement lv2 = lv.value();
            if (!lv2.criteria().isEmpty()) continue;
            this.grantCriterion(lv, "");
            lv2.rewards().apply(this.owner);
        }
    }

    private void load(ServerAdvancementLoader advancementLoader) {
        if (Files.isRegularFile(this.filePath, new LinkOption[0])) {
            try (JsonReader jsonReader = new JsonReader(Files.newBufferedReader(this.filePath, StandardCharsets.UTF_8));){
                jsonReader.setLenient(false);
                JsonElement jsonElement = Streams.parse(jsonReader);
                ProgressMap lv = (ProgressMap)this.progressMapCodec.parse(JsonOps.INSTANCE, jsonElement).getOrThrow(JsonParseException::new);
                this.loadProgressMap(advancementLoader, lv);
            } catch (JsonIOException | IOException exception) {
                LOGGER.error("Couldn't access player advancements in {}", (Object)this.filePath, (Object)exception);
            } catch (JsonParseException jsonParseException) {
                LOGGER.error("Couldn't parse player advancements in {}", (Object)this.filePath, (Object)jsonParseException);
            }
        }
        this.rewardEmptyAdvancements(advancementLoader);
        this.beginTrackingAllAdvancements(advancementLoader);
    }

    public void save() {
        JsonElement jsonElement = this.progressMapCodec.encodeStart(JsonOps.INSTANCE, this.createProgressMap()).getOrThrow();
        try {
            PathUtil.createDirectories(this.filePath.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(this.filePath, StandardCharsets.UTF_8, new OpenOption[0]);){
                GSON.toJson(jsonElement, GSON.newJsonWriter(writer));
            }
        } catch (JsonIOException | IOException exception) {
            LOGGER.error("Couldn't save player advancements to {}", (Object)this.filePath, (Object)exception);
        }
    }

    private void loadProgressMap(ServerAdvancementLoader loader, ProgressMap progressMap) {
        progressMap.forEach((id, progress) -> {
            AdvancementEntry lv = loader.get((Identifier)id);
            if (lv == null) {
                LOGGER.warn("Ignored advancement '{}' in progress file {} - it doesn't exist anymore?", id, (Object)this.filePath);
                return;
            }
            this.initProgress(lv, (AdvancementProgress)progress);
            this.progressUpdates.add(lv);
            this.onStatusUpdate(lv);
        });
    }

    private ProgressMap createProgressMap() {
        LinkedHashMap<Identifier, AdvancementProgress> map = new LinkedHashMap<Identifier, AdvancementProgress>();
        this.progress.forEach((entry, progress) -> {
            if (progress.isAnyObtained()) {
                map.put(entry.id(), (AdvancementProgress)progress);
            }
        });
        return new ProgressMap(map);
    }

    public boolean grantCriterion(AdvancementEntry advancement, String criterionName) {
        boolean bl = false;
        AdvancementProgress lv = this.getProgress(advancement);
        boolean bl2 = lv.isDone();
        if (lv.obtain(criterionName)) {
            this.endTrackingCompleted(advancement);
            this.progressUpdates.add(advancement);
            bl = true;
            if (!bl2 && lv.isDone()) {
                advancement.value().rewards().apply(this.owner);
                advancement.value().display().ifPresent(display -> {
                    if (display.shouldAnnounceToChat() && this.owner.getWorld().getGameRules().getBoolean(GameRules.ANNOUNCE_ADVANCEMENTS)) {
                        this.playerManager.broadcast(display.getFrame().getChatAnnouncementText(advancement, this.owner), false);
                    }
                });
            }
        }
        if (!bl2 && lv.isDone()) {
            this.onStatusUpdate(advancement);
        }
        return bl;
    }

    public boolean revokeCriterion(AdvancementEntry advancement, String criterionName) {
        boolean bl = false;
        AdvancementProgress lv = this.getProgress(advancement);
        boolean bl2 = lv.isDone();
        if (lv.reset(criterionName)) {
            this.beginTracking(advancement);
            this.progressUpdates.add(advancement);
            bl = true;
        }
        if (bl2 && !lv.isDone()) {
            this.onStatusUpdate(advancement);
        }
        return bl;
    }

    private void onStatusUpdate(AdvancementEntry advancement) {
        PlacedAdvancement lv = this.advancementManager.get(advancement);
        if (lv != null) {
            this.updatedRoots.add(lv.getRoot());
        }
    }

    private void beginTracking(AdvancementEntry advancement) {
        AdvancementProgress lv = this.getProgress(advancement);
        if (lv.isDone()) {
            return;
        }
        for (Map.Entry<String, AdvancementCriterion<?>> entry : advancement.value().criteria().entrySet()) {
            CriterionProgress lv2 = lv.getCriterionProgress(entry.getKey());
            if (lv2 == null || lv2.isObtained()) continue;
            this.beginTracking(advancement, entry.getKey(), entry.getValue());
        }
    }

    private <T extends CriterionConditions> void beginTracking(AdvancementEntry advancement, String id, AdvancementCriterion<T> criterion) {
        criterion.trigger().beginTrackingCondition(this, new Criterion.ConditionsContainer<T>(criterion.conditions(), advancement, id));
    }

    private void endTrackingCompleted(AdvancementEntry advancement) {
        AdvancementProgress lv = this.getProgress(advancement);
        for (Map.Entry<String, AdvancementCriterion<?>> entry : advancement.value().criteria().entrySet()) {
            CriterionProgress lv2 = lv.getCriterionProgress(entry.getKey());
            if (lv2 == null || !lv2.isObtained() && !lv.isDone()) continue;
            this.endTrackingCompleted(advancement, entry.getKey(), entry.getValue());
        }
    }

    private <T extends CriterionConditions> void endTrackingCompleted(AdvancementEntry advancement, String id, AdvancementCriterion<T> criterion) {
        criterion.trigger().endTrackingCondition(this, new Criterion.ConditionsContainer<T>(criterion.conditions(), advancement, id));
    }

    public void sendUpdate(ServerPlayerEntity player) {
        if (this.dirty || !this.updatedRoots.isEmpty() || !this.progressUpdates.isEmpty()) {
            HashMap<Identifier, AdvancementProgress> map = new HashMap<Identifier, AdvancementProgress>();
            HashSet<AdvancementEntry> set = new HashSet<AdvancementEntry>();
            HashSet<Identifier> set2 = new HashSet<Identifier>();
            for (PlacedAdvancement lv : this.updatedRoots) {
                this.calculateDisplay(lv, set, set2);
            }
            this.updatedRoots.clear();
            for (AdvancementEntry lv2 : this.progressUpdates) {
                if (!this.visibleAdvancements.contains(lv2)) continue;
                map.put(lv2.id(), this.progress.get(lv2));
            }
            this.progressUpdates.clear();
            if (!(map.isEmpty() && set.isEmpty() && set2.isEmpty())) {
                player.networkHandler.sendPacket(new AdvancementUpdateS2CPacket(this.dirty, set, set2, map));
            }
        }
        this.dirty = false;
    }

    public void setDisplayTab(@Nullable AdvancementEntry advancement) {
        AdvancementEntry lv = this.currentDisplayTab;
        this.currentDisplayTab = advancement != null && advancement.value().isRoot() && advancement.value().display().isPresent() ? advancement : null;
        if (lv != this.currentDisplayTab) {
            this.owner.networkHandler.sendPacket(new SelectAdvancementTabS2CPacket(this.currentDisplayTab == null ? null : this.currentDisplayTab.id()));
        }
    }

    public AdvancementProgress getProgress(AdvancementEntry advancement) {
        AdvancementProgress lv = this.progress.get(advancement);
        if (lv == null) {
            lv = new AdvancementProgress();
            this.initProgress(advancement, lv);
        }
        return lv;
    }

    private void initProgress(AdvancementEntry advancement, AdvancementProgress progress) {
        progress.init(advancement.value().requirements());
        this.progress.put(advancement, progress);
    }

    private void calculateDisplay(PlacedAdvancement root, Set<AdvancementEntry> added, Set<Identifier> removed) {
        AdvancementDisplays.calculateDisplay(root, advancement -> this.getProgress(advancement.getAdvancementEntry()).isDone(), (advancement, displayed) -> {
            AdvancementEntry lv = advancement.getAdvancementEntry();
            if (displayed) {
                if (this.visibleAdvancements.add(lv)) {
                    added.add(lv);
                    if (this.progress.containsKey(lv)) {
                        this.progressUpdates.add(lv);
                    }
                }
            } else if (this.visibleAdvancements.remove(lv)) {
                removed.add(lv.id());
            }
        });
    }

    record ProgressMap(Map<Identifier, AdvancementProgress> map) {
        public static final Codec<ProgressMap> CODEC = Codec.unboundedMap(Identifier.CODEC, AdvancementProgress.CODEC).xmap(ProgressMap::new, ProgressMap::map);

        public void forEach(BiConsumer<Identifier, AdvancementProgress> consumer) {
            this.map.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach((? super T entry) -> consumer.accept((Identifier)entry.getKey(), (AdvancementProgress)entry.getValue()));
        }
    }
}

