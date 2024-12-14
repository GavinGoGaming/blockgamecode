/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimMaterials;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.item.trim.ArmorTrimPatterns;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpawnArmorTrimsCommand {
    private static final Map<Pair<RegistryEntry<ArmorMaterial>, EquipmentSlot>, Item> ARMOR_PIECES = Util.make(Maps.newHashMap(), map -> {
        map.put(Pair.of(ArmorMaterials.CHAIN, EquipmentSlot.HEAD), Items.CHAINMAIL_HELMET);
        map.put(Pair.of(ArmorMaterials.CHAIN, EquipmentSlot.CHEST), Items.CHAINMAIL_CHESTPLATE);
        map.put(Pair.of(ArmorMaterials.CHAIN, EquipmentSlot.LEGS), Items.CHAINMAIL_LEGGINGS);
        map.put(Pair.of(ArmorMaterials.CHAIN, EquipmentSlot.FEET), Items.CHAINMAIL_BOOTS);
        map.put(Pair.of(ArmorMaterials.IRON, EquipmentSlot.HEAD), Items.IRON_HELMET);
        map.put(Pair.of(ArmorMaterials.IRON, EquipmentSlot.CHEST), Items.IRON_CHESTPLATE);
        map.put(Pair.of(ArmorMaterials.IRON, EquipmentSlot.LEGS), Items.IRON_LEGGINGS);
        map.put(Pair.of(ArmorMaterials.IRON, EquipmentSlot.FEET), Items.IRON_BOOTS);
        map.put(Pair.of(ArmorMaterials.GOLD, EquipmentSlot.HEAD), Items.GOLDEN_HELMET);
        map.put(Pair.of(ArmorMaterials.GOLD, EquipmentSlot.CHEST), Items.GOLDEN_CHESTPLATE);
        map.put(Pair.of(ArmorMaterials.GOLD, EquipmentSlot.LEGS), Items.GOLDEN_LEGGINGS);
        map.put(Pair.of(ArmorMaterials.GOLD, EquipmentSlot.FEET), Items.GOLDEN_BOOTS);
        map.put(Pair.of(ArmorMaterials.NETHERITE, EquipmentSlot.HEAD), Items.NETHERITE_HELMET);
        map.put(Pair.of(ArmorMaterials.NETHERITE, EquipmentSlot.CHEST), Items.NETHERITE_CHESTPLATE);
        map.put(Pair.of(ArmorMaterials.NETHERITE, EquipmentSlot.LEGS), Items.NETHERITE_LEGGINGS);
        map.put(Pair.of(ArmorMaterials.NETHERITE, EquipmentSlot.FEET), Items.NETHERITE_BOOTS);
        map.put(Pair.of(ArmorMaterials.DIAMOND, EquipmentSlot.HEAD), Items.DIAMOND_HELMET);
        map.put(Pair.of(ArmorMaterials.DIAMOND, EquipmentSlot.CHEST), Items.DIAMOND_CHESTPLATE);
        map.put(Pair.of(ArmorMaterials.DIAMOND, EquipmentSlot.LEGS), Items.DIAMOND_LEGGINGS);
        map.put(Pair.of(ArmorMaterials.DIAMOND, EquipmentSlot.FEET), Items.DIAMOND_BOOTS);
        map.put(Pair.of(ArmorMaterials.TURTLE, EquipmentSlot.HEAD), Items.TURTLE_HELMET);
    });
    private static final List<RegistryKey<ArmorTrimPattern>> PATTERNS = List.of(ArmorTrimPatterns.SENTRY, ArmorTrimPatterns.DUNE, ArmorTrimPatterns.COAST, ArmorTrimPatterns.WILD, ArmorTrimPatterns.WARD, ArmorTrimPatterns.EYE, ArmorTrimPatterns.VEX, ArmorTrimPatterns.TIDE, ArmorTrimPatterns.SNOUT, ArmorTrimPatterns.RIB, ArmorTrimPatterns.SPIRE, ArmorTrimPatterns.WAYFINDER, ArmorTrimPatterns.SHAPER, ArmorTrimPatterns.SILENCE, ArmorTrimPatterns.RAISER, ArmorTrimPatterns.HOST, ArmorTrimPatterns.FLOW, ArmorTrimPatterns.BOLT);
    private static final List<RegistryKey<ArmorTrimMaterial>> MATERIALS = List.of(ArmorTrimMaterials.QUARTZ, ArmorTrimMaterials.IRON, ArmorTrimMaterials.NETHERITE, ArmorTrimMaterials.REDSTONE, ArmorTrimMaterials.COPPER, ArmorTrimMaterials.GOLD, ArmorTrimMaterials.EMERALD, ArmorTrimMaterials.DIAMOND, ArmorTrimMaterials.LAPIS, ArmorTrimMaterials.AMETHYST);
    private static final ToIntFunction<RegistryKey<ArmorTrimPattern>> PATTERN_INDEX_GETTER = Util.lastIndexGetter(PATTERNS);
    private static final ToIntFunction<RegistryKey<ArmorTrimMaterial>> MATERIAL_INDEX_GETTER = Util.lastIndexGetter(MATERIALS);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("spawn_armor_trims").requires(source -> source.hasPermissionLevel(2))).executes(context -> SpawnArmorTrimsCommand.execute((ServerCommandSource)context.getSource(), ((ServerCommandSource)context.getSource()).getPlayerOrThrow())));
    }

    private static int execute(ServerCommandSource source, PlayerEntity player) {
        World lv = player.getWorld();
        DefaultedList<ArmorTrim> lv2 = DefaultedList.of();
        Registry<ArmorTrimPattern> lv3 = lv.getRegistryManager().get(RegistryKeys.TRIM_PATTERN);
        Registry<ArmorTrimMaterial> lv4 = lv.getRegistryManager().get(RegistryKeys.TRIM_MATERIAL);
        lv3.stream().sorted(Comparator.comparing(pattern -> PATTERN_INDEX_GETTER.applyAsInt(lv3.getKey((ArmorTrimPattern)pattern).orElse(null)))).forEachOrdered(pattern -> lv4.stream().sorted(Comparator.comparing(material -> MATERIAL_INDEX_GETTER.applyAsInt(lv4.getKey((ArmorTrimMaterial)material).orElse(null)))).forEachOrdered(material -> lv2.add(new ArmorTrim(lv4.getEntry((ArmorTrimMaterial)material), lv3.getEntry((ArmorTrimPattern)pattern)))));
        BlockPos lv5 = player.getBlockPos().offset(player.getHorizontalFacing(), 5);
        Registry<ArmorMaterial> lv6 = source.getRegistryManager().get(RegistryKeys.ARMOR_MATERIAL);
        int i = lv6.size() - 1;
        double d = 3.0;
        int j = 0;
        int k = 0;
        for (ArmorTrim lv7 : lv2) {
            for (ArmorMaterial lv8 : lv6) {
                if (lv8 == ArmorMaterials.LEATHER.value()) continue;
                double e = (double)lv5.getX() + 0.5 - (double)(j % lv4.size()) * 3.0;
                double f = (double)lv5.getY() + 0.5 + (double)(k % i) * 3.0;
                double g = (double)lv5.getZ() + 0.5 + (double)(j / lv4.size() * 10);
                ArmorStandEntity lv9 = new ArmorStandEntity(lv, e, f, g);
                lv9.setYaw(180.0f);
                lv9.setNoGravity(true);
                for (EquipmentSlot lv10 : EquipmentSlot.values()) {
                    ArmorItem lv13;
                    Item lv11 = ARMOR_PIECES.get(Pair.of(lv8, lv10));
                    if (lv11 == null) continue;
                    ItemStack lv12 = new ItemStack(lv11);
                    lv12.set(DataComponentTypes.TRIM, lv7);
                    lv9.equipStack(lv10, lv12);
                    if (lv11 instanceof ArmorItem && (lv13 = (ArmorItem)lv11).getMaterial().matches(ArmorMaterials.TURTLE)) {
                        lv9.setCustomName(lv7.getPattern().value().getDescription(lv7.getMaterial()).copy().append(" ").append(lv7.getMaterial().value().description()));
                        lv9.setCustomNameVisible(true);
                        continue;
                    }
                    lv9.setInvisible(true);
                }
                lv.spawnEntity(lv9);
                ++k;
            }
            ++j;
        }
        source.sendFeedback(() -> Text.literal("Armorstands with trimmed armor spawned around you"), true);
        return 1;
    }
}

