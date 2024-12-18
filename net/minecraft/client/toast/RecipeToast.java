/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.toast;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class RecipeToast
implements Toast {
    private static final Identifier TEXTURE = Identifier.ofVanilla("toast/recipe");
    private static final long DEFAULT_DURATION_MS = 5000L;
    private static final Text TITLE = Text.translatable("recipe.toast.title");
    private static final Text DESCRIPTION = Text.translatable("recipe.toast.description");
    private final List<RecipeEntry<?>> recipes = Lists.newArrayList();
    private long startTime;
    private boolean justUpdated;

    public RecipeToast(RecipeEntry<?> recipe) {
        this.recipes.add(recipe);
    }

    @Override
    public Toast.Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        if (this.justUpdated) {
            this.startTime = startTime;
            this.justUpdated = false;
        }
        if (this.recipes.isEmpty()) {
            return Toast.Visibility.HIDE;
        }
        context.drawGuiTexture(TEXTURE, 0, 0, this.getWidth(), this.getHeight());
        context.drawText(manager.getClient().textRenderer, TITLE, 30, 7, -11534256, false);
        context.drawText(manager.getClient().textRenderer, DESCRIPTION, 30, 18, Colors.BLACK, false);
        RecipeEntry<?> lv = this.recipes.get((int)((double)startTime / Math.max(1.0, 5000.0 * manager.getNotificationDisplayTimeMultiplier() / (double)this.recipes.size()) % (double)this.recipes.size()));
        ItemStack lv2 = lv.value().createIcon();
        context.getMatrices().push();
        context.getMatrices().scale(0.6f, 0.6f, 1.0f);
        context.drawItemWithoutEntity(lv2, 3, 3);
        context.getMatrices().pop();
        context.drawItemWithoutEntity(lv.value().getResult(manager.getClient().world.getRegistryManager()), 8, 8);
        return (double)(startTime - this.startTime) >= 5000.0 * manager.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    private void addRecipes(RecipeEntry<?> recipe) {
        this.recipes.add(recipe);
        this.justUpdated = true;
    }

    public static void show(ToastManager manager, RecipeEntry<?> recipe) {
        RecipeToast lv = manager.getToast(RecipeToast.class, TYPE);
        if (lv == null) {
            manager.add(new RecipeToast(recipe));
        } else {
            lv.addRecipes(recipe);
        }
    }
}

