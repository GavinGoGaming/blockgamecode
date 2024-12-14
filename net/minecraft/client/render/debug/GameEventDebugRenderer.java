/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.debug;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.listener.GameEventListener;

@Environment(value=EnvType.CLIENT)
public class GameEventDebugRenderer
implements DebugRenderer.Renderer {
    private final MinecraftClient client;
    private static final int field_32899 = 32;
    private static final float field_32900 = 1.0f;
    private final List<Entry> entries = Lists.newArrayList();
    private final List<Listener> listeners = Lists.newArrayList();

    public GameEventDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        ClientWorld lv = this.client.world;
        if (lv == null) {
            this.entries.clear();
            this.listeners.clear();
            return;
        }
        Vec3d lv2 = new Vec3d(cameraX, 0.0, cameraZ);
        this.entries.removeIf(Entry::hasExpired);
        this.listeners.removeIf(listener -> listener.isTooFar(lv, lv2));
        VertexConsumer lv3 = vertexConsumers.getBuffer(RenderLayer.getLines());
        for (Listener lv4 : this.listeners) {
            lv4.getPos(lv).ifPresent(pos -> {
                double g = pos.getX() - (double)lv4.getRange();
                double h = pos.getY() - (double)lv4.getRange();
                double i = pos.getZ() - (double)lv4.getRange();
                double j = pos.getX() + (double)lv4.getRange();
                double k = pos.getY() + (double)lv4.getRange();
                double l = pos.getZ() + (double)lv4.getRange();
                WorldRenderer.drawShapeOutline(matrices, lv3, VoxelShapes.cuboid(new Box(g, h, i, j, k, l)), -cameraX, -cameraY, -cameraZ, 1.0f, 1.0f, 0.0f, 0.35f, true);
            });
        }
        VertexConsumer lv5 = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
        for (Listener lv6 : this.listeners) {
            lv6.getPos(lv).ifPresent(pos -> WorldRenderer.renderFilledBox(matrices, lv5, pos.getX() - 0.25 - cameraX, pos.getY() - cameraY, pos.getZ() - 0.25 - cameraZ, pos.getX() + 0.25 - cameraX, pos.getY() - cameraY + 1.0, pos.getZ() + 0.25 - cameraZ, 1.0f, 1.0f, 0.0f, 0.35f));
        }
        for (Listener lv6 : this.listeners) {
            lv6.getPos(lv).ifPresent(pos -> {
                DebugRenderer.drawString(matrices, vertexConsumers, "Listener Origin", pos.getX(), pos.getY() + (double)1.8f, pos.getZ(), Colors.WHITE, 0.025f);
                DebugRenderer.drawString(matrices, vertexConsumers, BlockPos.ofFloored(pos).toString(), pos.getX(), pos.getY() + 1.5, pos.getZ(), -6959665, 0.025f);
            });
        }
        for (Entry lv7 : this.entries) {
            Vec3d lv8 = lv7.pos;
            double g = 0.2f;
            double h = lv8.x - (double)0.2f;
            double i = lv8.y - (double)0.2f;
            double j = lv8.z - (double)0.2f;
            double k = lv8.x + (double)0.2f;
            double l = lv8.y + (double)0.2f + 0.5;
            double m = lv8.z + (double)0.2f;
            GameEventDebugRenderer.drawBoxIfCameraReady(matrices, vertexConsumers, new Box(h, i, j, k, l, m), 1.0f, 1.0f, 1.0f, 0.2f);
            DebugRenderer.drawString(matrices, vertexConsumers, lv7.event.getValue().toString(), lv8.x, lv8.y + (double)0.85f, lv8.z, -7564911, 0.0075f);
        }
    }

    private static void drawBoxIfCameraReady(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Box box, float red, float green, float blue, float alpha) {
        Camera lv = MinecraftClient.getInstance().gameRenderer.getCamera();
        if (!lv.isReady()) {
            return;
        }
        Vec3d lv2 = lv.getPos().negate();
        DebugRenderer.drawBox(matrices, vertexConsumers, box.offset(lv2), red, green, blue, alpha);
    }

    public void addEvent(RegistryKey<GameEvent> eventKey, Vec3d pos) {
        this.entries.add(new Entry(Util.getMeasuringTimeMs(), eventKey, pos));
    }

    public void addListener(PositionSource positionSource, int range) {
        this.listeners.add(new Listener(positionSource, range));
    }

    @Environment(value=EnvType.CLIENT)
    static class Listener
    implements GameEventListener {
        public final PositionSource positionSource;
        public final int range;

        public Listener(PositionSource positionSource, int range) {
            this.positionSource = positionSource;
            this.range = range;
        }

        public boolean isTooFar(World world, Vec3d pos) {
            return this.positionSource.getPos(world).filter(pos2 -> pos2.squaredDistanceTo(pos) <= 1024.0).isPresent();
        }

        public Optional<Vec3d> getPos(World world) {
            return this.positionSource.getPos(world);
        }

        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public int getRange() {
            return this.range;
        }

        @Override
        public boolean listen(ServerWorld world, RegistryEntry<GameEvent> event, GameEvent.Emitter emitter, Vec3d emitterPos) {
            return false;
        }
    }

    @Environment(value=EnvType.CLIENT)
    record Entry(long startingMs, RegistryKey<GameEvent> event, Vec3d pos) {
        public boolean hasExpired() {
            return Util.getMeasuringTimeMs() - this.startingMs > 3000L;
        }
    }
}

