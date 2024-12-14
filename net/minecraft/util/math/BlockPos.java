/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Unmodifiable
 */
package net.minecraft.util.math;

import com.google.common.collect.AbstractIterator;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisCycleDirection;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;

@Unmodifiable
public class BlockPos
extends Vec3i {
    public static final Codec<BlockPos> CODEC = Codec.INT_STREAM.comapFlatMap(stream -> Util.decodeFixedLengthArray(stream, 3).map(values -> new BlockPos(values[0], values[1], values[2])), pos -> IntStream.of(pos.getX(), pos.getY(), pos.getZ())).stable();
    public static final PacketCodec<ByteBuf, BlockPos> PACKET_CODEC = new PacketCodec<ByteBuf, BlockPos>(){

        @Override
        public BlockPos decode(ByteBuf byteBuf) {
            return PacketByteBuf.readBlockPos(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, BlockPos arg) {
            PacketByteBuf.writeBlockPos(byteBuf, arg);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (BlockPos)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final BlockPos ORIGIN = new BlockPos(0, 0, 0);
    private static final int SIZE_BITS_X;
    private static final int SIZE_BITS_Z;
    public static final int SIZE_BITS_Y;
    private static final long BITS_X;
    private static final long BITS_Y;
    private static final long BITS_Z;
    private static final int field_33083 = 0;
    private static final int BIT_SHIFT_Z;
    private static final int BIT_SHIFT_X;

    public BlockPos(int i, int j, int k) {
        super(i, j, k);
    }

    public BlockPos(Vec3i pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public static long offset(long value, Direction direction) {
        return BlockPos.add(value, direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ());
    }

    public static long add(long value, int x, int y, int z) {
        return BlockPos.asLong(BlockPos.unpackLongX(value) + x, BlockPos.unpackLongY(value) + y, BlockPos.unpackLongZ(value) + z);
    }

    public static int unpackLongX(long packedPos) {
        return (int)(packedPos << 64 - BIT_SHIFT_X - SIZE_BITS_X >> 64 - SIZE_BITS_X);
    }

    public static int unpackLongY(long packedPos) {
        return (int)(packedPos << 64 - SIZE_BITS_Y >> 64 - SIZE_BITS_Y);
    }

    public static int unpackLongZ(long packedPos) {
        return (int)(packedPos << 64 - BIT_SHIFT_Z - SIZE_BITS_Z >> 64 - SIZE_BITS_Z);
    }

    public static BlockPos fromLong(long packedPos) {
        return new BlockPos(BlockPos.unpackLongX(packedPos), BlockPos.unpackLongY(packedPos), BlockPos.unpackLongZ(packedPos));
    }

    public static BlockPos ofFloored(double x, double y, double z) {
        return new BlockPos(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
    }

    public static BlockPos ofFloored(Position pos) {
        return BlockPos.ofFloored(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPos min(BlockPos a, BlockPos b) {
        return new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
    }

    public static BlockPos max(BlockPos a, BlockPos b) {
        return new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
    }

    public long asLong() {
        return BlockPos.asLong(this.getX(), this.getY(), this.getZ());
    }

    public static long asLong(int x, int y, int z) {
        long l = 0L;
        l |= ((long)x & BITS_X) << BIT_SHIFT_X;
        l |= ((long)y & BITS_Y) << 0;
        return l |= ((long)z & BITS_Z) << BIT_SHIFT_Z;
    }

    public static long removeChunkSectionLocalY(long y) {
        return y & 0xFFFFFFFFFFFFFFF0L;
    }

    @Override
    public BlockPos add(int i, int j, int k) {
        if (i == 0 && j == 0 && k == 0) {
            return this;
        }
        return new BlockPos(this.getX() + i, this.getY() + j, this.getZ() + k);
    }

    public Vec3d toCenterPos() {
        return Vec3d.ofCenter(this);
    }

    public Vec3d toBottomCenterPos() {
        return Vec3d.ofBottomCenter(this);
    }

    @Override
    public BlockPos add(Vec3i arg) {
        return this.add(arg.getX(), arg.getY(), arg.getZ());
    }

    @Override
    public BlockPos subtract(Vec3i arg) {
        return this.add(-arg.getX(), -arg.getY(), -arg.getZ());
    }

    @Override
    public BlockPos multiply(int i) {
        if (i == 1) {
            return this;
        }
        if (i == 0) {
            return ORIGIN;
        }
        return new BlockPos(this.getX() * i, this.getY() * i, this.getZ() * i);
    }

    @Override
    public BlockPos up() {
        return this.offset(Direction.UP);
    }

    @Override
    public BlockPos up(int distance) {
        return this.offset(Direction.UP, distance);
    }

    @Override
    public BlockPos down() {
        return this.offset(Direction.DOWN);
    }

    @Override
    public BlockPos down(int i) {
        return this.offset(Direction.DOWN, i);
    }

    @Override
    public BlockPos north() {
        return this.offset(Direction.NORTH);
    }

    @Override
    public BlockPos north(int distance) {
        return this.offset(Direction.NORTH, distance);
    }

    @Override
    public BlockPos south() {
        return this.offset(Direction.SOUTH);
    }

    @Override
    public BlockPos south(int distance) {
        return this.offset(Direction.SOUTH, distance);
    }

    @Override
    public BlockPos west() {
        return this.offset(Direction.WEST);
    }

    @Override
    public BlockPos west(int distance) {
        return this.offset(Direction.WEST, distance);
    }

    @Override
    public BlockPos east() {
        return this.offset(Direction.EAST);
    }

    @Override
    public BlockPos east(int distance) {
        return this.offset(Direction.EAST, distance);
    }

    @Override
    public BlockPos offset(Direction arg) {
        return new BlockPos(this.getX() + arg.getOffsetX(), this.getY() + arg.getOffsetY(), this.getZ() + arg.getOffsetZ());
    }

    @Override
    public BlockPos offset(Direction arg, int i) {
        if (i == 0) {
            return this;
        }
        return new BlockPos(this.getX() + arg.getOffsetX() * i, this.getY() + arg.getOffsetY() * i, this.getZ() + arg.getOffsetZ() * i);
    }

    @Override
    public BlockPos offset(Direction.Axis arg, int i) {
        if (i == 0) {
            return this;
        }
        int j = arg == Direction.Axis.X ? i : 0;
        int k = arg == Direction.Axis.Y ? i : 0;
        int l = arg == Direction.Axis.Z ? i : 0;
        return new BlockPos(this.getX() + j, this.getY() + k, this.getZ() + l);
    }

    public BlockPos rotate(BlockRotation rotation) {
        switch (rotation) {
            default: {
                return this;
            }
            case CLOCKWISE_90: {
                return new BlockPos(-this.getZ(), this.getY(), this.getX());
            }
            case CLOCKWISE_180: {
                return new BlockPos(-this.getX(), this.getY(), -this.getZ());
            }
            case COUNTERCLOCKWISE_90: 
        }
        return new BlockPos(this.getZ(), this.getY(), -this.getX());
    }

    @Override
    public BlockPos crossProduct(Vec3i pos) {
        return new BlockPos(this.getY() * pos.getZ() - this.getZ() * pos.getY(), this.getZ() * pos.getX() - this.getX() * pos.getZ(), this.getX() * pos.getY() - this.getY() * pos.getX());
    }

    public BlockPos withY(int y) {
        return new BlockPos(this.getX(), y, this.getZ());
    }

    public BlockPos toImmutable() {
        return this;
    }

    public Mutable mutableCopy() {
        return new Mutable(this.getX(), this.getY(), this.getZ());
    }

    public Vec3d clampToWithin(Vec3d pos) {
        return new Vec3d(MathHelper.clamp(pos.x, (double)((float)this.getX() + 1.0E-5f), (double)this.getX() + 1.0 - (double)1.0E-5f), MathHelper.clamp(pos.y, (double)((float)this.getY() + 1.0E-5f), (double)this.getY() + 1.0 - (double)1.0E-5f), MathHelper.clamp(pos.z, (double)((float)this.getZ() + 1.0E-5f), (double)this.getZ() + 1.0 - (double)1.0E-5f));
    }

    public static Iterable<BlockPos> iterateRandomly(Random random, int count, BlockPos around, int range) {
        return BlockPos.iterateRandomly(random, count, around.getX() - range, around.getY() - range, around.getZ() - range, around.getX() + range, around.getY() + range, around.getZ() + range);
    }

    @Deprecated
    public static Stream<BlockPos> streamSouthEastSquare(BlockPos pos) {
        return Stream.of(pos, pos.south(), pos.east(), pos.south().east());
    }

    public static Iterable<BlockPos> iterateRandomly(final Random random, final int count, final int minX, final int minY, final int minZ, int maxX, int maxY, int maxZ) {
        final int p = maxX - minX + 1;
        final int q = maxY - minY + 1;
        final int r = maxZ - minZ + 1;
        return () -> new AbstractIterator<BlockPos>(){
            final Mutable pos = new Mutable();
            int remaining = count;

            @Override
            protected BlockPos computeNext() {
                if (this.remaining <= 0) {
                    return (BlockPos)this.endOfData();
                }
                Mutable lv = this.pos.set(minX + random.nextInt(p), minY + random.nextInt(q), minZ + random.nextInt(r));
                --this.remaining;
                return lv;
            }

            @Override
            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static Iterable<BlockPos> iterateOutwards(BlockPos center, final int rangeX, final int rangeY, final int rangeZ) {
        final int l = rangeX + rangeY + rangeZ;
        final int m = center.getX();
        final int n = center.getY();
        final int o = center.getZ();
        return () -> new AbstractIterator<BlockPos>(){
            private final Mutable pos = new Mutable();
            private int manhattanDistance;
            private int limitX;
            private int limitY;
            private int dx;
            private int dy;
            private boolean swapZ;

            @Override
            protected BlockPos computeNext() {
                if (this.swapZ) {
                    this.swapZ = false;
                    this.pos.setZ(o - (this.pos.getZ() - o));
                    return this.pos;
                }
                Mutable lv = null;
                while (lv == null) {
                    if (this.dy > this.limitY) {
                        ++this.dx;
                        if (this.dx > this.limitX) {
                            ++this.manhattanDistance;
                            if (this.manhattanDistance > l) {
                                return (BlockPos)this.endOfData();
                            }
                            this.limitX = Math.min(rangeX, this.manhattanDistance);
                            this.dx = -this.limitX;
                        }
                        this.limitY = Math.min(rangeY, this.manhattanDistance - Math.abs(this.dx));
                        this.dy = -this.limitY;
                    }
                    int i = this.dx;
                    int j = this.dy;
                    int k = this.manhattanDistance - Math.abs(i) - Math.abs(j);
                    if (k <= rangeZ) {
                        this.swapZ = k != 0;
                        lv = this.pos.set(m + i, n + j, o + k);
                    }
                    ++this.dy;
                }
                return lv;
            }

            @Override
            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static Optional<BlockPos> findClosest(BlockPos pos, int horizontalRange, int verticalRange, Predicate<BlockPos> condition) {
        for (BlockPos lv : BlockPos.iterateOutwards(pos, horizontalRange, verticalRange, horizontalRange)) {
            if (!condition.test(lv)) continue;
            return Optional.of(lv);
        }
        return Optional.empty();
    }

    public static Stream<BlockPos> streamOutwards(BlockPos center, int maxX, int maxY, int maxZ) {
        return StreamSupport.stream(BlockPos.iterateOutwards(center, maxX, maxY, maxZ).spliterator(), false);
    }

    public static Iterable<BlockPos> iterate(BlockPos start, BlockPos end) {
        return BlockPos.iterate(Math.min(start.getX(), end.getX()), Math.min(start.getY(), end.getY()), Math.min(start.getZ(), end.getZ()), Math.max(start.getX(), end.getX()), Math.max(start.getY(), end.getY()), Math.max(start.getZ(), end.getZ()));
    }

    public static Stream<BlockPos> stream(BlockPos start, BlockPos end) {
        return StreamSupport.stream(BlockPos.iterate(start, end).spliterator(), false);
    }

    public static Stream<BlockPos> stream(BlockBox box) {
        return BlockPos.stream(Math.min(box.getMinX(), box.getMaxX()), Math.min(box.getMinY(), box.getMaxY()), Math.min(box.getMinZ(), box.getMaxZ()), Math.max(box.getMinX(), box.getMaxX()), Math.max(box.getMinY(), box.getMaxY()), Math.max(box.getMinZ(), box.getMaxZ()));
    }

    public static Stream<BlockPos> stream(Box box) {
        return BlockPos.stream(MathHelper.floor(box.minX), MathHelper.floor(box.minY), MathHelper.floor(box.minZ), MathHelper.floor(box.maxX), MathHelper.floor(box.maxY), MathHelper.floor(box.maxZ));
    }

    public static Stream<BlockPos> stream(int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return StreamSupport.stream(BlockPos.iterate(startX, startY, startZ, endX, endY, endZ).spliterator(), false);
    }

    public static Iterable<BlockPos> iterate(final int startX, final int startY, final int startZ, int endX, int endY, int endZ) {
        final int o = endX - startX + 1;
        final int p = endY - startY + 1;
        int q = endZ - startZ + 1;
        final int r = o * p * q;
        return () -> new AbstractIterator<BlockPos>(){
            private final Mutable pos = new Mutable();
            private int index;

            @Override
            protected BlockPos computeNext() {
                if (this.index == r) {
                    return (BlockPos)this.endOfData();
                }
                int i = this.index % o;
                int j = this.index / o;
                int k = j % p;
                int l = j / p;
                ++this.index;
                return this.pos.set(startX + i, startY + k, startZ + l);
            }

            @Override
            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static Iterable<Mutable> iterateInSquare(final BlockPos center, final int radius, final Direction firstDirection, final Direction secondDirection) {
        Validate.validState(firstDirection.getAxis() != secondDirection.getAxis(), "The two directions cannot be on the same axis", new Object[0]);
        return () -> new AbstractIterator<Mutable>(){
            private final Direction[] directions;
            private final Mutable pos;
            private final int maxDirectionChanges;
            private int directionChangeCount;
            private int maxSteps;
            private int steps;
            private int currentX;
            private int currentY;
            private int currentZ;
            {
                this.directions = new Direction[]{firstDirection, secondDirection, firstDirection.getOpposite(), secondDirection.getOpposite()};
                this.pos = center.mutableCopy().move(secondDirection);
                this.maxDirectionChanges = 4 * radius;
                this.directionChangeCount = -1;
                this.currentX = this.pos.getX();
                this.currentY = this.pos.getY();
                this.currentZ = this.pos.getZ();
            }

            @Override
            protected Mutable computeNext() {
                this.pos.set(this.currentX, this.currentY, this.currentZ).move(this.directions[(this.directionChangeCount + 4) % 4]);
                this.currentX = this.pos.getX();
                this.currentY = this.pos.getY();
                this.currentZ = this.pos.getZ();
                if (this.steps >= this.maxSteps) {
                    if (this.directionChangeCount >= this.maxDirectionChanges) {
                        return (Mutable)this.endOfData();
                    }
                    ++this.directionChangeCount;
                    this.steps = 0;
                    this.maxSteps = this.directionChangeCount / 2 + 1;
                }
                ++this.steps;
                return this.pos;
            }

            @Override
            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static int iterateRecursively(BlockPos pos, int maxDepth, int maxIterations, BiConsumer<BlockPos, Consumer<BlockPos>> nextQueuer, Predicate<BlockPos> callback) {
        ArrayDeque<Pair<BlockPos, Integer>> queue = new ArrayDeque<Pair<BlockPos, Integer>>();
        LongOpenHashSet longSet = new LongOpenHashSet();
        queue.add(Pair.of(pos, 0));
        int k = 0;
        while (!queue.isEmpty()) {
            Pair pair = (Pair)queue.poll();
            BlockPos lv = (BlockPos)pair.getLeft();
            int l = (Integer)pair.getRight();
            long m = lv.asLong();
            if (!longSet.add(m) || !callback.test(lv)) continue;
            if (++k >= maxIterations) {
                return k;
            }
            if (l >= maxDepth) continue;
            nextQueuer.accept(lv, queuedPos -> queue.add(Pair.of(queuedPos, l + 1)));
        }
        return k;
    }

    @Override
    public /* synthetic */ Vec3i crossProduct(Vec3i vec) {
        return this.crossProduct(vec);
    }

    @Override
    public /* synthetic */ Vec3i offset(Direction.Axis axis, int distance) {
        return this.offset(axis, distance);
    }

    @Override
    public /* synthetic */ Vec3i offset(Direction direction, int distance) {
        return this.offset(direction, distance);
    }

    @Override
    public /* synthetic */ Vec3i offset(Direction direction) {
        return this.offset(direction);
    }

    @Override
    public /* synthetic */ Vec3i east(int distance) {
        return this.east(distance);
    }

    @Override
    public /* synthetic */ Vec3i east() {
        return this.east();
    }

    @Override
    public /* synthetic */ Vec3i west(int distance) {
        return this.west(distance);
    }

    @Override
    public /* synthetic */ Vec3i west() {
        return this.west();
    }

    @Override
    public /* synthetic */ Vec3i south(int distance) {
        return this.south(distance);
    }

    @Override
    public /* synthetic */ Vec3i south() {
        return this.south();
    }

    @Override
    public /* synthetic */ Vec3i north(int distance) {
        return this.north(distance);
    }

    @Override
    public /* synthetic */ Vec3i north() {
        return this.north();
    }

    @Override
    public /* synthetic */ Vec3i down(int distance) {
        return this.down(distance);
    }

    @Override
    public /* synthetic */ Vec3i down() {
        return this.down();
    }

    @Override
    public /* synthetic */ Vec3i up(int distance) {
        return this.up(distance);
    }

    @Override
    public /* synthetic */ Vec3i up() {
        return this.up();
    }

    @Override
    public /* synthetic */ Vec3i multiply(int scale) {
        return this.multiply(scale);
    }

    @Override
    public /* synthetic */ Vec3i subtract(Vec3i vec) {
        return this.subtract(vec);
    }

    @Override
    public /* synthetic */ Vec3i add(Vec3i vec) {
        return this.add(vec);
    }

    @Override
    public /* synthetic */ Vec3i add(int x, int y, int z) {
        return this.add(x, y, z);
    }

    static {
        SIZE_BITS_Z = SIZE_BITS_X = 1 + MathHelper.floorLog2(MathHelper.smallestEncompassingPowerOfTwo(30000000));
        SIZE_BITS_Y = 64 - SIZE_BITS_X - SIZE_BITS_Z;
        BITS_X = (1L << SIZE_BITS_X) - 1L;
        BITS_Y = (1L << SIZE_BITS_Y) - 1L;
        BITS_Z = (1L << SIZE_BITS_Z) - 1L;
        BIT_SHIFT_Z = SIZE_BITS_Y;
        BIT_SHIFT_X = SIZE_BITS_Y + SIZE_BITS_Z;
    }

    public static class Mutable
    extends BlockPos {
        public Mutable() {
            this(0, 0, 0);
        }

        public Mutable(int i, int j, int k) {
            super(i, j, k);
        }

        public Mutable(double x, double y, double z) {
            this(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
        }

        @Override
        public BlockPos add(int i, int j, int k) {
            return super.add(i, j, k).toImmutable();
        }

        @Override
        public BlockPos multiply(int i) {
            return super.multiply(i).toImmutable();
        }

        @Override
        public BlockPos offset(Direction arg, int i) {
            return super.offset(arg, i).toImmutable();
        }

        @Override
        public BlockPos offset(Direction.Axis arg, int i) {
            return super.offset(arg, i).toImmutable();
        }

        @Override
        public BlockPos rotate(BlockRotation rotation) {
            return super.rotate(rotation).toImmutable();
        }

        public Mutable set(int x, int y, int z) {
            this.setX(x);
            this.setY(y);
            this.setZ(z);
            return this;
        }

        public Mutable set(double x, double y, double z) {
            return this.set(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
        }

        public Mutable set(Vec3i pos) {
            return this.set(pos.getX(), pos.getY(), pos.getZ());
        }

        public Mutable set(long pos) {
            return this.set(Mutable.unpackLongX(pos), Mutable.unpackLongY(pos), Mutable.unpackLongZ(pos));
        }

        public Mutable set(AxisCycleDirection axis, int x, int y, int z) {
            return this.set(axis.choose(x, y, z, Direction.Axis.X), axis.choose(x, y, z, Direction.Axis.Y), axis.choose(x, y, z, Direction.Axis.Z));
        }

        public Mutable set(Vec3i pos, Direction direction) {
            return this.set(pos.getX() + direction.getOffsetX(), pos.getY() + direction.getOffsetY(), pos.getZ() + direction.getOffsetZ());
        }

        public Mutable set(Vec3i pos, int x, int y, int z) {
            return this.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
        }

        public Mutable set(Vec3i vec1, Vec3i vec2) {
            return this.set(vec1.getX() + vec2.getX(), vec1.getY() + vec2.getY(), vec1.getZ() + vec2.getZ());
        }

        public Mutable move(Direction direction) {
            return this.move(direction, 1);
        }

        public Mutable move(Direction direction, int distance) {
            return this.set(this.getX() + direction.getOffsetX() * distance, this.getY() + direction.getOffsetY() * distance, this.getZ() + direction.getOffsetZ() * distance);
        }

        public Mutable move(int dx, int dy, int dz) {
            return this.set(this.getX() + dx, this.getY() + dy, this.getZ() + dz);
        }

        public Mutable move(Vec3i vec) {
            return this.set(this.getX() + vec.getX(), this.getY() + vec.getY(), this.getZ() + vec.getZ());
        }

        public Mutable clamp(Direction.Axis axis, int min, int max) {
            switch (axis) {
                case X: {
                    return this.set(MathHelper.clamp(this.getX(), min, max), this.getY(), this.getZ());
                }
                case Y: {
                    return this.set(this.getX(), MathHelper.clamp(this.getY(), min, max), this.getZ());
                }
                case Z: {
                    return this.set(this.getX(), this.getY(), MathHelper.clamp(this.getZ(), min, max));
                }
            }
            throw new IllegalStateException("Unable to clamp axis " + String.valueOf(axis));
        }

        @Override
        public Mutable setX(int i) {
            super.setX(i);
            return this;
        }

        @Override
        public Mutable setY(int i) {
            super.setY(i);
            return this;
        }

        @Override
        public Mutable setZ(int i) {
            super.setZ(i);
            return this;
        }

        @Override
        public BlockPos toImmutable() {
            return new BlockPos(this);
        }

        @Override
        public /* synthetic */ Vec3i crossProduct(Vec3i vec) {
            return super.crossProduct(vec);
        }

        @Override
        public /* synthetic */ Vec3i offset(Direction.Axis axis, int distance) {
            return this.offset(axis, distance);
        }

        @Override
        public /* synthetic */ Vec3i offset(Direction direction, int distance) {
            return this.offset(direction, distance);
        }

        @Override
        public /* synthetic */ Vec3i offset(Direction direction) {
            return super.offset(direction);
        }

        @Override
        public /* synthetic */ Vec3i east(int distance) {
            return super.east(distance);
        }

        @Override
        public /* synthetic */ Vec3i east() {
            return super.east();
        }

        @Override
        public /* synthetic */ Vec3i west(int distance) {
            return super.west(distance);
        }

        @Override
        public /* synthetic */ Vec3i west() {
            return super.west();
        }

        @Override
        public /* synthetic */ Vec3i south(int distance) {
            return super.south(distance);
        }

        @Override
        public /* synthetic */ Vec3i south() {
            return super.south();
        }

        @Override
        public /* synthetic */ Vec3i north(int distance) {
            return super.north(distance);
        }

        @Override
        public /* synthetic */ Vec3i north() {
            return super.north();
        }

        @Override
        public /* synthetic */ Vec3i down(int distance) {
            return super.down(distance);
        }

        @Override
        public /* synthetic */ Vec3i down() {
            return super.down();
        }

        @Override
        public /* synthetic */ Vec3i up(int distance) {
            return super.up(distance);
        }

        @Override
        public /* synthetic */ Vec3i up() {
            return super.up();
        }

        @Override
        public /* synthetic */ Vec3i multiply(int scale) {
            return this.multiply(scale);
        }

        @Override
        public /* synthetic */ Vec3i subtract(Vec3i vec) {
            return super.subtract(vec);
        }

        @Override
        public /* synthetic */ Vec3i add(Vec3i vec) {
            return super.add(vec);
        }

        @Override
        public /* synthetic */ Vec3i add(int x, int y, int z) {
            return this.add(x, y, z);
        }

        @Override
        public /* synthetic */ Vec3i setZ(int z) {
            return this.setZ(z);
        }

        @Override
        public /* synthetic */ Vec3i setY(int y) {
            return this.setY(y);
        }

        @Override
        public /* synthetic */ Vec3i setX(int x) {
            return this.setX(x);
        }
    }
}

