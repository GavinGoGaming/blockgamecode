/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.pathing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class Path {
    private final List<PathNode> nodes;
    @Nullable
    private DebugNodeInfo debugNodeInfos;
    private int currentNodeIndex;
    private final BlockPos target;
    private final float manhattanDistanceFromTarget;
    private final boolean reachesTarget;

    public Path(List<PathNode> nodes, BlockPos target, boolean reachesTarget) {
        this.nodes = nodes;
        this.target = target;
        this.manhattanDistanceFromTarget = nodes.isEmpty() ? Float.MAX_VALUE : this.nodes.get(this.nodes.size() - 1).getManhattanDistance(this.target);
        this.reachesTarget = reachesTarget;
    }

    public void next() {
        ++this.currentNodeIndex;
    }

    public boolean isStart() {
        return this.currentNodeIndex <= 0;
    }

    public boolean isFinished() {
        return this.currentNodeIndex >= this.nodes.size();
    }

    @Nullable
    public PathNode getEnd() {
        if (!this.nodes.isEmpty()) {
            return this.nodes.get(this.nodes.size() - 1);
        }
        return null;
    }

    public PathNode getNode(int index) {
        return this.nodes.get(index);
    }

    public void setLength(int length) {
        if (this.nodes.size() > length) {
            this.nodes.subList(length, this.nodes.size()).clear();
        }
    }

    public void setNode(int index, PathNode node) {
        this.nodes.set(index, node);
    }

    public int getLength() {
        return this.nodes.size();
    }

    public int getCurrentNodeIndex() {
        return this.currentNodeIndex;
    }

    public void setCurrentNodeIndex(int nodeIndex) {
        this.currentNodeIndex = nodeIndex;
    }

    public Vec3d getNodePosition(Entity entity, int index) {
        PathNode lv = this.nodes.get(index);
        double d = (double)lv.x + (double)((int)(entity.getWidth() + 1.0f)) * 0.5;
        double e = lv.y;
        double f = (double)lv.z + (double)((int)(entity.getWidth() + 1.0f)) * 0.5;
        return new Vec3d(d, e, f);
    }

    public BlockPos getNodePos(int index) {
        return this.nodes.get(index).getBlockPos();
    }

    public Vec3d getNodePosition(Entity entity) {
        return this.getNodePosition(entity, this.currentNodeIndex);
    }

    public BlockPos getCurrentNodePos() {
        return this.nodes.get(this.currentNodeIndex).getBlockPos();
    }

    public PathNode getCurrentNode() {
        return this.nodes.get(this.currentNodeIndex);
    }

    @Nullable
    public PathNode getLastNode() {
        return this.currentNodeIndex > 0 ? this.nodes.get(this.currentNodeIndex - 1) : null;
    }

    public boolean equalsPath(@Nullable Path o) {
        if (o == null) {
            return false;
        }
        if (o.nodes.size() != this.nodes.size()) {
            return false;
        }
        for (int i = 0; i < this.nodes.size(); ++i) {
            PathNode lv = this.nodes.get(i);
            PathNode lv2 = o.nodes.get(i);
            if (lv.x == lv2.x && lv.y == lv2.y && lv.z == lv2.z) continue;
            return false;
        }
        return true;
    }

    public boolean reachesTarget() {
        return this.reachesTarget;
    }

    @Debug
    void setDebugInfo(PathNode[] debugNodes, PathNode[] debugSecondNodes, Set<TargetPathNode> debugTargetNodes) {
        this.debugNodeInfos = new DebugNodeInfo(debugNodes, debugSecondNodes, debugTargetNodes);
    }

    @Nullable
    public DebugNodeInfo getDebugNodeInfos() {
        return this.debugNodeInfos;
    }

    public void toBuf(PacketByteBuf buf2) {
        if (this.debugNodeInfos == null || this.debugNodeInfos.targetNodes.isEmpty()) {
            return;
        }
        buf2.writeBoolean(this.reachesTarget);
        buf2.writeInt(this.currentNodeIndex);
        buf2.writeBlockPos(this.target);
        buf2.writeCollection(this.nodes, (buf, node) -> node.write((PacketByteBuf)buf));
        this.debugNodeInfos.write(buf2);
    }

    public static Path fromBuf(PacketByteBuf buf) {
        boolean bl = buf.readBoolean();
        int i = buf.readInt();
        BlockPos lv = buf.readBlockPos();
        List<PathNode> list = buf.readList(PathNode::fromBuf);
        DebugNodeInfo lv2 = DebugNodeInfo.fromBuf(buf);
        Path lv3 = new Path(list, lv, bl);
        lv3.debugNodeInfos = lv2;
        lv3.currentNodeIndex = i;
        return lv3;
    }

    public String toString() {
        return "Path(length=" + this.nodes.size() + ")";
    }

    public BlockPos getTarget() {
        return this.target;
    }

    public float getManhattanDistanceFromTarget() {
        return this.manhattanDistanceFromTarget;
    }

    static PathNode[] nodesFromBuf(PacketByteBuf buf) {
        PathNode[] lvs = new PathNode[buf.readVarInt()];
        for (int i = 0; i < lvs.length; ++i) {
            lvs[i] = PathNode.fromBuf(buf);
        }
        return lvs;
    }

    static void write(PacketByteBuf buf, PathNode[] nodes) {
        buf.writeVarInt(nodes.length);
        for (PathNode lv : nodes) {
            lv.write(buf);
        }
    }

    public Path copy() {
        Path lv = new Path(this.nodes, this.target, this.reachesTarget);
        lv.debugNodeInfos = this.debugNodeInfos;
        lv.currentNodeIndex = this.currentNodeIndex;
        return lv;
    }

    public record DebugNodeInfo(PathNode[] openSet, PathNode[] closedSet, Set<TargetPathNode> targetNodes) {
        public void write(PacketByteBuf buf2) {
            buf2.writeCollection(this.targetNodes, (buf, node) -> node.write((PacketByteBuf)buf));
            Path.write(buf2, this.openSet);
            Path.write(buf2, this.closedSet);
        }

        public static DebugNodeInfo fromBuf(PacketByteBuf buf) {
            HashSet hashSet = buf.readCollection(HashSet::new, TargetPathNode::fromBuffer);
            PathNode[] lvs = Path.nodesFromBuf(buf);
            PathNode[] lvs2 = Path.nodesFromBuf(buf);
            return new DebugNodeInfo(lvs, lvs2, hashSet);
        }
    }
}

