/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.SoundListenerTransform;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.openal.AL10;

@Environment(value=EnvType.CLIENT)
public class SoundListener {
    private float volume = 1.0f;
    private SoundListenerTransform transform = SoundListenerTransform.DEFAULT;

    public void setTransform(SoundListenerTransform transform) {
        this.transform = transform;
        Vec3d lv = transform.position();
        Vec3d lv2 = transform.forward();
        Vec3d lv3 = transform.up();
        AL10.alListener3f(4100, (float)lv.x, (float)lv.y, (float)lv.z);
        AL10.alListenerfv(4111, new float[]{(float)lv2.x, (float)lv2.y, (float)lv2.z, (float)lv3.getX(), (float)lv3.getY(), (float)lv3.getZ()});
    }

    public void setVolume(float volume) {
        AL10.alListenerf(4106, volume);
        this.volume = volume;
    }

    public float getVolume() {
        return this.volume;
    }

    public void init() {
        this.setTransform(SoundListenerTransform.DEFAULT);
    }

    public SoundListenerTransform getTransform() {
        return this.transform;
    }
}

