/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.biome.source.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.jetbrains.annotations.Nullable;

public class MultiNoiseUtil {
    private static final boolean field_34477 = false;
    private static final float TO_LONG_FACTOR = 10000.0f;
    @VisibleForTesting
    protected static final int HYPERCUBE_DIMENSION = 7;

    public static NoiseValuePoint createNoiseValuePoint(float temperatureNoise, float humidityNoise, float continentalnessNoise, float erosionNoise, float depth, float weirdnessNoise) {
        return new NoiseValuePoint(MultiNoiseUtil.toLong(temperatureNoise), MultiNoiseUtil.toLong(humidityNoise), MultiNoiseUtil.toLong(continentalnessNoise), MultiNoiseUtil.toLong(erosionNoise), MultiNoiseUtil.toLong(depth), MultiNoiseUtil.toLong(weirdnessNoise));
    }

    public static NoiseHypercube createNoiseHypercube(float temperature, float humidity, float continentalness, float erosion, float depth, float weirdness, float offset) {
        return new NoiseHypercube(ParameterRange.of(temperature), ParameterRange.of(humidity), ParameterRange.of(continentalness), ParameterRange.of(erosion), ParameterRange.of(depth), ParameterRange.of(weirdness), MultiNoiseUtil.toLong(offset));
    }

    public static NoiseHypercube createNoiseHypercube(ParameterRange temperature, ParameterRange humidity, ParameterRange continentalness, ParameterRange erosion, ParameterRange depth, ParameterRange weirdness, float offset) {
        return new NoiseHypercube(temperature, humidity, continentalness, erosion, depth, weirdness, MultiNoiseUtil.toLong(offset));
    }

    public static long toLong(float value) {
        return (long)(value * 10000.0f);
    }

    public static float toFloat(long value) {
        return (float)value / 10000.0f;
    }

    public static MultiNoiseSampler createEmptyMultiNoiseSampler() {
        DensityFunction lv = DensityFunctionTypes.zero();
        return new MultiNoiseSampler(lv, lv, lv, lv, lv, lv, List.of());
    }

    public static BlockPos findFittestPosition(List<NoiseHypercube> noises, MultiNoiseSampler sampler) {
        return new FittestPositionFinder(noises, (MultiNoiseSampler)sampler).bestResult.location();
    }

    public record NoiseValuePoint(long temperatureNoise, long humidityNoise, long continentalnessNoise, long erosionNoise, long depth, long weirdnessNoise) {
        @VisibleForTesting
        protected long[] getNoiseValueList() {
            return new long[]{this.temperatureNoise, this.humidityNoise, this.continentalnessNoise, this.erosionNoise, this.depth, this.weirdnessNoise, 0L};
        }
    }

    public record NoiseHypercube(ParameterRange temperature, ParameterRange humidity, ParameterRange continentalness, ParameterRange erosion, ParameterRange depth, ParameterRange weirdness, long offset) {
        public static final Codec<NoiseHypercube> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ParameterRange.CODEC.fieldOf("temperature")).forGetter(arg -> arg.temperature), ((MapCodec)ParameterRange.CODEC.fieldOf("humidity")).forGetter(arg -> arg.humidity), ((MapCodec)ParameterRange.CODEC.fieldOf("continentalness")).forGetter(arg -> arg.continentalness), ((MapCodec)ParameterRange.CODEC.fieldOf("erosion")).forGetter(arg -> arg.erosion), ((MapCodec)ParameterRange.CODEC.fieldOf("depth")).forGetter(arg -> arg.depth), ((MapCodec)ParameterRange.CODEC.fieldOf("weirdness")).forGetter(arg -> arg.weirdness), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("offset")).xmap(MultiNoiseUtil::toLong, MultiNoiseUtil::toFloat).forGetter(arg -> arg.offset)).apply((Applicative<NoiseHypercube, ?>)instance, NoiseHypercube::new));

        long getSquaredDistance(NoiseValuePoint point) {
            return MathHelper.square(this.temperature.getDistance(point.temperatureNoise)) + MathHelper.square(this.humidity.getDistance(point.humidityNoise)) + MathHelper.square(this.continentalness.getDistance(point.continentalnessNoise)) + MathHelper.square(this.erosion.getDistance(point.erosionNoise)) + MathHelper.square(this.depth.getDistance(point.depth)) + MathHelper.square(this.weirdness.getDistance(point.weirdnessNoise)) + MathHelper.square(this.offset);
        }

        protected List<ParameterRange> getParameters() {
            return ImmutableList.of(this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, new ParameterRange(this.offset, this.offset));
        }
    }

    public record ParameterRange(long min, long max) {
        public static final Codec<ParameterRange> CODEC = Codecs.createCodecForPairObject(Codec.floatRange(-2.0f, 2.0f), "min", "max", (min, max) -> {
            if (min.compareTo((Float)max) > 0) {
                return DataResult.error(() -> "Cannon construct interval, min > max (" + min + " > " + max + ")");
            }
            return DataResult.success(new ParameterRange(MultiNoiseUtil.toLong(min.floatValue()), MultiNoiseUtil.toLong(max.floatValue())));
        }, arg -> Float.valueOf(MultiNoiseUtil.toFloat(arg.min())), arg -> Float.valueOf(MultiNoiseUtil.toFloat(arg.max())));

        public static ParameterRange of(float point) {
            return ParameterRange.of(point, point);
        }

        public static ParameterRange of(float min, float max) {
            if (min > max) {
                throw new IllegalArgumentException("min > max: " + min + " " + max);
            }
            return new ParameterRange(MultiNoiseUtil.toLong(min), MultiNoiseUtil.toLong(max));
        }

        public static ParameterRange combine(ParameterRange min, ParameterRange max) {
            if (min.min() > max.max()) {
                throw new IllegalArgumentException("min > max: " + String.valueOf(min) + " " + String.valueOf(max));
            }
            return new ParameterRange(min.min(), max.max());
        }

        @Override
        public String toString() {
            return this.min == this.max ? String.format(Locale.ROOT, "%d", this.min) : String.format(Locale.ROOT, "[%d-%d]", this.min, this.max);
        }

        public long getDistance(long noise) {
            long m = noise - this.max;
            long n = this.min - noise;
            if (m > 0L) {
                return m;
            }
            return Math.max(n, 0L);
        }

        public long getDistance(ParameterRange other) {
            long l = other.min() - this.max;
            long m = this.min - other.max();
            if (l > 0L) {
                return l;
            }
            return Math.max(m, 0L);
        }

        public ParameterRange combine(@Nullable ParameterRange other) {
            return other == null ? this : new ParameterRange(Math.min(this.min, other.min()), Math.max(this.max, other.max()));
        }
    }

    public record MultiNoiseSampler(DensityFunction temperature, DensityFunction humidity, DensityFunction continentalness, DensityFunction erosion, DensityFunction depth, DensityFunction weirdness, List<NoiseHypercube> spawnTarget) {
        public NoiseValuePoint sample(int x, int y, int z) {
            int l = BiomeCoords.toBlock(x);
            int m = BiomeCoords.toBlock(y);
            int n = BiomeCoords.toBlock(z);
            DensityFunction.UnblendedNoisePos lv = new DensityFunction.UnblendedNoisePos(l, m, n);
            return MultiNoiseUtil.createNoiseValuePoint((float)this.temperature.sample(lv), (float)this.humidity.sample(lv), (float)this.continentalness.sample(lv), (float)this.erosion.sample(lv), (float)this.depth.sample(lv), (float)this.weirdness.sample(lv));
        }

        public BlockPos findBestSpawnPosition() {
            if (this.spawnTarget.isEmpty()) {
                return BlockPos.ORIGIN;
            }
            return MultiNoiseUtil.findFittestPosition(this.spawnTarget, this);
        }
    }

    static class FittestPositionFinder {
        Result bestResult;

        FittestPositionFinder(List<NoiseHypercube> noises, MultiNoiseSampler sampler) {
            this.bestResult = FittestPositionFinder.calculateFitness(noises, sampler, 0, 0);
            this.findFittest(noises, sampler, 2048.0f, 512.0f);
            this.findFittest(noises, sampler, 512.0f, 32.0f);
        }

        private void findFittest(List<NoiseHypercube> noises, MultiNoiseSampler sampler, float maxDistance, float step) {
            float h = 0.0f;
            float i = step;
            BlockPos lv = this.bestResult.location();
            while (i <= maxDistance) {
                int k;
                int j = lv.getX() + (int)(Math.sin(h) * (double)i);
                Result lv2 = FittestPositionFinder.calculateFitness(noises, sampler, j, k = lv.getZ() + (int)(Math.cos(h) * (double)i));
                if (lv2.fitness() < this.bestResult.fitness()) {
                    this.bestResult = lv2;
                }
                if (!((double)(h += step / i) > Math.PI * 2)) continue;
                h = 0.0f;
                i += step;
            }
        }

        private static Result calculateFitness(List<NoiseHypercube> noises, MultiNoiseSampler sampler, int x, int z) {
            double d = MathHelper.square(2500.0);
            int k = 2;
            long l = (long)((double)MathHelper.square(10000.0f) * Math.pow((double)(MathHelper.square((long)x) + MathHelper.square((long)z)) / d, 2.0));
            NoiseValuePoint lv = sampler.sample(BiomeCoords.fromBlock(x), 0, BiomeCoords.fromBlock(z));
            NoiseValuePoint lv2 = new NoiseValuePoint(lv.temperatureNoise(), lv.humidityNoise(), lv.continentalnessNoise(), lv.erosionNoise(), 0L, lv.weirdnessNoise());
            long m = Long.MAX_VALUE;
            for (NoiseHypercube lv3 : noises) {
                m = Math.min(m, lv3.getSquaredDistance(lv2));
            }
            return new Result(new BlockPos(x, 0, z), l + m);
        }

        record Result(BlockPos location, long fitness) {
        }
    }

    public static class Entries<T> {
        private final List<Pair<NoiseHypercube, T>> entries;
        private final SearchTree<T> tree;

        public static <T> Codec<Entries<T>> createCodec(MapCodec<T> entryCodec) {
            return Codecs.nonEmptyList(RecordCodecBuilder.create(instance -> instance.group(((MapCodec)NoiseHypercube.CODEC.fieldOf("parameters")).forGetter(Pair::getFirst), entryCodec.forGetter(Pair::getSecond)).apply((Applicative<Pair, ?>)instance, Pair::of)).listOf()).xmap(Entries::new, Entries::getEntries);
        }

        public Entries(List<Pair<NoiseHypercube, T>> entries) {
            this.entries = entries;
            this.tree = SearchTree.create(entries);
        }

        public List<Pair<NoiseHypercube, T>> getEntries() {
            return this.entries;
        }

        public T get(NoiseValuePoint point) {
            return this.getValue(point);
        }

        @VisibleForTesting
        public T getValueSimple(NoiseValuePoint point) {
            Iterator<Pair<NoiseHypercube, T>> iterator = this.getEntries().iterator();
            Pair<NoiseHypercube, T> pair = iterator.next();
            long l = pair.getFirst().getSquaredDistance(point);
            T object = pair.getSecond();
            while (iterator.hasNext()) {
                Pair<NoiseHypercube, T> pair2 = iterator.next();
                long m = pair2.getFirst().getSquaredDistance(point);
                if (m >= l) continue;
                l = m;
                object = pair2.getSecond();
            }
            return object;
        }

        public T getValue(NoiseValuePoint point) {
            return this.getValue(point, SearchTree.TreeNode::getSquaredDistance);
        }

        protected T getValue(NoiseValuePoint point, NodeDistanceFunction<T> distanceFunction) {
            return this.tree.get(point, distanceFunction);
        }
    }

    protected static final class SearchTree<T> {
        private static final int MAX_NODES_FOR_SIMPLE_TREE = 6;
        private final TreeNode<T> firstNode;
        private final ThreadLocal<TreeLeafNode<T>> previousResultNode = new ThreadLocal();

        private SearchTree(TreeNode<T> firstNode) {
            this.firstNode = firstNode;
        }

        public static <T> SearchTree<T> create(List<Pair<NoiseHypercube, T>> entries) {
            if (entries.isEmpty()) {
                throw new IllegalArgumentException("Need at least one value to build the search tree.");
            }
            int i = entries.get(0).getFirst().getParameters().size();
            if (i != 7) {
                throw new IllegalStateException("Expecting parameter space to be 7, got " + i);
            }
            List list2 = entries.stream().map(entry -> new TreeLeafNode((NoiseHypercube)entry.getFirst(), entry.getSecond())).collect(Collectors.toCollection(ArrayList::new));
            return new SearchTree<T>(SearchTree.createNode(i, list2));
        }

        private static <T> TreeNode<T> createNode(int parameterNumber, List<? extends TreeNode<T>> subTree) {
            if (subTree.isEmpty()) {
                throw new IllegalStateException("Need at least one child to build a node");
            }
            if (subTree.size() == 1) {
                return subTree.get(0);
            }
            if (subTree.size() <= 6) {
                subTree.sort(Comparator.comparingLong(node -> {
                    long l = 0L;
                    for (int j = 0; j < parameterNumber; ++j) {
                        ParameterRange lv = node.parameters[j];
                        l += Math.abs((lv.min() + lv.max()) / 2L);
                    }
                    return l;
                }));
                return new TreeBranchNode(subTree);
            }
            long l = Long.MAX_VALUE;
            int j = -1;
            List<TreeBranchNode<T>> list2 = null;
            for (int k = 0; k < parameterNumber; ++k) {
                SearchTree.sortTree(subTree, parameterNumber, k, false);
                List<TreeBranchNode<T>> list3 = SearchTree.getBatchedTree(subTree);
                long m = 0L;
                for (TreeBranchNode<T> lv : list3) {
                    m += SearchTree.getRangeLengthSum(lv.parameters);
                }
                if (l <= m) continue;
                l = m;
                j = k;
                list2 = list3;
            }
            SearchTree.sortTree(list2, parameterNumber, j, true);
            return new TreeBranchNode(list2.stream().map(node -> SearchTree.createNode(parameterNumber, Arrays.asList(node.subTree))).collect(Collectors.toList()));
        }

        private static <T> void sortTree(List<? extends TreeNode<T>> subTree, int parameterNumber, int currentParameter, boolean abs) {
            Comparator<TreeNode<TreeNode<T>>> comparator = SearchTree.createNodeComparator(currentParameter, abs);
            for (int k = 1; k < parameterNumber; ++k) {
                comparator = comparator.thenComparing(SearchTree.createNodeComparator((currentParameter + k) % parameterNumber, abs));
            }
            subTree.sort(comparator);
        }

        private static <T> Comparator<TreeNode<T>> createNodeComparator(int currentParameter, boolean abs) {
            return Comparator.comparingLong(arg -> {
                ParameterRange lv = arg.parameters[currentParameter];
                long l = (lv.min() + lv.max()) / 2L;
                return abs ? Math.abs(l) : l;
            });
        }

        private static <T> List<TreeBranchNode<T>> getBatchedTree(List<? extends TreeNode<T>> nodes) {
            ArrayList<TreeBranchNode<T>> list2 = Lists.newArrayList();
            ArrayList<TreeNode<T>> list3 = Lists.newArrayList();
            int i = (int)Math.pow(6.0, Math.floor(Math.log((double)nodes.size() - 0.01) / Math.log(6.0)));
            for (TreeNode<T> lv : nodes) {
                list3.add(lv);
                if (list3.size() < i) continue;
                list2.add(new TreeBranchNode(list3));
                list3 = Lists.newArrayList();
            }
            if (!list3.isEmpty()) {
                list2.add(new TreeBranchNode(list3));
            }
            return list2;
        }

        private static long getRangeLengthSum(ParameterRange[] parameters) {
            long l = 0L;
            for (ParameterRange lv : parameters) {
                l += Math.abs(lv.max() - lv.min());
            }
            return l;
        }

        static <T> List<ParameterRange> getEnclosingParameters(List<? extends TreeNode<T>> subTree) {
            if (subTree.isEmpty()) {
                throw new IllegalArgumentException("SubTree needs at least one child");
            }
            int i = 7;
            ArrayList<ParameterRange> list2 = Lists.newArrayList();
            for (int j = 0; j < 7; ++j) {
                list2.add(null);
            }
            for (TreeNode<T> lv : subTree) {
                for (int k = 0; k < 7; ++k) {
                    list2.set(k, lv.parameters[k].combine((ParameterRange)list2.get(k)));
                }
            }
            return list2;
        }

        public T get(NoiseValuePoint point, NodeDistanceFunction<T> distanceFunction) {
            long[] ls = point.getNoiseValueList();
            TreeLeafNode<T> lv = this.firstNode.getResultingNode(ls, this.previousResultNode.get(), distanceFunction);
            this.previousResultNode.set(lv);
            return lv.value;
        }

        static abstract class TreeNode<T> {
            protected final ParameterRange[] parameters;

            protected TreeNode(List<ParameterRange> parameters) {
                this.parameters = parameters.toArray(new ParameterRange[0]);
            }

            protected abstract TreeLeafNode<T> getResultingNode(long[] var1, @Nullable TreeLeafNode<T> var2, NodeDistanceFunction<T> var3);

            protected long getSquaredDistance(long[] otherParameters) {
                long l = 0L;
                for (int i = 0; i < 7; ++i) {
                    l += MathHelper.square(this.parameters[i].getDistance(otherParameters[i]));
                }
                return l;
            }

            public String toString() {
                return Arrays.toString(this.parameters);
            }
        }

        static final class TreeBranchNode<T>
        extends TreeNode<T> {
            final TreeNode<T>[] subTree;

            protected TreeBranchNode(List<? extends TreeNode<T>> list) {
                this(SearchTree.getEnclosingParameters(list), list);
            }

            protected TreeBranchNode(List<ParameterRange> parameters, List<? extends TreeNode<T>> subTree) {
                super(parameters);
                this.subTree = subTree.toArray(new TreeNode[0]);
            }

            @Override
            protected TreeLeafNode<T> getResultingNode(long[] otherParameters, @Nullable TreeLeafNode<T> alternative, NodeDistanceFunction<T> distanceFunction) {
                long l = alternative == null ? Long.MAX_VALUE : distanceFunction.getDistance(alternative, otherParameters);
                TreeLeafNode<T> lv = alternative;
                for (TreeNode<T> lv2 : this.subTree) {
                    long n;
                    long m = distanceFunction.getDistance(lv2, otherParameters);
                    if (l <= m) continue;
                    TreeLeafNode<T> lv3 = lv2.getResultingNode(otherParameters, lv, distanceFunction);
                    long l2 = n = lv2 == lv3 ? m : distanceFunction.getDistance(lv3, otherParameters);
                    if (l <= n) continue;
                    l = n;
                    lv = lv3;
                }
                return lv;
            }
        }

        static final class TreeLeafNode<T>
        extends TreeNode<T> {
            final T value;

            TreeLeafNode(NoiseHypercube parameters, T value) {
                super(parameters.getParameters());
                this.value = value;
            }

            @Override
            protected TreeLeafNode<T> getResultingNode(long[] otherParameters, @Nullable TreeLeafNode<T> alternative, NodeDistanceFunction<T> distanceFunction) {
                return this;
            }
        }
    }

    static interface NodeDistanceFunction<T> {
        public long getDistance(SearchTree.TreeNode<T> var1, long[] var2);
    }
}

