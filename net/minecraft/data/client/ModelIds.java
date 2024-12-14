/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.client;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ModelIds {
    @Deprecated
    public static Identifier getMinecraftNamespacedBlock(String name) {
        return Identifier.ofVanilla("block/" + name);
    }

    public static Identifier getMinecraftNamespacedItem(String name) {
        return Identifier.ofVanilla("item/" + name);
    }

    public static Identifier getBlockSubModelId(Block block, String suffix) {
        Identifier lv = Registries.BLOCK.getId(block);
        return lv.withPath(path -> "block/" + path + suffix);
    }

    public static Identifier getBlockModelId(Block block) {
        Identifier lv = Registries.BLOCK.getId(block);
        return lv.withPrefixedPath("block/");
    }

    public static Identifier getItemModelId(Item item) {
        Identifier lv = Registries.ITEM.getId(item);
        return lv.withPrefixedPath("item/");
    }

    public static Identifier getItemSubModelId(Item item, String suffix) {
        Identifier lv = Registries.ITEM.getId(item);
        return lv.withPath(path -> "item/" + path + suffix);
    }
}

