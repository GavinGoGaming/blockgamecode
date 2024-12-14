/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.OperatorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.block.enums.Orientation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class JigsawBlock
extends Block
implements BlockEntityProvider,
OperatorBlock {
    public static final MapCodec<JigsawBlock> CODEC = JigsawBlock.createCodec(JigsawBlock::new);
    public static final EnumProperty<Orientation> ORIENTATION = Properties.ORIENTATION;

    public MapCodec<JigsawBlock> getCodec() {
        return CODEC;
    }

    protected JigsawBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(ORIENTATION, Orientation.NORTH_UP));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ORIENTATION);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(ORIENTATION, rotation.getDirectionTransformation().mapJigsawOrientation(state.get(ORIENTATION)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return (BlockState)state.with(ORIENTATION, mirror.getDirectionTransformation().mapJigsawOrientation(state.get(ORIENTATION)));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction lv = ctx.getSide();
        Direction lv2 = lv.getAxis() == Direction.Axis.Y ? ctx.getHorizontalPlayerFacing().getOpposite() : Direction.UP;
        return (BlockState)this.getDefaultState().with(ORIENTATION, Orientation.byDirections(lv, lv2));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new JigsawBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof JigsawBlockEntity && player.isCreativeLevelTwoOp()) {
            player.openJigsawScreen((JigsawBlockEntity)lv);
            return ActionResult.success(world.isClient);
        }
        return ActionResult.PASS;
    }

    public static boolean attachmentMatches(StructureTemplate.StructureBlockInfo info1, StructureTemplate.StructureBlockInfo info2) {
        Direction lv = JigsawBlock.getFacing(info1.state());
        Direction lv2 = JigsawBlock.getFacing(info2.state());
        Direction lv3 = JigsawBlock.getRotation(info1.state());
        Direction lv4 = JigsawBlock.getRotation(info2.state());
        JigsawBlockEntity.Joint lv5 = JigsawBlockEntity.Joint.byName(info1.nbt().getString("joint")).orElseGet(() -> lv.getAxis().isHorizontal() ? JigsawBlockEntity.Joint.ALIGNED : JigsawBlockEntity.Joint.ROLLABLE);
        boolean bl = lv5 == JigsawBlockEntity.Joint.ROLLABLE;
        return lv == lv2.getOpposite() && (bl || lv3 == lv4) && info1.nbt().getString("target").equals(info2.nbt().getString("name"));
    }

    public static Direction getFacing(BlockState state) {
        return state.get(ORIENTATION).getFacing();
    }

    public static Direction getRotation(BlockState state) {
        return state.get(ORIENTATION).getRotation();
    }
}

