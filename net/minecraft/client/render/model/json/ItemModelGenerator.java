/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model.json;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.render.model.json.ModelElementTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.math.Direction;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class ItemModelGenerator {
    public static final List<String> LAYERS = Lists.newArrayList("layer0", "layer1", "layer2", "layer3", "layer4");
    private static final float field_32806 = 7.5f;
    private static final float field_32807 = 8.5f;

    public JsonUnbakedModel create(Function<SpriteIdentifier, Sprite> textureGetter, JsonUnbakedModel blockModel) {
        String string;
        HashMap<String, Either<SpriteIdentifier, String>> map = Maps.newHashMap();
        ArrayList<ModelElement> list = Lists.newArrayList();
        for (int i = 0; i < LAYERS.size() && blockModel.textureExists(string = LAYERS.get(i)); ++i) {
            SpriteIdentifier lv = blockModel.resolveSprite(string);
            map.put(string, Either.left(lv));
            SpriteContents lv2 = textureGetter.apply(lv).getContents();
            list.addAll(this.addLayerElements(i, string, lv2));
        }
        map.put("particle", blockModel.textureExists("particle") ? Either.left(blockModel.resolveSprite("particle")) : (Either)map.get("layer0"));
        JsonUnbakedModel lv3 = new JsonUnbakedModel(null, list, map, false, blockModel.getGuiLight(), blockModel.getTransformations(), blockModel.getOverrides());
        lv3.id = blockModel.id;
        return lv3;
    }

    private List<ModelElement> addLayerElements(int layer, String key, SpriteContents sprite) {
        HashMap<Direction, ModelElementFace> map = Maps.newHashMap();
        map.put(Direction.SOUTH, new ModelElementFace(null, layer, key, new ModelElementTexture(new float[]{0.0f, 0.0f, 16.0f, 16.0f}, 0)));
        map.put(Direction.NORTH, new ModelElementFace(null, layer, key, new ModelElementTexture(new float[]{16.0f, 0.0f, 0.0f, 16.0f}, 0)));
        ArrayList<ModelElement> list = Lists.newArrayList();
        list.add(new ModelElement(new Vector3f(0.0f, 0.0f, 7.5f), new Vector3f(16.0f, 16.0f, 8.5f), map, null, true));
        list.addAll(this.addSubComponents(sprite, key, layer));
        return list;
    }

    private List<ModelElement> addSubComponents(SpriteContents sprite, String key, int layer) {
        float f = sprite.getWidth();
        float g = sprite.getHeight();
        ArrayList<ModelElement> list = Lists.newArrayList();
        for (Frame lv : this.getFrames(sprite)) {
            float h = 0.0f;
            float j = 0.0f;
            float k = 0.0f;
            float l = 0.0f;
            float m = 0.0f;
            float n = 0.0f;
            float o = 0.0f;
            float p = 0.0f;
            float q = 16.0f / f;
            float r = 16.0f / g;
            float s = lv.getMin();
            float t = lv.getMax();
            float u = lv.getLevel();
            Side lv2 = lv.getSide();
            switch (lv2.ordinal()) {
                case 0: {
                    h = m = s;
                    k = n = t + 1.0f;
                    j = o = u;
                    l = u;
                    p = u + 1.0f;
                    break;
                }
                case 1: {
                    o = u;
                    p = u + 1.0f;
                    h = m = s;
                    k = n = t + 1.0f;
                    j = u + 1.0f;
                    l = u + 1.0f;
                    break;
                }
                case 2: {
                    h = m = u;
                    k = u;
                    n = u + 1.0f;
                    j = p = s;
                    l = o = t + 1.0f;
                    break;
                }
                case 3: {
                    m = u;
                    n = u + 1.0f;
                    h = u + 1.0f;
                    k = u + 1.0f;
                    j = p = s;
                    l = o = t + 1.0f;
                }
            }
            h *= q;
            k *= q;
            j *= r;
            l *= r;
            j = 16.0f - j;
            l = 16.0f - l;
            HashMap<Direction, ModelElementFace> map = Maps.newHashMap();
            map.put(lv2.getDirection(), new ModelElementFace(null, layer, key, new ModelElementTexture(new float[]{m *= q, o *= r, n *= q, p *= r}, 0)));
            switch (lv2.ordinal()) {
                case 0: {
                    list.add(new ModelElement(new Vector3f(h, j, 7.5f), new Vector3f(k, j, 8.5f), map, null, true));
                    break;
                }
                case 1: {
                    list.add(new ModelElement(new Vector3f(h, l, 7.5f), new Vector3f(k, l, 8.5f), map, null, true));
                    break;
                }
                case 2: {
                    list.add(new ModelElement(new Vector3f(h, j, 7.5f), new Vector3f(h, l, 8.5f), map, null, true));
                    break;
                }
                case 3: {
                    list.add(new ModelElement(new Vector3f(k, j, 7.5f), new Vector3f(k, l, 8.5f), map, null, true));
                }
            }
        }
        return list;
    }

    private List<Frame> getFrames(SpriteContents sprite) {
        int i = sprite.getWidth();
        int j = sprite.getHeight();
        ArrayList<Frame> list = Lists.newArrayList();
        sprite.getDistinctFrameCount().forEach(frame -> {
            for (int l = 0; l < j; ++l) {
                for (int m = 0; m < i; ++m) {
                    boolean bl = !this.isPixelTransparent(sprite, frame, m, l, i, j);
                    this.buildCube(Side.UP, list, sprite, frame, m, l, i, j, bl);
                    this.buildCube(Side.DOWN, list, sprite, frame, m, l, i, j, bl);
                    this.buildCube(Side.LEFT, list, sprite, frame, m, l, i, j, bl);
                    this.buildCube(Side.RIGHT, list, sprite, frame, m, l, i, j, bl);
                }
            }
        });
        return list;
    }

    private void buildCube(Side side, List<Frame> cubes, SpriteContents sprite, int frame, int x, int y, int width, int height, boolean bl) {
        boolean bl2;
        boolean bl3 = bl2 = this.isPixelTransparent(sprite, frame, x + side.getOffsetX(), y + side.getOffsetY(), width, height) && bl;
        if (bl2) {
            this.buildCube(cubes, side, x, y);
        }
    }

    private void buildCube(List<Frame> cubes, Side side, int x, int y) {
        int m;
        Frame lv = null;
        for (Frame lv2 : cubes) {
            int k;
            if (lv2.getSide() != side) continue;
            int n = k = side.isVertical() ? y : x;
            if (lv2.getLevel() != k) continue;
            lv = lv2;
            break;
        }
        int l = side.isVertical() ? y : x;
        int n = m = side.isVertical() ? x : y;
        if (lv == null) {
            cubes.add(new Frame(side, m, l));
        } else {
            lv.expand(m);
        }
    }

    private boolean isPixelTransparent(SpriteContents sprite, int frame, int x, int y, int width, int height) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return true;
        }
        return sprite.isPixelTransparent(frame, x, y);
    }

    @Environment(value=EnvType.CLIENT)
    static class Frame {
        private final Side side;
        private int min;
        private int max;
        private final int level;

        public Frame(Side side, int width, int depth) {
            this.side = side;
            this.min = width;
            this.max = width;
            this.level = depth;
        }

        public void expand(int newValue) {
            if (newValue < this.min) {
                this.min = newValue;
            } else if (newValue > this.max) {
                this.max = newValue;
            }
        }

        public Side getSide() {
            return this.side;
        }

        public int getMin() {
            return this.min;
        }

        public int getMax() {
            return this.max;
        }

        public int getLevel() {
            return this.level;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum Side {
        UP(Direction.UP, 0, -1),
        DOWN(Direction.DOWN, 0, 1),
        LEFT(Direction.EAST, -1, 0),
        RIGHT(Direction.WEST, 1, 0);

        private final Direction direction;
        private final int offsetX;
        private final int offsetY;

        private Side(Direction direction, int offsetX, int offsetY) {
            this.direction = direction;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        public Direction getDirection() {
            return this.direction;
        }

        public int getOffsetX() {
            return this.offsetX;
        }

        public int getOffsetY() {
            return this.offsetY;
        }

        boolean isVertical() {
            return this == DOWN || this == UP;
        }
    }
}

