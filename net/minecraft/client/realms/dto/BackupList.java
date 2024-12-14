/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.Backup;
import net.minecraft.client.realms.dto.ValueObject;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class BackupList
extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public List<Backup> backups;

    public static BackupList parse(String json) {
        JsonParser jsonParser = new JsonParser();
        BackupList lv = new BackupList();
        lv.backups = Lists.newArrayList();
        try {
            JsonElement jsonElement = jsonParser.parse(json).getAsJsonObject().get("backups");
            if (jsonElement.isJsonArray()) {
                Iterator<JsonElement> iterator = jsonElement.getAsJsonArray().iterator();
                while (iterator.hasNext()) {
                    lv.backups.add(Backup.parse(iterator.next()));
                }
            }
        } catch (Exception exception) {
            LOGGER.error("Could not parse BackupList: {}", (Object)exception.getMessage());
        }
        return lv;
    }
}

