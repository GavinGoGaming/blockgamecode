/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.poi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.slf4j.Logger;

public class PointOfInterestSet {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Short2ObjectMap<PointOfInterest> pointsOfInterestByPos = new Short2ObjectOpenHashMap<PointOfInterest>();
    private final Map<RegistryEntry<PointOfInterestType>, Set<PointOfInterest>> pointsOfInterestByType = Maps.newHashMap();
    private final Runnable updateListener;
    private boolean valid;

    public static Codec<PointOfInterestSet> createCodec(Runnable updateListener) {
        return RecordCodecBuilder.create(instance -> instance.group(RecordCodecBuilder.point(updateListener), Codec.BOOL.lenientOptionalFieldOf("Valid", false).forGetter(poiSet -> poiSet.valid), ((MapCodec)PointOfInterest.createCodec(updateListener).listOf().fieldOf("Records")).forGetter(poiSet -> ImmutableList.copyOf(poiSet.pointsOfInterestByPos.values()))).apply((Applicative<PointOfInterestSet, ?>)instance, PointOfInterestSet::new)).orElseGet(Util.addPrefix("Failed to read POI section: ", LOGGER::error), () -> new PointOfInterestSet(updateListener, false, ImmutableList.of()));
    }

    public PointOfInterestSet(Runnable updateListener) {
        this(updateListener, true, ImmutableList.of());
    }

    private PointOfInterestSet(Runnable updateListener, boolean valid, List<PointOfInterest> pois) {
        this.updateListener = updateListener;
        this.valid = valid;
        pois.forEach(this::add);
    }

    public Stream<PointOfInterest> get(Predicate<RegistryEntry<PointOfInterestType>> predicate, PointOfInterestStorage.OccupationStatus occupationStatus) {
        return this.pointsOfInterestByType.entrySet().stream().filter(entry -> predicate.test((RegistryEntry)entry.getKey())).flatMap(entry -> ((Set)entry.getValue()).stream()).filter(occupationStatus.getPredicate());
    }

    public void add(BlockPos pos, RegistryEntry<PointOfInterestType> type) {
        if (this.add(new PointOfInterest(pos, type, this.updateListener))) {
            LOGGER.debug("Added POI of type {} @ {}", (Object)type.getIdAsString(), (Object)pos);
            this.updateListener.run();
        }
    }

    private boolean add(PointOfInterest poi) {
        BlockPos lv = poi.getPos();
        RegistryEntry<PointOfInterestType> lv2 = poi.getType();
        short s = ChunkSectionPos.packLocal(lv);
        PointOfInterest lv3 = (PointOfInterest)this.pointsOfInterestByPos.get(s);
        if (lv3 != null) {
            if (lv2.equals(lv3.getType())) {
                return false;
            }
            Util.error("POI data mismatch: already registered at " + String.valueOf(lv));
        }
        this.pointsOfInterestByPos.put(s, poi);
        this.pointsOfInterestByType.computeIfAbsent(lv2, type -> Sets.newHashSet()).add(poi);
        return true;
    }

    public void remove(BlockPos pos) {
        PointOfInterest lv = (PointOfInterest)this.pointsOfInterestByPos.remove(ChunkSectionPos.packLocal(pos));
        if (lv == null) {
            LOGGER.error("POI data mismatch: never registered at {}", (Object)pos);
            return;
        }
        this.pointsOfInterestByType.get(lv.getType()).remove(lv);
        LOGGER.debug("Removed POI of type {} @ {}", LogUtils.defer(lv::getType), LogUtils.defer(lv::getPos));
        this.updateListener.run();
    }

    @Deprecated
    @Debug
    public int getFreeTickets(BlockPos pos) {
        return this.get(pos).map(PointOfInterest::getFreeTickets).orElse(0);
    }

    public boolean releaseTicket(BlockPos pos) {
        PointOfInterest lv = (PointOfInterest)this.pointsOfInterestByPos.get(ChunkSectionPos.packLocal(pos));
        if (lv == null) {
            throw Util.throwOrPause(new IllegalStateException("POI never registered at " + String.valueOf(pos)));
        }
        boolean bl = lv.releaseTicket();
        this.updateListener.run();
        return bl;
    }

    public boolean test(BlockPos pos, Predicate<RegistryEntry<PointOfInterestType>> predicate) {
        return this.getType(pos).filter(predicate).isPresent();
    }

    public Optional<RegistryEntry<PointOfInterestType>> getType(BlockPos pos) {
        return this.get(pos).map(PointOfInterest::getType);
    }

    private Optional<PointOfInterest> get(BlockPos pos) {
        return Optional.ofNullable((PointOfInterest)this.pointsOfInterestByPos.get(ChunkSectionPos.packLocal(pos)));
    }

    public void updatePointsOfInterest(Consumer<BiConsumer<BlockPos, RegistryEntry<PointOfInterestType>>> updater) {
        if (!this.valid) {
            Short2ObjectOpenHashMap<PointOfInterest> short2ObjectMap = new Short2ObjectOpenHashMap<PointOfInterest>(this.pointsOfInterestByPos);
            this.clear();
            updater.accept((pos, arg2) -> {
                short s2 = ChunkSectionPos.packLocal(pos);
                PointOfInterest lv = short2ObjectMap.computeIfAbsent(s2, s -> new PointOfInterest((BlockPos)pos, (RegistryEntry<PointOfInterestType>)arg2, this.updateListener));
                this.add(lv);
            });
            this.valid = true;
            this.updateListener.run();
        }
    }

    private void clear() {
        this.pointsOfInterestByPos.clear();
        this.pointsOfInterestByType.clear();
    }

    boolean isValid() {
        return this.valid;
    }
}

