/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import net.minecraft.block.MapColor;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public enum DyeColor implements StringIdentifiable
{
    WHITE(0, "white", 0xF9FFFE, MapColor.WHITE, 0xF0F0F0, 0xFFFFFF),
    ORANGE(1, "orange", 16351261, MapColor.ORANGE, 15435844, 16738335),
    MAGENTA(2, "magenta", 13061821, MapColor.MAGENTA, 12801229, 0xFF00FF),
    LIGHT_BLUE(3, "light_blue", 3847130, MapColor.LIGHT_BLUE, 6719955, 10141901),
    YELLOW(4, "yellow", 16701501, MapColor.YELLOW, 14602026, 0xFFFF00),
    LIME(5, "lime", 8439583, MapColor.LIME, 4312372, 0xBFFF00),
    PINK(6, "pink", 15961002, MapColor.PINK, 14188952, 16738740),
    GRAY(7, "gray", 4673362, MapColor.GRAY, 0x434343, 0x808080),
    LIGHT_GRAY(8, "light_gray", 0x9D9D97, MapColor.LIGHT_GRAY, 0xABABAB, 0xD3D3D3),
    CYAN(9, "cyan", 1481884, MapColor.CYAN, 2651799, 65535),
    PURPLE(10, "purple", 8991416, MapColor.PURPLE, 8073150, 10494192),
    BLUE(11, "blue", 3949738, MapColor.BLUE, 2437522, 255),
    BROWN(12, "brown", 8606770, MapColor.BROWN, 5320730, 9127187),
    GREEN(13, "green", 6192150, MapColor.GREEN, 3887386, 65280),
    RED(14, "red", 11546150, MapColor.RED, 11743532, 0xFF0000),
    BLACK(15, "black", 0x1D1D21, MapColor.BLACK, 0x1E1B1B, 0);

    private static final IntFunction<DyeColor> BY_ID;
    private static final Int2ObjectOpenHashMap<DyeColor> BY_FIREWORK_COLOR;
    public static final StringIdentifiable.EnumCodec<DyeColor> CODEC;
    public static final PacketCodec<ByteBuf, DyeColor> PACKET_CODEC;
    private final int id;
    private final String name;
    private final MapColor mapColor;
    private final int entityColor;
    private final int fireworkColor;
    private final int signColor;

    private DyeColor(int id, String name, int entityColor, MapColor mapColor, int fireworkColor, int signColor) {
        this.id = id;
        this.name = name;
        this.mapColor = mapColor;
        this.signColor = signColor;
        this.entityColor = ColorHelper.Argb.fullAlpha(entityColor);
        this.fireworkColor = fireworkColor;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getEntityColor() {
        return this.entityColor;
    }

    public MapColor getMapColor() {
        return this.mapColor;
    }

    public int getFireworkColor() {
        return this.fireworkColor;
    }

    public int getSignColor() {
        return this.signColor;
    }

    public static DyeColor byId(int id) {
        return BY_ID.apply(id);
    }

    @Nullable
    @Contract(value="_,!null->!null;_,null->_")
    public static DyeColor byName(String name, @Nullable DyeColor defaultColor) {
        DyeColor lv = CODEC.byId(name);
        return lv != null ? lv : defaultColor;
    }

    @Nullable
    public static DyeColor byFireworkColor(int color) {
        return BY_FIREWORK_COLOR.get(color);
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    static {
        BY_ID = ValueLists.createIdToValueFunction(DyeColor::getId, DyeColor.values(), ValueLists.OutOfBoundsHandling.ZERO);
        BY_FIREWORK_COLOR = new Int2ObjectOpenHashMap<DyeColor>(Arrays.stream(DyeColor.values()).collect(Collectors.toMap(color -> color.fireworkColor, color -> color)));
        CODEC = StringIdentifiable.createCodec(DyeColor::values);
        PACKET_CODEC = PacketCodecs.indexed(BY_ID, DyeColor::getId);
    }
}

