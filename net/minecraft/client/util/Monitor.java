/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.util;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.VideoMode;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

@Environment(value=EnvType.CLIENT)
public final class Monitor {
    private final long handle;
    private final List<VideoMode> videoModes;
    private VideoMode currentVideoMode;
    private int x;
    private int y;

    public Monitor(long handle) {
        this.handle = handle;
        this.videoModes = Lists.newArrayList();
        this.populateVideoModes();
    }

    public void populateVideoModes() {
        this.videoModes.clear();
        GLFWVidMode.Buffer buffer = GLFW.glfwGetVideoModes(this.handle);
        for (int i = buffer.limit() - 1; i >= 0; --i) {
            buffer.position(i);
            VideoMode lv = new VideoMode(buffer);
            if (lv.getRedBits() < 8 || lv.getGreenBits() < 8 || lv.getBlueBits() < 8) continue;
            this.videoModes.add(lv);
        }
        int[] is = new int[1];
        int[] js = new int[1];
        GLFW.glfwGetMonitorPos(this.handle, is, js);
        this.x = is[0];
        this.y = js[0];
        GLFWVidMode gLFWVidMode = GLFW.glfwGetVideoMode(this.handle);
        this.currentVideoMode = new VideoMode(gLFWVidMode);
    }

    public VideoMode findClosestVideoMode(Optional<VideoMode> videoMode) {
        if (videoMode.isPresent()) {
            VideoMode lv = videoMode.get();
            for (VideoMode lv2 : this.videoModes) {
                if (!lv2.equals(lv)) continue;
                return lv2;
            }
        }
        return this.getCurrentVideoMode();
    }

    public int findClosestVideoModeIndex(VideoMode videoMode) {
        return this.videoModes.indexOf(videoMode);
    }

    public VideoMode getCurrentVideoMode() {
        return this.currentVideoMode;
    }

    public int getViewportX() {
        return this.x;
    }

    public int getViewportY() {
        return this.y;
    }

    public VideoMode getVideoMode(int index) {
        return this.videoModes.get(index);
    }

    public int getVideoModeCount() {
        return this.videoModes.size();
    }

    public long getHandle() {
        return this.handle;
    }

    public String toString() {
        return String.format(Locale.ROOT, "Monitor[%s %sx%s %s]", this.handle, this.x, this.y, this.currentVideoMode);
    }
}

