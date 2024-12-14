/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render.block.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.WallSkullBlock;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.PiglinHeadEntityModel;
import net.minecraft.client.render.entity.model.SkullEntityModel;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationPropertyHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SkullBlockEntityRenderer
implements BlockEntityRenderer<SkullBlockEntity> {
    private final Map<SkullBlock.SkullType, SkullBlockEntityModel> MODELS;
    private static final Map<SkullBlock.SkullType, Identifier> TEXTURES = Util.make(Maps.newHashMap(), map -> {
        map.put(SkullBlock.Type.SKELETON, Identifier.ofVanilla("textures/entity/skeleton/skeleton.png"));
        map.put(SkullBlock.Type.WITHER_SKELETON, Identifier.ofVanilla("textures/entity/skeleton/wither_skeleton.png"));
        map.put(SkullBlock.Type.ZOMBIE, Identifier.ofVanilla("textures/entity/zombie/zombie.png"));
        map.put(SkullBlock.Type.CREEPER, Identifier.ofVanilla("textures/entity/creeper/creeper.png"));
        map.put(SkullBlock.Type.DRAGON, Identifier.ofVanilla("textures/entity/enderdragon/dragon.png"));
        map.put(SkullBlock.Type.PIGLIN, Identifier.ofVanilla("textures/entity/piglin/piglin.png"));
        map.put(SkullBlock.Type.PLAYER, DefaultSkinHelper.getTexture());
    });

    public static Map<SkullBlock.SkullType, SkullBlockEntityModel> getModels(EntityModelLoader modelLoader) {
        ImmutableMap.Builder<SkullBlock.Type, SkullBlockEntityModel> builder = ImmutableMap.builder();
        builder.put(SkullBlock.Type.SKELETON, new SkullEntityModel(modelLoader.getModelPart(EntityModelLayers.SKELETON_SKULL)));
        builder.put(SkullBlock.Type.WITHER_SKELETON, new SkullEntityModel(modelLoader.getModelPart(EntityModelLayers.WITHER_SKELETON_SKULL)));
        builder.put(SkullBlock.Type.PLAYER, new SkullEntityModel(modelLoader.getModelPart(EntityModelLayers.PLAYER_HEAD)));
        builder.put(SkullBlock.Type.ZOMBIE, new SkullEntityModel(modelLoader.getModelPart(EntityModelLayers.ZOMBIE_HEAD)));
        builder.put(SkullBlock.Type.CREEPER, new SkullEntityModel(modelLoader.getModelPart(EntityModelLayers.CREEPER_HEAD)));
        builder.put(SkullBlock.Type.DRAGON, new DragonHeadEntityModel(modelLoader.getModelPart(EntityModelLayers.DRAGON_SKULL)));
        builder.put(SkullBlock.Type.PIGLIN, new PiglinHeadEntityModel(modelLoader.getModelPart(EntityModelLayers.PIGLIN_HEAD)));
        return builder.build();
    }

    public SkullBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.MODELS = SkullBlockEntityRenderer.getModels(ctx.getLayerRenderDispatcher());
    }

    @Override
    public void render(SkullBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
        float g = arg.getPoweredTicks(f);
        BlockState lv = arg.getCachedState();
        boolean bl = lv.getBlock() instanceof WallSkullBlock;
        Direction lv2 = bl ? lv.get(WallSkullBlock.FACING) : null;
        int k = bl ? RotationPropertyHelper.fromDirection(lv2.getOpposite()) : lv.get(SkullBlock.ROTATION);
        float h = RotationPropertyHelper.toDegrees(k);
        SkullBlock.SkullType lv3 = ((AbstractSkullBlock)lv.getBlock()).getSkullType();
        SkullBlockEntityModel lv4 = this.MODELS.get(lv3);
        RenderLayer lv5 = SkullBlockEntityRenderer.getRenderLayer(lv3, arg.getOwner());
        SkullBlockEntityRenderer.renderSkull(lv2, h, g, arg2, arg3, i, lv4, lv5);
    }

    public static void renderSkull(@Nullable Direction direction, float yaw, float animationProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, SkullBlockEntityModel model, RenderLayer renderLayer) {
        matrices.push();
        if (direction == null) {
            matrices.translate(0.5f, 0.0f, 0.5f);
        } else {
            float h = 0.25f;
            matrices.translate(0.5f - (float)direction.getOffsetX() * 0.25f, 0.25f, 0.5f - (float)direction.getOffsetZ() * 0.25f);
        }
        matrices.scale(-1.0f, -1.0f, 1.0f);
        VertexConsumer lv = vertexConsumers.getBuffer(renderLayer);
        model.setHeadRotation(animationProgress, yaw, 0.0f);
        model.render(matrices, lv, light, OverlayTexture.DEFAULT_UV);
        matrices.pop();
    }

    public static RenderLayer getRenderLayer(SkullBlock.SkullType type, @Nullable ProfileComponent profile) {
        Identifier lv = TEXTURES.get(type);
        if (type != SkullBlock.Type.PLAYER || profile == null) {
            return RenderLayer.getEntityCutoutNoCullZOffset(lv);
        }
        PlayerSkinProvider lv2 = MinecraftClient.getInstance().getSkinProvider();
        return RenderLayer.getEntityTranslucent(lv2.getSkinTextures(profile.gameProfile()).texture());
    }
}

