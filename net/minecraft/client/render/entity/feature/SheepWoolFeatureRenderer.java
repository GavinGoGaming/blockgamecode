/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.SheepEntityModel;
import net.minecraft.client.render.entity.model.SheepWoolEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

@Environment(value=EnvType.CLIENT)
public class SheepWoolFeatureRenderer
extends FeatureRenderer<SheepEntity, SheepEntityModel<SheepEntity>> {
    private static final Identifier SKIN = Identifier.ofVanilla("textures/entity/sheep/sheep_fur.png");
    private final SheepWoolEntityModel<SheepEntity> model;

    public SheepWoolFeatureRenderer(FeatureRendererContext<SheepEntity, SheepEntityModel<SheepEntity>> context, EntityModelLoader loader) {
        super(context);
        this.model = new SheepWoolEntityModel(loader.getModelPart(EntityModelLayers.SHEEP_FUR));
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, SheepEntity arg3, float f, float g, float h, float j, float k, float l) {
        int u;
        if (arg3.isSheared()) {
            return;
        }
        if (arg3.isInvisible()) {
            MinecraftClient lv = MinecraftClient.getInstance();
            boolean bl = lv.hasOutline(arg3);
            if (bl) {
                ((SheepEntityModel)this.getContextModel()).copyStateTo(this.model);
                this.model.animateModel(arg3, f, g, h);
                this.model.setAngles(arg3, f, g, j, k, l);
                VertexConsumer lv2 = arg2.getBuffer(RenderLayer.getOutline(SKIN));
                this.model.render(arg, lv2, i, LivingEntityRenderer.getOverlay(arg3, 0.0f), -16777216);
            }
            return;
        }
        if (arg3.hasCustomName() && "jeb_".equals(arg3.getName().getString())) {
            int m = 25;
            int n = arg3.age / 25 + arg3.getId();
            int o = DyeColor.values().length;
            int p = n % o;
            int q = (n + 1) % o;
            float r = ((float)(arg3.age % 25) + h) / 25.0f;
            int s = SheepEntity.getRgbColor(DyeColor.byId(p));
            int t = SheepEntity.getRgbColor(DyeColor.byId(q));
            u = ColorHelper.Argb.lerp(r, s, t);
        } else {
            u = SheepEntity.getRgbColor(arg3.getColor());
        }
        SheepWoolFeatureRenderer.render(this.getContextModel(), this.model, SKIN, arg, arg2, i, arg3, f, g, j, k, l, h, u);
    }
}

