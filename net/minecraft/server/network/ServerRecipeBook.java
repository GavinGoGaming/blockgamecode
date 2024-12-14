/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.ChangeUnlockedRecipesS2CPacket;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.recipe.book.RecipeBookOptions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import org.slf4j.Logger;

public class ServerRecipeBook
extends RecipeBook {
    public static final String RECIPE_BOOK_KEY = "recipeBook";
    private static final Logger LOGGER = LogUtils.getLogger();

    public int unlockRecipes(Collection<RecipeEntry<?>> recipes, ServerPlayerEntity player) {
        ArrayList<Identifier> list = Lists.newArrayList();
        int i = 0;
        for (RecipeEntry<?> lv : recipes) {
            Identifier lv2 = lv.id();
            if (this.recipes.contains(lv2) || lv.value().isIgnoredInRecipeBook()) continue;
            this.add(lv2);
            this.display(lv2);
            list.add(lv2);
            Criteria.RECIPE_UNLOCKED.trigger(player, lv);
            ++i;
        }
        if (list.size() > 0) {
            this.sendUnlockRecipesPacket(ChangeUnlockedRecipesS2CPacket.Action.ADD, player, list);
        }
        return i;
    }

    public int lockRecipes(Collection<RecipeEntry<?>> recipes, ServerPlayerEntity player) {
        ArrayList<Identifier> list = Lists.newArrayList();
        int i = 0;
        for (RecipeEntry<?> lv : recipes) {
            Identifier lv2 = lv.id();
            if (!this.recipes.contains(lv2)) continue;
            this.remove(lv2);
            list.add(lv2);
            ++i;
        }
        this.sendUnlockRecipesPacket(ChangeUnlockedRecipesS2CPacket.Action.REMOVE, player, list);
        return i;
    }

    private void sendUnlockRecipesPacket(ChangeUnlockedRecipesS2CPacket.Action action, ServerPlayerEntity player, List<Identifier> recipeIds) {
        player.networkHandler.sendPacket(new ChangeUnlockedRecipesS2CPacket(action, recipeIds, Collections.emptyList(), this.getOptions()));
    }

    public NbtCompound toNbt() {
        NbtCompound lv = new NbtCompound();
        this.getOptions().writeNbt(lv);
        NbtList lv2 = new NbtList();
        for (Identifier lv3 : this.recipes) {
            lv2.add(NbtString.of(lv3.toString()));
        }
        lv.put("recipes", lv2);
        NbtList lv4 = new NbtList();
        for (Identifier lv5 : this.toBeDisplayed) {
            lv4.add(NbtString.of(lv5.toString()));
        }
        lv.put("toBeDisplayed", lv4);
        return lv;
    }

    public void readNbt(NbtCompound nbt, RecipeManager recipeManager) {
        this.setOptions(RecipeBookOptions.fromNbt(nbt));
        NbtList lv = nbt.getList("recipes", NbtElement.STRING_TYPE);
        this.handleList(lv, this::add, recipeManager);
        NbtList lv2 = nbt.getList("toBeDisplayed", NbtElement.STRING_TYPE);
        this.handleList(lv2, this::display, recipeManager);
    }

    private void handleList(NbtList list, Consumer<RecipeEntry<?>> handler, RecipeManager recipeManager) {
        for (int i = 0; i < list.size(); ++i) {
            String string = list.getString(i);
            try {
                Identifier lv = Identifier.of(string);
                Optional<RecipeEntry<?>> optional = recipeManager.get(lv);
                if (optional.isEmpty()) {
                    LOGGER.error("Tried to load unrecognized recipe: {} removed now.", (Object)lv);
                    continue;
                }
                handler.accept(optional.get());
                continue;
            } catch (InvalidIdentifierException lv2) {
                LOGGER.error("Tried to load improperly formatted recipe: {} removed now.", (Object)string);
            }
        }
    }

    public void sendInitRecipesPacket(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new ChangeUnlockedRecipesS2CPacket(ChangeUnlockedRecipesS2CPacket.Action.INIT, this.recipes, this.toBeDisplayed, this.getOptions()));
    }
}

