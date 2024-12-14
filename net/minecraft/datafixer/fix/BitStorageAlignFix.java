/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.stream.LongStream;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.math.MathHelper;

public class BitStorageAlignFix
extends DataFix {
    private static final int ELEMENT_BIT_SHIFT = 6;
    private static final int CHUNK_WIDTH = 16;
    private static final int CHUNK_LENGTH = 16;
    private static final int MAX_BLOCK_STATE_ID = 4096;
    private static final int HEIGHT_VALUE_BITS = 9;
    private static final int MAX_HEIGHT_VALUE = 256;

    public BitStorageAlignFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.CHUNK);
        Type<?> type2 = type.findFieldType("Level");
        OpticFinder<?> opticFinder = DSL.fieldFinder("Level", type2);
        OpticFinder<?> opticFinder2 = opticFinder.type().findField("Sections");
        Type type3 = ((List.ListType)opticFinder2.type()).getElement();
        OpticFinder opticFinder3 = DSL.typeFinder(type3);
        Type<Pair<String, Dynamic<?>>> type4 = DSL.named(TypeReferences.BLOCK_STATE.typeName(), DSL.remainderType());
        OpticFinder<Pair<String, Dynamic<?>>> opticFinder4 = DSL.fieldFinder("Palette", DSL.list(type4));
        return this.fixTypeEverywhereTyped("BitStorageAlignFix", type, this.getOutputSchema().getType(TypeReferences.CHUNK), (Typed<?> chunk) -> chunk.updateTyped(opticFinder, level -> this.fixHeightmaps(BitStorageAlignFix.fixLevel(opticFinder2, opticFinder3, opticFinder4, level))));
    }

    private Typed<?> fixHeightmaps(Typed<?> fixedLevel) {
        return fixedLevel.update(DSL.remainderFinder(), levelDynamic -> levelDynamic.update("Heightmaps", heightmapsDynamic -> heightmapsDynamic.updateMapValues(heightmap -> heightmap.mapSecond(heightmapDynamic -> BitStorageAlignFix.fixBitStorageArray(levelDynamic, heightmapDynamic, 256, 9)))));
    }

    private static Typed<?> fixLevel(OpticFinder<?> levelSectionsFinder, OpticFinder<?> sectionFinder, OpticFinder<List<Pair<String, Dynamic<?>>>> paletteFinder, Typed<?> level) {
        return level.updateTyped(levelSectionsFinder, levelSection -> levelSection.updateTyped(sectionFinder, section -> {
            int i = section.getOptional(paletteFinder).map(palette -> Math.max(4, DataFixUtils.ceillog2(palette.size()))).orElse(0);
            if (i == 0 || MathHelper.isPowerOfTwo(i)) {
                return section;
            }
            return section.update(DSL.remainderFinder(), sectionDynamic -> sectionDynamic.update("BlockStates", statesDynamic -> BitStorageAlignFix.fixBitStorageArray(sectionDynamic, statesDynamic, 4096, i)));
        }));
    }

    private static Dynamic<?> fixBitStorageArray(Dynamic<?> sectionDynamic, Dynamic<?> statesDynamic, int maxValue, int elementBits) {
        long[] ls = statesDynamic.asLongStream().toArray();
        long[] ms = BitStorageAlignFix.resizePackedIntArray(maxValue, elementBits, ls);
        return sectionDynamic.createLongList(LongStream.of(ms));
    }

    public static long[] resizePackedIntArray(int maxValue, int elementBits, long[] elements) {
        int k = elements.length;
        if (k == 0) {
            return elements;
        }
        long l = (1L << elementBits) - 1L;
        int m = 64 / elementBits;
        int n = (maxValue + m - 1) / m;
        long[] ms = new long[n];
        int o = 0;
        int p = 0;
        long q = 0L;
        int r = 0;
        long s = elements[0];
        long t = k > 1 ? elements[1] : 0L;
        for (int u = 0; u < maxValue; ++u) {
            int aa;
            long z;
            int v = u * elementBits;
            int w = v >> 6;
            int x = (u + 1) * elementBits - 1 >> 6;
            int y = v ^ w << 6;
            if (w != r) {
                s = t;
                t = w + 1 < k ? elements[w + 1] : 0L;
                r = w;
            }
            if (w == x) {
                z = s >>> y & l;
            } else {
                aa = 64 - y;
                z = (s >>> y | t << aa) & l;
            }
            aa = p + elementBits;
            if (aa >= 64) {
                ms[o++] = q;
                q = z;
                p = elementBits;
                continue;
            }
            q |= z << p;
            p = aa;
        }
        if (q != 0L) {
            ms[o] = q;
        }
        return ms;
    }
}

