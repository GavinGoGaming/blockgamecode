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
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.block.vault.VaultClientData;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

@Environment(value=EnvType.CLIENT)
public class VaultBlockEntityRenderer
implements BlockEntityRenderer<VaultBlockEntity> {
    private final ItemRenderer itemRenderer;
    private final Random random = Random.create();

    public VaultBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(VaultBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
        if (!VaultBlockEntity.Client.hasDisplayItem(arg.getSharedData())) {
            return;
        }
        World lv = arg.getWorld();
        if (lv == null) {
            return;
        }
        ItemStack lv2 = arg.getSharedData().getDisplayItem();
        if (lv2.isEmpty()) {
            return;
        }
        this.random.setSeed(ItemEntityRenderer.getSeed(lv2));
        VaultClientData lv3 = arg.getClientData();
        VaultBlockEntityRenderer.renderDisplayItem(f, lv, arg2, arg3, i, lv2, this.itemRenderer, lv3.getPreviousDisplayRotation(), lv3.getDisplayRotation(), this.random);
    }

    public static void renderDisplayItem(float tickDelta, World world, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ItemStack stack, ItemRenderer itemRenderer, float prevRotation, float rotation, Random random) {
        matrices.push();
        matrices.translate(0.5f, 0.4f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerpAngleDegrees(tickDelta, prevRotation, rotation)));
        ItemEntityRenderer.renderStack(itemRenderer, matrices, vertexConsumers, light, stack, random, world);
        matrices.pop();
    }
}

