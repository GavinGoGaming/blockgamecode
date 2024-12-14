/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.recipe;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;

public final class Ingredient
implements Predicate<ItemStack> {
    public static final Ingredient EMPTY = new Ingredient(Stream.empty());
    public static final PacketCodec<RegistryByteBuf, Ingredient> PACKET_CODEC = ItemStack.LIST_PACKET_CODEC.xmap(list -> Ingredient.ofEntries(list.stream().map(StackEntry::new)), arg -> Arrays.asList(arg.getMatchingStacks()));
    private final Entry[] entries;
    @Nullable
    private ItemStack[] matchingStacks;
    @Nullable
    private IntList ids;
    public static final Codec<Ingredient> ALLOW_EMPTY_CODEC = Ingredient.createCodec(true);
    public static final Codec<Ingredient> DISALLOW_EMPTY_CODEC = Ingredient.createCodec(false);

    private Ingredient(Stream<? extends Entry> entries) {
        this.entries = (Entry[])entries.toArray(Entry[]::new);
    }

    private Ingredient(Entry[] entries) {
        this.entries = entries;
    }

    public ItemStack[] getMatchingStacks() {
        if (this.matchingStacks == null) {
            this.matchingStacks = (ItemStack[])Arrays.stream(this.entries).flatMap(entry -> entry.getStacks().stream()).distinct().toArray(ItemStack[]::new);
        }
        return this.matchingStacks;
    }

    @Override
    public boolean test(@Nullable ItemStack arg) {
        if (arg == null) {
            return false;
        }
        if (this.isEmpty()) {
            return arg.isEmpty();
        }
        for (ItemStack lv : this.getMatchingStacks()) {
            if (!lv.isOf(arg.getItem())) continue;
            return true;
        }
        return false;
    }

    public IntList getMatchingItemIds() {
        if (this.ids == null) {
            ItemStack[] lvs = this.getMatchingStacks();
            this.ids = new IntArrayList(lvs.length);
            for (ItemStack lv : lvs) {
                this.ids.add(RecipeMatcher.getItemId(lv));
            }
            this.ids.sort(IntComparators.NATURAL_COMPARATOR);
        }
        return this.ids;
    }

    public boolean isEmpty() {
        return this.entries.length == 0;
    }

    public boolean equals(Object o) {
        if (o instanceof Ingredient) {
            Ingredient lv = (Ingredient)o;
            return Arrays.equals(this.entries, lv.entries);
        }
        return false;
    }

    private static Ingredient ofEntries(Stream<? extends Entry> entries) {
        Ingredient lv = new Ingredient(entries);
        return lv.isEmpty() ? EMPTY : lv;
    }

    public static Ingredient empty() {
        return EMPTY;
    }

    public static Ingredient ofItems(ItemConvertible ... items) {
        return Ingredient.ofStacks(Arrays.stream(items).map(ItemStack::new));
    }

    public static Ingredient ofStacks(ItemStack ... stacks) {
        return Ingredient.ofStacks(Arrays.stream(stacks));
    }

    public static Ingredient ofStacks(Stream<ItemStack> stacks) {
        return Ingredient.ofEntries(stacks.filter(stack -> !stack.isEmpty()).map(StackEntry::new));
    }

    public static Ingredient fromTag(TagKey<Item> tag) {
        return Ingredient.ofEntries(Stream.of(new TagEntry(tag)));
    }

    private static Codec<Ingredient> createCodec(boolean allowEmpty) {
        Codec<Entry[]> codec = Codec.list(Entry.CODEC).comapFlatMap(entries -> {
            if (!allowEmpty && entries.size() < 1) {
                return DataResult.error(() -> "Item array cannot be empty, at least one item must be defined");
            }
            return DataResult.success(entries.toArray(new Entry[0]));
        }, List::of);
        return Codec.either(codec, Entry.CODEC).flatComapMap(either -> either.map(Ingredient::new, entry -> new Ingredient(new Entry[]{entry})), ingredient -> {
            if (ingredient.entries.length == 1) {
                return DataResult.success(Either.right(ingredient.entries[0]));
            }
            if (ingredient.entries.length == 0 && !allowEmpty) {
                return DataResult.error(() -> "Item array cannot be empty, at least one item must be defined");
            }
            return DataResult.success(Either.left(ingredient.entries));
        });
    }

    @Override
    public /* synthetic */ boolean test(@Nullable Object stack) {
        return this.test((ItemStack)stack);
    }

    static interface Entry {
        public static final Codec<Entry> CODEC = Codec.xor(StackEntry.CODEC, TagEntry.CODEC).xmap(either -> (Entry)((Object)either.map(stackEntry -> stackEntry, tagEntry -> tagEntry)), entry -> {
            if (entry instanceof TagEntry) {
                TagEntry lv = (TagEntry)entry;
                return Either.right(lv);
            }
            if (entry instanceof StackEntry) {
                StackEntry lv2 = (StackEntry)entry;
                return Either.left(lv2);
            }
            throw new UnsupportedOperationException("This is neither an item value nor a tag value.");
        });

        public Collection<ItemStack> getStacks();
    }

    record TagEntry(TagKey<Item> tag) implements Entry
    {
        static final Codec<TagEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)TagKey.unprefixedCodec(RegistryKeys.ITEM).fieldOf("tag")).forGetter(entry -> entry.tag)).apply((Applicative<TagEntry, ?>)instance, TagEntry::new));

        @Override
        public boolean equals(Object o) {
            if (o instanceof TagEntry) {
                TagEntry lv = (TagEntry)o;
                return lv.tag.id().equals(this.tag.id());
            }
            return false;
        }

        @Override
        public Collection<ItemStack> getStacks() {
            ArrayList<ItemStack> list = Lists.newArrayList();
            for (RegistryEntry<Item> lv : Registries.ITEM.iterateEntries(this.tag)) {
                list.add(new ItemStack(lv));
            }
            return list;
        }
    }

    record StackEntry(ItemStack stack) implements Entry
    {
        static final Codec<StackEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ItemStack.REGISTRY_ENTRY_CODEC.fieldOf("item")).forGetter(entry -> entry.stack)).apply((Applicative<StackEntry, ?>)instance, StackEntry::new));

        @Override
        public boolean equals(Object o) {
            if (o instanceof StackEntry) {
                StackEntry lv = (StackEntry)o;
                return lv.stack.getItem().equals(this.stack.getItem()) && lv.stack.getCount() == this.stack.getCount();
            }
            return false;
        }

        @Override
        public Collection<ItemStack> getStacks() {
            return Collections.singleton(this.stack);
        }
    }
}

