/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render.block;

import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.BitSet;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BlockModelRenderer {
    private static final int field_32782 = 0;
    private static final int field_32783 = 1;
    static final Direction[] DIRECTIONS = Direction.values();
    private final BlockColors colors;
    private static final int BRIGHTNESS_CACHE_MAX_SIZE = 100;
    static final ThreadLocal<BrightnessCache> BRIGHTNESS_CACHE = ThreadLocal.withInitial(BrightnessCache::new);

    public BlockModelRenderer(BlockColors colors) {
        this.colors = colors;
    }

    public void render(BlockRenderView world, BakedModel model, BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, Random random, long seed, int overlay) {
        boolean bl2 = MinecraftClient.isAmbientOcclusionEnabled() && state.getLuminance() == 0 && model.useAmbientOcclusion();
        Vec3d lv = state.getModelOffset(world, pos);
        matrices.translate(lv.x, lv.y, lv.z);
        try {
            if (bl2) {
                this.renderSmooth(world, model, state, pos, matrices, vertexConsumer, cull, random, seed, overlay);
            } else {
                this.renderFlat(world, model, state, pos, matrices, vertexConsumer, cull, random, seed, overlay);
            }
        } catch (Throwable throwable) {
            CrashReport lv2 = CrashReport.create(throwable, "Tesselating block model");
            CrashReportSection lv3 = lv2.addElement("Block model being tesselated");
            CrashReportSection.addBlockInfo(lv3, world, pos, state);
            lv3.add("Using AO", bl2);
            throw new CrashException(lv2);
        }
    }

    public void renderSmooth(BlockRenderView world, BakedModel model, BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, Random random, long seed, int overlay) {
        float[] fs = new float[DIRECTIONS.length * 2];
        BitSet bitSet = new BitSet(3);
        AmbientOcclusionCalculator lv = new AmbientOcclusionCalculator();
        BlockPos.Mutable lv2 = pos.mutableCopy();
        for (Direction lv3 : DIRECTIONS) {
            random.setSeed(seed);
            List<BakedQuad> list = model.getQuads(state, lv3, random);
            if (list.isEmpty()) continue;
            lv2.set((Vec3i)pos, lv3);
            if (cull && !Block.shouldDrawSide(state, world, pos, lv3, lv2)) continue;
            this.renderQuadsSmooth(world, state, pos, matrices, vertexConsumer, list, fs, bitSet, lv, overlay);
        }
        random.setSeed(seed);
        List<BakedQuad> list2 = model.getQuads(state, null, random);
        if (!list2.isEmpty()) {
            this.renderQuadsSmooth(world, state, pos, matrices, vertexConsumer, list2, fs, bitSet, lv, overlay);
        }
    }

    public void renderFlat(BlockRenderView world, BakedModel model, BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, Random random, long seed, int overlay) {
        BitSet bitSet = new BitSet(3);
        BlockPos.Mutable lv = pos.mutableCopy();
        for (Direction lv2 : DIRECTIONS) {
            random.setSeed(seed);
            List<BakedQuad> list = model.getQuads(state, lv2, random);
            if (list.isEmpty()) continue;
            lv.set((Vec3i)pos, lv2);
            if (cull && !Block.shouldDrawSide(state, world, pos, lv2, lv)) continue;
            int j = WorldRenderer.getLightmapCoordinates(world, state, lv);
            this.renderQuadsFlat(world, state, pos, j, overlay, false, matrices, vertexConsumer, list, bitSet);
        }
        random.setSeed(seed);
        List<BakedQuad> list2 = model.getQuads(state, null, random);
        if (!list2.isEmpty()) {
            this.renderQuadsFlat(world, state, pos, -1, overlay, true, matrices, vertexConsumer, list2, bitSet);
        }
    }

    private void renderQuadsSmooth(BlockRenderView world, BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumer vertexConsumer, List<BakedQuad> quads, float[] box, BitSet flags, AmbientOcclusionCalculator ambientOcclusionCalculator, int overlay) {
        for (BakedQuad lv : quads) {
            this.getQuadDimensions(world, state, pos, lv.getVertexData(), lv.getFace(), box, flags);
            ambientOcclusionCalculator.apply(world, state, pos, lv.getFace(), box, flags, lv.hasShade());
            this.renderQuad(world, state, pos, vertexConsumer, matrices.peek(), lv, ambientOcclusionCalculator.brightness[0], ambientOcclusionCalculator.brightness[1], ambientOcclusionCalculator.brightness[2], ambientOcclusionCalculator.brightness[3], ambientOcclusionCalculator.light[0], ambientOcclusionCalculator.light[1], ambientOcclusionCalculator.light[2], ambientOcclusionCalculator.light[3], overlay);
        }
    }

    private void renderQuad(BlockRenderView world, BlockState state, BlockPos pos, VertexConsumer vertexConsumer, MatrixStack.Entry matrixEntry, BakedQuad quad, float brightness0, float brightness1, float brightness2, float brightness3, int light0, int light1, int light2, int light3, int overlay) {
        float r;
        float q;
        float p;
        if (quad.hasColor()) {
            int o = this.colors.getColor(state, world, pos, quad.getColorIndex());
            p = (float)(o >> 16 & 0xFF) / 255.0f;
            q = (float)(o >> 8 & 0xFF) / 255.0f;
            r = (float)(o & 0xFF) / 255.0f;
        } else {
            p = 1.0f;
            q = 1.0f;
            r = 1.0f;
        }
        vertexConsumer.quad(matrixEntry, quad, new float[]{brightness0, brightness1, brightness2, brightness3}, p, q, r, 1.0f, new int[]{light0, light1, light2, light3}, overlay, true);
    }

    private void getQuadDimensions(BlockRenderView world, BlockState state, BlockPos pos, int[] vertexData, Direction face, @Nullable float[] box, BitSet flags) {
        float m;
        int l;
        float f = 32.0f;
        float g = 32.0f;
        float h = 32.0f;
        float i = -32.0f;
        float j = -32.0f;
        float k = -32.0f;
        for (l = 0; l < 4; ++l) {
            m = Float.intBitsToFloat(vertexData[l * 8]);
            float n = Float.intBitsToFloat(vertexData[l * 8 + 1]);
            float o = Float.intBitsToFloat(vertexData[l * 8 + 2]);
            f = Math.min(f, m);
            g = Math.min(g, n);
            h = Math.min(h, o);
            i = Math.max(i, m);
            j = Math.max(j, n);
            k = Math.max(k, o);
        }
        if (box != null) {
            box[Direction.WEST.getId()] = f;
            box[Direction.EAST.getId()] = i;
            box[Direction.DOWN.getId()] = g;
            box[Direction.UP.getId()] = j;
            box[Direction.NORTH.getId()] = h;
            box[Direction.SOUTH.getId()] = k;
            l = DIRECTIONS.length;
            box[Direction.WEST.getId() + l] = 1.0f - f;
            box[Direction.EAST.getId() + l] = 1.0f - i;
            box[Direction.DOWN.getId() + l] = 1.0f - g;
            box[Direction.UP.getId() + l] = 1.0f - j;
            box[Direction.NORTH.getId() + l] = 1.0f - h;
            box[Direction.SOUTH.getId() + l] = 1.0f - k;
        }
        float p = 1.0E-4f;
        m = 0.9999f;
        switch (face) {
            case DOWN: {
                flags.set(1, f >= 1.0E-4f || h >= 1.0E-4f || i <= 0.9999f || k <= 0.9999f);
                flags.set(0, g == j && (g < 1.0E-4f || state.isFullCube(world, pos)));
                break;
            }
            case UP: {
                flags.set(1, f >= 1.0E-4f || h >= 1.0E-4f || i <= 0.9999f || k <= 0.9999f);
                flags.set(0, g == j && (j > 0.9999f || state.isFullCube(world, pos)));
                break;
            }
            case NORTH: {
                flags.set(1, f >= 1.0E-4f || g >= 1.0E-4f || i <= 0.9999f || j <= 0.9999f);
                flags.set(0, h == k && (h < 1.0E-4f || state.isFullCube(world, pos)));
                break;
            }
            case SOUTH: {
                flags.set(1, f >= 1.0E-4f || g >= 1.0E-4f || i <= 0.9999f || j <= 0.9999f);
                flags.set(0, h == k && (k > 0.9999f || state.isFullCube(world, pos)));
                break;
            }
            case WEST: {
                flags.set(1, g >= 1.0E-4f || h >= 1.0E-4f || j <= 0.9999f || k <= 0.9999f);
                flags.set(0, f == i && (f < 1.0E-4f || state.isFullCube(world, pos)));
                break;
            }
            case EAST: {
                flags.set(1, g >= 1.0E-4f || h >= 1.0E-4f || j <= 0.9999f || k <= 0.9999f);
                flags.set(0, f == i && (i > 0.9999f || state.isFullCube(world, pos)));
            }
        }
    }

    private void renderQuadsFlat(BlockRenderView world, BlockState state, BlockPos pos, int light, int overlay, boolean useWorldLight, MatrixStack matrices, VertexConsumer vertexConsumer, List<BakedQuad> quads, BitSet flags) {
        for (BakedQuad lv : quads) {
            if (useWorldLight) {
                this.getQuadDimensions(world, state, pos, lv.getVertexData(), lv.getFace(), null, flags);
                BlockPos lv2 = flags.get(0) ? pos.offset(lv.getFace()) : pos;
                light = WorldRenderer.getLightmapCoordinates(world, state, lv2);
            }
            float f = world.getBrightness(lv.getFace(), lv.hasShade());
            this.renderQuad(world, state, pos, vertexConsumer, matrices.peek(), lv, f, f, f, f, light, light, light, light, overlay);
        }
    }

    public void render(MatrixStack.Entry entry, VertexConsumer vertexConsumer, @Nullable BlockState state, BakedModel bakedModel, float red, float green, float blue, int light, int overlay) {
        Random lv = Random.create();
        long l = 42L;
        for (Direction lv2 : DIRECTIONS) {
            lv.setSeed(42L);
            BlockModelRenderer.renderQuads(entry, vertexConsumer, red, green, blue, bakedModel.getQuads(state, lv2, lv), light, overlay);
        }
        lv.setSeed(42L);
        BlockModelRenderer.renderQuads(entry, vertexConsumer, red, green, blue, bakedModel.getQuads(state, null, lv), light, overlay);
    }

    private static void renderQuads(MatrixStack.Entry entry, VertexConsumer vertexConsumer, float red, float green, float blue, List<BakedQuad> quads, int light, int overlay) {
        for (BakedQuad lv : quads) {
            float m;
            float l;
            float k;
            if (lv.hasColor()) {
                k = MathHelper.clamp(red, 0.0f, 1.0f);
                l = MathHelper.clamp(green, 0.0f, 1.0f);
                m = MathHelper.clamp(blue, 0.0f, 1.0f);
            } else {
                k = 1.0f;
                l = 1.0f;
                m = 1.0f;
            }
            vertexConsumer.quad(entry, lv, k, l, m, 1.0f, light, overlay);
        }
    }

    public static void enableBrightnessCache() {
        BRIGHTNESS_CACHE.get().enable();
    }

    public static void disableBrightnessCache() {
        BRIGHTNESS_CACHE.get().disable();
    }

    @Environment(value=EnvType.CLIENT)
    static class AmbientOcclusionCalculator {
        final float[] brightness = new float[4];
        final int[] light = new int[4];

        public void apply(BlockRenderView world, BlockState state, BlockPos pos, Direction direction, float[] box, BitSet flags, boolean shaded) {
            float x;
            int u;
            float t;
            int s;
            float r;
            int q;
            float p;
            int o;
            float n;
            BlockState lv13;
            boolean bl5;
            BlockPos lv = flags.get(0) ? pos.offset(direction) : pos;
            NeighborData lv2 = NeighborData.getData(direction);
            BlockPos.Mutable lv3 = new BlockPos.Mutable();
            BrightnessCache lv4 = BRIGHTNESS_CACHE.get();
            lv3.set((Vec3i)lv, lv2.faces[0]);
            BlockState lv5 = world.getBlockState(lv3);
            int i = lv4.getInt(lv5, world, lv3);
            float f = lv4.getFloat(lv5, world, lv3);
            lv3.set((Vec3i)lv, lv2.faces[1]);
            BlockState lv6 = world.getBlockState(lv3);
            int j = lv4.getInt(lv6, world, lv3);
            float g = lv4.getFloat(lv6, world, lv3);
            lv3.set((Vec3i)lv, lv2.faces[2]);
            BlockState lv7 = world.getBlockState(lv3);
            int k = lv4.getInt(lv7, world, lv3);
            float h = lv4.getFloat(lv7, world, lv3);
            lv3.set((Vec3i)lv, lv2.faces[3]);
            BlockState lv8 = world.getBlockState(lv3);
            int l = lv4.getInt(lv8, world, lv3);
            float m = lv4.getFloat(lv8, world, lv3);
            BlockState lv9 = world.getBlockState(lv3.set((Vec3i)lv, lv2.faces[0]).move(direction));
            boolean bl2 = !lv9.shouldBlockVision(world, lv3) || lv9.getOpacity(world, lv3) == 0;
            BlockState lv10 = world.getBlockState(lv3.set((Vec3i)lv, lv2.faces[1]).move(direction));
            boolean bl3 = !lv10.shouldBlockVision(world, lv3) || lv10.getOpacity(world, lv3) == 0;
            BlockState lv11 = world.getBlockState(lv3.set((Vec3i)lv, lv2.faces[2]).move(direction));
            boolean bl4 = !lv11.shouldBlockVision(world, lv3) || lv11.getOpacity(world, lv3) == 0;
            BlockState lv12 = world.getBlockState(lv3.set((Vec3i)lv, lv2.faces[3]).move(direction));
            boolean bl = bl5 = !lv12.shouldBlockVision(world, lv3) || lv12.getOpacity(world, lv3) == 0;
            if (bl4 || bl2) {
                lv3.set((Vec3i)lv, lv2.faces[0]).move(lv2.faces[2]);
                lv13 = world.getBlockState(lv3);
                n = lv4.getFloat(lv13, world, lv3);
                o = lv4.getInt(lv13, world, lv3);
            } else {
                n = f;
                o = i;
            }
            if (bl5 || bl2) {
                lv3.set((Vec3i)lv, lv2.faces[0]).move(lv2.faces[3]);
                lv13 = world.getBlockState(lv3);
                p = lv4.getFloat(lv13, world, lv3);
                q = lv4.getInt(lv13, world, lv3);
            } else {
                p = f;
                q = i;
            }
            if (bl4 || bl3) {
                lv3.set((Vec3i)lv, lv2.faces[1]).move(lv2.faces[2]);
                lv13 = world.getBlockState(lv3);
                r = lv4.getFloat(lv13, world, lv3);
                s = lv4.getInt(lv13, world, lv3);
            } else {
                r = f;
                s = i;
            }
            if (bl5 || bl3) {
                lv3.set((Vec3i)lv, lv2.faces[1]).move(lv2.faces[3]);
                lv13 = world.getBlockState(lv3);
                t = lv4.getFloat(lv13, world, lv3);
                u = lv4.getInt(lv13, world, lv3);
            } else {
                t = f;
                u = i;
            }
            int v = lv4.getInt(state, world, pos);
            lv3.set((Vec3i)pos, direction);
            BlockState lv14 = world.getBlockState(lv3);
            if (flags.get(0) || !lv14.isOpaqueFullCube(world, lv3)) {
                v = lv4.getInt(lv14, world, lv3);
            }
            float w = flags.get(0) ? lv4.getFloat(world.getBlockState(lv), world, lv) : lv4.getFloat(world.getBlockState(pos), world, pos);
            Translation lv15 = Translation.getTranslations(direction);
            if (!flags.get(1) || !lv2.nonCubicWeight) {
                x = (m + f + p + w) * 0.25f;
                y = (h + f + n + w) * 0.25f;
                z = (h + g + r + w) * 0.25f;
                aa = (m + g + t + w) * 0.25f;
                this.light[lv15.firstCorner] = this.getAmbientOcclusionBrightness(l, i, q, v);
                this.light[lv15.secondCorner] = this.getAmbientOcclusionBrightness(k, i, o, v);
                this.light[lv15.thirdCorner] = this.getAmbientOcclusionBrightness(k, j, s, v);
                this.light[lv15.fourthCorner] = this.getAmbientOcclusionBrightness(l, j, u, v);
                this.brightness[lv15.firstCorner] = x;
                this.brightness[lv15.secondCorner] = y;
                this.brightness[lv15.thirdCorner] = z;
                this.brightness[lv15.fourthCorner] = aa;
            } else {
                x = (m + f + p + w) * 0.25f;
                y = (h + f + n + w) * 0.25f;
                z = (h + g + r + w) * 0.25f;
                aa = (m + g + t + w) * 0.25f;
                float ab = box[lv2.field_4192[0].shape] * box[lv2.field_4192[1].shape];
                float ac = box[lv2.field_4192[2].shape] * box[lv2.field_4192[3].shape];
                float ad = box[lv2.field_4192[4].shape] * box[lv2.field_4192[5].shape];
                float ae = box[lv2.field_4192[6].shape] * box[lv2.field_4192[7].shape];
                float af = box[lv2.field_4185[0].shape] * box[lv2.field_4185[1].shape];
                float ag = box[lv2.field_4185[2].shape] * box[lv2.field_4185[3].shape];
                float ah = box[lv2.field_4185[4].shape] * box[lv2.field_4185[5].shape];
                float ai = box[lv2.field_4185[6].shape] * box[lv2.field_4185[7].shape];
                float aj = box[lv2.field_4180[0].shape] * box[lv2.field_4180[1].shape];
                float ak = box[lv2.field_4180[2].shape] * box[lv2.field_4180[3].shape];
                float al = box[lv2.field_4180[4].shape] * box[lv2.field_4180[5].shape];
                float am = box[lv2.field_4180[6].shape] * box[lv2.field_4180[7].shape];
                float an = box[lv2.field_4188[0].shape] * box[lv2.field_4188[1].shape];
                float ao = box[lv2.field_4188[2].shape] * box[lv2.field_4188[3].shape];
                float ap = box[lv2.field_4188[4].shape] * box[lv2.field_4188[5].shape];
                float aq = box[lv2.field_4188[6].shape] * box[lv2.field_4188[7].shape];
                this.brightness[lv15.firstCorner] = x * ab + y * ac + z * ad + aa * ae;
                this.brightness[lv15.secondCorner] = x * af + y * ag + z * ah + aa * ai;
                this.brightness[lv15.thirdCorner] = x * aj + y * ak + z * al + aa * am;
                this.brightness[lv15.fourthCorner] = x * an + y * ao + z * ap + aa * aq;
                int ar = this.getAmbientOcclusionBrightness(l, i, q, v);
                int as = this.getAmbientOcclusionBrightness(k, i, o, v);
                int at = this.getAmbientOcclusionBrightness(k, j, s, v);
                int au = this.getAmbientOcclusionBrightness(l, j, u, v);
                this.light[lv15.firstCorner] = this.getBrightness(ar, as, at, au, ab, ac, ad, ae);
                this.light[lv15.secondCorner] = this.getBrightness(ar, as, at, au, af, ag, ah, ai);
                this.light[lv15.thirdCorner] = this.getBrightness(ar, as, at, au, aj, ak, al, am);
                this.light[lv15.fourthCorner] = this.getBrightness(ar, as, at, au, an, ao, ap, aq);
            }
            x = world.getBrightness(direction, shaded);
            int av = 0;
            while (av < this.brightness.length) {
                int n2 = av++;
                this.brightness[n2] = this.brightness[n2] * x;
            }
        }

        private int getAmbientOcclusionBrightness(int i, int j, int k, int l) {
            if (i == 0) {
                i = l;
            }
            if (j == 0) {
                j = l;
            }
            if (k == 0) {
                k = l;
            }
            return i + j + k + l >> 2 & 0xFF00FF;
        }

        private int getBrightness(int i, int j, int k, int l, float f, float g, float h, float m) {
            int n = (int)((float)(i >> 16 & 0xFF) * f + (float)(j >> 16 & 0xFF) * g + (float)(k >> 16 & 0xFF) * h + (float)(l >> 16 & 0xFF) * m) & 0xFF;
            int o = (int)((float)(i & 0xFF) * f + (float)(j & 0xFF) * g + (float)(k & 0xFF) * h + (float)(l & 0xFF) * m) & 0xFF;
            return n << 16 | o;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class BrightnessCache {
        private boolean enabled;
        private final Long2IntLinkedOpenHashMap intCache = Util.make(() -> {
            Long2IntLinkedOpenHashMap long2IntLinkedOpenHashMap = new Long2IntLinkedOpenHashMap(100, 0.25f){

                @Override
                protected void rehash(int newN) {
                }
            };
            long2IntLinkedOpenHashMap.defaultReturnValue(Integer.MAX_VALUE);
            return long2IntLinkedOpenHashMap;
        });
        private final Long2FloatLinkedOpenHashMap floatCache = Util.make(() -> {
            Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = new Long2FloatLinkedOpenHashMap(100, 0.25f){

                @Override
                protected void rehash(int newN) {
                }
            };
            long2FloatLinkedOpenHashMap.defaultReturnValue(Float.NaN);
            return long2FloatLinkedOpenHashMap;
        });

        private BrightnessCache() {
        }

        public void enable() {
            this.enabled = true;
        }

        public void disable() {
            this.enabled = false;
            this.intCache.clear();
            this.floatCache.clear();
        }

        public int getInt(BlockState state, BlockRenderView world, BlockPos pos) {
            int i;
            long l = pos.asLong();
            if (this.enabled && (i = this.intCache.get(l)) != Integer.MAX_VALUE) {
                return i;
            }
            i = WorldRenderer.getLightmapCoordinates(world, state, pos);
            if (this.enabled) {
                if (this.intCache.size() == 100) {
                    this.intCache.removeFirstInt();
                }
                this.intCache.put(l, i);
            }
            return i;
        }

        public float getFloat(BlockState state, BlockRenderView blockView, BlockPos pos) {
            float f;
            long l = pos.asLong();
            if (this.enabled && !Float.isNaN(f = this.floatCache.get(l))) {
                return f;
            }
            f = state.getAmbientOcclusionLightLevel(blockView, pos);
            if (this.enabled) {
                if (this.floatCache.size() == 100) {
                    this.floatCache.removeFirstFloat();
                }
                this.floatCache.put(l, f);
            }
            return f;
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static enum NeighborData {
        DOWN(new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}, 0.5f, true, new NeighborOrientation[]{NeighborOrientation.FLIP_WEST, NeighborOrientation.SOUTH, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.WEST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.WEST, NeighborOrientation.SOUTH}, new NeighborOrientation[]{NeighborOrientation.FLIP_WEST, NeighborOrientation.NORTH, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.WEST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.WEST, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.FLIP_EAST, NeighborOrientation.NORTH, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.EAST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.EAST, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.FLIP_EAST, NeighborOrientation.SOUTH, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.EAST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.EAST, NeighborOrientation.SOUTH}),
        UP(new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}, 1.0f, true, new NeighborOrientation[]{NeighborOrientation.EAST, NeighborOrientation.SOUTH, NeighborOrientation.EAST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_EAST, NeighborOrientation.SOUTH}, new NeighborOrientation[]{NeighborOrientation.EAST, NeighborOrientation.NORTH, NeighborOrientation.EAST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_EAST, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.WEST, NeighborOrientation.NORTH, NeighborOrientation.WEST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_WEST, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.WEST, NeighborOrientation.SOUTH, NeighborOrientation.WEST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_WEST, NeighborOrientation.SOUTH}),
        NORTH(new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST}, 0.8f, true, new NeighborOrientation[]{NeighborOrientation.UP, NeighborOrientation.FLIP_WEST, NeighborOrientation.UP, NeighborOrientation.WEST, NeighborOrientation.FLIP_UP, NeighborOrientation.WEST, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_WEST}, new NeighborOrientation[]{NeighborOrientation.UP, NeighborOrientation.FLIP_EAST, NeighborOrientation.UP, NeighborOrientation.EAST, NeighborOrientation.FLIP_UP, NeighborOrientation.EAST, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_EAST}, new NeighborOrientation[]{NeighborOrientation.DOWN, NeighborOrientation.FLIP_EAST, NeighborOrientation.DOWN, NeighborOrientation.EAST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.EAST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_EAST}, new NeighborOrientation[]{NeighborOrientation.DOWN, NeighborOrientation.FLIP_WEST, NeighborOrientation.DOWN, NeighborOrientation.WEST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.WEST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_WEST}),
        SOUTH(new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP}, 0.8f, true, new NeighborOrientation[]{NeighborOrientation.UP, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_UP, NeighborOrientation.WEST, NeighborOrientation.UP, NeighborOrientation.WEST}, new NeighborOrientation[]{NeighborOrientation.DOWN, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_WEST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.WEST, NeighborOrientation.DOWN, NeighborOrientation.WEST}, new NeighborOrientation[]{NeighborOrientation.DOWN, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_DOWN, NeighborOrientation.EAST, NeighborOrientation.DOWN, NeighborOrientation.EAST}, new NeighborOrientation[]{NeighborOrientation.UP, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_EAST, NeighborOrientation.FLIP_UP, NeighborOrientation.EAST, NeighborOrientation.UP, NeighborOrientation.EAST}),
        WEST(new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH}, 0.6f, true, new NeighborOrientation[]{NeighborOrientation.UP, NeighborOrientation.SOUTH, NeighborOrientation.UP, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_UP, NeighborOrientation.SOUTH}, new NeighborOrientation[]{NeighborOrientation.UP, NeighborOrientation.NORTH, NeighborOrientation.UP, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_UP, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.DOWN, NeighborOrientation.NORTH, NeighborOrientation.DOWN, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_NORTH, NeighborOrientation.FLIP_DOWN, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.DOWN, NeighborOrientation.SOUTH, NeighborOrientation.DOWN, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.FLIP_DOWN, NeighborOrientation.SOUTH}),
        EAST(new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH}, 0.6f, true, new NeighborOrientation[]{NeighborOrientation.FLIP_DOWN, NeighborOrientation.SOUTH, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.DOWN, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.DOWN, NeighborOrientation.SOUTH}, new NeighborOrientation[]{NeighborOrientation.FLIP_DOWN, NeighborOrientation.NORTH, NeighborOrientation.FLIP_DOWN, NeighborOrientation.FLIP_NORTH, NeighborOrientation.DOWN, NeighborOrientation.FLIP_NORTH, NeighborOrientation.DOWN, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.FLIP_UP, NeighborOrientation.NORTH, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_NORTH, NeighborOrientation.UP, NeighborOrientation.FLIP_NORTH, NeighborOrientation.UP, NeighborOrientation.NORTH}, new NeighborOrientation[]{NeighborOrientation.FLIP_UP, NeighborOrientation.SOUTH, NeighborOrientation.FLIP_UP, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.UP, NeighborOrientation.FLIP_SOUTH, NeighborOrientation.UP, NeighborOrientation.SOUTH});

        final Direction[] faces;
        final boolean nonCubicWeight;
        final NeighborOrientation[] field_4192;
        final NeighborOrientation[] field_4185;
        final NeighborOrientation[] field_4180;
        final NeighborOrientation[] field_4188;
        private static final NeighborData[] VALUES;

        private NeighborData(Direction[] faces, float f, boolean nonCubicWeight, NeighborOrientation[] args2, NeighborOrientation[] args3, NeighborOrientation[] args4, NeighborOrientation[] args5) {
            this.faces = faces;
            this.nonCubicWeight = nonCubicWeight;
            this.field_4192 = args2;
            this.field_4185 = args3;
            this.field_4180 = args4;
            this.field_4188 = args5;
        }

        public static NeighborData getData(Direction direction) {
            return VALUES[direction.getId()];
        }

        static {
            VALUES = Util.make(new NeighborData[6], values -> {
                values[Direction.DOWN.getId()] = DOWN;
                values[Direction.UP.getId()] = UP;
                values[Direction.NORTH.getId()] = NORTH;
                values[Direction.SOUTH.getId()] = SOUTH;
                values[Direction.WEST.getId()] = WEST;
                values[Direction.EAST.getId()] = EAST;
            });
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static enum NeighborOrientation {
        DOWN(Direction.DOWN, false),
        UP(Direction.UP, false),
        NORTH(Direction.NORTH, false),
        SOUTH(Direction.SOUTH, false),
        WEST(Direction.WEST, false),
        EAST(Direction.EAST, false),
        FLIP_DOWN(Direction.DOWN, true),
        FLIP_UP(Direction.UP, true),
        FLIP_NORTH(Direction.NORTH, true),
        FLIP_SOUTH(Direction.SOUTH, true),
        FLIP_WEST(Direction.WEST, true),
        FLIP_EAST(Direction.EAST, true);

        final int shape;

        private NeighborOrientation(Direction direction, boolean flip) {
            this.shape = direction.getId() + (flip ? DIRECTIONS.length : 0);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum Translation {
        DOWN(0, 1, 2, 3),
        UP(2, 3, 0, 1),
        NORTH(3, 0, 1, 2),
        SOUTH(0, 1, 2, 3),
        WEST(3, 0, 1, 2),
        EAST(1, 2, 3, 0);

        final int firstCorner;
        final int secondCorner;
        final int thirdCorner;
        final int fourthCorner;
        private static final Translation[] VALUES;

        private Translation(int firstCorner, int secondCorner, int thirdCorner, int fourthCorner) {
            this.firstCorner = firstCorner;
            this.secondCorner = secondCorner;
            this.thirdCorner = thirdCorner;
            this.fourthCorner = fourthCorner;
        }

        public static Translation getTranslations(Direction direction) {
            return VALUES[direction.getId()];
        }

        static {
            VALUES = Util.make(new Translation[6], values -> {
                values[Direction.DOWN.getId()] = DOWN;
                values[Direction.UP.getId()] = UP;
                values[Direction.NORTH.getId()] = NORTH;
                values[Direction.SOUTH.getId()] = SOUTH;
                values[Direction.WEST.getId()] = WEST;
                values[Direction.EAST.getId()] = EAST;
            });
        }
    }
}

