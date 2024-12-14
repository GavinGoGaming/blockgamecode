/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.HangingSignBlock;
import net.minecraft.block.WoodType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.model.Model;
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
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public class HangingSignBlockEntityRenderer
extends SignBlockEntityRenderer {
    private static final String PLANK = "plank";
    private static final String V_CHAINS = "vChains";
    private static final String NORMAL_CHAINS = "normalChains";
    private static final String CHAIN_L1 = "chainL1";
    private static final String CHAIN_L2 = "chainL2";
    private static final String CHAIN_R1 = "chainR1";
    private static final String CHAIN_R2 = "chainR2";
    private static final String BOARD = "board";
    private static final float MODEL_SCALE = 1.0f;
    private static final float TEXT_SCALE = 0.9f;
    private static final Vec3d TEXT_OFFSET = new Vec3d(0.0, -0.32f, 0.073f);
    private final Map<WoodType, HangingSignModel> MODELS = WoodType.stream().collect(ImmutableMap.toImmutableMap(arg -> arg, type -> new HangingSignModel(arg2.getLayerModelPart(EntityModelLayers.createHangingSign(type)))));

    public HangingSignBlockEntityRenderer(BlockEntityRendererFactory.Context arg2) {
        super(arg2);
    }

    @Override
    public float getSignScale() {
        return 1.0f;
    }

    @Override
    public float getTextScale() {
        return 0.9f;
    }

    @Override
    public void render(SignBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
        BlockState lv = arg.getCachedState();
        AbstractSignBlock lv2 = (AbstractSignBlock)lv.getBlock();
        WoodType lv3 = AbstractSignBlock.getWoodType(lv2);
        HangingSignModel lv4 = this.MODELS.get(lv3);
        lv4.updateVisibleParts(lv);
        this.render(arg, arg2, arg3, i, j, lv, lv2, lv3, lv4);
    }

    @Override
    void setAngles(MatrixStack matrices, float rotationDegrees, BlockState state) {
        matrices.translate(0.5, 0.9375, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationDegrees));
        matrices.translate(0.0f, -0.3125f, 0.0f);
    }

    @Override
    void renderSignModel(MatrixStack matrices, int light, int overlay, Model model, VertexConsumer vertexConsumers) {
        HangingSignModel lv = (HangingSignModel)model;
        lv.root.render(matrices, vertexConsumers, light, overlay);
    }

    @Override
    SpriteIdentifier getTextureId(WoodType signType) {
        return TexturedRenderLayers.getHangingSignTextureId(signType);
    }

    @Override
    Vec3d getTextOffset() {
        return TEXT_OFFSET;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(BOARD, ModelPartBuilder.create().uv(0, 12).cuboid(-7.0f, 0.0f, -1.0f, 14.0f, 10.0f, 2.0f), ModelTransform.NONE);
        lv2.addChild(PLANK, ModelPartBuilder.create().uv(0, 0).cuboid(-8.0f, -6.0f, -2.0f, 16.0f, 2.0f, 4.0f), ModelTransform.NONE);
        ModelPartData lv3 = lv2.addChild(NORMAL_CHAINS, ModelPartBuilder.create(), ModelTransform.NONE);
        lv3.addChild(CHAIN_L1, ModelPartBuilder.create().uv(0, 6).cuboid(-1.5f, 0.0f, 0.0f, 3.0f, 6.0f, 0.0f), ModelTransform.of(-5.0f, -6.0f, 0.0f, 0.0f, -0.7853982f, 0.0f));
        lv3.addChild(CHAIN_L2, ModelPartBuilder.create().uv(6, 6).cuboid(-1.5f, 0.0f, 0.0f, 3.0f, 6.0f, 0.0f), ModelTransform.of(-5.0f, -6.0f, 0.0f, 0.0f, 0.7853982f, 0.0f));
        lv3.addChild(CHAIN_R1, ModelPartBuilder.create().uv(0, 6).cuboid(-1.5f, 0.0f, 0.0f, 3.0f, 6.0f, 0.0f), ModelTransform.of(5.0f, -6.0f, 0.0f, 0.0f, -0.7853982f, 0.0f));
        lv3.addChild(CHAIN_R2, ModelPartBuilder.create().uv(6, 6).cuboid(-1.5f, 0.0f, 0.0f, 3.0f, 6.0f, 0.0f), ModelTransform.of(5.0f, -6.0f, 0.0f, 0.0f, 0.7853982f, 0.0f));
        lv2.addChild(V_CHAINS, ModelPartBuilder.create().uv(14, 6).cuboid(-6.0f, -6.0f, 0.0f, 12.0f, 6.0f, 0.0f), ModelTransform.NONE);
        return TexturedModelData.of(lv, 64, 32);
    }

    @Environment(value=EnvType.CLIENT)
    public static final class HangingSignModel
    extends Model {
        public final ModelPart root;
        public final ModelPart plank;
        public final ModelPart vChains;
        public final ModelPart normalChains;

        public HangingSignModel(ModelPart root) {
            super(RenderLayer::getEntityCutoutNoCull);
            this.root = root;
            this.plank = root.getChild(HangingSignBlockEntityRenderer.PLANK);
            this.normalChains = root.getChild(HangingSignBlockEntityRenderer.NORMAL_CHAINS);
            this.vChains = root.getChild(HangingSignBlockEntityRenderer.V_CHAINS);
        }

        public void updateVisibleParts(BlockState state) {
            boolean bl;
            this.plank.visible = bl = !(state.getBlock() instanceof HangingSignBlock);
            this.vChains.visible = false;
            this.normalChains.visible = true;
            if (!bl) {
                boolean bl2 = state.get(Properties.ATTACHED);
                this.normalChains.visible = !bl2;
                this.vChains.visible = bl2;
            }
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
            this.root.render(matrices, vertices, light, overlay, color);
        }
    }
}

