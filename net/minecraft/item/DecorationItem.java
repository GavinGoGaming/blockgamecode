/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import java.util.List;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.GlowItemFrameEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class DecorationItem
extends Item {
    private static final Text RANDOM_TEXT = Text.translatable("painting.random").formatted(Formatting.GRAY);
    private final EntityType<? extends AbstractDecorationEntity> entityType;

    public DecorationItem(EntityType<? extends AbstractDecorationEntity> type, Item.Settings settings) {
        super(settings);
        this.entityType = type;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        AbstractDecorationEntity lv7;
        BlockPos lv = context.getBlockPos();
        Direction lv2 = context.getSide();
        BlockPos lv3 = lv.offset(lv2);
        PlayerEntity lv4 = context.getPlayer();
        ItemStack lv5 = context.getStack();
        if (lv4 != null && !this.canPlaceOn(lv4, lv2, lv5, lv3)) {
            return ActionResult.FAIL;
        }
        World lv6 = context.getWorld();
        if (this.entityType == EntityType.PAINTING) {
            Optional<PaintingEntity> optional = PaintingEntity.placePainting(lv6, lv3, lv2);
            if (optional.isEmpty()) {
                return ActionResult.CONSUME;
            }
            lv7 = optional.get();
        } else if (this.entityType == EntityType.ITEM_FRAME) {
            lv7 = new ItemFrameEntity(lv6, lv3, lv2);
        } else if (this.entityType == EntityType.GLOW_ITEM_FRAME) {
            lv7 = new GlowItemFrameEntity(lv6, lv3, lv2);
        } else {
            return ActionResult.success(lv6.isClient);
        }
        NbtComponent lv8 = lv5.getOrDefault(DataComponentTypes.ENTITY_DATA, NbtComponent.DEFAULT);
        if (!lv8.isEmpty()) {
            EntityType.loadFromEntityNbt(lv6, lv4, lv7, lv8);
        }
        if (lv7.canStayAttached()) {
            if (!lv6.isClient) {
                lv7.onPlace();
                lv6.emitGameEvent((Entity)lv4, GameEvent.ENTITY_PLACE, lv7.getPos());
                lv6.spawnEntity(lv7);
            }
            lv5.decrement(1);
            return ActionResult.success(lv6.isClient);
        }
        return ActionResult.CONSUME;
    }

    protected boolean canPlaceOn(PlayerEntity player, Direction side, ItemStack stack, BlockPos pos) {
        return !side.getAxis().isVertical() && player.canPlaceOn(pos, side, stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        RegistryWrapper.WrapperLookup lv = context.getRegistryLookup();
        if (lv != null && this.entityType == EntityType.PAINTING) {
            NbtComponent lv2 = stack.getOrDefault(DataComponentTypes.ENTITY_DATA, NbtComponent.DEFAULT);
            if (!lv2.isEmpty()) {
                lv2.get(lv.getOps(NbtOps.INSTANCE), PaintingEntity.VARIANT_MAP_CODEC).result().ifPresentOrElse(variant -> {
                    variant.getKey().ifPresent(key -> {
                        tooltip.add(Text.translatable(key.getValue().toTranslationKey("painting", "title")).formatted(Formatting.YELLOW));
                        tooltip.add(Text.translatable(key.getValue().toTranslationKey("painting", "author")).formatted(Formatting.GRAY));
                    });
                    tooltip.add(Text.translatable("painting.dimensions", ((PaintingVariant)variant.value()).width(), ((PaintingVariant)variant.value()).height()));
                }, () -> tooltip.add(RANDOM_TEXT));
            } else if (type.isCreative()) {
                tooltip.add(RANDOM_TEXT);
            }
        }
    }
}

