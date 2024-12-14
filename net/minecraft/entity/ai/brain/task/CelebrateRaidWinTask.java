/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.SeekSkyTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.raid.Raid;
import org.jetbrains.annotations.Nullable;

public class CelebrateRaidWinTask
extends MultiTickTask<VillagerEntity> {
    @Nullable
    private Raid raid;

    public CelebrateRaidWinTask(int minRunTime, int maxRunTime) {
        super(ImmutableMap.of(), minRunTime, maxRunTime);
    }

    @Override
    protected boolean shouldRun(ServerWorld arg, VillagerEntity arg2) {
        BlockPos lv = arg2.getBlockPos();
        this.raid = arg.getRaidAt(lv);
        return this.raid != null && this.raid.hasWon() && SeekSkyTask.isSkyVisible(arg, arg2, lv);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        return this.raid != null && !this.raid.hasStopped();
    }

    @Override
    protected void finishRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        this.raid = null;
        arg2.getBrain().refreshActivities(arg.getTimeOfDay(), arg.getTime());
    }

    @Override
    protected void keepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        Random lv = arg2.getRandom();
        if (lv.nextInt(100) == 0) {
            arg2.playCelebrateSound();
        }
        if (lv.nextInt(200) == 0 && SeekSkyTask.isSkyVisible(arg, arg2, arg2.getBlockPos())) {
            DyeColor lv2 = Util.getRandom(DyeColor.values(), lv);
            int i = lv.nextInt(3);
            ItemStack lv3 = this.createFirework(lv2, i);
            FireworkRocketEntity lv4 = new FireworkRocketEntity(arg2.getWorld(), arg2, arg2.getX(), arg2.getEyeY(), arg2.getZ(), lv3);
            arg2.getWorld().spawnEntity(lv4);
        }
    }

    private ItemStack createFirework(DyeColor color, int flight) {
        ItemStack lv = new ItemStack(Items.FIREWORK_ROCKET);
        lv.set(DataComponentTypes.FIREWORKS, new FireworksComponent((byte)flight, List.of(new FireworkExplosionComponent(FireworkExplosionComponent.Type.BURST, IntList.of(color.getFireworkColor()), IntList.of(), false, false))));
        return lv;
    }
}

