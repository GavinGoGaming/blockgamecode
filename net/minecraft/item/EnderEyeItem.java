/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

public class EnderEyeItem
extends Item {
    public EnderEyeItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos lv2;
        World lv = context.getWorld();
        BlockState lv3 = lv.getBlockState(lv2 = context.getBlockPos());
        if (!lv3.isOf(Blocks.END_PORTAL_FRAME) || lv3.get(EndPortalFrameBlock.EYE).booleanValue()) {
            return ActionResult.PASS;
        }
        if (lv.isClient) {
            return ActionResult.SUCCESS;
        }
        BlockState lv4 = (BlockState)lv3.with(EndPortalFrameBlock.EYE, true);
        Block.pushEntitiesUpBeforeBlockChange(lv3, lv4, lv, lv2);
        lv.setBlockState(lv2, lv4, Block.NOTIFY_LISTENERS);
        lv.updateComparators(lv2, Blocks.END_PORTAL_FRAME);
        context.getStack().decrement(1);
        lv.syncWorldEvent(WorldEvents.END_PORTAL_FRAME_FILLED, lv2, 0);
        BlockPattern.Result lv5 = EndPortalFrameBlock.getCompletedFramePattern().searchAround(lv, lv2);
        if (lv5 != null) {
            BlockPos lv6 = lv5.getFrontTopLeft().add(-3, 0, -3);
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    lv.setBlockState(lv6.add(i, 0, j), Blocks.END_PORTAL.getDefaultState(), Block.NOTIFY_LISTENERS);
                }
            }
            lv.syncGlobalEvent(WorldEvents.END_PORTAL_OPENED, lv6.add(1, 0, 1), 0);
        }
        return ActionResult.CONSUME;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 0;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ServerWorld lv3;
        BlockPos lv4;
        ItemStack lv = user.getStackInHand(hand);
        BlockHitResult lv2 = EnderEyeItem.raycast(world, user, RaycastContext.FluidHandling.NONE);
        if (lv2.getType() == HitResult.Type.BLOCK && world.getBlockState(lv2.getBlockPos()).isOf(Blocks.END_PORTAL_FRAME)) {
            return TypedActionResult.pass(lv);
        }
        user.setCurrentHand(hand);
        if (world instanceof ServerWorld && (lv4 = (lv3 = (ServerWorld)world).locateStructure(StructureTags.EYE_OF_ENDER_LOCATED, user.getBlockPos(), 100, false)) != null) {
            EyeOfEnderEntity lv5 = new EyeOfEnderEntity(world, user.getX(), user.getBodyY(0.5), user.getZ());
            lv5.setItem(lv);
            lv5.initTargetPos(lv4);
            world.emitGameEvent(GameEvent.PROJECTILE_SHOOT, lv5.getPos(), GameEvent.Emitter.of(user));
            world.spawnEntity(lv5);
            if (user instanceof ServerPlayerEntity) {
                ServerPlayerEntity lv6 = (ServerPlayerEntity)user;
                Criteria.USED_ENDER_EYE.trigger(lv6, lv4);
            }
            float f = MathHelper.lerp(world.random.nextFloat(), 0.33f, 0.5f);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.NEUTRAL, 1.0f, f);
            lv.decrementUnlessCreative(1, user);
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            user.swingHand(hand, true);
            return TypedActionResult.success(lv);
        }
        return TypedActionResult.consume(lv);
    }
}

