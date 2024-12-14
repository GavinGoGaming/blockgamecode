/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util;

import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceImmutableList;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.Schemas;
import net.minecraft.registry.Registry;
import net.minecraft.state.property.Property;
import net.minecraft.util.CachedMapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.TimeSupplier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.crash.ReportType;
import net.minecraft.util.function.CharPredicate;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class Util {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_PARALLELISM = 255;
    private static final int BACKUP_ATTEMPTS = 10;
    private static final String MAX_BG_THREADS_PROPERTY = "max.bg.threads";
    private static final ExecutorService MAIN_WORKER_EXECUTOR = Util.createWorker("Main");
    private static final ExecutorService IO_WORKER_EXECUTOR = Util.createIoWorker("IO-Worker-", false);
    private static final ExecutorService DOWNLOAD_WORKER_EXECUTOR = Util.createIoWorker("Download-", true);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);
    public static final int field_46220 = 8;
    private static final Set<String> SUPPORTED_URI_PROTOCOLS = Set.of("http", "https");
    public static final long field_45714 = 1000000L;
    public static TimeSupplier.Nanoseconds nanoTimeSupplier = System::nanoTime;
    public static final Ticker TICKER = new Ticker(){

        @Override
        public long read() {
            return nanoTimeSupplier.getAsLong();
        }
    };
    public static final UUID NIL_UUID = new UUID(0L, 0L);
    public static final FileSystemProvider JAR_FILE_SYSTEM_PROVIDER = FileSystemProvider.installedProviders().stream().filter(fileSystemProvider -> fileSystemProvider.getScheme().equalsIgnoreCase("jar")).findFirst().orElseThrow(() -> new IllegalStateException("No jar file system provider found"));
    private static Consumer<String> missingBreakpointHandler = message -> {};

    public static <K, V> Collector<Map.Entry<? extends K, ? extends V>, ?, Map<K, V>> toMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    public static <T> Collector<T, ?, List<T>> toArrayList() {
        return Collectors.toCollection(Lists::newArrayList);
    }

    public static <T extends Comparable<T>> String getValueAsString(Property<T> property, Object value) {
        return property.name((Comparable)value);
    }

    public static String createTranslationKey(String type, @Nullable Identifier id) {
        if (id == null) {
            return type + ".unregistered_sadface";
        }
        return type + "." + id.getNamespace() + "." + id.getPath().replace('/', '.');
    }

    public static long getMeasuringTimeMs() {
        return Util.getMeasuringTimeNano() / 1000000L;
    }

    public static long getMeasuringTimeNano() {
        return nanoTimeSupplier.getAsLong();
    }

    public static long getEpochTimeMs() {
        return Instant.now().toEpochMilli();
    }

    public static String getFormattedCurrentTime() {
        return DATE_TIME_FORMATTER.format(ZonedDateTime.now());
    }

    private static ExecutorService createWorker(String name) {
        ExecutorService executorService;
        int i = MathHelper.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, Util.getMaxBackgroundThreads());
        if (i <= 0) {
            executorService = MoreExecutors.newDirectExecutorService();
        } else {
            AtomicInteger atomicInteger = new AtomicInteger(1);
            executorService = new ForkJoinPool(i, pool -> {
                ForkJoinWorkerThread forkJoinWorkerThread = new ForkJoinWorkerThread(pool){

                    @Override
                    protected void onTermination(Throwable throwable) {
                        if (throwable != null) {
                            LOGGER.warn("{} died", (Object)this.getName(), (Object)throwable);
                        } else {
                            LOGGER.debug("{} shutdown", (Object)this.getName());
                        }
                        super.onTermination(throwable);
                    }
                };
                forkJoinWorkerThread.setName("Worker-" + name + "-" + atomicInteger.getAndIncrement());
                return forkJoinWorkerThread;
            }, Util::uncaughtExceptionHandler, true);
        }
        return executorService;
    }

    private static int getMaxBackgroundThreads() {
        String string = System.getProperty(MAX_BG_THREADS_PROPERTY);
        if (string != null) {
            try {
                int i = Integer.parseInt(string);
                if (i >= 1 && i <= 255) {
                    return i;
                }
                LOGGER.error("Wrong {} property value '{}'. Should be an integer value between 1 and {}.", MAX_BG_THREADS_PROPERTY, string, 255);
            } catch (NumberFormatException numberFormatException) {
                LOGGER.error("Could not parse {} property value '{}'. Should be an integer value between 1 and {}.", MAX_BG_THREADS_PROPERTY, string, 255);
            }
        }
        return 255;
    }

    public static ExecutorService getMainWorkerExecutor() {
        return MAIN_WORKER_EXECUTOR;
    }

    public static ExecutorService getIoWorkerExecutor() {
        return IO_WORKER_EXECUTOR;
    }

    public static ExecutorService getDownloadWorkerExecutor() {
        return DOWNLOAD_WORKER_EXECUTOR;
    }

    public static void shutdownExecutors() {
        Util.attemptShutdown(MAIN_WORKER_EXECUTOR);
        Util.attemptShutdown(IO_WORKER_EXECUTOR);
    }

    private static void attemptShutdown(ExecutorService service) {
        boolean bl;
        service.shutdown();
        try {
            bl = service.awaitTermination(3L, TimeUnit.SECONDS);
        } catch (InterruptedException interruptedException) {
            bl = false;
        }
        if (!bl) {
            service.shutdownNow();
        }
    }

    private static ExecutorService createIoWorker(String namePrefix, boolean daemon) {
        AtomicInteger atomicInteger = new AtomicInteger(1);
        return Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(namePrefix + atomicInteger.getAndIncrement());
            thread.setDaemon(daemon);
            thread.setUncaughtExceptionHandler(Util::uncaughtExceptionHandler);
            return thread;
        });
    }

    public static void throwUnchecked(Throwable t) {
        throw t instanceof RuntimeException ? (RuntimeException)t : new RuntimeException(t);
    }

    private static void uncaughtExceptionHandler(Thread thread, Throwable t) {
        Util.throwOrPause(t);
        if (t instanceof CompletionException) {
            t = t.getCause();
        }
        if (t instanceof CrashException) {
            CrashException lv = (CrashException)t;
            Bootstrap.println(lv.getReport().asString(ReportType.MINECRAFT_CRASH_REPORT));
            System.exit(-1);
        }
        LOGGER.error(String.format(Locale.ROOT, "Caught exception in thread %s", thread), t);
    }

    @Nullable
    public static Type<?> getChoiceType(DSL.TypeReference typeReference, String id) {
        if (!SharedConstants.useChoiceTypeRegistrations) {
            return null;
        }
        return Util.getChoiceTypeInternal(typeReference, id);
    }

    @Nullable
    private static Type<?> getChoiceTypeInternal(DSL.TypeReference typeReference, String id) {
        Type<?> type;
        block2: {
            type = null;
            try {
                type = Schemas.getFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getGameVersion().getSaveVersion().getId())).getChoiceType(typeReference, id);
            } catch (IllegalArgumentException illegalArgumentException) {
                LOGGER.error("No data fixer registered for {}", (Object)id);
                if (!SharedConstants.isDevelopment) break block2;
                throw illegalArgumentException;
            }
        }
        return type;
    }

    public static Runnable debugRunnable(String activeThreadName, Runnable task) {
        if (SharedConstants.isDevelopment) {
            return () -> {
                Thread thread = Thread.currentThread();
                String string2 = thread.getName();
                thread.setName(activeThreadName);
                try {
                    task.run();
                } finally {
                    thread.setName(string2);
                }
            };
        }
        return task;
    }

    public static <V> Supplier<V> debugSupplier(String activeThreadName, Supplier<V> supplier) {
        if (SharedConstants.isDevelopment) {
            return () -> {
                Thread thread = Thread.currentThread();
                String string2 = thread.getName();
                thread.setName(activeThreadName);
                try {
                    Object t = supplier.get();
                    return t;
                } finally {
                    thread.setName(string2);
                }
            };
        }
        return supplier;
    }

    public static <T> String registryValueToString(Registry<T> registry, T value) {
        Identifier lv = registry.getId(value);
        if (lv == null) {
            return "[unregistered]";
        }
        return lv.toString();
    }

    public static <T> Predicate<T> allOf(List<? extends Predicate<T>> predicates) {
        return switch (predicates.size()) {
            case 0 -> object -> true;
            case 1 -> predicates.get(0);
            case 2 -> predicates.get(0).and(predicates.get(1));
            default -> {
                Predicate[] predicates = (Predicate[])predicates.toArray(Predicate[]::new);
                yield object -> {
                    for (Predicate predicate : predicates) {
                        if (predicate.test(object)) continue;
                        return false;
                    }
                    return true;
                };
            }
        };
    }

    public static <T> Predicate<T> anyOf(List<? extends Predicate<T>> predicates) {
        return switch (predicates.size()) {
            case 0 -> object -> false;
            case 1 -> predicates.get(0);
            case 2 -> predicates.get(0).or(predicates.get(1));
            default -> {
                Predicate[] predicates = (Predicate[])predicates.toArray(Predicate[]::new);
                yield object -> {
                    for (Predicate predicate : predicates) {
                        if (!predicate.test(object)) continue;
                        return true;
                    }
                    return false;
                };
            }
        };
    }

    public static <T> boolean isSymmetrical(int width, int height, List<T> list) {
        if (width == 1) {
            return true;
        }
        int k = width / 2;
        for (int l = 0; l < height; ++l) {
            for (int m = 0; m < k; ++m) {
                T object2;
                int n = width - 1 - m;
                T object = list.get(m + l * width);
                if (object.equals(object2 = list.get(n + l * width))) continue;
                return false;
            }
        }
        return true;
    }

    public static OperatingSystem getOperatingSystem() {
        String string = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (string.contains("win")) {
            return OperatingSystem.WINDOWS;
        }
        if (string.contains("mac")) {
            return OperatingSystem.OSX;
        }
        if (string.contains("solaris")) {
            return OperatingSystem.SOLARIS;
        }
        if (string.contains("sunos")) {
            return OperatingSystem.SOLARIS;
        }
        if (string.contains("linux")) {
            return OperatingSystem.LINUX;
        }
        if (string.contains("unix")) {
            return OperatingSystem.LINUX;
        }
        return OperatingSystem.UNKNOWN;
    }

    public static URI validateUri(String uri) throws URISyntaxException {
        URI uRI = new URI(uri);
        String string2 = uRI.getScheme();
        if (string2 == null) {
            throw new URISyntaxException(uri, "Missing protocol in URI: " + uri);
        }
        String string3 = string2.toLowerCase(Locale.ROOT);
        if (!SUPPORTED_URI_PROTOCOLS.contains(string3)) {
            throw new URISyntaxException(uri, "Unsupported protocol in URI: " + uri);
        }
        return uRI;
    }

    public static Stream<String> getJVMFlags() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return runtimeMXBean.getInputArguments().stream().filter(runtimeArg -> runtimeArg.startsWith("-X"));
    }

    public static <T> T getLast(List<T> list) {
        return list.get(list.size() - 1);
    }

    public static <T> T next(Iterable<T> iterable, @Nullable T object) {
        Iterator<T> iterator = iterable.iterator();
        T object2 = iterator.next();
        if (object != null) {
            T object3 = object2;
            while (true) {
                if (object3 == object) {
                    if (!iterator.hasNext()) break;
                    return iterator.next();
                }
                if (!iterator.hasNext()) continue;
                object3 = iterator.next();
            }
        }
        return object2;
    }

    public static <T> T previous(Iterable<T> iterable, @Nullable T object) {
        Iterator<T> iterator = iterable.iterator();
        T object2 = null;
        while (iterator.hasNext()) {
            T object3 = iterator.next();
            if (object3 == object) {
                if (object2 != null) break;
                object2 = iterator.hasNext() ? Iterators.getLast(iterator) : object;
                break;
            }
            object2 = object3;
        }
        return object2;
    }

    public static <T> T make(Supplier<T> factory) {
        return factory.get();
    }

    public static <T> T make(T object, Consumer<? super T> initializer) {
        initializer.accept(object);
        return object;
    }

    public static <V> CompletableFuture<List<V>> combineSafe(List<? extends CompletableFuture<V>> futures) {
        if (futures.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }
        if (futures.size() == 1) {
            return futures.get(0).thenApply(List::of);
        }
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        return completableFuture.thenApply(void_ -> futures.stream().map(CompletableFuture::join).toList());
    }

    public static <V> CompletableFuture<List<V>> combine(List<? extends CompletableFuture<? extends V>> futures) {
        CompletableFuture completableFuture = new CompletableFuture();
        return Util.combine(futures, completableFuture::completeExceptionally).applyToEither((CompletionStage)completableFuture, Function.identity());
    }

    public static <V> CompletableFuture<List<V>> combineCancellable(List<? extends CompletableFuture<? extends V>> futures) {
        CompletableFuture completableFuture = new CompletableFuture();
        return Util.combine(futures, throwable -> {
            if (completableFuture.completeExceptionally((Throwable)throwable)) {
                for (CompletableFuture completableFuture2 : futures) {
                    completableFuture2.cancel(true);
                }
            }
        }).applyToEither((CompletionStage)completableFuture, Function.identity());
    }

    private static <V> CompletableFuture<List<V>> combine(List<? extends CompletableFuture<? extends V>> futures, Consumer<Throwable> exceptionHandler) {
        ArrayList list2 = Lists.newArrayListWithCapacity(futures.size());
        CompletableFuture[] completableFutures = new CompletableFuture[futures.size()];
        futures.forEach(future -> {
            int i = list2.size();
            list2.add(null);
            completableFutures[i] = future.whenComplete((value, throwable) -> {
                if (throwable != null) {
                    exceptionHandler.accept((Throwable)throwable);
                } else {
                    list2.set(i, value);
                }
            });
        });
        return CompletableFuture.allOf(completableFutures).thenApply(void_ -> list2);
    }

    public static <T> Optional<T> ifPresentOrElse(Optional<T> optional, Consumer<T> presentAction, Runnable elseAction) {
        if (optional.isPresent()) {
            presentAction.accept(optional.get());
        } else {
            elseAction.run();
        }
        return optional;
    }

    public static <T> Supplier<T> debugSupplier(Supplier<T> supplier, Supplier<String> messageSupplier) {
        return supplier;
    }

    public static Runnable debugRunnable(Runnable runnable, Supplier<String> messageSupplier) {
        return runnable;
    }

    public static void error(String message) {
        LOGGER.error(message);
        if (SharedConstants.isDevelopment) {
            Util.pause(message);
        }
    }

    public static void error(String message, Throwable throwable) {
        LOGGER.error(message, throwable);
        if (SharedConstants.isDevelopment) {
            Util.pause(message);
        }
    }

    public static <T extends Throwable> T throwOrPause(T t) {
        if (SharedConstants.isDevelopment) {
            LOGGER.error("Trying to throw a fatal exception, pausing in IDE", t);
            Util.pause(t.getMessage());
        }
        return t;
    }

    public static void setMissingBreakpointHandler(Consumer<String> missingBreakpointHandler) {
        Util.missingBreakpointHandler = missingBreakpointHandler;
    }

    private static void pause(String message) {
        boolean bl;
        Instant instant = Instant.now();
        LOGGER.warn("Did you remember to set a breakpoint here?");
        boolean bl2 = bl = Duration.between(instant, Instant.now()).toMillis() > 500L;
        if (!bl) {
            missingBreakpointHandler.accept(message);
        }
    }

    public static String getInnermostMessage(Throwable t) {
        if (t.getCause() != null) {
            return Util.getInnermostMessage(t.getCause());
        }
        if (t.getMessage() != null) {
            return t.getMessage();
        }
        return t.toString();
    }

    public static <T> T getRandom(T[] array, Random random) {
        return array[random.nextInt(array.length)];
    }

    public static int getRandom(int[] array, Random random) {
        return array[random.nextInt(array.length)];
    }

    public static <T> T getRandom(List<T> list, Random random) {
        return list.get(random.nextInt(list.size()));
    }

    public static <T> Optional<T> getRandomOrEmpty(List<T> list, Random random) {
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Util.getRandom(list, random));
    }

    private static BooleanSupplier renameTask(final Path src, final Path dest) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                try {
                    Files.move(src, dest, new CopyOption[0]);
                    return true;
                } catch (IOException iOException) {
                    LOGGER.error("Failed to rename", iOException);
                    return false;
                }
            }

            public String toString() {
                return "rename " + String.valueOf(src) + " to " + String.valueOf(dest);
            }
        };
    }

    private static BooleanSupplier deleteTask(final Path path) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                try {
                    Files.deleteIfExists(path);
                    return true;
                } catch (IOException iOException) {
                    LOGGER.warn("Failed to delete", iOException);
                    return false;
                }
            }

            public String toString() {
                return "delete old " + String.valueOf(path);
            }
        };
    }

    private static BooleanSupplier deletionVerifyTask(final Path path) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                return !Files.exists(path, new LinkOption[0]);
            }

            public String toString() {
                return "verify that " + String.valueOf(path) + " is deleted";
            }
        };
    }

    private static BooleanSupplier existenceCheckTask(final Path path) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                return Files.isRegularFile(path, new LinkOption[0]);
            }

            public String toString() {
                return "verify that " + String.valueOf(path) + " is present";
            }
        };
    }

    private static boolean attemptTasks(BooleanSupplier ... tasks) {
        for (BooleanSupplier booleanSupplier : tasks) {
            if (booleanSupplier.getAsBoolean()) continue;
            LOGGER.warn("Failed to execute {}", (Object)booleanSupplier);
            return false;
        }
        return true;
    }

    private static boolean attemptTasks(int retries, String taskName, BooleanSupplier ... tasks) {
        for (int j = 0; j < retries; ++j) {
            if (Util.attemptTasks(tasks)) {
                return true;
            }
            LOGGER.error("Failed to {}, retrying {}/{}", taskName, j, retries);
        }
        LOGGER.error("Failed to {}, aborting, progress might be lost", (Object)taskName);
        return false;
    }

    public static void backupAndReplace(Path current, Path newPath, Path backup) {
        Util.backupAndReplace(current, newPath, backup, false);
    }

    public static boolean backupAndReplace(Path current, Path newPath, Path backup, boolean noRestoreOnFail) {
        if (Files.exists(current, new LinkOption[0]) && !Util.attemptTasks(10, "create backup " + String.valueOf(backup), Util.deleteTask(backup), Util.renameTask(current, backup), Util.existenceCheckTask(backup))) {
            return false;
        }
        if (!Util.attemptTasks(10, "remove old " + String.valueOf(current), Util.deleteTask(current), Util.deletionVerifyTask(current))) {
            return false;
        }
        if (!Util.attemptTasks(10, "replace " + String.valueOf(current) + " with " + String.valueOf(newPath), Util.renameTask(newPath, current), Util.existenceCheckTask(current)) && !noRestoreOnFail) {
            Util.attemptTasks(10, "restore " + String.valueOf(current) + " from " + String.valueOf(backup), Util.renameTask(backup, current), Util.existenceCheckTask(current));
            return false;
        }
        return true;
    }

    public static int moveCursor(String string, int cursor, int delta) {
        int k = string.length();
        if (delta >= 0) {
            for (int l = 0; cursor < k && l < delta; ++l) {
                if (!Character.isHighSurrogate(string.charAt(cursor++)) || cursor >= k || !Character.isLowSurrogate(string.charAt(cursor))) continue;
                ++cursor;
            }
        } else {
            for (int l = delta; cursor > 0 && l < 0; ++l) {
                if (!Character.isLowSurrogate(string.charAt(--cursor)) || cursor <= 0 || !Character.isHighSurrogate(string.charAt(cursor - 1))) continue;
                --cursor;
            }
        }
        return cursor;
    }

    public static Consumer<String> addPrefix(String prefix, Consumer<String> consumer) {
        return string2 -> consumer.accept(prefix + string2);
    }

    public static DataResult<int[]> decodeFixedLengthArray(IntStream stream, int length) {
        int[] is = stream.limit(length + 1).toArray();
        if (is.length != length) {
            Supplier<String> supplier = () -> "Input is not a list of " + length + " ints";
            if (is.length >= length) {
                return DataResult.error(supplier, Arrays.copyOf(is, length));
            }
            return DataResult.error(supplier);
        }
        return DataResult.success(is);
    }

    public static DataResult<long[]> decodeFixedLengthArray(LongStream stream, int length) {
        long[] ls = stream.limit(length + 1).toArray();
        if (ls.length != length) {
            Supplier<String> supplier = () -> "Input is not a list of " + length + " longs";
            if (ls.length >= length) {
                return DataResult.error(supplier, Arrays.copyOf(ls, length));
            }
            return DataResult.error(supplier);
        }
        return DataResult.success(ls);
    }

    public static <T> DataResult<List<T>> decodeFixedLengthList(List<T> list, int length) {
        if (list.size() != length) {
            Supplier<String> supplier = () -> "Input is not a list of " + length + " elements";
            if (list.size() >= length) {
                return DataResult.error(supplier, list.subList(0, length));
            }
            return DataResult.error(supplier);
        }
        return DataResult.success(list);
    }

    public static void startTimerHack() {
        Thread thread = new Thread("Timer hack thread"){

            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(Integer.MAX_VALUE);
                    }
                } catch (InterruptedException interruptedException) {
                    LOGGER.warn("Timer hack thread interrupted, that really should not happen");
                    return;
                }
            }
        };
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
        thread.start();
    }

    public static void relativeCopy(Path src, Path dest, Path toCopy) throws IOException {
        Path path4 = src.relativize(toCopy);
        Path path5 = dest.resolve(path4);
        Files.copy(toCopy, path5, new CopyOption[0]);
    }

    public static String replaceInvalidChars(String string, CharPredicate predicate) {
        return string.toLowerCase(Locale.ROOT).chars().mapToObj(charCode -> predicate.test((char)charCode) ? Character.toString((char)charCode) : "_").collect(Collectors.joining());
    }

    public static <K, V> CachedMapper<K, V> cachedMapper(Function<K, V> mapper) {
        return new CachedMapper<K, V>(mapper);
    }

    public static <T, R> Function<T, R> memoize(final Function<T, R> function) {
        return new Function<T, R>(){
            private final Map<T, R> cache = new ConcurrentHashMap();

            @Override
            public R apply(T object) {
                return this.cache.computeIfAbsent(object, function);
            }

            public String toString() {
                return "memoize/1[function=" + String.valueOf(function) + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T, U, R> BiFunction<T, U, R> memoize(final BiFunction<T, U, R> biFunction) {
        return new BiFunction<T, U, R>(){
            private final Map<Pair<T, U>, R> cache = new ConcurrentHashMap();

            @Override
            public R apply(T a, U b) {
                return this.cache.computeIfAbsent(Pair.of(a, b), pair -> biFunction.apply(pair.getFirst(), pair.getSecond()));
            }

            public String toString() {
                return "memoize/2[function=" + String.valueOf(biFunction) + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T> List<T> copyShuffled(Stream<T> stream, Random random) {
        ObjectArrayList objectArrayList = stream.collect(ObjectArrayList.toList());
        Util.shuffle(objectArrayList, random);
        return objectArrayList;
    }

    public static IntArrayList shuffle(IntStream stream, Random random) {
        int i;
        IntArrayList intArrayList = IntArrayList.wrap(stream.toArray());
        for (int j = i = intArrayList.size(); j > 1; --j) {
            int k = random.nextInt(j);
            intArrayList.set(j - 1, intArrayList.set(k, intArrayList.getInt(j - 1)));
        }
        return intArrayList;
    }

    public static <T> List<T> copyShuffled(T[] array, Random random) {
        ObjectArrayList<T> objectArrayList = new ObjectArrayList<T>(array);
        Util.shuffle(objectArrayList, random);
        return objectArrayList;
    }

    public static <T> List<T> copyShuffled(ObjectArrayList<T> list, Random random) {
        ObjectArrayList<T> objectArrayList2 = new ObjectArrayList<T>(list);
        Util.shuffle(objectArrayList2, random);
        return objectArrayList2;
    }

    public static <T> void shuffle(List<T> list, Random random) {
        int i;
        for (int j = i = list.size(); j > 1; --j) {
            int k = random.nextInt(j);
            list.set(j - 1, list.set(k, list.get(j - 1)));
        }
    }

    public static <T> CompletableFuture<T> waitAndApply(Function<Executor, CompletableFuture<T>> resultFactory) {
        return Util.waitAndApply(resultFactory, CompletableFuture::isDone);
    }

    public static <T> T waitAndApply(Function<Executor, T> resultFactory, Predicate<T> donePredicate) {
        int i;
        LinkedBlockingQueue blockingQueue = new LinkedBlockingQueue();
        T object = resultFactory.apply(blockingQueue::add);
        while (!donePredicate.test(object)) {
            try {
                Runnable runnable = (Runnable)blockingQueue.poll(100L, TimeUnit.MILLISECONDS);
                if (runnable == null) continue;
                runnable.run();
            } catch (InterruptedException interruptedException) {
                LOGGER.warn("Interrupted wait");
                break;
            }
        }
        if ((i = blockingQueue.size()) > 0) {
            LOGGER.warn("Tasks left in queue: {}", (Object)i);
        }
        return object;
    }

    public static <T> ToIntFunction<T> lastIndexGetter(List<T> values) {
        int i = values.size();
        if (i < 8) {
            return values::indexOf;
        }
        Object2IntOpenHashMap<T> object2IntMap = new Object2IntOpenHashMap<T>(i);
        object2IntMap.defaultReturnValue(-1);
        for (int j = 0; j < i; ++j) {
            object2IntMap.put(values.get(j), j);
        }
        return object2IntMap;
    }

    public static <T> ToIntFunction<T> lastIdentityIndexGetter(List<T> values) {
        int i = values.size();
        if (i < 8) {
            ReferenceImmutableList<T> referenceList = new ReferenceImmutableList<T>(values);
            return referenceList::indexOf;
        }
        Reference2IntOpenHashMap<T> reference2IntMap = new Reference2IntOpenHashMap<T>(i);
        reference2IntMap.defaultReturnValue(-1);
        for (int j = 0; j < i; ++j) {
            reference2IntMap.put(values.get(j), j);
        }
        return reference2IntMap;
    }

    public static <A, B> Typed<B> apply(Typed<A> typed, Type<B> type, UnaryOperator<Dynamic<?>> modifier) {
        Dynamic<?> dynamic = typed.write().getOrThrow();
        return Util.readTyped(type, (Dynamic)modifier.apply(dynamic), true);
    }

    public static <T> Typed<T> readTyped(Type<T> type, Dynamic<?> value) {
        return Util.readTyped(type, value, false);
    }

    public static <T> Typed<T> readTyped(Type<T> type, Dynamic<?> value, boolean allowPartial) {
        DataResult<Typed> dataResult = type.readTyped(value).map(Pair::getFirst);
        try {
            if (allowPartial) {
                return dataResult.getPartialOrThrow(IllegalStateException::new);
            }
            return dataResult.getOrThrow(IllegalStateException::new);
        } catch (IllegalStateException illegalStateException) {
            CrashReport lv = CrashReport.create(illegalStateException, "Reading type");
            CrashReportSection lv2 = lv.addElement("Info");
            lv2.add("Data", value);
            lv2.add("Type", type);
            throw new CrashException(lv);
        }
    }

    public static <T> List<T> withAppended(List<T> list, T valueToAppend) {
        return ((ImmutableList.Builder)((ImmutableList.Builder)ImmutableList.builderWithExpectedSize(list.size() + 1).addAll(list)).add(valueToAppend)).build();
    }

    public static <T> List<T> withPrepended(T valueToPrepend, List<T> list) {
        return ((ImmutableList.Builder)((ImmutableList.Builder)ImmutableList.builderWithExpectedSize(list.size() + 1).add(valueToPrepend)).addAll(list)).build();
    }

    public static <K, V> Map<K, V> mapWith(Map<K, V> map, K keyToAppend, V valueToAppend) {
        return ImmutableMap.builderWithExpectedSize(map.size() + 1).putAll(map).put(keyToAppend, valueToAppend).buildKeepingLast();
    }

    public static enum OperatingSystem {
        LINUX("linux"),
        SOLARIS("solaris"),
        WINDOWS("windows"){

            @Override
            protected String[] getURIOpenCommand(URI uri) {
                return new String[]{"rundll32", "url.dll,FileProtocolHandler", uri.toString()};
            }
        }
        ,
        OSX("mac"){

            @Override
            protected String[] getURIOpenCommand(URI uri) {
                return new String[]{"open", uri.toString()};
            }
        }
        ,
        UNKNOWN("unknown");

        private final String name;

        OperatingSystem(String name) {
            this.name = name;
        }

        public void open(URI uri) {
            try {
                Process process = AccessController.doPrivileged(() -> Runtime.getRuntime().exec(this.getURIOpenCommand(uri)));
                process.getInputStream().close();
                process.getErrorStream().close();
                process.getOutputStream().close();
            } catch (IOException | PrivilegedActionException exception) {
                LOGGER.error("Couldn't open location '{}'", (Object)uri, (Object)exception);
            }
        }

        public void open(File file) {
            this.open(file.toURI());
        }

        public void open(Path path) {
            this.open(path.toUri());
        }

        protected String[] getURIOpenCommand(URI uri) {
            String string = uri.toString();
            if ("file".equals(uri.getScheme())) {
                string = string.replace("file:", "file://");
            }
            return new String[]{"xdg-open", string};
        }

        public void open(String uri) {
            try {
                this.open(new URI(uri));
            } catch (IllegalArgumentException | URISyntaxException exception) {
                LOGGER.error("Couldn't open uri '{}'", (Object)uri, (Object)exception);
            }
        }

        public String getName() {
            return this.name;
        }
    }
}

