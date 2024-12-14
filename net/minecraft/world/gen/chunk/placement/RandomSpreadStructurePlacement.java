/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.chunk.placement;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.gen.chunk.placement.SpreadType;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.chunk.placement.StructurePlacementType;

public class RandomSpreadStructurePlacement
extends StructurePlacement {
    public static final MapCodec<RandomSpreadStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> RandomSpreadStructurePlacement.buildCodec(instance).and(instance.group(((MapCodec)Codec.intRange(0, 4096).fieldOf("spacing")).forGetter(RandomSpreadStructurePlacement::getSpacing), ((MapCodec)Codec.intRange(0, 4096).fieldOf("separation")).forGetter(RandomSpreadStructurePlacement::getSeparation), SpreadType.CODEC.optionalFieldOf("spread_type", SpreadType.LINEAR).forGetter(RandomSpreadStructurePlacement::getSpreadType))).apply((Applicative<RandomSpreadStructurePlacement, ?>)instance, RandomSpreadStructurePlacement::new)).validate(RandomSpreadStructurePlacement::validate);
    private final int spacing;
    private final int separation;
    private final SpreadType spreadType;

    private static DataResult<RandomSpreadStructurePlacement> validate(RandomSpreadStructurePlacement structurePlacement) {
        if (structurePlacement.spacing <= structurePlacement.separation) {
            return DataResult.error(() -> "Spacing has to be larger than separation");
        }
        return DataResult.success(structurePlacement);
    }

    public RandomSpreadStructurePlacement(Vec3i locateOffset, StructurePlacement.FrequencyReductionMethod frequencyReductionMethod, float frequency, int salt, Optional<StructurePlacement.ExclusionZone> exclusionZone, int spacing, int separation, SpreadType spreadType) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);
        this.spacing = spacing;
        this.separation = separation;
        this.spreadType = spreadType;
    }

    public RandomSpreadStructurePlacement(int spacing, int separation, SpreadType spreadType, int salt) {
        this(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0f, salt, Optional.empty(), spacing, separation, spreadType);
    }

    public int getSpacing() {
        return this.spacing;
    }

    public int getSeparation() {
        return this.separation;
    }

    public SpreadType getSpreadType() {
        return this.spreadType;
    }

    public ChunkPos getStartChunk(long seed, int chunkX, int chunkZ) {
        int k = Math.floorDiv(chunkX, this.spacing);
        int m = Math.floorDiv(chunkZ, this.spacing);
        ChunkRandom lv = new ChunkRandom(new CheckedRandom(0L));
        lv.setRegionSeed(seed, k, m, this.getSalt());
        int n = this.spacing - this.separation;
        int o = this.spreadType.get(lv, n);
        int p = this.spreadType.get(lv, n);
        return new ChunkPos(k * this.spacing + o, m * this.spacing + p);
    }

    @Override
    protected boolean isStartChunk(StructurePlacementCalculator calculator, int chunkX, int chunkZ) {
        ChunkPos lv = this.getStartChunk(calculator.getStructureSeed(), chunkX, chunkZ);
        return lv.x == chunkX && lv.z == chunkZ;
    }

    @Override
    public StructurePlacementType<?> getType() {
        return StructurePlacementType.RANDOM_SPREAD;
    }
}

