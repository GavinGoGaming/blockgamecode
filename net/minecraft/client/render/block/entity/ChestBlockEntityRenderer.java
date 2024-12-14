/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.Calendar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LidOpenable;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.LightmapCoordinatesRetriever;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

@Environment(value=EnvType.CLIENT)
public class ChestBlockEntityRenderer<T extends BlockEntity>
implements BlockEntityRenderer<T> {
    private static final String BASE = "bottom";
    private static final String LID = "lid";
    private static final String LATCH = "lock";
    private final ModelPart singleChestLid;
    private final ModelPart singleChestBase;
    private final ModelPart singleChestLatch;
    private final ModelPart doubleChestLeftLid;
    private final ModelPart doubleChestLeftBase;
    private final ModelPart doubleChestLeftLatch;
    private final ModelPart doubleChestRightLid;
    private final ModelPart doubleChestRightBase;
    private final ModelPart doubleChestRightLatch;
    private boolean christmas;

    public ChestBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26) {
            this.christmas = true;
        }
        ModelPart lv = ctx.getLayerModelPart(EntityModelLayers.CHEST);
        this.singleChestBase = lv.getChild(BASE);
        this.singleChestLid = lv.getChild(LID);
        this.singleChestLatch = lv.getChild(LATCH);
        ModelPart lv2 = ctx.getLayerModelPart(EntityModelLayers.DOUBLE_CHEST_LEFT);
        this.doubleChestLeftBase = lv2.getChild(BASE);
        this.doubleChestLeftLid = lv2.getChild(LID);
        this.doubleChestLeftLatch = lv2.getChild(LATCH);
        ModelPart lv3 = ctx.getLayerModelPart(EntityModelLayers.DOUBLE_CHEST_RIGHT);
        this.doubleChestRightBase = lv3.getChild(BASE);
        this.doubleChestRightLid = lv3.getChild(LID);
        this.doubleChestRightLatch = lv3.getChild(LATCH);
    }

    public static TexturedModelData getSingleTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(BASE, ModelPartBuilder.create().uv(0, 19).cuboid(1.0f, 0.0f, 1.0f, 14.0f, 10.0f, 14.0f), ModelTransform.NONE);
        lv2.addChild(LID, ModelPartBuilder.create().uv(0, 0).cuboid(1.0f, 0.0f, 0.0f, 14.0f, 5.0f, 14.0f), ModelTransform.pivot(0.0f, 9.0f, 1.0f));
        lv2.addChild(LATCH, ModelPartBuilder.create().uv(0, 0).cuboid(7.0f, -2.0f, 14.0f, 2.0f, 4.0f, 1.0f), ModelTransform.pivot(0.0f, 9.0f, 1.0f));
        return TexturedModelData.of(lv, 64, 64);
    }

    public static TexturedModelData getRightDoubleTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(BASE, ModelPartBuilder.create().uv(0, 19).cuboid(1.0f, 0.0f, 1.0f, 15.0f, 10.0f, 14.0f), ModelTransform.NONE);
        lv2.addChild(LID, ModelPartBuilder.create().uv(0, 0).cuboid(1.0f, 0.0f, 0.0f, 15.0f, 5.0f, 14.0f), ModelTransform.pivot(0.0f, 9.0f, 1.0f));
        lv2.addChild(LATCH, ModelPartBuilder.create().uv(0, 0).cuboid(15.0f, -2.0f, 14.0f, 1.0f, 4.0f, 1.0f), ModelTransform.pivot(0.0f, 9.0f, 1.0f));
        return TexturedModelData.of(lv, 64, 64);
    }

    public static TexturedModelData getLeftDoubleTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(BASE, ModelPartBuilder.create().uv(0, 19).cuboid(0.0f, 0.0f, 1.0f, 15.0f, 10.0f, 14.0f), ModelTransform.NONE);
        lv2.addChild(LID, ModelPartBuilder.create().uv(0, 0).cuboid(0.0f, 0.0f, 0.0f, 15.0f, 5.0f, 14.0f), ModelTransform.pivot(0.0f, 9.0f, 1.0f));
        lv2.addChild(LATCH, ModelPartBuilder.create().uv(0, 0).cuboid(0.0f, -2.0f, 14.0f, 1.0f, 4.0f, 1.0f), ModelTransform.pivot(0.0f, 9.0f, 1.0f));
        return TexturedModelData.of(lv, 64, 64);
    }

    @Override
    public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        World lv = ((BlockEntity)entity).getWorld();
        boolean bl = lv != null;
        BlockState lv2 = bl ? ((BlockEntity)entity).getCachedState() : (BlockState)Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.SOUTH);
        ChestType lv3 = lv2.contains(ChestBlock.CHEST_TYPE) ? lv2.get(ChestBlock.CHEST_TYPE) : ChestType.SINGLE;
        Block lv4 = lv2.getBlock();
        if (!(lv4 instanceof AbstractChestBlock)) {
            return;
        }
        AbstractChestBlock lv5 = (AbstractChestBlock)lv4;
        boolean bl2 = lv3 != ChestType.SINGLE;
        matrices.push();
        float g = lv2.get(ChestBlock.FACING).asRotation();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-g));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        DoubleBlockProperties.PropertySource<Object> lv6 = bl ? lv5.getBlockEntitySource(lv2, lv, ((BlockEntity)entity).getPos(), true) : DoubleBlockProperties.PropertyRetriever::getFallback;
        float h = lv6.apply(ChestBlock.getAnimationProgressRetriever((LidOpenable)entity)).get(tickDelta);
        h = 1.0f - h;
        h = 1.0f - h * h * h;
        int k = ((Int2IntFunction)lv6.apply(new LightmapCoordinatesRetriever())).applyAsInt(light);
        SpriteIdentifier lv7 = TexturedRenderLayers.getChestTextureId(entity, lv3, this.christmas);
        VertexConsumer lv8 = lv7.getVertexConsumer(vertexConsumers, RenderLayer::getEntityCutout);
        if (bl2) {
            if (lv3 == ChestType.LEFT) {
                this.render(matrices, lv8, this.doubleChestLeftLid, this.doubleChestLeftLatch, this.doubleChestLeftBase, h, k, overlay);
            } else {
                this.render(matrices, lv8, this.doubleChestRightLid, this.doubleChestRightLatch, this.doubleChestRightBase, h, k, overlay);
            }
        } else {
            this.render(matrices, lv8, this.singleChestLid, this.singleChestLatch, this.singleChestBase, h, k, overlay);
        }
        matrices.pop();
    }

    private void render(MatrixStack matrices, VertexConsumer vertices, ModelPart lid, ModelPart latch, ModelPart base, float openFactor, int light, int overlay) {
        latch.pitch = lid.pitch = -(openFactor * 1.5707964f);
        lid.render(matrices, vertices, light, overlay);
        latch.render(matrices, vertices, light, overlay);
        base.render(matrices, vertices, light, overlay);
    }
}

