/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.netty.util.ResourceLeakDetector;
import java.time.Duration;
import net.minecraft.GameVersion;
import net.minecraft.MinecraftVersion;
import net.minecraft.command.TranslatableBuiltInExceptions;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;

public class SharedConstants {
    @Deprecated
    public static final boolean IS_DEVELOPMENT_VERSION = false;
    @Deprecated
    public static final int WORLD_VERSION = 3953;
    @Deprecated
    public static final String CURRENT_SERIES = "main";
    @Deprecated
    public static final String VERSION_NAME = "1.21";
    @Deprecated
    public static final int RELEASE_TARGET_PROTOCOL_VERSION = 767;
    @Deprecated
    public static final int field_29736 = 203;
    public static final int SNBT_TOO_OLD_THRESHOLD = 3937;
    private static final int field_29708 = 30;
    public static final boolean CRASH_ON_UNCAUGHT_THREAD_EXCEPTION = false;
    @Deprecated
    public static final int RESOURCE_PACK_VERSION = 34;
    @Deprecated
    public static final int DATA_PACK_VERSION = 48;
    @Deprecated
    public static final int field_39963 = 1;
    public static final int field_39964 = 1;
    public static final String DATA_VERSION_KEY = "DataVersion";
    public static final boolean field_29745 = false;
    public static final boolean field_33851 = false;
    public static final boolean field_29747 = false;
    public static final boolean field_35006 = false;
    public static final boolean field_35563 = false;
    public static final boolean field_29748 = false;
    public static final boolean field_33753 = false;
    public static final boolean field_29749 = false;
    public static final boolean field_29750 = false;
    public static final boolean field_29751 = false;
    public static final boolean field_29752 = false;
    public static final boolean field_29753 = false;
    public static final boolean field_52311 = false;
    public static final boolean field_44779 = false;
    public static final boolean field_29754 = false;
    public static final boolean field_29755 = false;
    public static final boolean field_29756 = false;
    public static final boolean field_29676 = false;
    public static final boolean field_44582 = false;
    public static final boolean field_29677 = false;
    public static final boolean field_29678 = false;
    public static final boolean field_29679 = false;
    public static final boolean field_29680 = false;
    public static final boolean field_29681 = false;
    public static final boolean field_29682 = false;
    public static final boolean field_29683 = false;
    public static final boolean field_29684 = false;
    public static final boolean field_29685 = false;
    public static final boolean field_29686 = false;
    public static final boolean field_29687 = false;
    public static final boolean field_29688 = false;
    public static final boolean field_29689 = false;
    public static final boolean field_29690 = false;
    public static final boolean field_29691 = false;
    public static final boolean field_29692 = false;
    public static final boolean field_29693 = false;
    public static final boolean field_29694 = false;
    public static final boolean field_29695 = false;
    public static final boolean field_29696 = false;
    public static final boolean field_29697 = false;
    public static final boolean field_29698 = false;
    public static final boolean field_29700 = false;
    public static final boolean field_33554 = false;
    public static final boolean field_37273 = false;
    public static final boolean field_39090 = false;
    public static final boolean field_39460 = false;
    public static final boolean field_39962 = false;
    public static final boolean field_46154 = false;
    public static final boolean field_47176 = false;
    public static final boolean field_47177 = false;
    public static final boolean field_48778 = false;
    public static final boolean field_47178 = false;
    public static final boolean field_34368 = false;
    public static final boolean field_29710 = false;
    public static final boolean field_34369 = false;
    public static final boolean field_34370 = false;
    public static boolean DEBUG_BIOME_SOURCE = false;
    public static boolean DEBUG_NOISE = false;
    public static final boolean field_29711 = false;
    public static final boolean field_29712 = false;
    public static final boolean field_29713 = false;
    public static final boolean field_29715 = false;
    public static final boolean field_29716 = false;
    public static final boolean field_29717 = false;
    public static final boolean field_29718 = false;
    public static final boolean field_33555 = false;
    public static final boolean field_35438 = false;
    public static final boolean field_35439 = false;
    public static final int DEFAULT_PORT = 25565;
    public static final boolean field_29720 = false;
    public static final boolean field_29721 = false;
    public static final int field_29722 = 0;
    public static final int field_29723 = 0;
    public static final ResourceLeakDetector.Level RESOURCE_LEAK_DETECTOR_DISABLED = ResourceLeakDetector.Level.DISABLED;
    public static final boolean field_29724 = false;
    public static final boolean field_29725 = false;
    public static final boolean field_29726 = false;
    public static final boolean field_35652 = false;
    public static final boolean field_39961 = false;
    public static final boolean field_41533 = false;
    public static final boolean field_44780 = false;
    public static final long field_22251 = Duration.ofMillis(300L).toNanos();
    public static final float field_49016 = 3600000.0f;
    public static final boolean field_44583 = false;
    public static final boolean field_49773 = false;
    public static boolean useChoiceTypeRegistrations = true;
    public static boolean isDevelopment;
    public static final int CHUNK_WIDTH = 16;
    public static final int DEFAULT_WORLD_HEIGHT = 256;
    public static final int COMMAND_MAX_LENGTH = 32500;
    public static final int EXPANDED_MACRO_COMMAND_MAX_LENGTH = 2000000;
    public static final int field_49170 = 16;
    public static final int field_38052 = 1000000;
    public static final int field_39898 = 32;
    public static final char[] INVALID_CHARS_LEVEL_NAME;
    public static final int TICKS_PER_SECOND = 20;
    public static final int field_44973 = 50;
    public static final int TICKS_PER_MINUTE = 1200;
    public static final int TICKS_PER_IN_GAME_DAY = 24000;
    public static final float field_29705 = 1365.3334f;
    public static final float field_29706 = 0.87890625f;
    public static final float field_29707 = 17.578125f;
    public static final int field_44922 = 64;
    @Nullable
    private static GameVersion gameVersion;

    public static void setGameVersion(GameVersion gameVersion) {
        if (SharedConstants.gameVersion == null) {
            SharedConstants.gameVersion = gameVersion;
        } else if (gameVersion != SharedConstants.gameVersion) {
            throw new IllegalStateException("Cannot override the current game version!");
        }
    }

    public static void createGameVersion() {
        if (gameVersion == null) {
            gameVersion = MinecraftVersion.create();
        }
    }

    public static GameVersion getGameVersion() {
        if (gameVersion == null) {
            throw new IllegalStateException("Game version not set");
        }
        return gameVersion;
    }

    public static int getProtocolVersion() {
        return 767;
    }

    public static boolean isOutsideGenerationArea(ChunkPos pos) {
        int i = pos.getStartX();
        int j = pos.getStartZ();
        if (DEBUG_BIOME_SOURCE) {
            return i > 8192 || i < 0 || j > 1024 || j < 0;
        }
        return false;
    }

    static {
        INVALID_CHARS_LEVEL_NAME = new char[]{'/', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};
        ResourceLeakDetector.setLevel(RESOURCE_LEAK_DETECTOR_DISABLED);
        CommandSyntaxException.ENABLE_COMMAND_STACK_TRACES = false;
        CommandSyntaxException.BUILT_IN_EXCEPTIONS = new TranslatableBuiltInExceptions();
    }
}

