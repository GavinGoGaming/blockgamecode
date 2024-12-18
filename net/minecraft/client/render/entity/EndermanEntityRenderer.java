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
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.EndermanBlockFeatureRenderer;
import net.minecraft.client.render.entity.feature.EndermanEyesFeatureRenderer;
import net.minecraft.client.render.entity.model.EndermanEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class EndermanEntityRenderer
extends MobEntityRenderer<EndermanEntity, EndermanEntityModel<EndermanEntity>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/enderman/enderman.png");
    private final Random random = Random.create();

    public EndermanEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new EndermanEntityModel(arg.getPart(EntityModelLayers.ENDERMAN)), 0.5f);
        this.addFeature(new EndermanEyesFeatureRenderer<EndermanEntity>(this));
        this.addFeature(new EndermanBlockFeatureRenderer(this, arg.getBlockRenderManager()));
    }

    @Override
    public void render(EndermanEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        BlockState lv = arg.getCarriedBlock();
        EndermanEntityModel lv2 = (EndermanEntityModel)this.getModel();
        lv2.carryingBlock = lv != null;
        lv2.angry = arg.isAngry();
        super.render(arg, f, g, arg2, arg3, i);
    }

    @Override
    public Vec3d getPositionOffset(EndermanEntity arg, float f) {
        if (arg.isAngry()) {
            double d = 0.02 * (double)arg.getScale();
            return new Vec3d(this.random.nextGaussian() * d, 0.0, this.random.nextGaussian() * d);
        }
        return super.getPositionOffset(arg, f);
    }

    @Override
    public Identifier getTexture(EndermanEntity arg) {
        return TEXTURE;
    }
}

