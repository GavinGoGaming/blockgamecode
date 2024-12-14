/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.explosion;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.EntityExplosionBehavior;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

public class Explosion {
    private static final ExplosionBehavior DEFAULT_BEHAVIOR = new ExplosionBehavior();
    private static final int field_30960 = 16;
    private final boolean createFire;
    private final DestructionType destructionType;
    private final Random random = Random.create();
    private final World world;
    private final double x;
    private final double y;
    private final double z;
    @Nullable
    private final Entity entity;
    private final float power;
    private final DamageSource damageSource;
    private final ExplosionBehavior behavior;
    private final ParticleEffect particle;
    private final ParticleEffect emitterParticle;
    private final RegistryEntry<SoundEvent> soundEvent;
    private final ObjectArrayList<BlockPos> affectedBlocks = new ObjectArrayList();
    private final Map<PlayerEntity, Vec3d> affectedPlayers = Maps.newHashMap();

    public static DamageSource createDamageSource(World world, @Nullable Entity source) {
        return world.getDamageSources().explosion(source, Explosion.getCausingEntity(source));
    }

    public Explosion(World world, @Nullable Entity entity, double x, double y, double z, float power, List<BlockPos> affectedBlocks, DestructionType destructionType, ParticleEffect particle, ParticleEffect emitterParticle, RegistryEntry<SoundEvent> soundEvent) {
        this(world, entity, Explosion.createDamageSource(world, entity), null, x, y, z, power, false, destructionType, particle, emitterParticle, soundEvent);
        this.affectedBlocks.addAll((Collection<BlockPos>)affectedBlocks);
    }

    public Explosion(World world, @Nullable Entity entity, double x, double y, double z, float power, boolean createFire, DestructionType destructionType, List<BlockPos> affectedBlocks) {
        this(world, entity, x, y, z, power, createFire, destructionType);
        this.affectedBlocks.addAll((Collection<BlockPos>)affectedBlocks);
    }

    public Explosion(World world, @Nullable Entity entity, double x, double y, double z, float power, boolean createFire, DestructionType destructionType) {
        this(world, entity, Explosion.createDamageSource(world, entity), null, x, y, z, power, createFire, destructionType, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.ENTITY_GENERIC_EXPLODE);
    }

    public Explosion(World world, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, DestructionType destructionType, ParticleEffect particle, ParticleEffect emitterParticle, RegistryEntry<SoundEvent> soundEvent) {
        this.world = world;
        this.entity = entity;
        this.power = power;
        this.x = x;
        this.y = y;
        this.z = z;
        this.createFire = createFire;
        this.destructionType = destructionType;
        this.damageSource = damageSource == null ? world.getDamageSources().explosion(this) : damageSource;
        this.behavior = behavior == null ? this.chooseBehavior(entity) : behavior;
        this.particle = particle;
        this.emitterParticle = emitterParticle;
        this.soundEvent = soundEvent;
    }

    private ExplosionBehavior chooseBehavior(@Nullable Entity entity) {
        return entity == null ? DEFAULT_BEHAVIOR : new EntityExplosionBehavior(entity);
    }

    public static float getExposure(Vec3d source, Entity entity) {
        Box lv = entity.getBoundingBox();
        double d = 1.0 / ((lv.maxX - lv.minX) * 2.0 + 1.0);
        double e = 1.0 / ((lv.maxY - lv.minY) * 2.0 + 1.0);
        double f = 1.0 / ((lv.maxZ - lv.minZ) * 2.0 + 1.0);
        double g = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
        double h = (1.0 - Math.floor(1.0 / f) * f) / 2.0;
        if (d < 0.0 || e < 0.0 || f < 0.0) {
            return 0.0f;
        }
        int i = 0;
        int j = 0;
        for (double k = 0.0; k <= 1.0; k += d) {
            for (double l = 0.0; l <= 1.0; l += e) {
                for (double m = 0.0; m <= 1.0; m += f) {
                    double n = MathHelper.lerp(k, lv.minX, lv.maxX);
                    double o = MathHelper.lerp(l, lv.minY, lv.maxY);
                    double p = MathHelper.lerp(m, lv.minZ, lv.maxZ);
                    Vec3d lv2 = new Vec3d(n + g, o, p + h);
                    if (entity.getWorld().raycast(new RaycastContext(lv2, source, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity)).getType() == HitResult.Type.MISS) {
                        ++i;
                    }
                    ++j;
                }
            }
        }
        return (float)i / (float)j;
    }

    public float getPower() {
        return this.power;
    }

    public Vec3d getPosition() {
        return new Vec3d(this.x, this.y, this.z);
    }

    public void collectBlocksAndDamageEntities() {
        int l;
        int k;
        this.world.emitGameEvent(this.entity, GameEvent.EXPLODE, new Vec3d(this.x, this.y, this.z));
        HashSet<BlockPos> set = Sets.newHashSet();
        int i = 16;
        for (int j = 0; j < 16; ++j) {
            for (k = 0; k < 16; ++k) {
                block2: for (l = 0; l < 16; ++l) {
                    if (j != 0 && j != 15 && k != 0 && k != 15 && l != 0 && l != 15) continue;
                    double d = (float)j / 15.0f * 2.0f - 1.0f;
                    double e = (float)k / 15.0f * 2.0f - 1.0f;
                    double f = (float)l / 15.0f * 2.0f - 1.0f;
                    double g = Math.sqrt(d * d + e * e + f * f);
                    d /= g;
                    e /= g;
                    f /= g;
                    double m = this.x;
                    double n = this.y;
                    double o = this.z;
                    float p = 0.3f;
                    for (float h = this.power * (0.7f + this.world.random.nextFloat() * 0.6f); h > 0.0f; h -= 0.22500001f) {
                        BlockPos lv = BlockPos.ofFloored(m, n, o);
                        BlockState lv2 = this.world.getBlockState(lv);
                        FluidState lv3 = this.world.getFluidState(lv);
                        if (!this.world.isInBuildLimit(lv)) continue block2;
                        Optional<Float> optional = this.behavior.getBlastResistance(this, this.world, lv, lv2, lv3);
                        if (optional.isPresent()) {
                            h -= (optional.get().floatValue() + 0.3f) * 0.3f;
                        }
                        if (h > 0.0f && this.behavior.canDestroyBlock(this, this.world, lv, lv2, h)) {
                            set.add(lv);
                        }
                        m += d * (double)0.3f;
                        n += e * (double)0.3f;
                        o += f * (double)0.3f;
                    }
                }
            }
        }
        this.affectedBlocks.addAll((Collection<BlockPos>)set);
        float q = this.power * 2.0f;
        k = MathHelper.floor(this.x - (double)q - 1.0);
        l = MathHelper.floor(this.x + (double)q + 1.0);
        int r = MathHelper.floor(this.y - (double)q - 1.0);
        int s = MathHelper.floor(this.y + (double)q + 1.0);
        int t = MathHelper.floor(this.z - (double)q - 1.0);
        int u = MathHelper.floor(this.z + (double)q + 1.0);
        List<Entity> list = this.world.getOtherEntities(this.entity, new Box(k, r, t, l, s, u));
        Vec3d lv4 = new Vec3d(this.x, this.y, this.z);
        for (Entity lv5 : list) {
            PlayerEntity lv8;
            double ab;
            double y;
            double x;
            double w;
            double z;
            double v;
            if (lv5.isImmuneToExplosion(this) || !((v = Math.sqrt(lv5.squaredDistanceTo(lv4)) / (double)q) <= 1.0) || (z = Math.sqrt((w = lv5.getX() - this.x) * w + (x = (lv5 instanceof TntEntity ? lv5.getY() : lv5.getEyeY()) - this.y) * x + (y = lv5.getZ() - this.z) * y)) == 0.0) continue;
            w /= z;
            x /= z;
            y /= z;
            if (this.behavior.shouldDamage(this, lv5)) {
                lv5.damage(this.damageSource, this.behavior.calculateDamage(this, lv5));
            }
            double aa = (1.0 - v) * (double)Explosion.getExposure(lv4, lv5) * (double)this.behavior.getKnockbackModifier(lv5);
            if (lv5 instanceof LivingEntity) {
                LivingEntity lv6 = (LivingEntity)lv5;
                ab = aa * (1.0 - lv6.getAttributeValue(EntityAttributes.GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE));
            } else {
                ab = aa;
            }
            Vec3d lv7 = new Vec3d(w *= ab, x *= ab, y *= ab);
            lv5.setVelocity(lv5.getVelocity().add(lv7));
            if (!(!(lv5 instanceof PlayerEntity) || (lv8 = (PlayerEntity)lv5).isSpectator() || lv8.isCreative() && lv8.getAbilities().flying)) {
                this.affectedPlayers.put(lv8, lv7);
            }
            lv5.onExplodedBy(this.entity);
        }
    }

    public void affectWorld(boolean particles) {
        if (this.world.isClient) {
            this.world.playSound(this.x, this.y, this.z, this.soundEvent.value(), SoundCategory.BLOCKS, 4.0f, (1.0f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f, false);
        }
        boolean bl2 = this.shouldDestroy();
        if (particles) {
            ParticleEffect lv = this.power < 2.0f || !bl2 ? this.particle : this.emitterParticle;
            this.world.addParticle(lv, this.x, this.y, this.z, 1.0, 0.0, 0.0);
        }
        if (bl2) {
            this.world.getProfiler().push("explosion_blocks");
            ArrayList list = new ArrayList();
            Util.shuffle(this.affectedBlocks, this.world.random);
            for (BlockPos lv2 : this.affectedBlocks) {
                this.world.getBlockState(lv2).onExploded(this.world, lv2, this, (stack, pos) -> Explosion.tryMergeStack(list, stack, pos));
            }
            for (Pair pair : list) {
                Block.dropStack(this.world, (BlockPos)pair.getSecond(), (ItemStack)pair.getFirst());
            }
            this.world.getProfiler().pop();
        }
        if (this.createFire) {
            for (BlockPos lv3 : this.affectedBlocks) {
                if (this.random.nextInt(3) != 0 || !this.world.getBlockState(lv3).isAir() || !this.world.getBlockState(lv3.down()).isOpaqueFullCube(this.world, lv3.down())) continue;
                this.world.setBlockState(lv3, AbstractFireBlock.getState(this.world, lv3));
            }
        }
    }

    private static void tryMergeStack(List<Pair<ItemStack, BlockPos>> stacks, ItemStack stack, BlockPos pos) {
        for (int i = 0; i < stacks.size(); ++i) {
            Pair<ItemStack, BlockPos> pair = stacks.get(i);
            ItemStack lv = pair.getFirst();
            if (!ItemEntity.canMerge(lv, stack)) continue;
            stacks.set(i, Pair.of(ItemEntity.merge(lv, stack, 16), pair.getSecond()));
            if (!stack.isEmpty()) continue;
            return;
        }
        stacks.add(Pair.of(stack, pos));
    }

    public boolean shouldDestroy() {
        return this.destructionType != DestructionType.KEEP;
    }

    public Map<PlayerEntity, Vec3d> getAffectedPlayers() {
        return this.affectedPlayers;
    }

    @Nullable
    private static LivingEntity getCausingEntity(@Nullable Entity from) {
        ProjectileEntity lv3;
        Entity lv4;
        if (from == null) {
            return null;
        }
        if (from instanceof TntEntity) {
            TntEntity lv = (TntEntity)from;
            return lv.getOwner();
        }
        if (from instanceof LivingEntity) {
            LivingEntity lv2 = (LivingEntity)from;
            return lv2;
        }
        if (from instanceof ProjectileEntity && (lv4 = (lv3 = (ProjectileEntity)from).getOwner()) instanceof LivingEntity) {
            LivingEntity lv5 = (LivingEntity)lv4;
            return lv5;
        }
        return null;
    }

    @Nullable
    public LivingEntity getCausingEntity() {
        return Explosion.getCausingEntity(this.entity);
    }

    @Nullable
    public Entity getEntity() {
        return this.entity;
    }

    public void clearAffectedBlocks() {
        this.affectedBlocks.clear();
    }

    public List<BlockPos> getAffectedBlocks() {
        return this.affectedBlocks;
    }

    public DestructionType getDestructionType() {
        return this.destructionType;
    }

    public ParticleEffect getParticle() {
        return this.particle;
    }

    public ParticleEffect getEmitterParticle() {
        return this.emitterParticle;
    }

    public RegistryEntry<SoundEvent> getSoundEvent() {
        return this.soundEvent;
    }

    public boolean canTriggerBlocks() {
        if (this.destructionType != DestructionType.TRIGGER_BLOCK || this.world.isClient()) {
            return false;
        }
        if (this.entity != null && this.entity.getType() == EntityType.BREEZE_WIND_CHARGE) {
            return this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
        }
        return true;
    }

    public static enum DestructionType {
        KEEP,
        DESTROY,
        DESTROY_WITH_DECAY,
        TRIGGER_BLOCK;

    }
}

