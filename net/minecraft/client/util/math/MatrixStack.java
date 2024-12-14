/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.util.math;

import com.google.common.collect.Queues;
import java.util.Deque;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Util;
import net.minecraft.util.math.MatrixUtil;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class MatrixStack {
    private final Deque<Entry> stack = Util.make(Queues.newArrayDeque(), stack -> {
        Matrix4f matrix4f = new Matrix4f();
        Matrix3f matrix3f = new Matrix3f();
        stack.add(new Entry(matrix4f, matrix3f));
    });

    public void translate(double x, double y, double z) {
        this.translate((float)x, (float)y, (float)z);
    }

    public void translate(float x, float y, float z) {
        Entry lv = this.stack.getLast();
        lv.positionMatrix.translate(x, y, z);
    }

    public void scale(float x, float y, float z) {
        Entry lv = this.stack.getLast();
        lv.positionMatrix.scale(x, y, z);
        if (Math.abs(x) == Math.abs(y) && Math.abs(y) == Math.abs(z)) {
            if (x < 0.0f || y < 0.0f || z < 0.0f) {
                lv.normalMatrix.scale(Math.signum(x), Math.signum(y), Math.signum(z));
            }
            return;
        }
        lv.normalMatrix.scale(1.0f / x, 1.0f / y, 1.0f / z);
        lv.canSkipNormalization = false;
    }

    public void multiply(Quaternionf quaternion) {
        Entry lv = this.stack.getLast();
        lv.positionMatrix.rotate(quaternion);
        lv.normalMatrix.rotate(quaternion);
    }

    public void multiply(Quaternionf quaternion, float originX, float originY, float originZ) {
        Entry lv = this.stack.getLast();
        lv.positionMatrix.rotateAround(quaternion, originX, originY, originZ);
        lv.normalMatrix.rotate(quaternion);
    }

    public void push() {
        this.stack.addLast(new Entry(this.stack.getLast()));
    }

    public void pop() {
        this.stack.removeLast();
    }

    public Entry peek() {
        return this.stack.getLast();
    }

    public boolean isEmpty() {
        return this.stack.size() == 1;
    }

    public void loadIdentity() {
        Entry lv = this.stack.getLast();
        lv.positionMatrix.identity();
        lv.normalMatrix.identity();
        lv.canSkipNormalization = true;
    }

    public void multiplyPositionMatrix(Matrix4f matrix) {
        Entry lv = this.stack.getLast();
        lv.positionMatrix.mul(matrix);
        if (!MatrixUtil.isTranslation(matrix)) {
            if (MatrixUtil.isOrthonormal(matrix)) {
                lv.normalMatrix.mul(new Matrix3f(matrix));
            } else {
                lv.computeNormal();
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class Entry {
        final Matrix4f positionMatrix;
        final Matrix3f normalMatrix;
        boolean canSkipNormalization = true;

        Entry(Matrix4f positionMatrix, Matrix3f normalMatrix) {
            this.positionMatrix = positionMatrix;
            this.normalMatrix = normalMatrix;
        }

        Entry(Entry matrix) {
            this.positionMatrix = new Matrix4f(matrix.positionMatrix);
            this.normalMatrix = new Matrix3f(matrix.normalMatrix);
            this.canSkipNormalization = matrix.canSkipNormalization;
        }

        void computeNormal() {
            this.normalMatrix.set(this.positionMatrix).invert().transpose();
            this.canSkipNormalization = false;
        }

        public Matrix4f getPositionMatrix() {
            return this.positionMatrix;
        }

        public Matrix3f getNormalMatrix() {
            return this.normalMatrix;
        }

        public Vector3f transformNormal(Vector3f vec, Vector3f dest) {
            return this.transformNormal(vec.x, vec.y, vec.z, dest);
        }

        public Vector3f transformNormal(float x, float y, float z, Vector3f dest) {
            Vector3f vector3f2 = this.normalMatrix.transform(x, y, z, dest);
            return this.canSkipNormalization ? vector3f2 : vector3f2.normalize();
        }

        public Entry copy() {
            return new Entry(this);
        }
    }
}

