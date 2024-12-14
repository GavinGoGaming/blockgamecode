/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;

public record StructurePiecesList(List<StructurePiece> pieces) {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier JIGSAW = Identifier.ofVanilla("jigsaw");
    private static final Map<Identifier, Identifier> ID_UPDATES = ImmutableMap.builder().put(Identifier.ofVanilla("nvi"), JIGSAW).put(Identifier.ofVanilla("pcp"), JIGSAW).put(Identifier.ofVanilla("bastionremnant"), JIGSAW).put(Identifier.ofVanilla("runtime"), JIGSAW).build();

    public StructurePiecesList(List<StructurePiece> pieces) {
        this.pieces = List.copyOf(pieces);
    }

    public boolean isEmpty() {
        return this.pieces.isEmpty();
    }

    public boolean contains(BlockPos pos) {
        for (StructurePiece lv : this.pieces) {
            if (!lv.getBoundingBox().contains(pos)) continue;
            return true;
        }
        return false;
    }

    public NbtElement toNbt(StructureContext context) {
        NbtList lv = new NbtList();
        for (StructurePiece lv2 : this.pieces) {
            lv.add(lv2.toNbt(context));
        }
        return lv;
    }

    public static StructurePiecesList fromNbt(NbtList list, StructureContext context) {
        ArrayList<StructurePiece> list2 = Lists.newArrayList();
        for (int i = 0; i < list.size(); ++i) {
            NbtCompound lv = list.getCompound(i);
            String string = lv.getString("id").toLowerCase(Locale.ROOT);
            Identifier lv2 = Identifier.of(string);
            Identifier lv3 = ID_UPDATES.getOrDefault(lv2, lv2);
            StructurePieceType lv4 = Registries.STRUCTURE_PIECE.get(lv3);
            if (lv4 == null) {
                LOGGER.error("Unknown structure piece id: {}", (Object)lv3);
                continue;
            }
            try {
                StructurePiece lv5 = lv4.load(context, lv);
                list2.add(lv5);
                continue;
            } catch (Exception exception) {
                LOGGER.error("Exception loading structure piece with id {}", (Object)lv3, (Object)exception);
            }
        }
        return new StructurePiecesList(list2);
    }

    public BlockBox getBoundingBox() {
        return StructurePiece.boundingBox(this.pieces.stream());
    }
}

