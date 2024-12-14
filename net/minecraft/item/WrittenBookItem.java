/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.StringHelper;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WrittenBookItem
extends Item {
    public WrittenBookItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public Text getName(ItemStack stack) {
        String string;
        WrittenBookContentComponent lv = stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
        if (lv != null && !StringHelper.isBlank(string = lv.title().raw())) {
            return Text.literal(string);
        }
        return super.getName(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        WrittenBookContentComponent lv = stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
        if (lv != null) {
            if (!StringHelper.isBlank(lv.author())) {
                tooltip.add(Text.translatable("book.byAuthor", lv.author()).formatted(Formatting.GRAY));
            }
            tooltip.add(Text.translatable("book.generation." + lv.generation()).formatted(Formatting.GRAY));
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        user.useBook(lv, hand);
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        return TypedActionResult.success(lv, world.isClient());
    }

    public static boolean resolve(ItemStack book, ServerCommandSource commandSource, @Nullable PlayerEntity player) {
        WrittenBookContentComponent lv = book.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
        if (lv != null && !lv.resolved()) {
            WrittenBookContentComponent lv2 = lv.resolve(commandSource, player);
            if (lv2 != null) {
                book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, lv2);
                return true;
            }
            book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, lv.asResolved());
        }
        return false;
    }
}

