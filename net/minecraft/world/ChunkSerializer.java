/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtShort;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.BelowZeroRetrogen;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ChunkType;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadableContainer;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.CarvingMask;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.StorageKey;
import net.minecraft.world.tick.ChunkTickScheduler;
import net.minecraft.world.tick.SimpleTickScheduler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ChunkSerializer {
    private static final Codec<PalettedContainer<BlockState>> CODEC = PalettedContainer.createPalettedContainerCodec(Block.STATE_IDS, BlockState.CODEC, PalettedContainer.PaletteProvider.BLOCK_STATE, Blocks.AIR.getDefaultState());
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String UPGRADE_DATA_KEY = "UpgradeData";
    private static final String BLOCK_TICKS = "block_ticks";
    private static final String FLUID_TICKS = "fluid_ticks";
    public static final String X_POS_KEY = "xPos";
    public static final String Z_POS_KEY = "zPos";
    public static final String HEIGHTMAPS_KEY = "Heightmaps";
    public static final String IS_LIGHT_ON_KEY = "isLightOn";
    public static final String SECTIONS_KEY = "sections";
    public static final String BLOCK_LIGHT_KEY = "BlockLight";
    public static final String SKY_LIGHT_KEY = "SkyLight";

    public static ProtoChunk deserialize(ServerWorld world, PointOfInterestStorage poiStorage, StorageKey key, ChunkPos chunkPos, NbtCompound nbt) {
        int o;
        NbtList lv25;
        Chunk lv16;
        ChunkPos lv = new ChunkPos(nbt.getInt(X_POS_KEY), nbt.getInt(Z_POS_KEY));
        if (!Objects.equals(chunkPos, lv)) {
            LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", chunkPos, chunkPos, lv);
            world.getServer().onChunkMisplacement(lv, chunkPos, key);
        }
        UpgradeData lv2 = nbt.contains(UPGRADE_DATA_KEY, NbtElement.COMPOUND_TYPE) ? new UpgradeData(nbt.getCompound(UPGRADE_DATA_KEY), world) : UpgradeData.NO_UPGRADE_DATA;
        boolean bl = nbt.getBoolean(IS_LIGHT_ON_KEY);
        NbtList lv3 = nbt.getList(SECTIONS_KEY, NbtElement.COMPOUND_TYPE);
        int i = world.countVerticalSections();
        ChunkSection[] lvs = new ChunkSection[i];
        boolean bl2 = world.getDimension().hasSkyLight();
        ServerChunkManager lv4 = world.getChunkManager();
        LightingProvider lv5 = ((ChunkManager)lv4).getLightingProvider();
        Registry<Biome> lv6 = world.getRegistryManager().get(RegistryKeys.BIOME);
        Codec<ReadableContainer<RegistryEntry<Biome>>> codec = ChunkSerializer.createCodec(lv6);
        boolean bl3 = false;
        for (int j = 0; j < lv3.size(); ++j) {
            boolean bl5;
            NbtCompound lv7 = lv3.getCompound(j);
            byte k = lv7.getByte("Y");
            int l = world.sectionCoordToIndex(k);
            if (l >= 0 && l < lvs.length) {
                ChunkSection lv10;
                PalettedContainer lv8 = lv7.contains("block_states", NbtElement.COMPOUND_TYPE) ? (PalettedContainer)CODEC.parse(NbtOps.INSTANCE, lv7.getCompound("block_states")).promotePartial(errorMessage -> ChunkSerializer.logRecoverableError(chunkPos, k, errorMessage)).getOrThrow(ChunkLoadingException::new) : new PalettedContainer(Block.STATE_IDS, Blocks.AIR.getDefaultState(), PalettedContainer.PaletteProvider.BLOCK_STATE);
                ReadableContainer<RegistryEntry.Reference<Biome>> lv9 = lv7.contains("biomes", NbtElement.COMPOUND_TYPE) ? (ReadableContainer)codec.parse(NbtOps.INSTANCE, lv7.getCompound("biomes")).promotePartial(errorMessage -> ChunkSerializer.logRecoverableError(chunkPos, k, errorMessage)).getOrThrow(ChunkLoadingException::new) : new PalettedContainer<RegistryEntry.Reference<Biome>>(lv6.getIndexedEntries(), lv6.entryOf(BiomeKeys.PLAINS), PalettedContainer.PaletteProvider.BIOME);
                lvs[l] = lv10 = new ChunkSection(lv8, lv9);
                ChunkSectionPos lv11 = ChunkSectionPos.from(chunkPos, k);
                poiStorage.initForPalette(lv11, lv10);
            }
            boolean bl4 = lv7.contains(BLOCK_LIGHT_KEY, NbtElement.BYTE_ARRAY_TYPE);
            boolean bl6 = bl5 = bl2 && lv7.contains(SKY_LIGHT_KEY, NbtElement.BYTE_ARRAY_TYPE);
            if (!bl4 && !bl5) continue;
            if (!bl3) {
                lv5.setRetainData(chunkPos, true);
                bl3 = true;
            }
            if (bl4) {
                lv5.enqueueSectionData(LightType.BLOCK, ChunkSectionPos.from(chunkPos, k), new ChunkNibbleArray(lv7.getByteArray(BLOCK_LIGHT_KEY)));
            }
            if (!bl5) continue;
            lv5.enqueueSectionData(LightType.SKY, ChunkSectionPos.from(chunkPos, k), new ChunkNibbleArray(lv7.getByteArray(SKY_LIGHT_KEY)));
        }
        long m = nbt.getLong("InhabitedTime");
        ChunkType lv12 = ChunkSerializer.getChunkType(nbt);
        BlendingData lv13 = nbt.contains("blending_data", NbtElement.COMPOUND_TYPE) ? (BlendingData)BlendingData.CODEC.parse(new Dynamic<NbtCompound>(NbtOps.INSTANCE, nbt.getCompound("blending_data"))).resultOrPartial(LOGGER::error).orElse(null) : null;
        if (lv12 == ChunkType.LEVELCHUNK) {
            ChunkTickScheduler<Block> lv14 = ChunkTickScheduler.create(nbt.getList(BLOCK_TICKS, NbtElement.COMPOUND_TYPE), id -> Registries.BLOCK.getOrEmpty(Identifier.tryParse(id)), chunkPos);
            ChunkTickScheduler<Fluid> lv15 = ChunkTickScheduler.create(nbt.getList(FLUID_TICKS, NbtElement.COMPOUND_TYPE), id -> Registries.FLUID.getOrEmpty(Identifier.tryParse(id)), chunkPos);
            lv16 = new WorldChunk(world.toServerWorld(), chunkPos, lv2, lv14, lv15, m, lvs, ChunkSerializer.getEntityLoadingCallback(world, nbt), lv13);
        } else {
            SimpleTickScheduler<Block> lv17 = SimpleTickScheduler.tick(nbt.getList(BLOCK_TICKS, NbtElement.COMPOUND_TYPE), id -> Registries.BLOCK.getOrEmpty(Identifier.tryParse(id)), chunkPos);
            SimpleTickScheduler<Fluid> lv18 = SimpleTickScheduler.tick(nbt.getList(FLUID_TICKS, NbtElement.COMPOUND_TYPE), id -> Registries.FLUID.getOrEmpty(Identifier.tryParse(id)), chunkPos);
            ProtoChunk lv19 = new ProtoChunk(chunkPos, lv2, lvs, lv17, lv18, world, lv6, lv13);
            lv16 = lv19;
            lv16.setInhabitedTime(m);
            if (nbt.contains("below_zero_retrogen", NbtElement.COMPOUND_TYPE)) {
                BelowZeroRetrogen.CODEC.parse(new Dynamic<NbtCompound>(NbtOps.INSTANCE, nbt.getCompound("below_zero_retrogen"))).resultOrPartial(LOGGER::error).ifPresent(lv19::setBelowZeroRetrogen);
            }
            ChunkStatus lv20 = ChunkStatus.byId(nbt.getString("Status"));
            lv19.setStatus(lv20);
            if (lv20.isAtLeast(ChunkStatus.INITIALIZE_LIGHT)) {
                lv19.setLightingProvider(lv5);
            }
        }
        lv16.setLightOn(bl);
        NbtCompound lv21 = nbt.getCompound(HEIGHTMAPS_KEY);
        EnumSet<Heightmap.Type> enumSet = EnumSet.noneOf(Heightmap.Type.class);
        for (Heightmap.Type lv22 : lv16.getStatus().getHeightmapTypes()) {
            String string = lv22.getName();
            if (lv21.contains(string, NbtElement.LONG_ARRAY_TYPE)) {
                lv16.setHeightmap(lv22, lv21.getLongArray(string));
                continue;
            }
            enumSet.add(lv22);
        }
        Heightmap.populateHeightmaps(lv16, enumSet);
        NbtCompound lv23 = nbt.getCompound("structures");
        lv16.setStructureStarts(ChunkSerializer.readStructureStarts(StructureContext.from(world), lv23, world.getSeed()));
        lv16.setStructureReferences(ChunkSerializer.readStructureReferences(world.getRegistryManager(), chunkPos, lv23));
        if (nbt.getBoolean("shouldSave")) {
            lv16.setNeedsSaving(true);
        }
        NbtList lv24 = nbt.getList("PostProcessing", NbtElement.LIST_TYPE);
        for (int n = 0; n < lv24.size(); ++n) {
            lv25 = lv24.getList(n);
            for (o = 0; o < lv25.size(); ++o) {
                lv16.markBlockForPostProcessing(lv25.getShort(o), n);
            }
        }
        if (lv12 == ChunkType.LEVELCHUNK) {
            return new WrapperProtoChunk((WorldChunk)lv16, false);
        }
        ProtoChunk lv26 = (ProtoChunk)lv16;
        lv25 = nbt.getList("entities", NbtElement.COMPOUND_TYPE);
        for (o = 0; o < lv25.size(); ++o) {
            lv26.addEntity(lv25.getCompound(o));
        }
        NbtList lv27 = nbt.getList("block_entities", NbtElement.COMPOUND_TYPE);
        for (int p = 0; p < lv27.size(); ++p) {
            NbtCompound lv28 = lv27.getCompound(p);
            lv16.addPendingBlockEntityNbt(lv28);
        }
        NbtCompound lv29 = nbt.getCompound("CarvingMasks");
        for (String string2 : lv29.getKeys()) {
            GenerationStep.Carver lv30 = GenerationStep.Carver.valueOf(string2);
            lv26.setCarvingMask(lv30, new CarvingMask(lv29.getLongArray(string2), lv16.getBottomY()));
        }
        return lv26;
    }

    private static void logRecoverableError(ChunkPos chunkPos, int y, String message) {
        LOGGER.error("Recoverable errors when loading section [{}, {}, {}]: {}", chunkPos.x, y, chunkPos.z, message);
    }

    private static Codec<ReadableContainer<RegistryEntry<Biome>>> createCodec(Registry<Biome> biomeRegistry) {
        return PalettedContainer.createReadableContainerCodec(biomeRegistry.getIndexedEntries(), biomeRegistry.getEntryCodec(), PalettedContainer.PaletteProvider.BIOME, biomeRegistry.entryOf(BiomeKeys.PLAINS));
    }

    public static NbtCompound serialize(ServerWorld world, Chunk chunk) {
        NbtCompound lv15;
        UpgradeData lv5;
        BelowZeroRetrogen lv4;
        ChunkPos lv = chunk.getPos();
        NbtCompound lv2 = NbtHelper.putDataVersion(new NbtCompound());
        lv2.putInt(X_POS_KEY, lv.x);
        lv2.putInt("yPos", chunk.getBottomSectionCoord());
        lv2.putInt(Z_POS_KEY, lv.z);
        lv2.putLong("LastUpdate", world.getTime());
        lv2.putLong("InhabitedTime", chunk.getInhabitedTime());
        lv2.putString("Status", Registries.CHUNK_STATUS.getId(chunk.getStatus()).toString());
        BlendingData lv3 = chunk.getBlendingData();
        if (lv3 != null) {
            BlendingData.CODEC.encodeStart(NbtOps.INSTANCE, lv3).resultOrPartial(LOGGER::error).ifPresent(arg2 -> lv2.put("blending_data", (NbtElement)arg2));
        }
        if ((lv4 = chunk.getBelowZeroRetrogen()) != null) {
            BelowZeroRetrogen.CODEC.encodeStart(NbtOps.INSTANCE, lv4).resultOrPartial(LOGGER::error).ifPresent(arg2 -> lv2.put("below_zero_retrogen", (NbtElement)arg2));
        }
        if (!(lv5 = chunk.getUpgradeData()).isDone()) {
            lv2.put(UPGRADE_DATA_KEY, lv5.toNbt());
        }
        ChunkSection[] lvs = chunk.getSectionArray();
        NbtList lv6 = new NbtList();
        ServerLightingProvider lv7 = world.getChunkManager().getLightingProvider();
        Registry<Biome> lv8 = world.getRegistryManager().get(RegistryKeys.BIOME);
        Codec<ReadableContainer<RegistryEntry<Biome>>> codec = ChunkSerializer.createCodec(lv8);
        boolean bl = chunk.isLightOn();
        for (int i = lv7.getBottomY(); i < lv7.getTopY(); ++i) {
            int j = chunk.sectionCoordToIndex(i);
            boolean bl2 = j >= 0 && j < lvs.length;
            ChunkNibbleArray lv9 = lv7.get(LightType.BLOCK).getLightSection(ChunkSectionPos.from(lv, i));
            ChunkNibbleArray lv10 = lv7.get(LightType.SKY).getLightSection(ChunkSectionPos.from(lv, i));
            if (!bl2 && lv9 == null && lv10 == null) continue;
            NbtCompound lv11 = new NbtCompound();
            if (bl2) {
                ChunkSection lv12 = lvs[j];
                lv11.put("block_states", CODEC.encodeStart(NbtOps.INSTANCE, lv12.getBlockStateContainer()).getOrThrow());
                lv11.put("biomes", codec.encodeStart(NbtOps.INSTANCE, lv12.getBiomeContainer()).getOrThrow());
            }
            if (lv9 != null && !lv9.isUninitialized()) {
                lv11.putByteArray(BLOCK_LIGHT_KEY, lv9.asByteArray());
            }
            if (lv10 != null && !lv10.isUninitialized()) {
                lv11.putByteArray(SKY_LIGHT_KEY, lv10.asByteArray());
            }
            if (lv11.isEmpty()) continue;
            lv11.putByte("Y", (byte)i);
            lv6.add(lv11);
        }
        lv2.put(SECTIONS_KEY, lv6);
        if (bl) {
            lv2.putBoolean(IS_LIGHT_ON_KEY, true);
        }
        NbtList lv13 = new NbtList();
        for (BlockPos lv14 : chunk.getBlockEntityPositions()) {
            lv15 = chunk.getPackedBlockEntityNbt(lv14, world.getRegistryManager());
            if (lv15 == null) continue;
            lv13.add(lv15);
        }
        lv2.put("block_entities", lv13);
        if (chunk.getStatus().getChunkType() == ChunkType.PROTOCHUNK) {
            ProtoChunk lv16 = (ProtoChunk)chunk;
            NbtList lv17 = new NbtList();
            lv17.addAll(lv16.getEntities());
            lv2.put("entities", lv17);
            lv15 = new NbtCompound();
            for (GenerationStep.Carver lv18 : GenerationStep.Carver.values()) {
                CarvingMask lv19 = lv16.getCarvingMask(lv18);
                if (lv19 == null) continue;
                lv15.putLongArray(lv18.toString(), lv19.getMask());
            }
            lv2.put("CarvingMasks", lv15);
        }
        ChunkSerializer.serializeTicks(world, lv2, chunk.getTickSchedulers());
        lv2.put("PostProcessing", ChunkSerializer.toNbt(chunk.getPostProcessingLists()));
        NbtCompound lv20 = new NbtCompound();
        for (Map.Entry<Heightmap.Type, Heightmap> entry : chunk.getHeightmaps()) {
            if (!chunk.getStatus().getHeightmapTypes().contains(entry.getKey())) continue;
            lv20.put(entry.getKey().getName(), new NbtLongArray(entry.getValue().asLongArray()));
        }
        lv2.put(HEIGHTMAPS_KEY, lv20);
        lv2.put("structures", ChunkSerializer.writeStructures(StructureContext.from(world), lv, chunk.getStructureStarts(), chunk.getStructureReferences()));
        return lv2;
    }

    private static void serializeTicks(ServerWorld world, NbtCompound nbt, Chunk.TickSchedulers tickSchedulers) {
        long l = world.getLevelProperties().getTime();
        nbt.put(BLOCK_TICKS, tickSchedulers.blocks().toNbt(l, block -> Registries.BLOCK.getId((Block)block).toString()));
        nbt.put(FLUID_TICKS, tickSchedulers.fluids().toNbt(l, fluid -> Registries.FLUID.getId((Fluid)fluid).toString()));
    }

    public static ChunkType getChunkType(@Nullable NbtCompound nbt) {
        if (nbt != null) {
            return ChunkStatus.byId(nbt.getString("Status")).getChunkType();
        }
        return ChunkType.PROTOCHUNK;
    }

    @Nullable
    private static WorldChunk.EntityLoader getEntityLoadingCallback(ServerWorld world, NbtCompound nbt) {
        NbtList lv = ChunkSerializer.getList(nbt, "entities");
        NbtList lv2 = ChunkSerializer.getList(nbt, "block_entities");
        if (lv == null && lv2 == null) {
            return null;
        }
        return chunk -> {
            if (lv != null) {
                world.loadEntities(EntityType.streamFromNbt(lv, world));
            }
            if (lv2 != null) {
                for (int i = 0; i < lv2.size(); ++i) {
                    NbtCompound lv = lv2.getCompound(i);
                    boolean bl = lv.getBoolean("keepPacked");
                    if (bl) {
                        chunk.addPendingBlockEntityNbt(lv);
                        continue;
                    }
                    BlockPos lv2 = BlockEntity.posFromNbt(lv);
                    BlockEntity lv3 = BlockEntity.createFromNbt(lv2, chunk.getBlockState(lv2), lv, world.getRegistryManager());
                    if (lv3 == null) continue;
                    chunk.setBlockEntity(lv3);
                }
            }
        };
    }

    @Nullable
    private static NbtList getList(NbtCompound nbt, String key) {
        NbtList lv = nbt.getList(key, NbtElement.COMPOUND_TYPE);
        return lv.isEmpty() ? null : lv;
    }

    private static NbtCompound writeStructures(StructureContext context, ChunkPos pos, Map<Structure, StructureStart> starts, Map<Structure, LongSet> references) {
        NbtCompound lv = new NbtCompound();
        NbtCompound lv2 = new NbtCompound();
        Registry<Structure> lv3 = context.registryManager().get(RegistryKeys.STRUCTURE);
        for (Map.Entry<Structure, StructureStart> entry : starts.entrySet()) {
            Identifier lv4 = lv3.getId(entry.getKey());
            lv2.put(lv4.toString(), entry.getValue().toNbt(context, pos));
        }
        lv.put("starts", lv2);
        NbtCompound lv5 = new NbtCompound();
        for (Map.Entry<Structure, LongSet> entry2 : references.entrySet()) {
            if (entry2.getValue().isEmpty()) continue;
            Identifier lv6 = lv3.getId(entry2.getKey());
            lv5.put(lv6.toString(), new NbtLongArray(entry2.getValue()));
        }
        lv.put("References", lv5);
        return lv;
    }

    private static Map<Structure, StructureStart> readStructureStarts(StructureContext context, NbtCompound nbt, long worldSeed) {
        HashMap<Structure, StructureStart> map = Maps.newHashMap();
        Registry<Structure> lv = context.registryManager().get(RegistryKeys.STRUCTURE);
        NbtCompound lv2 = nbt.getCompound("starts");
        for (String string : lv2.getKeys()) {
            Identifier lv3 = Identifier.tryParse(string);
            Structure lv4 = lv.get(lv3);
            if (lv4 == null) {
                LOGGER.error("Unknown structure start: {}", (Object)lv3);
                continue;
            }
            StructureStart lv5 = StructureStart.fromNbt(context, lv2.getCompound(string), worldSeed);
            if (lv5 == null) continue;
            map.put(lv4, lv5);
        }
        return map;
    }

    private static Map<Structure, LongSet> readStructureReferences(DynamicRegistryManager registryManager, ChunkPos pos, NbtCompound nbt) {
        HashMap<Structure, LongSet> map = Maps.newHashMap();
        Registry<Structure> lv = registryManager.get(RegistryKeys.STRUCTURE);
        NbtCompound lv2 = nbt.getCompound("References");
        for (String string : lv2.getKeys()) {
            Identifier lv3 = Identifier.tryParse(string);
            Structure lv4 = lv.get(lv3);
            if (lv4 == null) {
                LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", (Object)lv3, (Object)pos);
                continue;
            }
            long[] ls = lv2.getLongArray(string);
            if (ls.length == 0) continue;
            map.put(lv4, new LongOpenHashSet(Arrays.stream(ls).filter(packedPos -> {
                ChunkPos lv = new ChunkPos(packedPos);
                if (lv.getChebyshevDistance(pos) > 8) {
                    LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", lv3, lv, pos);
                    return false;
                }
                return true;
            }).toArray()));
        }
        return map;
    }

    public static NbtList toNbt(ShortList[] lists) {
        NbtList lv = new NbtList();
        for (ShortList shortList : lists) {
            NbtList lv2 = new NbtList();
            if (shortList != null) {
                for (Short short_ : shortList) {
                    lv2.add(NbtShort.of(short_));
                }
            }
            lv.add(lv2);
        }
        return lv;
    }

    public static class ChunkLoadingException
    extends NbtException {
        public ChunkLoadingException(String message) {
            super(message);
        }
    }
}

