/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.text.Text;

public class TippedArrowItem
extends ArrowItem {
    public TippedArrowItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack lv = super.getDefaultStack();
        lv.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.POISON));
        return lv;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        PotionContentsComponent lv = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (lv == null) {
            return;
        }
        lv.buildTooltip(tooltip::add, 0.125f, context.getUpdateTickRate());
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return Potion.finishTranslationKey(stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).potion(), this.getTranslationKey() + ".effect.");
    }
}

