/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.option;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.HotbarStorageEntry;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class HotbarStorage {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int STORAGE_ENTRY_COUNT = 9;
    private final Path file;
    private final DataFixer dataFixer;
    private final HotbarStorageEntry[] entries = new HotbarStorageEntry[9];
    private boolean loaded;

    public HotbarStorage(Path directory, DataFixer dataFixer) {
        this.file = directory.resolve("hotbar.nbt");
        this.dataFixer = dataFixer;
        for (int i = 0; i < 9; ++i) {
            this.entries[i] = new HotbarStorageEntry();
        }
    }

    private void load() {
        try {
            NbtCompound lv = NbtIo.read(this.file);
            if (lv == null) {
                return;
            }
            int i = NbtHelper.getDataVersion(lv, 1343);
            lv = DataFixTypes.HOTBAR.update(this.dataFixer, lv, i);
            for (int j = 0; j < 9; ++j) {
                this.entries[j] = HotbarStorageEntry.CODEC.parse(NbtOps.INSTANCE, lv.get(String.valueOf(j))).resultOrPartial(error -> LOGGER.warn("Failed to parse hotbar: {}", error)).orElseGet(HotbarStorageEntry::new);
            }
        } catch (Exception exception) {
            LOGGER.error("Failed to load creative mode options", exception);
        }
    }

    public void save() {
        try {
            NbtCompound lv = NbtHelper.putDataVersion(new NbtCompound());
            for (int i = 0; i < 9; ++i) {
                HotbarStorageEntry lv2 = this.getSavedHotbar(i);
                DataResult<NbtElement> dataResult = HotbarStorageEntry.CODEC.encodeStart(NbtOps.INSTANCE, lv2);
                lv.put(String.valueOf(i), dataResult.getOrThrow());
            }
            NbtIo.write(lv, this.file);
        } catch (Exception exception) {
            LOGGER.error("Failed to save creative mode options", exception);
        }
    }

    public HotbarStorageEntry getSavedHotbar(int i) {
        if (!this.loaded) {
            this.load();
            this.loaded = true;
        }
        return this.entries[i];
    }
}

