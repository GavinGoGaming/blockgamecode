/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render.entity;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public abstract class LivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>>
extends EntityRenderer<T>
implements FeatureRendererContext<T, M> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final float field_32939 = 0.1f;
    protected M model;
    protected final List<FeatureRenderer<T, M>> features = Lists.newArrayList();

    public LivingEntityRenderer(EntityRendererFactory.Context ctx, M model, float shadowRadius) {
        super(ctx);
        this.model = model;
        this.shadowRadius = shadowRadius;
    }

    protected final boolean addFeature(FeatureRenderer<T, M> feature) {
        return this.features.add(feature);
    }

    @Override
    public M getModel() {
        return this.model;
    }

    @Override
    public void render(T arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        float n;
        Direction lv2;
        Entity entity;
        arg2.push();
        ((EntityModel)this.model).handSwingProgress = this.getHandSwingProgress(arg, g);
        ((EntityModel)this.model).riding = ((Entity)arg).hasVehicle();
        ((EntityModel)this.model).child = ((LivingEntity)arg).isBaby();
        float h = MathHelper.lerpAngleDegrees(g, ((LivingEntity)arg).prevBodyYaw, ((LivingEntity)arg).bodyYaw);
        float j = MathHelper.lerpAngleDegrees(g, ((LivingEntity)arg).prevHeadYaw, ((LivingEntity)arg).headYaw);
        float k = j - h;
        if (((Entity)arg).hasVehicle() && (entity = ((Entity)arg).getVehicle()) instanceof LivingEntity) {
            LivingEntity lv = (LivingEntity)entity;
            h = MathHelper.lerpAngleDegrees(g, lv.prevBodyYaw, lv.bodyYaw);
            k = j - h;
            float l = MathHelper.wrapDegrees(k);
            if (l < -85.0f) {
                l = -85.0f;
            }
            if (l >= 85.0f) {
                l = 85.0f;
            }
            h = j - l;
            if (l * l > 2500.0f) {
                h += l * 0.2f;
            }
            k = j - h;
        }
        float m = MathHelper.lerp(g, ((LivingEntity)arg).prevPitch, ((Entity)arg).getPitch());
        if (LivingEntityRenderer.shouldFlipUpsideDown(arg)) {
            m *= -1.0f;
            k *= -1.0f;
        }
        k = MathHelper.wrapDegrees(k);
        if (((Entity)arg).isInPose(EntityPose.SLEEPING) && (lv2 = ((LivingEntity)arg).getSleepingDirection()) != null) {
            n = ((Entity)arg).getEyeHeight(EntityPose.STANDING) - 0.1f;
            arg2.translate((float)(-lv2.getOffsetX()) * n, 0.0f, (float)(-lv2.getOffsetZ()) * n);
        }
        float l = ((LivingEntity)arg).getScale();
        arg2.scale(l, l, l);
        n = this.getAnimationProgress(arg, g);
        this.setupTransforms(arg, arg2, n, h, g, l);
        arg2.scale(-1.0f, -1.0f, 1.0f);
        this.scale(arg, arg2, g);
        arg2.translate(0.0f, -1.501f, 0.0f);
        float o = 0.0f;
        float p = 0.0f;
        if (!((Entity)arg).hasVehicle() && ((LivingEntity)arg).isAlive()) {
            o = ((LivingEntity)arg).limbAnimator.getSpeed(g);
            p = ((LivingEntity)arg).limbAnimator.getPos(g);
            if (((LivingEntity)arg).isBaby()) {
                p *= 3.0f;
            }
            if (o > 1.0f) {
                o = 1.0f;
            }
        }
        ((EntityModel)this.model).animateModel(arg, p, o, g);
        ((EntityModel)this.model).setAngles(arg, p, o, n, k, m);
        MinecraftClient lv3 = MinecraftClient.getInstance();
        boolean bl = this.isVisible(arg);
        boolean bl2 = !bl && !((Entity)arg).isInvisibleTo(lv3.player);
        boolean bl3 = lv3.hasOutline((Entity)arg);
        RenderLayer lv4 = this.getRenderLayer(arg, bl, bl2, bl3);
        if (lv4 != null) {
            VertexConsumer lv5 = arg3.getBuffer(lv4);
            int q = LivingEntityRenderer.getOverlay(arg, this.getAnimationCounter(arg, g));
            ((Model)this.model).render(arg2, lv5, i, q, bl2 ? 0x26FFFFFF : -1);
        }
        if (!((Entity)arg).isSpectator()) {
            for (FeatureRenderer<T, M> lv6 : this.features) {
                lv6.render(arg2, arg3, i, arg, p, o, g, n, k, m);
            }
        }
        arg2.pop();
        super.render(arg, f, g, arg2, arg3, i);
    }

    @Nullable
    protected RenderLayer getRenderLayer(T entity, boolean showBody, boolean translucent, boolean showOutline) {
        Identifier lv = this.getTexture(entity);
        if (translucent) {
            return RenderLayer.getItemEntityTranslucentCull(lv);
        }
        if (showBody) {
            return ((Model)this.model).getLayer(lv);
        }
        if (showOutline) {
            return RenderLayer.getOutline(lv);
        }
        return null;
    }

    public static int getOverlay(LivingEntity entity, float whiteOverlayProgress) {
        return OverlayTexture.packUv(OverlayTexture.getU(whiteOverlayProgress), OverlayTexture.getV(entity.hurtTime > 0 || entity.deathTime > 0));
    }

    protected boolean isVisible(T entity) {
        return !((Entity)entity).isInvisible();
    }

    private static float getYaw(Direction direction) {
        switch (direction) {
            case SOUTH: {
                return 90.0f;
            }
            case WEST: {
                return 0.0f;
            }
            case NORTH: {
                return 270.0f;
            }
            case EAST: {
                return 180.0f;
            }
        }
        return 0.0f;
    }

    protected boolean isShaking(T entity) {
        return ((Entity)entity).isFrozen();
    }

    protected void setupTransforms(T entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta, float scale) {
        if (this.isShaking(entity)) {
            bodyYaw += (float)(Math.cos((double)((LivingEntity)entity).age * 3.25) * Math.PI * (double)0.4f);
        }
        if (!((Entity)entity).isInPose(EntityPose.SLEEPING)) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - bodyYaw));
        }
        if (((LivingEntity)entity).deathTime > 0) {
            float j = ((float)((LivingEntity)entity).deathTime + tickDelta - 1.0f) / 20.0f * 1.6f;
            if ((j = MathHelper.sqrt(j)) > 1.0f) {
                j = 1.0f;
            }
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(j * this.getLyingAngle(entity)));
        } else if (((LivingEntity)entity).isUsingRiptide()) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f - ((Entity)entity).getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(((float)((LivingEntity)entity).age + tickDelta) * -75.0f));
        } else if (((Entity)entity).isInPose(EntityPose.SLEEPING)) {
            Direction lv = ((LivingEntity)entity).getSleepingDirection();
            float k = lv != null ? LivingEntityRenderer.getYaw(lv) : bodyYaw;
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(k));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.getLyingAngle(entity)));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270.0f));
        } else if (LivingEntityRenderer.shouldFlipUpsideDown(entity)) {
            matrices.translate(0.0f, (((Entity)entity).getHeight() + 0.1f) / scale, 0.0f);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
        }
    }

    protected float getHandSwingProgress(T entity, float tickDelta) {
        return ((LivingEntity)entity).getHandSwingProgress(tickDelta);
    }

    protected float getAnimationProgress(T entity, float tickDelta) {
        return (float)((LivingEntity)entity).age + tickDelta;
    }

    protected float getLyingAngle(T entity) {
        return 90.0f;
    }

    protected float getAnimationCounter(T entity, float tickDelta) {
        return 0.0f;
    }

    protected void scale(T entity, MatrixStack matrices, float amount) {
    }

    @Override
    protected boolean hasLabel(T arg) {
        boolean bl;
        float f;
        double d = this.dispatcher.getSquaredDistanceToCamera((Entity)arg);
        float f2 = f = ((Entity)arg).isSneaky() ? 32.0f : 64.0f;
        if (d >= (double)(f * f)) {
            return false;
        }
        MinecraftClient lv = MinecraftClient.getInstance();
        ClientPlayerEntity lv2 = lv.player;
        boolean bl2 = bl = !((Entity)arg).isInvisibleTo(lv2);
        if (arg != lv2) {
            Team lv3 = ((Entity)arg).getScoreboardTeam();
            Team lv4 = lv2.getScoreboardTeam();
            if (lv3 != null) {
                AbstractTeam.VisibilityRule lv5 = ((AbstractTeam)lv3).getNameTagVisibilityRule();
                switch (lv5) {
                    case ALWAYS: {
                        return bl;
                    }
                    case NEVER: {
                        return false;
                    }
                    case HIDE_FOR_OTHER_TEAMS: {
                        return lv4 == null ? bl : lv3.isEqual(lv4) && (((AbstractTeam)lv3).shouldShowFriendlyInvisibles() || bl);
                    }
                    case HIDE_FOR_OWN_TEAM: {
                        return lv4 == null ? bl : !lv3.isEqual(lv4) && bl;
                    }
                }
                return true;
            }
        }
        return MinecraftClient.isHudEnabled() && arg != lv.getCameraEntity() && bl && !((Entity)arg).hasPassengers();
    }

    public static boolean shouldFlipUpsideDown(LivingEntity entity) {
        String string;
        if ((entity instanceof PlayerEntity || entity.hasCustomName()) && ("Dinnerbone".equals(string = Formatting.strip(entity.getName().getString())) || "Grumm".equals(string))) {
            return !(entity instanceof PlayerEntity) || ((PlayerEntity)entity).isPartVisible(PlayerModelPart.CAPE);
        }
        return false;
    }

    @Override
    protected float getShadowRadius(T arg) {
        return super.getShadowRadius(arg) * ((LivingEntity)arg).getScale();
    }

    @Override
    protected /* synthetic */ float getShadowRadius(Entity entity) {
        return this.getShadowRadius((T)((LivingEntity)entity));
    }
}

