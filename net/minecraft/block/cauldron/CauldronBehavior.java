/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.cauldron;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public interface CauldronBehavior {
    public static final Map<String, CauldronBehaviorMap> BEHAVIOR_MAPS = new Object2ObjectArrayMap<String, CauldronBehaviorMap>();
    public static final Codec<CauldronBehaviorMap> CODEC = Codec.stringResolver(CauldronBehaviorMap::name, BEHAVIOR_MAPS::get);
    public static final CauldronBehaviorMap EMPTY_CAULDRON_BEHAVIOR = CauldronBehavior.createMap("empty");
    public static final CauldronBehaviorMap WATER_CAULDRON_BEHAVIOR = CauldronBehavior.createMap("water");
    public static final CauldronBehaviorMap LAVA_CAULDRON_BEHAVIOR = CauldronBehavior.createMap("lava");
    public static final CauldronBehaviorMap POWDER_SNOW_CAULDRON_BEHAVIOR = CauldronBehavior.createMap("powder_snow");
    public static final CauldronBehavior FILL_WITH_WATER = (state, world, pos, player, hand, stack) -> CauldronBehavior.fillCauldron(world, pos, player, hand, stack, (BlockState)Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3), SoundEvents.ITEM_BUCKET_EMPTY);
    public static final CauldronBehavior FILL_WITH_LAVA = (state, world, pos, player, hand, stack) -> CauldronBehavior.fillCauldron(world, pos, player, hand, stack, Blocks.LAVA_CAULDRON.getDefaultState(), SoundEvents.ITEM_BUCKET_EMPTY_LAVA);
    public static final CauldronBehavior FILL_WITH_POWDER_SNOW = (state, world, pos, player, hand, stack) -> CauldronBehavior.fillCauldron(world, pos, player, hand, stack, (BlockState)Blocks.POWDER_SNOW_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3), SoundEvents.ITEM_BUCKET_EMPTY_POWDER_SNOW);
    public static final CauldronBehavior CLEAN_SHULKER_BOX = (state, world, pos, player, hand, stack) -> {
        Block lv = Block.getBlockFromItem(stack.getItem());
        if (!(lv instanceof ShulkerBoxBlock)) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!world.isClient) {
            ItemStack lv2 = stack.copyComponentsToNewStack(Blocks.SHULKER_BOX, 1);
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, lv2, false));
            player.incrementStat(Stats.CLEAN_SHULKER_BOX);
            LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
        }
        return ItemActionResult.success(world.isClient);
    };
    public static final CauldronBehavior CLEAN_BANNER = (state, world, pos, player, hand, stack) -> {
        BannerPatternsComponent lv = stack.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT);
        if (lv.layers().isEmpty()) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!world.isClient) {
            ItemStack lv2 = stack.copyWithCount(1);
            lv2.set(DataComponentTypes.BANNER_PATTERNS, lv.withoutTopLayer());
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, lv2, false));
            player.incrementStat(Stats.CLEAN_BANNER);
            LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
        }
        return ItemActionResult.success(world.isClient);
    };
    public static final CauldronBehavior CLEAN_DYEABLE_ITEM = (state, world, pos, player, hand, stack) -> {
        if (!stack.isIn(ItemTags.DYEABLE)) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!stack.contains(DataComponentTypes.DYED_COLOR)) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!world.isClient) {
            stack.remove(DataComponentTypes.DYED_COLOR);
            player.incrementStat(Stats.CLEAN_ARMOR);
            LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
        }
        return ItemActionResult.success(world.isClient);
    };

    public static CauldronBehaviorMap createMap(String name) {
        Object2ObjectOpenHashMap<Item, CauldronBehavior> object2ObjectOpenHashMap = new Object2ObjectOpenHashMap<Item, CauldronBehavior>();
        object2ObjectOpenHashMap.defaultReturnValue((state, world, pos, player, hand, stack) -> ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
        CauldronBehaviorMap lv = new CauldronBehaviorMap(name, object2ObjectOpenHashMap);
        BEHAVIOR_MAPS.put(name, lv);
        return lv;
    }

    public ItemActionResult interact(BlockState var1, World var2, BlockPos var3, PlayerEntity var4, Hand var5, ItemStack var6);

    public static void registerBehavior() {
        Map<Item, CauldronBehavior> map = EMPTY_CAULDRON_BEHAVIOR.map();
        CauldronBehavior.registerBucketBehavior(map);
        map.put(Items.POTION, (state, world, pos, player, hand, stack) -> {
            PotionContentsComponent lv = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (lv == null || !lv.matches(Potions.WATER)) {
                return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (!world.isClient) {
                Item lv2 = stack.getItem();
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                player.incrementStat(Stats.USE_CAULDRON);
                player.incrementStat(Stats.USED.getOrCreateStat(lv2));
                world.setBlockState(pos, Blocks.WATER_CAULDRON.getDefaultState());
                world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
                world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return ItemActionResult.success(world.isClient);
        });
        Map<Item, CauldronBehavior> map2 = WATER_CAULDRON_BEHAVIOR.map();
        CauldronBehavior.registerBucketBehavior(map2);
        map2.put(Items.BUCKET, (state, world, pos, player, hand, stack) -> CauldronBehavior.emptyCauldron(state, world, pos, player, hand, stack, new ItemStack(Items.WATER_BUCKET), statex -> statex.get(LeveledCauldronBlock.LEVEL) == 3, SoundEvents.ITEM_BUCKET_FILL));
        map2.put(Items.GLASS_BOTTLE, (state, world, pos, player, hand, stack) -> {
            if (!world.isClient) {
                Item lv = stack.getItem();
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, PotionContentsComponent.createStack(Items.POTION, Potions.WATER)));
                player.incrementStat(Stats.USE_CAULDRON);
                player.incrementStat(Stats.USED.getOrCreateStat(lv));
                LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
                world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return ItemActionResult.success(world.isClient);
        });
        map2.put(Items.POTION, (state, world, pos, player, hand, stack) -> {
            if (state.get(LeveledCauldronBlock.LEVEL) == 3) {
                return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            PotionContentsComponent lv = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (lv == null || !lv.matches(Potions.WATER)) {
                return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (!world.isClient) {
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                player.incrementStat(Stats.USE_CAULDRON);
                player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
                world.setBlockState(pos, (BlockState)state.cycle(LeveledCauldronBlock.LEVEL));
                world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
                world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return ItemActionResult.success(world.isClient);
        });
        map2.put(Items.LEATHER_BOOTS, CLEAN_DYEABLE_ITEM);
        map2.put(Items.LEATHER_LEGGINGS, CLEAN_DYEABLE_ITEM);
        map2.put(Items.LEATHER_CHESTPLATE, CLEAN_DYEABLE_ITEM);
        map2.put(Items.LEATHER_HELMET, CLEAN_DYEABLE_ITEM);
        map2.put(Items.LEATHER_HORSE_ARMOR, CLEAN_DYEABLE_ITEM);
        map2.put(Items.WOLF_ARMOR, CLEAN_DYEABLE_ITEM);
        map2.put(Items.WHITE_BANNER, CLEAN_BANNER);
        map2.put(Items.GRAY_BANNER, CLEAN_BANNER);
        map2.put(Items.BLACK_BANNER, CLEAN_BANNER);
        map2.put(Items.BLUE_BANNER, CLEAN_BANNER);
        map2.put(Items.BROWN_BANNER, CLEAN_BANNER);
        map2.put(Items.CYAN_BANNER, CLEAN_BANNER);
        map2.put(Items.GREEN_BANNER, CLEAN_BANNER);
        map2.put(Items.LIGHT_BLUE_BANNER, CLEAN_BANNER);
        map2.put(Items.LIGHT_GRAY_BANNER, CLEAN_BANNER);
        map2.put(Items.LIME_BANNER, CLEAN_BANNER);
        map2.put(Items.MAGENTA_BANNER, CLEAN_BANNER);
        map2.put(Items.ORANGE_BANNER, CLEAN_BANNER);
        map2.put(Items.PINK_BANNER, CLEAN_BANNER);
        map2.put(Items.PURPLE_BANNER, CLEAN_BANNER);
        map2.put(Items.RED_BANNER, CLEAN_BANNER);
        map2.put(Items.YELLOW_BANNER, CLEAN_BANNER);
        map2.put(Items.WHITE_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.GRAY_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.BLACK_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.BLUE_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.BROWN_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.CYAN_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.GREEN_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.LIGHT_BLUE_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.LIGHT_GRAY_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.LIME_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.MAGENTA_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.ORANGE_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.PINK_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.PURPLE_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.RED_SHULKER_BOX, CLEAN_SHULKER_BOX);
        map2.put(Items.YELLOW_SHULKER_BOX, CLEAN_SHULKER_BOX);
        Map<Item, CauldronBehavior> map3 = LAVA_CAULDRON_BEHAVIOR.map();
        map3.put(Items.BUCKET, (state, world, pos, player, hand, stack) -> CauldronBehavior.emptyCauldron(state, world, pos, player, hand, stack, new ItemStack(Items.LAVA_BUCKET), statex -> true, SoundEvents.ITEM_BUCKET_FILL_LAVA));
        CauldronBehavior.registerBucketBehavior(map3);
        Map<Item, CauldronBehavior> map4 = POWDER_SNOW_CAULDRON_BEHAVIOR.map();
        map4.put(Items.BUCKET, (state, world, pos, player, hand, stack) -> CauldronBehavior.emptyCauldron(state, world, pos, player, hand, stack, new ItemStack(Items.POWDER_SNOW_BUCKET), statex -> statex.get(LeveledCauldronBlock.LEVEL) == 3, SoundEvents.ITEM_BUCKET_FILL_POWDER_SNOW));
        CauldronBehavior.registerBucketBehavior(map4);
    }

    public static void registerBucketBehavior(Map<Item, CauldronBehavior> behavior) {
        behavior.put(Items.LAVA_BUCKET, FILL_WITH_LAVA);
        behavior.put(Items.WATER_BUCKET, FILL_WITH_WATER);
        behavior.put(Items.POWDER_SNOW_BUCKET, FILL_WITH_POWDER_SNOW);
    }

    public static ItemActionResult emptyCauldron(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack, ItemStack output, Predicate<BlockState> fullPredicate, SoundEvent soundEvent) {
        if (!fullPredicate.test(state)) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!world.isClient) {
            Item lv = stack.getItem();
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, output));
            player.incrementStat(Stats.USE_CAULDRON);
            player.incrementStat(Stats.USED.getOrCreateStat(lv));
            world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
            world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
        }
        return ItemActionResult.success(world.isClient);
    }

    public static ItemActionResult fillCauldron(World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack, BlockState state, SoundEvent soundEvent) {
        if (!world.isClient) {
            Item lv = stack.getItem();
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.BUCKET)));
            player.incrementStat(Stats.FILL_CAULDRON);
            player.incrementStat(Stats.USED.getOrCreateStat(lv));
            world.setBlockState(pos, state);
            world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
        return ItemActionResult.success(world.isClient);
    }

    public record CauldronBehaviorMap(String name, Map<Item, CauldronBehavior> map) {
    }
}

