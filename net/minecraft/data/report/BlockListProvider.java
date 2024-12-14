/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.report;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockTypes;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Util;

public class BlockListProvider
implements DataProvider {
    private final DataOutput output;
    private final CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture;

    public BlockListProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture) {
        this.output = output;
        this.registryLookupFuture = registryLookupFuture;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        Path path = this.output.resolvePath(DataOutput.OutputType.REPORTS).resolve("blocks.json");
        return this.registryLookupFuture.thenCompose(registryLookup -> {
            JsonObject jsonObject = new JsonObject();
            RegistryOps<JsonElement> lv = registryLookup.getOps(JsonOps.INSTANCE);
            registryLookup.getWrapperOrThrow(RegistryKeys.BLOCK).streamEntries().forEach(entry -> {
                JsonObject jsonObject2 = new JsonObject();
                StateManager<Block, BlockState> lv = ((Block)entry.value()).getStateManager();
                if (!lv.getProperties().isEmpty()) {
                    JsonObject jsonObject3 = new JsonObject();
                    for (Property property : lv.getProperties()) {
                        JsonArray jsonArray = new JsonArray();
                        for (Comparable comparable : property.getValues()) {
                            jsonArray.add(Util.getValueAsString(property, comparable));
                        }
                        jsonObject3.add(property.getName(), jsonArray);
                    }
                    jsonObject2.add("properties", jsonObject3);
                }
                JsonArray jsonArray2 = new JsonArray();
                for (BlockState blockState : lv.getStates()) {
                    JsonObject jsonObject4 = new JsonObject();
                    JsonObject jsonObject5 = new JsonObject();
                    for (Property<?> lv4 : lv.getProperties()) {
                        jsonObject5.addProperty(lv4.getName(), Util.getValueAsString(lv4, blockState.get(lv4)));
                    }
                    if (jsonObject5.size() > 0) {
                        jsonObject4.add("properties", jsonObject5);
                    }
                    jsonObject4.addProperty("id", Block.getRawIdFromState(blockState));
                    if (blockState == ((Block)entry.value()).getDefaultState()) {
                        jsonObject4.addProperty("default", true);
                    }
                    jsonArray2.add(jsonObject4);
                }
                jsonObject2.add("states", jsonArray2);
                String string = entry.getIdAsString();
                JsonElement jsonElement = (JsonElement)BlockTypes.CODEC.codec().encodeStart(lv, (Block)entry.value()).getOrThrow(string2 -> new AssertionError((Object)("Failed to serialize block " + string + " (is type registered in BlockTypes?): " + string2)));
                jsonObject2.add("definition", jsonElement);
                jsonObject.add(string, jsonObject2);
            });
            return DataProvider.writeToPath(writer, jsonObject, path);
        });
    }

    @Override
    public final String getName() {
        return "Block List";
    }
}

