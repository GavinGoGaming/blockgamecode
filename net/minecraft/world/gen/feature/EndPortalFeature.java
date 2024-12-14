/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class EndPortalFeature
extends Feature<DefaultFeatureConfig> {
    public static final int field_31503 = 4;
    public static final int field_31504 = 4;
    public static final int field_31505 = 1;
    public static final float field_31506 = 0.5f;
    private static final BlockPos ORIGIN = BlockPos.ORIGIN;
    private final boolean open;

    public static BlockPos offsetOrigin(BlockPos pos) {
        return ORIGIN.add(pos);
    }

    public EndPortalFeature(boolean open) {
        super(DefaultFeatureConfig.CODEC);
        this.open = open;
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        BlockPos lv = context.getOrigin();
        StructureWorldAccess lv2 = context.getWorld();
        for (BlockPos lv3 : BlockPos.iterate(new BlockPos(lv.getX() - 4, lv.getY() - 1, lv.getZ() - 4), new BlockPos(lv.getX() + 4, lv.getY() + 32, lv.getZ() + 4))) {
            boolean bl = lv3.isWithinDistance(lv, 2.5);
            if (!bl && !lv3.isWithinDistance(lv, 3.5)) continue;
            if (lv3.getY() < lv.getY()) {
                if (bl) {
                    this.setBlockState(lv2, lv3, Blocks.BEDROCK.getDefaultState());
                    continue;
                }
                if (lv3.getY() >= lv.getY()) continue;
                this.setBlockState(lv2, lv3, Blocks.END_STONE.getDefaultState());
                continue;
            }
            if (lv3.getY() > lv.getY()) {
                this.setBlockState(lv2, lv3, Blocks.AIR.getDefaultState());
                continue;
            }
            if (!bl) {
                this.setBlockState(lv2, lv3, Blocks.BEDROCK.getDefaultState());
                continue;
            }
            if (this.open) {
                this.setBlockState(lv2, new BlockPos(lv3), Blocks.END_PORTAL.getDefaultState());
                continue;
            }
            this.setBlockState(lv2, new BlockPos(lv3), Blocks.AIR.getDefaultState());
        }
        for (int i = 0; i < 4; ++i) {
            this.setBlockState(lv2, lv.up(i), Blocks.BEDROCK.getDefaultState());
        }
        BlockPos lv4 = lv.up(2);
        for (Direction lv5 : Direction.Type.HORIZONTAL) {
            this.setBlockState(lv2, lv4.offset(lv5), (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, lv5));
        }
        return true;
    }
}

