/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.nbt.scanner;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.scanner.NbtCollector;
import net.minecraft.nbt.scanner.NbtScanQuery;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.scanner.NbtTreeNode;

public class ExclusiveNbtCollector
extends NbtCollector {
    private final Deque<NbtTreeNode> treeStack = new ArrayDeque<NbtTreeNode>();

    public ExclusiveNbtCollector(NbtScanQuery ... excludedQueries) {
        NbtTreeNode lv = NbtTreeNode.createRoot();
        for (NbtScanQuery lv2 : excludedQueries) {
            lv.add(lv2);
        }
        this.treeStack.push(lv);
    }

    @Override
    public NbtScanner.NestedResult startSubNbt(NbtType<?> type, String key) {
        NbtTreeNode lv2;
        NbtTreeNode lv = this.treeStack.element();
        if (lv.isTypeEqual(type, key)) {
            return NbtScanner.NestedResult.SKIP;
        }
        if (type == NbtCompound.TYPE && (lv2 = lv.fieldsToRecurse().get(key)) != null) {
            this.treeStack.push(lv2);
        }
        return super.startSubNbt(type, key);
    }

    @Override
    public NbtScanner.Result endNested() {
        if (this.getDepth() == this.treeStack.element().depth()) {
            this.treeStack.pop();
        }
        return super.endNested();
    }
}

