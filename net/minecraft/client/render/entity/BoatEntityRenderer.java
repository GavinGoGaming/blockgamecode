/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BoatEntityModel;
import net.minecraft.client.render.entity.model.ChestBoatEntityModel;
import net.minecraft.client.render.entity.model.ChestRaftEntityModel;
import net.minecraft.client.render.entity.model.CompositeEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ModelWithWaterPatch;
import net.minecraft.client.render.entity.model.RaftEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;

@Environment(value=EnvType.CLIENT)
public class BoatEntityRenderer
extends EntityRenderer<BoatEntity> {
    private final Map<BoatEntity.Type, Pair<Identifier, CompositeEntityModel<BoatEntity>>> texturesAndModels;

    public BoatEntityRenderer(EntityRendererFactory.Context ctx, boolean chest) {
        super(ctx);
        this.shadowRadius = 0.8f;
        this.texturesAndModels = Stream.of(BoatEntity.Type.values()).collect(ImmutableMap.toImmutableMap(type -> type, type -> Pair.of(BoatEntityRenderer.getTexture(type, chest), this.createModel(ctx, (BoatEntity.Type)type, chest))));
    }

    private CompositeEntityModel<BoatEntity> createModel(EntityRendererFactory.Context ctx, BoatEntity.Type type, boolean chest) {
        EntityModelLayer lv = chest ? EntityModelLayers.createChestBoat(type) : EntityModelLayers.createBoat(type);
        ModelPart lv2 = ctx.getPart(lv);
        if (type == BoatEntity.Type.BAMBOO) {
            return chest ? new ChestRaftEntityModel(lv2) : new RaftEntityModel(lv2);
        }
        return chest ? new ChestBoatEntityModel(lv2) : new BoatEntityModel(lv2);
    }

    private static Identifier getTexture(BoatEntity.Type type, boolean chest) {
        if (chest) {
            return Identifier.ofVanilla("textures/entity/chest_boat/" + type.getName() + ".png");
        }
        return Identifier.ofVanilla("textures/entity/boat/" + type.getName() + ".png");
    }

    @Override
    public void render(BoatEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        float k;
        arg2.push();
        arg2.translate(0.0f, 0.375f, 0.0f);
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - f));
        float h = (float)arg.getDamageWobbleTicks() - g;
        float j = arg.getDamageWobbleStrength() - g;
        if (j < 0.0f) {
            j = 0.0f;
        }
        if (h > 0.0f) {
            arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.sin(h) * h * j / 10.0f * (float)arg.getDamageWobbleSide()));
        }
        if (!MathHelper.approximatelyEquals(k = arg.interpolateBubbleWobble(g), 0.0f)) {
            arg2.multiply(new Quaternionf().setAngleAxis(arg.interpolateBubbleWobble(g) * ((float)Math.PI / 180), 1.0f, 0.0f, 1.0f));
        }
        Pair<Identifier, CompositeEntityModel<BoatEntity>> pair = this.texturesAndModels.get(arg.getVariant());
        Identifier lv = pair.getFirst();
        CompositeEntityModel<BoatEntity> lv2 = pair.getSecond();
        arg2.scale(-1.0f, -1.0f, 1.0f);
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0f));
        lv2.setAngles(arg, g, 0.0f, -0.1f, 0.0f, 0.0f);
        VertexConsumer lv3 = arg3.getBuffer(lv2.getLayer(lv));
        lv2.render(arg2, lv3, i, OverlayTexture.DEFAULT_UV);
        if (!arg.isSubmergedInWater()) {
            VertexConsumer lv4 = arg3.getBuffer(RenderLayer.getWaterMask());
            if (lv2 instanceof ModelWithWaterPatch) {
                ModelWithWaterPatch lv5 = (ModelWithWaterPatch)((Object)lv2);
                lv5.getWaterPatch().render(arg2, lv4, i, OverlayTexture.DEFAULT_UV);
            }
        }
        arg2.pop();
        super.render(arg, f, g, arg2, arg3, i);
    }

    @Override
    public Identifier getTexture(BoatEntity arg) {
        return this.texturesAndModels.get(arg.getVariant()).getFirst();
    }
}

