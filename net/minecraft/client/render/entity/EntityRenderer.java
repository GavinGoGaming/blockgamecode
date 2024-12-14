/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.Leashable;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public abstract class EntityRenderer<T extends Entity> {
    protected static final float field_32921 = 0.025f;
    public static final int field_52257 = 24;
    protected final EntityRenderDispatcher dispatcher;
    private final TextRenderer textRenderer;
    protected float shadowRadius;
    protected float shadowOpacity = 1.0f;

    protected EntityRenderer(EntityRendererFactory.Context ctx) {
        this.dispatcher = ctx.getRenderDispatcher();
        this.textRenderer = ctx.getTextRenderer();
    }

    public final int getLight(T entity, float tickDelta) {
        BlockPos lv = BlockPos.ofFloored(((Entity)entity).getClientCameraPosVec(tickDelta));
        return LightmapTextureManager.pack(this.getBlockLight(entity, lv), this.getSkyLight(entity, lv));
    }

    protected int getSkyLight(T entity, BlockPos pos) {
        return ((Entity)entity).getWorld().getLightLevel(LightType.SKY, pos);
    }

    protected int getBlockLight(T entity, BlockPos pos) {
        if (((Entity)entity).isOnFire()) {
            return 15;
        }
        return ((Entity)entity).getWorld().getLightLevel(LightType.BLOCK, pos);
    }

    public boolean shouldRender(T entity, Frustum frustum, double x, double y, double z) {
        Leashable lv2;
        Entity lv3;
        if (!((Entity)entity).shouldRender(x, y, z)) {
            return false;
        }
        if (((Entity)entity).ignoreCameraFrustum) {
            return true;
        }
        Box lv = ((Entity)entity).getVisibilityBoundingBox().expand(0.5);
        if (lv.isNaN() || lv.getAverageSideLength() == 0.0) {
            lv = new Box(((Entity)entity).getX() - 2.0, ((Entity)entity).getY() - 2.0, ((Entity)entity).getZ() - 2.0, ((Entity)entity).getX() + 2.0, ((Entity)entity).getY() + 2.0, ((Entity)entity).getZ() + 2.0);
        }
        if (frustum.isVisible(lv)) {
            return true;
        }
        if (entity instanceof Leashable && (lv3 = (lv2 = (Leashable)entity).getLeashHolder()) != null) {
            return frustum.isVisible(lv3.getVisibilityBoundingBox());
        }
        return false;
    }

    public Vec3d getPositionOffset(T entity, float tickDelta) {
        return Vec3d.ZERO;
    }

    public void render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        Leashable lv;
        Entity lv2;
        if (entity instanceof Leashable && (lv2 = (lv = (Leashable)entity).getLeashHolder()) != null) {
            this.renderLeash(entity, tickDelta, matrices, vertexConsumers, lv2);
        }
        if (!this.hasLabel(entity)) {
            return;
        }
        this.renderLabelIfPresent(entity, ((Entity)entity).getDisplayName(), matrices, vertexConsumers, light, tickDelta);
    }

    private <E extends Entity> void renderLeash(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, E leashHolder) {
        int v;
        matrices.push();
        Vec3d lv = leashHolder.getLeashPos(tickDelta);
        double d = (double)(((Entity)entity).lerpYaw(tickDelta) * ((float)Math.PI / 180)) + 1.5707963267948966;
        Vec3d lv2 = ((Entity)entity).getLeashOffset(tickDelta);
        double e = Math.cos(d) * lv2.z + Math.sin(d) * lv2.x;
        double g = Math.sin(d) * lv2.z - Math.cos(d) * lv2.x;
        double h = MathHelper.lerp((double)tickDelta, ((Entity)entity).prevX, ((Entity)entity).getX()) + e;
        double i = MathHelper.lerp((double)tickDelta, ((Entity)entity).prevY, ((Entity)entity).getY()) + lv2.y;
        double j = MathHelper.lerp((double)tickDelta, ((Entity)entity).prevZ, ((Entity)entity).getZ()) + g;
        matrices.translate(e, lv2.y, g);
        float k = (float)(lv.x - h);
        float l = (float)(lv.y - i);
        float m = (float)(lv.z - j);
        float n = 0.025f;
        VertexConsumer lv3 = vertexConsumers.getBuffer(RenderLayer.getLeash());
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float o = MathHelper.inverseSqrt(k * k + m * m) * 0.025f / 2.0f;
        float p = m * o;
        float q = k * o;
        BlockPos lv4 = BlockPos.ofFloored(((Entity)entity).getCameraPosVec(tickDelta));
        BlockPos lv5 = BlockPos.ofFloored(leashHolder.getCameraPosVec(tickDelta));
        int r = this.getBlockLight(entity, lv4);
        int s = this.dispatcher.getRenderer(leashHolder).getBlockLight(leashHolder, lv5);
        int t = ((Entity)entity).getWorld().getLightLevel(LightType.SKY, lv4);
        int u = ((Entity)entity).getWorld().getLightLevel(LightType.SKY, lv5);
        for (v = 0; v <= 24; ++v) {
            EntityRenderer.renderLeashSegment(lv3, matrix4f, k, l, m, r, s, t, u, 0.025f, 0.025f, p, q, v, false);
        }
        for (v = 24; v >= 0; --v) {
            EntityRenderer.renderLeashSegment(lv3, matrix4f, k, l, m, r, s, t, u, 0.025f, 0.0f, p, q, v, true);
        }
        matrices.pop();
    }

    private static void renderLeashSegment(VertexConsumer vertexConsumer, Matrix4f matrix, float leashedEntityX, float leashedEntityY, float leashedEntityZ, int leashedEntityBlockLight, int leashHolderBlockLight, int leashedEntitySkyLight, int leashHolderSkyLight, float m, float n, float o, float p, int segmentIndex, boolean isLeashKnot) {
        float r = (float)segmentIndex / 24.0f;
        int s = (int)MathHelper.lerp(r, (float)leashedEntityBlockLight, (float)leashHolderBlockLight);
        int t = (int)MathHelper.lerp(r, (float)leashedEntitySkyLight, (float)leashHolderSkyLight);
        int u = LightmapTextureManager.pack(s, t);
        float v = segmentIndex % 2 == (isLeashKnot ? 1 : 0) ? 0.7f : 1.0f;
        float w = 0.5f * v;
        float x = 0.4f * v;
        float y = 0.3f * v;
        float z = leashedEntityX * r;
        float aa = leashedEntityY > 0.0f ? leashedEntityY * r * r : leashedEntityY - leashedEntityY * (1.0f - r) * (1.0f - r);
        float ab = leashedEntityZ * r;
        vertexConsumer.vertex(matrix, z - o, aa + n, ab + p).color(w, x, y, 1.0f).light(u);
        vertexConsumer.vertex(matrix, z + o, aa + m - n, ab - p).color(w, x, y, 1.0f).light(u);
    }

    protected boolean hasLabel(T entity) {
        return ((Entity)entity).shouldRenderName() || ((Entity)entity).hasCustomName() && entity == this.dispatcher.targetedEntity;
    }

    public abstract Identifier getTexture(T var1);

    public TextRenderer getTextRenderer() {
        return this.textRenderer;
    }

    protected void renderLabelIfPresent(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta) {
        double d = this.dispatcher.getSquaredDistanceToCamera((Entity)entity);
        if (d > 4096.0) {
            return;
        }
        Vec3d lv = ((Entity)entity).getAttachments().getPointNullable(EntityAttachmentType.NAME_TAG, 0, ((Entity)entity).getYaw(tickDelta));
        if (lv == null) {
            return;
        }
        boolean bl = !((Entity)entity).isSneaky();
        int j = "deadmau5".equals(text.getString()) ? -10 : 0;
        matrices.push();
        matrices.translate(lv.x, lv.y + 0.5, lv.z);
        matrices.multiply(this.dispatcher.getRotation());
        matrices.scale(0.025f, -0.025f, 0.025f);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25f);
        int k = (int)(g * 255.0f) << 24;
        TextRenderer lv2 = this.getTextRenderer();
        float h = -lv2.getWidth(text) / 2;
        lv2.draw(text, h, (float)j, 0x20FFFFFF, false, matrix4f, vertexConsumers, bl ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, k, light);
        if (bl) {
            lv2.draw(text, h, (float)j, Colors.WHITE, false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
        }
        matrices.pop();
    }

    protected float getShadowRadius(T entity) {
        return this.shadowRadius;
    }
}

