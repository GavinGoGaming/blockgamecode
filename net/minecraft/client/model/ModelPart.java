/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public final class ModelPart {
    public static final float field_37937 = 1.0f;
    public float pivotX;
    public float pivotY;
    public float pivotZ;
    public float pitch;
    public float yaw;
    public float roll;
    public float xScale = 1.0f;
    public float yScale = 1.0f;
    public float zScale = 1.0f;
    public boolean visible = true;
    public boolean hidden;
    private final List<Cuboid> cuboids;
    private final Map<String, ModelPart> children;
    private ModelTransform defaultTransform = ModelTransform.NONE;

    public ModelPart(List<Cuboid> cuboids, Map<String, ModelPart> children) {
        this.cuboids = cuboids;
        this.children = children;
    }

    public ModelTransform getTransform() {
        return ModelTransform.of(this.pivotX, this.pivotY, this.pivotZ, this.pitch, this.yaw, this.roll);
    }

    public ModelTransform getDefaultTransform() {
        return this.defaultTransform;
    }

    public void setDefaultTransform(ModelTransform transform) {
        this.defaultTransform = transform;
    }

    public void resetTransform() {
        this.setTransform(this.defaultTransform);
    }

    public void setTransform(ModelTransform rotationData) {
        this.pivotX = rotationData.pivotX;
        this.pivotY = rotationData.pivotY;
        this.pivotZ = rotationData.pivotZ;
        this.pitch = rotationData.pitch;
        this.yaw = rotationData.yaw;
        this.roll = rotationData.roll;
        this.xScale = 1.0f;
        this.yScale = 1.0f;
        this.zScale = 1.0f;
    }

    public void copyTransform(ModelPart part) {
        this.xScale = part.xScale;
        this.yScale = part.yScale;
        this.zScale = part.zScale;
        this.pitch = part.pitch;
        this.yaw = part.yaw;
        this.roll = part.roll;
        this.pivotX = part.pivotX;
        this.pivotY = part.pivotY;
        this.pivotZ = part.pivotZ;
    }

    public boolean hasChild(String child) {
        return this.children.containsKey(child);
    }

    public ModelPart getChild(String name) {
        ModelPart lv = this.children.get(name);
        if (lv == null) {
            throw new NoSuchElementException("Can't find part " + name);
        }
        return lv;
    }

    public void setPivot(float x, float y, float z) {
        this.pivotX = x;
        this.pivotY = y;
        this.pivotZ = z;
    }

    public void setAngles(float pitch, float yaw, float roll) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }

    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        this.render(matrices, vertices, light, overlay, -1);
    }

    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        if (!this.visible) {
            return;
        }
        if (this.cuboids.isEmpty() && this.children.isEmpty()) {
            return;
        }
        matrices.push();
        this.rotate(matrices);
        if (!this.hidden) {
            this.renderCuboids(matrices.peek(), vertices, light, overlay, color);
        }
        for (ModelPart lv : this.children.values()) {
            lv.render(matrices, vertices, light, overlay, color);
        }
        matrices.pop();
    }

    public void forEachCuboid(MatrixStack matrices, CuboidConsumer consumer) {
        this.forEachCuboid(matrices, consumer, "");
    }

    private void forEachCuboid(MatrixStack matrices, CuboidConsumer consumer, String path) {
        if (this.cuboids.isEmpty() && this.children.isEmpty()) {
            return;
        }
        matrices.push();
        this.rotate(matrices);
        MatrixStack.Entry lv = matrices.peek();
        for (int i = 0; i < this.cuboids.size(); ++i) {
            consumer.accept(lv, path, i, this.cuboids.get(i));
        }
        String string2 = path + "/";
        this.children.forEach((name, part) -> part.forEachCuboid(matrices, consumer, string2 + name));
        matrices.pop();
    }

    public void rotate(MatrixStack matrices) {
        matrices.translate(this.pivotX / 16.0f, this.pivotY / 16.0f, this.pivotZ / 16.0f);
        if (this.pitch != 0.0f || this.yaw != 0.0f || this.roll != 0.0f) {
            matrices.multiply(new Quaternionf().rotationZYX(this.roll, this.yaw, this.pitch));
        }
        if (this.xScale != 1.0f || this.yScale != 1.0f || this.zScale != 1.0f) {
            matrices.scale(this.xScale, this.yScale, this.zScale);
        }
    }

    private void renderCuboids(MatrixStack.Entry entry, VertexConsumer vertexConsumer, int light, int overlay, int color) {
        for (Cuboid lv : this.cuboids) {
            lv.renderCuboid(entry, vertexConsumer, light, overlay, color);
        }
    }

    public Cuboid getRandomCuboid(Random random) {
        return this.cuboids.get(random.nextInt(this.cuboids.size()));
    }

    public boolean isEmpty() {
        return this.cuboids.isEmpty();
    }

    public void translate(Vector3f vec3f) {
        this.pivotX += vec3f.x();
        this.pivotY += vec3f.y();
        this.pivotZ += vec3f.z();
    }

    public void rotate(Vector3f vec3f) {
        this.pitch += vec3f.x();
        this.yaw += vec3f.y();
        this.roll += vec3f.z();
    }

    public void scale(Vector3f vec3f) {
        this.xScale += vec3f.x();
        this.yScale += vec3f.y();
        this.zScale += vec3f.z();
    }

    public Stream<ModelPart> traverse() {
        return Stream.concat(Stream.of(this), this.children.values().stream().flatMap(ModelPart::traverse));
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface CuboidConsumer {
        public void accept(MatrixStack.Entry var1, String var2, int var3, Cuboid var4);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Cuboid {
        private final Quad[] sides;
        public final float minX;
        public final float minY;
        public final float minZ;
        public final float maxX;
        public final float maxY;
        public final float maxZ;

        public Cuboid(int u, int v, float x, float y, float z, float sizeX, float sizeY, float sizeZ, float extraX, float extraY, float extraZ, boolean mirror, float textureWidth, float textureHeight, Set<Direction> set) {
            this.minX = x;
            this.minY = y;
            this.minZ = z;
            this.maxX = x + sizeX;
            this.maxY = y + sizeY;
            this.maxZ = z + sizeZ;
            this.sides = new Quad[set.size()];
            float s = x + sizeX;
            float t = y + sizeY;
            float u2 = z + sizeZ;
            x -= extraX;
            y -= extraY;
            z -= extraZ;
            s += extraX;
            t += extraY;
            u2 += extraZ;
            if (mirror) {
                float v2 = s;
                s = x;
                x = v2;
            }
            Vertex lv = new Vertex(x, y, z, 0.0f, 0.0f);
            Vertex lv2 = new Vertex(s, y, z, 0.0f, 8.0f);
            Vertex lv3 = new Vertex(s, t, z, 8.0f, 8.0f);
            Vertex lv4 = new Vertex(x, t, z, 8.0f, 0.0f);
            Vertex lv5 = new Vertex(x, y, u2, 0.0f, 0.0f);
            Vertex lv6 = new Vertex(s, y, u2, 0.0f, 8.0f);
            Vertex lv7 = new Vertex(s, t, u2, 8.0f, 8.0f);
            Vertex lv8 = new Vertex(x, t, u2, 8.0f, 0.0f);
            float w = u;
            float x2 = (float)u + sizeZ;
            float y2 = (float)u + sizeZ + sizeX;
            float z2 = (float)u + sizeZ + sizeX + sizeX;
            float aa = (float)u + sizeZ + sizeX + sizeZ;
            float ab = (float)u + sizeZ + sizeX + sizeZ + sizeX;
            float ac = v;
            float ad = (float)v + sizeZ;
            float ae = (float)v + sizeZ + sizeY;
            int af = 0;
            if (set.contains(Direction.DOWN)) {
                this.sides[af++] = new Quad(new Vertex[]{lv6, lv5, lv, lv2}, x2, ac, y2, ad, textureWidth, textureHeight, mirror, Direction.DOWN);
            }
            if (set.contains(Direction.UP)) {
                this.sides[af++] = new Quad(new Vertex[]{lv3, lv4, lv8, lv7}, y2, ad, z2, ac, textureWidth, textureHeight, mirror, Direction.UP);
            }
            if (set.contains(Direction.WEST)) {
                this.sides[af++] = new Quad(new Vertex[]{lv, lv5, lv8, lv4}, w, ad, x2, ae, textureWidth, textureHeight, mirror, Direction.WEST);
            }
            if (set.contains(Direction.NORTH)) {
                this.sides[af++] = new Quad(new Vertex[]{lv2, lv, lv4, lv3}, x2, ad, y2, ae, textureWidth, textureHeight, mirror, Direction.NORTH);
            }
            if (set.contains(Direction.EAST)) {
                this.sides[af++] = new Quad(new Vertex[]{lv6, lv2, lv3, lv7}, y2, ad, aa, ae, textureWidth, textureHeight, mirror, Direction.EAST);
            }
            if (set.contains(Direction.SOUTH)) {
                this.sides[af] = new Quad(new Vertex[]{lv5, lv6, lv7, lv8}, aa, ad, ab, ae, textureWidth, textureHeight, mirror, Direction.SOUTH);
            }
        }

        public void renderCuboid(MatrixStack.Entry entry, VertexConsumer vertexConsumer, int light, int overlay, int color) {
            Matrix4f matrix4f = entry.getPositionMatrix();
            Vector3f vector3f = new Vector3f();
            for (Quad lv : this.sides) {
                Vector3f vector3f2 = entry.transformNormal(lv.direction, vector3f);
                float f = vector3f2.x();
                float g = vector3f2.y();
                float h = vector3f2.z();
                for (Vertex lv2 : lv.vertices) {
                    float l = lv2.pos.x() / 16.0f;
                    float m = lv2.pos.y() / 16.0f;
                    float n = lv2.pos.z() / 16.0f;
                    Vector3f vector3f3 = matrix4f.transformPosition(l, m, n, vector3f);
                    vertexConsumer.vertex(vector3f3.x(), vector3f3.y(), vector3f3.z(), color, lv2.u, lv2.v, overlay, light, f, g, h);
                }
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Vertex {
        public final Vector3f pos;
        public final float u;
        public final float v;

        public Vertex(float x, float y, float z, float u, float v) {
            this(new Vector3f(x, y, z), u, v);
        }

        public Vertex remap(float u, float v) {
            return new Vertex(this.pos, u, v);
        }

        public Vertex(Vector3f pos, float u, float v) {
            this.pos = pos;
            this.u = u;
            this.v = v;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Quad {
        public final Vertex[] vertices;
        public final Vector3f direction;

        public Quad(Vertex[] vertices, float u1, float v1, float u2, float v2, float squishU, float squishV, boolean flip, Direction direction) {
            this.vertices = vertices;
            float l = 0.0f / squishU;
            float m = 0.0f / squishV;
            vertices[0] = vertices[0].remap(u2 / squishU - l, v1 / squishV + m);
            vertices[1] = vertices[1].remap(u1 / squishU + l, v1 / squishV + m);
            vertices[2] = vertices[2].remap(u1 / squishU + l, v2 / squishV - m);
            vertices[3] = vertices[3].remap(u2 / squishU - l, v2 / squishV - m);
            if (flip) {
                int n = vertices.length;
                for (int o = 0; o < n / 2; ++o) {
                    Vertex lv = vertices[o];
                    vertices[o] = vertices[n - 1 - o];
                    vertices[n - 1 - o] = lv;
                }
            }
            this.direction = direction.getUnitVector();
            if (flip) {
                this.direction.mul(-1.0f, 1.0f, 1.0f);
            }
        }
    }
}

