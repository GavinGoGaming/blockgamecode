/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.toast;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TutorialToast
implements Toast {
    private static final Identifier TEXTURE = Identifier.ofVanilla("toast/tutorial");
    public static final int PROGRESS_BAR_WIDTH = 154;
    public static final int PROGRESS_BAR_HEIGHT = 1;
    public static final int PROGRESS_BAR_X = 3;
    public static final int PROGRESS_BAR_Y = 28;
    private final Type type;
    private final Text title;
    @Nullable
    private final Text description;
    private Toast.Visibility visibility = Toast.Visibility.SHOW;
    private long lastTime;
    private float lastProgress;
    private float progress;
    private final boolean hasProgressBar;

    public TutorialToast(Type type, Text title, @Nullable Text description, boolean hasProgressBar) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.hasProgressBar = hasProgressBar;
    }

    @Override
    public Toast.Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        context.drawGuiTexture(TEXTURE, 0, 0, this.getWidth(), this.getHeight());
        this.type.drawIcon(context, 6, 6);
        if (this.description == null) {
            context.drawText(manager.getClient().textRenderer, this.title, 30, 12, -11534256, false);
        } else {
            context.drawText(manager.getClient().textRenderer, this.title, 30, 7, -11534256, false);
            context.drawText(manager.getClient().textRenderer, this.description, 30, 18, Colors.BLACK, false);
        }
        if (this.hasProgressBar) {
            context.fill(3, 28, 157, 29, Colors.WHITE);
            float f = MathHelper.clampedLerp(this.lastProgress, this.progress, (float)(startTime - this.lastTime) / 100.0f);
            int i = this.progress >= this.lastProgress ? -16755456 : -11206656;
            context.fill(3, 28, (int)(3.0f + 154.0f * f), 29, i);
            this.lastProgress = f;
            this.lastTime = startTime;
        }
        return this.visibility;
    }

    public void hide() {
        this.visibility = Toast.Visibility.HIDE;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Type {
        MOVEMENT_KEYS(Identifier.ofVanilla("toast/movement_keys")),
        MOUSE(Identifier.ofVanilla("toast/mouse")),
        TREE(Identifier.ofVanilla("toast/tree")),
        RECIPE_BOOK(Identifier.ofVanilla("toast/recipe_book")),
        WOODEN_PLANKS(Identifier.ofVanilla("toast/wooden_planks")),
        SOCIAL_INTERACTIONS(Identifier.ofVanilla("toast/social_interactions")),
        RIGHT_CLICK(Identifier.ofVanilla("toast/right_click"));

        private final Identifier texture;

        private Type(Identifier texture) {
            this.texture = texture;
        }

        public void drawIcon(DrawContext context, int x, int y) {
            RenderSystem.enableBlend();
            context.drawGuiTexture(this.texture, x, y, 20, 20);
        }
    }
}

