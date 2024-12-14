/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render.entity;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class EntityRenderDispatcher
implements SynchronousResourceReloader {
    private static final RenderLayer SHADOW_LAYER = RenderLayer.getEntityShadow(Identifier.ofVanilla("textures/misc/shadow.png"));
    private static final float field_43377 = 32.0f;
    private static final float field_43378 = 0.5f;
    private Map<EntityType<?>, EntityRenderer<?>> renderers = ImmutableMap.of();
    private Map<SkinTextures.Model, EntityRenderer<? extends PlayerEntity>> modelRenderers = Map.of();
    public final TextureManager textureManager;
    private World world;
    public Camera camera;
    private Quaternionf rotation;
    public Entity targetedEntity;
    private final ItemRenderer itemRenderer;
    private final BlockRenderManager blockRenderManager;
    private final HeldItemRenderer heldItemRenderer;
    private final TextRenderer textRenderer;
    public final GameOptions gameOptions;
    private final EntityModelLoader modelLoader;
    private boolean renderShadows = true;
    private boolean renderHitboxes;

    public <E extends Entity> int getLight(E entity, float tickDelta) {
        return this.getRenderer(entity).getLight(entity, tickDelta);
    }

    public EntityRenderDispatcher(MinecraftClient client, TextureManager textureManager, ItemRenderer itemRenderer, BlockRenderManager blockRenderManager, TextRenderer textRenderer, GameOptions gameOptions, EntityModelLoader modelLoader) {
        this.textureManager = textureManager;
        this.itemRenderer = itemRenderer;
        this.heldItemRenderer = new HeldItemRenderer(client, this, itemRenderer);
        this.blockRenderManager = blockRenderManager;
        this.textRenderer = textRenderer;
        this.gameOptions = gameOptions;
        this.modelLoader = modelLoader;
    }

    public <T extends Entity> EntityRenderer<? super T> getRenderer(T entity) {
        if (entity instanceof AbstractClientPlayerEntity) {
            AbstractClientPlayerEntity lv = (AbstractClientPlayerEntity)entity;
            SkinTextures.Model lv2 = lv.getSkinTextures().model();
            EntityRenderer<? extends PlayerEntity> lv3 = this.modelRenderers.get((Object)lv2);
            if (lv3 != null) {
                return lv3;
            }
            return this.modelRenderers.get((Object)SkinTextures.Model.WIDE);
        }
        return this.renderers.get(entity.getType());
    }

    public void configure(World world, Camera camera, Entity target) {
        this.world = world;
        this.camera = camera;
        this.rotation = camera.getRotation();
        this.targetedEntity = target;
    }

    public void setRotation(Quaternionf rotation) {
        this.rotation = rotation;
    }

    public void setRenderShadows(boolean renderShadows) {
        this.renderShadows = renderShadows;
    }

    public void setRenderHitboxes(boolean renderHitboxes) {
        this.renderHitboxes = renderHitboxes;
    }

    public boolean shouldRenderHitboxes() {
        return this.renderHitboxes;
    }

    public <E extends Entity> boolean shouldRender(E entity, Frustum frustum, double x, double y, double z) {
        EntityRenderer<E> lv = this.getRenderer(entity);
        return lv.shouldRender(entity, frustum, x, y, z);
    }

    public <E extends Entity> void render(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        EntityRenderer<E> lv = this.getRenderer(entity);
        try {
            double n;
            float o;
            float m;
            Vec3d lv2 = lv.getPositionOffset(entity, tickDelta);
            double j = x + lv2.getX();
            double k = y + lv2.getY();
            double l = z + lv2.getZ();
            matrices.push();
            matrices.translate(j, k, l);
            lv.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
            if (entity.doesRenderOnFire()) {
                this.renderFire(matrices, vertexConsumers, entity, MathHelper.rotateAround(MathHelper.Y_AXIS, this.rotation, new Quaternionf()));
            }
            matrices.translate(-lv2.getX(), -lv2.getY(), -lv2.getZ());
            if (this.gameOptions.getEntityShadows().getValue().booleanValue() && this.renderShadows && !entity.isInvisible() && (m = lv.getShadowRadius(entity)) > 0.0f && (o = (float)((1.0 - (n = this.getSquaredDistanceToCamera(entity.getX(), entity.getY(), entity.getZ())) / 256.0) * (double)lv.shadowOpacity)) > 0.0f) {
                EntityRenderDispatcher.renderShadow(matrices, vertexConsumers, entity, o, tickDelta, this.world, Math.min(m, 32.0f));
            }
            if (this.renderHitboxes && !entity.isInvisible() && !MinecraftClient.getInstance().hasReducedDebugInfo()) {
                EntityRenderDispatcher.renderHitbox(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), entity, tickDelta, 1.0f, 1.0f, 1.0f);
            }
            matrices.pop();
        } catch (Throwable throwable) {
            CrashReport lv3 = CrashReport.create(throwable, "Rendering entity in world");
            CrashReportSection lv4 = lv3.addElement("Entity being rendered");
            entity.populateCrashReport(lv4);
            CrashReportSection lv5 = lv3.addElement("Renderer details");
            lv5.add("Assigned renderer", lv);
            lv5.add("Location", CrashReportSection.createPositionString((HeightLimitView)this.world, x, y, z));
            lv5.add("Rotation", Float.valueOf(yaw));
            lv5.add("Delta", Float.valueOf(tickDelta));
            throw new CrashException(lv3);
        }
    }

    private static void renderServerSideHitbox(MatrixStack matrices, Entity entity, VertexConsumerProvider vertexConsumers) {
        Entity lv = EntityRenderDispatcher.getIntegratedServerEntity(entity);
        if (lv == null) {
            DebugRenderer.drawString(matrices, vertexConsumers, "Missing", entity.getX(), entity.getBoundingBox().maxY + 1.5, entity.getZ(), Colors.RED);
            return;
        }
        matrices.push();
        matrices.translate(lv.getX() - entity.getX(), lv.getY() - entity.getY(), lv.getZ() - entity.getZ());
        EntityRenderDispatcher.renderHitbox(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), lv, 1.0f, 0.0f, 1.0f, 0.0f);
        EntityRenderDispatcher.drawVector(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), new Vector3f(), lv.getVelocity(), -256);
        matrices.pop();
    }

    @Nullable
    private static Entity getIntegratedServerEntity(Entity entity) {
        ServerWorld lv2;
        IntegratedServer lv = MinecraftClient.getInstance().getServer();
        if (lv != null && (lv2 = lv.getWorld(entity.getWorld().getRegistryKey())) != null) {
            return lv2.getEntityById(entity.getId());
        }
        return null;
    }

    private static void renderHitbox(MatrixStack matrices, VertexConsumer vertices, Entity entity, float tickDelta, float red, float green, float blue) {
        Entity lv3;
        Box lv = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
        WorldRenderer.drawBox(matrices, vertices, lv, red, green, blue, 1.0f);
        if (entity instanceof EnderDragonEntity) {
            double d = -MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX());
            double e = -MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY());
            double j = -MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ());
            for (EnderDragonPart lv2 : ((EnderDragonEntity)entity).getBodyParts()) {
                matrices.push();
                double k = d + MathHelper.lerp((double)tickDelta, lv2.lastRenderX, lv2.getX());
                double l = e + MathHelper.lerp((double)tickDelta, lv2.lastRenderY, lv2.getY());
                double m = j + MathHelper.lerp((double)tickDelta, lv2.lastRenderZ, lv2.getZ());
                matrices.translate(k, l, m);
                WorldRenderer.drawBox(matrices, vertices, lv2.getBoundingBox().offset(-lv2.getX(), -lv2.getY(), -lv2.getZ()), 0.25f, 1.0f, 0.0f, 1.0f);
                matrices.pop();
            }
        }
        if (entity instanceof LivingEntity) {
            float n = 0.01f;
            WorldRenderer.drawBox(matrices, vertices, lv.minX, entity.getStandingEyeHeight() - 0.01f, lv.minZ, lv.maxX, entity.getStandingEyeHeight() + 0.01f, lv.maxZ, 1.0f, 0.0f, 0.0f, 1.0f);
        }
        if ((lv3 = entity.getVehicle()) != null) {
            float o = Math.min(lv3.getWidth(), entity.getWidth()) / 2.0f;
            float p = 0.0625f;
            Vec3d lv4 = lv3.getPassengerRidingPos(entity).subtract(entity.getPos());
            WorldRenderer.drawBox(matrices, vertices, lv4.x - (double)o, lv4.y, lv4.z - (double)o, lv4.x + (double)o, lv4.y + 0.0625, lv4.z + (double)o, 1.0f, 1.0f, 0.0f, 1.0f);
        }
        EntityRenderDispatcher.drawVector(matrices, vertices, new Vector3f(0.0f, entity.getStandingEyeHeight(), 0.0f), entity.getRotationVec(tickDelta).multiply(2.0), -16776961);
    }

    private static void drawVector(MatrixStack matrices, VertexConsumer vertexConsumers, Vector3f offset, Vec3d vec, int color) {
        MatrixStack.Entry lv = matrices.peek();
        vertexConsumers.vertex(lv, offset).color(color).normal(lv, (float)vec.x, (float)vec.y, (float)vec.z);
        vertexConsumers.vertex(lv, (float)((double)offset.x() + vec.x), (float)((double)offset.y() + vec.y), (float)((double)offset.z() + vec.z)).color(color).normal(lv, (float)vec.x, (float)vec.y, (float)vec.z);
    }

    private void renderFire(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity, Quaternionf rotation) {
        Sprite lv = ModelLoader.FIRE_0.getSprite();
        Sprite lv2 = ModelLoader.FIRE_1.getSprite();
        matrices.push();
        float f = entity.getWidth() * 1.4f;
        matrices.scale(f, f, f);
        float g = 0.5f;
        float h = 0.0f;
        float i = entity.getHeight() / f;
        float j = 0.0f;
        matrices.multiply(rotation);
        matrices.translate(0.0f, 0.0f, 0.3f - (float)((int)i) * 0.02f);
        float k = 0.0f;
        int l = 0;
        VertexConsumer lv3 = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());
        MatrixStack.Entry lv4 = matrices.peek();
        while (i > 0.0f) {
            Sprite lv5 = l % 2 == 0 ? lv : lv2;
            float m = lv5.getMinU();
            float n = lv5.getMinV();
            float o = lv5.getMaxU();
            float p = lv5.getMaxV();
            if (l / 2 % 2 == 0) {
                float q = o;
                o = m;
                m = q;
            }
            EntityRenderDispatcher.drawFireVertex(lv4, lv3, -g - 0.0f, 0.0f - j, k, o, p);
            EntityRenderDispatcher.drawFireVertex(lv4, lv3, g - 0.0f, 0.0f - j, k, m, p);
            EntityRenderDispatcher.drawFireVertex(lv4, lv3, g - 0.0f, 1.4f - j, k, m, n);
            EntityRenderDispatcher.drawFireVertex(lv4, lv3, -g - 0.0f, 1.4f - j, k, o, n);
            i -= 0.45f;
            j -= 0.45f;
            g *= 0.9f;
            k -= 0.03f;
            ++l;
        }
        matrices.pop();
    }

    private static void drawFireVertex(MatrixStack.Entry entry, VertexConsumer vertices, float x, float y, float z, float u, float v) {
        vertices.vertex(entry, x, y, z).color(Colors.WHITE).texture(u, v).overlay(0, 10).light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE).normal(entry, 0.0f, 1.0f, 0.0f);
    }

    private static void renderShadow(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity, float opacity, float tickDelta, WorldView world, float radius) {
        double d = MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX());
        double e = MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY());
        double i = MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ());
        float j = Math.min(opacity / 0.5f, radius);
        int k = MathHelper.floor(d - (double)radius);
        int l = MathHelper.floor(d + (double)radius);
        int m = MathHelper.floor(e - (double)j);
        int n = MathHelper.floor(e);
        int o = MathHelper.floor(i - (double)radius);
        int p = MathHelper.floor(i + (double)radius);
        MatrixStack.Entry lv = matrices.peek();
        VertexConsumer lv2 = vertexConsumers.getBuffer(SHADOW_LAYER);
        BlockPos.Mutable lv3 = new BlockPos.Mutable();
        for (int q = o; q <= p; ++q) {
            for (int r = k; r <= l; ++r) {
                lv3.set(r, 0, q);
                Chunk lv4 = world.getChunk(lv3);
                for (int s = m; s <= n; ++s) {
                    lv3.setY(s);
                    float t = opacity - (float)(e - (double)lv3.getY()) * 0.5f;
                    EntityRenderDispatcher.renderShadowPart(lv, lv2, lv4, world, lv3, d, e, i, radius, t);
                }
            }
        }
    }

    private static void renderShadowPart(MatrixStack.Entry entry, VertexConsumer vertices, Chunk chunk, WorldView world, BlockPos pos, double x, double y, double z, float radius, float opacity) {
        BlockPos lv = pos.down();
        BlockState lv2 = chunk.getBlockState(lv);
        if (lv2.getRenderType() == BlockRenderType.INVISIBLE || world.getLightLevel(pos) <= 3) {
            return;
        }
        if (!lv2.isFullCube(chunk, lv)) {
            return;
        }
        VoxelShape lv3 = lv2.getOutlineShape(chunk, lv);
        if (lv3.isEmpty()) {
            return;
        }
        float i = LightmapTextureManager.getBrightness(world.getDimension(), world.getLightLevel(pos));
        float j = opacity * 0.5f * i;
        if (j >= 0.0f) {
            if (j > 1.0f) {
                j = 1.0f;
            }
            int k = ColorHelper.Argb.getArgb(MathHelper.floor(j * 255.0f), 255, 255, 255);
            Box lv4 = lv3.getBoundingBox();
            double l = (double)pos.getX() + lv4.minX;
            double m = (double)pos.getX() + lv4.maxX;
            double n = (double)pos.getY() + lv4.minY;
            double o = (double)pos.getZ() + lv4.minZ;
            double p = (double)pos.getZ() + lv4.maxZ;
            float q = (float)(l - x);
            float r = (float)(m - x);
            float s = (float)(n - y);
            float t = (float)(o - z);
            float u = (float)(p - z);
            float v = -q / 2.0f / radius + 0.5f;
            float w = -r / 2.0f / radius + 0.5f;
            float x2 = -t / 2.0f / radius + 0.5f;
            float y2 = -u / 2.0f / radius + 0.5f;
            EntityRenderDispatcher.drawShadowVertex(entry, vertices, k, q, s, t, v, x2);
            EntityRenderDispatcher.drawShadowVertex(entry, vertices, k, q, s, u, v, y2);
            EntityRenderDispatcher.drawShadowVertex(entry, vertices, k, r, s, u, w, y2);
            EntityRenderDispatcher.drawShadowVertex(entry, vertices, k, r, s, t, w, x2);
        }
    }

    private static void drawShadowVertex(MatrixStack.Entry entry, VertexConsumer vertices, int i, float x, float y, float z, float u, float v) {
        Vector3f vector3f = entry.getPositionMatrix().transformPosition(x, y, z, new Vector3f());
        vertices.vertex(vector3f.x(), vector3f.y(), vector3f.z(), i, u, v, OverlayTexture.DEFAULT_UV, 0xF000F0, 0.0f, 1.0f, 0.0f);
    }

    public void setWorld(@Nullable World world) {
        this.world = world;
        if (world == null) {
            this.camera = null;
        }
    }

    public double getSquaredDistanceToCamera(Entity entity) {
        return this.camera.getPos().squaredDistanceTo(entity.getPos());
    }

    public double getSquaredDistanceToCamera(double x, double y, double z) {
        return this.camera.getPos().squaredDistanceTo(x, y, z);
    }

    public Quaternionf getRotation() {
        return this.rotation;
    }

    public HeldItemRenderer getHeldItemRenderer() {
        return this.heldItemRenderer;
    }

    @Override
    public void reload(ResourceManager manager) {
        EntityRendererFactory.Context lv = new EntityRendererFactory.Context(this, this.itemRenderer, this.blockRenderManager, this.heldItemRenderer, manager, this.modelLoader, this.textRenderer);
        this.renderers = EntityRenderers.reloadEntityRenderers(lv);
        this.modelRenderers = EntityRenderers.reloadPlayerRenderers(lv);
    }
}

