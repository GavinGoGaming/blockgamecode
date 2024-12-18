/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Locale;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.structure.JigsawJunction;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.JigsawStructure;
import org.slf4j.Logger;

public class PoolStructurePiece
extends StructurePiece {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final StructurePoolElement poolElement;
    protected BlockPos pos;
    private final int groundLevelDelta;
    protected final BlockRotation rotation;
    private final List<JigsawJunction> junctions = Lists.newArrayList();
    private final StructureTemplateManager structureTemplateManager;
    private final StructureLiquidSettings liquidSettings;

    public PoolStructurePiece(StructureTemplateManager structureTemplateManager, StructurePoolElement poolElement, BlockPos pos, int groundLevelDelta, BlockRotation rotation, BlockBox boundingBox, StructureLiquidSettings liquidSettings) {
        super(StructurePieceType.JIGSAW, 0, boundingBox);
        this.structureTemplateManager = structureTemplateManager;
        this.poolElement = poolElement;
        this.pos = pos;
        this.groundLevelDelta = groundLevelDelta;
        this.rotation = rotation;
        this.liquidSettings = liquidSettings;
    }

    public PoolStructurePiece(StructureContext context, NbtCompound nbt) {
        super(StructurePieceType.JIGSAW, nbt);
        this.structureTemplateManager = context.structureTemplateManager();
        this.pos = new BlockPos(nbt.getInt("PosX"), nbt.getInt("PosY"), nbt.getInt("PosZ"));
        this.groundLevelDelta = nbt.getInt("ground_level_delta");
        RegistryOps<NbtElement> dynamicOps = context.registryManager().getOps(NbtOps.INSTANCE);
        this.poolElement = (StructurePoolElement)StructurePoolElement.CODEC.parse(dynamicOps, nbt.getCompound("pool_element")).getPartialOrThrow(error -> new IllegalStateException("Invalid pool element found: " + error));
        this.rotation = BlockRotation.valueOf(nbt.getString("rotation"));
        this.boundingBox = this.poolElement.getBoundingBox(this.structureTemplateManager, this.pos, this.rotation);
        NbtList lv = nbt.getList("junctions", NbtElement.COMPOUND_TYPE);
        this.junctions.clear();
        lv.forEach(junctionTag -> this.junctions.add(JigsawJunction.deserialize(new Dynamic<NbtElement>((DynamicOps<NbtElement>)dynamicOps, (NbtElement)junctionTag))));
        this.liquidSettings = StructureLiquidSettings.codec.parse(NbtOps.INSTANCE, nbt.get("liquid_settings")).result().orElse(JigsawStructure.DEFAULT_LIQUID_SETTINGS);
    }

    @Override
    protected void writeNbt(StructureContext context, NbtCompound nbt) {
        nbt.putInt("PosX", this.pos.getX());
        nbt.putInt("PosY", this.pos.getY());
        nbt.putInt("PosZ", this.pos.getZ());
        nbt.putInt("ground_level_delta", this.groundLevelDelta);
        RegistryOps<NbtElement> dynamicOps = context.registryManager().getOps(NbtOps.INSTANCE);
        StructurePoolElement.CODEC.encodeStart(dynamicOps, this.poolElement).resultOrPartial(LOGGER::error).ifPresent(poolElement -> nbt.put("pool_element", (NbtElement)poolElement));
        nbt.putString("rotation", this.rotation.name());
        NbtList lv = new NbtList();
        for (JigsawJunction lv2 : this.junctions) {
            lv.add(lv2.serialize(dynamicOps).getValue());
        }
        nbt.put("junctions", lv);
        if (this.liquidSettings != JigsawStructure.DEFAULT_LIQUID_SETTINGS) {
            nbt.put("liquid_settings", StructureLiquidSettings.codec.encodeStart(NbtOps.INSTANCE, this.liquidSettings).getOrThrow());
        }
    }

    @Override
    public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        this.generate(world, structureAccessor, chunkGenerator, random, chunkBox, pivot, false);
    }

    public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, BlockPos pivot, boolean keepJigsaws) {
        this.poolElement.generate(this.structureTemplateManager, world, structureAccessor, chunkGenerator, this.pos, pivot, this.rotation, boundingBox, random, this.liquidSettings, keepJigsaws);
    }

    @Override
    public void translate(int x, int y, int z) {
        super.translate(x, y, z);
        this.pos = this.pos.add(x, y, z);
    }

    @Override
    public BlockRotation getRotation() {
        return this.rotation;
    }

    public String toString() {
        return String.format(Locale.ROOT, "<%s | %s | %s | %s>", this.getClass().getSimpleName(), this.pos, this.rotation, this.poolElement);
    }

    public StructurePoolElement getPoolElement() {
        return this.poolElement;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public int getGroundLevelDelta() {
        return this.groundLevelDelta;
    }

    public void addJunction(JigsawJunction junction) {
        this.junctions.add(junction);
    }

    public List<JigsawJunction> getJunctions() {
        return this.junctions;
    }
}

