/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public abstract class BillboardParticle
extends Particle {
    protected float scale;

    protected BillboardParticle(ClientWorld arg, double d, double e, double f) {
        super(arg, d, e, f);
        this.scale = 0.1f * (this.random.nextFloat() * 0.5f + 0.5f) * 2.0f;
    }

    protected BillboardParticle(ClientWorld arg, double d, double e, double f, double g, double h, double i) {
        super(arg, d, e, f, g, h, i);
        this.scale = 0.1f * (this.random.nextFloat() * 0.5f + 0.5f) * 2.0f;
    }

    public Rotator getRotator() {
        return Rotator.ALL_AXIS;
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        Quaternionf quaternionf = new Quaternionf();
        this.getRotator().setRotation(quaternionf, camera, tickDelta);
        if (this.angle != 0.0f) {
            quaternionf.rotateZ(MathHelper.lerp(tickDelta, this.prevAngle, this.angle));
        }
        this.method_60373(vertexConsumer, camera, quaternionf, tickDelta);
    }

    protected void method_60373(VertexConsumer arg, Camera arg2, Quaternionf quaternionf, float f) {
        Vec3d lv = arg2.getPos();
        float g = (float)(MathHelper.lerp((double)f, this.prevPosX, this.x) - lv.getX());
        float h = (float)(MathHelper.lerp((double)f, this.prevPosY, this.y) - lv.getY());
        float i = (float)(MathHelper.lerp((double)f, this.prevPosZ, this.z) - lv.getZ());
        this.method_60374(arg, quaternionf, g, h, i, f);
    }

    protected void method_60374(VertexConsumer arg, Quaternionf quaternionf, float f, float g, float h, float i) {
        float j = this.getSize(i);
        float k = this.getMinU();
        float l = this.getMaxU();
        float m = this.getMinV();
        float n = this.getMaxV();
        int o = this.getBrightness(i);
        this.method_60375(arg, quaternionf, f, g, h, 1.0f, -1.0f, j, l, n, o);
        this.method_60375(arg, quaternionf, f, g, h, 1.0f, 1.0f, j, l, m, o);
        this.method_60375(arg, quaternionf, f, g, h, -1.0f, 1.0f, j, k, m, o);
        this.method_60375(arg, quaternionf, f, g, h, -1.0f, -1.0f, j, k, n, o);
    }

    private void method_60375(VertexConsumer arg, Quaternionf quaternionf, float f, float g, float h, float i, float j, float k, float l, float m, int n) {
        Vector3f vector3f = new Vector3f(i, j, 0.0f).rotate(quaternionf).mul(k).add(f, g, h);
        arg.vertex(vector3f.x(), vector3f.y(), vector3f.z()).texture(l, m).color(this.red, this.green, this.blue, this.alpha).light(n);
    }

    public float getSize(float tickDelta) {
        return this.scale;
    }

    @Override
    public Particle scale(float scale) {
        this.scale *= scale;
        return super.scale(scale);
    }

    protected abstract float getMinU();

    protected abstract float getMaxU();

    protected abstract float getMinV();

    protected abstract float getMaxV();

    @Environment(value=EnvType.CLIENT)
    public static interface Rotator {
        public static final Rotator ALL_AXIS = (quaternion, camera, tickDelta) -> quaternion.set(camera.getRotation());
        public static final Rotator Y_AND_W_ONLY = (quaternion, camera, tickDelta) -> quaternion.set(0.0f, camera.getRotation().y, 0.0f, camera.getRotation().w);

        public void setRotation(Quaternionf var1, Camera var2, float var3);
    }
}

