/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.model;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public final class ModelCuboidData {
    @Nullable
    private final String name;
    private final Vector3f offset;
    private final Vector3f dimensions;
    private final Dilation extraSize;
    private final boolean mirror;
    private final Vector2f textureUV;
    private final Vector2f textureScale;
    private final Set<Direction> directions;

    protected ModelCuboidData(@Nullable String name, float textureX, float textureY, float offsetX, float offsetY, float offsetZ, float sizeX, float sizeY, float sizeZ, Dilation extra, boolean mirror, float textureScaleX, float textureScaleY, Set<Direction> directions) {
        this.name = name;
        this.textureUV = new Vector2f(textureX, textureY);
        this.offset = new Vector3f(offsetX, offsetY, offsetZ);
        this.dimensions = new Vector3f(sizeX, sizeY, sizeZ);
        this.extraSize = extra;
        this.mirror = mirror;
        this.textureScale = new Vector2f(textureScaleX, textureScaleY);
        this.directions = directions;
    }

    public ModelPart.Cuboid createCuboid(int textureWidth, int textureHeight) {
        return new ModelPart.Cuboid((int)this.textureUV.getX(), (int)this.textureUV.getY(), this.offset.x(), this.offset.y(), this.offset.z(), this.dimensions.x(), this.dimensions.y(), this.dimensions.z(), this.extraSize.radiusX, this.extraSize.radiusY, this.extraSize.radiusZ, this.mirror, (float)textureWidth * this.textureScale.getX(), (float)textureHeight * this.textureScale.getY(), this.directions);
    }
}

