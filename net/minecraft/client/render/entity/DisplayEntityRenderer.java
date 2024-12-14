/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render.entity;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@Environment(value=EnvType.CLIENT)
public abstract class DisplayEntityRenderer<T extends DisplayEntity, S>
extends EntityRenderer<T> {
    private final EntityRenderDispatcher renderDispatcher;

    protected DisplayEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
        this.renderDispatcher = arg.getRenderDispatcher();
    }

    @Override
    public Identifier getTexture(T arg) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }

    @Override
    public void render(T arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        DisplayEntity.RenderState lv = ((DisplayEntity)arg).getRenderState();
        if (lv == null) {
            return;
        }
        S object = this.getData(arg);
        if (object == null) {
            return;
        }
        float h = ((DisplayEntity)arg).getLerpProgress(g);
        this.shadowRadius = lv.shadowRadius().lerp(h);
        this.shadowOpacity = lv.shadowStrength().lerp(h);
        int j = lv.brightnessOverride();
        int k = j != -1 ? j : i;
        super.render(arg, f, g, arg2, arg3, k);
        arg2.push();
        arg2.multiply(this.getBillboardRotation(lv, arg, g, new Quaternionf()));
        AffineTransformation lv2 = lv.transformation().interpolate(h);
        arg2.multiplyPositionMatrix(lv2.getMatrix());
        this.render(arg, object, arg2, arg3, k, h);
        arg2.pop();
    }

    private Quaternionf getBillboardRotation(DisplayEntity.RenderState renderState, T entity, float yaw, Quaternionf rotation) {
        Camera lv = this.renderDispatcher.camera;
        return switch (renderState.billboardConstraints()) {
            default -> throw new MatchException(null, null);
            case DisplayEntity.BillboardMode.FIXED -> rotation.rotationYXZ((float)(-Math.PI) / 180 * DisplayEntityRenderer.lerpYaw(entity, yaw), (float)Math.PI / 180 * DisplayEntityRenderer.lerpPitch(entity, yaw), 0.0f);
            case DisplayEntity.BillboardMode.HORIZONTAL -> rotation.rotationYXZ((float)(-Math.PI) / 180 * DisplayEntityRenderer.lerpYaw(entity, yaw), (float)Math.PI / 180 * DisplayEntityRenderer.getNegatedPitch(lv), 0.0f);
            case DisplayEntity.BillboardMode.VERTICAL -> rotation.rotationYXZ((float)(-Math.PI) / 180 * DisplayEntityRenderer.getBackwardsYaw(lv), (float)Math.PI / 180 * DisplayEntityRenderer.lerpPitch(entity, yaw), 0.0f);
            case DisplayEntity.BillboardMode.CENTER -> rotation.rotationYXZ((float)(-Math.PI) / 180 * DisplayEntityRenderer.getBackwardsYaw(lv), (float)Math.PI / 180 * DisplayEntityRenderer.getNegatedPitch(lv), 0.0f);
        };
    }

    private static float getBackwardsYaw(Camera camera) {
        return camera.getYaw() - 180.0f;
    }

    private static float getNegatedPitch(Camera camera) {
        return -camera.getPitch();
    }

    private static <T extends DisplayEntity> float lerpYaw(T entity, float delta) {
        return MathHelper.lerpAngleDegrees(delta, entity.prevYaw, entity.getYaw());
    }

    private static <T extends DisplayEntity> float lerpPitch(T entity, float delta) {
        return MathHelper.lerp(delta, entity.prevPitch, entity.getPitch());
    }

    @Nullable
    protected abstract S getData(T var1);

    protected abstract void render(T var1, S var2, MatrixStack var3, VertexConsumerProvider var4, int var5, float var6);

    @Environment(value=EnvType.CLIENT)
    public static class TextDisplayEntityRenderer
    extends DisplayEntityRenderer<DisplayEntity.TextDisplayEntity, DisplayEntity.TextDisplayEntity.Data> {
        private final TextRenderer displayTextRenderer;

        protected TextDisplayEntityRenderer(EntityRendererFactory.Context arg) {
            super(arg);
            this.displayTextRenderer = arg.getTextRenderer();
        }

        private DisplayEntity.TextDisplayEntity.TextLines getLines(Text text, int width) {
            List<OrderedText> list = this.displayTextRenderer.wrapLines(text, width);
            ArrayList<DisplayEntity.TextDisplayEntity.TextLine> list2 = new ArrayList<DisplayEntity.TextDisplayEntity.TextLine>(list.size());
            int j = 0;
            for (OrderedText lv : list) {
                int k = this.displayTextRenderer.getWidth(lv);
                j = Math.max(j, k);
                list2.add(new DisplayEntity.TextDisplayEntity.TextLine(lv, k));
            }
            return new DisplayEntity.TextDisplayEntity.TextLines(list2, j);
        }

        @Override
        @Nullable
        protected DisplayEntity.TextDisplayEntity.Data getData(DisplayEntity.TextDisplayEntity arg) {
            return arg.getData();
        }

        @Override
        public void render(DisplayEntity.TextDisplayEntity arg, DisplayEntity.TextDisplayEntity.Data arg2, MatrixStack arg3, VertexConsumerProvider arg4, int i, float f) {
            int j;
            float g;
            byte b = arg2.flags();
            boolean bl = (b & DisplayEntity.TextDisplayEntity.SEE_THROUGH_FLAG) != 0;
            boolean bl2 = (b & DisplayEntity.TextDisplayEntity.DEFAULT_BACKGROUND_FLAG) != 0;
            boolean bl3 = (b & DisplayEntity.TextDisplayEntity.SHADOW_FLAG) != 0;
            DisplayEntity.TextDisplayEntity.TextAlignment lv = DisplayEntity.TextDisplayEntity.getAlignment(b);
            byte c = (byte)arg2.textOpacity().lerp(f);
            if (bl2) {
                g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25f);
                j = (int)(g * 255.0f) << 24;
            } else {
                j = arg2.backgroundColor().lerp(f);
            }
            g = 0.0f;
            Matrix4f matrix4f = arg3.peek().getPositionMatrix();
            matrix4f.rotate((float)Math.PI, 0.0f, 1.0f, 0.0f);
            matrix4f.scale(-0.025f, -0.025f, -0.025f);
            DisplayEntity.TextDisplayEntity.TextLines lv2 = arg.splitLines(this::getLines);
            int k = this.displayTextRenderer.fontHeight + 1;
            int l = lv2.width();
            int m = lv2.lines().size() * k;
            matrix4f.translate(1.0f - (float)l / 2.0f, -m, 0.0f);
            if (j != 0) {
                VertexConsumer lv3 = arg4.getBuffer(bl ? RenderLayer.getTextBackgroundSeeThrough() : RenderLayer.getTextBackground());
                lv3.vertex(matrix4f, -1.0f, -1.0f, 0.0f).color(j).light(i);
                lv3.vertex(matrix4f, -1.0f, (float)m, 0.0f).color(j).light(i);
                lv3.vertex(matrix4f, (float)l, (float)m, 0.0f).color(j).light(i);
                lv3.vertex(matrix4f, (float)l, -1.0f, 0.0f).color(j).light(i);
            }
            for (DisplayEntity.TextDisplayEntity.TextLine lv4 : lv2.lines()) {
                float h = switch (lv) {
                    default -> throw new MatchException(null, null);
                    case DisplayEntity.TextDisplayEntity.TextAlignment.LEFT -> 0.0f;
                    case DisplayEntity.TextDisplayEntity.TextAlignment.RIGHT -> l - lv4.width();
                    case DisplayEntity.TextDisplayEntity.TextAlignment.CENTER -> (float)l / 2.0f - (float)lv4.width() / 2.0f;
                };
                this.displayTextRenderer.draw(lv4.contents(), h, g, c << 24 | 0xFFFFFF, bl3, matrix4f, arg4, bl ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.POLYGON_OFFSET, 0, i);
                g += (float)k;
            }
        }

        @Override
        @Nullable
        protected /* synthetic */ Object getData(DisplayEntity entity) {
            return this.getData((DisplayEntity.TextDisplayEntity)entity);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class ItemDisplayEntityRenderer
    extends DisplayEntityRenderer<DisplayEntity.ItemDisplayEntity, DisplayEntity.ItemDisplayEntity.Data> {
        private final ItemRenderer itemRenderer;

        protected ItemDisplayEntityRenderer(EntityRendererFactory.Context arg) {
            super(arg);
            this.itemRenderer = arg.getItemRenderer();
        }

        @Override
        @Nullable
        protected DisplayEntity.ItemDisplayEntity.Data getData(DisplayEntity.ItemDisplayEntity arg) {
            return arg.getData();
        }

        @Override
        public void render(DisplayEntity.ItemDisplayEntity arg, DisplayEntity.ItemDisplayEntity.Data arg2, MatrixStack arg3, VertexConsumerProvider arg4, int i, float f) {
            arg3.multiply(RotationAxis.POSITIVE_Y.rotation((float)Math.PI));
            this.itemRenderer.renderItem(arg2.itemStack(), arg2.itemTransform(), i, OverlayTexture.DEFAULT_UV, arg3, arg4, arg.getWorld(), arg.getId());
        }

        @Override
        @Nullable
        protected /* synthetic */ Object getData(DisplayEntity entity) {
            return this.getData((DisplayEntity.ItemDisplayEntity)entity);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class BlockDisplayEntityRenderer
    extends DisplayEntityRenderer<DisplayEntity.BlockDisplayEntity, DisplayEntity.BlockDisplayEntity.Data> {
        private final BlockRenderManager blockRenderManager;

        protected BlockDisplayEntityRenderer(EntityRendererFactory.Context arg) {
            super(arg);
            this.blockRenderManager = arg.getBlockRenderManager();
        }

        @Override
        @Nullable
        protected DisplayEntity.BlockDisplayEntity.Data getData(DisplayEntity.BlockDisplayEntity arg) {
            return arg.getData();
        }

        @Override
        public void render(DisplayEntity.BlockDisplayEntity arg, DisplayEntity.BlockDisplayEntity.Data arg2, MatrixStack arg3, VertexConsumerProvider arg4, int i, float f) {
            this.blockRenderManager.renderBlockAsEntity(arg2.blockState(), arg3, arg4, i, OverlayTexture.DEFAULT_UV);
        }

        @Override
        @Nullable
        protected /* synthetic */ Object getData(DisplayEntity entity) {
            return this.getData((DisplayEntity.BlockDisplayEntity)entity);
        }
    }
}

