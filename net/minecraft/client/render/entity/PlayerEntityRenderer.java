/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.feature.Deadmau5FeatureRenderer;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.PlayerHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.feature.ShoulderParrotFeatureRenderer;
import net.minecraft.client.render.entity.feature.StuckArrowsFeatureRenderer;
import net.minecraft.client.render.entity.feature.StuckStingersFeatureRenderer;
import net.minecraft.client.render.entity.feature.TridentRiptideFeatureRenderer;
import net.minecraft.client.render.entity.model.ArmorEntityModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public class PlayerEntityRenderer
extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public PlayerEntityRenderer(EntityRendererFactory.Context ctx, boolean slim) {
        super(ctx, new PlayerEntityModel(ctx.getPart(slim ? EntityModelLayers.PLAYER_SLIM : EntityModelLayers.PLAYER), slim), 0.5f);
        this.addFeature(new ArmorFeatureRenderer(this, new ArmorEntityModel(ctx.getPart(slim ? EntityModelLayers.PLAYER_SLIM_INNER_ARMOR : EntityModelLayers.PLAYER_INNER_ARMOR)), new ArmorEntityModel(ctx.getPart(slim ? EntityModelLayers.PLAYER_SLIM_OUTER_ARMOR : EntityModelLayers.PLAYER_OUTER_ARMOR)), ctx.getModelManager()));
        this.addFeature(new PlayerHeldItemFeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>(this, ctx.getHeldItemRenderer()));
        this.addFeature(new StuckArrowsFeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>(ctx, this));
        this.addFeature(new Deadmau5FeatureRenderer(this));
        this.addFeature(new CapeFeatureRenderer(this));
        this.addFeature(new HeadFeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>(this, ctx.getModelLoader(), ctx.getHeldItemRenderer()));
        this.addFeature(new ElytraFeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>(this, ctx.getModelLoader()));
        this.addFeature(new ShoulderParrotFeatureRenderer<AbstractClientPlayerEntity>(this, ctx.getModelLoader()));
        this.addFeature(new TridentRiptideFeatureRenderer<AbstractClientPlayerEntity>(this, ctx.getModelLoader()));
        this.addFeature(new StuckStingersFeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>(this));
    }

    @Override
    public void render(AbstractClientPlayerEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        this.setModelPose(arg);
        super.render(arg, f, g, arg2, arg3, i);
    }

    @Override
    public Vec3d getPositionOffset(AbstractClientPlayerEntity arg, float f) {
        if (arg.isInSneakingPose()) {
            return new Vec3d(0.0, (double)(arg.getScale() * -2.0f) / 16.0, 0.0);
        }
        return super.getPositionOffset(arg, f);
    }

    private void setModelPose(AbstractClientPlayerEntity player) {
        PlayerEntityModel lv = (PlayerEntityModel)this.getModel();
        if (player.isSpectator()) {
            lv.setVisible(false);
            lv.head.visible = true;
            lv.hat.visible = true;
        } else {
            lv.setVisible(true);
            lv.hat.visible = player.isPartVisible(PlayerModelPart.HAT);
            lv.jacket.visible = player.isPartVisible(PlayerModelPart.JACKET);
            lv.leftPants.visible = player.isPartVisible(PlayerModelPart.LEFT_PANTS_LEG);
            lv.rightPants.visible = player.isPartVisible(PlayerModelPart.RIGHT_PANTS_LEG);
            lv.leftSleeve.visible = player.isPartVisible(PlayerModelPart.LEFT_SLEEVE);
            lv.rightSleeve.visible = player.isPartVisible(PlayerModelPart.RIGHT_SLEEVE);
            lv.sneaking = player.isInSneakingPose();
            BipedEntityModel.ArmPose lv2 = PlayerEntityRenderer.getArmPose(player, Hand.MAIN_HAND);
            BipedEntityModel.ArmPose lv3 = PlayerEntityRenderer.getArmPose(player, Hand.OFF_HAND);
            if (lv2.isTwoHanded()) {
                BipedEntityModel.ArmPose armPose = lv3 = player.getOffHandStack().isEmpty() ? BipedEntityModel.ArmPose.EMPTY : BipedEntityModel.ArmPose.ITEM;
            }
            if (player.getMainArm() == Arm.RIGHT) {
                lv.rightArmPose = lv2;
                lv.leftArmPose = lv3;
            } else {
                lv.rightArmPose = lv3;
                lv.leftArmPose = lv2;
            }
        }
    }

    private static BipedEntityModel.ArmPose getArmPose(AbstractClientPlayerEntity player, Hand hand) {
        ItemStack lv = player.getStackInHand(hand);
        if (lv.isEmpty()) {
            return BipedEntityModel.ArmPose.EMPTY;
        }
        if (player.getActiveHand() == hand && player.getItemUseTimeLeft() > 0) {
            UseAction lv2 = lv.getUseAction();
            if (lv2 == UseAction.BLOCK) {
                return BipedEntityModel.ArmPose.BLOCK;
            }
            if (lv2 == UseAction.BOW) {
                return BipedEntityModel.ArmPose.BOW_AND_ARROW;
            }
            if (lv2 == UseAction.SPEAR) {
                return BipedEntityModel.ArmPose.THROW_SPEAR;
            }
            if (lv2 == UseAction.CROSSBOW && hand == player.getActiveHand()) {
                return BipedEntityModel.ArmPose.CROSSBOW_CHARGE;
            }
            if (lv2 == UseAction.SPYGLASS) {
                return BipedEntityModel.ArmPose.SPYGLASS;
            }
            if (lv2 == UseAction.TOOT_HORN) {
                return BipedEntityModel.ArmPose.TOOT_HORN;
            }
            if (lv2 == UseAction.BRUSH) {
                return BipedEntityModel.ArmPose.BRUSH;
            }
        } else if (!player.handSwinging && lv.isOf(Items.CROSSBOW) && CrossbowItem.isCharged(lv)) {
            return BipedEntityModel.ArmPose.CROSSBOW_HOLD;
        }
        return BipedEntityModel.ArmPose.ITEM;
    }

    @Override
    public Identifier getTexture(AbstractClientPlayerEntity arg) {
        return arg.getSkinTextures().texture();
    }

    @Override
    protected void scale(AbstractClientPlayerEntity arg, MatrixStack arg2, float f) {
        float g = 0.9375f;
        arg2.scale(0.9375f, 0.9375f, 0.9375f);
    }

    @Override
    protected void renderLabelIfPresent(AbstractClientPlayerEntity arg, Text arg2, MatrixStack arg3, VertexConsumerProvider arg4, int i, float f) {
        Scoreboard lv;
        ScoreboardObjective lv2;
        double d = this.dispatcher.getSquaredDistanceToCamera(arg);
        arg3.push();
        if (d < 100.0 && (lv2 = (lv = arg.getScoreboard()).getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME)) != null) {
            ReadableScoreboardScore lv3 = lv.getScore(arg, lv2);
            MutableText lv4 = ReadableScoreboardScore.getFormattedScore(lv3, lv2.getNumberFormatOr(StyledNumberFormat.EMPTY));
            super.renderLabelIfPresent(arg, Text.empty().append(lv4).append(ScreenTexts.SPACE).append(lv2.getDisplayName()), arg3, arg4, i, f);
            Objects.requireNonNull(this.getTextRenderer());
            arg3.translate(0.0f, 9.0f * 1.15f * 0.025f, 0.0f);
        }
        super.renderLabelIfPresent(arg, arg2, arg3, arg4, i, f);
        arg3.pop();
    }

    public void renderRightArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player) {
        this.renderArm(matrices, vertexConsumers, light, player, ((PlayerEntityModel)this.model).rightArm, ((PlayerEntityModel)this.model).rightSleeve);
    }

    public void renderLeftArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player) {
        this.renderArm(matrices, vertexConsumers, light, player, ((PlayerEntityModel)this.model).leftArm, ((PlayerEntityModel)this.model).leftSleeve);
    }

    private void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve) {
        PlayerEntityModel lv = (PlayerEntityModel)this.getModel();
        this.setModelPose(player);
        lv.handSwingProgress = 0.0f;
        lv.sneaking = false;
        lv.leaningPitch = 0.0f;
        lv.setAngles(player, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        arm.pitch = 0.0f;
        Identifier lv2 = player.getSkinTextures().texture();
        arm.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntitySolid(lv2)), light, OverlayTexture.DEFAULT_UV);
        sleeve.pitch = 0.0f;
        sleeve.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(lv2)), light, OverlayTexture.DEFAULT_UV);
    }

    @Override
    protected void setupTransforms(AbstractClientPlayerEntity arg, MatrixStack arg2, float f, float g, float h, float i) {
        float j = arg.getLeaningPitch(h);
        float k = arg.getPitch(h);
        if (arg.isFallFlying()) {
            super.setupTransforms(arg, arg2, f, g, h, i);
            float l = (float)arg.getFallFlyingTicks() + h;
            float m = MathHelper.clamp(l * l / 100.0f, 0.0f, 1.0f);
            if (!arg.isUsingRiptide()) {
                arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m * (-90.0f - k)));
            }
            Vec3d lv = arg.getRotationVec(h);
            Vec3d lv2 = arg.lerpVelocity(h);
            double d = lv2.horizontalLengthSquared();
            double e = lv.horizontalLengthSquared();
            if (d > 0.0 && e > 0.0) {
                double n = (lv2.x * lv.x + lv2.z * lv.z) / Math.sqrt(d * e);
                double o = lv2.x * lv.z - lv2.z * lv.x;
                arg2.multiply(RotationAxis.POSITIVE_Y.rotation((float)(Math.signum(o) * Math.acos(n))));
            }
        } else if (j > 0.0f) {
            super.setupTransforms(arg, arg2, f, g, h, i);
            float l = arg.isTouchingWater() ? -90.0f - k : -90.0f;
            float m = MathHelper.lerp(j, 0.0f, l);
            arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m));
            if (arg.isInSwimmingPose()) {
                arg2.translate(0.0f, -1.0f, 0.3f);
            }
        } else {
            super.setupTransforms(arg, arg2, f, g, h, i);
        }
    }
}

