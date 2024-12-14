/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.texture;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.Animator;
import net.minecraft.client.texture.MipmapHelper;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.client.texture.TextureStitcher;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class SpriteContents
implements TextureStitcher.Stitchable,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Identifier id;
    final int width;
    final int height;
    private final NativeImage image;
    NativeImage[] mipmapLevelsImages;
    @Nullable
    private final Animation animation;
    private final ResourceMetadata metadata;

    public SpriteContents(Identifier id, SpriteDimensions dimensions, NativeImage image, ResourceMetadata metadata) {
        this.id = id;
        this.width = dimensions.width();
        this.height = dimensions.height();
        this.metadata = metadata;
        AnimationResourceMetadata lv = metadata.decode(AnimationResourceMetadata.READER).orElse(AnimationResourceMetadata.EMPTY);
        this.animation = this.createAnimation(dimensions, image.getWidth(), image.getHeight(), lv);
        this.image = image;
        this.mipmapLevelsImages = new NativeImage[]{this.image};
    }

    public void generateMipmaps(int mipmapLevels) {
        try {
            this.mipmapLevelsImages = MipmapHelper.getMipmapLevelsImages(this.mipmapLevelsImages, mipmapLevels);
        } catch (Throwable throwable) {
            CrashReport lv = CrashReport.create(throwable, "Generating mipmaps for frame");
            CrashReportSection lv2 = lv.addElement("Sprite being mipmapped");
            lv2.add("First frame", () -> {
                StringBuilder stringBuilder = new StringBuilder();
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(this.image.getWidth()).append("x").append(this.image.getHeight());
                return stringBuilder.toString();
            });
            CrashReportSection lv3 = lv.addElement("Frame being iterated");
            lv3.add("Sprite name", this.id);
            lv3.add("Sprite size", () -> this.width + " x " + this.height);
            lv3.add("Sprite frames", () -> this.getFrameCount() + " frames");
            lv3.add("Mipmap levels", mipmapLevels);
            throw new CrashException(lv);
        }
    }

    private int getFrameCount() {
        return this.animation != null ? this.animation.frames.size() : 1;
    }

    @Nullable
    private Animation createAnimation(SpriteDimensions dimensions, int imageWidth, int imageHeight, AnimationResourceMetadata metadata) {
        int k = imageWidth / dimensions.width();
        int l = imageHeight / dimensions.height();
        int m = k * l;
        ArrayList<AnimationFrame> list = new ArrayList<AnimationFrame>();
        metadata.forEachFrame((index, frameTime) -> list.add(new AnimationFrame(index, frameTime)));
        if (list.isEmpty()) {
            for (int n = 0; n < m; ++n) {
                list.add(new AnimationFrame(n, metadata.getDefaultFrameTime()));
            }
        } else {
            int n = 0;
            IntOpenHashSet intSet = new IntOpenHashSet();
            Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                AnimationFrame lv = (AnimationFrame)iterator.next();
                boolean bl = true;
                if (lv.time <= 0) {
                    LOGGER.warn("Invalid frame duration on sprite {} frame {}: {}", this.id, n, lv.time);
                    bl = false;
                }
                if (lv.index < 0 || lv.index >= m) {
                    LOGGER.warn("Invalid frame index on sprite {} frame {}: {}", this.id, n, lv.index);
                    bl = false;
                }
                if (bl) {
                    intSet.add(lv.index);
                } else {
                    iterator.remove();
                }
                ++n;
            }
            int[] is = IntStream.range(0, m).filter(i -> !intSet.contains(i)).toArray();
            if (is.length > 0) {
                LOGGER.warn("Unused frames in sprite {}: {}", (Object)this.id, (Object)Arrays.toString(is));
            }
        }
        if (list.size() <= 1) {
            return null;
        }
        return new Animation(ImmutableList.copyOf(list), k, metadata.shouldInterpolate());
    }

    void upload(int x, int y, int unpackSkipPixels, int unpackSkipRows, NativeImage[] images) {
        for (int m = 0; m < this.mipmapLevelsImages.length; ++m) {
            images[m].upload(m, x >> m, y >> m, unpackSkipPixels >> m, unpackSkipRows >> m, this.width >> m, this.height >> m, this.mipmapLevelsImages.length > 1, false);
        }
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    public IntStream getDistinctFrameCount() {
        return this.animation != null ? this.animation.getDistinctFrameCount() : IntStream.of(1);
    }

    @Nullable
    public Animator createAnimator() {
        return this.animation != null ? this.animation.createAnimator() : null;
    }

    public ResourceMetadata getMetadata() {
        return this.metadata;
    }

    @Override
    public void close() {
        for (NativeImage lv : this.mipmapLevelsImages) {
            lv.close();
        }
    }

    public String toString() {
        return "SpriteContents{name=" + String.valueOf(this.id) + ", frameCount=" + this.getFrameCount() + ", height=" + this.height + ", width=" + this.width + "}";
    }

    public boolean isPixelTransparent(int frame, int x, int y) {
        int l = x;
        int m = y;
        if (this.animation != null) {
            l += this.animation.getFrameX(frame) * this.width;
            m += this.animation.getFrameY(frame) * this.height;
        }
        return (this.image.getColor(l, m) >> 24 & 0xFF) == 0;
    }

    public void upload(int x, int y) {
        if (this.animation != null) {
            this.animation.upload(x, y);
        } else {
            this.upload(x, y, 0, 0, this.mipmapLevelsImages);
        }
    }

    @Environment(value=EnvType.CLIENT)
    class Animation {
        final List<AnimationFrame> frames;
        private final int frameCount;
        private final boolean interpolation;

        Animation(List<AnimationFrame> frames, int frameCount, boolean interpolation) {
            this.frames = frames;
            this.frameCount = frameCount;
            this.interpolation = interpolation;
        }

        int getFrameX(int frame) {
            return frame % this.frameCount;
        }

        int getFrameY(int frame) {
            return frame / this.frameCount;
        }

        void upload(int x, int y, int frame) {
            int l = this.getFrameX(frame) * SpriteContents.this.width;
            int m = this.getFrameY(frame) * SpriteContents.this.height;
            SpriteContents.this.upload(x, y, l, m, SpriteContents.this.mipmapLevelsImages);
        }

        public Animator createAnimator() {
            return new AnimatorImpl(SpriteContents.this, this, this.interpolation ? new Interpolation() : null);
        }

        public void upload(int x, int y) {
            this.upload(x, y, this.frames.get((int)0).index);
        }

        public IntStream getDistinctFrameCount() {
            return this.frames.stream().mapToInt(frame -> frame.index).distinct();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class AnimationFrame {
        final int index;
        final int time;

        AnimationFrame(int index, int time) {
            this.index = index;
            this.time = time;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class AnimatorImpl
    implements Animator {
        int frame;
        int currentTime;
        final Animation animation;
        @Nullable
        private final Interpolation interpolation;

        AnimatorImpl(SpriteContents arg, @Nullable Animation animation, Interpolation interpolation) {
            this.animation = animation;
            this.interpolation = interpolation;
        }

        @Override
        public void tick(int x, int y) {
            ++this.currentTime;
            AnimationFrame lv = this.animation.frames.get(this.frame);
            if (this.currentTime >= lv.time) {
                int k = lv.index;
                this.frame = (this.frame + 1) % this.animation.frames.size();
                this.currentTime = 0;
                int l = this.animation.frames.get((int)this.frame).index;
                if (k != l) {
                    this.animation.upload(x, y, l);
                }
            } else if (this.interpolation != null) {
                if (!RenderSystem.isOnRenderThread()) {
                    RenderSystem.recordRenderCall(() -> this.interpolation.apply(x, y, this));
                } else {
                    this.interpolation.apply(x, y, this);
                }
            }
        }

        @Override
        public void close() {
            if (this.interpolation != null) {
                this.interpolation.close();
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    final class Interpolation
    implements AutoCloseable {
        private final NativeImage[] images;

        Interpolation() {
            this.images = new NativeImage[SpriteContents.this.mipmapLevelsImages.length];
            for (int i = 0; i < this.images.length; ++i) {
                int j = SpriteContents.this.width >> i;
                int k = SpriteContents.this.height >> i;
                this.images[i] = new NativeImage(j, k, false);
            }
        }

        void apply(int x, int y, AnimatorImpl animator) {
            Animation lv = animator.animation;
            List<AnimationFrame> list = lv.frames;
            AnimationFrame lv2 = list.get(animator.frame);
            double d = 1.0 - (double)animator.currentTime / (double)lv2.time;
            int k = lv2.index;
            int l = list.get((int)((animator.frame + 1) % list.size())).index;
            if (k != l) {
                for (int m = 0; m < this.images.length; ++m) {
                    int n = SpriteContents.this.width >> m;
                    int o = SpriteContents.this.height >> m;
                    for (int p = 0; p < o; ++p) {
                        for (int q = 0; q < n; ++q) {
                            int r = this.getPixelColor(lv, k, m, q, p);
                            int s = this.getPixelColor(lv, l, m, q, p);
                            int t = this.lerp(d, r >> 16 & 0xFF, s >> 16 & 0xFF);
                            int u = this.lerp(d, r >> 8 & 0xFF, s >> 8 & 0xFF);
                            int v = this.lerp(d, r & 0xFF, s & 0xFF);
                            this.images[m].setColor(q, p, r & 0xFF000000 | t << 16 | u << 8 | v);
                        }
                    }
                }
                SpriteContents.this.upload(x, y, 0, 0, this.images);
            }
        }

        private int getPixelColor(Animation animation, int frameIndex, int layer, int x, int y) {
            return SpriteContents.this.mipmapLevelsImages[layer].getColor(x + (animation.getFrameX(frameIndex) * SpriteContents.this.width >> layer), y + (animation.getFrameY(frameIndex) * SpriteContents.this.height >> layer));
        }

        private int lerp(double delta, int to, int from) {
            return (int)(delta * (double)to + (1.0 - delta) * (double)from);
        }

        @Override
        public void close() {
            for (NativeImage lv : this.images) {
                lv.close();
            }
        }
    }
}

