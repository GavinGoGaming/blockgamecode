/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.chunk;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureHolder;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.chunk.BelowZeroRetrogen;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.ChunkSkyLight;
import net.minecraft.world.chunk.light.LightSourceView;
import net.minecraft.world.event.listener.GameEventDispatcher;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.tick.BasicTickScheduler;
import net.minecraft.world.tick.SerializableTickScheduler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class Chunk
implements BlockView,
BiomeAccess.Storage,
LightSourceView,
StructureHolder {
    public static final int MISSING_SECTION = -1;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final LongSet EMPTY_STRUCTURE_REFERENCES = new LongOpenHashSet();
    protected final ShortList[] postProcessingLists;
    protected volatile boolean needsSaving;
    private volatile boolean lightOn;
    protected final ChunkPos pos;
    private long inhabitedTime;
    @Nullable
    @Deprecated
    private GenerationSettings generationSettings;
    @Nullable
    protected ChunkNoiseSampler chunkNoiseSampler;
    protected final UpgradeData upgradeData;
    @Nullable
    protected BlendingData blendingData;
    protected final Map<Heightmap.Type, Heightmap> heightmaps = Maps.newEnumMap(Heightmap.Type.class);
    protected ChunkSkyLight chunkSkyLight;
    private final Map<Structure, StructureStart> structureStarts = Maps.newHashMap();
    private final Map<Structure, LongSet> structureReferences = Maps.newHashMap();
    protected final Map<BlockPos, NbtCompound> blockEntityNbts = Maps.newHashMap();
    protected final Map<BlockPos, BlockEntity> blockEntities = new Object2ObjectOpenHashMap<BlockPos, BlockEntity>();
    protected final HeightLimitView heightLimitView;
    protected final ChunkSection[] sectionArray;

    public Chunk(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> biomeRegistry, long inhabitedTime, @Nullable ChunkSection[] sectionArray, @Nullable BlendingData blendingData) {
        this.pos = pos;
        this.upgradeData = upgradeData;
        this.heightLimitView = heightLimitView;
        this.sectionArray = new ChunkSection[heightLimitView.countVerticalSections()];
        this.inhabitedTime = inhabitedTime;
        this.postProcessingLists = new ShortList[heightLimitView.countVerticalSections()];
        this.blendingData = blendingData;
        this.chunkSkyLight = new ChunkSkyLight(heightLimitView);
        if (sectionArray != null) {
            if (this.sectionArray.length == sectionArray.length) {
                System.arraycopy(sectionArray, 0, this.sectionArray, 0, this.sectionArray.length);
            } else {
                LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", (Object)sectionArray.length, (Object)this.sectionArray.length);
            }
        }
        Chunk.fillSectionArray(biomeRegistry, this.sectionArray);
    }

    private static void fillSectionArray(Registry<Biome> biomeRegistry, ChunkSection[] sectionArray) {
        for (int i = 0; i < sectionArray.length; ++i) {
            if (sectionArray[i] != null) continue;
            sectionArray[i] = new ChunkSection(biomeRegistry);
        }
    }

    public GameEventDispatcher getGameEventDispatcher(int ySectionCoord) {
        return GameEventDispatcher.EMPTY;
    }

    @Nullable
    public abstract BlockState setBlockState(BlockPos var1, BlockState var2, boolean var3);

    public abstract void setBlockEntity(BlockEntity var1);

    public abstract void addEntity(Entity var1);

    public int getHighestNonEmptySection() {
        ChunkSection[] lvs = this.getSectionArray();
        for (int i = lvs.length - 1; i >= 0; --i) {
            ChunkSection lv = lvs[i];
            if (lv.isEmpty()) continue;
            return i;
        }
        return -1;
    }

    @Deprecated(forRemoval=true)
    public int getHighestNonEmptySectionYOffset() {
        int i = this.getHighestNonEmptySection();
        return i == -1 ? this.getBottomY() : ChunkSectionPos.getBlockCoord(this.sectionIndexToCoord(i));
    }

    public Set<BlockPos> getBlockEntityPositions() {
        HashSet<BlockPos> set = Sets.newHashSet(this.blockEntityNbts.keySet());
        set.addAll(this.blockEntities.keySet());
        return set;
    }

    public ChunkSection[] getSectionArray() {
        return this.sectionArray;
    }

    public ChunkSection getSection(int yIndex) {
        return this.getSectionArray()[yIndex];
    }

    public Collection<Map.Entry<Heightmap.Type, Heightmap>> getHeightmaps() {
        return Collections.unmodifiableSet(this.heightmaps.entrySet());
    }

    public void setHeightmap(Heightmap.Type type, long[] heightmap) {
        this.getHeightmap(type).setTo(this, type, heightmap);
    }

    public Heightmap getHeightmap(Heightmap.Type type) {
        return this.heightmaps.computeIfAbsent(type, type2 -> new Heightmap(this, (Heightmap.Type)type2));
    }

    public boolean hasHeightmap(Heightmap.Type type) {
        return this.heightmaps.get(type) != null;
    }

    public int sampleHeightmap(Heightmap.Type type, int x, int z) {
        Heightmap lv = this.heightmaps.get(type);
        if (lv == null) {
            if (SharedConstants.isDevelopment && this instanceof WorldChunk) {
                LOGGER.error("Unprimed heightmap: " + String.valueOf(type) + " " + x + " " + z);
            }
            Heightmap.populateHeightmaps(this, EnumSet.of(type));
            lv = this.heightmaps.get(type);
        }
        return lv.get(x & 0xF, z & 0xF) - 1;
    }

    public ChunkPos getPos() {
        return this.pos;
    }

    @Override
    @Nullable
    public StructureStart getStructureStart(Structure structure) {
        return this.structureStarts.get(structure);
    }

    @Override
    public void setStructureStart(Structure structure, StructureStart start) {
        this.structureStarts.put(structure, start);
        this.needsSaving = true;
    }

    public Map<Structure, StructureStart> getStructureStarts() {
        return Collections.unmodifiableMap(this.structureStarts);
    }

    public void setStructureStarts(Map<Structure, StructureStart> structureStarts) {
        this.structureStarts.clear();
        this.structureStarts.putAll(structureStarts);
        this.needsSaving = true;
    }

    @Override
    public LongSet getStructureReferences(Structure structure) {
        return this.structureReferences.getOrDefault(structure, EMPTY_STRUCTURE_REFERENCES);
    }

    @Override
    public void addStructureReference(Structure structure, long reference) {
        this.structureReferences.computeIfAbsent(structure, type2 -> new LongOpenHashSet()).add(reference);
        this.needsSaving = true;
    }

    @Override
    public Map<Structure, LongSet> getStructureReferences() {
        return Collections.unmodifiableMap(this.structureReferences);
    }

    @Override
    public void setStructureReferences(Map<Structure, LongSet> structureReferences) {
        this.structureReferences.clear();
        this.structureReferences.putAll(structureReferences);
        this.needsSaving = true;
    }

    public boolean areSectionsEmptyBetween(int lowerHeight, int upperHeight) {
        if (lowerHeight < this.getBottomY()) {
            lowerHeight = this.getBottomY();
        }
        if (upperHeight >= this.getTopY()) {
            upperHeight = this.getTopY() - 1;
        }
        for (int k = lowerHeight; k <= upperHeight; k += 16) {
            if (this.getSection(this.getSectionIndex(k)).isEmpty()) continue;
            return false;
        }
        return true;
    }

    public boolean isSectionEmpty(int sectionCoord) {
        return this.getSection(this.sectionCoordToIndex(sectionCoord)).isEmpty();
    }

    public void setNeedsSaving(boolean needsSaving) {
        this.needsSaving = needsSaving;
    }

    public boolean needsSaving() {
        return this.needsSaving;
    }

    public abstract ChunkStatus getStatus();

    public ChunkStatus getMaxStatus() {
        ChunkStatus lv = this.getStatus();
        BelowZeroRetrogen lv2 = this.getBelowZeroRetrogen();
        if (lv2 != null) {
            ChunkStatus lv3 = lv2.getTargetStatus();
            return ChunkStatus.max(lv3, lv);
        }
        return lv;
    }

    public abstract void removeBlockEntity(BlockPos var1);

    public void markBlockForPostProcessing(BlockPos pos) {
        LOGGER.warn("Trying to mark a block for PostProcessing @ {}, but this operation is not supported.", (Object)pos);
    }

    public ShortList[] getPostProcessingLists() {
        return this.postProcessingLists;
    }

    public void markBlockForPostProcessing(short packedPos, int index) {
        Chunk.getList(this.getPostProcessingLists(), index).add(packedPos);
    }

    public void addPendingBlockEntityNbt(NbtCompound nbt) {
        this.blockEntityNbts.put(BlockEntity.posFromNbt(nbt), nbt);
    }

    @Nullable
    public NbtCompound getBlockEntityNbt(BlockPos pos) {
        return this.blockEntityNbts.get(pos);
    }

    @Nullable
    public abstract NbtCompound getPackedBlockEntityNbt(BlockPos var1, RegistryWrapper.WrapperLookup var2);

    @Override
    public final void forEachLightSource(BiConsumer<BlockPos, BlockState> callback) {
        this.forEachBlockMatchingPredicate(blockState -> blockState.getLuminance() != 0, callback);
    }

    public void forEachBlockMatchingPredicate(Predicate<BlockState> predicate, BiConsumer<BlockPos, BlockState> consumer) {
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (int i = this.getBottomSectionCoord(); i < this.getTopSectionCoord(); ++i) {
            ChunkSection lv2 = this.getSection(this.sectionCoordToIndex(i));
            if (!lv2.hasAny(predicate)) continue;
            BlockPos lv3 = ChunkSectionPos.from(this.pos, i).getMinPos();
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    for (int l = 0; l < 16; ++l) {
                        BlockState lv4 = lv2.getBlockState(l, j, k);
                        if (!predicate.test(lv4)) continue;
                        consumer.accept(lv.set(lv3, l, j, k), lv4);
                    }
                }
            }
        }
    }

    public abstract BasicTickScheduler<Block> getBlockTickScheduler();

    public abstract BasicTickScheduler<Fluid> getFluidTickScheduler();

    public abstract TickSchedulers getTickSchedulers();

    public UpgradeData getUpgradeData() {
        return this.upgradeData;
    }

    public boolean usesOldNoise() {
        return this.blendingData != null;
    }

    @Nullable
    public BlendingData getBlendingData() {
        return this.blendingData;
    }

    public void setBlendingData(BlendingData blendingData) {
        this.blendingData = blendingData;
    }

    public long getInhabitedTime() {
        return this.inhabitedTime;
    }

    public void increaseInhabitedTime(long delta) {
        this.inhabitedTime += delta;
    }

    public void setInhabitedTime(long inhabitedTime) {
        this.inhabitedTime = inhabitedTime;
    }

    public static ShortList getList(ShortList[] lists, int index) {
        if (lists[index] == null) {
            lists[index] = new ShortArrayList();
        }
        return lists[index];
    }

    public boolean isLightOn() {
        return this.lightOn;
    }

    public void setLightOn(boolean lightOn) {
        this.lightOn = lightOn;
        this.setNeedsSaving(true);
    }

    @Override
    public int getBottomY() {
        return this.heightLimitView.getBottomY();
    }

    @Override
    public int getHeight() {
        return this.heightLimitView.getHeight();
    }

    public ChunkNoiseSampler getOrCreateChunkNoiseSampler(Function<Chunk, ChunkNoiseSampler> chunkNoiseSamplerCreator) {
        if (this.chunkNoiseSampler == null) {
            this.chunkNoiseSampler = chunkNoiseSamplerCreator.apply(this);
        }
        return this.chunkNoiseSampler;
    }

    @Deprecated
    public GenerationSettings getOrCreateGenerationSettings(Supplier<GenerationSettings> generationSettingsCreator) {
        if (this.generationSettings == null) {
            this.generationSettings = generationSettingsCreator.get();
        }
        return this.generationSettings;
    }

    @Override
    public RegistryEntry<Biome> getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        try {
            int l = BiomeCoords.fromBlock(this.getBottomY());
            int m = l + BiomeCoords.fromBlock(this.getHeight()) - 1;
            int n = MathHelper.clamp(biomeY, l, m);
            int o = this.getSectionIndex(BiomeCoords.toBlock(n));
            return this.sectionArray[o].getBiome(biomeX & 3, n & 3, biomeZ & 3);
        } catch (Throwable throwable) {
            CrashReport lv = CrashReport.create(throwable, "Getting biome");
            CrashReportSection lv2 = lv.addElement("Biome being got");
            lv2.add("Location", () -> CrashReportSection.createPositionString((HeightLimitView)this, biomeX, biomeY, biomeZ));
            throw new CrashException(lv);
        }
    }

    public void populateBiomes(BiomeSupplier biomeSupplier, MultiNoiseUtil.MultiNoiseSampler sampler) {
        ChunkPos lv = this.getPos();
        int i = BiomeCoords.fromBlock(lv.getStartX());
        int j = BiomeCoords.fromBlock(lv.getStartZ());
        HeightLimitView lv2 = this.getHeightLimitView();
        for (int k = lv2.getBottomSectionCoord(); k < lv2.getTopSectionCoord(); ++k) {
            ChunkSection lv3 = this.getSection(this.sectionCoordToIndex(k));
            int l = BiomeCoords.fromChunk(k);
            lv3.populateBiomes(biomeSupplier, sampler, i, l, j);
        }
    }

    public boolean hasStructureReferences() {
        return !this.getStructureReferences().isEmpty();
    }

    @Nullable
    public BelowZeroRetrogen getBelowZeroRetrogen() {
        return null;
    }

    public boolean hasBelowZeroRetrogen() {
        return this.getBelowZeroRetrogen() != null;
    }

    public HeightLimitView getHeightLimitView() {
        return this;
    }

    public void refreshSurfaceY() {
        this.chunkSkyLight.refreshSurfaceY(this);
    }

    @Override
    public ChunkSkyLight getChunkSkyLight() {
        return this.chunkSkyLight;
    }

    public record TickSchedulers(SerializableTickScheduler<Block> blocks, SerializableTickScheduler<Fluid> fluids) {
    }
}

