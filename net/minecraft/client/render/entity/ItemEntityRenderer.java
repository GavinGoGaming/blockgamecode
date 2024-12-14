/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import com.google.common.annotations.VisibleForTesting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

@Environment(value=EnvType.CLIENT)
public class ItemEntityRenderer
extends EntityRenderer<ItemEntity> {
    private static final float field_32924 = 0.15f;
    private static final float field_32929 = 0.0f;
    private static final float field_32930 = 0.0f;
    private static final float field_32931 = 0.09375f;
    private final ItemRenderer itemRenderer;
    private final Random random = Random.create();

    public ItemEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
        this.itemRenderer = arg.getItemRenderer();
        this.shadowRadius = 0.15f;
        this.shadowOpacity = 0.75f;
    }

    @Override
    public Identifier getTexture(ItemEntity arg) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }

    @Override
    public void render(ItemEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        arg2.push();
        ItemStack lv = arg.getStack();
        this.random.setSeed(ItemEntityRenderer.getSeed(lv));
        BakedModel lv2 = this.itemRenderer.getModel(lv, arg.getWorld(), null, arg.getId());
        boolean bl = lv2.hasDepth();
        float h = 0.25f;
        float j = MathHelper.sin(((float)arg.getItemAge() + g) / 10.0f + arg.uniqueOffset) * 0.1f + 0.1f;
        float k = lv2.getTransformation().getTransformation((ModelTransformationMode)ModelTransformationMode.GROUND).scale.y();
        arg2.translate(0.0f, j + 0.25f * k, 0.0f);
        float l = arg.getRotation(g);
        arg2.multiply(RotationAxis.POSITIVE_Y.rotation(l));
        ItemEntityRenderer.renderStack(this.itemRenderer, arg2, arg3, i, lv, lv2, bl, this.random);
        arg2.pop();
        super.render(arg, f, g, arg2, arg3, i);
    }

    public static int getSeed(ItemStack stack) {
        return stack.isEmpty() ? 187 : Item.getRawId(stack.getItem()) + stack.getDamage();
    }

    @VisibleForTesting
    static int getRenderedAmount(int stackSize) {
        if (stackSize <= 1) {
            return 1;
        }
        if (stackSize <= 16) {
            return 2;
        }
        if (stackSize <= 32) {
            return 3;
        }
        if (stackSize <= 48) {
            return 4;
        }
        return 5;
    }

    public static void renderStack(ItemRenderer itemRenderer, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ItemStack stack, Random random, World world) {
        BakedModel lv = itemRenderer.getModel(stack, world, null, 0);
        ItemEntityRenderer.renderStack(itemRenderer, matrices, vertexConsumers, light, stack, lv, lv.hasDepth(), random);
    }

    public static void renderStack(ItemRenderer itemRenderer, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ItemStack stack, BakedModel model, boolean depth, Random random) {
        float m;
        float l;
        int j = ItemEntityRenderer.getRenderedAmount(stack.getCount());
        float f = model.getTransformation().ground.scale.x();
        float g = model.getTransformation().ground.scale.y();
        float h = model.getTransformation().ground.scale.z();
        if (!depth) {
            float k = -0.0f * (float)(j - 1) * 0.5f * f;
            l = -0.0f * (float)(j - 1) * 0.5f * g;
            m = -0.09375f * (float)(j - 1) * 0.5f * h;
            matrices.translate(k, l, m);
        }
        for (int n = 0; n < j; ++n) {
            matrices.push();
            if (n > 0) {
                if (depth) {
                    l = (random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    m = (random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    float o = (random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    matrices.translate(l, m, o);
                } else {
                    l = (random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                    m = (random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                    matrices.translate(l, m, 0.0f);
                }
            }
            itemRenderer.renderItem(stack, ModelTransformationMode.GROUND, false, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV, model);
            matrices.pop();
            if (depth) continue;
            matrices.translate(0.0f * f, 0.0f * g, 0.09375f * h);
        }
    }
}

