/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.item;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.ShieldEntityModel;
import net.minecraft.client.render.entity.model.TridentEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

@Environment(value=EnvType.CLIENT)
public class BuiltinModelItemRenderer
implements SynchronousResourceReloader {
    private static final ShulkerBoxBlockEntity[] RENDER_SHULKER_BOX_DYED = (ShulkerBoxBlockEntity[])Arrays.stream(DyeColor.values()).sorted(Comparator.comparingInt(DyeColor::getId)).map(color -> new ShulkerBoxBlockEntity((DyeColor)color, BlockPos.ORIGIN, Blocks.SHULKER_BOX.getDefaultState())).toArray(ShulkerBoxBlockEntity[]::new);
    private static final ShulkerBoxBlockEntity RENDER_SHULKER_BOX = new ShulkerBoxBlockEntity(BlockPos.ORIGIN, Blocks.SHULKER_BOX.getDefaultState());
    private final ChestBlockEntity renderChestNormal = new ChestBlockEntity(BlockPos.ORIGIN, Blocks.CHEST.getDefaultState());
    private final ChestBlockEntity renderChestTrapped = new TrappedChestBlockEntity(BlockPos.ORIGIN, Blocks.TRAPPED_CHEST.getDefaultState());
    private final EnderChestBlockEntity renderChestEnder = new EnderChestBlockEntity(BlockPos.ORIGIN, Blocks.ENDER_CHEST.getDefaultState());
    private final BannerBlockEntity renderBanner = new BannerBlockEntity(BlockPos.ORIGIN, Blocks.WHITE_BANNER.getDefaultState());
    private final BedBlockEntity renderBed = new BedBlockEntity(BlockPos.ORIGIN, Blocks.RED_BED.getDefaultState());
    private final ConduitBlockEntity renderConduit = new ConduitBlockEntity(BlockPos.ORIGIN, Blocks.CONDUIT.getDefaultState());
    private final DecoratedPotBlockEntity renderDecoratedPot = new DecoratedPotBlockEntity(BlockPos.ORIGIN, Blocks.DECORATED_POT.getDefaultState());
    private ShieldEntityModel modelShield;
    private TridentEntityModel modelTrident;
    private Map<SkullBlock.SkullType, SkullBlockEntityModel> skullModels;
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private final EntityModelLoader entityModelLoader;

    public BuiltinModelItemRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelLoader entityModelLoader) {
        this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
        this.entityModelLoader = entityModelLoader;
    }

    @Override
    public void reload(ResourceManager manager) {
        this.modelShield = new ShieldEntityModel(this.entityModelLoader.getModelPart(EntityModelLayers.SHIELD));
        this.modelTrident = new TridentEntityModel(this.entityModelLoader.getModelPart(EntityModelLayers.TRIDENT));
        this.skullModels = SkullBlockEntityRenderer.getModels(this.entityModelLoader);
    }

    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        Item lv = stack.getItem();
        if (lv instanceof BlockItem) {
            BlockEntity lv8;
            Block lv2 = ((BlockItem)lv).getBlock();
            if (lv2 instanceof AbstractSkullBlock) {
                AbstractSkullBlock lv3 = (AbstractSkullBlock)lv2;
                ProfileComponent lv4 = stack.get(DataComponentTypes.PROFILE);
                if (lv4 != null && !lv4.isCompleted()) {
                    stack.remove(DataComponentTypes.PROFILE);
                    lv4.getFuture().thenAcceptAsync(profile -> stack.set(DataComponentTypes.PROFILE, profile), (Executor)MinecraftClient.getInstance());
                    lv4 = null;
                }
                SkullBlockEntityModel lv5 = this.skullModels.get(lv3.getSkullType());
                RenderLayer lv6 = SkullBlockEntityRenderer.getRenderLayer(lv3.getSkullType(), lv4);
                SkullBlockEntityRenderer.renderSkull(null, 180.0f, 0.0f, matrices, vertexConsumers, light, lv5, lv6);
                return;
            }
            BlockState lv7 = lv2.getDefaultState();
            if (lv2 instanceof AbstractBannerBlock) {
                this.renderBanner.readFrom(stack, ((AbstractBannerBlock)lv2).getColor());
                lv8 = this.renderBanner;
            } else if (lv2 instanceof BedBlock) {
                this.renderBed.setColor(((BedBlock)lv2).getColor());
                lv8 = this.renderBed;
            } else if (lv7.isOf(Blocks.CONDUIT)) {
                lv8 = this.renderConduit;
            } else if (lv7.isOf(Blocks.CHEST)) {
                lv8 = this.renderChestNormal;
            } else if (lv7.isOf(Blocks.ENDER_CHEST)) {
                lv8 = this.renderChestEnder;
            } else if (lv7.isOf(Blocks.TRAPPED_CHEST)) {
                lv8 = this.renderChestTrapped;
            } else if (lv7.isOf(Blocks.DECORATED_POT)) {
                this.renderDecoratedPot.readFrom(stack);
                lv8 = this.renderDecoratedPot;
            } else if (lv2 instanceof ShulkerBoxBlock) {
                DyeColor lv9 = ShulkerBoxBlock.getColor(lv);
                lv8 = lv9 == null ? RENDER_SHULKER_BOX : RENDER_SHULKER_BOX_DYED[lv9.getId()];
            } else {
                return;
            }
            this.blockEntityRenderDispatcher.renderEntity(lv8, matrices, vertexConsumers, light, overlay);
            return;
        }
        if (stack.isOf(Items.SHIELD)) {
            BannerPatternsComponent lv10 = stack.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT);
            DyeColor lv11 = stack.get(DataComponentTypes.BASE_COLOR);
            boolean bl = !lv10.layers().isEmpty() || lv11 != null;
            matrices.push();
            matrices.scale(1.0f, -1.0f, -1.0f);
            SpriteIdentifier lv12 = bl ? ModelLoader.SHIELD_BASE : ModelLoader.SHIELD_BASE_NO_PATTERN;
            VertexConsumer lv13 = lv12.getSprite().getTextureSpecificVertexConsumer(ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, this.modelShield.getLayer(lv12.getAtlasId()), true, stack.hasGlint()));
            this.modelShield.getHandle().render(matrices, lv13, light, overlay);
            if (bl) {
                BannerBlockEntityRenderer.renderCanvas(matrices, vertexConsumers, light, overlay, this.modelShield.getPlate(), lv12, false, Objects.requireNonNullElse(lv11, DyeColor.WHITE), lv10, stack.hasGlint());
            } else {
                this.modelShield.getPlate().render(matrices, lv13, light, overlay);
            }
            matrices.pop();
        } else if (stack.isOf(Items.TRIDENT)) {
            matrices.push();
            matrices.scale(1.0f, -1.0f, -1.0f);
            VertexConsumer lv14 = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, this.modelTrident.getLayer(TridentEntityModel.TEXTURE), false, stack.hasGlint());
            this.modelTrident.render(matrices, lv14, light, overlay);
            matrices.pop();
        }
    }
}

