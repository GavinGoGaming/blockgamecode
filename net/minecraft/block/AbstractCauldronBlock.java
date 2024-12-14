/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PointedDripstoneBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public abstract class AbstractCauldronBlock
extends Block {
    private static final int field_30989 = 2;
    private static final int field_30990 = 4;
    private static final int field_30991 = 3;
    private static final int field_30992 = 2;
    protected static final int field_30988 = 4;
    private static final VoxelShape RAYCAST_SHAPE = AbstractCauldronBlock.createCuboidShape(2.0, 4.0, 2.0, 14.0, 16.0, 14.0);
    protected static final VoxelShape OUTLINE_SHAPE = VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(), VoxelShapes.union(AbstractCauldronBlock.createCuboidShape(0.0, 0.0, 4.0, 16.0, 3.0, 12.0), AbstractCauldronBlock.createCuboidShape(4.0, 0.0, 0.0, 12.0, 3.0, 16.0), AbstractCauldronBlock.createCuboidShape(2.0, 0.0, 2.0, 14.0, 3.0, 14.0), RAYCAST_SHAPE), BooleanBiFunction.ONLY_FIRST);
    protected final CauldronBehavior.CauldronBehaviorMap behaviorMap;

    protected abstract MapCodec<? extends AbstractCauldronBlock> getCodec();

    public AbstractCauldronBlock(AbstractBlock.Settings settings, CauldronBehavior.CauldronBehaviorMap behaviorMap) {
        super(settings);
        this.behaviorMap = behaviorMap;
    }

    protected double getFluidHeight(BlockState state) {
        return 0.0;
    }

    protected boolean isEntityTouchingFluid(BlockState state, BlockPos pos, Entity entity) {
        return entity.getY() < (double)pos.getY() + this.getFluidHeight(state) && entity.getBoundingBox().maxY > (double)pos.getY() + 0.25;
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        CauldronBehavior lv = this.behaviorMap.map().get(stack.getItem());
        return lv.interact(state, world, pos, player, hand, stack);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    protected VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return RAYCAST_SHAPE;
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    public abstract boolean isFull(BlockState var1);

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockPos lv = PointedDripstoneBlock.getDripPos(world, pos);
        if (lv == null) {
            return;
        }
        Fluid lv2 = PointedDripstoneBlock.getDripFluid(world, lv);
        if (lv2 != Fluids.EMPTY && this.canBeFilledByDripstone(lv2)) {
            this.fillFromDripstone(state, world, pos, lv2);
        }
    }

    protected boolean canBeFilledByDripstone(Fluid fluid) {
        return false;
    }

    protected void fillFromDripstone(BlockState state, World world, BlockPos pos, Fluid fluid) {
    }
}

