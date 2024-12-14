/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.structure;

import com.mojang.logging.LogUtils;
import java.util.List;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.OceanMonumentStructure;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class StructureStart {
    public static final String INVALID = "INVALID";
    public static final StructureStart DEFAULT = new StructureStart(null, new ChunkPos(0, 0), 0, new StructurePiecesList(List.of()));
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Structure structure;
    private final StructurePiecesList children;
    private final ChunkPos pos;
    private int references;
    @Nullable
    private volatile BlockBox boundingBox;

    public StructureStart(Structure structure, ChunkPos pos, int references, StructurePiecesList children) {
        this.structure = structure;
        this.pos = pos;
        this.references = references;
        this.children = children;
    }

    @Nullable
    public static StructureStart fromNbt(StructureContext context, NbtCompound nbt, long seed) {
        String string = nbt.getString("id");
        if (INVALID.equals(string)) {
            return DEFAULT;
        }
        Registry<Structure> lv = context.registryManager().get(RegistryKeys.STRUCTURE);
        Structure lv2 = lv.get(Identifier.of(string));
        if (lv2 == null) {
            LOGGER.error("Unknown stucture id: {}", (Object)string);
            return null;
        }
        ChunkPos lv3 = new ChunkPos(nbt.getInt("ChunkX"), nbt.getInt("ChunkZ"));
        int i = nbt.getInt("references");
        NbtList lv4 = nbt.getList("Children", NbtElement.COMPOUND_TYPE);
        try {
            StructurePiecesList lv5 = StructurePiecesList.fromNbt(lv4, context);
            if (lv2 instanceof OceanMonumentStructure) {
                lv5 = OceanMonumentStructure.modifyPiecesOnRead(lv3, seed, lv5);
            }
            return new StructureStart(lv2, lv3, i, lv5);
        } catch (Exception exception) {
            LOGGER.error("Failed Start with id {}", (Object)string, (Object)exception);
            return null;
        }
    }

    public BlockBox getBoundingBox() {
        BlockBox lv = this.boundingBox;
        if (lv == null) {
            this.boundingBox = lv = this.structure.expandBoxIfShouldAdaptNoise(this.children.getBoundingBox());
        }
        return lv;
    }

    public void place(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos) {
        List<StructurePiece> list = this.children.pieces();
        if (list.isEmpty()) {
            return;
        }
        BlockBox lv = list.get((int)0).boundingBox;
        BlockPos lv2 = lv.getCenter();
        BlockPos lv3 = new BlockPos(lv2.getX(), lv.getMinY(), lv2.getZ());
        for (StructurePiece lv4 : list) {
            if (!lv4.getBoundingBox().intersects(chunkBox)) continue;
            lv4.generate(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, lv3);
        }
        this.structure.postPlace(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, this.children);
    }

    public NbtCompound toNbt(StructureContext context, ChunkPos chunkPos) {
        NbtCompound lv = new NbtCompound();
        if (!this.hasChildren()) {
            lv.putString("id", INVALID);
            return lv;
        }
        lv.putString("id", context.registryManager().get(RegistryKeys.STRUCTURE).getId(this.structure).toString());
        lv.putInt("ChunkX", chunkPos.x);
        lv.putInt("ChunkZ", chunkPos.z);
        lv.putInt("references", this.references);
        lv.put("Children", this.children.toNbt(context));
        return lv;
    }

    public boolean hasChildren() {
        return !this.children.isEmpty();
    }

    public ChunkPos getPos() {
        return this.pos;
    }

    public boolean isNeverReferenced() {
        return this.references < this.getMinReferencedStructureReferenceCount();
    }

    public void incrementReferences() {
        ++this.references;
    }

    public int getReferences() {
        return this.references;
    }

    protected int getMinReferencedStructureReferenceCount() {
        return 1;
    }

    public Structure getStructure() {
        return this.structure;
    }

    public List<StructurePiece> getChildren() {
        return this.children.pieces();
    }
}

