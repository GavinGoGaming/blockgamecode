/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe.book;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.util.Util;

public final class RecipeBookOptions {
    private static final Map<RecipeBookCategory, Pair<String, String>> CATEGORY_OPTION_NAMES = ImmutableMap.of(RecipeBookCategory.CRAFTING, Pair.of("isGuiOpen", "isFilteringCraftable"), RecipeBookCategory.FURNACE, Pair.of("isFurnaceGuiOpen", "isFurnaceFilteringCraftable"), RecipeBookCategory.BLAST_FURNACE, Pair.of("isBlastingFurnaceGuiOpen", "isBlastingFurnaceFilteringCraftable"), RecipeBookCategory.SMOKER, Pair.of("isSmokerGuiOpen", "isSmokerFilteringCraftable"));
    private final Map<RecipeBookCategory, CategoryOption> categoryOptions;

    private RecipeBookOptions(Map<RecipeBookCategory, CategoryOption> categoryOptions) {
        this.categoryOptions = categoryOptions;
    }

    public RecipeBookOptions() {
        this(Util.make(Maps.newEnumMap(RecipeBookCategory.class), categoryOptions -> {
            for (RecipeBookCategory lv : RecipeBookCategory.values()) {
                categoryOptions.put(lv, new CategoryOption(false, false));
            }
        }));
    }

    public boolean isGuiOpen(RecipeBookCategory category) {
        return this.categoryOptions.get((Object)((Object)category)).guiOpen;
    }

    public void setGuiOpen(RecipeBookCategory category, boolean open) {
        this.categoryOptions.get((Object)((Object)category)).guiOpen = open;
    }

    public boolean isFilteringCraftable(RecipeBookCategory category) {
        return this.categoryOptions.get((Object)((Object)category)).filteringCraftable;
    }

    public void setFilteringCraftable(RecipeBookCategory category, boolean filtering) {
        this.categoryOptions.get((Object)((Object)category)).filteringCraftable = filtering;
    }

    public static RecipeBookOptions fromPacket(PacketByteBuf buf) {
        EnumMap<RecipeBookCategory, CategoryOption> map = Maps.newEnumMap(RecipeBookCategory.class);
        for (RecipeBookCategory lv : RecipeBookCategory.values()) {
            boolean bl = buf.readBoolean();
            boolean bl2 = buf.readBoolean();
            map.put(lv, new CategoryOption(bl, bl2));
        }
        return new RecipeBookOptions(map);
    }

    public void toPacket(PacketByteBuf buf) {
        for (RecipeBookCategory lv : RecipeBookCategory.values()) {
            CategoryOption lv2 = this.categoryOptions.get((Object)lv);
            if (lv2 == null) {
                buf.writeBoolean(false);
                buf.writeBoolean(false);
                continue;
            }
            buf.writeBoolean(lv2.guiOpen);
            buf.writeBoolean(lv2.filteringCraftable);
        }
    }

    public static RecipeBookOptions fromNbt(NbtCompound nbt) {
        EnumMap<RecipeBookCategory, CategoryOption> map = Maps.newEnumMap(RecipeBookCategory.class);
        CATEGORY_OPTION_NAMES.forEach((category, pair) -> {
            boolean bl = nbt.getBoolean((String)pair.getFirst());
            boolean bl2 = nbt.getBoolean((String)pair.getSecond());
            map.put((RecipeBookCategory)((Object)category), new CategoryOption(bl, bl2));
        });
        return new RecipeBookOptions(map);
    }

    public void writeNbt(NbtCompound nbt) {
        CATEGORY_OPTION_NAMES.forEach((category, pair) -> {
            CategoryOption lv = this.categoryOptions.get(category);
            nbt.putBoolean((String)pair.getFirst(), lv.guiOpen);
            nbt.putBoolean((String)pair.getSecond(), lv.filteringCraftable);
        });
    }

    public RecipeBookOptions copy() {
        EnumMap<RecipeBookCategory, CategoryOption> map = Maps.newEnumMap(RecipeBookCategory.class);
        for (RecipeBookCategory lv : RecipeBookCategory.values()) {
            CategoryOption lv2 = this.categoryOptions.get((Object)lv);
            map.put(lv, lv2.copy());
        }
        return new RecipeBookOptions(map);
    }

    public void copyFrom(RecipeBookOptions other) {
        this.categoryOptions.clear();
        for (RecipeBookCategory lv : RecipeBookCategory.values()) {
            CategoryOption lv2 = other.categoryOptions.get((Object)lv);
            this.categoryOptions.put(lv, lv2.copy());
        }
    }

    public boolean equals(Object o) {
        return this == o || o instanceof RecipeBookOptions && this.categoryOptions.equals(((RecipeBookOptions)o).categoryOptions);
    }

    public int hashCode() {
        return this.categoryOptions.hashCode();
    }

    static final class CategoryOption {
        boolean guiOpen;
        boolean filteringCraftable;

        public CategoryOption(boolean guiOpen, boolean filteringCraftable) {
            this.guiOpen = guiOpen;
            this.filteringCraftable = filteringCraftable;
        }

        public CategoryOption copy() {
            return new CategoryOption(this.guiOpen, this.filteringCraftable);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof CategoryOption) {
                CategoryOption lv = (CategoryOption)o;
                return this.guiOpen == lv.guiOpen && this.filteringCraftable == lv.filteringCraftable;
            }
            return false;
        }

        public int hashCode() {
            int i = this.guiOpen ? 1 : 0;
            i = 31 * i + (this.filteringCraftable ? 1 : 0);
            return i;
        }

        public String toString() {
            return "[open=" + this.guiOpen + ", filtering=" + this.filteringCraftable + "]";
        }
    }
}

