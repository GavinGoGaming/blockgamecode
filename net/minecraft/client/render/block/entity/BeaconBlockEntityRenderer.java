/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public class BeaconBlockEntityRenderer
implements BlockEntityRenderer<BeaconBlockEntity> {
    public static final Identifier BEAM_TEXTURE = Identifier.ofVanilla("textures/entity/beacon_beam.png");
    public static final int MAX_BEAM_HEIGHT = 1024;

    public BeaconBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(BeaconBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
        long l = arg.getWorld().getTime();
        List<BeaconBlockEntity.BeamSegment> list = arg.getBeamSegments();
        int k = 0;
        for (int m = 0; m < list.size(); ++m) {
            BeaconBlockEntity.BeamSegment lv = list.get(m);
            BeaconBlockEntityRenderer.renderBeam(arg2, arg3, f, l, k, m == list.size() - 1 ? 1024 : lv.getHeight(), lv.getColor());
            k += lv.getHeight();
        }
    }

    private static void renderBeam(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta, long worldTime, int yOffset, int maxY, int color) {
        BeaconBlockEntityRenderer.renderBeam(matrices, vertexConsumers, BEAM_TEXTURE, tickDelta, 1.0f, worldTime, yOffset, maxY, color, 0.2f, 0.25f);
    }

    public static void renderBeam(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Identifier textureId, float tickDelta, float heightScale, long worldTime, int yOffset, int maxY, int color, float innerRadius, float outerRadius) {
        int n = yOffset + maxY;
        matrices.push();
        matrices.translate(0.5, 0.0, 0.5);
        float o = (float)Math.floorMod(worldTime, 40) + tickDelta;
        float p = maxY < 0 ? o : -o;
        float q = MathHelper.fractionalPart(p * 0.2f - (float)MathHelper.floor(p * 0.1f));
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(o * 2.25f - 45.0f));
        float r = 0.0f;
        float s = innerRadius;
        float t = innerRadius;
        float u = 0.0f;
        float v = -innerRadius;
        float w = 0.0f;
        float x = 0.0f;
        float y = -innerRadius;
        float z = 0.0f;
        float aa = 1.0f;
        float ab = -1.0f + q;
        float ac = (float)maxY * heightScale * (0.5f / innerRadius) + ab;
        BeaconBlockEntityRenderer.renderBeamLayer(matrices, vertexConsumers.getBuffer(RenderLayer.getBeaconBeam(textureId, false)), color, yOffset, n, 0.0f, s, t, 0.0f, v, 0.0f, 0.0f, y, 0.0f, 1.0f, ac, ab);
        matrices.pop();
        r = -outerRadius;
        s = -outerRadius;
        t = outerRadius;
        u = -outerRadius;
        v = -outerRadius;
        w = outerRadius;
        x = outerRadius;
        y = outerRadius;
        z = 0.0f;
        aa = 1.0f;
        ab = -1.0f + q;
        ac = (float)maxY * heightScale + ab;
        BeaconBlockEntityRenderer.renderBeamLayer(matrices, vertexConsumers.getBuffer(RenderLayer.getBeaconBeam(textureId, true)), ColorHelper.Argb.withAlpha(32, color), yOffset, n, r, s, t, u, v, w, x, y, 0.0f, 1.0f, ac, ab);
        matrices.pop();
    }

    private static void renderBeamLayer(MatrixStack matrices, VertexConsumer vertices, int color, int yOffset, int height, float x1, float z1, float x2, float z2, float x3, float z3, float x4, float z4, float u1, float u2, float v1, float v2) {
        MatrixStack.Entry lv = matrices.peek();
        BeaconBlockEntityRenderer.renderBeamFace(lv, vertices, color, yOffset, height, x1, z1, x2, z2, u1, u2, v1, v2);
        BeaconBlockEntityRenderer.renderBeamFace(lv, vertices, color, yOffset, height, x4, z4, x3, z3, u1, u2, v1, v2);
        BeaconBlockEntityRenderer.renderBeamFace(lv, vertices, color, yOffset, height, x2, z2, x4, z4, u1, u2, v1, v2);
        BeaconBlockEntityRenderer.renderBeamFace(lv, vertices, color, yOffset, height, x3, z3, x1, z1, u1, u2, v1, v2);
    }

    private static void renderBeamFace(MatrixStack.Entry matrix, VertexConsumer vertices, int color, int yOffset, int height, float x1, float z1, float x2, float z2, float u1, float u2, float v1, float v2) {
        BeaconBlockEntityRenderer.renderBeamVertex(matrix, vertices, color, height, x1, z1, u2, v1);
        BeaconBlockEntityRenderer.renderBeamVertex(matrix, vertices, color, yOffset, x1, z1, u2, v2);
        BeaconBlockEntityRenderer.renderBeamVertex(matrix, vertices, color, yOffset, x2, z2, u1, v2);
        BeaconBlockEntityRenderer.renderBeamVertex(matrix, vertices, color, height, x2, z2, u1, v1);
    }

    private static void renderBeamVertex(MatrixStack.Entry matrix, VertexConsumer vertices, int color, int y, float x, float z, float u, float v) {
        vertices.vertex(matrix, x, (float)y, z).color(color).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(matrix, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public boolean rendersOutsideBoundingBox(BeaconBlockEntity arg) {
        return true;
    }

    @Override
    public int getRenderDistance() {
        return 256;
    }

    @Override
    public boolean isInRenderDistance(BeaconBlockEntity arg, Vec3d arg2) {
        return Vec3d.ofCenter(arg.getPos()).multiply(1.0, 0.0, 1.0).isInRange(arg2.multiply(1.0, 0.0, 1.0), this.getRenderDistance());
    }
}

