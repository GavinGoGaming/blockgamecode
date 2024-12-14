/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.CoordinateArgument;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class DefaultPosArgument
implements PosArgument {
    private final CoordinateArgument x;
    private final CoordinateArgument y;
    private final CoordinateArgument z;

    public DefaultPosArgument(CoordinateArgument x, CoordinateArgument y, CoordinateArgument z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public Vec3d toAbsolutePos(ServerCommandSource source) {
        Vec3d lv = source.getPosition();
        return new Vec3d(this.x.toAbsoluteCoordinate(lv.x), this.y.toAbsoluteCoordinate(lv.y), this.z.toAbsoluteCoordinate(lv.z));
    }

    @Override
    public Vec2f toAbsoluteRotation(ServerCommandSource source) {
        Vec2f lv = source.getRotation();
        return new Vec2f((float)this.x.toAbsoluteCoordinate(lv.x), (float)this.y.toAbsoluteCoordinate(lv.y));
    }

    @Override
    public boolean isXRelative() {
        return this.x.isRelative();
    }

    @Override
    public boolean isYRelative() {
        return this.y.isRelative();
    }

    @Override
    public boolean isZRelative() {
        return this.z.isRelative();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultPosArgument)) {
            return false;
        }
        DefaultPosArgument lv = (DefaultPosArgument)o;
        if (!this.x.equals(lv.x)) {
            return false;
        }
        if (!this.y.equals(lv.y)) {
            return false;
        }
        return this.z.equals(lv.z);
    }

    public static DefaultPosArgument parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        CoordinateArgument lv = CoordinateArgument.parse(reader);
        if (!reader.canRead() || reader.peek() != ' ') {
            reader.setCursor(i);
            throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
        }
        reader.skip();
        CoordinateArgument lv2 = CoordinateArgument.parse(reader);
        if (!reader.canRead() || reader.peek() != ' ') {
            reader.setCursor(i);
            throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
        }
        reader.skip();
        CoordinateArgument lv3 = CoordinateArgument.parse(reader);
        return new DefaultPosArgument(lv, lv2, lv3);
    }

    public static DefaultPosArgument parse(StringReader reader, boolean centerIntegers) throws CommandSyntaxException {
        int i = reader.getCursor();
        CoordinateArgument lv = CoordinateArgument.parse(reader, centerIntegers);
        if (!reader.canRead() || reader.peek() != ' ') {
            reader.setCursor(i);
            throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
        }
        reader.skip();
        CoordinateArgument lv2 = CoordinateArgument.parse(reader, false);
        if (!reader.canRead() || reader.peek() != ' ') {
            reader.setCursor(i);
            throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
        }
        reader.skip();
        CoordinateArgument lv3 = CoordinateArgument.parse(reader, centerIntegers);
        return new DefaultPosArgument(lv, lv2, lv3);
    }

    public static DefaultPosArgument absolute(double x, double y, double z) {
        return new DefaultPosArgument(new CoordinateArgument(false, x), new CoordinateArgument(false, y), new CoordinateArgument(false, z));
    }

    public static DefaultPosArgument absolute(Vec2f vec) {
        return new DefaultPosArgument(new CoordinateArgument(false, vec.x), new CoordinateArgument(false, vec.y), new CoordinateArgument(true, 0.0));
    }

    public static DefaultPosArgument zero() {
        return new DefaultPosArgument(new CoordinateArgument(true, 0.0), new CoordinateArgument(true, 0.0), new CoordinateArgument(true, 0.0));
    }

    public int hashCode() {
        int i = this.x.hashCode();
        i = 31 * i + this.y.hashCode();
        i = 31 * i + this.z.hashCode();
        return i;
    }
}

