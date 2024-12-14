/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Box;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector4f;

@Environment(value=EnvType.CLIENT)
public class Frustum {
    public static final int RECESSION_SCALE = 4;
    private final FrustumIntersection frustumIntersection = new FrustumIntersection();
    private final Matrix4f positionProjectionMatrix = new Matrix4f();
    private Vector4f recession;
    private double x;
    private double y;
    private double z;

    public Frustum(Matrix4f positionMatrix, Matrix4f projectionMatrix) {
        this.init(positionMatrix, projectionMatrix);
    }

    public Frustum(Frustum frustum) {
        this.frustumIntersection.set(frustum.positionProjectionMatrix);
        this.positionProjectionMatrix.set(frustum.positionProjectionMatrix);
        this.x = frustum.x;
        this.y = frustum.y;
        this.z = frustum.z;
        this.recession = frustum.recession;
    }

    public Frustum coverBoxAroundSetPosition(int boxSize) {
        double d = Math.floor(this.x / (double)boxSize) * (double)boxSize;
        double e = Math.floor(this.y / (double)boxSize) * (double)boxSize;
        double f = Math.floor(this.z / (double)boxSize) * (double)boxSize;
        double g = Math.ceil(this.x / (double)boxSize) * (double)boxSize;
        double h = Math.ceil(this.y / (double)boxSize) * (double)boxSize;
        double j = Math.ceil(this.z / (double)boxSize) * (double)boxSize;
        while (this.frustumIntersection.intersectAab((float)(d - this.x), (float)(e - this.y), (float)(f - this.z), (float)(g - this.x), (float)(h - this.y), (float)(j - this.z)) != -2) {
            this.x -= (double)(this.recession.x() * 4.0f);
            this.y -= (double)(this.recession.y() * 4.0f);
            this.z -= (double)(this.recession.z() * 4.0f);
        }
        return this;
    }

    public void setPosition(double cameraX, double cameraY, double cameraZ) {
        this.x = cameraX;
        this.y = cameraY;
        this.z = cameraZ;
    }

    private void init(Matrix4f positionMatrix, Matrix4f projectionMatrix) {
        projectionMatrix.mul(positionMatrix, this.positionProjectionMatrix);
        this.frustumIntersection.set(this.positionProjectionMatrix);
        this.recession = this.positionProjectionMatrix.transformTranspose(new Vector4f(0.0f, 0.0f, 1.0f, 0.0f));
    }

    public boolean isVisible(Box box) {
        return this.isVisible(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    private boolean isVisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        float j = (float)(minX - this.x);
        float k = (float)(minY - this.y);
        float l = (float)(minZ - this.z);
        float m = (float)(maxX - this.x);
        float n = (float)(maxY - this.y);
        float o = (float)(maxZ - this.z);
        return this.frustumIntersection.testAab(j, k, l, m, n, o);
    }
}

