/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.MultipartBakedModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.render.model.json.MultipartModelComponent;
import net.minecraft.client.render.model.json.WeightedUnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class MultipartUnbakedModel
implements UnbakedModel {
    private final StateManager<Block, BlockState> stateFactory;
    private final List<MultipartModelComponent> components;

    public MultipartUnbakedModel(StateManager<Block, BlockState> stateFactory, List<MultipartModelComponent> components) {
        this.stateFactory = stateFactory;
        this.components = components;
    }

    public List<MultipartModelComponent> getComponents() {
        return this.components;
    }

    public Set<WeightedUnbakedModel> getModels() {
        HashSet<WeightedUnbakedModel> set = Sets.newHashSet();
        for (MultipartModelComponent lv : this.components) {
            set.add(lv.getModel());
        }
        return set;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof MultipartUnbakedModel) {
            MultipartUnbakedModel lv = (MultipartUnbakedModel)o;
            return Objects.equals(this.stateFactory, lv.stateFactory) && Objects.equals(this.components, lv.components);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.stateFactory, this.components);
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return this.getComponents().stream().flatMap(component -> component.getModel().getModelDependencies().stream()).collect(Collectors.toSet());
    }

    @Override
    public void setParents(Function<Identifier, UnbakedModel> modelLoader) {
        this.getComponents().forEach(component -> component.getModel().setParents(modelLoader));
    }

    @Override
    @Nullable
    public BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer) {
        MultipartBakedModel.Builder lv = new MultipartBakedModel.Builder();
        for (MultipartModelComponent lv2 : this.getComponents()) {
            BakedModel lv3 = lv2.getModel().bake(baker, textureGetter, rotationContainer);
            if (lv3 == null) continue;
            lv.addComponent(lv2.getPredicate(this.stateFactory), lv3);
        }
        return lv.build();
    }

    @Environment(value=EnvType.CLIENT)
    public static class Deserializer
    implements JsonDeserializer<MultipartUnbakedModel> {
        private final ModelVariantMap.DeserializationContext context;

        public Deserializer(ModelVariantMap.DeserializationContext context) {
            this.context = context;
        }

        @Override
        public MultipartUnbakedModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return new MultipartUnbakedModel(this.context.getStateFactory(), this.deserializeComponents(jsonDeserializationContext, jsonElement.getAsJsonArray()));
        }

        private List<MultipartModelComponent> deserializeComponents(JsonDeserializationContext context, JsonArray array) {
            ArrayList<MultipartModelComponent> list = Lists.newArrayList();
            for (JsonElement jsonElement : array) {
                list.add((MultipartModelComponent)context.deserialize(jsonElement, (Type)((Object)MultipartModelComponent.class)));
            }
            return list;
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            return this.deserialize(json, type, context);
        }
    }
}

