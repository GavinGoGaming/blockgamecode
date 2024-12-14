/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class Dilation {
    public static final Dilation NONE = new Dilation(0.0f);
    final float radiusX;
    final float radiusY;
    final float radiusZ;

    public Dilation(float radiusX, float radiusY, float radiusZ) {
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.radiusZ = radiusZ;
    }

    public Dilation(float radius) {
        this(radius, radius, radius);
    }

    public Dilation add(float radius) {
        return new Dilation(this.radiusX + radius, this.radiusY + radius, this.radiusZ + radius);
    }

    public Dilation add(float radiusX, float radiusY, float radiusZ) {
        return new Dilation(this.radiusX + radiusX, this.radiusY + radiusY, this.radiusZ + radiusZ);
    }
}

