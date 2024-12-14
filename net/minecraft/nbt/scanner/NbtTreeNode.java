/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.nbt.scanner;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.scanner.NbtScanQuery;

public record NbtTreeNode(int depth, Map<String, NbtType<?>> selectedFields, Map<String, NbtTreeNode> fieldsToRecurse) {
    private NbtTreeNode(int depth) {
        this(depth, new HashMap(), new HashMap<String, NbtTreeNode>());
    }

    public static NbtTreeNode createRoot() {
        return new NbtTreeNode(1);
    }

    public void add(NbtScanQuery query) {
        if (this.depth <= query.path().size()) {
            this.fieldsToRecurse.computeIfAbsent(query.path().get(this.depth - 1), path -> new NbtTreeNode(this.depth + 1)).add(query);
        } else {
            this.selectedFields.put(query.key(), query.type());
        }
    }

    public boolean isTypeEqual(NbtType<?> type, String key) {
        return type.equals(this.selectedFields().get(key));
    }
}

