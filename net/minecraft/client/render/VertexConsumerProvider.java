/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMaps;
import java.util.HashMap;
import java.util.Map;
import java.util.SequencedMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.BufferAllocator;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface VertexConsumerProvider {
    public static Immediate immediate(BufferAllocator buffer) {
        return VertexConsumerProvider.immediate(Object2ObjectSortedMaps.emptyMap(), buffer);
    }

    public static Immediate immediate(SequencedMap<RenderLayer, BufferAllocator> layerBuffers, BufferAllocator fallbackBuffer) {
        return new Immediate(fallbackBuffer, layerBuffers);
    }

    public VertexConsumer getBuffer(RenderLayer var1);

    @Environment(value=EnvType.CLIENT)
    public static class Immediate
    implements VertexConsumerProvider {
        protected final BufferAllocator allocator;
        protected final SequencedMap<RenderLayer, BufferAllocator> layerBuffers;
        protected final Map<RenderLayer, BufferBuilder> pending = new HashMap<RenderLayer, BufferBuilder>();
        @Nullable
        protected RenderLayer currentLayer;

        protected Immediate(BufferAllocator allocator, SequencedMap<RenderLayer, BufferAllocator> sequencedMap) {
            this.allocator = allocator;
            this.layerBuffers = sequencedMap;
        }

        @Override
        public VertexConsumer getBuffer(RenderLayer arg) {
            BufferBuilder lv = this.pending.get(arg);
            if (lv != null && !arg.areVerticesNotShared()) {
                this.draw(arg, lv);
                lv = null;
            }
            if (lv != null) {
                return lv;
            }
            BufferAllocator lv2 = (BufferAllocator)this.layerBuffers.get(arg);
            if (lv2 != null) {
                lv = new BufferBuilder(lv2, arg.getDrawMode(), arg.getVertexFormat());
            } else {
                if (this.currentLayer != null) {
                    this.draw(this.currentLayer);
                }
                lv = new BufferBuilder(this.allocator, arg.getDrawMode(), arg.getVertexFormat());
                this.currentLayer = arg;
            }
            this.pending.put(arg, lv);
            return lv;
        }

        public void drawCurrentLayer() {
            if (this.currentLayer != null) {
                this.draw(this.currentLayer);
                this.currentLayer = null;
            }
        }

        public void draw() {
            this.drawCurrentLayer();
            for (RenderLayer lv : this.layerBuffers.keySet()) {
                this.draw(lv);
            }
        }

        public void draw(RenderLayer layer) {
            BufferBuilder lv = this.pending.remove(layer);
            if (lv != null) {
                this.draw(layer, lv);
            }
        }

        private void draw(RenderLayer layer, BufferBuilder builder) {
            BuiltBuffer lv = builder.endNullable();
            if (lv != null) {
                if (layer.isTranslucent()) {
                    BufferAllocator lv2 = this.layerBuffers.getOrDefault(layer, this.allocator);
                    lv.sortQuads(lv2, RenderSystem.getVertexSorting());
                }
                layer.draw(lv);
            }
            if (layer.equals(this.currentLayer)) {
                this.currentLayer = null;
            }
        }
    }
}

