/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.OperatorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class StructureBlock
extends BlockWithEntity
implements OperatorBlock {
    public static final MapCodec<StructureBlock> CODEC = StructureBlock.createCodec(StructureBlock::new);
    public static final EnumProperty<StructureBlockMode> MODE = Properties.STRUCTURE_BLOCK_MODE;

    public MapCodec<StructureBlock> getCodec() {
        return CODEC;
    }

    protected StructureBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(MODE, StructureBlockMode.LOAD));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new StructureBlockBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof StructureBlockBlockEntity) {
            return ((StructureBlockBlockEntity)lv).openScreen(player) ? ActionResult.success(world.isClient) : ActionResult.PASS;
        }
        return ActionResult.PASS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        BlockEntity lv;
        if (world.isClient) {
            return;
        }
        if (placer != null && (lv = world.getBlockEntity(pos)) instanceof StructureBlockBlockEntity) {
            ((StructureBlockBlockEntity)lv).setAuthor(placer);
        }
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(MODE);
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (!(world instanceof ServerWorld)) {
            return;
        }
        BlockEntity lv = world.getBlockEntity(pos);
        if (!(lv instanceof StructureBlockBlockEntity)) {
            return;
        }
        StructureBlockBlockEntity lv2 = (StructureBlockBlockEntity)lv;
        boolean bl2 = world.isReceivingRedstonePower(pos);
        boolean bl3 = lv2.isPowered();
        if (bl2 && !bl3) {
            lv2.setPowered(true);
            this.doAction((ServerWorld)world, lv2);
        } else if (!bl2 && bl3) {
            lv2.setPowered(false);
        }
    }

    private void doAction(ServerWorld world, StructureBlockBlockEntity blockEntity) {
        switch (blockEntity.getMode()) {
            case SAVE: {
                blockEntity.saveStructure(false);
                break;
            }
            case LOAD: {
                blockEntity.loadAndPlaceStructure(world);
                break;
            }
            case CORNER: {
                blockEntity.unloadStructure();
                break;
            }
            case DATA: {
                break;
            }
        }
    }
}

