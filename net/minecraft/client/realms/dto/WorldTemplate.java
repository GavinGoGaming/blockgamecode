/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.realms.dto;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.ValueObject;
import net.minecraft.client.realms.util.JsonUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class WorldTemplate
extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public String id = "";
    public String name = "";
    public String version = "";
    public String author = "";
    public String link = "";
    @Nullable
    public String image;
    public String trailer = "";
    public String recommendedPlayers = "";
    public WorldTemplateType type = WorldTemplateType.WORLD_TEMPLATE;

    public static WorldTemplate parse(JsonObject node) {
        WorldTemplate lv = new WorldTemplate();
        try {
            lv.id = JsonUtils.getNullableStringOr("id", node, "");
            lv.name = JsonUtils.getNullableStringOr("name", node, "");
            lv.version = JsonUtils.getNullableStringOr("version", node, "");
            lv.author = JsonUtils.getNullableStringOr("author", node, "");
            lv.link = JsonUtils.getNullableStringOr("link", node, "");
            lv.image = JsonUtils.getNullableStringOr("image", node, null);
            lv.trailer = JsonUtils.getNullableStringOr("trailer", node, "");
            lv.recommendedPlayers = JsonUtils.getNullableStringOr("recommendedPlayers", node, "");
            lv.type = WorldTemplateType.valueOf(JsonUtils.getNullableStringOr("type", node, WorldTemplateType.WORLD_TEMPLATE.name()));
        } catch (Exception exception) {
            LOGGER.error("Could not parse WorldTemplate: {}", (Object)exception.getMessage());
        }
        return lv;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum WorldTemplateType {
        WORLD_TEMPLATE,
        MINIGAME,
        ADVENTUREMAP,
        EXPERIENCE,
        INSPIRATION;

    }
}

