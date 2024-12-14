/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.recipebook;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;

@Environment(value=EnvType.CLIENT)
public interface RecipeBookProvider {
    public void refreshRecipeBook();

    public RecipeBookWidget getRecipeBookWidget();
}

