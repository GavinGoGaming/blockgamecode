/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.realms.gui.screen;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPOutputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.realms.FileUpload;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.SizeUnit;
import net.minecraft.client.realms.UploadStatus;
import net.minecraft.client.realms.dto.UploadInfo;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.exception.RetryCallException;
import net.minecraft.client.realms.gui.screen.RealmsConfigureWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsCreateWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsLongRunningMcoTaskScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.realms.task.LongRunningTask;
import net.minecraft.client.realms.task.SwitchSlotTask;
import net.minecraft.client.realms.task.WorldCreationTask;
import net.minecraft.client.realms.util.UploadTokenCache;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsUploadScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ReentrantLock UPLOAD_LOCK = new ReentrantLock();
    private static final int field_41776 = 200;
    private static final int field_41773 = 80;
    private static final int field_41774 = 95;
    private static final int field_41775 = 1;
    private static final String[] DOTS = new String[]{"", ".", ". .", ". . ."};
    private static final Text VERIFYING_TEXT = Text.translatable("mco.upload.verifying");
    private final RealmsCreateWorldScreen parent;
    private final LevelSummary selectedLevel;
    @Nullable
    private final WorldCreationTask creationTask;
    private final long worldId;
    private final int slotId;
    private final UploadStatus uploadStatus;
    private final RateLimiter narrationRateLimiter;
    @Nullable
    private volatile Text[] statusTexts;
    private volatile Text status = Text.translatable("mco.upload.preparing");
    @Nullable
    private volatile String progress;
    private volatile boolean cancelled;
    private volatile boolean uploadFinished;
    private volatile boolean showDots = true;
    private volatile boolean uploadStarted;
    @Nullable
    private ButtonWidget backButton;
    @Nullable
    private ButtonWidget cancelButton;
    private int animTick;
    @Nullable
    private Long previousWrittenBytes;
    @Nullable
    private Long previousTimeSnapshot;
    private long bytesPerSecond;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

    public RealmsUploadScreen(@Nullable WorldCreationTask creationTask, long worldId, int slotId, RealmsCreateWorldScreen parent, LevelSummary selectedLevel) {
        super(NarratorManager.EMPTY);
        this.creationTask = creationTask;
        this.worldId = worldId;
        this.slotId = slotId;
        this.parent = parent;
        this.selectedLevel = selectedLevel;
        this.uploadStatus = new UploadStatus();
        this.narrationRateLimiter = RateLimiter.create(0.1f);
    }

    @Override
    public void init() {
        this.backButton = this.layout.addFooter(ButtonWidget.builder(ScreenTexts.BACK, button -> this.onBack()).build());
        this.backButton.visible = false;
        this.cancelButton = this.layout.addFooter(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.onCancel()).build());
        if (!this.uploadStarted) {
            if (this.parent.slot == -1) {
                this.uploadStarted = true;
                this.upload();
            } else {
                ArrayList<LongRunningTask> list = new ArrayList<LongRunningTask>();
                if (this.creationTask != null) {
                    list.add(this.creationTask);
                }
                list.add(new SwitchSlotTask(this.worldId, this.parent.slot, () -> {
                    if (!this.uploadStarted) {
                        this.uploadStarted = true;
                        this.client.execute(() -> {
                            this.client.setScreen(this);
                            this.upload();
                        });
                    }
                }));
                this.client.setScreen(new RealmsLongRunningMcoTaskScreen(this.parent, list.toArray(new LongRunningTask[0])));
            }
        }
        this.layout.forEachChild(child -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
        });
        this.initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        this.layout.refreshPositions();
    }

    private void onBack() {
        this.client.setScreen(new RealmsConfigureWorldScreen(new RealmsMainScreen(new TitleScreen()), this.worldId));
    }

    private void onCancel() {
        this.cancelled = true;
        this.client.setScreen(this.parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (this.showDots) {
                this.onCancel();
            } else {
                this.onBack();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Text[] lvs;
        super.render(context, mouseX, mouseY, delta);
        if (!this.uploadFinished && this.uploadStatus.bytesWritten != 0L && this.uploadStatus.bytesWritten == this.uploadStatus.totalBytes && this.cancelButton != null) {
            this.status = VERIFYING_TEXT;
            this.cancelButton.active = false;
        }
        context.drawCenteredTextWithShadow(this.textRenderer, this.status, this.width / 2, 50, Colors.WHITE);
        if (this.showDots) {
            context.drawText(this.textRenderer, DOTS[this.animTick / 10 % DOTS.length], this.width / 2 + this.textRenderer.getWidth(this.status) / 2 + 5, 50, Colors.WHITE, false);
        }
        if (this.uploadStatus.bytesWritten != 0L && !this.cancelled) {
            this.drawProgressBar(context);
            this.drawUploadSpeed(context);
        }
        if ((lvs = this.statusTexts) != null) {
            for (int k = 0; k < lvs.length; ++k) {
                context.drawCenteredTextWithShadow(this.textRenderer, lvs[k], this.width / 2, 110 + 12 * k, Colors.RED);
            }
        }
    }

    private void drawProgressBar(DrawContext context) {
        double d = Math.min((double)this.uploadStatus.bytesWritten / (double)this.uploadStatus.totalBytes, 1.0);
        this.progress = String.format(Locale.ROOT, "%.1f", d * 100.0);
        int i = (this.width - 200) / 2;
        int j = i + (int)Math.round(200.0 * d);
        context.fill(i - 1, 79, j + 1, 96, Colors.WHITE);
        context.fill(i, 80, j, 95, Colors.GRAY);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("mco.upload.percent", this.progress), this.width / 2, 84, Colors.WHITE);
    }

    private void drawUploadSpeed(DrawContext context) {
        if (this.animTick % 20 == 0) {
            if (this.previousWrittenBytes != null && this.previousTimeSnapshot != null) {
                long l = Util.getMeasuringTimeMs() - this.previousTimeSnapshot;
                if (l == 0L) {
                    l = 1L;
                }
                this.bytesPerSecond = 1000L * (this.uploadStatus.bytesWritten - this.previousWrittenBytes) / l;
                this.drawUploadSpeed0(context, this.bytesPerSecond);
            }
            this.previousWrittenBytes = this.uploadStatus.bytesWritten;
            this.previousTimeSnapshot = Util.getMeasuringTimeMs();
        } else {
            this.drawUploadSpeed0(context, this.bytesPerSecond);
        }
    }

    private void drawUploadSpeed0(DrawContext context, long bytesPerSecond) {
        String string = this.progress;
        if (bytesPerSecond > 0L && string != null) {
            int i = this.textRenderer.getWidth(string);
            String string2 = "(" + SizeUnit.getUserFriendlyString(bytesPerSecond) + "/s)";
            context.drawText(this.textRenderer, string2, this.width / 2 + i / 2 + 15, 84, Colors.WHITE, false);
        }
    }

    @Override
    public void tick() {
        super.tick();
        ++this.animTick;
        if (this.narrationRateLimiter.tryAcquire(1)) {
            Text lv = this.getNarration();
            this.client.getNarratorManager().narrate(lv);
        }
    }

    private Text getNarration() {
        Text[] lvs;
        ArrayList<Text> list = Lists.newArrayList();
        list.add(this.status);
        if (this.progress != null) {
            list.add(Text.translatable("mco.upload.percent", this.progress));
        }
        if ((lvs = this.statusTexts) != null) {
            list.addAll(Arrays.asList(lvs));
        }
        return ScreenTexts.joinLines(list);
    }

    private void upload() {
        new Thread(() -> {
            File file = null;
            RealmsClient lv = RealmsClient.create();
            try {
                if (!UPLOAD_LOCK.tryLock(1L, TimeUnit.SECONDS)) {
                    this.status = Text.translatable("mco.upload.close.failure");
                    return;
                }
                UploadInfo lv2 = null;
                for (int i = 0; i < 20; ++i) {
                    block37: {
                        if (!this.cancelled) break block37;
                        this.uploadCancelled();
                        return;
                    }
                    try {
                        lv2 = lv.upload(this.worldId, UploadTokenCache.get(this.worldId));
                        if (lv2 == null) continue;
                        break;
                    } catch (RetryCallException lv3) {
                        Thread.sleep(lv3.delaySeconds * 1000);
                    }
                }
                if (lv2 == null) {
                    this.status = Text.translatable("mco.upload.close.failure");
                    return;
                }
                UploadTokenCache.put(this.worldId, lv2.getToken());
                if (!lv2.isWorldClosed()) {
                    this.status = Text.translatable("mco.upload.close.failure");
                    return;
                }
                if (this.cancelled) {
                    this.uploadCancelled();
                    return;
                }
                File file2 = new File(this.client.runDirectory.getAbsolutePath(), "saves");
                file = this.tarGzipArchive(new File(file2, this.selectedLevel.getName()));
                if (this.cancelled) {
                    this.uploadCancelled();
                    return;
                }
                if (!this.verify(file)) {
                    long l = file.length();
                    SizeUnit lv4 = SizeUnit.getLargestUnit(l);
                    SizeUnit lv5 = SizeUnit.getLargestUnit(0x140000000L);
                    if (SizeUnit.humanReadableSize(l, lv4).equals(SizeUnit.humanReadableSize(0x140000000L, lv5)) && lv4 != SizeUnit.B) {
                        SizeUnit lv6 = SizeUnit.values()[lv4.ordinal() - 1];
                        this.setStatusTexts(Text.translatable("mco.upload.size.failure.line1", this.selectedLevel.getDisplayName()), Text.translatable("mco.upload.size.failure.line2", SizeUnit.humanReadableSize(l, lv6), SizeUnit.humanReadableSize(0x140000000L, lv6)));
                        return;
                    }
                    this.setStatusTexts(Text.translatable("mco.upload.size.failure.line1", this.selectedLevel.getDisplayName()), Text.translatable("mco.upload.size.failure.line2", SizeUnit.humanReadableSize(l, lv4), SizeUnit.humanReadableSize(0x140000000L, lv5)));
                    return;
                }
                this.status = Text.translatable("mco.upload.uploading", this.selectedLevel.getDisplayName());
                FileUpload lv7 = new FileUpload(file, this.worldId, this.slotId, lv2, this.client.getSession(), SharedConstants.getGameVersion().getName(), this.selectedLevel.getVersionInfo().getVersionName(), this.uploadStatus);
                lv7.upload(result -> {
                    if (result.statusCode >= 200 && result.statusCode < 300) {
                        this.uploadFinished = true;
                        this.status = Text.translatable("mco.upload.done");
                        if (this.backButton != null) {
                            this.backButton.setMessage(ScreenTexts.DONE);
                        }
                        UploadTokenCache.invalidate(this.worldId);
                    } else if (result.statusCode == 400 && result.errorMessage != null) {
                        this.setStatusTexts(Text.translatable("mco.upload.failed", result.errorMessage));
                    } else {
                        this.setStatusTexts(Text.translatable("mco.upload.failed", result.statusCode));
                    }
                });
                while (!lv7.isFinished()) {
                    if (this.cancelled) {
                        lv7.cancel();
                        this.uploadCancelled();
                        return;
                    }
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException interruptedException) {
                        LOGGER.error("Failed to check Realms file upload status");
                    }
                }
            } catch (IOException iOException) {
                this.setStatusTexts(Text.translatable("mco.upload.failed", iOException.getMessage()));
            } catch (RealmsServiceException lv8) {
                this.setStatusTexts(Text.translatable("mco.upload.failed", lv8.error.getText()));
            } catch (InterruptedException interruptedException2) {
                LOGGER.error("Could not acquire upload lock");
            } finally {
                this.uploadFinished = true;
                if (!UPLOAD_LOCK.isHeldByCurrentThread()) {
                    return;
                }
                UPLOAD_LOCK.unlock();
                this.showDots = false;
                if (this.backButton != null) {
                    this.backButton.visible = true;
                }
                if (this.cancelButton != null) {
                    this.cancelButton.visible = false;
                }
                if (file != null) {
                    LOGGER.debug("Deleting file {}", (Object)file.getAbsolutePath());
                    file.delete();
                }
            }
        }).start();
    }

    private void setStatusTexts(Text ... statusTexts) {
        this.statusTexts = statusTexts;
    }

    private void uploadCancelled() {
        this.status = Text.translatable("mco.upload.cancelled");
        LOGGER.debug("Upload was cancelled");
    }

    private boolean verify(File archive) {
        return archive.length() < 0x140000000L;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private File tarGzipArchive(File pathToDirectoryFile) throws IOException {
        try (TarArchiveOutputStream tarArchiveOutputStream = null;){
            File file2 = File.createTempFile("realms-upload-file", ".tar.gz");
            tarArchiveOutputStream = new TarArchiveOutputStream(new GZIPOutputStream(new FileOutputStream(file2)));
            tarArchiveOutputStream.setLongFileMode(3);
            this.addFileToTarGz(tarArchiveOutputStream, pathToDirectoryFile.getAbsolutePath(), "world", true);
            tarArchiveOutputStream.finish();
            File file = file2;
            return file;
        }
    }

    private void addFileToTarGz(TarArchiveOutputStream tOut, String path, String base, boolean root) throws IOException {
        if (this.cancelled) {
            return;
        }
        File file = new File(path);
        String string3 = root ? base : base + file.getName();
        TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(file, string3);
        tOut.putArchiveEntry(tarArchiveEntry);
        if (file.isFile()) {
            try (FileInputStream inputStream = new FileInputStream(file);){
                ((InputStream)inputStream).transferTo(tOut);
            }
            tOut.closeArchiveEntry();
        } else {
            tOut.closeArchiveEntry();
            File[] files = file.listFiles();
            if (files != null) {
                for (File file2 : files) {
                    this.addFileToTarGz(tOut, file2.getAbsolutePath(), string3 + "/", false);
                }
            }
        }
    }
}

