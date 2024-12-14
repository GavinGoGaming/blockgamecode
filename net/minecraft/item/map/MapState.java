/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item.map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapColorComponent;
import net.minecraft.component.type.MapDecorationsComponent;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapBannerMarker;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.item.map.MapFrameMarker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class MapState
extends PersistentState {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SIZE = 128;
    private static final int SIZE_HALF = 64;
    public static final int MAX_SCALE = 4;
    public static final int MAX_DECORATIONS = 256;
    private static final String FRAME_PREFIX = "frame-";
    public final int centerX;
    public final int centerZ;
    public final RegistryKey<World> dimension;
    private final boolean showDecorations;
    private final boolean unlimitedTracking;
    public final byte scale;
    public byte[] colors = new byte[16384];
    public final boolean locked;
    private final List<PlayerUpdateTracker> updateTrackers = Lists.newArrayList();
    private final Map<PlayerEntity, PlayerUpdateTracker> updateTrackersByPlayer = Maps.newHashMap();
    private final Map<String, MapBannerMarker> banners = Maps.newHashMap();
    final Map<String, MapDecoration> decorations = Maps.newLinkedHashMap();
    private final Map<String, MapFrameMarker> frames = Maps.newHashMap();
    private int decorationCount;

    public static PersistentState.Type<MapState> getPersistentStateType() {
        return new PersistentState.Type<MapState>(() -> {
            throw new IllegalStateException("Should never create an empty map saved data");
        }, MapState::fromNbt, DataFixTypes.SAVED_DATA_MAP_DATA);
    }

    private MapState(int centerX, int centerZ, byte scale, boolean showDecorations, boolean unlimitedTracking, boolean locked, RegistryKey<World> dimension) {
        this.scale = scale;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.dimension = dimension;
        this.showDecorations = showDecorations;
        this.unlimitedTracking = unlimitedTracking;
        this.locked = locked;
        this.markDirty();
    }

    public static MapState of(double centerX, double centerZ, byte scale, boolean showDecorations, boolean unlimitedTracking, RegistryKey<World> dimension) {
        int i = 128 * (1 << scale);
        int j = MathHelper.floor((centerX + 64.0) / (double)i);
        int k = MathHelper.floor((centerZ + 64.0) / (double)i);
        int l = j * i + i / 2 - 64;
        int m = k * i + i / 2 - 64;
        return new MapState(l, m, scale, showDecorations, unlimitedTracking, false, dimension);
    }

    public static MapState of(byte scale, boolean locked, RegistryKey<World> dimension) {
        return new MapState(0, 0, scale, false, false, locked, dimension);
    }

    public static MapState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        RegistryKey<World> lv = DimensionType.worldFromDimensionNbt(new Dynamic<NbtElement>(NbtOps.INSTANCE, nbt.get("dimension"))).resultOrPartial(LOGGER::error).orElseThrow(() -> new IllegalArgumentException("Invalid map dimension: " + String.valueOf(nbt.get("dimension"))));
        int i = nbt.getInt("xCenter");
        int j = nbt.getInt("zCenter");
        byte b = (byte)MathHelper.clamp(nbt.getByte("scale"), 0, 4);
        boolean bl = !nbt.contains("trackingPosition", NbtElement.BYTE_TYPE) || nbt.getBoolean("trackingPosition");
        boolean bl2 = nbt.getBoolean("unlimitedTracking");
        boolean bl3 = nbt.getBoolean("locked");
        MapState lv2 = new MapState(i, j, b, bl, bl2, bl3, lv);
        byte[] bs = nbt.getByteArray("colors");
        if (bs.length == 16384) {
            lv2.colors = bs;
        }
        RegistryOps<NbtElement> lv3 = registryLookup.getOps(NbtOps.INSTANCE);
        List list = MapBannerMarker.LIST_CODEC.parse(lv3, nbt.get("banners")).resultOrPartial(banner -> LOGGER.warn("Failed to parse map banner: '{}'", banner)).orElse(List.of());
        for (MapBannerMarker lv4 : list) {
            lv2.banners.put(lv4.getKey(), lv4);
            lv2.addDecoration(lv4.getDecorationType(), null, lv4.getKey(), lv4.pos().getX(), lv4.pos().getZ(), 180.0, lv4.name().orElse(null));
        }
        NbtList lv5 = nbt.getList("frames", NbtElement.COMPOUND_TYPE);
        for (int k = 0; k < lv5.size(); ++k) {
            MapFrameMarker lv6 = MapFrameMarker.fromNbt(lv5.getCompound(k));
            if (lv6 == null) continue;
            lv2.frames.put(lv6.getKey(), lv6);
            lv2.addDecoration(MapDecorationTypes.FRAME, null, MapState.getFrameDecorationKey(lv6.getEntityId()), lv6.getPos().getX(), lv6.getPos().getZ(), lv6.getRotation(), null);
        }
        return lv2;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        Identifier.CODEC.encodeStart(NbtOps.INSTANCE, this.dimension.getValue()).resultOrPartial(LOGGER::error).ifPresent(arg2 -> nbt.put("dimension", (NbtElement)arg2));
        nbt.putInt("xCenter", this.centerX);
        nbt.putInt("zCenter", this.centerZ);
        nbt.putByte("scale", this.scale);
        nbt.putByteArray("colors", this.colors);
        nbt.putBoolean("trackingPosition", this.showDecorations);
        nbt.putBoolean("unlimitedTracking", this.unlimitedTracking);
        nbt.putBoolean("locked", this.locked);
        RegistryOps<NbtElement> lv = registryLookup.getOps(NbtOps.INSTANCE);
        nbt.put("banners", MapBannerMarker.LIST_CODEC.encodeStart(lv, List.copyOf(this.banners.values())).getOrThrow());
        NbtList lv2 = new NbtList();
        for (MapFrameMarker lv3 : this.frames.values()) {
            lv2.add(lv3.toNbt());
        }
        nbt.put("frames", lv2);
        return nbt;
    }

    public MapState copy() {
        MapState lv = new MapState(this.centerX, this.centerZ, this.scale, this.showDecorations, this.unlimitedTracking, true, this.dimension);
        lv.banners.putAll(this.banners);
        lv.decorations.putAll(this.decorations);
        lv.decorationCount = this.decorationCount;
        System.arraycopy(this.colors, 0, lv.colors, 0, this.colors.length);
        lv.markDirty();
        return lv;
    }

    public MapState zoomOut() {
        return MapState.of(this.centerX, this.centerZ, (byte)MathHelper.clamp(this.scale + 1, 0, 4), this.showDecorations, this.unlimitedTracking, this.dimension);
    }

    private static Predicate<ItemStack> getEqualPredicate(ItemStack stack) {
        MapIdComponent lv = stack.get(DataComponentTypes.MAP_ID);
        return other -> {
            if (other == stack) {
                return true;
            }
            return other.isOf(stack.getItem()) && Objects.equals(lv, other.get(DataComponentTypes.MAP_ID));
        };
    }

    public void update(PlayerEntity player, ItemStack stack) {
        if (!this.updateTrackersByPlayer.containsKey(player)) {
            PlayerUpdateTracker lv = new PlayerUpdateTracker(player);
            this.updateTrackersByPlayer.put(player, lv);
            this.updateTrackers.add(lv);
        }
        Predicate<ItemStack> predicate = MapState.getEqualPredicate(stack);
        if (!player.getInventory().contains(predicate)) {
            this.removeDecoration(player.getName().getString());
        }
        for (int i = 0; i < this.updateTrackers.size(); ++i) {
            PlayerUpdateTracker lv2 = this.updateTrackers.get(i);
            String string = lv2.player.getName().getString();
            if (lv2.player.isRemoved() || !lv2.player.getInventory().contains(predicate) && !stack.isInFrame()) {
                this.updateTrackersByPlayer.remove(lv2.player);
                this.updateTrackers.remove(lv2);
                this.removeDecoration(string);
                continue;
            }
            if (stack.isInFrame() || lv2.player.getWorld().getRegistryKey() != this.dimension || !this.showDecorations) continue;
            this.addDecoration(MapDecorationTypes.PLAYER, lv2.player.getWorld(), string, lv2.player.getX(), lv2.player.getZ(), lv2.player.getYaw(), null);
        }
        if (stack.isInFrame() && this.showDecorations) {
            ItemFrameEntity lv3 = stack.getFrame();
            BlockPos lv4 = lv3.getAttachedBlockPos();
            MapFrameMarker lv5 = this.frames.get(MapFrameMarker.getKey(lv4));
            if (lv5 != null && lv3.getId() != lv5.getEntityId() && this.frames.containsKey(lv5.getKey())) {
                this.removeDecoration(MapState.getFrameDecorationKey(lv5.getEntityId()));
            }
            MapFrameMarker lv6 = new MapFrameMarker(lv4, lv3.getHorizontalFacing().getHorizontal() * 90, lv3.getId());
            this.addDecoration(MapDecorationTypes.FRAME, player.getWorld(), MapState.getFrameDecorationKey(lv3.getId()), lv4.getX(), lv4.getZ(), lv3.getHorizontalFacing().getHorizontal() * 90, null);
            this.frames.put(lv6.getKey(), lv6);
        }
        MapDecorationsComponent lv7 = stack.getOrDefault(DataComponentTypes.MAP_DECORATIONS, MapDecorationsComponent.DEFAULT);
        if (!this.decorations.keySet().containsAll(lv7.decorations().keySet())) {
            lv7.decorations().forEach((id, decoration) -> {
                if (!this.decorations.containsKey(id)) {
                    this.addDecoration(decoration.type(), player.getWorld(), (String)id, decoration.x(), decoration.z(), decoration.rotation(), null);
                }
            });
        }
    }

    private void removeDecoration(String id) {
        MapDecoration lv = this.decorations.remove(id);
        if (lv != null && lv.type().value().trackCount()) {
            --this.decorationCount;
        }
        this.markDecorationsDirty();
    }

    public static void addDecorationsNbt(ItemStack stack, BlockPos pos, String id, RegistryEntry<MapDecorationType> decorationType) {
        MapDecorationsComponent.Decoration lv = new MapDecorationsComponent.Decoration(decorationType, pos.getX(), pos.getZ(), 180.0f);
        stack.apply(DataComponentTypes.MAP_DECORATIONS, MapDecorationsComponent.DEFAULT, decorations -> decorations.with(id, lv));
        if (decorationType.value().hasMapColor()) {
            stack.set(DataComponentTypes.MAP_COLOR, new MapColorComponent(decorationType.value().mapColor()));
        }
    }

    private void addDecoration(RegistryEntry<MapDecorationType> type, @Nullable WorldAccess world, String key, double x, double z, double rotation, @Nullable Text text) {
        MapDecoration lv2;
        MapDecoration lv;
        byte k;
        int i = 1 << this.scale;
        float g = (float)(x - (double)this.centerX) / (float)i;
        float h = (float)(z - (double)this.centerZ) / (float)i;
        byte b = (byte)((double)(g * 2.0f) + 0.5);
        byte c = (byte)((double)(h * 2.0f) + 0.5);
        int j = 63;
        if (g >= -63.0f && h >= -63.0f && g <= 63.0f && h <= 63.0f) {
            k = (byte)((rotation += rotation < 0.0 ? -8.0 : 8.0) * 16.0 / 360.0);
            if (this.dimension == World.NETHER && world != null) {
                l = (int)(world.getLevelProperties().getTimeOfDay() / 10L);
                k = (byte)(l * l * 34187121 + l * 121 >> 15 & 0xF);
            }
        } else if (type.matches(MapDecorationTypes.PLAYER)) {
            l = 320;
            if (Math.abs(g) < 320.0f && Math.abs(h) < 320.0f) {
                type = MapDecorationTypes.PLAYER_OFF_MAP;
            } else if (this.unlimitedTracking) {
                type = MapDecorationTypes.PLAYER_OFF_LIMITS;
            } else {
                this.removeDecoration(key);
                return;
            }
            k = 0;
            if (g <= -63.0f) {
                b = -128;
            }
            if (h <= -63.0f) {
                c = -128;
            }
            if (g >= 63.0f) {
                b = 127;
            }
            if (h >= 63.0f) {
                c = 127;
            }
        } else {
            this.removeDecoration(key);
            return;
        }
        if (!(lv = new MapDecoration(type, b, c, k, Optional.ofNullable(text))).equals(lv2 = this.decorations.put(key, lv))) {
            if (lv2 != null && lv2.type().value().trackCount()) {
                --this.decorationCount;
            }
            if (type.value().trackCount()) {
                ++this.decorationCount;
            }
            this.markDecorationsDirty();
        }
    }

    @Nullable
    public Packet<?> getPlayerMarkerPacket(MapIdComponent mapId, PlayerEntity player) {
        PlayerUpdateTracker lv = this.updateTrackersByPlayer.get(player);
        if (lv == null) {
            return null;
        }
        return lv.getPacket(mapId);
    }

    private void markDirty(int x, int z) {
        this.markDirty();
        for (PlayerUpdateTracker lv : this.updateTrackers) {
            lv.markDirty(x, z);
        }
    }

    private void markDecorationsDirty() {
        this.markDirty();
        this.updateTrackers.forEach(PlayerUpdateTracker::markDecorationsDirty);
    }

    public PlayerUpdateTracker getPlayerSyncData(PlayerEntity player) {
        PlayerUpdateTracker lv = this.updateTrackersByPlayer.get(player);
        if (lv == null) {
            lv = new PlayerUpdateTracker(player);
            this.updateTrackersByPlayer.put(player, lv);
            this.updateTrackers.add(lv);
        }
        return lv;
    }

    public boolean addBanner(WorldAccess world, BlockPos pos) {
        double d = (double)pos.getX() + 0.5;
        double e = (double)pos.getZ() + 0.5;
        int i = 1 << this.scale;
        double f = (d - (double)this.centerX) / (double)i;
        double g = (e - (double)this.centerZ) / (double)i;
        int j = 63;
        if (f >= -63.0 && g >= -63.0 && f <= 63.0 && g <= 63.0) {
            MapBannerMarker lv = MapBannerMarker.fromWorldBlock(world, pos);
            if (lv == null) {
                return false;
            }
            if (this.banners.remove(lv.getKey(), lv)) {
                this.removeDecoration(lv.getKey());
                return true;
            }
            if (!this.decorationCountNotLessThan(256)) {
                this.banners.put(lv.getKey(), lv);
                this.addDecoration(lv.getDecorationType(), world, lv.getKey(), d, e, 180.0, lv.name().orElse(null));
                return true;
            }
        }
        return false;
    }

    public void removeBanner(BlockView world, int x, int z) {
        Iterator<MapBannerMarker> iterator = this.banners.values().iterator();
        while (iterator.hasNext()) {
            MapBannerMarker lv2;
            MapBannerMarker lv = iterator.next();
            if (lv.pos().getX() != x || lv.pos().getZ() != z || lv.equals(lv2 = MapBannerMarker.fromWorldBlock(world, lv.pos()))) continue;
            iterator.remove();
            this.removeDecoration(lv.getKey());
        }
    }

    public Collection<MapBannerMarker> getBanners() {
        return this.banners.values();
    }

    public void removeFrame(BlockPos pos, int id) {
        this.removeDecoration(MapState.getFrameDecorationKey(id));
        this.frames.remove(MapFrameMarker.getKey(pos));
    }

    public boolean putColor(int x, int z, byte color) {
        byte c = this.colors[x + z * 128];
        if (c != color) {
            this.setColor(x, z, color);
            return true;
        }
        return false;
    }

    public void setColor(int x, int z, byte color) {
        this.colors[x + z * 128] = color;
        this.markDirty(x, z);
    }

    public boolean hasExplorationMapDecoration() {
        for (MapDecoration lv : this.decorations.values()) {
            if (!lv.type().value().explorationMapElement()) continue;
            return true;
        }
        return false;
    }

    public void replaceDecorations(List<MapDecoration> decorations) {
        this.decorations.clear();
        this.decorationCount = 0;
        for (int i = 0; i < decorations.size(); ++i) {
            MapDecoration lv = decorations.get(i);
            this.decorations.put("icon-" + i, lv);
            if (!lv.type().value().trackCount()) continue;
            ++this.decorationCount;
        }
    }

    public Iterable<MapDecoration> getDecorations() {
        return this.decorations.values();
    }

    public boolean decorationCountNotLessThan(int decorationCount) {
        return this.decorationCount >= decorationCount;
    }

    private static String getFrameDecorationKey(int id) {
        return FRAME_PREFIX + id;
    }

    public class PlayerUpdateTracker {
        public final PlayerEntity player;
        private boolean dirty = true;
        private int startX;
        private int startZ;
        private int endX = 127;
        private int endZ = 127;
        private boolean decorationsDirty = true;
        private int emptyPacketsRequested;
        public int field_131;

        PlayerUpdateTracker(PlayerEntity player) {
            this.player = player;
        }

        private UpdateData getMapUpdateData() {
            int i = this.startX;
            int j = this.startZ;
            int k = this.endX + 1 - this.startX;
            int l = this.endZ + 1 - this.startZ;
            byte[] bs = new byte[k * l];
            for (int m = 0; m < k; ++m) {
                for (int n = 0; n < l; ++n) {
                    bs[m + n * k] = MapState.this.colors[i + m + (j + n) * 128];
                }
            }
            return new UpdateData(i, j, k, l, bs);
        }

        @Nullable
        Packet<?> getPacket(MapIdComponent mapId) {
            Collection<MapDecoration> collection;
            UpdateData lv;
            if (this.dirty) {
                this.dirty = false;
                lv = this.getMapUpdateData();
            } else {
                lv = null;
            }
            if (this.decorationsDirty && this.emptyPacketsRequested++ % 5 == 0) {
                this.decorationsDirty = false;
                collection = MapState.this.decorations.values();
            } else {
                collection = null;
            }
            if (collection != null || lv != null) {
                return new MapUpdateS2CPacket(mapId, MapState.this.scale, MapState.this.locked, collection, lv);
            }
            return null;
        }

        void markDirty(int startX, int startZ) {
            if (this.dirty) {
                this.startX = Math.min(this.startX, startX);
                this.startZ = Math.min(this.startZ, startZ);
                this.endX = Math.max(this.endX, startX);
                this.endZ = Math.max(this.endZ, startZ);
            } else {
                this.dirty = true;
                this.startX = startX;
                this.startZ = startZ;
                this.endX = startX;
                this.endZ = startZ;
            }
        }

        private void markDecorationsDirty() {
            this.decorationsDirty = true;
        }
    }

    public record UpdateData(int startX, int startZ, int width, int height, byte[] colors) {
        public static final PacketCodec<ByteBuf, Optional<UpdateData>> CODEC = PacketCodec.ofStatic(UpdateData::encode, UpdateData::decode);

        private static void encode(ByteBuf buf, Optional<UpdateData> updateData) {
            if (updateData.isPresent()) {
                UpdateData lv = updateData.get();
                buf.writeByte(lv.width);
                buf.writeByte(lv.height);
                buf.writeByte(lv.startX);
                buf.writeByte(lv.startZ);
                PacketByteBuf.writeByteArray(buf, lv.colors);
            } else {
                buf.writeByte(0);
            }
        }

        private static Optional<UpdateData> decode(ByteBuf buf) {
            short i = buf.readUnsignedByte();
            if (i > 0) {
                short j = buf.readUnsignedByte();
                short k = buf.readUnsignedByte();
                short l = buf.readUnsignedByte();
                byte[] bs = PacketByteBuf.readByteArray(buf);
                return Optional.of(new UpdateData(k, l, i, j, bs));
            }
            return Optional.empty();
        }

        public void setColorsTo(MapState mapState) {
            for (int i = 0; i < this.width; ++i) {
                for (int j = 0; j < this.height; ++j) {
                    mapState.setColor(this.startX + i, this.startZ + j, this.colors[i + j * this.width]);
                }
            }
        }
    }
}

