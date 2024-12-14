/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class Camera {
    private static final float BASE_CAMERA_DISTANCE = 4.0f;
    private static final Vector3f HORIZONTAL = new Vector3f(0.0f, 0.0f, -1.0f);
    private static final Vector3f VERTICAL = new Vector3f(0.0f, 1.0f, 0.0f);
    private static final Vector3f DIAGONAL = new Vector3f(-1.0f, 0.0f, 0.0f);
    private boolean ready;
    private BlockView area;
    private Entity focusedEntity;
    private Vec3d pos = Vec3d.ZERO;
    private final BlockPos.Mutable blockPos = new BlockPos.Mutable();
    private final Vector3f horizontalPlane = new Vector3f(HORIZONTAL);
    private final Vector3f verticalPlane = new Vector3f(VERTICAL);
    private final Vector3f diagonalPlane = new Vector3f(DIAGONAL);
    private float pitch;
    private float yaw;
    private final Quaternionf rotation = new Quaternionf();
    private boolean thirdPerson;
    private float cameraY;
    private float lastCameraY;
    private float lastTickDelta;
    public static final float field_32133 = 0.083333336f;

    public void update(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta) {
        this.ready = true;
        this.area = area;
        this.focusedEntity = focusedEntity;
        this.thirdPerson = thirdPerson;
        this.lastTickDelta = tickDelta;
        this.setRotation(focusedEntity.getYaw(tickDelta), focusedEntity.getPitch(tickDelta));
        this.setPos(MathHelper.lerp((double)tickDelta, focusedEntity.prevX, focusedEntity.getX()), MathHelper.lerp((double)tickDelta, focusedEntity.prevY, focusedEntity.getY()) + (double)MathHelper.lerp(tickDelta, this.lastCameraY, this.cameraY), MathHelper.lerp((double)tickDelta, focusedEntity.prevZ, focusedEntity.getZ()));
        if (thirdPerson) {
            float f;
            if (inverseView) {
                this.setRotation(this.yaw + 180.0f, -this.pitch);
            }
            if (focusedEntity instanceof LivingEntity) {
                LivingEntity lv = (LivingEntity)focusedEntity;
                f = lv.getScale();
            } else {
                f = 1.0f;
            }
            float g = f;
            this.moveBy(-this.clipToSpace(4.0f * g), 0.0f, 0.0f);
        } else if (focusedEntity instanceof LivingEntity && ((LivingEntity)focusedEntity).isSleeping()) {
            Direction lv2 = ((LivingEntity)focusedEntity).getSleepingDirection();
            this.setRotation(lv2 != null ? lv2.asRotation() - 180.0f : 0.0f, 0.0f);
            this.moveBy(0.0f, 0.3f, 0.0f);
        }
    }

    public void updateEyeHeight() {
        if (this.focusedEntity != null) {
            this.lastCameraY = this.cameraY;
            this.cameraY += (this.focusedEntity.getStandingEyeHeight() - this.cameraY) * 0.5f;
        }
    }

    private float clipToSpace(float f) {
        float g = 0.1f;
        for (int i = 0; i < 8; ++i) {
            float l;
            Vec3d lv2;
            float h = (i & 1) * 2 - 1;
            float j = (i >> 1 & 1) * 2 - 1;
            float k = (i >> 2 & 1) * 2 - 1;
            Vec3d lv = this.pos.add(h * 0.1f, j * 0.1f, k * 0.1f);
            BlockHitResult lv3 = this.area.raycast(new RaycastContext(lv, lv2 = lv.add(new Vec3d(this.horizontalPlane).multiply(-f)), RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.NONE, this.focusedEntity));
            if (((HitResult)lv3).getType() == HitResult.Type.MISS || !((l = (float)lv3.getPos().squaredDistanceTo(this.pos)) < MathHelper.square(f))) continue;
            f = MathHelper.sqrt(l);
        }
        return f;
    }

    protected void moveBy(float f, float g, float h) {
        Vector3f vector3f = new Vector3f(h, g, -f).rotate(this.rotation);
        this.setPos(new Vec3d(this.pos.x + (double)vector3f.x, this.pos.y + (double)vector3f.y, this.pos.z + (double)vector3f.z));
    }

    protected void setRotation(float yaw, float pitch) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.rotation.rotationYXZ((float)Math.PI - yaw * ((float)Math.PI / 180), -pitch * ((float)Math.PI / 180), 0.0f);
        HORIZONTAL.rotate(this.rotation, this.horizontalPlane);
        VERTICAL.rotate(this.rotation, this.verticalPlane);
        DIAGONAL.rotate(this.rotation, this.diagonalPlane);
    }

    protected void setPos(double x, double y, double z) {
        this.setPos(new Vec3d(x, y, z));
    }

    protected void setPos(Vec3d pos) {
        this.pos = pos;
        this.blockPos.set(pos.x, pos.y, pos.z);
    }

    public Vec3d getPos() {
        return this.pos;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public Quaternionf getRotation() {
        return this.rotation;
    }

    public Entity getFocusedEntity() {
        return this.focusedEntity;
    }

    public boolean isReady() {
        return this.ready;
    }

    public boolean isThirdPerson() {
        return this.thirdPerson;
    }

    public Projection getProjection() {
        MinecraftClient lv = MinecraftClient.getInstance();
        double d = (double)lv.getWindow().getFramebufferWidth() / (double)lv.getWindow().getFramebufferHeight();
        double e = Math.tan((double)((float)lv.options.getFov().getValue().intValue() * ((float)Math.PI / 180)) / 2.0) * (double)0.05f;
        double f = e * d;
        Vec3d lv2 = new Vec3d(this.horizontalPlane).multiply(0.05f);
        Vec3d lv3 = new Vec3d(this.diagonalPlane).multiply(f);
        Vec3d lv4 = new Vec3d(this.verticalPlane).multiply(e);
        return new Projection(lv2, lv3, lv4);
    }

    public CameraSubmersionType getSubmersionType() {
        if (!this.ready) {
            return CameraSubmersionType.NONE;
        }
        FluidState lv = this.area.getFluidState(this.blockPos);
        if (lv.isIn(FluidTags.WATER) && this.pos.y < (double)((float)this.blockPos.getY() + lv.getHeight(this.area, this.blockPos))) {
            return CameraSubmersionType.WATER;
        }
        Projection lv2 = this.getProjection();
        List<Vec3d> list = Arrays.asList(lv2.center, lv2.getBottomRight(), lv2.getTopRight(), lv2.getBottomLeft(), lv2.getTopLeft());
        for (Vec3d lv3 : list) {
            Vec3d lv4 = this.pos.add(lv3);
            BlockPos lv5 = BlockPos.ofFloored(lv4);
            FluidState lv6 = this.area.getFluidState(lv5);
            if (lv6.isIn(FluidTags.LAVA)) {
                if (!(lv4.y <= (double)(lv6.getHeight(this.area, lv5) + (float)lv5.getY()))) continue;
                return CameraSubmersionType.LAVA;
            }
            BlockState lv7 = this.area.getBlockState(lv5);
            if (!lv7.isOf(Blocks.POWDER_SNOW)) continue;
            return CameraSubmersionType.POWDER_SNOW;
        }
        return CameraSubmersionType.NONE;
    }

    public final Vector3f getHorizontalPlane() {
        return this.horizontalPlane;
    }

    public final Vector3f getVerticalPlane() {
        return this.verticalPlane;
    }

    public final Vector3f getDiagonalPlane() {
        return this.diagonalPlane;
    }

    public void reset() {
        this.area = null;
        this.focusedEntity = null;
        this.ready = false;
    }

    public float getLastTickDelta() {
        return this.lastTickDelta;
    }

    @Environment(value=EnvType.CLIENT)
    public static class Projection {
        final Vec3d center;
        private final Vec3d x;
        private final Vec3d y;

        Projection(Vec3d center, Vec3d x, Vec3d y) {
            this.center = center;
            this.x = x;
            this.y = y;
        }

        public Vec3d getBottomRight() {
            return this.center.add(this.y).add(this.x);
        }

        public Vec3d getTopRight() {
            return this.center.add(this.y).subtract(this.x);
        }

        public Vec3d getBottomLeft() {
            return this.center.subtract(this.y).add(this.x);
        }

        public Vec3d getTopLeft() {
            return this.center.subtract(this.y).subtract(this.x);
        }

        public Vec3d getPosition(float factorX, float factorY) {
            return this.center.add(this.y.multiply(factorY)).subtract(this.x.multiply(factorX));
        }
    }
}

