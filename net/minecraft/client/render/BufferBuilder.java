/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render;

import java.nio.ByteOrder;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

@Environment(value=EnvType.CLIENT)
public class BufferBuilder
implements VertexConsumer {
    private static final long field_52068 = -1L;
    private static final long field_52069 = -1L;
    private static final boolean LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
    private final BufferAllocator allocator;
    private long vertexPointer = -1L;
    private int vertexCount;
    private final VertexFormat format;
    private final VertexFormat.DrawMode drawMode;
    private final boolean canSkipElementChecks;
    private final boolean hasOverlay;
    private final int vertexSizeByte;
    private final int requiredMask;
    private final int[] offsetsByElementId;
    private int currentMask;
    private boolean building = true;

    public BufferBuilder(BufferAllocator allocator, VertexFormat.DrawMode drawMode, VertexFormat format) {
        if (!format.has(VertexFormatElement.POSITION)) {
            throw new IllegalArgumentException("Cannot build mesh with no position element");
        }
        this.allocator = allocator;
        this.drawMode = drawMode;
        this.format = format;
        this.vertexSizeByte = format.getVertexSizeByte();
        this.requiredMask = format.getRequiredMask() & ~VertexFormatElement.POSITION.getBit();
        this.offsetsByElementId = format.getOffsetsByElementId();
        boolean bl = format == VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL;
        boolean bl2 = format == VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL;
        this.canSkipElementChecks = bl || bl2;
        this.hasOverlay = bl;
    }

    @Nullable
    public BuiltBuffer endNullable() {
        this.ensureBuilding();
        this.endVertex();
        BuiltBuffer lv = this.build();
        this.building = false;
        this.vertexPointer = -1L;
        return lv;
    }

    public BuiltBuffer end() {
        BuiltBuffer lv = this.endNullable();
        if (lv == null) {
            throw new IllegalStateException("BufferBuilder was empty");
        }
        return lv;
    }

    private void ensureBuilding() {
        if (!this.building) {
            throw new IllegalStateException("Not building!");
        }
    }

    @Nullable
    private BuiltBuffer build() {
        if (this.vertexCount == 0) {
            return null;
        }
        BufferAllocator.CloseableBuffer lv = this.allocator.getAllocated();
        if (lv == null) {
            return null;
        }
        int i = this.drawMode.getIndexCount(this.vertexCount);
        VertexFormat.IndexType lv2 = VertexFormat.IndexType.smallestFor(this.vertexCount);
        return new BuiltBuffer(lv, new BuiltBuffer.DrawParameters(this.format, this.vertexCount, i, this.drawMode, lv2));
    }

    private long beginVertex() {
        long l;
        this.ensureBuilding();
        this.endVertex();
        ++this.vertexCount;
        this.vertexPointer = l = this.allocator.allocate(this.vertexSizeByte);
        return l;
    }

    private long beginElement(VertexFormatElement element) {
        int i = this.currentMask;
        int j = i & ~element.getBit();
        if (j == i) {
            return -1L;
        }
        this.currentMask = j;
        long l = this.vertexPointer;
        if (l == -1L) {
            throw new IllegalArgumentException("Not currently building vertex");
        }
        return l + (long)this.offsetsByElementId[element.id()];
    }

    private void endVertex() {
        if (this.vertexCount == 0) {
            return;
        }
        if (this.currentMask != 0) {
            String string = VertexFormatElement.streamFromMask(this.currentMask).map(this.format::getName).collect(Collectors.joining(", "));
            throw new IllegalStateException("Missing elements in vertex: " + string);
        }
        if (this.drawMode == VertexFormat.DrawMode.LINES || this.drawMode == VertexFormat.DrawMode.LINE_STRIP) {
            long l = this.allocator.allocate(this.vertexSizeByte);
            MemoryUtil.memCopy(l - (long)this.vertexSizeByte, l, this.vertexSizeByte);
            ++this.vertexCount;
        }
    }

    private static void putColor(long pointer, int argb) {
        int j = ColorHelper.Abgr.toAbgr(argb);
        MemoryUtil.memPutInt(pointer, LITTLE_ENDIAN ? j : Integer.reverseBytes(j));
    }

    private static void putInt(long pointer, int i) {
        if (LITTLE_ENDIAN) {
            MemoryUtil.memPutInt(pointer, i);
        } else {
            MemoryUtil.memPutShort(pointer, (short)(i & 0xFFFF));
            MemoryUtil.memPutShort(pointer + 2L, (short)(i >> 16 & 0xFFFF));
        }
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        long l = this.beginVertex() + (long)this.offsetsByElementId[VertexFormatElement.POSITION.id()];
        this.currentMask = this.requiredMask;
        MemoryUtil.memPutFloat(l, x);
        MemoryUtil.memPutFloat(l + 4L, y);
        MemoryUtil.memPutFloat(l + 8L, z);
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        long m = this.beginElement(VertexFormatElement.COLOR);
        if (m != -1L) {
            MemoryUtil.memPutByte(m, (byte)red);
            MemoryUtil.memPutByte(m + 1L, (byte)green);
            MemoryUtil.memPutByte(m + 2L, (byte)blue);
            MemoryUtil.memPutByte(m + 3L, (byte)alpha);
        }
        return this;
    }

    @Override
    public VertexConsumer color(int argb) {
        long l = this.beginElement(VertexFormatElement.COLOR);
        if (l != -1L) {
            BufferBuilder.putColor(l, argb);
        }
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        long l = this.beginElement(VertexFormatElement.UV_0);
        if (l != -1L) {
            MemoryUtil.memPutFloat(l, u);
            MemoryUtil.memPutFloat(l + 4L, v);
        }
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        return this.putUv((short)u, (short)v, VertexFormatElement.UV_1);
    }

    @Override
    public VertexConsumer overlay(int uv) {
        long l = this.beginElement(VertexFormatElement.UV_1);
        if (l != -1L) {
            BufferBuilder.putInt(l, uv);
        }
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        return this.putUv((short)u, (short)v, VertexFormatElement.UV_2);
    }

    @Override
    public VertexConsumer light(int uv) {
        long l = this.beginElement(VertexFormatElement.UV_2);
        if (l != -1L) {
            BufferBuilder.putInt(l, uv);
        }
        return this;
    }

    private VertexConsumer putUv(short u, short v, VertexFormatElement element) {
        long l = this.beginElement(element);
        if (l != -1L) {
            MemoryUtil.memPutShort(l, u);
            MemoryUtil.memPutShort(l + 2L, v);
        }
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        long l = this.beginElement(VertexFormatElement.NORMAL);
        if (l != -1L) {
            MemoryUtil.memPutByte(l, BufferBuilder.floatToByte(x));
            MemoryUtil.memPutByte(l + 1L, BufferBuilder.floatToByte(y));
            MemoryUtil.memPutByte(l + 2L, BufferBuilder.floatToByte(z));
        }
        return this;
    }

    private static byte floatToByte(float f) {
        return (byte)((int)(MathHelper.clamp(f, -1.0f, 1.0f) * 127.0f) & 0xFF);
    }

    @Override
    public void vertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
        if (this.canSkipElementChecks) {
            long r;
            long q = this.beginVertex();
            MemoryUtil.memPutFloat(q + 0L, x);
            MemoryUtil.memPutFloat(q + 4L, y);
            MemoryUtil.memPutFloat(q + 8L, z);
            BufferBuilder.putColor(q + 12L, color);
            MemoryUtil.memPutFloat(q + 16L, u);
            MemoryUtil.memPutFloat(q + 20L, v);
            if (this.hasOverlay) {
                BufferBuilder.putInt(q + 24L, overlay);
                r = q + 28L;
            } else {
                r = q + 24L;
            }
            BufferBuilder.putInt(r + 0L, light);
            MemoryUtil.memPutByte(r + 4L, BufferBuilder.floatToByte(normalX));
            MemoryUtil.memPutByte(r + 5L, BufferBuilder.floatToByte(normalY));
            MemoryUtil.memPutByte(r + 6L, BufferBuilder.floatToByte(normalZ));
            return;
        }
        VertexConsumer.super.vertex(x, y, z, color, u, v, overlay, light, normalX, normalY, normalZ);
    }
}

