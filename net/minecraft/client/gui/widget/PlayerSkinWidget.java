/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.widget;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class PlayerSkinWidget
extends ClickableWidget {
    private static final float field_45996 = 0.0625f;
    private static final float field_45997 = 2.125f;
    private static final float field_45998 = 100.0f;
    private static final float field_45999 = 2.5f;
    private static final float field_46000 = -5.0f;
    private static final float field_46001 = 30.0f;
    private static final float field_46002 = 50.0f;
    private final Models models;
    private final Supplier<SkinTextures> skinSupplier;
    private float xRotation = -5.0f;
    private float yRotation = 30.0f;

    public PlayerSkinWidget(int width, int height, EntityModelLoader entityModelLoader, Supplier<SkinTextures> skinSupplier) {
        super(0, 0, width, height, ScreenTexts.EMPTY);
        this.models = Models.create(entityModelLoader);
        this.skinSupplier = skinSupplier;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.getMatrices().push();
        context.getMatrices().translate((float)this.getX() + (float)this.getWidth() / 2.0f, this.getY() + this.getHeight(), 100.0f);
        float g = (float)this.getHeight() / 2.125f;
        context.getMatrices().scale(g, g, g);
        context.getMatrices().translate(0.0f, -0.0625f, 0.0f);
        context.getMatrices().multiply(RotationAxis.POSITIVE_X.rotationDegrees(this.xRotation), 0.0f, -1.0625f, 0.0f);
        context.getMatrices().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(this.yRotation));
        context.draw();
        DiffuseLighting.method_56819(RotationAxis.POSITIVE_X.rotationDegrees(this.xRotation));
        this.models.draw(context, this.skinSupplier.get());
        context.draw();
        DiffuseLighting.enableGuiDepthLighting();
        context.getMatrices().pop();
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        this.xRotation = MathHelper.clamp(this.xRotation - (float)deltaY * 2.5f, -50.0f, 50.0f);
        this.yRotation += (float)deltaX * 2.5f;
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    @Override
    public boolean isNarratable() {
        return false;
    }

    @Override
    @Nullable
    public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        return null;
    }

    @Environment(value=EnvType.CLIENT)
    record Models(PlayerEntityModel<?> wideModel, PlayerEntityModel<?> slimModel) {
        public static Models create(EntityModelLoader entityModelLoader) {
            PlayerEntityModel lv = new PlayerEntityModel(entityModelLoader.getModelPart(EntityModelLayers.PLAYER), false);
            PlayerEntityModel lv2 = new PlayerEntityModel(entityModelLoader.getModelPart(EntityModelLayers.PLAYER_SLIM), true);
            lv.child = false;
            lv2.child = false;
            return new Models(lv, lv2);
        }

        public void draw(DrawContext context, SkinTextures skinTextures) {
            context.getMatrices().push();
            context.getMatrices().scale(1.0f, 1.0f, -1.0f);
            context.getMatrices().translate(0.0f, -1.5f, 0.0f);
            PlayerEntityModel<?> lv = skinTextures.model() == SkinTextures.Model.SLIM ? this.slimModel : this.wideModel;
            RenderLayer lv2 = lv.getLayer(skinTextures.texture());
            lv.render(context.getMatrices(), context.getVertexConsumers().getBuffer(lv2), 0xF000F0, OverlayTexture.DEFAULT_UV);
            context.getMatrices().pop();
        }
    }
}

