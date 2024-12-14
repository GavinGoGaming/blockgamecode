/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.treedecorator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CocoaBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

public class CocoaBeansTreeDecorator
extends TreeDecorator {
    public static final MapCodec<CocoaBeansTreeDecorator> CODEC = ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("probability")).xmap(CocoaBeansTreeDecorator::new, decorator -> Float.valueOf(decorator.probability));
    private final float probability;

    public CocoaBeansTreeDecorator(float probability) {
        this.probability = probability;
    }

    @Override
    protected TreeDecoratorType<?> getType() {
        return TreeDecoratorType.COCOA;
    }

    @Override
    public void generate(TreeDecorator.Generator generator) {
        Random lv = generator.getRandom();
        if (lv.nextFloat() >= this.probability) {
            return;
        }
        ObjectArrayList<BlockPos> list = generator.getLogPositions();
        int i = ((BlockPos)list.get(0)).getY();
        list.stream().filter(pos -> pos.getY() - i <= 2).forEach(pos -> {
            for (Direction lv : Direction.Type.HORIZONTAL) {
                Direction lv2;
                BlockPos lv3;
                if (!(lv.nextFloat() <= 0.25f) || !generator.isAir(lv3 = pos.add((lv2 = lv.getOpposite()).getOffsetX(), 0, lv2.getOffsetZ()))) continue;
                generator.replace(lv3, (BlockState)((BlockState)Blocks.COCOA.getDefaultState().with(CocoaBlock.AGE, lv.nextInt(3))).with(CocoaBlock.FACING, lv));
            }
        });
    }
}

