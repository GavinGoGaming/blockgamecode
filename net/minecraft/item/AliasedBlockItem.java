/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public class AliasedBlockItem
extends BlockItem {
    public AliasedBlockItem(Block arg, Item.Settings arg2) {
        super(arg, arg2);
    }

    @Override
    public String getTranslationKey() {
        return this.getOrCreateTranslationKey();
    }
}

