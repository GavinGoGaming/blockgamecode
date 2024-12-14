/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.Collection;
import java.util.Map;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.advancement.AdvancementPositioner;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerAdvancementLoader
extends JsonDataLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private Map<Identifier, AdvancementEntry> advancements = Map.of();
    private AdvancementManager manager = new AdvancementManager();
    private final RegistryWrapper.WrapperLookup registryLookup;

    public ServerAdvancementLoader(RegistryWrapper.WrapperLookup registryLookup) {
        super(GSON, RegistryKeys.getPath(RegistryKeys.ADVANCEMENT));
        this.registryLookup = registryLookup;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> map, ResourceManager arg, Profiler arg2) {
        RegistryOps<JsonElement> lv = this.registryLookup.getOps(JsonOps.INSTANCE);
        ImmutableMap.Builder builder = ImmutableMap.builder();
        map.forEach((id, json) -> {
            try {
                Advancement lv = (Advancement)Advancement.CODEC.parse(lv, json).getOrThrow(JsonParseException::new);
                this.validate((Identifier)id, lv);
                builder.put(id, new AdvancementEntry((Identifier)id, lv));
            } catch (Exception exception) {
                LOGGER.error("Parsing error loading custom advancement {}: {}", id, (Object)exception.getMessage());
            }
        });
        this.advancements = builder.buildOrThrow();
        AdvancementManager lv2 = new AdvancementManager();
        lv2.addAll(this.advancements.values());
        for (PlacedAdvancement lv3 : lv2.getRoots()) {
            if (!lv3.getAdvancementEntry().value().display().isPresent()) continue;
            AdvancementPositioner.arrangeForTree(lv3);
        }
        this.manager = lv2;
    }

    private void validate(Identifier id, Advancement advancement) {
        ErrorReporter.Impl lv = new ErrorReporter.Impl();
        advancement.validate(lv, this.registryLookup.createRegistryLookup());
        lv.getErrorsAsString().ifPresent(string -> LOGGER.warn("Found validation problems in advancement {}: \n{}", (Object)id, string));
    }

    @Nullable
    public AdvancementEntry get(Identifier id) {
        return this.advancements.get(id);
    }

    public AdvancementManager getManager() {
        return this.manager;
    }

    public Collection<AdvancementEntry> getAdvancements() {
        return this.advancements.values();
    }
}

