/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.realms.util;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.Backoff;
import net.minecraft.util.TimeSupplier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class PeriodicRunnerFactory {
    static final Logger LOGGER = LogUtils.getLogger();
    final Executor executor;
    final TimeUnit timeUnit;
    final TimeSupplier timeSupplier;

    public PeriodicRunnerFactory(Executor executor, TimeUnit timeUnit, TimeSupplier timeSupplier) {
        this.executor = executor;
        this.timeUnit = timeUnit;
        this.timeSupplier = timeSupplier;
    }

    public <T> PeriodicRunner<T> create(String name, Callable<T> task, Duration cycle, Backoff backoff) {
        long l = this.timeUnit.convert(cycle);
        if (l == 0L) {
            throw new IllegalArgumentException("Period of " + String.valueOf(cycle) + " too short for selected resolution of " + String.valueOf((Object)this.timeUnit));
        }
        return new PeriodicRunner<T>(name, task, l, backoff);
    }

    public RunnersManager create() {
        return new RunnersManager();
    }

    @Environment(value=EnvType.CLIENT)
    public class PeriodicRunner<T> {
        private final String name;
        private final Callable<T> task;
        private final long unitDuration;
        private final Backoff backoff;
        @Nullable
        private CompletableFuture<TimedErrableResult<T>> resultFuture;
        @Nullable
        TimedResult<T> lastResult;
        private long nextTime = -1L;

        PeriodicRunner(String name, Callable<T> task, long unitDuration, Backoff backoff) {
            this.name = name;
            this.task = task;
            this.unitDuration = unitDuration;
            this.backoff = backoff;
        }

        void run(long currentTime) {
            if (this.resultFuture != null) {
                TimedErrableResult lv = this.resultFuture.getNow(null);
                if (lv == null) {
                    return;
                }
                this.resultFuture = null;
                long m = lv.time;
                lv.value().ifLeft(value -> {
                    this.lastResult = new TimedResult<Object>(value, m);
                    this.nextTime = m + this.unitDuration * this.backoff.success();
                }).ifRight(exception -> {
                    long m = this.backoff.fail();
                    LOGGER.warn("Failed to process task {}, will repeat after {} cycles", this.name, m, exception);
                    this.nextTime = m + this.unitDuration * m;
                });
            }
            if (this.nextTime <= currentTime) {
                this.resultFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        T object = this.task.call();
                        long l = PeriodicRunnerFactory.this.timeSupplier.get(PeriodicRunnerFactory.this.timeUnit);
                        return new TimedErrableResult<T>(Either.left(object), l);
                    } catch (Exception exception) {
                        long l = PeriodicRunnerFactory.this.timeSupplier.get(PeriodicRunnerFactory.this.timeUnit);
                        return new TimedErrableResult(Either.right(exception), l);
                    }
                }, PeriodicRunnerFactory.this.executor);
            }
        }

        public void reset() {
            this.resultFuture = null;
            this.lastResult = null;
            this.nextTime = -1L;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class RunnersManager {
        private final List<ResultListenableRunner<?>> runners = new ArrayList();

        public <T> void add(PeriodicRunner<T> runner, Consumer<T> resultListener) {
            ResultListenableRunner<T> lv = new ResultListenableRunner<T>(PeriodicRunnerFactory.this, runner, resultListener);
            this.runners.add(lv);
            lv.runListener();
        }

        public void forceRunListeners() {
            for (ResultListenableRunner<?> lv : this.runners) {
                lv.forceRunListener();
            }
        }

        public void runAll() {
            for (ResultListenableRunner<?> lv : this.runners) {
                lv.run(PeriodicRunnerFactory.this.timeSupplier.get(PeriodicRunnerFactory.this.timeUnit));
            }
        }

        public void resetAll() {
            for (ResultListenableRunner<?> lv : this.runners) {
                lv.reset();
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class ResultListenableRunner<T> {
        private final PeriodicRunner<T> runner;
        private final Consumer<T> resultListener;
        private long lastRunTime = -1L;

        ResultListenableRunner(PeriodicRunnerFactory arg, PeriodicRunner<T> runner, Consumer<T> resultListener) {
            this.runner = runner;
            this.resultListener = resultListener;
        }

        void run(long currentTime) {
            this.runner.run(currentTime);
            this.runListener();
        }

        void runListener() {
            TimedResult lv = this.runner.lastResult;
            if (lv != null && this.lastRunTime < lv.time) {
                this.resultListener.accept(lv.value);
                this.lastRunTime = lv.time;
            }
        }

        void forceRunListener() {
            TimedResult lv = this.runner.lastResult;
            if (lv != null) {
                this.resultListener.accept(lv.value);
                this.lastRunTime = lv.time;
            }
        }

        void reset() {
            this.runner.reset();
            this.lastRunTime = -1L;
        }
    }

    @Environment(value=EnvType.CLIENT)
    record TimedResult<T>(T value, long time) {
    }

    @Environment(value=EnvType.CLIENT)
    record TimedErrableResult<T>(Either<T, Exception> value, long time) {
    }
}

