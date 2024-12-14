/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.enums;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.spawner.MobSpawnerEntry;
import net.minecraft.block.spawner.TrialSpawnerConfig;
import net.minecraft.block.spawner.TrialSpawnerData;
import net.minecraft.block.spawner.TrialSpawnerLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.OminousItemSpawnerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public enum TrialSpawnerState implements StringIdentifiable
{
    INACTIVE("inactive", 0, ParticleEmitter.NONE, -1.0, false),
    WAITING_FOR_PLAYERS("waiting_for_players", 4, ParticleEmitter.WAITING, 200.0, true),
    ACTIVE("active", 8, ParticleEmitter.ACTIVE, 1000.0, true),
    WAITING_FOR_REWARD_EJECTION("waiting_for_reward_ejection", 8, ParticleEmitter.WAITING, -1.0, false),
    EJECTING_REWARD("ejecting_reward", 8, ParticleEmitter.WAITING, -1.0, false),
    COOLDOWN("cooldown", 0, ParticleEmitter.COOLDOWN, -1.0, false);

    private static final float START_EJECTING_REWARDS_COOLDOWN = 40.0f;
    private static final int BETWEEN_EJECTING_REWARDS_COOLDOWN;
    private final String id;
    private final int luminance;
    private final double displayRotationSpeed;
    private final ParticleEmitter particleEmitter;
    private final boolean playsSound;

    private TrialSpawnerState(String id, int luminance, ParticleEmitter particleEmitter, double displayRotationSpeed, boolean playsSound) {
        this.id = id;
        this.luminance = luminance;
        this.particleEmitter = particleEmitter;
        this.displayRotationSpeed = displayRotationSpeed;
        this.playsSound = playsSound;
    }

    TrialSpawnerState tick(BlockPos pos, TrialSpawnerLogic logic, ServerWorld world) {
        TrialSpawnerData lv = logic.getData();
        TrialSpawnerConfig lv2 = logic.getConfig();
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (lv.setDisplayEntity(logic, world, WAITING_FOR_PLAYERS) == null) {
                    yield this;
                }
                yield WAITING_FOR_PLAYERS;
            }
            case 1 -> {
                if (!logic.canActivate(world)) {
                    lv.reset();
                    yield this;
                }
                if (!lv.hasSpawnData(logic, world.random)) {
                    yield INACTIVE;
                }
                lv.updatePlayers(world, pos, logic);
                if (lv.players.isEmpty()) {
                    yield this;
                }
                yield ACTIVE;
            }
            case 2 -> {
                if (!logic.canActivate(world)) {
                    lv.reset();
                    yield WAITING_FOR_PLAYERS;
                }
                if (!lv.hasSpawnData(logic, world.random)) {
                    yield INACTIVE;
                }
                int i = lv.getAdditionalPlayers(pos);
                lv.updatePlayers(world, pos, logic);
                if (logic.isOminous()) {
                    this.spawnOminousItemSpawner(world, pos, logic);
                }
                if (lv.hasSpawnedAllMobs(lv2, i)) {
                    if (lv.areMobsDead()) {
                        lv.cooldownEnd = world.getTime() + (long)logic.getCooldownLength();
                        lv.totalSpawnedMobs = 0;
                        lv.nextMobSpawnsAt = 0L;
                        yield WAITING_FOR_REWARD_EJECTION;
                    }
                } else if (lv.canSpawnMore(world, lv2, i)) {
                    logic.trySpawnMob(world, pos).ifPresent(uuid -> {
                        arg.spawnedMobsAlive.add((UUID)uuid);
                        ++arg.totalSpawnedMobs;
                        arg.nextMobSpawnsAt = world.getTime() + (long)lv2.ticksBetweenSpawn();
                        lv2.spawnPotentialsDefinition().getOrEmpty(world.getRandom()).ifPresent(spawnData -> {
                            arg.spawnData = Optional.of((MobSpawnerEntry)spawnData.data());
                            logic.updateListeners();
                        });
                    });
                }
                yield this;
            }
            case 3 -> {
                if (lv.isCooldownPast(world, 40.0f, logic.getCooldownLength())) {
                    world.playSound(null, pos, SoundEvents.BLOCK_TRIAL_SPAWNER_OPEN_SHUTTER, SoundCategory.BLOCKS);
                    yield EJECTING_REWARD;
                }
                yield this;
            }
            case 4 -> {
                if (!lv.isCooldownAtRepeating(world, BETWEEN_EJECTING_REWARDS_COOLDOWN, logic.getCooldownLength())) {
                    yield this;
                }
                if (lv.players.isEmpty()) {
                    world.playSound(null, pos, SoundEvents.BLOCK_TRIAL_SPAWNER_CLOSE_SHUTTER, SoundCategory.BLOCKS);
                    lv.rewardLootTable = Optional.empty();
                    yield COOLDOWN;
                }
                if (lv.rewardLootTable.isEmpty()) {
                    lv.rewardLootTable = lv2.lootTablesToEject().getDataOrEmpty(world.getRandom());
                }
                lv.rewardLootTable.ifPresent(lootTable -> logic.ejectLootTable(world, pos, (RegistryKey<LootTable>)lootTable));
                lv.players.remove(lv.players.iterator().next());
                yield this;
            }
            case 5 -> {
                lv.updatePlayers(world, pos, logic);
                if (!lv.players.isEmpty()) {
                    lv.totalSpawnedMobs = 0;
                    lv.nextMobSpawnsAt = 0L;
                    yield ACTIVE;
                }
                if (lv.isCooldownOver(world)) {
                    logic.setNotOminous(world, pos);
                    lv.reset();
                    yield WAITING_FOR_PLAYERS;
                }
                yield this;
            }
        };
    }

    private void spawnOminousItemSpawner(ServerWorld world, BlockPos pos2, TrialSpawnerLogic logic) {
        TrialSpawnerConfig lv2;
        TrialSpawnerData lv = logic.getData();
        ItemStack lv3 = lv.getItemsToDropWhenOminous(world, lv2 = logic.getConfig(), pos2).getDataOrEmpty(world.random).orElse(ItemStack.EMPTY);
        if (lv3.isEmpty()) {
            return;
        }
        if (this.shouldCooldownEnd(world, lv)) {
            TrialSpawnerState.getPosToSpawnItemSpawner(world, pos2, logic, lv).ifPresent(pos -> {
                OminousItemSpawnerEntity lv = OminousItemSpawnerEntity.create(world, lv3);
                lv.refreshPositionAfterTeleport((Vec3d)pos);
                world.spawnEntity(lv);
                float f = (world.getRandom().nextFloat() - world.getRandom().nextFloat()) * 0.2f + 1.0f;
                world.playSound(null, BlockPos.ofFloored(pos), SoundEvents.BLOCK_TRIAL_SPAWNER_SPAWN_ITEM_BEGIN, SoundCategory.BLOCKS, 1.0f, f);
                arg3.cooldownEnd = world.getTime() + logic.getOminousConfig().getCooldownLength();
            });
        }
    }

    private static Optional<Vec3d> getPosToSpawnItemSpawner(ServerWorld world, BlockPos pos, TrialSpawnerLogic logic, TrialSpawnerData data) {
        List<PlayerEntity> list = data.players.stream().map(world::getPlayerByUuid).filter(Objects::nonNull).filter(player -> !player.isCreative() && !player.isSpectator() && player.isAlive() && player.squaredDistanceTo(pos.toCenterPos()) <= (double)MathHelper.square(logic.getDetectionRadius())).toList();
        if (list.isEmpty()) {
            return Optional.empty();
        }
        Entity lv = TrialSpawnerState.getRandomEntity(list, data.spawnedMobsAlive, logic, pos, world);
        if (lv == null) {
            return Optional.empty();
        }
        return TrialSpawnerState.getPosAbove(lv, world);
    }

    private static Optional<Vec3d> getPosAbove(Entity entity, ServerWorld world) {
        Vec3d lv2;
        Vec3d lv = entity.getPos();
        BlockHitResult lv3 = world.raycast(new RaycastContext(lv, lv2 = lv.offset(Direction.UP, entity.getHeight() + 2.0f + (float)world.random.nextInt(4)), RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.NONE, ShapeContext.absent()));
        Vec3d lv4 = lv3.getBlockPos().toCenterPos().offset(Direction.DOWN, 1.0);
        BlockPos lv5 = BlockPos.ofFloored(lv4);
        if (!world.getBlockState(lv5).getCollisionShape(world, lv5).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(lv4);
    }

    @Nullable
    private static Entity getRandomEntity(List<PlayerEntity> players, Set<UUID> entityUuids, TrialSpawnerLogic logic, BlockPos pos, ServerWorld world) {
        List<Entity> list2;
        Stream<Entity> stream = entityUuids.stream().map(world::getEntity).filter(Objects::nonNull).filter(entity -> entity.isAlive() && entity.squaredDistanceTo(pos.toCenterPos()) <= (double)MathHelper.square(logic.getDetectionRadius()));
        List<Entity> list = list2 = world.random.nextBoolean() ? stream.toList() : players;
        if (list2.isEmpty()) {
            return null;
        }
        if (list2.size() == 1) {
            return list2.getFirst();
        }
        return Util.getRandom(list2, world.random);
    }

    private boolean shouldCooldownEnd(ServerWorld world, TrialSpawnerData data) {
        return world.getTime() >= data.cooldownEnd;
    }

    public int getLuminance() {
        return this.luminance;
    }

    public double getDisplayRotationSpeed() {
        return this.displayRotationSpeed;
    }

    public boolean doesDisplayRotate() {
        return this.displayRotationSpeed >= 0.0;
    }

    public boolean playsSound() {
        return this.playsSound;
    }

    public void emitParticles(World world, BlockPos pos, boolean ominous) {
        this.particleEmitter.emit(world, world.getRandom(), pos, ominous);
    }

    @Override
    public String asString() {
        return this.id;
    }

    static {
        BETWEEN_EJECTING_REWARDS_COOLDOWN = MathHelper.floor(30.0f);
    }

    static interface ParticleEmitter {
        public static final ParticleEmitter NONE = (world, random, pos, ominous) -> {};
        public static final ParticleEmitter WAITING = (world, random, pos, ominous) -> {
            if (random.nextInt(2) == 0) {
                Vec3d lv = pos.toCenterPos().addRandom(random, 0.9f);
                ParticleEmitter.emitParticle(ominous ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME, lv, world);
            }
        };
        public static final ParticleEmitter ACTIVE = (world, random, pos, ominous) -> {
            Vec3d lv = pos.toCenterPos().addRandom(random, 1.0f);
            ParticleEmitter.emitParticle(ParticleTypes.SMOKE, lv, world);
            ParticleEmitter.emitParticle(ominous ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME, lv, world);
        };
        public static final ParticleEmitter COOLDOWN = (world, random, pos, ominous) -> {
            Vec3d lv = pos.toCenterPos().addRandom(random, 0.9f);
            if (random.nextInt(3) == 0) {
                ParticleEmitter.emitParticle(ParticleTypes.SMOKE, lv, world);
            }
            if (world.getTime() % 20L == 0L) {
                Vec3d lv2 = pos.toCenterPos().add(0.0, 0.5, 0.0);
                int i = world.getRandom().nextInt(4) + 20;
                for (int j = 0; j < i; ++j) {
                    ParticleEmitter.emitParticle(ParticleTypes.SMOKE, lv2, world);
                }
            }
        };

        private static void emitParticle(SimpleParticleType type, Vec3d pos, World world) {
            world.addParticle(type, pos.getX(), pos.getY(), pos.getZ(), 0.0, 0.0, 0.0);
        }

        public void emit(World var1, Random var2, BlockPos var3, boolean var4);
    }

    static class Luminance {
        private static final int NONE = 0;
        private static final int LOW = 4;
        private static final int HIGH = 8;

        private Luminance() {
        }
    }

    static class DisplayRotationSpeed {
        private static final double NONE = -1.0;
        private static final double SLOW = 200.0;
        private static final double FAST = 1000.0;

        private DisplayRotationSpeed() {
        }
    }
}

