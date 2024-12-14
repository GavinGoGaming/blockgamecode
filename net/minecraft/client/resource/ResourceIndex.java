/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.resource;

import com.google.common.base.Splitter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.fs.ResourceFileSystem;
import net.minecraft.util.JsonHelper;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ResourceIndex {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Splitter SEPARATOR_SPLITTER = Splitter.on('/');

    public static Path buildFileSystem(Path assetsDir, String indexName) {
        Path path2 = assetsDir.resolve("objects");
        ResourceFileSystem.Builder lv = ResourceFileSystem.builder();
        Path path3 = assetsDir.resolve("indexes/" + indexName + ".json");
        try (BufferedReader bufferedReader = Files.newBufferedReader(path3, StandardCharsets.UTF_8);){
            JsonObject jsonObject = JsonHelper.deserialize(bufferedReader);
            JsonObject jsonObject2 = JsonHelper.getObject(jsonObject, "objects", null);
            if (jsonObject2 != null) {
                for (Map.Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
                    JsonObject jsonObject3 = (JsonObject)entry.getValue();
                    String string2 = entry.getKey();
                    List<String> list = SEPARATOR_SPLITTER.splitToList(string2);
                    String string3 = JsonHelper.getString(jsonObject3, "hash");
                    Path path4 = path2.resolve(string3.substring(0, 2) + "/" + string3);
                    lv.withFile(list, path4);
                }
            }
        } catch (JsonParseException jsonParseException) {
            LOGGER.error("Unable to parse resource index file: {}", (Object)path3);
        } catch (IOException iOException) {
            LOGGER.error("Can't open the resource index file: {}", (Object)path3);
        }
        return lv.build("index-" + indexName).getPath("/", new String[0]);
    }
}

