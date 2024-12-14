/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Portal;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class EndGatewayBlock
extends BlockWithEntity
implements Portal {
    public static final MapCodec<EndGatewayBlock> CODEC = EndGatewayBlock.createCodec(EndGatewayBlock::new);

    public MapCodec<EndGatewayBlock> getCodec() {
        return CODEC;
    }

    protected EndGatewayBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EndGatewayBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return EndGatewayBlock.validateTicker(type, BlockEntityType.END_GATEWAY, world.isClient ? EndGatewayBlockEntity::clientTick : EndGatewayBlockEntity::serverTick);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (!(lv instanceof EndGatewayBlockEntity)) {
            return;
        }
        int i = ((EndGatewayBlockEntity)lv).getDrawnSidesCount();
        for (int j = 0; j < i; ++j) {
            double d = (double)pos.getX() + random.nextDouble();
            double e = (double)pos.getY() + random.nextDouble();
            double f = (double)pos.getZ() + random.nextDouble();
            double g = (random.nextDouble() - 0.5) * 0.5;
            double h = (random.nextDouble() - 0.5) * 0.5;
            double k = (random.nextDouble() - 0.5) * 0.5;
            int l = random.nextInt(2) * 2 - 1;
            if (random.nextBoolean()) {
                f = (double)pos.getZ() + 0.5 + 0.25 * (double)l;
                k = random.nextFloat() * 2.0f * (float)l;
            } else {
                d = (double)pos.getX() + 0.5 + 0.25 * (double)l;
                g = random.nextFloat() * 2.0f * (float)l;
            }
            world.addParticle(ParticleTypes.PORTAL, d, e, f, g, h, k);
        }
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    protected boolean canBucketPlace(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity.canUsePortals(false)) {
            EndGatewayBlockEntity lv2;
            BlockEntity lv = world.getBlockEntity(pos);
            if (!world.isClient && lv instanceof EndGatewayBlockEntity && !(lv2 = (EndGatewayBlockEntity)lv).needsCooldownBeforeTeleporting()) {
                entity.tryUsePortal(this, pos);
                EndGatewayBlockEntity.startTeleportCooldown(world, pos, state, lv2);
            }
        }
    }

    @Override
    @Nullable
    public TeleportTarget createTeleportTarget(ServerWorld world, Entity entity, BlockPos pos) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (!(lv instanceof EndGatewayBlockEntity)) {
            return null;
        }
        EndGatewayBlockEntity lv2 = (EndGatewayBlockEntity)lv;
        Vec3d lv3 = lv2.getOrCreateExitPortalPos(world, pos);
        if (lv3 != null) {
            return new TeleportTarget(world, lv3, EndGatewayBlock.getTeleportVelocity(entity), entity.getYaw(), entity.getPitch(), TeleportTarget.ADD_PORTAL_CHUNK_TICKET);
        }
        return null;
    }

    private static Vec3d getTeleportVelocity(Entity entity) {
        if (entity instanceof EnderPearlEntity) {
            return new Vec3d(0.0, -1.0, 0.0);
        }
        return entity.getVelocity();
    }
}

