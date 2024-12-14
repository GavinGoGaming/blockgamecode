/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.feature;

import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LimbAnimator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class HeadFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>>
extends FeatureRenderer<T, M> {
    private final float scaleX;
    private final float scaleY;
    private final float scaleZ;
    private final Map<SkullBlock.SkullType, SkullBlockEntityModel> headModels;
    private final HeldItemRenderer heldItemRenderer;

    public HeadFeatureRenderer(FeatureRendererContext<T, M> context, EntityModelLoader loader, HeldItemRenderer heldItemRenderer) {
        this(context, loader, 1.0f, 1.0f, 1.0f, heldItemRenderer);
    }

    public HeadFeatureRenderer(FeatureRendererContext<T, M> context, EntityModelLoader loader, float scaleX, float scaleY, float scaleZ, HeldItemRenderer heldItemRenderer) {
        super(context);
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
        this.headModels = SkullBlockEntityRenderer.getModels(loader);
        this.heldItemRenderer = heldItemRenderer;
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, T arg3, float f, float g, float h, float j, float k, float l) {
        ArmorItem lv9;
        float n;
        boolean bl;
        ItemStack lv = ((LivingEntity)arg3).getEquippedStack(EquipmentSlot.HEAD);
        if (lv.isEmpty()) {
            return;
        }
        Item lv2 = lv.getItem();
        arg.push();
        arg.scale(this.scaleX, this.scaleY, this.scaleZ);
        boolean bl2 = bl = arg3 instanceof VillagerEntity || arg3 instanceof ZombieVillagerEntity;
        if (((LivingEntity)arg3).isBaby() && !(arg3 instanceof VillagerEntity)) {
            float m = 2.0f;
            n = 1.4f;
            arg.translate(0.0f, 0.03125f, 0.0f);
            arg.scale(0.7f, 0.7f, 0.7f);
            arg.translate(0.0f, 1.0f, 0.0f);
        }
        ((ModelWithHead)this.getContextModel()).getHead().rotate(arg);
        if (lv2 instanceof BlockItem && ((BlockItem)lv2).getBlock() instanceof AbstractSkullBlock) {
            LimbAnimator lv8;
            n = 1.1875f;
            arg.scale(1.1875f, -1.1875f, -1.1875f);
            if (bl) {
                arg.translate(0.0f, 0.0625f, 0.0f);
            }
            ProfileComponent lv3 = lv.get(DataComponentTypes.PROFILE);
            arg.translate(-0.5, 0.0, -0.5);
            SkullBlock.SkullType lv4 = ((AbstractSkullBlock)((BlockItem)lv2).getBlock()).getSkullType();
            SkullBlockEntityModel lv5 = this.headModels.get(lv4);
            RenderLayer lv6 = SkullBlockEntityRenderer.getRenderLayer(lv4, lv3);
            Entity entity = ((Entity)arg3).getVehicle();
            if (entity instanceof LivingEntity) {
                LivingEntity lv7 = (LivingEntity)entity;
                lv8 = lv7.limbAnimator;
            } else {
                lv8 = ((LivingEntity)arg3).limbAnimator;
            }
            float o = lv8.getPos(h);
            SkullBlockEntityRenderer.renderSkull(null, 180.0f, o, arg, arg2, i, lv5, lv6);
        } else if (!(lv2 instanceof ArmorItem) || (lv9 = (ArmorItem)lv2).getSlotType() != EquipmentSlot.HEAD) {
            HeadFeatureRenderer.translate(arg, bl);
            this.heldItemRenderer.renderItem((LivingEntity)arg3, lv, ModelTransformationMode.HEAD, false, arg, arg2, i);
        }
        arg.pop();
    }

    public static void translate(MatrixStack matrices, boolean villager) {
        float f = 0.625f;
        matrices.translate(0.0f, -0.25f, 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
        matrices.scale(0.625f, -0.625f, -0.625f);
        if (villager) {
            matrices.translate(0.0f, 0.1875f, 0.0f);
        }
    }
}

