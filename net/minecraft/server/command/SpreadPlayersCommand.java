/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic4CommandExceptionType;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec2ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;

public class SpreadPlayersCommand {
    private static final int MAX_ATTEMPTS = 10000;
    private static final Dynamic4CommandExceptionType FAILED_TEAMS_EXCEPTION = new Dynamic4CommandExceptionType((pilesCount, x, z, maxSpreadDistance) -> Text.stringifiedTranslatable("commands.spreadplayers.failed.teams", pilesCount, x, z, maxSpreadDistance));
    private static final Dynamic4CommandExceptionType FAILED_ENTITIES_EXCEPTION = new Dynamic4CommandExceptionType((pilesCount, x, z, maxSpreadDistance) -> Text.stringifiedTranslatable("commands.spreadplayers.failed.entities", pilesCount, x, z, maxSpreadDistance));
    private static final Dynamic2CommandExceptionType INVALID_HEIGHT_EXCEPTION = new Dynamic2CommandExceptionType((maxY, worldBottomY) -> Text.stringifiedTranslatable("commands.spreadplayers.failed.invalid.height", maxY, worldBottomY));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("spreadplayers").requires(source -> source.hasPermissionLevel(2))).then(CommandManager.argument("center", Vec2ArgumentType.vec2()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("spreadDistance", FloatArgumentType.floatArg(0.0f)).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("maxRange", FloatArgumentType.floatArg(1.0f)).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("respectTeams", BoolArgumentType.bool()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("targets", EntityArgumentType.entities()).executes(context -> SpreadPlayersCommand.execute((ServerCommandSource)context.getSource(), Vec2ArgumentType.getVec2(context, "center"), FloatArgumentType.getFloat(context, "spreadDistance"), FloatArgumentType.getFloat(context, "maxRange"), ((ServerCommandSource)context.getSource()).getWorld().getTopY(), BoolArgumentType.getBool(context, "respectTeams"), EntityArgumentType.getEntities(context, "targets")))))).then(CommandManager.literal("under").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("maxHeight", IntegerArgumentType.integer()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("respectTeams", BoolArgumentType.bool()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("targets", EntityArgumentType.entities()).executes(context -> SpreadPlayersCommand.execute((ServerCommandSource)context.getSource(), Vec2ArgumentType.getVec2(context, "center"), FloatArgumentType.getFloat(context, "spreadDistance"), FloatArgumentType.getFloat(context, "maxRange"), IntegerArgumentType.getInteger(context, "maxHeight"), BoolArgumentType.getBool(context, "respectTeams"), EntityArgumentType.getEntities(context, "targets")))))))))));
    }

    private static int execute(ServerCommandSource source, Vec2f center, float spreadDistance, float maxRange, int maxY, boolean respectTeams, Collection<? extends Entity> players) throws CommandSyntaxException {
        ServerWorld lv = source.getWorld();
        int j = lv.getBottomY();
        if (maxY < j) {
            throw INVALID_HEIGHT_EXCEPTION.create(maxY, j);
        }
        Random lv2 = Random.create();
        double d = center.x - maxRange;
        double e = center.y - maxRange;
        double h = center.x + maxRange;
        double k = center.y + maxRange;
        Pile[] lvs = SpreadPlayersCommand.makePiles(lv2, respectTeams ? SpreadPlayersCommand.getPileCountRespectingTeams(players) : players.size(), d, e, h, k);
        SpreadPlayersCommand.spread(center, spreadDistance, lv, lv2, d, e, h, k, maxY, lvs, respectTeams);
        double l = SpreadPlayersCommand.getMinDistance(players, lv, lvs, maxY, respectTeams);
        source.sendFeedback(() -> Text.translatable("commands.spreadplayers.success." + (respectTeams ? "teams" : "entities"), lvs.length, Float.valueOf(arg.x), Float.valueOf(arg.y), String.format(Locale.ROOT, "%.2f", l)), true);
        return lvs.length;
    }

    private static int getPileCountRespectingTeams(Collection<? extends Entity> entities) {
        HashSet<Team> set = Sets.newHashSet();
        for (Entity entity : entities) {
            if (entity instanceof PlayerEntity) {
                set.add(entity.getScoreboardTeam());
                continue;
            }
            set.add(null);
        }
        return set.size();
    }

    private static void spread(Vec2f center, double spreadDistance, ServerWorld world, Random random, double minX, double minZ, double maxX, double maxZ, int maxY, Pile[] piles, boolean respectTeams) throws CommandSyntaxException {
        int k;
        boolean bl2 = true;
        double j = 3.4028234663852886E38;
        for (k = 0; k < 10000 && bl2; ++k) {
            bl2 = false;
            j = 3.4028234663852886E38;
            for (int l = 0; l < piles.length; ++l) {
                Pile lv = piles[l];
                int m = 0;
                Pile lv2 = new Pile();
                for (int n = 0; n < piles.length; ++n) {
                    if (l == n) continue;
                    Pile lv3 = piles[n];
                    double o = lv.getDistance(lv3);
                    j = Math.min(o, j);
                    if (!(o < spreadDistance)) continue;
                    ++m;
                    lv2.x += lv3.x - lv.x;
                    lv2.z += lv3.z - lv.z;
                }
                if (m > 0) {
                    lv2.x /= (double)m;
                    lv2.z /= (double)m;
                    double p = lv2.absolute();
                    if (p > 0.0) {
                        lv2.normalize();
                        lv.subtract(lv2);
                    } else {
                        lv.setPileLocation(random, minX, minZ, maxX, maxZ);
                    }
                    bl2 = true;
                }
                if (!lv.clamp(minX, minZ, maxX, maxZ)) continue;
                bl2 = true;
            }
            if (bl2) continue;
            for (Pile lv2 : piles) {
                if (lv2.isSafe(world, maxY)) continue;
                lv2.setPileLocation(random, minX, minZ, maxX, maxZ);
                bl2 = true;
            }
        }
        if (j == 3.4028234663852886E38) {
            j = 0.0;
        }
        if (k >= 10000) {
            if (respectTeams) {
                throw FAILED_TEAMS_EXCEPTION.create(piles.length, Float.valueOf(center.x), Float.valueOf(center.y), String.format(Locale.ROOT, "%.2f", j));
            }
            throw FAILED_ENTITIES_EXCEPTION.create(piles.length, Float.valueOf(center.x), Float.valueOf(center.y), String.format(Locale.ROOT, "%.2f", j));
        }
    }

    private static double getMinDistance(Collection<? extends Entity> entities, ServerWorld world, Pile[] piles, int maxY, boolean respectTeams) {
        double d = 0.0;
        int j = 0;
        HashMap<Team, Pile> map = Maps.newHashMap();
        for (Entity entity : entities) {
            Pile lv3;
            if (respectTeams) {
                Team lv2;
                Team team = lv2 = entity instanceof PlayerEntity ? entity.getScoreboardTeam() : null;
                if (!map.containsKey(lv2)) {
                    map.put(lv2, piles[j++]);
                }
                lv3 = (Pile)map.get(lv2);
            } else {
                lv3 = piles[j++];
            }
            entity.teleport(world, (double)MathHelper.floor(lv3.x) + 0.5, lv3.getY(world, maxY), (double)MathHelper.floor(lv3.z) + 0.5, Set.of(), entity.getYaw(), entity.getPitch());
            double e = Double.MAX_VALUE;
            for (Pile lv4 : piles) {
                if (lv3 == lv4) continue;
                double f = lv3.getDistance(lv4);
                e = Math.min(f, e);
            }
            d += e;
        }
        if (entities.size() < 2) {
            return 0.0;
        }
        return d /= (double)entities.size();
    }

    private static Pile[] makePiles(Random random, int count, double minX, double minZ, double maxX, double maxZ) {
        Pile[] lvs = new Pile[count];
        for (int j = 0; j < lvs.length; ++j) {
            Pile lv = new Pile();
            lv.setPileLocation(random, minX, minZ, maxX, maxZ);
            lvs[j] = lv;
        }
        return lvs;
    }

    static class Pile {
        double x;
        double z;

        Pile() {
        }

        double getDistance(Pile other) {
            double d = this.x - other.x;
            double e = this.z - other.z;
            return Math.sqrt(d * d + e * e);
        }

        void normalize() {
            double d = this.absolute();
            this.x /= d;
            this.z /= d;
        }

        double absolute() {
            return Math.sqrt(this.x * this.x + this.z * this.z);
        }

        public void subtract(Pile other) {
            this.x -= other.x;
            this.z -= other.z;
        }

        public boolean clamp(double minX, double minZ, double maxX, double maxZ) {
            boolean bl = false;
            if (this.x < minX) {
                this.x = minX;
                bl = true;
            } else if (this.x > maxX) {
                this.x = maxX;
                bl = true;
            }
            if (this.z < minZ) {
                this.z = minZ;
                bl = true;
            } else if (this.z > maxZ) {
                this.z = maxZ;
                bl = true;
            }
            return bl;
        }

        public int getY(BlockView blockView, int maxY) {
            BlockPos.Mutable lv = new BlockPos.Mutable(this.x, (double)(maxY + 1), this.z);
            boolean bl = blockView.getBlockState(lv).isAir();
            lv.move(Direction.DOWN);
            boolean bl2 = blockView.getBlockState(lv).isAir();
            while (lv.getY() > blockView.getBottomY()) {
                lv.move(Direction.DOWN);
                boolean bl3 = blockView.getBlockState(lv).isAir();
                if (!bl3 && bl2 && bl) {
                    return lv.getY() + 1;
                }
                bl = bl2;
                bl2 = bl3;
            }
            return maxY + 1;
        }

        public boolean isSafe(BlockView world, int maxY) {
            BlockPos lv = BlockPos.ofFloored(this.x, this.getY(world, maxY) - 1, this.z);
            BlockState lv2 = world.getBlockState(lv);
            return lv.getY() < maxY && !lv2.isLiquid() && !lv2.isIn(BlockTags.FIRE);
        }

        public void setPileLocation(Random random, double minX, double minZ, double maxX, double maxZ) {
            this.x = MathHelper.nextDouble(random, minX, maxX);
            this.z = MathHelper.nextDouble(random, minZ, maxZ);
        }
    }
}

