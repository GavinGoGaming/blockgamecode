/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.slf4j.Logger;

public class KnowledgeBookItem
extends Item {
    private static final Logger LOGGER = LogUtils.getLogger();

    public KnowledgeBookItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        List list = lv.getOrDefault(DataComponentTypes.RECIPES, List.of());
        lv.decrementUnlessCreative(1, user);
        if (list.isEmpty()) {
            return TypedActionResult.fail(lv);
        }
        if (!world.isClient) {
            RecipeManager lv2 = world.getServer().getRecipeManager();
            ArrayList list2 = new ArrayList(list.size());
            for (Identifier lv3 : list) {
                Optional<RecipeEntry<?>> optional = lv2.get(lv3);
                if (optional.isPresent()) {
                    list2.add(optional.get());
                    continue;
                }
                LOGGER.error("Invalid recipe: {}", (Object)lv3);
                return TypedActionResult.fail(lv);
            }
            user.unlockRecipes(list2);
            user.incrementStat(Stats.USED.getOrCreateStat(this));
        }
        return TypedActionResult.success(lv, world.isClient());
    }
}

