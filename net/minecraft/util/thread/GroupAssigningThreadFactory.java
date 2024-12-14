/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.thread;

import com.mojang.logging.LogUtils;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;

public class GroupAssigningThreadFactory
implements ThreadFactory {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ThreadGroup group;
    private final AtomicInteger nextIndex = new AtomicInteger(1);
    private final String prefix;

    public GroupAssigningThreadFactory(String name) {
        SecurityManager securityManager = System.getSecurityManager();
        this.group = securityManager != null ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.prefix = name + "-";
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread2 = new Thread(this.group, r, this.prefix + this.nextIndex.getAndIncrement(), 0L);
        thread2.setUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.error("Caught exception in thread {} from {}", (Object)thread, (Object)r);
            LOGGER.error("", throwable);
        });
        if (thread2.getPriority() != 5) {
            thread2.setPriority(5);
        }
        return thread2;
    }
}

