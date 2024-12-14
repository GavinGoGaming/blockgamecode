/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.item.Item;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;

public class PickaxeItem
extends MiningToolItem {
    public PickaxeItem(ToolMaterial material, Item.Settings arg2) {
        super(material, BlockTags.PICKAXE_MINEABLE, arg2);
    }
}

