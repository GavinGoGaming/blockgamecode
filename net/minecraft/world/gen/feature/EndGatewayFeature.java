/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.EndGatewayFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class EndGatewayFeature
extends Feature<EndGatewayFeatureConfig> {
    public EndGatewayFeature(Codec<EndGatewayFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<EndGatewayFeatureConfig> context) {
        BlockPos lv = context.getOrigin();
        StructureWorldAccess lv2 = context.getWorld();
        EndGatewayFeatureConfig lv3 = context.getConfig();
        for (BlockPos lv4 : BlockPos.iterate(lv.add(-1, -2, -1), lv.add(1, 2, 1))) {
            boolean bl4;
            boolean bl = lv4.getX() == lv.getX();
            boolean bl2 = lv4.getY() == lv.getY();
            boolean bl3 = lv4.getZ() == lv.getZ();
            boolean bl5 = bl4 = Math.abs(lv4.getY() - lv.getY()) == 2;
            if (bl && bl2 && bl3) {
                BlockPos lv5 = lv4.toImmutable();
                this.setBlockState(lv2, lv5, Blocks.END_GATEWAY.getDefaultState());
                lv3.getExitPos().ifPresent(pos -> {
                    BlockEntity lv = lv2.getBlockEntity(lv5);
                    if (lv instanceof EndGatewayBlockEntity) {
                        EndGatewayBlockEntity lv2 = (EndGatewayBlockEntity)lv;
                        lv2.setExitPortalPos((BlockPos)pos, lv3.isExact());
                    }
                });
                continue;
            }
            if (bl2) {
                this.setBlockState(lv2, lv4, Blocks.AIR.getDefaultState());
                continue;
            }
            if (bl4 && bl && bl3) {
                this.setBlockState(lv2, lv4, Blocks.BEDROCK.getDefaultState());
                continue;
            }
            if (!bl && !bl3 || bl4) {
                this.setBlockState(lv2, lv4, Blocks.AIR.getDefaultState());
                continue;
            }
            this.setBlockState(lv2, lv4, Blocks.BEDROCK.getDefaultState());
        }
        return true;
    }
}

