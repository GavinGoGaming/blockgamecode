/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render.chunk;

import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class BlockBufferBuilderPool {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Queue<BlockBufferAllocatorStorage> availableBuilders;
    private volatile int availableBuilderCount;

    private BlockBufferBuilderPool(List<BlockBufferAllocatorStorage> availableBuilders) {
        this.availableBuilders = Queues.newArrayDeque(availableBuilders);
        this.availableBuilderCount = this.availableBuilders.size();
    }

    public static BlockBufferBuilderPool allocate(int max) {
        int j = Math.max(1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3) / BlockBufferAllocatorStorage.EXPECTED_TOTAL_SIZE);
        int k = Math.max(1, Math.min(max, j));
        ArrayList<BlockBufferAllocatorStorage> list = new ArrayList<BlockBufferAllocatorStorage>(k);
        try {
            for (int l = 0; l < k; ++l) {
                list.add(new BlockBufferAllocatorStorage());
            }
        } catch (OutOfMemoryError outOfMemoryError) {
            LOGGER.warn("Allocated only {}/{} buffers", (Object)list.size(), (Object)k);
            int m = Math.min(list.size() * 2 / 3, list.size() - 1);
            for (int n = 0; n < m; ++n) {
                ((BlockBufferAllocatorStorage)list.remove(list.size() - 1)).close();
            }
        }
        return new BlockBufferBuilderPool(list);
    }

    @Nullable
    public BlockBufferAllocatorStorage acquire() {
        BlockBufferAllocatorStorage lv = this.availableBuilders.poll();
        if (lv != null) {
            this.availableBuilderCount = this.availableBuilders.size();
            return lv;
        }
        return null;
    }

    public void release(BlockBufferAllocatorStorage builders) {
        this.availableBuilders.add(builders);
        this.availableBuilderCount = this.availableBuilders.size();
    }

    public boolean hasNoAvailableBuilder() {
        return this.availableBuilders.isEmpty();
    }

    public int getAvailableBuilderCount() {
        return this.availableBuilderCount;
    }
}

