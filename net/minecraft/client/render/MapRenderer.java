/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.MapDecorationsAtlasManager;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class MapRenderer
implements AutoCloseable {
    private static final int DEFAULT_IMAGE_WIDTH = 128;
    private static final int DEFAULT_IMAGE_HEIGHT = 128;
    final TextureManager textureManager;
    final MapDecorationsAtlasManager mapDecorationsAtlasManager;
    private final Int2ObjectMap<MapTexture> mapTextures = new Int2ObjectOpenHashMap<MapTexture>();

    public MapRenderer(TextureManager textureManager, MapDecorationsAtlasManager mapDecorationsAtlasManager) {
        this.textureManager = textureManager;
        this.mapDecorationsAtlasManager = mapDecorationsAtlasManager;
    }

    public void updateTexture(MapIdComponent id, MapState state) {
        this.getMapTexture(id, state).setNeedsUpdate();
    }

    public void draw(MatrixStack matrices, VertexConsumerProvider vertexConsumers, MapIdComponent id, MapState state, boolean hidePlayerIcons, int light) {
        this.getMapTexture(id, state).draw(matrices, vertexConsumers, hidePlayerIcons, light);
    }

    private MapTexture getMapTexture(MapIdComponent id, MapState state) {
        return this.mapTextures.compute(id.id(), (id2, texture) -> {
            if (texture == null) {
                return new MapTexture((int)id2, state);
            }
            texture.setState(state);
            return texture;
        });
    }

    public void clearStateTextures() {
        for (MapTexture lv : this.mapTextures.values()) {
            lv.close();
        }
        this.mapTextures.clear();
    }

    @Override
    public void close() {
        this.clearStateTextures();
    }

    @Environment(value=EnvType.CLIENT)
    class MapTexture
    implements AutoCloseable {
        private MapState state;
        private final NativeImageBackedTexture texture;
        private final RenderLayer renderLayer;
        private boolean needsUpdate = true;

        MapTexture(int id, MapState state) {
            this.state = state;
            this.texture = new NativeImageBackedTexture(128, 128, true);
            Identifier lv = MapRenderer.this.textureManager.registerDynamicTexture("map/" + id, this.texture);
            this.renderLayer = RenderLayer.getText(lv);
        }

        void setState(MapState state) {
            boolean bl = this.state != state;
            this.state = state;
            this.needsUpdate |= bl;
        }

        public void setNeedsUpdate() {
            this.needsUpdate = true;
        }

        private void updateTexture() {
            for (int i = 0; i < 128; ++i) {
                for (int j = 0; j < 128; ++j) {
                    int k = j + i * 128;
                    this.texture.getImage().setColor(j, i, MapColor.getRenderColor(this.state.colors[k]));
                }
            }
            this.texture.upload();
        }

        void draw(MatrixStack matrices, VertexConsumerProvider vertexConsumers, boolean hidePlayerIcons, int light) {
            if (this.needsUpdate) {
                this.updateTexture();
                this.needsUpdate = false;
            }
            boolean j = false;
            boolean k = false;
            float f = 0.0f;
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            VertexConsumer lv = vertexConsumers.getBuffer(this.renderLayer);
            lv.vertex(matrix4f, 0.0f, 128.0f, -0.01f).color(Colors.WHITE).texture(0.0f, 1.0f).light(light);
            lv.vertex(matrix4f, 128.0f, 128.0f, -0.01f).color(Colors.WHITE).texture(1.0f, 1.0f).light(light);
            lv.vertex(matrix4f, 128.0f, 0.0f, -0.01f).color(Colors.WHITE).texture(1.0f, 0.0f).light(light);
            lv.vertex(matrix4f, 0.0f, 0.0f, -0.01f).color(Colors.WHITE).texture(0.0f, 0.0f).light(light);
            int l = 0;
            for (MapDecoration lv2 : this.state.getDecorations()) {
                if (hidePlayerIcons && !lv2.isAlwaysRendered()) continue;
                matrices.push();
                matrices.translate(0.0f + (float)lv2.x() / 2.0f + 64.0f, 0.0f + (float)lv2.z() / 2.0f + 64.0f, -0.02f);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(lv2.rotation() * 360) / 16.0f));
                matrices.scale(4.0f, 4.0f, 3.0f);
                matrices.translate(-0.125f, 0.125f, 0.0f);
                Matrix4f matrix4f2 = matrices.peek().getPositionMatrix();
                float g = -0.001f;
                Sprite lv3 = MapRenderer.this.mapDecorationsAtlasManager.getSprite(lv2);
                float h = lv3.getMinU();
                float m = lv3.getMinV();
                float n = lv3.getMaxU();
                float o = lv3.getMaxV();
                VertexConsumer lv4 = vertexConsumers.getBuffer(RenderLayer.getText(lv3.getAtlasId()));
                lv4.vertex(matrix4f2, -1.0f, 1.0f, (float)l * -0.001f).color(Colors.WHITE).texture(h, m).light(light);
                lv4.vertex(matrix4f2, 1.0f, 1.0f, (float)l * -0.001f).color(Colors.WHITE).texture(n, m).light(light);
                lv4.vertex(matrix4f2, 1.0f, -1.0f, (float)l * -0.001f).color(Colors.WHITE).texture(n, o).light(light);
                lv4.vertex(matrix4f2, -1.0f, -1.0f, (float)l * -0.001f).color(Colors.WHITE).texture(h, o).light(light);
                matrices.pop();
                if (lv2.name().isPresent()) {
                    TextRenderer lv5 = MinecraftClient.getInstance().textRenderer;
                    Text lv6 = lv2.name().get();
                    float p = lv5.getWidth(lv6);
                    float f2 = 25.0f / p;
                    Objects.requireNonNull(lv5);
                    float q = MathHelper.clamp(f2, 0.0f, 6.0f / 9.0f);
                    matrices.push();
                    matrices.translate(0.0f + (float)lv2.x() / 2.0f + 64.0f - p * q / 2.0f, 0.0f + (float)lv2.z() / 2.0f + 64.0f + 4.0f, -0.025f);
                    matrices.scale(q, q, 1.0f);
                    matrices.translate(0.0f, 0.0f, -0.1f);
                    lv5.draw(lv6, 0.0f, 0.0f, Colors.WHITE, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, Integer.MIN_VALUE, light);
                    matrices.pop();
                }
                ++l;
            }
        }

        @Override
        public void close() {
            this.texture.close();
        }
    }
}

