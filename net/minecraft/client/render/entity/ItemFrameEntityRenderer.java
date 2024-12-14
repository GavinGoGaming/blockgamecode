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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public class ItemFrameEntityRenderer<T extends ItemFrameEntity>
extends EntityRenderer<T> {
    private static final ModelIdentifier NORMAL_FRAME = ModelIdentifier.ofVanilla("item_frame", "map=false");
    private static final ModelIdentifier MAP_FRAME = ModelIdentifier.ofVanilla("item_frame", "map=true");
    private static final ModelIdentifier GLOW_FRAME = ModelIdentifier.ofVanilla("glow_item_frame", "map=false");
    private static final ModelIdentifier MAP_GLOW_FRAME = ModelIdentifier.ofVanilla("glow_item_frame", "map=true");
    public static final int GLOW_FRAME_BLOCK_LIGHT = 5;
    public static final int field_32933 = 30;
    private final ItemRenderer itemRenderer;
    private final BlockRenderManager blockRenderManager;

    public ItemFrameEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
        this.itemRenderer = arg.getItemRenderer();
        this.blockRenderManager = arg.getBlockRenderManager();
    }

    @Override
    protected int getBlockLight(T arg, BlockPos arg2) {
        if (((Entity)arg).getType() == EntityType.GLOW_ITEM_FRAME) {
            return Math.max(5, super.getBlockLight(arg, arg2));
        }
        return super.getBlockLight(arg, arg2);
    }

    @Override
    public void render(T arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        super.render(arg, f, g, arg2, arg3, i);
        arg2.push();
        Direction lv = ((AbstractDecorationEntity)arg).getHorizontalFacing();
        Vec3d lv2 = this.getPositionOffset(arg, g);
        arg2.translate(-lv2.getX(), -lv2.getY(), -lv2.getZ());
        double d = 0.46875;
        arg2.translate((double)lv.getOffsetX() * 0.46875, (double)lv.getOffsetY() * 0.46875, (double)lv.getOffsetZ() * 0.46875);
        arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(((Entity)arg).getPitch()));
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - ((Entity)arg).getYaw()));
        boolean bl = ((Entity)arg).isInvisible();
        ItemStack lv3 = ((ItemFrameEntity)arg).getHeldItemStack();
        if (!bl) {
            BakedModelManager lv4 = this.blockRenderManager.getModels().getModelManager();
            ModelIdentifier lv5 = this.getModelId(arg, lv3);
            arg2.push();
            arg2.translate(-0.5f, -0.5f, -0.5f);
            this.blockRenderManager.getModelRenderer().render(arg2.peek(), arg3.getBuffer(TexturedRenderLayers.getEntitySolid()), null, lv4.getModel(lv5), 1.0f, 1.0f, 1.0f, i, OverlayTexture.DEFAULT_UV);
            arg2.pop();
        }
        if (!lv3.isEmpty()) {
            MapIdComponent lv6 = ((ItemFrameEntity)arg).getMapId(lv3);
            if (bl) {
                arg2.translate(0.0f, 0.0f, 0.5f);
            } else {
                arg2.translate(0.0f, 0.0f, 0.4375f);
            }
            int j = lv6 != null ? ((ItemFrameEntity)arg).getRotation() % 4 * 2 : ((ItemFrameEntity)arg).getRotation();
            arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)j * 360.0f / 8.0f));
            if (lv6 != null) {
                arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
                float h = 0.0078125f;
                arg2.scale(0.0078125f, 0.0078125f, 0.0078125f);
                arg2.translate(-64.0f, -64.0f, 0.0f);
                MapState lv7 = FilledMapItem.getMapState(lv6, ((Entity)arg).getWorld());
                arg2.translate(0.0f, 0.0f, -1.0f);
                if (lv7 != null) {
                    int k = this.getLight(arg, LightmapTextureManager.MAX_SKY_LIGHT_COORDINATE | 0xD2, i);
                    MinecraftClient.getInstance().gameRenderer.getMapRenderer().draw(arg2, arg3, lv6, lv7, true, k);
                }
            } else {
                int l = this.getLight(arg, LightmapTextureManager.MAX_LIGHT_COORDINATE, i);
                arg2.scale(0.5f, 0.5f, 0.5f);
                this.itemRenderer.renderItem(lv3, ModelTransformationMode.FIXED, l, OverlayTexture.DEFAULT_UV, arg2, arg3, ((Entity)arg).getWorld(), ((Entity)arg).getId());
            }
        }
        arg2.pop();
    }

    private int getLight(T itemFrame, int glowLight, int regularLight) {
        return ((Entity)itemFrame).getType() == EntityType.GLOW_ITEM_FRAME ? glowLight : regularLight;
    }

    private ModelIdentifier getModelId(T entity, ItemStack stack) {
        boolean bl;
        boolean bl2 = bl = ((Entity)entity).getType() == EntityType.GLOW_ITEM_FRAME;
        if (stack.isOf(Items.FILLED_MAP)) {
            return bl ? MAP_GLOW_FRAME : MAP_FRAME;
        }
        return bl ? GLOW_FRAME : NORMAL_FRAME;
    }

    @Override
    public Vec3d getPositionOffset(T arg, float f) {
        return new Vec3d((float)((AbstractDecorationEntity)arg).getHorizontalFacing().getOffsetX() * 0.3f, -0.25, (float)((AbstractDecorationEntity)arg).getHorizontalFacing().getOffsetZ() * 0.3f);
    }

    @Override
    public Identifier getTexture(T arg) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }

    @Override
    protected boolean hasLabel(T arg) {
        if (!MinecraftClient.isHudEnabled() || ((ItemFrameEntity)arg).getHeldItemStack().isEmpty() || !((ItemFrameEntity)arg).getHeldItemStack().contains(DataComponentTypes.CUSTOM_NAME) || this.dispatcher.targetedEntity != arg) {
            return false;
        }
        double d = this.dispatcher.getSquaredDistanceToCamera((Entity)arg);
        float f = ((Entity)arg).isSneaky() ? 32.0f : 64.0f;
        return d < (double)(f * f);
    }

    @Override
    protected void renderLabelIfPresent(T arg, Text arg2, MatrixStack arg3, VertexConsumerProvider arg4, int i, float f) {
        super.renderLabelIfPresent(arg, ((ItemFrameEntity)arg).getHeldItemStack().getName(), arg3, arg4, i, f);
    }
}

