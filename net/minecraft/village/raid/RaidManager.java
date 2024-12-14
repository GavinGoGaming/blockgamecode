/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.village.raid;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.PointOfInterestTypeTags;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.GameRules;
import net.minecraft.world.PersistentState;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.Nullable;

public class RaidManager
extends PersistentState {
    private static final String RAIDS = "raids";
    private final Map<Integer, Raid> raids = Maps.newHashMap();
    private final ServerWorld world;
    private int nextAvailableId;
    private int currentTime;

    public static PersistentState.Type<RaidManager> getPersistentStateType(ServerWorld world) {
        return new PersistentState.Type<RaidManager>(() -> new RaidManager(world), (nbt, registryLookup) -> RaidManager.fromNbt(world, nbt), DataFixTypes.SAVED_DATA_RAIDS);
    }

    public RaidManager(ServerWorld world) {
        this.world = world;
        this.nextAvailableId = 1;
        this.markDirty();
    }

    public Raid getRaid(int id) {
        return this.raids.get(id);
    }

    public void tick() {
        ++this.currentTime;
        Iterator<Raid> iterator = this.raids.values().iterator();
        while (iterator.hasNext()) {
            Raid lv = iterator.next();
            if (this.world.getGameRules().getBoolean(GameRules.DISABLE_RAIDS)) {
                lv.invalidate();
            }
            if (lv.hasStopped()) {
                iterator.remove();
                this.markDirty();
                continue;
            }
            lv.tick();
        }
        if (this.currentTime % 200 == 0) {
            this.markDirty();
        }
        DebugInfoSender.sendRaids(this.world, this.raids.values());
    }

    public static boolean isValidRaiderFor(RaiderEntity raider, Raid raid) {
        if (raider != null && raid != null && raid.getWorld() != null) {
            return raider.isAlive() && raider.canJoinRaid() && raider.getDespawnCounter() <= 2400 && raider.getWorld().getDimension() == raid.getWorld().getDimension();
        }
        return false;
    }

    @Nullable
    public Raid startRaid(ServerPlayerEntity player, BlockPos pos) {
        BlockPos lv5;
        if (player.isSpectator()) {
            return null;
        }
        if (this.world.getGameRules().getBoolean(GameRules.DISABLE_RAIDS)) {
            return null;
        }
        DimensionType lv = player.getWorld().getDimension();
        if (!lv.hasRaids()) {
            return null;
        }
        List<PointOfInterest> list = this.world.getPointOfInterestStorage().getInCircle(poiType -> poiType.isIn(PointOfInterestTypeTags.VILLAGE), pos, 64, PointOfInterestStorage.OccupationStatus.IS_OCCUPIED).toList();
        int i = 0;
        Vec3d lv2 = Vec3d.ZERO;
        for (PointOfInterest lv3 : list) {
            BlockPos lv4 = lv3.getPos();
            lv2 = lv2.add(lv4.getX(), lv4.getY(), lv4.getZ());
            ++i;
        }
        if (i > 0) {
            lv2 = lv2.multiply(1.0 / (double)i);
            lv5 = BlockPos.ofFloored(lv2);
        } else {
            lv5 = pos;
        }
        Raid lv6 = this.getOrCreateRaid(player.getServerWorld(), lv5);
        if (!lv6.hasStarted() && !this.raids.containsKey(lv6.getRaidId())) {
            this.raids.put(lv6.getRaidId(), lv6);
        }
        if (!lv6.hasStarted() || lv6.getBadOmenLevel() < lv6.getMaxAcceptableBadOmenLevel()) {
            lv6.start(player);
        }
        this.markDirty();
        return lv6;
    }

    private Raid getOrCreateRaid(ServerWorld world, BlockPos pos) {
        Raid lv = world.getRaidAt(pos);
        return lv != null ? lv : new Raid(this.nextId(), world, pos);
    }

    public static RaidManager fromNbt(ServerWorld world, NbtCompound nbt) {
        RaidManager lv = new RaidManager(world);
        lv.nextAvailableId = nbt.getInt("NextAvailableID");
        lv.currentTime = nbt.getInt("Tick");
        NbtList lv2 = nbt.getList("Raids", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < lv2.size(); ++i) {
            NbtCompound lv3 = lv2.getCompound(i);
            Raid lv4 = new Raid(world, lv3);
            lv.raids.put(lv4.getRaidId(), lv4);
        }
        return lv;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putInt("NextAvailableID", this.nextAvailableId);
        nbt.putInt("Tick", this.currentTime);
        NbtList lv = new NbtList();
        for (Raid lv2 : this.raids.values()) {
            NbtCompound lv3 = new NbtCompound();
            lv2.writeNbt(lv3);
            lv.add(lv3);
        }
        nbt.put("Raids", lv);
        return nbt;
    }

    public static String nameFor(RegistryEntry<DimensionType> dimensionTypeEntry) {
        if (dimensionTypeEntry.matchesKey(DimensionTypes.THE_END)) {
            return "raids_end";
        }
        return RAIDS;
    }

    private int nextId() {
        return ++this.nextAvailableId;
    }

    @Nullable
    public Raid getRaidAt(BlockPos pos, int searchDistance) {
        Raid lv = null;
        double d = searchDistance;
        for (Raid lv2 : this.raids.values()) {
            double e = lv2.getCenter().getSquaredDistance(pos);
            if (!lv2.isActive() || !(e < d)) continue;
            lv = lv2;
            d = e;
        }
        return lv;
    }
}

