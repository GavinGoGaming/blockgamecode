/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtTypes;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.nbt.visitor.NbtOrderedStringFormatter;
import net.minecraft.nbt.visitor.NbtTextFormatter;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class NbtHelper {
    private static final Comparator<NbtList> BLOCK_POS_COMPARATOR = Comparator.comparingInt(nbt -> nbt.getInt(1)).thenComparingInt(nbt -> nbt.getInt(0)).thenComparingInt(nbt -> nbt.getInt(2));
    private static final Comparator<NbtList> ENTITY_POS_COMPARATOR = Comparator.comparingDouble(nbt -> nbt.getDouble(1)).thenComparingDouble(nbt -> nbt.getDouble(0)).thenComparingDouble(nbt -> nbt.getDouble(2));
    public static final String DATA_KEY = "data";
    private static final char LEFT_CURLY_BRACKET = '{';
    private static final char RIGHT_CURLY_BRACKET = '}';
    private static final String COMMA = ",";
    private static final char COLON = ':';
    private static final Splitter COMMA_SPLITTER = Splitter.on(",");
    private static final Splitter COLON_SPLITTER = Splitter.on(':').limit(2);
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int field_33229 = 2;
    private static final int field_33230 = -1;

    private NbtHelper() {
    }

    @VisibleForTesting
    public static boolean matches(@Nullable NbtElement standard, @Nullable NbtElement subject, boolean ignoreListOrder) {
        if (standard == subject) {
            return true;
        }
        if (standard == null) {
            return true;
        }
        if (subject == null) {
            return false;
        }
        if (!standard.getClass().equals(subject.getClass())) {
            return false;
        }
        if (standard instanceof NbtCompound) {
            NbtCompound lv = (NbtCompound)standard;
            NbtCompound lv2 = (NbtCompound)subject;
            if (lv2.getSize() < lv.getSize()) {
                return false;
            }
            for (String string : lv.getKeys()) {
                NbtElement lv3 = lv.get(string);
                if (NbtHelper.matches(lv3, lv2.get(string), ignoreListOrder)) continue;
                return false;
            }
            return true;
        }
        if (standard instanceof NbtList) {
            NbtList lv4 = (NbtList)standard;
            if (ignoreListOrder) {
                NbtList lv5 = (NbtList)subject;
                if (lv4.isEmpty()) {
                    return lv5.isEmpty();
                }
                if (lv5.size() < lv4.size()) {
                    return false;
                }
                for (NbtElement lv6 : lv4) {
                    boolean bl2 = false;
                    for (NbtElement lv7 : lv5) {
                        if (!NbtHelper.matches(lv6, lv7, ignoreListOrder)) continue;
                        bl2 = true;
                        break;
                    }
                    if (bl2) continue;
                    return false;
                }
                return true;
            }
        }
        return standard.equals(subject);
    }

    public static NbtIntArray fromUuid(UUID uuid) {
        return new NbtIntArray(Uuids.toIntArray(uuid));
    }

    public static UUID toUuid(NbtElement element) {
        if (element.getNbtType() != NbtIntArray.TYPE) {
            throw new IllegalArgumentException("Expected UUID-Tag to be of type " + NbtIntArray.TYPE.getCrashReportName() + ", but found " + element.getNbtType().getCrashReportName() + ".");
        }
        int[] is = ((NbtIntArray)element).getIntArray();
        if (is.length != 4) {
            throw new IllegalArgumentException("Expected UUID-Array to be of length 4, but found " + is.length + ".");
        }
        return Uuids.toUuid(is);
    }

    public static Optional<BlockPos> toBlockPos(NbtCompound nbt, String key) {
        int[] is = nbt.getIntArray(key);
        if (is.length == 3) {
            return Optional.of(new BlockPos(is[0], is[1], is[2]));
        }
        return Optional.empty();
    }

    public static NbtElement fromBlockPos(BlockPos pos) {
        return new NbtIntArray(new int[]{pos.getX(), pos.getY(), pos.getZ()});
    }

    public static BlockState toBlockState(RegistryEntryLookup<Block> blockLookup, NbtCompound nbt) {
        if (!nbt.contains("Name", NbtElement.STRING_TYPE)) {
            return Blocks.AIR.getDefaultState();
        }
        Identifier lv = Identifier.of(nbt.getString("Name"));
        Optional<RegistryEntry.Reference<Block>> optional = blockLookup.getOptional(RegistryKey.of(RegistryKeys.BLOCK, lv));
        if (optional.isEmpty()) {
            return Blocks.AIR.getDefaultState();
        }
        Block lv2 = (Block)((RegistryEntry)optional.get()).value();
        BlockState lv3 = lv2.getDefaultState();
        if (nbt.contains("Properties", NbtElement.COMPOUND_TYPE)) {
            NbtCompound lv4 = nbt.getCompound("Properties");
            StateManager<Block, BlockState> lv5 = lv2.getStateManager();
            for (String string : lv4.getKeys()) {
                Property<?> lv6 = lv5.getProperty(string);
                if (lv6 == null) continue;
                lv3 = NbtHelper.withProperty(lv3, lv6, string, lv4, nbt);
            }
        }
        return lv3;
    }

    private static <S extends State<?, S>, T extends Comparable<T>> S withProperty(S state, Property<T> property, String key, NbtCompound properties, NbtCompound root) {
        Optional<T> optional = property.parse(properties.getString(key));
        if (optional.isPresent()) {
            return (S)((State)state.with(property, (Comparable)((Comparable)optional.get())));
        }
        LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", key, properties.getString(key), root);
        return state;
    }

    public static NbtCompound fromBlockState(BlockState state) {
        NbtCompound lv = new NbtCompound();
        lv.putString("Name", Registries.BLOCK.getId(state.getBlock()).toString());
        Map<Property<?>, Comparable<?>> map = state.getEntries();
        if (!map.isEmpty()) {
            NbtCompound lv2 = new NbtCompound();
            for (Map.Entry<Property<?>, Comparable<?>> entry : map.entrySet()) {
                Property<?> lv3 = entry.getKey();
                lv2.putString(lv3.getName(), NbtHelper.nameValue(lv3, entry.getValue()));
            }
            lv.put("Properties", lv2);
        }
        return lv;
    }

    public static NbtCompound fromFluidState(FluidState state) {
        NbtCompound lv = new NbtCompound();
        lv.putString("Name", Registries.FLUID.getId(state.getFluid()).toString());
        Map<Property<?>, Comparable<?>> map = state.getEntries();
        if (!map.isEmpty()) {
            NbtCompound lv2 = new NbtCompound();
            for (Map.Entry<Property<?>, Comparable<?>> entry : map.entrySet()) {
                Property<?> lv3 = entry.getKey();
                lv2.putString(lv3.getName(), NbtHelper.nameValue(lv3, entry.getValue()));
            }
            lv.put("Properties", lv2);
        }
        return lv;
    }

    private static <T extends Comparable<T>> String nameValue(Property<T> property, Comparable<?> value) {
        return property.name(value);
    }

    public static String toFormattedString(NbtElement nbt) {
        return NbtHelper.toFormattedString(nbt, false);
    }

    public static String toFormattedString(NbtElement nbt, boolean withArrayContents) {
        return NbtHelper.appendFormattedString(new StringBuilder(), nbt, 0, withArrayContents).toString();
    }

    public static StringBuilder appendFormattedString(StringBuilder stringBuilder, NbtElement nbt, int depth, boolean withArrayContents) {
        switch (nbt.getType()) {
            case 1: 
            case 2: 
            case 3: 
            case 4: 
            case 5: 
            case 6: 
            case 8: {
                stringBuilder.append(nbt);
                break;
            }
            case 0: {
                break;
            }
            case 7: {
                NbtByteArray lv = (NbtByteArray)nbt;
                byte[] bs = lv.getByteArray();
                int j = bs.length;
                NbtHelper.appendIndent(depth, stringBuilder).append("byte[").append(j).append("] {\n");
                if (withArrayContents) {
                    NbtHelper.appendIndent(depth + 1, stringBuilder);
                    for (int k = 0; k < bs.length; ++k) {
                        if (k != 0) {
                            stringBuilder.append(',');
                        }
                        if (k % 16 == 0 && k / 16 > 0) {
                            stringBuilder.append('\n');
                            if (k < bs.length) {
                                NbtHelper.appendIndent(depth + 1, stringBuilder);
                            }
                        } else if (k != 0) {
                            stringBuilder.append(' ');
                        }
                        stringBuilder.append(String.format(Locale.ROOT, "0x%02X", bs[k] & 0xFF));
                    }
                } else {
                    NbtHelper.appendIndent(depth + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
                }
                stringBuilder.append('\n');
                NbtHelper.appendIndent(depth, stringBuilder).append('}');
                break;
            }
            case 9: {
                NbtList lv2 = (NbtList)nbt;
                int l = lv2.size();
                byte j = lv2.getHeldType();
                String string = j == 0 ? "undefined" : NbtTypes.byId(j).getCommandFeedbackName();
                NbtHelper.appendIndent(depth, stringBuilder).append("list<").append(string).append(">[").append(l).append("] [");
                if (l != 0) {
                    stringBuilder.append('\n');
                }
                for (int m = 0; m < l; ++m) {
                    if (m != 0) {
                        stringBuilder.append(",\n");
                    }
                    NbtHelper.appendIndent(depth + 1, stringBuilder);
                    NbtHelper.appendFormattedString(stringBuilder, lv2.get(m), depth + 1, withArrayContents);
                }
                if (l != 0) {
                    stringBuilder.append('\n');
                }
                NbtHelper.appendIndent(depth, stringBuilder).append(']');
                break;
            }
            case 11: {
                NbtIntArray lv3 = (NbtIntArray)nbt;
                int[] is = lv3.getIntArray();
                int j = 0;
                int[] string = is;
                int m = string.length;
                for (int i = 0; i < m; ++i) {
                    int n = string[i];
                    j = Math.max(j, String.format(Locale.ROOT, "%X", n).length());
                }
                int k = is.length;
                NbtHelper.appendIndent(depth, stringBuilder).append("int[").append(k).append("] {\n");
                if (withArrayContents) {
                    NbtHelper.appendIndent(depth + 1, stringBuilder);
                    for (m = 0; m < is.length; ++m) {
                        if (m != 0) {
                            stringBuilder.append(',');
                        }
                        if (m % 16 == 0 && m / 16 > 0) {
                            stringBuilder.append('\n');
                            if (m < is.length) {
                                NbtHelper.appendIndent(depth + 1, stringBuilder);
                            }
                        } else if (m != 0) {
                            stringBuilder.append(' ');
                        }
                        stringBuilder.append(String.format(Locale.ROOT, "0x%0" + j + "X", is[m]));
                    }
                } else {
                    NbtHelper.appendIndent(depth + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
                }
                stringBuilder.append('\n');
                NbtHelper.appendIndent(depth, stringBuilder).append('}');
                break;
            }
            case 10: {
                NbtCompound lv4 = (NbtCompound)nbt;
                ArrayList<String> list = Lists.newArrayList(lv4.getKeys());
                Collections.sort(list);
                NbtHelper.appendIndent(depth, stringBuilder).append('{');
                if (stringBuilder.length() - stringBuilder.lastIndexOf("\n") > 2 * (depth + 1)) {
                    stringBuilder.append('\n');
                    NbtHelper.appendIndent(depth + 1, stringBuilder);
                }
                int j = list.stream().mapToInt(String::length).max().orElse(0);
                String string = Strings.repeat(" ", j);
                for (int m = 0; m < list.size(); ++m) {
                    if (m != 0) {
                        stringBuilder.append(",\n");
                    }
                    String string2 = (String)list.get(m);
                    NbtHelper.appendIndent(depth + 1, stringBuilder).append('\"').append(string2).append('\"').append(string, 0, string.length() - string2.length()).append(": ");
                    NbtHelper.appendFormattedString(stringBuilder, lv4.get(string2), depth + 1, withArrayContents);
                }
                if (!list.isEmpty()) {
                    stringBuilder.append('\n');
                }
                NbtHelper.appendIndent(depth, stringBuilder).append('}');
                break;
            }
            case 12: {
                int n;
                NbtLongArray lv5 = (NbtLongArray)nbt;
                long[] ls = lv5.getLongArray();
                long o = 0L;
                long[] m = ls;
                int n2 = m.length;
                for (n = 0; n < n2; ++n) {
                    long p = m[n];
                    o = Math.max(o, (long)String.format(Locale.ROOT, "%X", p).length());
                }
                long q = ls.length;
                NbtHelper.appendIndent(depth, stringBuilder).append("long[").append(q).append("] {\n");
                if (withArrayContents) {
                    NbtHelper.appendIndent(depth + 1, stringBuilder);
                    for (n = 0; n < ls.length; ++n) {
                        if (n != 0) {
                            stringBuilder.append(',');
                        }
                        if (n % 16 == 0 && n / 16 > 0) {
                            stringBuilder.append('\n');
                            if (n < ls.length) {
                                NbtHelper.appendIndent(depth + 1, stringBuilder);
                            }
                        } else if (n != 0) {
                            stringBuilder.append(' ');
                        }
                        stringBuilder.append(String.format(Locale.ROOT, "0x%0" + o + "X", ls[n]));
                    }
                } else {
                    NbtHelper.appendIndent(depth + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
                }
                stringBuilder.append('\n');
                NbtHelper.appendIndent(depth, stringBuilder).append('}');
                break;
            }
            default: {
                stringBuilder.append("<UNKNOWN :(>");
            }
        }
        return stringBuilder;
    }

    private static StringBuilder appendIndent(int depth, StringBuilder stringBuilder) {
        int j = stringBuilder.lastIndexOf("\n") + 1;
        int k = stringBuilder.length() - j;
        for (int l = 0; l < 2 * depth - k; ++l) {
            stringBuilder.append(' ');
        }
        return stringBuilder;
    }

    public static Text toPrettyPrintedText(NbtElement element) {
        return new NbtTextFormatter("").apply(element);
    }

    public static String toNbtProviderString(NbtCompound compound) {
        return new NbtOrderedStringFormatter().apply(NbtHelper.toNbtProviderFormat(compound));
    }

    public static NbtCompound fromNbtProviderString(String string) throws CommandSyntaxException {
        return NbtHelper.fromNbtProviderFormat(StringNbtReader.parse(string));
    }

    @VisibleForTesting
    static NbtCompound toNbtProviderFormat(NbtCompound compound) {
        NbtList lv4;
        NbtList lv3;
        boolean bl = compound.contains("palettes", NbtElement.LIST_TYPE);
        NbtList lv = bl ? compound.getList("palettes", NbtElement.LIST_TYPE).getList(0) : compound.getList("palette", NbtElement.COMPOUND_TYPE);
        NbtList lv2 = lv.stream().map(NbtCompound.class::cast).map(NbtHelper::toNbtProviderFormattedPalette).map(NbtString::of).collect(Collectors.toCollection(NbtList::new));
        compound.put("palette", lv2);
        if (bl) {
            lv3 = new NbtList();
            lv4 = compound.getList("palettes", NbtElement.LIST_TYPE);
            lv4.stream().map(NbtList.class::cast).forEach(nbt -> {
                NbtCompound lv = new NbtCompound();
                for (int i = 0; i < nbt.size(); ++i) {
                    lv.putString(lv2.getString(i), NbtHelper.toNbtProviderFormattedPalette(nbt.getCompound(i)));
                }
                lv3.add(lv);
            });
            compound.put("palettes", lv3);
        }
        if (compound.contains("entities", NbtElement.LIST_TYPE)) {
            lv3 = compound.getList("entities", NbtElement.COMPOUND_TYPE);
            lv4 = lv3.stream().map(NbtCompound.class::cast).sorted(Comparator.comparing(nbt -> nbt.getList("pos", NbtElement.DOUBLE_TYPE), ENTITY_POS_COMPARATOR)).collect(Collectors.toCollection(NbtList::new));
            compound.put("entities", lv4);
        }
        lv3 = compound.getList("blocks", NbtElement.COMPOUND_TYPE).stream().map(NbtCompound.class::cast).sorted(Comparator.comparing(nbt -> nbt.getList("pos", NbtElement.INT_TYPE), BLOCK_POS_COMPARATOR)).peek(nbt -> nbt.putString("state", lv2.getString(nbt.getInt("state")))).collect(Collectors.toCollection(NbtList::new));
        compound.put(DATA_KEY, lv3);
        compound.remove("blocks");
        return compound;
    }

    @VisibleForTesting
    static NbtCompound fromNbtProviderFormat(NbtCompound compound) {
        NbtList lv = compound.getList("palette", NbtElement.STRING_TYPE);
        Map map = lv.stream().map(NbtString.class::cast).map(NbtString::asString).collect(ImmutableMap.toImmutableMap(Function.identity(), NbtHelper::fromNbtProviderFormattedPalette));
        if (compound.contains("palettes", NbtElement.LIST_TYPE)) {
            compound.put("palettes", compound.getList("palettes", NbtElement.COMPOUND_TYPE).stream().map(NbtCompound.class::cast).map(nbt -> map.keySet().stream().map(nbt::getString).map(NbtHelper::fromNbtProviderFormattedPalette).collect(Collectors.toCollection(NbtList::new))).collect(Collectors.toCollection(NbtList::new)));
            compound.remove("palette");
        } else {
            compound.put("palette", map.values().stream().collect(Collectors.toCollection(NbtList::new)));
        }
        if (compound.contains(DATA_KEY, NbtElement.LIST_TYPE)) {
            Object2IntOpenHashMap<String> object2IntMap = new Object2IntOpenHashMap<String>();
            object2IntMap.defaultReturnValue(-1);
            for (int i = 0; i < lv.size(); ++i) {
                object2IntMap.put(lv.getString(i), i);
            }
            NbtList lv2 = compound.getList(DATA_KEY, NbtElement.COMPOUND_TYPE);
            for (int j = 0; j < lv2.size(); ++j) {
                NbtCompound lv3 = lv2.getCompound(j);
                String string = lv3.getString("state");
                int k = object2IntMap.getInt(string);
                if (k == -1) {
                    throw new IllegalStateException("Entry " + string + " missing from palette");
                }
                lv3.putInt("state", k);
            }
            compound.put("blocks", lv2);
            compound.remove(DATA_KEY);
        }
        return compound;
    }

    @VisibleForTesting
    static String toNbtProviderFormattedPalette(NbtCompound compound) {
        StringBuilder stringBuilder = new StringBuilder(compound.getString("Name"));
        if (compound.contains("Properties", NbtElement.COMPOUND_TYPE)) {
            NbtCompound lv = compound.getCompound("Properties");
            String string = lv.getKeys().stream().sorted().map(key -> key + ":" + lv.get((String)key).asString()).collect(Collectors.joining(COMMA));
            stringBuilder.append('{').append(string).append('}');
        }
        return stringBuilder.toString();
    }

    @VisibleForTesting
    static NbtCompound fromNbtProviderFormattedPalette(String string) {
        String string2;
        NbtCompound lv = new NbtCompound();
        int i = string.indexOf(123);
        if (i >= 0) {
            string2 = string.substring(0, i);
            NbtCompound lv2 = new NbtCompound();
            if (i + 2 <= string.length()) {
                String string3 = string.substring(i + 1, string.indexOf(125, i));
                COMMA_SPLITTER.split(string3).forEach(property -> {
                    List<String> list = COLON_SPLITTER.splitToList((CharSequence)property);
                    if (list.size() == 2) {
                        lv2.putString(list.get(0), list.get(1));
                    } else {
                        LOGGER.error("Something went wrong parsing: '{}' -- incorrect gamedata!", (Object)string);
                    }
                });
                lv.put("Properties", lv2);
            }
        } else {
            string2 = string;
        }
        lv.putString("Name", string2);
        return lv;
    }

    public static NbtCompound putDataVersion(NbtCompound nbt) {
        int i = SharedConstants.getGameVersion().getSaveVersion().getId();
        return NbtHelper.putDataVersion(nbt, i);
    }

    public static NbtCompound putDataVersion(NbtCompound nbt, int dataVersion) {
        nbt.putInt("DataVersion", dataVersion);
        return nbt;
    }

    public static int getDataVersion(NbtCompound nbt, int fallback) {
        return nbt.contains("DataVersion", NbtElement.NUMBER_TYPE) ? nbt.getInt("DataVersion") : fallback;
    }
}

