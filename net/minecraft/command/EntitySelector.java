/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.predicate.NumberRange;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.Util;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class EntitySelector {
    public static final int MAX_VALUE = Integer.MAX_VALUE;
    public static final BiConsumer<Vec3d, List<? extends Entity>> ARBITRARY = (pos, entities) -> {};
    private static final TypeFilter<Entity, ?> PASSTHROUGH_FILTER = new TypeFilter<Entity, Entity>(){

        @Override
        public Entity downcast(Entity arg) {
            return arg;
        }

        @Override
        public Class<? extends Entity> getBaseClass() {
            return Entity.class;
        }
    };
    private final int limit;
    private final boolean includesNonPlayers;
    private final boolean localWorldOnly;
    private final List<Predicate<Entity>> predicates;
    private final NumberRange.DoubleRange distance;
    private final Function<Vec3d, Vec3d> positionOffset;
    @Nullable
    private final Box box;
    private final BiConsumer<Vec3d, List<? extends Entity>> sorter;
    private final boolean senderOnly;
    @Nullable
    private final String playerName;
    @Nullable
    private final UUID uuid;
    private final TypeFilter<Entity, ?> entityFilter;
    private final boolean usesAt;

    public EntitySelector(int count, boolean includesNonPlayers, boolean localWorldOnly, List<Predicate<Entity>> predicates, NumberRange.DoubleRange distance, Function<Vec3d, Vec3d> positionOffset, @Nullable Box box, BiConsumer<Vec3d, List<? extends Entity>> sorter, boolean senderOnly, @Nullable String playerName, @Nullable UUID uuid, @Nullable EntityType<?> type, boolean usesAt) {
        this.limit = count;
        this.includesNonPlayers = includesNonPlayers;
        this.localWorldOnly = localWorldOnly;
        this.predicates = predicates;
        this.distance = distance;
        this.positionOffset = positionOffset;
        this.box = box;
        this.sorter = sorter;
        this.senderOnly = senderOnly;
        this.playerName = playerName;
        this.uuid = uuid;
        this.entityFilter = type == null ? PASSTHROUGH_FILTER : type;
        this.usesAt = usesAt;
    }

    public int getLimit() {
        return this.limit;
    }

    public boolean includesNonPlayers() {
        return this.includesNonPlayers;
    }

    public boolean isSenderOnly() {
        return this.senderOnly;
    }

    public boolean isLocalWorldOnly() {
        return this.localWorldOnly;
    }

    public boolean usesAt() {
        return this.usesAt;
    }

    private void checkSourcePermission(ServerCommandSource source) throws CommandSyntaxException {
        if (this.usesAt && !source.hasPermissionLevel(2)) {
            throw EntityArgumentType.NOT_ALLOWED_EXCEPTION.create();
        }
    }

    public Entity getEntity(ServerCommandSource source) throws CommandSyntaxException {
        this.checkSourcePermission(source);
        List<? extends Entity> list = this.getEntities(source);
        if (list.isEmpty()) {
            throw EntityArgumentType.ENTITY_NOT_FOUND_EXCEPTION.create();
        }
        if (list.size() > 1) {
            throw EntityArgumentType.TOO_MANY_ENTITIES_EXCEPTION.create();
        }
        return list.get(0);
    }

    public List<? extends Entity> getEntities(ServerCommandSource source) throws CommandSyntaxException {
        this.checkSourcePermission(source);
        if (!this.includesNonPlayers) {
            return this.getPlayers(source);
        }
        if (this.playerName != null) {
            ServerPlayerEntity lv = source.getServer().getPlayerManager().getPlayer(this.playerName);
            if (lv == null) {
                return List.of();
            }
            return List.of(lv);
        }
        if (this.uuid != null) {
            for (ServerWorld lv2 : source.getServer().getWorlds()) {
                Entity lv3 = lv2.getEntity(this.uuid);
                if (lv3 == null) continue;
                if (!lv3.getType().isEnabled(source.getEnabledFeatures())) break;
                return List.of(lv3);
            }
            return List.of();
        }
        Vec3d lv4 = this.positionOffset.apply(source.getPosition());
        Box lv5 = this.getOffsetBox(lv4);
        if (this.senderOnly) {
            Predicate<Entity> predicate = this.getPositionPredicate(lv4, lv5, null);
            if (source.getEntity() != null && predicate.test(source.getEntity())) {
                return List.of(source.getEntity());
            }
            return List.of();
        }
        Predicate<Entity> predicate = this.getPositionPredicate(lv4, lv5, source.getEnabledFeatures());
        ObjectArrayList<Entity> list = new ObjectArrayList<Entity>();
        if (this.isLocalWorldOnly()) {
            this.appendEntitiesFromWorld(list, source.getWorld(), lv5, predicate);
        } else {
            for (ServerWorld lv6 : source.getServer().getWorlds()) {
                this.appendEntitiesFromWorld(list, lv6, lv5, predicate);
            }
        }
        return this.getEntities(lv4, list);
    }

    private void appendEntitiesFromWorld(List<Entity> entities, ServerWorld world, @Nullable Box box, Predicate<Entity> predicate) {
        int i = this.getAppendLimit();
        if (entities.size() >= i) {
            return;
        }
        if (box != null) {
            world.collectEntitiesByType(this.entityFilter, box, predicate, entities, i);
        } else {
            world.collectEntitiesByType(this.entityFilter, predicate, entities, i);
        }
    }

    private int getAppendLimit() {
        return this.sorter == ARBITRARY ? this.limit : Integer.MAX_VALUE;
    }

    public ServerPlayerEntity getPlayer(ServerCommandSource source) throws CommandSyntaxException {
        this.checkSourcePermission(source);
        List<ServerPlayerEntity> list = this.getPlayers(source);
        if (list.size() != 1) {
            throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
        }
        return list.get(0);
    }

    public List<ServerPlayerEntity> getPlayers(ServerCommandSource source) throws CommandSyntaxException {
        List<Object> list;
        this.checkSourcePermission(source);
        if (this.playerName != null) {
            ServerPlayerEntity lv = source.getServer().getPlayerManager().getPlayer(this.playerName);
            if (lv == null) {
                return List.of();
            }
            return List.of(lv);
        }
        if (this.uuid != null) {
            ServerPlayerEntity lv = source.getServer().getPlayerManager().getPlayer(this.uuid);
            if (lv == null) {
                return List.of();
            }
            return List.of(lv);
        }
        Vec3d lv2 = this.positionOffset.apply(source.getPosition());
        Box lv3 = this.getOffsetBox(lv2);
        Predicate<Entity> predicate = this.getPositionPredicate(lv2, lv3, null);
        if (this.senderOnly) {
            ServerPlayerEntity lv4;
            Entity entity = source.getEntity();
            if (entity instanceof ServerPlayerEntity && predicate.test(lv4 = (ServerPlayerEntity)entity)) {
                return List.of(lv4);
            }
            return List.of();
        }
        int i = this.getAppendLimit();
        if (this.isLocalWorldOnly()) {
            list = source.getWorld().getPlayers(predicate, i);
        } else {
            list = new ObjectArrayList();
            for (ServerPlayerEntity lv5 : source.getServer().getPlayerManager().getPlayerList()) {
                if (!predicate.test(lv5)) continue;
                list.add(lv5);
                if (list.size() < i) continue;
                return list;
            }
        }
        return this.getEntities(lv2, list);
    }

    @Nullable
    private Box getOffsetBox(Vec3d offset) {
        return this.box != null ? this.box.offset(offset) : null;
    }

    private Predicate<Entity> getPositionPredicate(Vec3d pos, @Nullable Box box, @Nullable FeatureSet enabledFeatures) {
        List<Predicate<Entity>> list;
        boolean bl3;
        boolean bl2;
        boolean bl = enabledFeatures != null;
        int i = (bl ? 1 : 0) + ((bl2 = box != null) ? 1 : 0) + ((bl3 = !this.distance.isDummy()) ? 1 : 0);
        if (i == 0) {
            list = this.predicates;
        } else {
            ObjectArrayList<Predicate<Entity>> list2 = new ObjectArrayList<Predicate<Entity>>(this.predicates.size() + i);
            list2.addAll(this.predicates);
            if (bl) {
                list2.add(entity -> entity.getType().isEnabled(enabledFeatures));
            }
            if (bl2) {
                list2.add(entity -> box.intersects(entity.getBoundingBox()));
            }
            if (bl3) {
                list2.add(entity -> this.distance.testSqrt(entity.squaredDistanceTo(pos)));
            }
            list = list2;
        }
        return Util.allOf(list);
    }

    private <T extends Entity> List<T> getEntities(Vec3d pos, List<T> entities) {
        if (entities.size() > 1) {
            this.sorter.accept(pos, entities);
        }
        return entities.subList(0, Math.min(this.limit, entities.size()));
    }

    public static Text getNames(List<? extends Entity> entities) {
        return Texts.join(entities, Entity::getDisplayName);
    }
}

