/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Set;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

public enum EightWayDirection {
    NORTH(Direction.NORTH),
    NORTH_EAST(Direction.NORTH, Direction.EAST),
    EAST(Direction.EAST),
    SOUTH_EAST(Direction.SOUTH, Direction.EAST),
    SOUTH(Direction.SOUTH),
    SOUTH_WEST(Direction.SOUTH, Direction.WEST),
    WEST(Direction.WEST),
    NORTH_WEST(Direction.NORTH, Direction.WEST);

    private final Set<Direction> directions;
    private final Vec3i offset;

    private EightWayDirection(Direction ... directions) {
        this.directions = Sets.immutableEnumSet(Arrays.asList(directions));
        this.offset = new Vec3i(0, 0, 0);
        for (Direction lv : directions) {
            this.offset.setX(this.offset.getX() + lv.getOffsetX()).setY(this.offset.getY() + lv.getOffsetY()).setZ(this.offset.getZ() + lv.getOffsetZ());
        }
    }

    public Set<Direction> getDirections() {
        return this.directions;
    }

    public int getOffsetX() {
        return this.offset.getX();
    }

    public int getOffsetZ() {
        return this.offset.getZ();
    }
}

