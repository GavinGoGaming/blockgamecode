/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.EnchantingTableBlockEntityRenderer;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class LecternBlockEntityRenderer
implements BlockEntityRenderer<LecternBlockEntity> {
    private final BookModel book;

    public LecternBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.book = new BookModel(ctx.getLayerModelPart(EntityModelLayers.BOOK));
    }

    @Override
    public void render(LecternBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
        BlockState lv = arg.getCachedState();
        if (!lv.get(LecternBlock.HAS_BOOK).booleanValue()) {
            return;
        }
        arg2.push();
        arg2.translate(0.5f, 1.0625f, 0.5f);
        float g = lv.get(LecternBlock.FACING).rotateYClockwise().asRotation();
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-g));
        arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(67.5f));
        arg2.translate(0.0f, -0.125f, 0.0f);
        this.book.setPageAngles(0.0f, 0.1f, 0.9f, 1.2f);
        VertexConsumer lv2 = EnchantingTableBlockEntityRenderer.BOOK_TEXTURE.getVertexConsumer(arg3, RenderLayer::getEntitySolid);
        this.book.renderBook(arg2, lv2, i, j, -1);
        arg2.pop();
    }
}

