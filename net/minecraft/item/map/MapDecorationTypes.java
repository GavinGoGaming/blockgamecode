/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item.map;

import net.minecraft.block.MapColor;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class MapDecorationTypes {
    private static final int TRIAL_CHAMBERS_MAP_COLOR = 12741452;
    public static final RegistryEntry<MapDecorationType> PLAYER = MapDecorationTypes.register("player", "player", false, true);
    public static final RegistryEntry<MapDecorationType> FRAME = MapDecorationTypes.register("frame", "frame", true, true);
    public static final RegistryEntry<MapDecorationType> RED_MARKER = MapDecorationTypes.register("red_marker", "red_marker", false, true);
    public static final RegistryEntry<MapDecorationType> BLUE_MARKER = MapDecorationTypes.register("blue_marker", "blue_marker", false, true);
    public static final RegistryEntry<MapDecorationType> TARGET_X = MapDecorationTypes.register("target_x", "target_x", true, false);
    public static final RegistryEntry<MapDecorationType> TARGET_POINT = MapDecorationTypes.register("target_point", "target_point", true, false);
    public static final RegistryEntry<MapDecorationType> PLAYER_OFF_MAP = MapDecorationTypes.register("player_off_map", "player_off_map", false, true);
    public static final RegistryEntry<MapDecorationType> PLAYER_OFF_LIMITS = MapDecorationTypes.register("player_off_limits", "player_off_limits", false, true);
    public static final RegistryEntry<MapDecorationType> MANSION = MapDecorationTypes.register("mansion", "woodland_mansion", true, 5393476, false, true);
    public static final RegistryEntry<MapDecorationType> MONUMENT = MapDecorationTypes.register("monument", "ocean_monument", true, 3830373, false, true);
    public static final RegistryEntry<MapDecorationType> BANNER_WHITE = MapDecorationTypes.register("banner_white", "white_banner", true, true);
    public static final RegistryEntry<MapDecorationType> BANNER_ORANGE = MapDecorationTypes.register("banner_orange", "orange_banner", true, true);
    public static final RegistryEntry<MapDecorationType> BANNER_MAGENTA = MapDecorationTypes.register("banner_magenta", "magenta_banner", true, true);
    public static final RegistryEntry<MapDecorationType> BANNER_LIGHT_BLUE = MapDecorationTypes.register("banner_light_blue", "light_blue_banner", true, true);
    public static final RegistryEntry<MapDecorationType> BANNER_YELLOW = MapDecorationTypes.register("banner_yellow", "yellow_banner", true, true);
    public static final RegistryEntry<MapDecorationType> BANNER_LIME = MapDecorationTypes.register("banner_lime", "lime_banner", true, true);
    public static final RegistryEntry<MapDecorationType> BANNER_PINK = MapDecorationTypes.register("banner_pink", "pink_banner", true, true);
    public static final RegistryEntry<MapDecorationType> BANNER_GRAY = MapDecorationTypes.register("banner_gray", "gray_banner", true, true);
    public static final RegistryEntry<MapDecorationType> BANNER_LIGHT_GRAY = MapDecorationTypes.register("banner_light_gray", "light_gray_banner", true, true);
    public static final RegistryEntry<MapDecorationType> BANNER_CYAN = MapDecorationTypes.register("banner_cyan", "cyan_banner", true, true);
    public static final RegistryEntry<MapDecorationType> BANNER_PURPLE = MapDecorationTypes.register("banner_purple", "purple_banner", true, true);
    public static final RegistryEntry<MapDecorationType> BANNER_BLUE = MapDecorationTypes.register("banner_blue", "blue_banner", true, true);
    public static final RegistryEntry<MapDecorationType> BANNER_BROWN = MapDecorationTypes.register("banner_brown", "brown_banner", true, true);
    public static final RegistryEntry<MapDecorationType> BANNER_GREEN = MapDecorationTypes.register("banner_green", "green_banner", true, true);
    public static final RegistryEntry<MapDecorationType> BANNER_RED = MapDecorationTypes.register("banner_red", "red_banner", true, true);
    public static final RegistryEntry<MapDecorationType> BANNER_BLACK = MapDecorationTypes.register("banner_black", "black_banner", true, true);
    public static final RegistryEntry<MapDecorationType> RED_X = MapDecorationTypes.register("red_x", "red_x", true, false);
    public static final RegistryEntry<MapDecorationType> VILLAGE_DESERT = MapDecorationTypes.register("village_desert", "desert_village", true, MapColor.LIGHT_GRAY.color, false, true);
    public static final RegistryEntry<MapDecorationType> VILLAGE_PLAINS = MapDecorationTypes.register("village_plains", "plains_village", true, MapColor.LIGHT_GRAY.color, false, true);
    public static final RegistryEntry<MapDecorationType> VILLAGE_SAVANNA = MapDecorationTypes.register("village_savanna", "savanna_village", true, MapColor.LIGHT_GRAY.color, false, true);
    public static final RegistryEntry<MapDecorationType> VILLAGE_SNOWY = MapDecorationTypes.register("village_snowy", "snowy_village", true, MapColor.LIGHT_GRAY.color, false, true);
    public static final RegistryEntry<MapDecorationType> VILLAGE_TAIGA = MapDecorationTypes.register("village_taiga", "taiga_village", true, MapColor.LIGHT_GRAY.color, false, true);
    public static final RegistryEntry<MapDecorationType> JUNGLE_TEMPLE = MapDecorationTypes.register("jungle_temple", "jungle_temple", true, MapColor.LIGHT_GRAY.color, false, true);
    public static final RegistryEntry<MapDecorationType> SWAMP_HUT = MapDecorationTypes.register("swamp_hut", "swamp_hut", true, MapColor.LIGHT_GRAY.color, false, true);
    public static final RegistryEntry<MapDecorationType> TRIAL_CHAMBERS = MapDecorationTypes.register("trial_chambers", "trial_chambers", true, 12741452, false, true);

    public static RegistryEntry<MapDecorationType> getDefault(Registry<MapDecorationType> registry) {
        return PLAYER;
    }

    private static RegistryEntry<MapDecorationType> register(String id, String assetId, boolean showOnItemFrame, boolean trackCount) {
        return MapDecorationTypes.register(id, assetId, showOnItemFrame, -1, trackCount, false);
    }

    private static RegistryEntry<MapDecorationType> register(String id, String assetId, boolean showOnItemFrame, int mapColor, boolean trackCount, boolean explorationMapElement) {
        RegistryKey<MapDecorationType> lv = RegistryKey.of(RegistryKeys.MAP_DECORATION_TYPE, Identifier.ofVanilla(id));
        MapDecorationType lv2 = new MapDecorationType(Identifier.ofVanilla(assetId), showOnItemFrame, mapColor, explorationMapElement, trackCount);
        return Registry.registerReference(Registries.MAP_DECORATION_TYPE, lv, lv2);
    }
}

