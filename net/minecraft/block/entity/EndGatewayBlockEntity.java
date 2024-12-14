/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.EndConfiguredFeatures;
import net.minecraft.world.gen.feature.EndGatewayFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class EndGatewayBlockEntity
extends EndPortalBlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int field_31368 = 200;
    private static final int field_31369 = 40;
    private static final int field_31370 = 2400;
    private static final int field_31371 = 1;
    private static final int field_31372 = 10;
    private long age;
    private int teleportCooldown;
    @Nullable
    private BlockPos exitPortalPos;
    private boolean exactTeleport;

    public EndGatewayBlockEntity(BlockPos arg, BlockState arg2) {
        super(BlockEntityType.END_GATEWAY, arg, arg2);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putLong("Age", this.age);
        if (this.exitPortalPos != null) {
            nbt.put("exit_portal", NbtHelper.fromBlockPos(this.exitPortalPos));
        }
        if (this.exactTeleport) {
            nbt.putBoolean("ExactTeleport", true);
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.age = nbt.getLong("Age");
        NbtHelper.toBlockPos(nbt, "exit_portal").filter(World::isValid).ifPresent(exitPortalPos -> {
            this.exitPortalPos = exitPortalPos;
        });
        this.exactTeleport = nbt.getBoolean("ExactTeleport");
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, EndGatewayBlockEntity blockEntity) {
        ++blockEntity.age;
        if (blockEntity.needsCooldownBeforeTeleporting()) {
            --blockEntity.teleportCooldown;
        }
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, EndGatewayBlockEntity blockEntity) {
        boolean bl = blockEntity.isRecentlyGenerated();
        boolean bl2 = blockEntity.needsCooldownBeforeTeleporting();
        ++blockEntity.age;
        if (bl2) {
            --blockEntity.teleportCooldown;
        } else if (blockEntity.age % 2400L == 0L) {
            EndGatewayBlockEntity.startTeleportCooldown(world, pos, state, blockEntity);
        }
        if (bl != blockEntity.isRecentlyGenerated() || bl2 != blockEntity.needsCooldownBeforeTeleporting()) {
            EndGatewayBlockEntity.markDirty(world, pos, state);
        }
    }

    public boolean isRecentlyGenerated() {
        return this.age < 200L;
    }

    public boolean needsCooldownBeforeTeleporting() {
        return this.teleportCooldown > 0;
    }

    public float getRecentlyGeneratedBeamHeight(float tickDelta) {
        return MathHelper.clamp(((float)this.age + tickDelta) / 200.0f, 0.0f, 1.0f);
    }

    public float getCooldownBeamHeight(float tickDelta) {
        return 1.0f - MathHelper.clamp(((float)this.teleportCooldown - tickDelta) / 40.0f, 0.0f, 1.0f);
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return this.createComponentlessNbt(registryLookup);
    }

    public static void startTeleportCooldown(World world, BlockPos pos, BlockState state, EndGatewayBlockEntity blockEntity) {
        if (!world.isClient) {
            blockEntity.teleportCooldown = 40;
            world.addSyncedBlockEvent(pos, state.getBlock(), 1, 0);
            EndGatewayBlockEntity.markDirty(world, pos, state);
        }
    }

    @Override
    public boolean onSyncedBlockEvent(int type, int data) {
        if (type == 1) {
            this.teleportCooldown = 40;
            return true;
        }
        return super.onSyncedBlockEvent(type, data);
    }

    @Nullable
    public Vec3d getOrCreateExitPortalPos(ServerWorld world, BlockPos pos) {
        BlockPos lv;
        if (this.exitPortalPos == null && world.getRegistryKey() == World.END) {
            lv = EndGatewayBlockEntity.setupExitPortalLocation(world, pos);
            lv = lv.up(10);
            LOGGER.debug("Creating portal at {}", (Object)lv);
            EndGatewayBlockEntity.createPortal(world, lv, EndGatewayFeatureConfig.createConfig(pos, false));
            this.setExitPortalPos(lv, this.exactTeleport);
        }
        if (this.exitPortalPos != null) {
            lv = this.exactTeleport ? this.exitPortalPos : EndGatewayBlockEntity.findBestPortalExitPos(world, this.exitPortalPos);
            return lv.toBottomCenterPos();
        }
        return null;
    }

    private static BlockPos findBestPortalExitPos(World world, BlockPos pos) {
        BlockPos lv = EndGatewayBlockEntity.findExitPortalPos(world, pos.add(0, 2, 0), 5, false);
        LOGGER.debug("Best exit position for portal at {} is {}", (Object)pos, (Object)lv);
        return lv.up();
    }

    private static BlockPos setupExitPortalLocation(ServerWorld world, BlockPos pos) {
        Vec3d lv = EndGatewayBlockEntity.findTeleportLocation(world, pos);
        WorldChunk lv2 = EndGatewayBlockEntity.getChunk(world, lv);
        BlockPos lv3 = EndGatewayBlockEntity.findPortalPosition(lv2);
        if (lv3 == null) {
            BlockPos lv4 = BlockPos.ofFloored(lv.x + 0.5, 75.0, lv.z + 0.5);
            LOGGER.debug("Failed to find a suitable block to teleport to, spawning an island on {}", (Object)lv4);
            world.getRegistryManager().getOptional(RegistryKeys.CONFIGURED_FEATURE).flatMap(arg -> arg.getEntry(EndConfiguredFeatures.END_ISLAND)).ifPresent(arg3 -> ((ConfiguredFeature)arg3.value()).generate(world, world.getChunkManager().getChunkGenerator(), Random.create(lv4.asLong()), lv4));
            lv3 = lv4;
        } else {
            LOGGER.debug("Found suitable block to teleport to: {}", (Object)lv3);
        }
        return EndGatewayBlockEntity.findExitPortalPos(world, lv3, 16, true);
    }

    private static Vec3d findTeleportLocation(ServerWorld world, BlockPos pos) {
        Vec3d lv = new Vec3d(pos.getX(), 0.0, pos.getZ()).normalize();
        int i = 1024;
        Vec3d lv2 = lv.multiply(1024.0);
        int j = 16;
        while (!EndGatewayBlockEntity.isChunkEmpty(world, lv2) && j-- > 0) {
            LOGGER.debug("Skipping backwards past nonempty chunk at {}", (Object)lv2);
            lv2 = lv2.add(lv.multiply(-16.0));
        }
        j = 16;
        while (EndGatewayBlockEntity.isChunkEmpty(world, lv2) && j-- > 0) {
            LOGGER.debug("Skipping forward past empty chunk at {}", (Object)lv2);
            lv2 = lv2.add(lv.multiply(16.0));
        }
        LOGGER.debug("Found chunk at {}", (Object)lv2);
        return lv2;
    }

    private static boolean isChunkEmpty(ServerWorld world, Vec3d pos) {
        return EndGatewayBlockEntity.getChunk(world, pos).getHighestNonEmptySection() == -1;
    }

    private static BlockPos findExitPortalPos(BlockView world, BlockPos pos, int searchRadius, boolean force) {
        Vec3i lv = null;
        for (int j = -searchRadius; j <= searchRadius; ++j) {
            block1: for (int k = -searchRadius; k <= searchRadius; ++k) {
                if (j == 0 && k == 0 && !force) continue;
                for (int l = world.getTopY() - 1; l > (lv == null ? world.getBottomY() : lv.getY()); --l) {
                    BlockPos lv2 = new BlockPos(pos.getX() + j, l, pos.getZ() + k);
                    BlockState lv3 = world.getBlockState(lv2);
                    if (!lv3.isFullCube(world, lv2) || !force && lv3.isOf(Blocks.BEDROCK)) continue;
                    lv = lv2;
                    continue block1;
                }
            }
        }
        return lv == null ? pos : lv;
    }

    private static WorldChunk getChunk(World world, Vec3d pos) {
        return world.getChunk(MathHelper.floor(pos.x / 16.0), MathHelper.floor(pos.z / 16.0));
    }

    @Nullable
    private static BlockPos findPortalPosition(WorldChunk chunk) {
        ChunkPos lv = chunk.getPos();
        BlockPos lv2 = new BlockPos(lv.getStartX(), 30, lv.getStartZ());
        int i = chunk.getHighestNonEmptySectionYOffset() + 16 - 1;
        BlockPos lv3 = new BlockPos(lv.getEndX(), i, lv.getEndZ());
        BlockPos lv4 = null;
        double d = 0.0;
        for (BlockPos lv5 : BlockPos.iterate(lv2, lv3)) {
            BlockState lv6 = chunk.getBlockState(lv5);
            BlockPos lv7 = lv5.up();
            BlockPos lv8 = lv5.up(2);
            if (!lv6.isOf(Blocks.END_STONE) || chunk.getBlockState(lv7).isFullCube(chunk, lv7) || chunk.getBlockState(lv8).isFullCube(chunk, lv8)) continue;
            double e = lv5.getSquaredDistanceFromCenter(0.0, 0.0, 0.0);
            if (lv4 != null && !(e < d)) continue;
            lv4 = lv5;
            d = e;
        }
        return lv4;
    }

    private static void createPortal(ServerWorld world, BlockPos pos, EndGatewayFeatureConfig config) {
        Feature.END_GATEWAY.generateIfValid(config, world, world.getChunkManager().getChunkGenerator(), Random.create(), pos);
    }

    @Override
    public boolean shouldDrawSide(Direction direction) {
        return Block.shouldDrawSide(this.getCachedState(), this.world, this.getPos(), direction, this.getPos().offset(direction));
    }

    public int getDrawnSidesCount() {
        int i = 0;
        for (Direction lv : Direction.values()) {
            i += this.shouldDrawSide(lv) ? 1 : 0;
        }
        return i;
    }

    public void setExitPortalPos(BlockPos pos, boolean exactTeleport) {
        this.exactTeleport = exactTeleport;
        this.exitPortalPos = pos;
        this.markDirty();
    }

    public /* synthetic */ Packet toUpdatePacket() {
        return this.toUpdatePacket();
    }
}

