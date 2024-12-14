/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.TranslucentBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;

@Environment(value=EnvType.CLIENT)
public class FluidRenderer {
    private static final float field_32781 = 0.8888889f;
    private final Sprite[] lavaSprites = new Sprite[2];
    private final Sprite[] waterSprites = new Sprite[2];
    private Sprite waterOverlaySprite;

    protected void onResourceReload() {
        this.lavaSprites[0] = MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(Blocks.LAVA.getDefaultState()).getParticleSprite();
        this.lavaSprites[1] = ModelLoader.LAVA_FLOW.getSprite();
        this.waterSprites[0] = MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(Blocks.WATER.getDefaultState()).getParticleSprite();
        this.waterSprites[1] = ModelLoader.WATER_FLOW.getSprite();
        this.waterOverlaySprite = ModelLoader.WATER_OVERLAY.getSprite();
    }

    private static boolean isSameFluid(FluidState a, FluidState b) {
        return b.getFluid().matchesType(a.getFluid());
    }

    private static boolean isSideCovered(BlockView world, Direction direction, float height, BlockPos pos, BlockState state) {
        if (state.isOpaque()) {
            VoxelShape lv = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, height, 1.0);
            VoxelShape lv2 = state.getCullingShape(world, pos);
            return VoxelShapes.isSideCovered(lv, lv2, direction);
        }
        return false;
    }

    private static boolean isSideCovered(BlockView world, BlockPos pos, Direction direction, float maxDeviation, BlockState state) {
        return FluidRenderer.isSideCovered(world, direction, maxDeviation, pos.offset(direction), state);
    }

    private static boolean isOppositeSideCovered(BlockView world, BlockPos pos, BlockState state, Direction direction) {
        return FluidRenderer.isSideCovered(world, direction.getOpposite(), 1.0f, pos, state);
    }

    public static boolean shouldRenderSide(BlockRenderView world, BlockPos pos, FluidState fluidState, BlockState blockState, Direction direction, FluidState neighborFluidState) {
        return !FluidRenderer.isOppositeSideCovered(world, pos, blockState, direction) && !FluidRenderer.isSameFluid(fluidState, neighborFluidState);
    }

    public void render(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
        float am;
        float al;
        float ai;
        float ae;
        float ad;
        float ac;
        float ab;
        float aa;
        float z;
        float x;
        float w;
        float v;
        float u;
        float t;
        float s;
        float r;
        float q;
        float p;
        float o;
        boolean bl = fluidState.isIn(FluidTags.LAVA);
        Sprite[] lvs = bl ? this.lavaSprites : this.waterSprites;
        int i = bl ? 0xFFFFFF : BiomeColors.getWaterColor(world, pos);
        float f = (float)(i >> 16 & 0xFF) / 255.0f;
        float g = (float)(i >> 8 & 0xFF) / 255.0f;
        float h = (float)(i & 0xFF) / 255.0f;
        BlockState lv = world.getBlockState(pos.offset(Direction.DOWN));
        FluidState lv2 = lv.getFluidState();
        BlockState lv3 = world.getBlockState(pos.offset(Direction.UP));
        FluidState lv4 = lv3.getFluidState();
        BlockState lv5 = world.getBlockState(pos.offset(Direction.NORTH));
        FluidState lv6 = lv5.getFluidState();
        BlockState lv7 = world.getBlockState(pos.offset(Direction.SOUTH));
        FluidState lv8 = lv7.getFluidState();
        BlockState lv9 = world.getBlockState(pos.offset(Direction.WEST));
        FluidState lv10 = lv9.getFluidState();
        BlockState lv11 = world.getBlockState(pos.offset(Direction.EAST));
        FluidState lv12 = lv11.getFluidState();
        boolean bl2 = !FluidRenderer.isSameFluid(fluidState, lv4);
        boolean bl3 = FluidRenderer.shouldRenderSide(world, pos, fluidState, blockState, Direction.DOWN, lv2) && !FluidRenderer.isSideCovered((BlockView)world, pos, Direction.DOWN, 0.8888889f, lv);
        boolean bl4 = FluidRenderer.shouldRenderSide(world, pos, fluidState, blockState, Direction.NORTH, lv6);
        boolean bl5 = FluidRenderer.shouldRenderSide(world, pos, fluidState, blockState, Direction.SOUTH, lv8);
        boolean bl6 = FluidRenderer.shouldRenderSide(world, pos, fluidState, blockState, Direction.WEST, lv10);
        boolean bl7 = FluidRenderer.shouldRenderSide(world, pos, fluidState, blockState, Direction.EAST, lv12);
        if (!(bl2 || bl3 || bl7 || bl6 || bl4 || bl5)) {
            return;
        }
        float j = world.getBrightness(Direction.DOWN, true);
        float k = world.getBrightness(Direction.UP, true);
        float l = world.getBrightness(Direction.NORTH, true);
        float m = world.getBrightness(Direction.WEST, true);
        Fluid lv13 = fluidState.getFluid();
        float n = this.getFluidHeight(world, lv13, pos, blockState, fluidState);
        if (n >= 1.0f) {
            o = 1.0f;
            p = 1.0f;
            q = 1.0f;
            r = 1.0f;
        } else {
            s = this.getFluidHeight(world, lv13, pos.north(), lv5, lv6);
            t = this.getFluidHeight(world, lv13, pos.south(), lv7, lv8);
            u = this.getFluidHeight(world, lv13, pos.east(), lv11, lv12);
            v = this.getFluidHeight(world, lv13, pos.west(), lv9, lv10);
            o = this.calculateFluidHeight(world, lv13, n, s, u, pos.offset(Direction.NORTH).offset(Direction.EAST));
            p = this.calculateFluidHeight(world, lv13, n, s, v, pos.offset(Direction.NORTH).offset(Direction.WEST));
            q = this.calculateFluidHeight(world, lv13, n, t, u, pos.offset(Direction.SOUTH).offset(Direction.EAST));
            r = this.calculateFluidHeight(world, lv13, n, t, v, pos.offset(Direction.SOUTH).offset(Direction.WEST));
        }
        s = pos.getX() & 0xF;
        t = pos.getY() & 0xF;
        u = pos.getZ() & 0xF;
        v = 0.001f;
        float f2 = w = bl3 ? 0.001f : 0.0f;
        if (bl2 && !FluidRenderer.isSideCovered((BlockView)world, pos, Direction.UP, Math.min(Math.min(p, r), Math.min(q, o)), lv3)) {
            float ag;
            float af;
            float y;
            p -= 0.001f;
            r -= 0.001f;
            q -= 0.001f;
            o -= 0.001f;
            Vec3d lv14 = fluidState.getVelocity(world, pos);
            if (lv14.x == 0.0 && lv14.z == 0.0) {
                lv15 = lvs[0];
                x = lv15.getFrameU(0.0f);
                y = lv15.getFrameV(0.0f);
                z = x;
                aa = lv15.getFrameV(1.0f);
                ab = lv15.getFrameU(1.0f);
                ac = aa;
                ad = ab;
                ae = y;
            } else {
                lv15 = lvs[1];
                af = (float)MathHelper.atan2(lv14.z, lv14.x) - 1.5707964f;
                ag = MathHelper.sin(af) * 0.25f;
                float ah = MathHelper.cos(af) * 0.25f;
                ai = 0.5f;
                x = lv15.getFrameU(0.5f + (-ah - ag));
                y = lv15.getFrameV(0.5f + (-ah + ag));
                z = lv15.getFrameU(0.5f + (-ah + ag));
                aa = lv15.getFrameV(0.5f + (ah + ag));
                ab = lv15.getFrameU(0.5f + (ah + ag));
                ac = lv15.getFrameV(0.5f + (ah - ag));
                ad = lv15.getFrameU(0.5f + (ah - ag));
                ae = lv15.getFrameV(0.5f + (-ah - ag));
            }
            float aj = (x + z + ab + ad) / 4.0f;
            af = (y + aa + ac + ae) / 4.0f;
            ag = lvs[0].getAnimationFrameDelta();
            x = MathHelper.lerp(ag, x, aj);
            z = MathHelper.lerp(ag, z, aj);
            ab = MathHelper.lerp(ag, ab, aj);
            ad = MathHelper.lerp(ag, ad, aj);
            y = MathHelper.lerp(ag, y, af);
            aa = MathHelper.lerp(ag, aa, af);
            ac = MathHelper.lerp(ag, ac, af);
            ae = MathHelper.lerp(ag, ae, af);
            int ak = this.getLight(world, pos);
            ai = k * f;
            al = k * g;
            am = k * h;
            this.vertex(vertexConsumer, s + 0.0f, t + p, u + 0.0f, ai, al, am, x, y, ak);
            this.vertex(vertexConsumer, s + 0.0f, t + r, u + 1.0f, ai, al, am, z, aa, ak);
            this.vertex(vertexConsumer, s + 1.0f, t + q, u + 1.0f, ai, al, am, ab, ac, ak);
            this.vertex(vertexConsumer, s + 1.0f, t + o, u + 0.0f, ai, al, am, ad, ae, ak);
            if (fluidState.canFlowTo(world, pos.up())) {
                this.vertex(vertexConsumer, s + 0.0f, t + p, u + 0.0f, ai, al, am, x, y, ak);
                this.vertex(vertexConsumer, s + 1.0f, t + o, u + 0.0f, ai, al, am, ad, ae, ak);
                this.vertex(vertexConsumer, s + 1.0f, t + q, u + 1.0f, ai, al, am, ab, ac, ak);
                this.vertex(vertexConsumer, s + 0.0f, t + r, u + 1.0f, ai, al, am, z, aa, ak);
            }
        }
        if (bl3) {
            x = lvs[0].getMinU();
            z = lvs[0].getMaxU();
            ab = lvs[0].getMinV();
            ad = lvs[0].getMaxV();
            int an = this.getLight(world, pos.down());
            aa = j * f;
            ac = j * g;
            ae = j * h;
            this.vertex(vertexConsumer, s, t + w, u + 1.0f, aa, ac, ae, x, ad, an);
            this.vertex(vertexConsumer, s, t + w, u, aa, ac, ae, x, ab, an);
            this.vertex(vertexConsumer, s + 1.0f, t + w, u, aa, ac, ae, z, ab, an);
            this.vertex(vertexConsumer, s + 1.0f, t + w, u + 1.0f, aa, ac, ae, z, ad, an);
        }
        int ao = this.getLight(world, pos);
        for (Direction lv16 : Direction.Type.HORIZONTAL) {
            Block lv19;
            float ap;
            float y;
            if (!(switch (lv16) {
                case Direction.NORTH -> {
                    ad = p;
                    y = o;
                    aa = s;
                    ae = s + 1.0f;
                    ac = u + 0.001f;
                    ap = u + 0.001f;
                    yield bl4;
                }
                case Direction.SOUTH -> {
                    ad = q;
                    y = r;
                    aa = s + 1.0f;
                    ae = s;
                    ac = u + 1.0f - 0.001f;
                    ap = u + 1.0f - 0.001f;
                    yield bl5;
                }
                case Direction.WEST -> {
                    ad = r;
                    y = p;
                    aa = s + 0.001f;
                    ae = s + 0.001f;
                    ac = u + 1.0f;
                    ap = u;
                    yield bl6;
                }
                default -> {
                    ad = o;
                    y = q;
                    aa = s + 1.0f - 0.001f;
                    ae = s + 1.0f - 0.001f;
                    ac = u;
                    ap = u + 1.0f;
                    yield bl7;
                }
            }) || FluidRenderer.isSideCovered((BlockView)world, pos, lv16, Math.max(ad, y), world.getBlockState(pos.offset(lv16)))) continue;
            BlockPos lv17 = pos.offset(lv16);
            Sprite lv18 = lvs[1];
            if (!bl && ((lv19 = world.getBlockState(lv17).getBlock()) instanceof TranslucentBlock || lv19 instanceof LeavesBlock)) {
                lv18 = this.waterOverlaySprite;
            }
            float ah = lv18.getFrameU(0.0f);
            ai = lv18.getFrameU(0.5f);
            al = lv18.getFrameV((1.0f - ad) * 0.5f);
            am = lv18.getFrameV((1.0f - y) * 0.5f);
            float aq = lv18.getFrameV(0.5f);
            float ar = lv16.getAxis() == Direction.Axis.Z ? l : m;
            float as = k * ar * f;
            float at = k * ar * g;
            float au = k * ar * h;
            this.vertex(vertexConsumer, aa, t + ad, ac, as, at, au, ah, al, ao);
            this.vertex(vertexConsumer, ae, t + y, ap, as, at, au, ai, am, ao);
            this.vertex(vertexConsumer, ae, t + w, ap, as, at, au, ai, aq, ao);
            this.vertex(vertexConsumer, aa, t + w, ac, as, at, au, ah, aq, ao);
            if (lv18 == this.waterOverlaySprite) continue;
            this.vertex(vertexConsumer, aa, t + w, ac, as, at, au, ah, aq, ao);
            this.vertex(vertexConsumer, ae, t + w, ap, as, at, au, ai, aq, ao);
            this.vertex(vertexConsumer, ae, t + y, ap, as, at, au, ai, am, ao);
            this.vertex(vertexConsumer, aa, t + ad, ac, as, at, au, ah, al, ao);
        }
    }

    private float calculateFluidHeight(BlockRenderView world, Fluid fluid, float originHeight, float northSouthHeight, float eastWestHeight, BlockPos pos) {
        if (eastWestHeight >= 1.0f || northSouthHeight >= 1.0f) {
            return 1.0f;
        }
        float[] fs = new float[2];
        if (eastWestHeight > 0.0f || northSouthHeight > 0.0f) {
            float i = this.getFluidHeight(world, fluid, pos);
            if (i >= 1.0f) {
                return 1.0f;
            }
            this.addHeight(fs, i);
        }
        this.addHeight(fs, originHeight);
        this.addHeight(fs, eastWestHeight);
        this.addHeight(fs, northSouthHeight);
        return fs[0] / fs[1];
    }

    private void addHeight(float[] weightedAverageHeight, float height) {
        if (height >= 0.8f) {
            weightedAverageHeight[0] = weightedAverageHeight[0] + height * 10.0f;
            weightedAverageHeight[1] = weightedAverageHeight[1] + 10.0f;
        } else if (height >= 0.0f) {
            weightedAverageHeight[0] = weightedAverageHeight[0] + height;
            weightedAverageHeight[1] = weightedAverageHeight[1] + 1.0f;
        }
    }

    private float getFluidHeight(BlockRenderView world, Fluid fluid, BlockPos pos) {
        BlockState lv = world.getBlockState(pos);
        return this.getFluidHeight(world, fluid, pos, lv, lv.getFluidState());
    }

    private float getFluidHeight(BlockRenderView world, Fluid fluid, BlockPos pos, BlockState blockState, FluidState fluidState) {
        if (fluid.matchesType(fluidState.getFluid())) {
            BlockState lv = world.getBlockState(pos.up());
            if (fluid.matchesType(lv.getFluidState().getFluid())) {
                return 1.0f;
            }
            return fluidState.getHeight();
        }
        if (!blockState.isSolid()) {
            return 0.0f;
        }
        return -1.0f;
    }

    private void vertex(VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k, float l, float m, int n) {
        vertexConsumer.vertex(f, g, h).color(i, j, k, 1.0f).texture(l, m).light(n).normal(0.0f, 1.0f, 0.0f);
    }

    private int getLight(BlockRenderView world, BlockPos pos) {
        int i = WorldRenderer.getLightmapCoordinates(world, pos);
        int j = WorldRenderer.getLightmapCoordinates(world, pos.up());
        int k = i & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xF);
        int l = j & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xF);
        int m = i >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xF);
        int n = j >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xF);
        return (k > l ? k : l) | (m > n ? m : n) << 16;
    }
}

