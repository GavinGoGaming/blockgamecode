/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

import java.util.concurrent.CompletableFuture;

public interface ResourceReload {
    public CompletableFuture<?> whenComplete();

    public float getProgress();

    default public boolean isComplete() {
        return this.whenComplete().isDone();
    }

    default public void throwException() {
        CompletableFuture<?> completableFuture = this.whenComplete();
        if (completableFuture.isCompletedExceptionally()) {
            completableFuture.join();
        }
    }
}

