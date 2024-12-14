/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.realms;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.mojang.logging.LogUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.realms.dto.WorldDownload;
import net.minecraft.client.realms.exception.RealmsDefaultUncaughtExceptionHandler;
import net.minecraft.client.realms.gui.screen.RealmsDownloadLatestWorldScreen;
import net.minecraft.nbt.NbtCrashException;
import net.minecraft.nbt.NbtException;
import net.minecraft.util.path.SymlinkValidationException;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class FileDownload {
    static final Logger LOGGER = LogUtils.getLogger();
    volatile boolean cancelled;
    volatile boolean finished;
    volatile boolean error;
    volatile boolean extracting;
    @Nullable
    private volatile File backupFile;
    volatile File resourcePackPath;
    @Nullable
    private volatile HttpGet httpRequest;
    @Nullable
    private Thread currentThread;
    private final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(120000).setConnectTimeout(120000).build();
    private static final String[] INVALID_FILE_NAMES = new String[]{"CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long contentLength(String downloadLink) {
        Closeable closeableHttpClient = null;
        HttpGet httpGet = null;
        try {
            httpGet = new HttpGet(downloadLink);
            closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
            CloseableHttpResponse closeableHttpResponse = ((CloseableHttpClient)closeableHttpClient).execute(httpGet);
            long l = Long.parseLong(closeableHttpResponse.getFirstHeader("Content-Length").getValue());
            return l;
        } catch (Throwable throwable) {
            LOGGER.error("Unable to get content length for download");
            long l = 0L;
            return l;
        } finally {
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
            if (closeableHttpClient != null) {
                try {
                    closeableHttpClient.close();
                } catch (IOException iOException) {
                    LOGGER.error("Could not close http client", iOException);
                }
            }
        }
    }

    public void downloadWorld(WorldDownload download, String message, RealmsDownloadLatestWorldScreen.DownloadStatus status, LevelStorage storage) {
        if (this.currentThread != null) {
            return;
        }
        this.currentThread = new Thread(() -> {
            Closeable closeableHttpClient = null;
            try {
                this.backupFile = File.createTempFile("backup", ".tar.gz");
                this.httpRequest = new HttpGet(arg.downloadLink);
                closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
                CloseableHttpResponse httpResponse = ((CloseableHttpClient)closeableHttpClient).execute(this.httpRequest);
                arg2.totalBytes = Long.parseLong(httpResponse.getFirstHeader("Content-Length").getValue());
                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                    this.error = true;
                    this.httpRequest.abort();
                    return;
                }
                FileOutputStream outputStream2 = new FileOutputStream(this.backupFile);
                ProgressListener lv3 = new ProgressListener(message.trim(), this.backupFile, storage, status);
                DownloadCountingOutputStream lv4 = new DownloadCountingOutputStream(outputStream2);
                lv4.setListener(lv3);
                IOUtils.copy(httpResponse.getEntity().getContent(), (OutputStream)lv4);
                return;
            } catch (Exception exception2) {
                LOGGER.error("Caught exception while downloading: {}", (Object)exception2.getMessage());
                this.error = true;
                return;
            } finally {
                block40: {
                    block41: {
                        CloseableHttpResponse httpResponse;
                        this.httpRequest.releaseConnection();
                        if (this.backupFile != null) {
                            this.backupFile.delete();
                        }
                        if (this.error) break block40;
                        if (arg.resourcePackUrl.isEmpty() || arg.resourcePackHash.isEmpty()) break block41;
                        try {
                            this.backupFile = File.createTempFile("resources", ".tar.gz");
                            this.httpRequest = new HttpGet(arg.resourcePackUrl);
                            httpResponse = ((CloseableHttpClient)closeableHttpClient).execute(this.httpRequest);
                            arg2.totalBytes = Long.parseLong(httpResponse.getFirstHeader("Content-Length").getValue());
                            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                                this.error = true;
                                this.httpRequest.abort();
                                return;
                            }
                        } catch (Exception exception2) {
                            LOGGER.error("Caught exception while downloading: {}", (Object)exception2.getMessage());
                            this.error = true;
                        }
                        FileOutputStream outputStream2 = new FileOutputStream(this.backupFile);
                        ResourcePackProgressListener lv5 = new ResourcePackProgressListener(this.backupFile, status, download);
                        DownloadCountingOutputStream lv4 = new DownloadCountingOutputStream(outputStream2);
                        lv4.setListener(lv5);
                        IOUtils.copy(httpResponse.getEntity().getContent(), (OutputStream)lv4);
                        break block40;
                        finally {
                            this.httpRequest.releaseConnection();
                            if (this.backupFile != null) {
                                this.backupFile.delete();
                            }
                        }
                    }
                    this.finished = true;
                }
                if (closeableHttpClient != null) {
                    try {
                        closeableHttpClient.close();
                    } catch (IOException iOException2) {
                        LOGGER.error("Failed to close Realms download client");
                    }
                }
            }
        });
        this.currentThread.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
        this.currentThread.start();
    }

    public void cancel() {
        if (this.httpRequest != null) {
            this.httpRequest.abort();
        }
        if (this.backupFile != null) {
            this.backupFile.delete();
        }
        this.cancelled = true;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public boolean isError() {
        return this.error;
    }

    public boolean isExtracting() {
        return this.extracting;
    }

    public static String findAvailableFolderName(String folder) {
        folder = ((String)folder).replaceAll("[\\./\"]", "_");
        for (String string2 : INVALID_FILE_NAMES) {
            if (!((String)folder).equalsIgnoreCase(string2)) continue;
            folder = "_" + (String)folder + "_";
        }
        return folder;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void untarGzipArchive(String name, @Nullable File archive, LevelStorage storage) throws IOException {
        Object string3;
        Pattern pattern = Pattern.compile(".*-([0-9]+)$");
        int i = 1;
        for (char c : SharedConstants.INVALID_CHARS_LEVEL_NAME) {
            name = name.replace(c, '_');
        }
        if (StringUtils.isEmpty(name)) {
            name = "Realm";
        }
        name = FileDownload.findAvailableFolderName(name);
        try {
            Object object = storage.getLevelList().iterator();
            while (object.hasNext()) {
                LevelStorage.LevelSave lv = (LevelStorage.LevelSave)object.next();
                String string2 = lv.getRootPath();
                if (!string2.toLowerCase(Locale.ROOT).startsWith(name.toLowerCase(Locale.ROOT))) continue;
                Matcher matcher = pattern.matcher(string2);
                if (matcher.matches()) {
                    int j = Integer.parseInt(matcher.group(1));
                    if (j <= i) continue;
                    i = j;
                    continue;
                }
                ++i;
            }
        } catch (Exception exception) {
            LOGGER.error("Error getting level list", exception);
            this.error = true;
            return;
        }
        if (!storage.isLevelNameValid(name) || i > 1) {
            string3 = name + (String)(i == 1 ? "" : "-" + i);
            if (!storage.isLevelNameValid((String)string3)) {
                boolean bl = false;
                while (!bl) {
                    if (!storage.isLevelNameValid((String)(string3 = name + (String)(++i == 1 ? "" : "-" + i)))) continue;
                    bl = true;
                }
            }
        } else {
            string3 = name;
        }
        TarArchiveInputStream tarArchiveInputStream = null;
        File file2 = new File(MinecraftClient.getInstance().runDirectory.getAbsolutePath(), "saves");
        try {
            file2.mkdir();
            tarArchiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(archive))));
            TarArchiveEntry tarArchiveEntry = tarArchiveInputStream.getNextTarEntry();
            while (tarArchiveEntry != null) {
                File file3 = new File(file2, tarArchiveEntry.getName().replace("world", (CharSequence)string3));
                if (tarArchiveEntry.isDirectory()) {
                    file3.mkdirs();
                } else {
                    file3.createNewFile();
                    try (FileOutputStream fileOutputStream = new FileOutputStream(file3);){
                        IOUtils.copy((InputStream)tarArchiveInputStream, (OutputStream)fileOutputStream);
                    }
                }
                tarArchiveEntry = tarArchiveInputStream.getNextTarEntry();
            }
        } catch (Exception exception2) {
            LOGGER.error("Error extracting world", exception2);
            this.error = true;
        } finally {
            if (tarArchiveInputStream != null) {
                tarArchiveInputStream.close();
            }
            if (archive != null) {
                archive.delete();
            }
            try (LevelStorage.Session lv2 = storage.createSession((String)string3);){
                lv2.removePlayerAndSave((String)string3);
            } catch (IOException | NbtCrashException | NbtException exception2) {
                LOGGER.error("Failed to modify unpacked realms level {}", string3, (Object)exception2);
            } catch (SymlinkValidationException lv3) {
                LOGGER.warn("{}", (Object)lv3.getMessage());
            }
            this.resourcePackPath = new File(file2, (String)string3 + File.separator + "resources.zip");
        }
    }

    @Environment(value=EnvType.CLIENT)
    class ResourcePackProgressListener
    implements ActionListener {
        private final File tempFile;
        private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;
        private final WorldDownload worldDownload;

        ResourcePackProgressListener(File tempFile, RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus, WorldDownload worldDownload) {
            this.tempFile = tempFile;
            this.downloadStatus = downloadStatus;
            this.worldDownload = worldDownload;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.downloadStatus.bytesWritten = ((DownloadCountingOutputStream)e.getSource()).getByteCount();
            if (this.downloadStatus.bytesWritten >= this.downloadStatus.totalBytes && !FileDownload.this.cancelled) {
                try {
                    String string = Hashing.sha1().hashBytes(Files.toByteArray(this.tempFile)).toString();
                    if (string.equals(this.worldDownload.resourcePackHash)) {
                        FileUtils.copyFile(this.tempFile, FileDownload.this.resourcePackPath);
                        FileDownload.this.finished = true;
                    } else {
                        LOGGER.error("Resourcepack had wrong hash (expected {}, found {}). Deleting it.", (Object)this.worldDownload.resourcePackHash, (Object)string);
                        FileUtils.deleteQuietly(this.tempFile);
                        FileDownload.this.error = true;
                    }
                } catch (IOException iOException) {
                    LOGGER.error("Error copying resourcepack file: {}", (Object)iOException.getMessage());
                    FileDownload.this.error = true;
                }
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class DownloadCountingOutputStream
    extends CountingOutputStream {
        @Nullable
        private ActionListener listener;

        public DownloadCountingOutputStream(OutputStream stream) {
            super(stream);
        }

        public void setListener(ActionListener listener) {
            this.listener = listener;
        }

        @Override
        protected void afterWrite(int n) throws IOException {
            super.afterWrite(n);
            if (this.listener != null) {
                this.listener.actionPerformed(new ActionEvent(this, 0, null));
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class ProgressListener
    implements ActionListener {
        private final String worldName;
        private final File tempFile;
        private final LevelStorage levelStorageSource;
        private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;

        ProgressListener(String worldName, File tempFile, LevelStorage levelStorageSource, RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus) {
            this.worldName = worldName;
            this.tempFile = tempFile;
            this.levelStorageSource = levelStorageSource;
            this.downloadStatus = downloadStatus;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.downloadStatus.bytesWritten = ((DownloadCountingOutputStream)e.getSource()).getByteCount();
            if (this.downloadStatus.bytesWritten >= this.downloadStatus.totalBytes && !FileDownload.this.cancelled && !FileDownload.this.error) {
                try {
                    FileDownload.this.extracting = true;
                    FileDownload.this.untarGzipArchive(this.worldName, this.tempFile, this.levelStorageSource);
                } catch (IOException iOException) {
                    LOGGER.error("Error extracting archive", iOException);
                    FileDownload.this.error = true;
                }
            }
        }
    }
}

