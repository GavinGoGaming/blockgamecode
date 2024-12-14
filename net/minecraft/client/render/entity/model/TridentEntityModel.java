/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class TridentEntityModel
extends Model {
    public static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/trident.png");
    private final ModelPart root;

    public TridentEntityModel(ModelPart root) {
        super(RenderLayer::getEntitySolid);
        this.root = root;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.addChild("pole", ModelPartBuilder.create().uv(0, 6).cuboid(-0.5f, 2.0f, -0.5f, 1.0f, 25.0f, 1.0f), ModelTransform.NONE);
        lv3.addChild("base", ModelPartBuilder.create().uv(4, 0).cuboid(-1.5f, 0.0f, -0.5f, 3.0f, 2.0f, 1.0f), ModelTransform.NONE);
        lv3.addChild("left_spike", ModelPartBuilder.create().uv(4, 3).cuboid(-2.5f, -3.0f, -0.5f, 1.0f, 4.0f, 1.0f), ModelTransform.NONE);
        lv3.addChild("middle_spike", ModelPartBuilder.create().uv(0, 0).cuboid(-0.5f, -4.0f, -0.5f, 1.0f, 4.0f, 1.0f), ModelTransform.NONE);
        lv3.addChild("right_spike", ModelPartBuilder.create().uv(4, 3).mirrored().cuboid(1.5f, -3.0f, -0.5f, 1.0f, 4.0f, 1.0f), ModelTransform.NONE);
        return TexturedModelData.of(lv, 32, 32);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        this.root.render(matrices, vertices, light, overlay, color);
    }
}

