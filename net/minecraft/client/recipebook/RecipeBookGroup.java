/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.recipebook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeBookCategory;

@Environment(value=EnvType.CLIENT)
public enum RecipeBookGroup {
    CRAFTING_SEARCH(new ItemStack(Items.COMPASS)),
    CRAFTING_BUILDING_BLOCKS(new ItemStack(Blocks.BRICKS)),
    CRAFTING_REDSTONE(new ItemStack(Items.REDSTONE)),
    CRAFTING_EQUIPMENT(new ItemStack(Items.IRON_AXE), new ItemStack(Items.GOLDEN_SWORD)),
    CRAFTING_MISC(new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.APPLE)),
    FURNACE_SEARCH(new ItemStack(Items.COMPASS)),
    FURNACE_FOOD(new ItemStack(Items.PORKCHOP)),
    FURNACE_BLOCKS(new ItemStack(Blocks.STONE)),
    FURNACE_MISC(new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.EMERALD)),
    BLAST_FURNACE_SEARCH(new ItemStack(Items.COMPASS)),
    BLAST_FURNACE_BLOCKS(new ItemStack(Blocks.REDSTONE_ORE)),
    BLAST_FURNACE_MISC(new ItemStack(Items.IRON_SHOVEL), new ItemStack(Items.GOLDEN_LEGGINGS)),
    SMOKER_SEARCH(new ItemStack(Items.COMPASS)),
    SMOKER_FOOD(new ItemStack(Items.PORKCHOP)),
    STONECUTTER(new ItemStack(Items.CHISELED_STONE_BRICKS)),
    SMITHING(new ItemStack(Items.NETHERITE_CHESTPLATE)),
    CAMPFIRE(new ItemStack(Items.PORKCHOP)),
    UNKNOWN(new ItemStack(Items.BARRIER));

    public static final List<RecipeBookGroup> SMOKER;
    public static final List<RecipeBookGroup> BLAST_FURNACE;
    public static final List<RecipeBookGroup> FURNACE;
    public static final List<RecipeBookGroup> CRAFTING;
    public static final Map<RecipeBookGroup, List<RecipeBookGroup>> SEARCH_MAP;
    private final List<ItemStack> icons;

    private RecipeBookGroup(ItemStack ... entries) {
        this.icons = ImmutableList.copyOf(entries);
    }

    public static List<RecipeBookGroup> getGroups(RecipeBookCategory category) {
        return switch (category) {
            default -> throw new MatchException(null, null);
            case RecipeBookCategory.CRAFTING -> CRAFTING;
            case RecipeBookCategory.FURNACE -> FURNACE;
            case RecipeBookCategory.BLAST_FURNACE -> BLAST_FURNACE;
            case RecipeBookCategory.SMOKER -> SMOKER;
        };
    }

    public List<ItemStack> getIcons() {
        return this.icons;
    }

    static {
        SMOKER = ImmutableList.of(SMOKER_SEARCH, SMOKER_FOOD);
        BLAST_FURNACE = ImmutableList.of(BLAST_FURNACE_SEARCH, BLAST_FURNACE_BLOCKS, BLAST_FURNACE_MISC);
        FURNACE = ImmutableList.of(FURNACE_SEARCH, FURNACE_FOOD, FURNACE_BLOCKS, FURNACE_MISC);
        CRAFTING = ImmutableList.of(CRAFTING_SEARCH, CRAFTING_EQUIPMENT, CRAFTING_BUILDING_BLOCKS, CRAFTING_MISC, CRAFTING_REDSTONE);
        SEARCH_MAP = ImmutableMap.of(CRAFTING_SEARCH, ImmutableList.of(CRAFTING_EQUIPMENT, CRAFTING_BUILDING_BLOCKS, CRAFTING_MISC, CRAFTING_REDSTONE), FURNACE_SEARCH, ImmutableList.of(FURNACE_FOOD, FURNACE_BLOCKS, FURNACE_MISC), BLAST_FURNACE_SEARCH, ImmutableList.of(BLAST_FURNACE_BLOCKS, BLAST_FURNACE_MISC), SMOKER_SEARCH, ImmutableList.of(SMOKER_FOOD));
    }
}

