/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ChunkTaskPrioritySystem;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.BelowZeroRetrogen;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationContext;
import net.minecraft.world.chunk.ChunkGenerationStep;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.Blender;

public class ChunkGenerating {
    private static boolean isLightOn(Chunk chunk) {
        return chunk.getStatus().isAtLeast(ChunkStatus.LIGHT) && chunk.isLightOn();
    }

    static CompletableFuture<Chunk> noop(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    static CompletableFuture<Chunk> generateStructures(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        ServerWorld lv = context.world();
        if (lv.getServer().getSaveProperties().getGeneratorOptions().shouldGenerateStructures()) {
            context.generator().setStructureStarts(lv.getRegistryManager(), lv.getChunkManager().getStructurePlacementCalculator(), lv.getStructureAccessor(), chunk, context.structureManager());
        }
        lv.cacheStructures(chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    static CompletableFuture<Chunk> loadStructures(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        context.world().cacheStructures(chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    static CompletableFuture<Chunk> generateStructureReferences(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        ServerWorld lv = context.world();
        ChunkRegion lv2 = new ChunkRegion(lv, chunks, step, chunk);
        context.generator().addStructureReferences(lv2, lv.getStructureAccessor().forRegion(lv2), chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    static CompletableFuture<Chunk> populateBiomes(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        ServerWorld lv = context.world();
        ChunkRegion lv2 = new ChunkRegion(lv, chunks, step, chunk);
        return context.generator().populateBiomes(lv.getChunkManager().getNoiseConfig(), Blender.getBlender(lv2), lv.getStructureAccessor().forRegion(lv2), chunk);
    }

    static CompletableFuture<Chunk> populateNoise(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        ServerWorld lv = context.world();
        ChunkRegion lv2 = new ChunkRegion(lv, chunks, step, chunk);
        return context.generator().populateNoise(Blender.getBlender(lv2), lv.getChunkManager().getNoiseConfig(), lv.getStructureAccessor().forRegion(lv2), chunk).thenApply(populated -> {
            ProtoChunk lv;
            BelowZeroRetrogen lv2;
            if (populated instanceof ProtoChunk && (lv2 = (lv = (ProtoChunk)populated).getBelowZeroRetrogen()) != null) {
                BelowZeroRetrogen.replaceOldBedrock(lv);
                if (lv2.hasMissingBedrock()) {
                    lv2.fillColumnsWithAirIfMissingBedrock(lv);
                }
            }
            return populated;
        });
    }

    static CompletableFuture<Chunk> buildSurface(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        ServerWorld lv = context.world();
        ChunkRegion lv2 = new ChunkRegion(lv, chunks, step, chunk);
        context.generator().buildSurface(lv2, lv.getStructureAccessor().forRegion(lv2), lv.getChunkManager().getNoiseConfig(), chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    static CompletableFuture<Chunk> carve(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        ServerWorld lv = context.world();
        ChunkRegion lv2 = new ChunkRegion(lv, chunks, step, chunk);
        if (chunk instanceof ProtoChunk) {
            ProtoChunk lv3 = (ProtoChunk)chunk;
            Blender.createCarvingMasks(lv2, lv3);
        }
        context.generator().carve(lv2, lv.getSeed(), lv.getChunkManager().getNoiseConfig(), lv.getBiomeAccess(), lv.getStructureAccessor().forRegion(lv2), chunk, GenerationStep.Carver.AIR);
        return CompletableFuture.completedFuture(chunk);
    }

    static CompletableFuture<Chunk> generateFeatures(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        ServerWorld lv = context.world();
        Heightmap.populateHeightmaps(chunk, EnumSet.of(Heightmap.Type.MOTION_BLOCKING, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, Heightmap.Type.OCEAN_FLOOR, Heightmap.Type.WORLD_SURFACE));
        ChunkRegion lv2 = new ChunkRegion(lv, chunks, step, chunk);
        context.generator().generateFeatures(lv2, chunk, lv.getStructureAccessor().forRegion(lv2));
        Blender.tickLeavesAndFluids(lv2, chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    static CompletableFuture<Chunk> initializeLight(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        ServerLightingProvider lv = context.lightingProvider();
        chunk.refreshSurfaceY();
        ((ProtoChunk)chunk).setLightingProvider(lv);
        boolean bl = ChunkGenerating.isLightOn(chunk);
        return lv.initializeLight(chunk, bl);
    }

    static CompletableFuture<Chunk> light(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        boolean bl = ChunkGenerating.isLightOn(chunk);
        return context.lightingProvider().light(chunk, bl);
    }

    static CompletableFuture<Chunk> generateEntities(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        if (!chunk.hasBelowZeroRetrogen()) {
            context.generator().populateEntities(new ChunkRegion(context.world(), chunks, step, chunk));
        }
        return CompletableFuture.completedFuture(chunk);
    }

    static CompletableFuture<Chunk> convertToFullChunk(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        ChunkPos lv = chunk.getPos();
        AbstractChunkHolder lv2 = chunks.get(lv.x, lv.z);
        return CompletableFuture.supplyAsync(() -> {
            WorldChunk lv3;
            ProtoChunk lv = (ProtoChunk)chunk;
            ServerWorld lv2 = context.world();
            if (lv instanceof WrapperProtoChunk) {
                lv3 = ((WrapperProtoChunk)lv).getWrappedChunk();
            } else {
                lv3 = new WorldChunk(lv2, lv, arg3 -> ChunkGenerating.addEntities(lv2, lv.getEntities()));
                lv2.replaceWith(new WrapperProtoChunk(lv3, false));
            }
            lv3.setLevelTypeProvider(lv2::getLevelType);
            lv3.loadEntities();
            lv3.setLoadedToWorld(true);
            lv3.updateAllBlockEntities();
            lv3.addChunkTickSchedulers(lv2);
            return lv3;
        }, runnable -> context.mainThreadMailBox().send(ChunkTaskPrioritySystem.createMessage(runnable, lv.toLong(), lv2::getLevel)));
    }

    private static void addEntities(ServerWorld world, List<NbtCompound> entities) {
        if (!entities.isEmpty()) {
            world.addEntities(EntityType.streamFromNbt(entities, world));
        }
    }
}

