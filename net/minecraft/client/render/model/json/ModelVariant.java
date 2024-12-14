/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model.json;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.AffineTransformation;

@Environment(value=EnvType.CLIENT)
public class ModelVariant
implements ModelBakeSettings {
    private final Identifier location;
    private final AffineTransformation rotation;
    private final boolean uvLock;
    private final int weight;

    public ModelVariant(Identifier location, AffineTransformation rotation, boolean uvLock, int weight) {
        this.location = location;
        this.rotation = rotation;
        this.uvLock = uvLock;
        this.weight = weight;
    }

    public Identifier getLocation() {
        return this.location;
    }

    @Override
    public AffineTransformation getRotation() {
        return this.rotation;
    }

    @Override
    public boolean isUvLocked() {
        return this.uvLock;
    }

    public int getWeight() {
        return this.weight;
    }

    public String toString() {
        return "Variant{modelLocation=" + String.valueOf(this.location) + ", rotation=" + String.valueOf(this.rotation) + ", uvLock=" + this.uvLock + ", weight=" + this.weight + "}";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ModelVariant) {
            ModelVariant lv = (ModelVariant)o;
            return this.location.equals(lv.location) && Objects.equals(this.rotation, lv.rotation) && this.uvLock == lv.uvLock && this.weight == lv.weight;
        }
        return false;
    }

    public int hashCode() {
        int i = this.location.hashCode();
        i = 31 * i + this.rotation.hashCode();
        i = 31 * i + Boolean.valueOf(this.uvLock).hashCode();
        i = 31 * i + this.weight;
        return i;
    }

    @Environment(value=EnvType.CLIENT)
    public static class Deserializer
    implements JsonDeserializer<ModelVariant> {
        @VisibleForTesting
        static final boolean DEFAULT_UV_LOCK = false;
        @VisibleForTesting
        static final int DEFAULT_WEIGHT = 1;
        @VisibleForTesting
        static final int DEFAULT_X_ROTATION = 0;
        @VisibleForTesting
        static final int DEFAULT_Y_ROTATION = 0;

        @Override
        public ModelVariant deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Identifier lv = this.deserializeModel(jsonObject);
            ModelRotation lv2 = this.deserializeRotation(jsonObject);
            boolean bl = this.deserializeUvLock(jsonObject);
            int i = this.deserializeWeight(jsonObject);
            return new ModelVariant(lv, lv2.getRotation(), bl, i);
        }

        private boolean deserializeUvLock(JsonObject object) {
            return JsonHelper.getBoolean(object, "uvlock", false);
        }

        protected ModelRotation deserializeRotation(JsonObject object) {
            int j;
            int i = JsonHelper.getInt(object, "x", 0);
            ModelRotation lv = ModelRotation.get(i, j = JsonHelper.getInt(object, "y", 0));
            if (lv == null) {
                throw new JsonParseException("Invalid BlockModelRotation x: " + i + ", y: " + j);
            }
            return lv;
        }

        protected Identifier deserializeModel(JsonObject object) {
            return Identifier.of(JsonHelper.getString(object, "model"));
        }

        protected int deserializeWeight(JsonObject object) {
            int i = JsonHelper.getInt(object, "weight", 1);
            if (i < 1) {
                throw new JsonParseException("Invalid weight " + i + " found, expected integer >= 1");
            }
            return i;
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement functionJson, Type unused, JsonDeserializationContext context) throws JsonParseException {
            return this.deserialize(functionJson, unused, context);
        }
    }
}

