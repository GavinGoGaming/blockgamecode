/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CrossbowItem
extends RangedWeaponItem {
    private static final float DEFAULT_PULL_TIME = 1.25f;
    public static final int RANGE = 8;
    private boolean charged = false;
    private boolean loaded = false;
    private static final float CHARGE_PROGRESS = 0.2f;
    private static final float LOAD_PROGRESS = 0.5f;
    private static final float DEFAULT_SPEED = 3.15f;
    private static final float FIREWORK_ROCKET_SPEED = 1.6f;
    public static final float field_49258 = 1.6f;
    private static final LoadingSounds DEFAULT_LOADING_SOUNDS = new LoadingSounds(Optional.of(SoundEvents.ITEM_CROSSBOW_LOADING_START), Optional.of(SoundEvents.ITEM_CROSSBOW_LOADING_MIDDLE), Optional.of(SoundEvents.ITEM_CROSSBOW_LOADING_END));

    public CrossbowItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public Predicate<ItemStack> getHeldProjectiles() {
        return CROSSBOW_HELD_PROJECTILES;
    }

    @Override
    public Predicate<ItemStack> getProjectiles() {
        return BOW_PROJECTILES;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        ChargedProjectilesComponent lv2 = lv.get(DataComponentTypes.CHARGED_PROJECTILES);
        if (lv2 != null && !lv2.isEmpty()) {
            this.shootAll(world, user, hand, lv, CrossbowItem.getSpeed(lv2), 1.0f, null);
            return TypedActionResult.consume(lv);
        }
        if (!user.getProjectileType(lv).isEmpty()) {
            this.charged = false;
            this.loaded = false;
            user.setCurrentHand(hand);
            return TypedActionResult.consume(lv);
        }
        return TypedActionResult.fail(lv);
    }

    private static float getSpeed(ChargedProjectilesComponent stack) {
        if (stack.contains(Items.FIREWORK_ROCKET)) {
            return 1.6f;
        }
        return 3.15f;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        int j = this.getMaxUseTime(stack, user) - remainingUseTicks;
        float f = CrossbowItem.getPullProgress(j, stack, user);
        if (f >= 1.0f && !CrossbowItem.isCharged(stack) && CrossbowItem.loadProjectiles(user, stack)) {
            LoadingSounds lv = this.getLoadingSounds(stack);
            lv.end().ifPresent(sound -> world.playSound(null, user.getX(), user.getY(), user.getZ(), (SoundEvent)sound.value(), user.getSoundCategory(), 1.0f, 1.0f / (world.getRandom().nextFloat() * 0.5f + 1.0f) + 0.2f));
        }
    }

    private static boolean loadProjectiles(LivingEntity shooter, ItemStack crossbow) {
        List<ItemStack> list = CrossbowItem.load(crossbow, shooter.getProjectileType(crossbow), shooter);
        if (!list.isEmpty()) {
            crossbow.set(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.of(list));
            return true;
        }
        return false;
    }

    public static boolean isCharged(ItemStack stack) {
        ChargedProjectilesComponent lv = stack.getOrDefault(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.DEFAULT);
        return !lv.isEmpty();
    }

    @Override
    protected void shoot(LivingEntity shooter, ProjectileEntity projectile, int index, float speed, float divergence, float yaw, @Nullable LivingEntity target) {
        Vector3f vector3f;
        if (target != null) {
            double d = target.getX() - shooter.getX();
            double e = target.getZ() - shooter.getZ();
            double j = Math.sqrt(d * d + e * e);
            double k = target.getBodyY(0.3333333333333333) - projectile.getY() + j * (double)0.2f;
            vector3f = CrossbowItem.calcVelocity(shooter, new Vec3d(d, k, e), yaw);
        } else {
            Vec3d lv = shooter.getOppositeRotationVector(1.0f);
            Quaternionf quaternionf = new Quaternionf().setAngleAxis((double)(yaw * ((float)Math.PI / 180)), lv.x, lv.y, lv.z);
            Vec3d lv2 = shooter.getRotationVec(1.0f);
            vector3f = lv2.toVector3f().rotate(quaternionf);
        }
        projectile.setVelocity(vector3f.x(), vector3f.y(), vector3f.z(), speed, divergence);
        float l = CrossbowItem.getSoundPitch(shooter.getRandom(), index);
        shooter.getWorld().playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.ITEM_CROSSBOW_SHOOT, shooter.getSoundCategory(), 1.0f, l);
    }

    private static Vector3f calcVelocity(LivingEntity shooter, Vec3d direction, float yaw) {
        Vector3f vector3f = direction.toVector3f().normalize();
        Vector3f vector3f2 = new Vector3f(vector3f).cross(new Vector3f(0.0f, 1.0f, 0.0f));
        if ((double)vector3f2.lengthSquared() <= 1.0E-7) {
            Vec3d lv = shooter.getOppositeRotationVector(1.0f);
            vector3f2 = new Vector3f(vector3f).cross(lv.toVector3f());
        }
        Vector3f vector3f3 = new Vector3f(vector3f).rotateAxis(1.5707964f, vector3f2.x, vector3f2.y, vector3f2.z);
        return new Vector3f(vector3f).rotateAxis(yaw * ((float)Math.PI / 180), vector3f3.x, vector3f3.y, vector3f3.z);
    }

    @Override
    protected ProjectileEntity createArrowEntity(World world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical) {
        if (projectileStack.isOf(Items.FIREWORK_ROCKET)) {
            return new FireworkRocketEntity(world, projectileStack, shooter, shooter.getX(), shooter.getEyeY() - (double)0.15f, shooter.getZ(), true);
        }
        ProjectileEntity lv = super.createArrowEntity(world, shooter, weaponStack, projectileStack, critical);
        if (lv instanceof PersistentProjectileEntity) {
            PersistentProjectileEntity lv2 = (PersistentProjectileEntity)lv;
            lv2.setSound(SoundEvents.ITEM_CROSSBOW_HIT);
        }
        return lv;
    }

    @Override
    protected int getWeaponStackDamage(ItemStack projectile) {
        return projectile.isOf(Items.FIREWORK_ROCKET) ? 3 : 1;
    }

    public void shootAll(World world, LivingEntity shooter, Hand hand, ItemStack stack, float speed, float divergence, @Nullable LivingEntity target) {
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld lv = (ServerWorld)world;
        ChargedProjectilesComponent lv2 = stack.set(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.DEFAULT);
        if (lv2 == null || lv2.isEmpty()) {
            return;
        }
        this.shootAll(lv, shooter, hand, stack, lv2.getProjectiles(), speed, divergence, shooter instanceof PlayerEntity, target);
        if (shooter instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv3 = (ServerPlayerEntity)shooter;
            Criteria.SHOT_CROSSBOW.trigger(lv3, stack);
            lv3.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
        }
    }

    private static float getSoundPitch(Random random, int index) {
        if (index == 0) {
            return 1.0f;
        }
        return CrossbowItem.getSoundPitch((index & 1) == 1, random);
    }

    private static float getSoundPitch(boolean flag, Random random) {
        float f = flag ? 0.63f : 0.43f;
        return 1.0f / (random.nextFloat() * 0.5f + 1.8f) + f;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClient) {
            LoadingSounds lv = this.getLoadingSounds(stack);
            float f = (float)(stack.getMaxUseTime(user) - remainingUseTicks) / (float)CrossbowItem.getPullTime(stack, user);
            if (f < 0.2f) {
                this.charged = false;
                this.loaded = false;
            }
            if (f >= 0.2f && !this.charged) {
                this.charged = true;
                lv.start().ifPresent(sound -> world.playSound(null, user.getX(), user.getY(), user.getZ(), (SoundEvent)sound.value(), SoundCategory.PLAYERS, 0.5f, 1.0f));
            }
            if (f >= 0.5f && !this.loaded) {
                this.loaded = true;
                lv.mid().ifPresent(sound -> world.playSound(null, user.getX(), user.getY(), user.getZ(), (SoundEvent)sound.value(), SoundCategory.PLAYERS, 0.5f, 1.0f));
            }
        }
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return CrossbowItem.getPullTime(stack, user) + 3;
    }

    public static int getPullTime(ItemStack stack, LivingEntity user) {
        float f = EnchantmentHelper.getCrossbowChargeTime(stack, user, 1.25f);
        return MathHelper.floor(f * 20.0f);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.CROSSBOW;
    }

    LoadingSounds getLoadingSounds(ItemStack stack) {
        return EnchantmentHelper.getEffect(stack, EnchantmentEffectComponentTypes.CROSSBOW_CHARGING_SOUNDS).orElse(DEFAULT_LOADING_SOUNDS);
    }

    private static float getPullProgress(int useTicks, ItemStack stack, LivingEntity user) {
        float f = (float)useTicks / (float)CrossbowItem.getPullTime(stack, user);
        if (f > 1.0f) {
            f = 1.0f;
        }
        return f;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        ChargedProjectilesComponent lv = stack.get(DataComponentTypes.CHARGED_PROJECTILES);
        if (lv == null || lv.isEmpty()) {
            return;
        }
        ItemStack lv2 = lv.getProjectiles().get(0);
        tooltip.add(Text.translatable("item.minecraft.crossbow.projectile").append(ScreenTexts.SPACE).append(lv2.toHoverableText()));
        if (type.isAdvanced() && lv2.isOf(Items.FIREWORK_ROCKET)) {
            ArrayList<Text> list2 = Lists.newArrayList();
            Items.FIREWORK_ROCKET.appendTooltip(lv2, context, list2, type);
            if (!list2.isEmpty()) {
                for (int i = 0; i < list2.size(); ++i) {
                    list2.set(i, Text.literal("  ").append((Text)list2.get(i)).formatted(Formatting.GRAY));
                }
                tooltip.addAll(list2);
            }
        }
    }

    @Override
    public boolean isUsedOnRelease(ItemStack stack) {
        return stack.isOf(this);
    }

    @Override
    public int getRange() {
        return 8;
    }

    public record LoadingSounds(Optional<RegistryEntry<SoundEvent>> start, Optional<RegistryEntry<SoundEvent>> mid, Optional<RegistryEntry<SoundEvent>> end) {
        public static final Codec<LoadingSounds> CODEC = RecordCodecBuilder.create(instance -> instance.group(SoundEvent.ENTRY_CODEC.optionalFieldOf("start").forGetter(LoadingSounds::start), SoundEvent.ENTRY_CODEC.optionalFieldOf("mid").forGetter(LoadingSounds::mid), SoundEvent.ENTRY_CODEC.optionalFieldOf("end").forGetter(LoadingSounds::end)).apply((Applicative<LoadingSounds, ?>)instance, LoadingSounds::new));
    }
}

