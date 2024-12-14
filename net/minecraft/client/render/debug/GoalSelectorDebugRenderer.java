/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.debug;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.custom.DebugGoalSelectorCustomPayload;
import net.minecraft.util.Colors;
import net.minecraft.util.math.BlockPos;

@Environment(value=EnvType.CLIENT)
public class GoalSelectorDebugRenderer
implements DebugRenderer.Renderer {
    private static final int RANGE = 160;
    private final MinecraftClient client;
    private final Int2ObjectMap<Entity> goalSelectors = new Int2ObjectOpenHashMap<Entity>();

    @Override
    public void clear() {
        this.goalSelectors.clear();
    }

    public void setGoalSelectorList(int index, BlockPos pos, List<DebugGoalSelectorCustomPayload.Goal> goals) {
        this.goalSelectors.put(index, new Entity(pos, goals));
    }

    public void removeGoalSelectorList(int index) {
        this.goalSelectors.remove(index);
    }

    public GoalSelectorDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        Camera lv = this.client.gameRenderer.getCamera();
        BlockPos lv2 = BlockPos.ofFloored(lv.getPos().x, 0.0, lv.getPos().z);
        for (Entity lv3 : this.goalSelectors.values()) {
            BlockPos lv4 = lv3.entityPos;
            if (!lv2.isWithinDistance(lv4, 160.0)) continue;
            for (int i = 0; i < lv3.goals.size(); ++i) {
                DebugGoalSelectorCustomPayload.Goal lv5 = lv3.goals.get(i);
                double g = (double)lv4.getX() + 0.5;
                double h = (double)lv4.getY() + 2.0 + (double)i * 0.25;
                double j = (double)lv4.getZ() + 0.5;
                int k = lv5.isRunning() ? Colors.GREEN : -3355444;
                DebugRenderer.drawString(matrices, vertexConsumers, lv5.name(), g, h, j, k);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    record Entity(BlockPos entityPos, List<DebugGoalSelectorCustomPayload.Goal> goals) {
    }
}

