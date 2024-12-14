/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Portal;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.feature.EndPlatformFeature;

public class EndPortalBlock
extends BlockWithEntity
implements Portal {
    public static final MapCodec<EndPortalBlock> CODEC = EndPortalBlock.createCodec(EndPortalBlock::new);
    protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 6.0, 0.0, 16.0, 12.0, 16.0);

    public MapCodec<EndPortalBlock> getCodec() {
        return CODEC;
    }

    protected EndPortalBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EndPortalBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    /*
     * Enabled aggressive block sorting
     */
    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!entity.canUsePortals(false)) return;
        if (!VoxelShapes.matchesAnywhere(VoxelShapes.cuboid(entity.getBoundingBox().offset(-pos.getX(), -pos.getY(), -pos.getZ())), state.getOutlineShape(world, pos), BooleanBiFunction.AND)) return;
        if (!world.isClient && world.getRegistryKey() == World.END && entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv = (ServerPlayerEntity)entity;
            if (!lv.seenCredits) {
                lv.detachForDimensionChange();
                return;
            }
        }
        entity.tryUsePortal(this, pos);
    }

    @Override
    public TeleportTarget createTeleportTarget(ServerWorld world, Entity entity, BlockPos pos) {
        RegistryKey<World> lv = world.getRegistryKey() == World.END ? World.OVERWORLD : World.END;
        ServerWorld lv2 = world.getServer().getWorld(lv);
        if (lv2 == null) {
            return null;
        }
        boolean bl = lv == World.END;
        BlockPos lv3 = bl ? ServerWorld.END_SPAWN_POS : lv2.getSpawnPos();
        Vec3d lv4 = lv3.toBottomCenterPos();
        float f = entity.getYaw();
        if (bl) {
            EndPlatformFeature.generate(lv2, BlockPos.ofFloored(lv4).down(), true);
            f = Direction.WEST.asRotation();
            if (entity instanceof ServerPlayerEntity) {
                lv4 = lv4.subtract(0.0, 1.0, 0.0);
            }
        } else {
            if (entity instanceof ServerPlayerEntity) {
                ServerPlayerEntity lv5 = (ServerPlayerEntity)entity;
                return lv5.getRespawnTarget(false, TeleportTarget.NO_OP);
            }
            lv4 = entity.getWorldSpawnPos(lv2, lv3).toBottomCenterPos();
        }
        return new TeleportTarget(lv2, lv4, entity.getVelocity(), f, entity.getPitch(), TeleportTarget.SEND_TRAVEL_THROUGH_PORTAL_PACKET.then(TeleportTarget.ADD_PORTAL_CHUNK_TICKET));
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        double d = (double)pos.getX() + random.nextDouble();
        double e = (double)pos.getY() + 0.8;
        double f = (double)pos.getZ() + random.nextDouble();
        world.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    protected boolean canBucketPlace(BlockState state, Fluid fluid) {
        return false;
    }
}

