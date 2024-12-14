/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.TagKey;

public interface ToolMaterial {
    public int getDurability();

    public float getMiningSpeedMultiplier();

    public float getAttackDamage();

    public TagKey<Block> getInverseTag();

    public int getEnchantability();

    public Ingredient getRepairIngredient();

    default public ToolComponent createComponent(TagKey<Block> tag) {
        return new ToolComponent(List.of(ToolComponent.Rule.ofNeverDropping(this.getInverseTag()), ToolComponent.Rule.ofAlwaysDropping(tag, this.getMiningSpeedMultiplier())), 1.0f, 1);
    }
}

