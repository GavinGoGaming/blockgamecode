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
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class BookModel
extends Model {
    private static final String LEFT_PAGES = "left_pages";
    private static final String RIGHT_PAGES = "right_pages";
    private static final String FLIP_PAGE1 = "flip_page1";
    private static final String FLIP_PAGE2 = "flip_page2";
    private final ModelPart root;
    private final ModelPart leftCover;
    private final ModelPart rightCover;
    private final ModelPart leftPages;
    private final ModelPart rightPages;
    private final ModelPart leftFlippingPage;
    private final ModelPart rightFlippingPage;

    public BookModel(ModelPart root) {
        super(RenderLayer::getEntitySolid);
        this.root = root;
        this.leftCover = root.getChild(EntityModelPartNames.LEFT_LID);
        this.rightCover = root.getChild(EntityModelPartNames.RIGHT_LID);
        this.leftPages = root.getChild(LEFT_PAGES);
        this.rightPages = root.getChild(RIGHT_PAGES);
        this.leftFlippingPage = root.getChild(FLIP_PAGE1);
        this.rightFlippingPage = root.getChild(FLIP_PAGE2);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.LEFT_LID, ModelPartBuilder.create().uv(0, 0).cuboid(-6.0f, -5.0f, -0.005f, 6.0f, 10.0f, 0.005f), ModelTransform.pivot(0.0f, 0.0f, -1.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_LID, ModelPartBuilder.create().uv(16, 0).cuboid(0.0f, -5.0f, -0.005f, 6.0f, 10.0f, 0.005f), ModelTransform.pivot(0.0f, 0.0f, 1.0f));
        lv2.addChild("seam", ModelPartBuilder.create().uv(12, 0).cuboid(-1.0f, -5.0f, 0.0f, 2.0f, 10.0f, 0.005f), ModelTransform.rotation(0.0f, 1.5707964f, 0.0f));
        lv2.addChild(LEFT_PAGES, ModelPartBuilder.create().uv(0, 10).cuboid(0.0f, -4.0f, -0.99f, 5.0f, 8.0f, 1.0f), ModelTransform.NONE);
        lv2.addChild(RIGHT_PAGES, ModelPartBuilder.create().uv(12, 10).cuboid(0.0f, -4.0f, -0.01f, 5.0f, 8.0f, 1.0f), ModelTransform.NONE);
        ModelPartBuilder lv3 = ModelPartBuilder.create().uv(24, 10).cuboid(0.0f, -4.0f, 0.0f, 5.0f, 8.0f, 0.005f);
        lv2.addChild(FLIP_PAGE1, lv3, ModelTransform.NONE);
        lv2.addChild(FLIP_PAGE2, lv3, ModelTransform.NONE);
        return TexturedModelData.of(lv, 64, 32);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        this.renderBook(matrices, vertices, light, overlay, color);
    }

    public void renderBook(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int k) {
        this.root.render(matrices, vertices, light, overlay, k);
    }

    public void setPageAngles(float pageTurnAmount, float leftFlipAmount, float rightFlipAmount, float pageTurnSpeed) {
        float j = (MathHelper.sin(pageTurnAmount * 0.02f) * 0.1f + 1.25f) * pageTurnSpeed;
        this.leftCover.yaw = (float)Math.PI + j;
        this.rightCover.yaw = -j;
        this.leftPages.yaw = j;
        this.rightPages.yaw = -j;
        this.leftFlippingPage.yaw = j - j * 2.0f * leftFlipAmount;
        this.rightFlippingPage.yaw = j - j * 2.0f * rightFlipAmount;
        this.leftPages.pivotX = MathHelper.sin(j);
        this.rightPages.pivotX = MathHelper.sin(j);
        this.leftFlippingPage.pivotX = MathHelper.sin(j);
        this.rightFlippingPage.pivotX = MathHelper.sin(j);
    }
}

