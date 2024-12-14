/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.gen.surfacebuilder;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.VerticalSurfaceType;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import org.jetbrains.annotations.Nullable;

public class MaterialRules {
    public static final MaterialCondition STONE_DEPTH_FLOOR = MaterialRules.stoneDepth(0, false, VerticalSurfaceType.FLOOR);
    public static final MaterialCondition STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH = MaterialRules.stoneDepth(0, true, VerticalSurfaceType.FLOOR);
    public static final MaterialCondition STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH_RANGE_6 = MaterialRules.stoneDepth(0, true, 6, VerticalSurfaceType.FLOOR);
    public static final MaterialCondition STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH_RANGE_30 = MaterialRules.stoneDepth(0, true, 30, VerticalSurfaceType.FLOOR);
    public static final MaterialCondition STONE_DEPTH_CEILING = MaterialRules.stoneDepth(0, false, VerticalSurfaceType.CEILING);
    public static final MaterialCondition STONE_DEPTH_CEILING_WITH_SURFACE_DEPTH = MaterialRules.stoneDepth(0, true, VerticalSurfaceType.CEILING);

    public static MaterialCondition stoneDepth(int offset, boolean addSurfaceDepth, VerticalSurfaceType verticalSurfaceType) {
        return new StoneDepthMaterialCondition(offset, addSurfaceDepth, 0, verticalSurfaceType);
    }

    public static MaterialCondition stoneDepth(int offset, boolean addSurfaceDepth, int secondaryDepthRange, VerticalSurfaceType verticalSurfaceType) {
        return new StoneDepthMaterialCondition(offset, addSurfaceDepth, secondaryDepthRange, verticalSurfaceType);
    }

    public static MaterialCondition not(MaterialCondition target) {
        return new NotMaterialCondition(target);
    }

    public static MaterialCondition aboveY(YOffset anchor, int runDepthMultiplier) {
        return new AboveYMaterialCondition(anchor, runDepthMultiplier, false);
    }

    public static MaterialCondition aboveYWithStoneDepth(YOffset anchor, int runDepthMultiplier) {
        return new AboveYMaterialCondition(anchor, runDepthMultiplier, true);
    }

    public static MaterialCondition water(int offset, int runDepthMultiplier) {
        return new WaterMaterialCondition(offset, runDepthMultiplier, false);
    }

    public static MaterialCondition waterWithStoneDepth(int offset, int runDepthMultiplier) {
        return new WaterMaterialCondition(offset, runDepthMultiplier, true);
    }

    @SafeVarargs
    public static MaterialCondition biome(RegistryKey<Biome> ... biomes) {
        return MaterialRules.biome(List.of(biomes));
    }

    private static BiomeMaterialCondition biome(List<RegistryKey<Biome>> biomes) {
        return new BiomeMaterialCondition(biomes);
    }

    public static MaterialCondition noiseThreshold(RegistryKey<DoublePerlinNoiseSampler.NoiseParameters> noise, double min) {
        return MaterialRules.noiseThreshold(noise, min, Double.MAX_VALUE);
    }

    public static MaterialCondition noiseThreshold(RegistryKey<DoublePerlinNoiseSampler.NoiseParameters> noise, double min, double max) {
        return new NoiseThresholdMaterialCondition(noise, min, max);
    }

    public static MaterialCondition verticalGradient(String id, YOffset trueAtAndBelow, YOffset falseAtAndAbove) {
        return new VerticalGradientMaterialCondition(Identifier.of(id), trueAtAndBelow, falseAtAndAbove);
    }

    public static MaterialCondition steepSlope() {
        return SteepMaterialCondition.INSTANCE;
    }

    public static MaterialCondition hole() {
        return HoleMaterialCondition.INSTANCE;
    }

    public static MaterialCondition surface() {
        return SurfaceMaterialCondition.INSTANCE;
    }

    public static MaterialCondition temperature() {
        return TemperatureMaterialCondition.INSTANCE;
    }

    public static MaterialRule condition(MaterialCondition condition, MaterialRule rule) {
        return new ConditionMaterialRule(condition, rule);
    }

    public static MaterialRule sequence(MaterialRule ... rules) {
        if (rules.length == 0) {
            throw new IllegalArgumentException("Need at least 1 rule for a sequence");
        }
        return new SequenceMaterialRule(Arrays.asList(rules));
    }

    public static MaterialRule block(BlockState state) {
        return new BlockMaterialRule(state);
    }

    public static MaterialRule terracottaBands() {
        return TerracottaBandsMaterialRule.INSTANCE;
    }

    static <A> MapCodec<? extends A> register(Registry<MapCodec<? extends A>> registry, String id, CodecHolder<? extends A> codecHolder) {
        return Registry.register(registry, id, codecHolder.codec());
    }

    record StoneDepthMaterialCondition(int offset, boolean addSurfaceDepth, int secondaryDepthRange, VerticalSurfaceType surfaceType) implements MaterialCondition
    {
        static final CodecHolder<StoneDepthMaterialCondition> CODEC = CodecHolder.of(RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("offset")).forGetter(StoneDepthMaterialCondition::offset), ((MapCodec)Codec.BOOL.fieldOf("add_surface_depth")).forGetter(StoneDepthMaterialCondition::addSurfaceDepth), ((MapCodec)Codec.INT.fieldOf("secondary_depth_range")).forGetter(StoneDepthMaterialCondition::secondaryDepthRange), ((MapCodec)VerticalSurfaceType.CODEC.fieldOf("surface_type")).forGetter(StoneDepthMaterialCondition::surfaceType)).apply((Applicative<StoneDepthMaterialCondition, ?>)instance, StoneDepthMaterialCondition::new)));

        @Override
        public CodecHolder<? extends MaterialCondition> codec() {
            return CODEC;
        }

        @Override
        public BooleanSupplier apply(final MaterialRuleContext arg) {
            final boolean bl = this.surfaceType == VerticalSurfaceType.CEILING;
            class StoneDepthPredicate
            extends FullLazyAbstractPredicate {
                StoneDepthPredicate() {
                    super(arg2);
                }

                @Override
                protected boolean test() {
                    int i = bl ? this.context.stoneDepthBelow : this.context.stoneDepthAbove;
                    int j = StoneDepthMaterialCondition.this.addSurfaceDepth ? this.context.runDepth : 0;
                    int k = StoneDepthMaterialCondition.this.secondaryDepthRange == 0 ? 0 : (int)MathHelper.map(this.context.getSecondaryDepth(), -1.0, 1.0, 0.0, (double)StoneDepthMaterialCondition.this.secondaryDepthRange);
                    return i <= 1 + StoneDepthMaterialCondition.this.offset + j + k;
                }
            }
            return new StoneDepthPredicate();
        }

        @Override
        public /* synthetic */ Object apply(Object context) {
            return this.apply((MaterialRuleContext)context);
        }
    }

    record NotMaterialCondition(MaterialCondition target) implements MaterialCondition
    {
        static final CodecHolder<NotMaterialCondition> CODEC = CodecHolder.of(MaterialCondition.CODEC.xmap(NotMaterialCondition::new, NotMaterialCondition::target).fieldOf("invert"));

        @Override
        public CodecHolder<? extends MaterialCondition> codec() {
            return CODEC;
        }

        @Override
        public BooleanSupplier apply(MaterialRuleContext arg) {
            return new InvertedBooleanSupplier((BooleanSupplier)this.target.apply(arg));
        }

        @Override
        public /* synthetic */ Object apply(Object context) {
            return this.apply((MaterialRuleContext)context);
        }
    }

    public static interface MaterialCondition
    extends Function<MaterialRuleContext, BooleanSupplier> {
        public static final Codec<MaterialCondition> CODEC = Registries.MATERIAL_CONDITION.getCodec().dispatch(arg -> arg.codec().codec(), Function.identity());

        public static MapCodec<? extends MaterialCondition> registerAndGetDefault(Registry<MapCodec<? extends MaterialCondition>> registry) {
            MaterialRules.register(registry, "biome", BiomeMaterialCondition.CODEC);
            MaterialRules.register(registry, "noise_threshold", NoiseThresholdMaterialCondition.CODEC);
            MaterialRules.register(registry, "vertical_gradient", VerticalGradientMaterialCondition.CODEC);
            MaterialRules.register(registry, "y_above", AboveYMaterialCondition.CODEC);
            MaterialRules.register(registry, "water", WaterMaterialCondition.CODEC);
            MaterialRules.register(registry, "temperature", TemperatureMaterialCondition.CODEC);
            MaterialRules.register(registry, "steep", SteepMaterialCondition.CODEC);
            MaterialRules.register(registry, "not", NotMaterialCondition.CODEC);
            MaterialRules.register(registry, "hole", HoleMaterialCondition.CODEC);
            MaterialRules.register(registry, "above_preliminary_surface", SurfaceMaterialCondition.CODEC);
            return MaterialRules.register(registry, "stone_depth", StoneDepthMaterialCondition.CODEC);
        }

        public CodecHolder<? extends MaterialCondition> codec();
    }

    record AboveYMaterialCondition(YOffset anchor, int surfaceDepthMultiplier, boolean addStoneDepth) implements MaterialCondition
    {
        static final CodecHolder<AboveYMaterialCondition> CODEC = CodecHolder.of(RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)YOffset.OFFSET_CODEC.fieldOf("anchor")).forGetter(AboveYMaterialCondition::anchor), ((MapCodec)Codec.intRange(-20, 20).fieldOf("surface_depth_multiplier")).forGetter(AboveYMaterialCondition::surfaceDepthMultiplier), ((MapCodec)Codec.BOOL.fieldOf("add_stone_depth")).forGetter(AboveYMaterialCondition::addStoneDepth)).apply((Applicative<AboveYMaterialCondition, ?>)instance, AboveYMaterialCondition::new)));

        @Override
        public CodecHolder<? extends MaterialCondition> codec() {
            return CODEC;
        }

        @Override
        public BooleanSupplier apply(final MaterialRuleContext arg) {
            class AboveYPredicate
            extends FullLazyAbstractPredicate {
                AboveYPredicate() {
                    super(arg2);
                }

                @Override
                protected boolean test() {
                    return this.context.blockY + (AboveYMaterialCondition.this.addStoneDepth ? this.context.stoneDepthAbove : 0) >= AboveYMaterialCondition.this.anchor.getY(this.context.heightContext) + this.context.runDepth * AboveYMaterialCondition.this.surfaceDepthMultiplier;
                }
            }
            return new AboveYPredicate();
        }

        @Override
        public /* synthetic */ Object apply(Object context) {
            return this.apply((MaterialRuleContext)context);
        }
    }

    record WaterMaterialCondition(int offset, int surfaceDepthMultiplier, boolean addStoneDepth) implements MaterialCondition
    {
        static final CodecHolder<WaterMaterialCondition> CODEC = CodecHolder.of(RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("offset")).forGetter(WaterMaterialCondition::offset), ((MapCodec)Codec.intRange(-20, 20).fieldOf("surface_depth_multiplier")).forGetter(WaterMaterialCondition::surfaceDepthMultiplier), ((MapCodec)Codec.BOOL.fieldOf("add_stone_depth")).forGetter(WaterMaterialCondition::addStoneDepth)).apply((Applicative<WaterMaterialCondition, ?>)instance, WaterMaterialCondition::new)));

        @Override
        public CodecHolder<? extends MaterialCondition> codec() {
            return CODEC;
        }

        @Override
        public BooleanSupplier apply(final MaterialRuleContext arg) {
            class WaterPredicate
            extends FullLazyAbstractPredicate {
                WaterPredicate() {
                    super(arg2);
                }

                @Override
                protected boolean test() {
                    return this.context.fluidHeight == Integer.MIN_VALUE || this.context.blockY + (WaterMaterialCondition.this.addStoneDepth ? this.context.stoneDepthAbove : 0) >= this.context.fluidHeight + WaterMaterialCondition.this.offset + this.context.runDepth * WaterMaterialCondition.this.surfaceDepthMultiplier;
                }
            }
            return new WaterPredicate();
        }

        @Override
        public /* synthetic */ Object apply(Object context) {
            return this.apply((MaterialRuleContext)context);
        }
    }

    static final class BiomeMaterialCondition
    implements MaterialCondition {
        static final CodecHolder<BiomeMaterialCondition> CODEC = CodecHolder.of(((MapCodec)RegistryKey.createCodec(RegistryKeys.BIOME).listOf().fieldOf("biome_is")).xmap(MaterialRules::biome, arg -> arg.biomes));
        private final List<RegistryKey<Biome>> biomes;
        final Predicate<RegistryKey<Biome>> predicate;

        BiomeMaterialCondition(List<RegistryKey<Biome>> biomes) {
            this.biomes = biomes;
            this.predicate = Set.copyOf(biomes)::contains;
        }

        @Override
        public CodecHolder<? extends MaterialCondition> codec() {
            return CODEC;
        }

        @Override
        public BooleanSupplier apply(final MaterialRuleContext arg) {
            class BiomePredicate
            extends FullLazyAbstractPredicate {
                BiomePredicate() {
                    super(arg2);
                }

                @Override
                protected boolean test() {
                    return this.context.biomeSupplier.get().matches(BiomeMaterialCondition.this.predicate);
                }
            }
            return new BiomePredicate();
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof BiomeMaterialCondition) {
                BiomeMaterialCondition lv = (BiomeMaterialCondition)object;
                return this.biomes.equals(lv.biomes);
            }
            return false;
        }

        public int hashCode() {
            return this.biomes.hashCode();
        }

        public String toString() {
            return "BiomeConditionSource[biomes=" + String.valueOf(this.biomes) + "]";
        }

        @Override
        public /* synthetic */ Object apply(Object context) {
            return this.apply((MaterialRuleContext)context);
        }
    }

    record NoiseThresholdMaterialCondition(RegistryKey<DoublePerlinNoiseSampler.NoiseParameters> noise, double minThreshold, double maxThreshold) implements MaterialCondition
    {
        static final CodecHolder<NoiseThresholdMaterialCondition> CODEC = CodecHolder.of(RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)RegistryKey.createCodec(RegistryKeys.NOISE_PARAMETERS).fieldOf("noise")).forGetter(NoiseThresholdMaterialCondition::noise), ((MapCodec)Codec.DOUBLE.fieldOf("min_threshold")).forGetter(NoiseThresholdMaterialCondition::minThreshold), ((MapCodec)Codec.DOUBLE.fieldOf("max_threshold")).forGetter(NoiseThresholdMaterialCondition::maxThreshold)).apply((Applicative<NoiseThresholdMaterialCondition, ?>)instance, NoiseThresholdMaterialCondition::new)));

        @Override
        public CodecHolder<? extends MaterialCondition> codec() {
            return CODEC;
        }

        @Override
        public BooleanSupplier apply(final MaterialRuleContext arg) {
            final DoublePerlinNoiseSampler lv = arg.noiseConfig.getOrCreateSampler(this.noise);
            class NoiseThresholdPredicate
            extends HorizontalLazyAbstractPredicate {
                NoiseThresholdPredicate() {
                    super(arg2);
                }

                @Override
                protected boolean test() {
                    double d = lv.sample(this.context.blockX, 0.0, this.context.blockZ);
                    return d >= NoiseThresholdMaterialCondition.this.minThreshold && d <= NoiseThresholdMaterialCondition.this.maxThreshold;
                }
            }
            return new NoiseThresholdPredicate();
        }

        @Override
        public /* synthetic */ Object apply(Object context) {
            return this.apply((MaterialRuleContext)context);
        }
    }

    record VerticalGradientMaterialCondition(Identifier randomName, YOffset trueAtAndBelow, YOffset falseAtAndAbove) implements MaterialCondition
    {
        static final CodecHolder<VerticalGradientMaterialCondition> CODEC = CodecHolder.of(RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("random_name")).forGetter(VerticalGradientMaterialCondition::randomName), ((MapCodec)YOffset.OFFSET_CODEC.fieldOf("true_at_and_below")).forGetter(VerticalGradientMaterialCondition::trueAtAndBelow), ((MapCodec)YOffset.OFFSET_CODEC.fieldOf("false_at_and_above")).forGetter(VerticalGradientMaterialCondition::falseAtAndAbove)).apply((Applicative<VerticalGradientMaterialCondition, ?>)instance, VerticalGradientMaterialCondition::new)));

        @Override
        public CodecHolder<? extends MaterialCondition> codec() {
            return CODEC;
        }

        @Override
        public BooleanSupplier apply(final MaterialRuleContext arg) {
            final int i = this.trueAtAndBelow().getY(arg.heightContext);
            final int j = this.falseAtAndAbove().getY(arg.heightContext);
            final RandomSplitter lv = arg.noiseConfig.getOrCreateRandomDeriver(this.randomName());
            class VerticalGradientPredicate
            extends FullLazyAbstractPredicate {
                VerticalGradientPredicate() {
                    super(arg2);
                }

                @Override
                protected boolean test() {
                    int i2 = this.context.blockY;
                    if (i2 <= i) {
                        return true;
                    }
                    if (i2 >= j) {
                        return false;
                    }
                    double d = MathHelper.map((double)i2, (double)i, (double)j, 1.0, 0.0);
                    Random lv2 = lv.split(this.context.blockX, i2, this.context.blockZ);
                    return (double)lv2.nextFloat() < d;
                }
            }
            return new VerticalGradientPredicate();
        }

        @Override
        public /* synthetic */ Object apply(Object context) {
            return this.apply((MaterialRuleContext)context);
        }
    }

    static enum SteepMaterialCondition implements MaterialCondition
    {
        INSTANCE;

        static final CodecHolder<SteepMaterialCondition> CODEC;

        @Override
        public CodecHolder<? extends MaterialCondition> codec() {
            return CODEC;
        }

        @Override
        public BooleanSupplier apply(MaterialRuleContext arg) {
            return arg.steepSlopePredicate;
        }

        @Override
        public /* synthetic */ Object apply(Object context) {
            return this.apply((MaterialRuleContext)context);
        }

        static {
            CODEC = CodecHolder.of(MapCodec.unit(INSTANCE));
        }
    }

    static enum HoleMaterialCondition implements MaterialCondition
    {
        INSTANCE;

        static final CodecHolder<HoleMaterialCondition> CODEC;

        @Override
        public CodecHolder<? extends MaterialCondition> codec() {
            return CODEC;
        }

        @Override
        public BooleanSupplier apply(MaterialRuleContext arg) {
            return arg.negativeRunDepthPredicate;
        }

        @Override
        public /* synthetic */ Object apply(Object context) {
            return this.apply((MaterialRuleContext)context);
        }

        static {
            CODEC = CodecHolder.of(MapCodec.unit(INSTANCE));
        }
    }

    static enum SurfaceMaterialCondition implements MaterialCondition
    {
        INSTANCE;

        static final CodecHolder<SurfaceMaterialCondition> CODEC;

        @Override
        public CodecHolder<? extends MaterialCondition> codec() {
            return CODEC;
        }

        @Override
        public BooleanSupplier apply(MaterialRuleContext arg) {
            return arg.surfacePredicate;
        }

        @Override
        public /* synthetic */ Object apply(Object context) {
            return this.apply((MaterialRuleContext)context);
        }

        static {
            CODEC = CodecHolder.of(MapCodec.unit(INSTANCE));
        }
    }

    static enum TemperatureMaterialCondition implements MaterialCondition
    {
        INSTANCE;

        static final CodecHolder<TemperatureMaterialCondition> CODEC;

        @Override
        public CodecHolder<? extends MaterialCondition> codec() {
            return CODEC;
        }

        @Override
        public BooleanSupplier apply(MaterialRuleContext arg) {
            return arg.biomeTemperaturePredicate;
        }

        @Override
        public /* synthetic */ Object apply(Object context) {
            return this.apply((MaterialRuleContext)context);
        }

        static {
            CODEC = CodecHolder.of(MapCodec.unit(INSTANCE));
        }
    }

    record ConditionMaterialRule(MaterialCondition ifTrue, MaterialRule thenRun) implements MaterialRule
    {
        static final CodecHolder<ConditionMaterialRule> CODEC = CodecHolder.of(RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)MaterialCondition.CODEC.fieldOf("if_true")).forGetter(ConditionMaterialRule::ifTrue), ((MapCodec)MaterialRule.CODEC.fieldOf("then_run")).forGetter(ConditionMaterialRule::thenRun)).apply((Applicative<ConditionMaterialRule, ?>)instance, ConditionMaterialRule::new)));

        @Override
        public CodecHolder<? extends MaterialRule> codec() {
            return CODEC;
        }

        @Override
        public BlockStateRule apply(MaterialRuleContext arg) {
            return new ConditionalBlockStateRule((BooleanSupplier)this.ifTrue.apply(arg), (BlockStateRule)this.thenRun.apply(arg));
        }

        @Override
        public /* synthetic */ Object apply(Object context) {
            return this.apply((MaterialRuleContext)context);
        }
    }

    public static interface MaterialRule
    extends Function<MaterialRuleContext, BlockStateRule> {
        public static final Codec<MaterialRule> CODEC = Registries.MATERIAL_RULE.getCodec().dispatch(arg -> arg.codec().codec(), Function.identity());

        public static MapCodec<? extends MaterialRule> registerAndGetDefault(Registry<MapCodec<? extends MaterialRule>> registry) {
            MaterialRules.register(registry, "bandlands", TerracottaBandsMaterialRule.CODEC);
            MaterialRules.register(registry, "block", BlockMaterialRule.CODEC);
            MaterialRules.register(registry, "sequence", SequenceMaterialRule.CODEC);
            return MaterialRules.register(registry, "condition", ConditionMaterialRule.CODEC);
        }

        public CodecHolder<? extends MaterialRule> codec();
    }

    record SequenceMaterialRule(List<MaterialRule> sequence) implements MaterialRule
    {
        static final CodecHolder<SequenceMaterialRule> CODEC = CodecHolder.of(MaterialRule.CODEC.listOf().xmap(SequenceMaterialRule::new, SequenceMaterialRule::sequence).fieldOf("sequence"));

        @Override
        public CodecHolder<? extends MaterialRule> codec() {
            return CODEC;
        }

        @Override
        public BlockStateRule apply(MaterialRuleContext arg) {
            if (this.sequence.size() == 1) {
                return (BlockStateRule)this.sequence.get(0).apply(arg);
            }
            ImmutableList.Builder builder = ImmutableList.builder();
            for (MaterialRule lv : this.sequence) {
                builder.add((BlockStateRule)lv.apply(arg));
            }
            return new SequenceBlockStateRule((List<BlockStateRule>)((Object)builder.build()));
        }

        @Override
        public /* synthetic */ Object apply(Object context) {
            return this.apply((MaterialRuleContext)context);
        }
    }

    record BlockMaterialRule(BlockState resultState, SimpleBlockStateRule rule) implements MaterialRule
    {
        static final CodecHolder<BlockMaterialRule> CODEC = CodecHolder.of(BlockState.CODEC.xmap(BlockMaterialRule::new, BlockMaterialRule::resultState).fieldOf("result_state"));

        BlockMaterialRule(BlockState resultState) {
            this(resultState, new SimpleBlockStateRule(resultState));
        }

        @Override
        public CodecHolder<? extends MaterialRule> codec() {
            return CODEC;
        }

        @Override
        public BlockStateRule apply(MaterialRuleContext arg) {
            return this.rule;
        }

        @Override
        public /* synthetic */ Object apply(Object context) {
            return this.apply((MaterialRuleContext)context);
        }
    }

    static enum TerracottaBandsMaterialRule implements MaterialRule
    {
        INSTANCE;

        static final CodecHolder<TerracottaBandsMaterialRule> CODEC;

        @Override
        public CodecHolder<? extends MaterialRule> codec() {
            return CODEC;
        }

        @Override
        public BlockStateRule apply(MaterialRuleContext arg) {
            return arg.surfaceBuilder::getTerracottaBlock;
        }

        @Override
        public /* synthetic */ Object apply(Object context) {
            return this.apply((MaterialRuleContext)context);
        }

        static {
            CODEC = CodecHolder.of(MapCodec.unit(INSTANCE));
        }
    }

    record SequenceBlockStateRule(List<BlockStateRule> rules) implements BlockStateRule
    {
        @Override
        @Nullable
        public BlockState tryApply(int i, int j, int k) {
            for (BlockStateRule lv : this.rules) {
                BlockState lv2 = lv.tryApply(i, j, k);
                if (lv2 == null) continue;
                return lv2;
            }
            return null;
        }
    }

    record ConditionalBlockStateRule(BooleanSupplier condition, BlockStateRule followup) implements BlockStateRule
    {
        @Override
        @Nullable
        public BlockState tryApply(int i, int j, int k) {
            if (!this.condition.get()) {
                return null;
            }
            return this.followup.tryApply(i, j, k);
        }
    }

    record SimpleBlockStateRule(BlockState state) implements BlockStateRule
    {
        @Override
        public BlockState tryApply(int i, int j, int k) {
            return this.state;
        }
    }

    protected static interface BlockStateRule {
        @Nullable
        public BlockState tryApply(int var1, int var2, int var3);
    }

    record InvertedBooleanSupplier(BooleanSupplier target) implements BooleanSupplier
    {
        @Override
        public boolean get() {
            return !this.target.get();
        }
    }

    static abstract class FullLazyAbstractPredicate
    extends LazyAbstractPredicate {
        protected FullLazyAbstractPredicate(MaterialRuleContext arg) {
            super(arg);
        }

        @Override
        protected long getCurrentUniqueValue() {
            return this.context.uniquePosValue;
        }
    }

    static abstract class HorizontalLazyAbstractPredicate
    extends LazyAbstractPredicate {
        protected HorizontalLazyAbstractPredicate(MaterialRuleContext arg) {
            super(arg);
        }

        @Override
        protected long getCurrentUniqueValue() {
            return this.context.uniqueHorizontalPosValue;
        }
    }

    static abstract class LazyAbstractPredicate
    implements BooleanSupplier {
        protected final MaterialRuleContext context;
        private long uniqueValue;
        @Nullable
        Boolean result;

        protected LazyAbstractPredicate(MaterialRuleContext context) {
            this.context = context;
            this.uniqueValue = this.getCurrentUniqueValue() - 1L;
        }

        @Override
        public boolean get() {
            long l = this.getCurrentUniqueValue();
            if (l == this.uniqueValue) {
                if (this.result == null) {
                    throw new IllegalStateException("Update triggered but the result is null");
                }
                return this.result;
            }
            this.uniqueValue = l;
            this.result = this.test();
            return this.result;
        }

        protected abstract long getCurrentUniqueValue();

        protected abstract boolean test();
    }

    static interface BooleanSupplier {
        public boolean get();
    }

    protected static final class MaterialRuleContext {
        private static final int field_36274 = 8;
        private static final int field_36275 = 4;
        private static final int field_36276 = 16;
        private static final int field_36277 = 15;
        final SurfaceBuilder surfaceBuilder;
        final BooleanSupplier biomeTemperaturePredicate = new BiomeTemperaturePredicate(this);
        final BooleanSupplier steepSlopePredicate = new SteepSlopePredicate(this);
        final BooleanSupplier negativeRunDepthPredicate = new NegativeRunDepthPredicate(this);
        final BooleanSupplier surfacePredicate = new SurfacePredicate();
        final NoiseConfig noiseConfig;
        final Chunk chunk;
        private final ChunkNoiseSampler chunkNoiseSampler;
        private final Function<BlockPos, RegistryEntry<Biome>> posToBiome;
        final HeightContext heightContext;
        private long packedChunkPos = Long.MAX_VALUE;
        private final int[] estimatedSurfaceHeights = new int[4];
        long uniqueHorizontalPosValue = -9223372036854775807L;
        int blockX;
        int blockZ;
        int runDepth;
        private long field_35677 = this.uniqueHorizontalPosValue - 1L;
        private double secondaryDepth;
        private long field_35679 = this.uniqueHorizontalPosValue - 1L;
        private int surfaceMinY;
        long uniquePosValue = -9223372036854775807L;
        final BlockPos.Mutable pos = new BlockPos.Mutable();
        Supplier<RegistryEntry<Biome>> biomeSupplier;
        int blockY;
        int fluidHeight;
        int stoneDepthBelow;
        int stoneDepthAbove;

        protected MaterialRuleContext(SurfaceBuilder surfaceBuilder, NoiseConfig noiseConfig, Chunk chunk, ChunkNoiseSampler chunkNoiseSampler, Function<BlockPos, RegistryEntry<Biome>> posToBiome, Registry<Biome> arg5, HeightContext heightContext) {
            this.surfaceBuilder = surfaceBuilder;
            this.noiseConfig = noiseConfig;
            this.chunk = chunk;
            this.chunkNoiseSampler = chunkNoiseSampler;
            this.posToBiome = posToBiome;
            this.heightContext = heightContext;
        }

        protected void initHorizontalContext(int blockX, int blockZ) {
            ++this.uniqueHorizontalPosValue;
            ++this.uniquePosValue;
            this.blockX = blockX;
            this.blockZ = blockZ;
            this.runDepth = this.surfaceBuilder.sampleRunDepth(blockX, blockZ);
        }

        protected void initVerticalContext(int stoneDepthAbove, int stoneDepthBelow, int fluidHeight, int blockX, int blockY, int blockZ) {
            ++this.uniquePosValue;
            this.biomeSupplier = Suppliers.memoize(() -> this.posToBiome.apply(this.pos.set(blockX, blockY, blockZ)));
            this.blockY = blockY;
            this.fluidHeight = fluidHeight;
            this.stoneDepthBelow = stoneDepthBelow;
            this.stoneDepthAbove = stoneDepthAbove;
        }

        protected double getSecondaryDepth() {
            if (this.field_35677 != this.uniqueHorizontalPosValue) {
                this.field_35677 = this.uniqueHorizontalPosValue;
                this.secondaryDepth = this.surfaceBuilder.sampleSecondaryDepth(this.blockX, this.blockZ);
            }
            return this.secondaryDepth;
        }

        private static int blockToChunkCoord(int blockCoord) {
            return blockCoord >> 4;
        }

        private static int chunkToBlockCoord(int chunkCoord) {
            return chunkCoord << 4;
        }

        protected int estimateSurfaceHeight() {
            if (this.field_35679 != this.uniqueHorizontalPosValue) {
                int j;
                this.field_35679 = this.uniqueHorizontalPosValue;
                int i = MaterialRuleContext.blockToChunkCoord(this.blockX);
                long l = ChunkPos.toLong(i, j = MaterialRuleContext.blockToChunkCoord(this.blockZ));
                if (this.packedChunkPos != l) {
                    this.packedChunkPos = l;
                    this.estimatedSurfaceHeights[0] = this.chunkNoiseSampler.estimateSurfaceHeight(MaterialRuleContext.chunkToBlockCoord(i), MaterialRuleContext.chunkToBlockCoord(j));
                    this.estimatedSurfaceHeights[1] = this.chunkNoiseSampler.estimateSurfaceHeight(MaterialRuleContext.chunkToBlockCoord(i + 1), MaterialRuleContext.chunkToBlockCoord(j));
                    this.estimatedSurfaceHeights[2] = this.chunkNoiseSampler.estimateSurfaceHeight(MaterialRuleContext.chunkToBlockCoord(i), MaterialRuleContext.chunkToBlockCoord(j + 1));
                    this.estimatedSurfaceHeights[3] = this.chunkNoiseSampler.estimateSurfaceHeight(MaterialRuleContext.chunkToBlockCoord(i + 1), MaterialRuleContext.chunkToBlockCoord(j + 1));
                }
                int k = MathHelper.floor(MathHelper.lerp2((float)(this.blockX & 0xF) / 16.0f, (float)(this.blockZ & 0xF) / 16.0f, this.estimatedSurfaceHeights[0], this.estimatedSurfaceHeights[1], this.estimatedSurfaceHeights[2], this.estimatedSurfaceHeights[3]));
                this.surfaceMinY = k + this.runDepth - 8;
            }
            return this.surfaceMinY;
        }

        static class BiomeTemperaturePredicate
        extends FullLazyAbstractPredicate {
            BiomeTemperaturePredicate(MaterialRuleContext arg) {
                super(arg);
            }

            @Override
            protected boolean test() {
                return this.context.biomeSupplier.get().value().isCold(this.context.pos.set(this.context.blockX, this.context.blockY, this.context.blockZ));
            }
        }

        static class SteepSlopePredicate
        extends HorizontalLazyAbstractPredicate {
            SteepSlopePredicate(MaterialRuleContext arg) {
                super(arg);
            }

            @Override
            protected boolean test() {
                int r;
                int i = this.context.blockX & 0xF;
                int j = this.context.blockZ & 0xF;
                int k = Math.max(j - 1, 0);
                int l = Math.min(j + 1, 15);
                Chunk lv = this.context.chunk;
                int m = lv.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, i, k);
                int n = lv.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, i, l);
                if (n >= m + 4) {
                    return true;
                }
                int o = Math.max(i - 1, 0);
                int p = Math.min(i + 1, 15);
                int q = lv.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, o, j);
                return q >= (r = lv.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, p, j)) + 4;
            }
        }

        static final class NegativeRunDepthPredicate
        extends HorizontalLazyAbstractPredicate {
            NegativeRunDepthPredicate(MaterialRuleContext arg) {
                super(arg);
            }

            @Override
            protected boolean test() {
                return this.context.runDepth <= 0;
            }
        }

        final class SurfacePredicate
        implements BooleanSupplier {
            SurfacePredicate() {
            }

            @Override
            public boolean get() {
                return MaterialRuleContext.this.blockY >= MaterialRuleContext.this.estimateSurfaceHeight();
            }
        }
    }
}

