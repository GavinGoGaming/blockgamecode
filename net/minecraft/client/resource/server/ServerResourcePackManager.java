/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.resource.server;

import com.google.common.hash.HashCode;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.server.DownloadQueuer;
import net.minecraft.client.resource.server.PackStateChangeCallback;
import net.minecraft.client.resource.server.ReloadScheduler;
import net.minecraft.util.Downloader;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ServerResourcePackManager {
    private final DownloadQueuer queuer;
    final PackStateChangeCallback stateChangeCallback;
    private final ReloadScheduler reloadScheduler;
    private final Runnable packChangeCallback;
    private AcceptanceStatus acceptanceStatus;
    final List<PackEntry> packs = new ArrayList<PackEntry>();

    public ServerResourcePackManager(DownloadQueuer queuer, PackStateChangeCallback stateChangeCallback, ReloadScheduler reloadScheduler, Runnable packChangeCallback, AcceptanceStatus acceptanceStatus) {
        this.queuer = queuer;
        this.stateChangeCallback = stateChangeCallback;
        this.reloadScheduler = reloadScheduler;
        this.packChangeCallback = packChangeCallback;
        this.acceptanceStatus = acceptanceStatus;
    }

    void onPackChanged() {
        this.packChangeCallback.run();
    }

    private void markReplaced(UUID id) {
        for (PackEntry lv : this.packs) {
            if (!lv.id.equals(id)) continue;
            lv.discard(DiscardReason.SERVER_REPLACED);
        }
    }

    public void addResourcePack(UUID id, URL url, @Nullable HashCode hashCode) {
        if (this.acceptanceStatus == AcceptanceStatus.DECLINED) {
            this.stateChangeCallback.onFinish(id, PackStateChangeCallback.FinishState.DECLINED);
            return;
        }
        this.onAdd(id, new PackEntry(id, url, hashCode));
    }

    public void addResourcePack(UUID id, Path path) {
        URL uRL;
        if (this.acceptanceStatus == AcceptanceStatus.DECLINED) {
            this.stateChangeCallback.onFinish(id, PackStateChangeCallback.FinishState.DECLINED);
            return;
        }
        try {
            uRL = path.toUri().toURL();
        } catch (MalformedURLException malformedURLException) {
            throw new IllegalStateException("Can't convert path to URL " + String.valueOf(path), malformedURLException);
        }
        PackEntry lv = new PackEntry(id, uRL, null);
        lv.loadStatus = LoadStatus.DONE;
        lv.path = path;
        this.onAdd(id, lv);
    }

    private void onAdd(UUID id, PackEntry pack) {
        this.markReplaced(id);
        this.packs.add(pack);
        if (this.acceptanceStatus == AcceptanceStatus.ALLOWED) {
            this.accept(pack);
        }
        this.onPackChanged();
    }

    private void accept(PackEntry pack) {
        this.stateChangeCallback.onStateChanged(pack.id, PackStateChangeCallback.State.ACCEPTED);
        pack.accepted = true;
    }

    @Nullable
    private PackEntry get(UUID id) {
        for (PackEntry lv : this.packs) {
            if (lv.isDiscarded() || !lv.id.equals(id)) continue;
            return lv;
        }
        return null;
    }

    public void remove(UUID id) {
        PackEntry lv = this.get(id);
        if (lv != null) {
            lv.discard(DiscardReason.SERVER_REMOVED);
            this.onPackChanged();
        }
    }

    public void removeAll() {
        for (PackEntry lv : this.packs) {
            lv.discard(DiscardReason.SERVER_REMOVED);
        }
        this.onPackChanged();
    }

    public void acceptAll() {
        this.acceptanceStatus = AcceptanceStatus.ALLOWED;
        for (PackEntry lv : this.packs) {
            if (lv.accepted || lv.isDiscarded()) continue;
            this.accept(lv);
        }
        this.onPackChanged();
    }

    public void declineAll() {
        this.acceptanceStatus = AcceptanceStatus.DECLINED;
        for (PackEntry lv : this.packs) {
            if (lv.accepted) continue;
            lv.discard(DiscardReason.DECLINED);
        }
        this.onPackChanged();
    }

    public void resetAcceptanceStatus() {
        this.acceptanceStatus = AcceptanceStatus.PENDING;
    }

    public void update() {
        boolean bl = this.enqueueDownloads();
        if (!bl) {
            this.applyDownloadedPacks();
        }
        this.removeInactivePacks();
    }

    private void removeInactivePacks() {
        this.packs.removeIf(pack -> {
            if (pack.status != Status.INACTIVE) {
                return false;
            }
            if (pack.discardReason != null) {
                PackStateChangeCallback.FinishState lv = pack.discardReason.state;
                if (lv != null) {
                    this.stateChangeCallback.onFinish(pack.id, lv);
                }
                return true;
            }
            return false;
        });
    }

    private void onDownload(Collection<PackEntry> packs, Downloader.DownloadResult result) {
        if (!result.failed().isEmpty()) {
            for (PackEntry lv : this.packs) {
                if (lv.status == Status.ACTIVE) continue;
                if (result.failed().contains(lv.id)) {
                    lv.discard(DiscardReason.DOWNLOAD_FAILED);
                    continue;
                }
                lv.discard(DiscardReason.DISCARDED);
            }
        }
        for (PackEntry lv : packs) {
            Path path = result.downloaded().get(lv.id);
            if (path == null) continue;
            lv.loadStatus = LoadStatus.DONE;
            lv.path = path;
            if (lv.isDiscarded()) continue;
            this.stateChangeCallback.onStateChanged(lv.id, PackStateChangeCallback.State.DOWNLOADED);
        }
        this.onPackChanged();
    }

    private boolean enqueueDownloads() {
        ArrayList<PackEntry> list = new ArrayList<PackEntry>();
        boolean bl = false;
        for (PackEntry lv : this.packs) {
            if (lv.isDiscarded() || !lv.accepted) continue;
            if (lv.loadStatus != LoadStatus.DONE) {
                bl = true;
            }
            if (lv.loadStatus != LoadStatus.REQUESTED) continue;
            lv.loadStatus = LoadStatus.PENDING;
            list.add(lv);
        }
        if (!list.isEmpty()) {
            HashMap<UUID, Downloader.DownloadEntry> map = new HashMap<UUID, Downloader.DownloadEntry>();
            for (PackEntry lv2 : list) {
                map.put(lv2.id, new Downloader.DownloadEntry(lv2.url, lv2.hashCode));
            }
            this.queuer.enqueue(map, result -> this.onDownload((Collection<PackEntry>)list, (Downloader.DownloadResult)result));
        }
        return bl;
    }

    private void applyDownloadedPacks() {
        boolean bl = false;
        final ArrayList<PackEntry> list = new ArrayList<PackEntry>();
        final ArrayList<PackEntry> list2 = new ArrayList<PackEntry>();
        for (PackEntry lv : this.packs) {
            boolean bl2;
            if (lv.status == Status.PENDING) {
                return;
            }
            boolean bl3 = bl2 = lv.accepted && lv.loadStatus == LoadStatus.DONE && !lv.isDiscarded();
            if (bl2 && lv.status == Status.INACTIVE) {
                list.add(lv);
                bl = true;
            }
            if (lv.status != Status.ACTIVE) continue;
            if (!bl2) {
                bl = true;
                list2.add(lv);
                continue;
            }
            list.add(lv);
        }
        if (bl) {
            for (PackEntry lv : list) {
                if (lv.status == Status.ACTIVE) continue;
                lv.status = Status.PENDING;
            }
            for (PackEntry lv : list2) {
                lv.status = Status.PENDING;
            }
            this.reloadScheduler.scheduleReload(new ReloadScheduler.ReloadContext(){

                @Override
                public void onSuccess() {
                    for (PackEntry lv : list) {
                        lv.status = Status.ACTIVE;
                        if (lv.discardReason != null) continue;
                        ServerResourcePackManager.this.stateChangeCallback.onFinish(lv.id, PackStateChangeCallback.FinishState.APPLIED);
                    }
                    for (PackEntry lv : list2) {
                        lv.status = Status.INACTIVE;
                    }
                    ServerResourcePackManager.this.onPackChanged();
                }

                @Override
                public void onFailure(boolean force) {
                    if (!force) {
                        list.clear();
                        for (PackEntry lv : ServerResourcePackManager.this.packs) {
                            switch (lv.status.ordinal()) {
                                case 2: {
                                    list.add(lv);
                                    break;
                                }
                                case 1: {
                                    lv.status = Status.INACTIVE;
                                    lv.discard(DiscardReason.ACTIVATION_FAILED);
                                    break;
                                }
                                case 0: {
                                    lv.discard(DiscardReason.DISCARDED);
                                }
                            }
                        }
                        ServerResourcePackManager.this.onPackChanged();
                    } else {
                        for (PackEntry lv : ServerResourcePackManager.this.packs) {
                            if (lv.status != Status.PENDING) continue;
                            lv.status = Status.INACTIVE;
                        }
                    }
                }

                @Override
                public List<ReloadScheduler.PackInfo> getPacks() {
                    return list.stream().map(pack -> new ReloadScheduler.PackInfo(pack.id, pack.path)).toList();
                }
            });
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum AcceptanceStatus {
        PENDING,
        ALLOWED,
        DECLINED;

    }

    @Environment(value=EnvType.CLIENT)
    static class PackEntry {
        final UUID id;
        final URL url;
        @Nullable
        final HashCode hashCode;
        @Nullable
        Path path;
        @Nullable
        DiscardReason discardReason;
        LoadStatus loadStatus = LoadStatus.REQUESTED;
        Status status = Status.INACTIVE;
        boolean accepted;

        PackEntry(UUID id, URL url, @Nullable HashCode hashCode) {
            this.id = id;
            this.url = url;
            this.hashCode = hashCode;
        }

        public void discard(DiscardReason reason) {
            if (this.discardReason == null) {
                this.discardReason = reason;
            }
        }

        public boolean isDiscarded() {
            return this.discardReason != null;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum DiscardReason {
        DOWNLOAD_FAILED(PackStateChangeCallback.FinishState.DOWNLOAD_FAILED),
        ACTIVATION_FAILED(PackStateChangeCallback.FinishState.ACTIVATION_FAILED),
        DECLINED(PackStateChangeCallback.FinishState.DECLINED),
        DISCARDED(PackStateChangeCallback.FinishState.DISCARDED),
        SERVER_REMOVED(null),
        SERVER_REPLACED(null);

        @Nullable
        final PackStateChangeCallback.FinishState state;

        private DiscardReason(PackStateChangeCallback.FinishState state) {
            this.state = state;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum LoadStatus {
        REQUESTED,
        PENDING,
        DONE;

    }

    @Environment(value=EnvType.CLIENT)
    static enum Status {
        INACTIVE,
        PENDING,
        ACTIVE;

    }
}

