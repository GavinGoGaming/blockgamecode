/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.test;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.command.FillBiomeCommand;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.test.GameTestException;
import net.minecraft.test.GameTestState;
import net.minecraft.test.PositionedException;
import net.minecraft.test.TimedTaskRunner;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

public class TestContext {
    private final GameTestState test;
    private boolean hasFinalClause;

    public TestContext(GameTestState test) {
        this.test = test;
    }

    public ServerWorld getWorld() {
        return this.test.getWorld();
    }

    public BlockState getBlockState(BlockPos pos) {
        return this.getWorld().getBlockState(this.getAbsolutePos(pos));
    }

    public <T extends BlockEntity> T getBlockEntity(BlockPos pos) {
        BlockEntity lv = this.getWorld().getBlockEntity(this.getAbsolutePos(pos));
        if (lv == null) {
            throw new PositionedException("Missing block entity", this.getAbsolutePos(pos), pos, this.test.getTick());
        }
        return (T)lv;
    }

    public void killAllEntities() {
        this.killAllEntities(Entity.class);
    }

    public void killAllEntities(Class entityClass) {
        Box lv = this.getTestBox();
        List<Entity> list = this.getWorld().getEntitiesByClass(entityClass, lv.expand(1.0), entity -> !(entity instanceof PlayerEntity));
        list.forEach(Entity::kill);
    }

    public ItemEntity spawnItem(Item item, Vec3d pos) {
        ServerWorld lv = this.getWorld();
        Vec3d lv2 = this.getAbsolute(pos);
        ItemEntity lv3 = new ItemEntity(lv, lv2.x, lv2.y, lv2.z, new ItemStack(item, 1));
        lv3.setVelocity(0.0, 0.0, 0.0);
        lv.spawnEntity(lv3);
        return lv3;
    }

    public ItemEntity spawnItem(Item item, float x, float y, float z) {
        return this.spawnItem(item, new Vec3d(x, y, z));
    }

    public ItemEntity spawnItem(Item item, BlockPos pos) {
        return this.spawnItem(item, pos.getX(), pos.getY(), pos.getZ());
    }

    public <E extends Entity> E spawnEntity(EntityType<E> type, BlockPos pos) {
        return this.spawnEntity(type, Vec3d.ofBottomCenter(pos));
    }

    public <E extends Entity> E spawnEntity(EntityType<E> type, Vec3d pos) {
        ServerWorld lv = this.getWorld();
        E lv2 = type.create(lv);
        if (lv2 == null) {
            throw new NullPointerException("Failed to create entity " + String.valueOf(type.getRegistryEntry().registryKey().getValue()));
        }
        if (lv2 instanceof MobEntity) {
            MobEntity lv3 = (MobEntity)lv2;
            lv3.setPersistent();
        }
        Vec3d lv4 = this.getAbsolute(pos);
        ((Entity)lv2).refreshPositionAndAngles(lv4.x, lv4.y, lv4.z, ((Entity)lv2).getYaw(), ((Entity)lv2).getPitch());
        lv.spawnEntity((Entity)lv2);
        return lv2;
    }

    public <E extends Entity> E expectEntityAtOrigin(EntityType<E> type) {
        return this.expectEntity(type, 0, 0, 0, 2.147483647E9);
    }

    public <E extends Entity> E expectEntity(EntityType<E> type, int x, int y, int z, double margin) {
        List<E> list = this.getEntitiesAround(type, x, y, z, margin);
        if (list.isEmpty()) {
            throw new GameTestException("Expected " + type.getUntranslatedName() + " to exist around " + x + "," + y + "," + z);
        }
        if (list.size() > 1) {
            throw new GameTestException("Expected only one " + type.getUntranslatedName() + " to exist around " + x + "," + y + "," + z + ", but found " + list.size());
        }
        Vec3d lv = this.getAbsolute(new Vec3d(x, y, z));
        list.sort((a, b) -> {
            double d = a.getPos().distanceTo(lv);
            double e = b.getPos().distanceTo(lv);
            return Double.compare(d, e);
        });
        return (E)((Entity)list.get(0));
    }

    public <E extends Entity> List<E> getEntitiesAround(EntityType<E> type, int x, int y, int z, double margin) {
        return this.getEntitiesAround(type, Vec3d.ofBottomCenter(new BlockPos(x, y, z)), margin);
    }

    public <E extends Entity> List<E> getEntitiesAround(EntityType<E> type, Vec3d pos, double margin) {
        ServerWorld lv = this.getWorld();
        Vec3d lv2 = this.getAbsolute(pos);
        Box lv3 = this.test.getBoundingBox();
        Box lv4 = new Box(lv2.add(-margin, -margin, -margin), lv2.add(margin, margin, margin));
        return lv.getEntitiesByType(type, lv3, entity -> entity.getBoundingBox().intersects(lv4) && entity.isAlive());
    }

    public <E extends Entity> E spawnEntity(EntityType<E> type, int x, int y, int z) {
        return this.spawnEntity(type, new BlockPos(x, y, z));
    }

    public <E extends Entity> E spawnEntity(EntityType<E> type, float x, float y, float z) {
        return this.spawnEntity(type, new Vec3d(x, y, z));
    }

    public <E extends MobEntity> E spawnMob(EntityType<E> type, BlockPos pos) {
        MobEntity lv = (MobEntity)this.spawnEntity(type, pos);
        lv.clearGoalsAndTasks();
        return (E)lv;
    }

    public <E extends MobEntity> E spawnMob(EntityType<E> type, int x, int y, int z) {
        return this.spawnMob(type, new BlockPos(x, y, z));
    }

    public <E extends MobEntity> E spawnMob(EntityType<E> type, Vec3d pos) {
        MobEntity lv = (MobEntity)this.spawnEntity(type, pos);
        lv.clearGoalsAndTasks();
        return (E)lv;
    }

    public <E extends MobEntity> E spawnMob(EntityType<E> type, float x, float y, float z) {
        return this.spawnMob(type, new Vec3d(x, y, z));
    }

    public void setEntityPos(MobEntity entity, float x, float y, float z) {
        Vec3d lv = this.getAbsolute(new Vec3d(x, y, z));
        entity.refreshPositionAndAngles(lv.x, lv.y, lv.z, entity.getYaw(), entity.getPitch());
    }

    public TimedTaskRunner startMovingTowards(MobEntity entity, BlockPos pos, float speed) {
        return this.createTimedTaskRunner().expectMinDurationAndRun(2, () -> {
            Path lv = entity.getNavigation().findPathTo(this.getAbsolutePos(pos), 0);
            entity.getNavigation().startMovingAlong(lv, speed);
        });
    }

    public void pushButton(int x, int y, int z) {
        this.pushButton(new BlockPos(x, y, z));
    }

    public void pushButton(BlockPos pos) {
        this.checkBlockState(pos, state -> state.isIn(BlockTags.BUTTONS), () -> "Expected button");
        BlockPos lv = this.getAbsolutePos(pos);
        BlockState lv2 = this.getWorld().getBlockState(lv);
        ButtonBlock lv3 = (ButtonBlock)lv2.getBlock();
        lv3.powerOn(lv2, this.getWorld(), lv, null);
    }

    public void useBlock(BlockPos pos) {
        this.useBlock(pos, this.createMockPlayer(GameMode.CREATIVE));
    }

    public void useBlock(BlockPos pos, PlayerEntity player) {
        BlockPos lv = this.getAbsolutePos(pos);
        this.useBlock(pos, player, new BlockHitResult(Vec3d.ofCenter(lv), Direction.NORTH, lv, true));
    }

    public void useBlock(BlockPos pos, PlayerEntity player, BlockHitResult result) {
        Hand lv3;
        BlockPos lv = this.getAbsolutePos(pos);
        BlockState lv2 = this.getWorld().getBlockState(lv);
        ItemActionResult lv4 = lv2.onUseWithItem(player.getStackInHand(lv3 = Hand.MAIN_HAND), this.getWorld(), player, lv3, result);
        if (lv4.isAccepted()) {
            return;
        }
        if (lv4 == ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION && lv2.onUse(this.getWorld(), player, result).isAccepted()) {
            return;
        }
        ItemUsageContext lv5 = new ItemUsageContext(player, lv3, result);
        player.getStackInHand(lv3).useOnBlock(lv5);
    }

    public LivingEntity drown(LivingEntity entity) {
        entity.setAir(0);
        entity.setHealth(0.25f);
        return entity;
    }

    public LivingEntity setHealthLow(LivingEntity entity) {
        entity.setHealth(0.25f);
        return entity;
    }

    public PlayerEntity createMockPlayer(final GameMode gameMode) {
        return new PlayerEntity(this, this.getWorld(), BlockPos.ORIGIN, 0.0f, new GameProfile(UUID.randomUUID(), "test-mock-player")){

            @Override
            public boolean isSpectator() {
                return gameMode == GameMode.SPECTATOR;
            }

            @Override
            public boolean isCreative() {
                return gameMode.isCreative();
            }

            @Override
            public boolean isMainPlayer() {
                return true;
            }
        };
    }

    @Deprecated(forRemoval=true)
    public ServerPlayerEntity createMockCreativeServerPlayerInWorld() {
        ConnectedClientData lv = ConnectedClientData.createDefault(new GameProfile(UUID.randomUUID(), "test-mock-player"), false);
        ServerPlayerEntity lv2 = new ServerPlayerEntity(this, this.getWorld().getServer(), this.getWorld(), lv.gameProfile(), lv.syncedOptions()){

            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return true;
            }
        };
        ClientConnection lv3 = new ClientConnection(NetworkSide.SERVERBOUND);
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(lv3);
        this.getWorld().getServer().getPlayerManager().onPlayerConnect(lv3, lv2, lv);
        return lv2;
    }

    public void toggleLever(int x, int y, int z) {
        this.toggleLever(new BlockPos(x, y, z));
    }

    public void toggleLever(BlockPos pos) {
        this.expectBlock(Blocks.LEVER, pos);
        BlockPos lv = this.getAbsolutePos(pos);
        BlockState lv2 = this.getWorld().getBlockState(lv);
        LeverBlock lv3 = (LeverBlock)lv2.getBlock();
        lv3.togglePower(lv2, this.getWorld(), lv, null);
    }

    public void putAndRemoveRedstoneBlock(BlockPos pos, long delay) {
        this.setBlockState(pos, Blocks.REDSTONE_BLOCK);
        this.waitAndRun(delay, () -> this.setBlockState(pos, Blocks.AIR));
    }

    public void removeBlock(BlockPos pos) {
        this.getWorld().breakBlock(this.getAbsolutePos(pos), false, null);
    }

    public void setBlockState(int x, int y, int z, Block block) {
        this.setBlockState(new BlockPos(x, y, z), block);
    }

    public void setBlockState(int x, int y, int z, BlockState state) {
        this.setBlockState(new BlockPos(x, y, z), state);
    }

    public void setBlockState(BlockPos pos, Block block) {
        this.setBlockState(pos, block.getDefaultState());
    }

    public void setBlockState(BlockPos pos, BlockState state) {
        this.getWorld().setBlockState(this.getAbsolutePos(pos), state, Block.NOTIFY_ALL);
    }

    public void useNightTime() {
        this.setTime(13000);
    }

    public void setTime(int timeOfDay) {
        this.getWorld().setTimeOfDay(timeOfDay);
    }

    public void expectBlock(Block block, int x, int y, int z) {
        this.expectBlock(block, new BlockPos(x, y, z));
    }

    public void expectBlock(Block block, BlockPos pos) {
        BlockState lv = this.getBlockState(pos);
        this.checkBlock(pos, (Block block1) -> lv.isOf(block), "Expected " + block.getName().getString() + ", got " + lv.getBlock().getName().getString());
    }

    public void dontExpectBlock(Block block, int x, int y, int z) {
        this.dontExpectBlock(block, new BlockPos(x, y, z));
    }

    public void dontExpectBlock(Block block, BlockPos pos) {
        this.checkBlock(pos, (Block block1) -> !this.getBlockState(pos).isOf(block), "Did not expect " + block.getName().getString());
    }

    public void expectBlockAtEnd(Block block, int x, int y, int z) {
        this.expectBlockAtEnd(block, new BlockPos(x, y, z));
    }

    public void expectBlockAtEnd(Block block, BlockPos pos) {
        this.addInstantFinalTask(() -> this.expectBlock(block, pos));
    }

    public void checkBlock(BlockPos pos, Predicate<Block> predicate, String errorMessage) {
        this.checkBlock(pos, predicate, () -> errorMessage);
    }

    public void checkBlock(BlockPos pos, Predicate<Block> predicate, Supplier<String> errorMessageSupplier) {
        this.checkBlockState(pos, state -> predicate.test(state.getBlock()), errorMessageSupplier);
    }

    public <T extends Comparable<T>> void expectBlockProperty(BlockPos pos, Property<T> property, T value) {
        BlockState lv = this.getBlockState(pos);
        boolean bl = lv.contains(property);
        if (!bl || !lv.get(property).equals(value)) {
            String string = bl ? "was " + String.valueOf(lv.get(property)) : "property " + property.getName() + " is missing";
            String string2 = String.format(Locale.ROOT, "Expected property %s to be %s, %s", property.getName(), value, string);
            throw new PositionedException(string2, this.getAbsolutePos(pos), pos, this.test.getTick());
        }
    }

    public <T extends Comparable<T>> void checkBlockProperty(BlockPos pos, Property<T> property, Predicate<T> predicate, String errorMessage) {
        this.checkBlockState(pos, state -> {
            if (!state.contains(property)) {
                return false;
            }
            Object comparable = state.get(property);
            return predicate.test(comparable);
        }, () -> errorMessage);
    }

    public void checkBlockState(BlockPos pos, Predicate<BlockState> predicate, Supplier<String> errorMessageSupplier) {
        BlockState lv = this.getBlockState(pos);
        if (!predicate.test(lv)) {
            throw new PositionedException(errorMessageSupplier.get(), this.getAbsolutePos(pos), pos, this.test.getTick());
        }
    }

    public <T extends BlockEntity> void checkBlockEntity(BlockPos pos, Predicate<T> predicate, Supplier<String> errorMessageSupplier) {
        T lv = this.getBlockEntity(pos);
        if (!predicate.test(lv)) {
            throw new PositionedException(errorMessageSupplier.get(), this.getAbsolutePos(pos), pos, this.test.getTick());
        }
    }

    public void expectRedstonePower(BlockPos pos, Direction direction, IntPredicate powerPredicate, Supplier<String> errorMessage) {
        BlockPos lv = this.getAbsolutePos(pos);
        ServerWorld lv2 = this.getWorld();
        BlockState lv3 = lv2.getBlockState(lv);
        int i = lv3.getWeakRedstonePower(lv2, lv, direction);
        if (!powerPredicate.test(i)) {
            throw new PositionedException(errorMessage.get(), lv, pos, this.test.getTick());
        }
    }

    public void expectEntity(EntityType<?> type) {
        List<Entity> list = this.getWorld().getEntitiesByType(type, this.getTestBox(), Entity::isAlive);
        if (list.isEmpty()) {
            throw new GameTestException("Expected " + type.getUntranslatedName() + " to exist");
        }
    }

    public void expectEntityAt(EntityType<?> type, int x, int y, int z) {
        this.expectEntityAt(type, new BlockPos(x, y, z));
    }

    public void expectEntityAt(EntityType<?> type, BlockPos pos) {
        BlockPos lv = this.getAbsolutePos(pos);
        List<Entity> list = this.getWorld().getEntitiesByType(type, new Box(lv), Entity::isAlive);
        if (list.isEmpty()) {
            throw new PositionedException("Expected " + type.getUntranslatedName(), lv, pos, this.test.getTick());
        }
    }

    public void expectEntityInside(EntityType<?> type, Vec3d pos1, Vec3d pos2) {
        List<Entity> list = this.getWorld().getEntitiesByType(type, new Box(pos1, pos2), Entity::isAlive);
        if (list.isEmpty()) {
            throw new PositionedException("Expected " + type.getUntranslatedName() + " between ", BlockPos.ofFloored(pos1), BlockPos.ofFloored(pos2), this.test.getTick());
        }
    }

    public void expectEntities(EntityType<?> type, int amount) {
        List<Entity> list = this.getWorld().getEntitiesByType(type, this.getTestBox(), Entity::isAlive);
        if (list.size() != amount) {
            throw new GameTestException("Expected " + amount + " of type " + type.getUntranslatedName() + " to exist, found " + list.size());
        }
    }

    public void expectEntitiesAround(EntityType<?> type, BlockPos pos, int amount, double radius) {
        BlockPos lv = this.getAbsolutePos(pos);
        List<?> list = this.getEntitiesAround(type, pos, radius);
        if (list.size() != amount) {
            throw new PositionedException("Expected " + amount + " entities of type " + type.getUntranslatedName() + ", actual number of entities found=" + list.size(), lv, pos, this.test.getTick());
        }
    }

    public void expectEntityAround(EntityType<?> type, BlockPos pos, double radius) {
        List<?> list = this.getEntitiesAround(type, pos, radius);
        if (list.isEmpty()) {
            BlockPos lv = this.getAbsolutePos(pos);
            throw new PositionedException("Expected " + type.getUntranslatedName(), lv, pos, this.test.getTick());
        }
    }

    public <T extends Entity> List<T> getEntitiesAround(EntityType<T> type, BlockPos pos, double radius) {
        BlockPos lv = this.getAbsolutePos(pos);
        return this.getWorld().getEntitiesByType(type, new Box(lv).expand(radius), Entity::isAlive);
    }

    public <T extends Entity> List<T> getEntities(EntityType<T> type) {
        return this.getWorld().getEntitiesByType(type, this.getTestBox(), Entity::isAlive);
    }

    public void expectEntityAt(Entity entity, int x, int y, int z) {
        this.expectEntityAt(entity, new BlockPos(x, y, z));
    }

    public void expectEntityAt(Entity entity, BlockPos pos) {
        BlockPos lv = this.getAbsolutePos(pos);
        List<Entity> list = this.getWorld().getEntitiesByType(entity.getType(), new Box(lv), Entity::isAlive);
        list.stream().filter(e -> e == entity).findFirst().orElseThrow(() -> new PositionedException("Expected " + entity.getType().getUntranslatedName(), lv, pos, this.test.getTick()));
    }

    public void expectItemsAt(Item item, BlockPos pos, double radius, int amount) {
        BlockPos lv = this.getAbsolutePos(pos);
        List<ItemEntity> list = this.getWorld().getEntitiesByType(EntityType.ITEM, new Box(lv).expand(radius), Entity::isAlive);
        int j = 0;
        for (ItemEntity lv2 : list) {
            ItemStack lv3 = lv2.getStack();
            if (!lv3.isOf(item)) continue;
            j += lv3.getCount();
        }
        if (j != amount) {
            throw new PositionedException("Expected " + amount + " " + item.getName().getString() + " items to exist (found " + j + ")", lv, pos, this.test.getTick());
        }
    }

    public void expectItemAt(Item item, BlockPos pos, double radius) {
        BlockPos lv = this.getAbsolutePos(pos);
        List<ItemEntity> list = this.getWorld().getEntitiesByType(EntityType.ITEM, new Box(lv).expand(radius), Entity::isAlive);
        for (Entity entity : list) {
            ItemEntity lv3 = (ItemEntity)entity;
            if (!lv3.getStack().getItem().equals(item)) continue;
            return;
        }
        throw new PositionedException("Expected " + item.getName().getString() + " item", lv, pos, this.test.getTick());
    }

    public void dontExpectItemAt(Item item, BlockPos pos, double radius) {
        BlockPos lv = this.getAbsolutePos(pos);
        List<ItemEntity> list = this.getWorld().getEntitiesByType(EntityType.ITEM, new Box(lv).expand(radius), Entity::isAlive);
        for (Entity entity : list) {
            ItemEntity lv3 = (ItemEntity)entity;
            if (!lv3.getStack().getItem().equals(item)) continue;
            throw new PositionedException("Did not expect " + item.getName().getString() + " item", lv, pos, this.test.getTick());
        }
    }

    public void expectItem(Item item) {
        List<ItemEntity> list = this.getWorld().getEntitiesByType(EntityType.ITEM, this.getTestBox(), Entity::isAlive);
        for (Entity entity : list) {
            ItemEntity lv2 = (ItemEntity)entity;
            if (!lv2.getStack().getItem().equals(item)) continue;
            return;
        }
        throw new GameTestException("Expected " + item.getName().getString() + " item");
    }

    public void dontExpectItem(Item item) {
        List<ItemEntity> list = this.getWorld().getEntitiesByType(EntityType.ITEM, this.getTestBox(), Entity::isAlive);
        for (Entity entity : list) {
            ItemEntity lv2 = (ItemEntity)entity;
            if (!lv2.getStack().getItem().equals(item)) continue;
            throw new GameTestException("Did not expect " + item.getName().getString() + " item");
        }
    }

    public void dontExpectEntity(EntityType<?> type) {
        List<Entity> list = this.getWorld().getEntitiesByType(type, this.getTestBox(), Entity::isAlive);
        if (!list.isEmpty()) {
            throw new GameTestException("Did not expect " + type.getUntranslatedName() + " to exist");
        }
    }

    public void dontExpectEntityAt(EntityType<?> type, int x, int y, int z) {
        this.dontExpectEntityAt(type, new BlockPos(x, y, z));
    }

    public void dontExpectEntityAt(EntityType<?> type, BlockPos pos) {
        BlockPos lv = this.getAbsolutePos(pos);
        List<Entity> list = this.getWorld().getEntitiesByType(type, new Box(lv), Entity::isAlive);
        if (!list.isEmpty()) {
            throw new PositionedException("Did not expect " + type.getUntranslatedName(), lv, pos, this.test.getTick());
        }
    }

    public void dontExpectEntityBetween(EntityType<?> type, Vec3d pos1, Vec3d pos2) {
        List<Entity> list = this.getWorld().getEntitiesByType(type, new Box(pos1, pos2), Entity::isAlive);
        if (!list.isEmpty()) {
            throw new PositionedException("Did not expect " + type.getUntranslatedName() + " between ", BlockPos.ofFloored(pos1), BlockPos.ofFloored(pos2), this.test.getTick());
        }
    }

    public void expectEntityToTouch(EntityType<?> type, double x, double y, double z) {
        Vec3d lv = new Vec3d(x, y, z);
        Vec3d lv2 = this.getAbsolute(lv);
        Predicate<Entity> predicate = entity -> entity.getBoundingBox().intersects(lv2, lv2);
        List<Entity> list = this.getWorld().getEntitiesByType(type, this.getTestBox(), predicate);
        if (list.isEmpty()) {
            throw new GameTestException("Expected " + type.getUntranslatedName() + " to touch " + String.valueOf(lv2) + " (relative " + String.valueOf(lv) + ")");
        }
    }

    public void dontExpectEntityToTouch(EntityType<?> type, double x, double y, double z) {
        Vec3d lv = new Vec3d(x, y, z);
        Vec3d lv2 = this.getAbsolute(lv);
        Predicate<Entity> predicate = entity -> !entity.getBoundingBox().intersects(lv2, lv2);
        List<Entity> list = this.getWorld().getEntitiesByType(type, this.getTestBox(), predicate);
        if (list.isEmpty()) {
            throw new GameTestException("Did not expect " + type.getUntranslatedName() + " to touch " + String.valueOf(lv2) + " (relative " + String.valueOf(lv) + ")");
        }
    }

    public <E extends Entity, T> void expectEntityWithData(BlockPos pos, EntityType<E> type, Function<? super E, T> entityDataGetter, @Nullable T data) {
        BlockPos lv = this.getAbsolutePos(pos);
        List<Entity> list = this.getWorld().getEntitiesByType(type, new Box(lv), Entity::isAlive);
        if (list.isEmpty()) {
            throw new PositionedException("Expected " + type.getUntranslatedName(), lv, pos, this.test.getTick());
        }
        for (Entity lv2 : list) {
            T object2 = entityDataGetter.apply(lv2);
            if (!(object2 == null ? data != null : !object2.equals(data))) continue;
            throw new GameTestException("Expected entity data to be: " + String.valueOf(data) + ", but was: " + String.valueOf(object2));
        }
    }

    public <E extends LivingEntity> void expectEntityHoldingItem(BlockPos pos, EntityType<E> entityType, Item item) {
        BlockPos lv = this.getAbsolutePos(pos);
        List<LivingEntity> list = this.getWorld().getEntitiesByType(entityType, new Box(lv), Entity::isAlive);
        if (list.isEmpty()) {
            throw new PositionedException("Expected entity of type: " + String.valueOf(entityType), lv, pos, this.getTick());
        }
        for (LivingEntity lv2 : list) {
            if (!lv2.isHolding(item)) continue;
            return;
        }
        throw new PositionedException("Entity should be holding: " + String.valueOf(item), lv, pos, this.getTick());
    }

    public <E extends Entity> void expectEntityWithItem(BlockPos pos, EntityType<E> entityType, Item item) {
        BlockPos lv = this.getAbsolutePos(pos);
        List<Entity> list = this.getWorld().getEntitiesByType(entityType, new Box(lv), entity -> ((Entity)entity).isAlive());
        if (list.isEmpty()) {
            throw new PositionedException("Expected " + entityType.getUntranslatedName() + " to exist", lv, pos, this.getTick());
        }
        for (Entity lv2 : list) {
            if (!((InventoryOwner)((Object)lv2)).getInventory().containsAny(stack -> stack.isOf(item))) continue;
            return;
        }
        throw new PositionedException("Entity inventory should contain: " + String.valueOf(item), lv, pos, this.getTick());
    }

    public void expectEmptyContainer(BlockPos pos) {
        BlockPos lv = this.getAbsolutePos(pos);
        BlockEntity lv2 = this.getWorld().getBlockEntity(lv);
        if (lv2 instanceof LockableContainerBlockEntity && !((LockableContainerBlockEntity)lv2).isEmpty()) {
            throw new GameTestException("Container should be empty");
        }
    }

    public void expectContainerWith(BlockPos pos, Item item) {
        BlockPos lv = this.getAbsolutePos(pos);
        BlockEntity lv2 = this.getWorld().getBlockEntity(lv);
        if (!(lv2 instanceof LockableContainerBlockEntity)) {
            throw new GameTestException("Expected a container at " + String.valueOf(pos) + ", found " + String.valueOf(Registries.BLOCK_ENTITY_TYPE.getId(lv2.getType())));
        }
        if (((LockableContainerBlockEntity)lv2).count(item) != 1) {
            throw new GameTestException("Container should contain: " + String.valueOf(item));
        }
    }

    public void expectSameStates(BlockBox checkedBlockBox, BlockPos correctStatePos) {
        BlockPos.stream(checkedBlockBox).forEach(checkedPos -> {
            BlockPos lv = correctStatePos.add(checkedPos.getX() - checkedBlockBox.getMinX(), checkedPos.getY() - checkedBlockBox.getMinY(), checkedPos.getZ() - checkedBlockBox.getMinZ());
            this.expectSameStates((BlockPos)checkedPos, lv);
        });
    }

    public void expectSameStates(BlockPos checkedPos, BlockPos correctStatePos) {
        BlockState lv2;
        BlockState lv = this.getBlockState(checkedPos);
        if (lv != (lv2 = this.getBlockState(correctStatePos))) {
            this.throwPositionedException("Incorrect state. Expected " + String.valueOf(lv2) + ", got " + String.valueOf(lv), checkedPos);
        }
    }

    public void expectContainerWith(long delay, BlockPos pos, Item item) {
        this.runAtTick(delay, () -> this.expectContainerWith(pos, item));
    }

    public void expectEmptyContainer(long delay, BlockPos pos) {
        this.runAtTick(delay, () -> this.expectEmptyContainer(pos));
    }

    public <E extends Entity, T> void expectEntityWithDataEnd(BlockPos pos, EntityType<E> type, Function<E, T> entityDataGetter, T data) {
        this.addInstantFinalTask(() -> this.expectEntityWithData(pos, type, entityDataGetter, data));
    }

    public void expectEntityIn(Entity entity, Box box, String message) {
        if (!box.contains(this.getRelative(entity.getPos()))) {
            this.throwGameTestException(message);
        }
    }

    public <E extends Entity> void testEntity(E entity, Predicate<E> predicate, String testName) {
        if (!predicate.test(entity)) {
            throw new GameTestException("Entity " + String.valueOf(entity) + " failed " + testName + " test");
        }
    }

    public <E extends Entity, T> void testEntityProperty(E entity, Function<E, T> propertyGetter, String propertyName, T expectedValue) {
        T object2 = propertyGetter.apply(entity);
        if (!object2.equals(expectedValue)) {
            throw new GameTestException("Entity " + String.valueOf(entity) + " value " + propertyName + "=" + String.valueOf(object2) + " is not equal to expected " + String.valueOf(expectedValue));
        }
    }

    public void expectEntityHasEffect(LivingEntity entity, RegistryEntry<StatusEffect> effect, int amplifier) {
        StatusEffectInstance lv = entity.getStatusEffect(effect);
        if (lv == null || lv.getAmplifier() != amplifier) {
            int j = amplifier + 1;
            throw new GameTestException("Entity " + String.valueOf(entity) + " failed has " + effect.value().getTranslationKey() + " x " + j + " test");
        }
    }

    public void expectEntityAtEnd(EntityType<?> type, int x, int y, int z) {
        this.expectEntityAtEnd(type, new BlockPos(x, y, z));
    }

    public void expectEntityAtEnd(EntityType<?> type, BlockPos pos) {
        this.addInstantFinalTask(() -> this.expectEntityAt(type, pos));
    }

    public void dontExpectEntityAtEnd(EntityType<?> type, int x, int y, int z) {
        this.dontExpectEntityAtEnd(type, new BlockPos(x, y, z));
    }

    public void dontExpectEntityAtEnd(EntityType<?> type, BlockPos pos) {
        this.addInstantFinalTask(() -> this.dontExpectEntityAt(type, pos));
    }

    public void complete() {
        this.test.completeIfSuccessful();
    }

    private void markFinalCause() {
        if (this.hasFinalClause) {
            throw new IllegalStateException("This test already has final clause");
        }
        this.hasFinalClause = true;
    }

    public void addFinalTask(Runnable runnable) {
        this.markFinalCause();
        this.test.createTimedTaskRunner().createAndAdd(0L, runnable).completeIfSuccessful();
    }

    public void addInstantFinalTask(Runnable runnable) {
        this.markFinalCause();
        this.test.createTimedTaskRunner().createAndAdd(runnable).completeIfSuccessful();
    }

    public void addFinalTaskWithDuration(int duration, Runnable runnable) {
        this.markFinalCause();
        this.test.createTimedTaskRunner().createAndAdd(duration, runnable).completeIfSuccessful();
    }

    public void runAtTick(long tick, Runnable runnable) {
        this.test.runAtTick(tick, runnable);
    }

    public void waitAndRun(long ticks, Runnable runnable) {
        this.runAtTick(this.test.getTick() + ticks, runnable);
    }

    public void forceRandomTick(BlockPos pos) {
        BlockPos lv = this.getAbsolutePos(pos);
        ServerWorld lv2 = this.getWorld();
        lv2.getBlockState(lv).randomTick(lv2, lv, lv2.random);
    }

    public void forceTickIceAndSnow(BlockPos pos) {
        BlockPos lv = this.getAbsolutePos(pos);
        ServerWorld lv2 = this.getWorld();
        lv2.tickIceAndSnow(lv);
    }

    public void forceTickIceAndSnow() {
        Box lv = this.getRelativeTestBox();
        int i = (int)Math.floor(lv.maxX);
        int j = (int)Math.floor(lv.maxZ);
        int k = (int)Math.floor(lv.maxY);
        for (int l = (int)Math.floor(lv.minX); l < i; ++l) {
            for (int m = (int)Math.floor(lv.minZ); m < j; ++m) {
                this.forceTickIceAndSnow(new BlockPos(l, k, m));
            }
        }
    }

    public int getRelativeTopY(Heightmap.Type heightmap, int x, int z) {
        BlockPos lv = this.getAbsolutePos(new BlockPos(x, 0, z));
        return this.getRelativePos(this.getWorld().getTopPosition(heightmap, lv)).getY();
    }

    public void throwPositionedException(String message, BlockPos pos) {
        throw new PositionedException(message, this.getAbsolutePos(pos), pos, this.getTick());
    }

    public void throwPositionedException(String message, Entity entity) {
        throw new PositionedException(message, entity.getBlockPos(), this.getRelativePos(entity.getBlockPos()), this.getTick());
    }

    public void throwGameTestException(String message) {
        throw new GameTestException(message);
    }

    public void addTask(Runnable task) {
        this.test.createTimedTaskRunner().createAndAdd(task).fail(() -> new GameTestException("Fail conditions met"));
    }

    public void runAtEveryTick(Runnable task) {
        LongStream.range(this.test.getTick(), this.test.getTickLimit()).forEach(tick -> this.test.runAtTick(tick, task::run));
    }

    public TimedTaskRunner createTimedTaskRunner() {
        return this.test.createTimedTaskRunner();
    }

    public BlockPos getAbsolutePos(BlockPos pos) {
        BlockPos lv = this.test.getPos();
        BlockPos lv2 = lv.add(pos);
        return StructureTemplate.transformAround(lv2, BlockMirror.NONE, this.test.getRotation(), lv);
    }

    public BlockPos getRelativePos(BlockPos pos) {
        BlockPos lv = this.test.getPos();
        BlockRotation lv2 = this.test.getRotation().rotate(BlockRotation.CLOCKWISE_180);
        BlockPos lv3 = StructureTemplate.transformAround(pos, BlockMirror.NONE, lv2, lv);
        return lv3.subtract(lv);
    }

    public Vec3d getAbsolute(Vec3d pos) {
        Vec3d lv = Vec3d.of(this.test.getPos());
        return StructureTemplate.transformAround(lv.add(pos), BlockMirror.NONE, this.test.getRotation(), this.test.getPos());
    }

    public Vec3d getRelative(Vec3d pos) {
        Vec3d lv = Vec3d.of(this.test.getPos());
        return StructureTemplate.transformAround(pos.subtract(lv), BlockMirror.NONE, this.test.getRotation(), this.test.getPos());
    }

    public BlockRotation getRotation() {
        return this.test.getRotation();
    }

    public void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new GameTestException(message);
        }
    }

    public <N> void assertEquals(N value, N expected, String name) {
        if (!value.equals(expected)) {
            throw new GameTestException("Expected " + name + " to be " + String.valueOf(expected) + ", but was " + String.valueOf(value));
        }
    }

    public void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new GameTestException(message);
        }
    }

    public long getTick() {
        return this.test.getTick();
    }

    public Box getTestBox() {
        return this.test.getBoundingBox();
    }

    private Box getRelativeTestBox() {
        Box lv = this.test.getBoundingBox();
        return lv.offset(BlockPos.ORIGIN.subtract(this.getAbsolutePos(BlockPos.ORIGIN)));
    }

    public void forEachRelativePos(Consumer<BlockPos> posConsumer) {
        Box lv = this.getRelativeTestBox().shrink(1.0, 1.0, 1.0);
        BlockPos.Mutable.stream(lv).forEach(posConsumer);
    }

    public void forEachRemainingTick(Runnable runnable) {
        LongStream.range(this.test.getTick(), this.test.getTickLimit()).forEach(tick -> this.test.runAtTick(tick, runnable::run));
    }

    public void useStackOnBlock(PlayerEntity player, ItemStack stack, BlockPos pos, Direction direction) {
        BlockPos lv = this.getAbsolutePos(pos.offset(direction));
        BlockHitResult lv2 = new BlockHitResult(Vec3d.ofCenter(lv), direction, lv, false);
        ItemUsageContext lv3 = new ItemUsageContext(player, Hand.MAIN_HAND, lv2);
        stack.useOnBlock(lv3);
    }

    public void setBiome(RegistryKey<Biome> biome) {
        Box lv = this.getTestBox();
        BlockPos lv2 = BlockPos.ofFloored(lv.minX, lv.minY, lv.minZ);
        BlockPos lv3 = BlockPos.ofFloored(lv.maxX, lv.maxY, lv.maxZ);
        Either<Integer, CommandSyntaxException> either = FillBiomeCommand.fillBiome(this.getWorld(), lv2, lv3, this.getWorld().getRegistryManager().get(RegistryKeys.BIOME).entryOf(biome));
        if (either.right().isPresent()) {
            this.throwGameTestException("Failed to set biome for test");
        }
    }
}

