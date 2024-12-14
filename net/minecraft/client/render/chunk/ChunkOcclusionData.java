/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.chunk;

import java.util.BitSet;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Direction;

@Environment(value=EnvType.CLIENT)
public class ChunkOcclusionData {
    private static final int DIRECTION_COUNT = Direction.values().length;
    private final BitSet visibility = new BitSet(DIRECTION_COUNT * DIRECTION_COUNT);

    public void addOpenEdgeFaces(Set<Direction> faces) {
        for (Direction lv : faces) {
            for (Direction lv2 : faces) {
                this.setVisibleThrough(lv, lv2, true);
            }
        }
    }

    public void setVisibleThrough(Direction from, Direction to, boolean visible) {
        this.visibility.set(from.ordinal() + to.ordinal() * DIRECTION_COUNT, visible);
        this.visibility.set(to.ordinal() + from.ordinal() * DIRECTION_COUNT, visible);
    }

    public void fill(boolean visible) {
        this.visibility.set(0, this.visibility.size(), visible);
    }

    public boolean isVisibleThrough(Direction from, Direction to) {
        return this.visibility.get(from.ordinal() + to.ordinal() * DIRECTION_COUNT);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(' ');
        for (Direction lv : Direction.values()) {
            stringBuilder.append(' ').append(lv.toString().toUpperCase().charAt(0));
        }
        stringBuilder.append('\n');
        for (Direction lv : Direction.values()) {
            stringBuilder.append(lv.toString().toUpperCase().charAt(0));
            for (Direction lv2 : Direction.values()) {
                if (lv == lv2) {
                    stringBuilder.append("  ");
                    continue;
                }
                boolean bl = this.isVisibleThrough(lv, lv2);
                stringBuilder.append(' ').append(bl ? (char)'Y' : 'n');
            }
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }
}

