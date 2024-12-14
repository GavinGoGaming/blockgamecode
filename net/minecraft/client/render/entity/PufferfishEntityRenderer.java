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
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LargePufferfishEntityModel;
import net.minecraft.client.render.entity.model.MediumPufferfishEntityModel;
import net.minecraft.client.render.entity.model.SmallPufferfishEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class PufferfishEntityRenderer
extends MobEntityRenderer<PufferfishEntity, EntityModel<PufferfishEntity>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/fish/pufferfish.png");
    private int modelSize = 3;
    private final EntityModel<PufferfishEntity> smallModel;
    private final EntityModel<PufferfishEntity> mediumModel;
    private final EntityModel<PufferfishEntity> largeModel = this.getModel();

    public PufferfishEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new LargePufferfishEntityModel(arg.getPart(EntityModelLayers.PUFFERFISH_BIG)), 0.2f);
        this.mediumModel = new MediumPufferfishEntityModel<PufferfishEntity>(arg.getPart(EntityModelLayers.PUFFERFISH_MEDIUM));
        this.smallModel = new SmallPufferfishEntityModel<PufferfishEntity>(arg.getPart(EntityModelLayers.PUFFERFISH_SMALL));
    }

    @Override
    public Identifier getTexture(PufferfishEntity arg) {
        return TEXTURE;
    }

    @Override
    public void render(PufferfishEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        int j = arg.getPuffState();
        if (j != this.modelSize) {
            this.model = j == 0 ? this.smallModel : (j == 1 ? this.mediumModel : this.largeModel);
        }
        this.modelSize = j;
        this.shadowRadius = 0.1f + 0.1f * (float)j;
        super.render(arg, f, g, arg2, arg3, i);
    }

    @Override
    protected void setupTransforms(PufferfishEntity arg, MatrixStack arg2, float f, float g, float h, float i) {
        arg2.translate(0.0f, MathHelper.cos(f * 0.05f) * 0.08f, 0.0f);
        super.setupTransforms(arg, arg2, f, g, h, i);
    }
}

