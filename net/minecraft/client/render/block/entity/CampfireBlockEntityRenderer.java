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
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class CampfireBlockEntityRenderer
implements BlockEntityRenderer<CampfireBlockEntity> {
    private static final float SCALE = 0.375f;
    private final ItemRenderer itemRenderer;

    public CampfireBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(CampfireBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
        Direction lv = arg.getCachedState().get(CampfireBlock.FACING);
        DefaultedList<ItemStack> lv2 = arg.getItemsBeingCooked();
        int k = (int)arg.getPos().asLong();
        for (int l = 0; l < lv2.size(); ++l) {
            ItemStack lv3 = lv2.get(l);
            if (lv3 == ItemStack.EMPTY) continue;
            arg2.push();
            arg2.translate(0.5f, 0.44921875f, 0.5f);
            Direction lv4 = Direction.fromHorizontal((l + lv.getHorizontal()) % 4);
            float g = -lv4.asRotation();
            arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(g));
            arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
            arg2.translate(-0.3125f, -0.3125f, 0.0f);
            arg2.scale(0.375f, 0.375f, 0.375f);
            this.itemRenderer.renderItem(lv3, ModelTransformationMode.FIXED, i, j, arg2, arg3, arg.getWorld(), k + l);
            arg2.pop();
        }
    }
}

