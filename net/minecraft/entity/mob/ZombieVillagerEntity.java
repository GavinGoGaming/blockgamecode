/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.mob;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.UUID;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityInteraction;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ZombieVillagerEntity
extends ZombieEntity
implements VillagerDataContainer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final TrackedData<Boolean> CONVERTING = DataTracker.registerData(ZombieVillagerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<VillagerData> VILLAGER_DATA = DataTracker.registerData(ZombieVillagerEntity.class, TrackedDataHandlerRegistry.VILLAGER_DATA);
    private static final int BASE_CONVERSION_DELAY = 3600;
    private static final int field_30520 = 6000;
    private static final int field_30521 = 14;
    private static final int field_30522 = 4;
    private int conversionTimer;
    @Nullable
    private UUID converter;
    @Nullable
    private NbtElement gossipData;
    @Nullable
    private TradeOfferList offerData;
    private int xp;

    public ZombieVillagerEntity(EntityType<? extends ZombieVillagerEntity> arg, World arg2) {
        super((EntityType<? extends ZombieEntity>)arg, arg2);
        Registries.VILLAGER_PROFESSION.getRandom(this.random).ifPresent(profession -> this.setVillagerData(this.getVillagerData().withProfession((VillagerProfession)profession.value())));
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CONVERTING, false);
        builder.add(VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, this.getVillagerData()).resultOrPartial(LOGGER::error).ifPresent(arg2 -> nbt.put("VillagerData", (NbtElement)arg2));
        if (this.offerData != null) {
            nbt.put("Offers", TradeOfferList.CODEC.encodeStart(this.getRegistryManager().getOps(NbtOps.INSTANCE), this.offerData).getOrThrow());
        }
        if (this.gossipData != null) {
            nbt.put("Gossips", this.gossipData);
        }
        nbt.putInt("ConversionTime", this.isConverting() ? this.conversionTimer : -1);
        if (this.converter != null) {
            nbt.putUuid("ConversionPlayer", this.converter);
        }
        nbt.putInt("Xp", this.xp);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("VillagerData", NbtElement.COMPOUND_TYPE)) {
            DataResult dataResult = VillagerData.CODEC.parse(new Dynamic<NbtElement>(NbtOps.INSTANCE, nbt.get("VillagerData")));
            dataResult.resultOrPartial(LOGGER::error).ifPresent(this::setVillagerData);
        }
        if (nbt.contains("Offers")) {
            TradeOfferList.CODEC.parse(this.getRegistryManager().getOps(NbtOps.INSTANCE), nbt.get("Offers")).resultOrPartial(Util.addPrefix("Failed to load offers: ", LOGGER::warn)).ifPresent(offerData -> {
                this.offerData = offerData;
            });
        }
        if (nbt.contains("Gossips", NbtElement.LIST_TYPE)) {
            this.gossipData = nbt.getList("Gossips", NbtElement.COMPOUND_TYPE);
        }
        if (nbt.contains("ConversionTime", NbtElement.NUMBER_TYPE) && nbt.getInt("ConversionTime") > -1) {
            this.setConverting(nbt.containsUuid("ConversionPlayer") ? nbt.getUuid("ConversionPlayer") : null, nbt.getInt("ConversionTime"));
        }
        if (nbt.contains("Xp", NbtElement.INT_TYPE)) {
            this.xp = nbt.getInt("Xp");
        }
    }

    @Override
    public void tick() {
        if (!this.getWorld().isClient && this.isAlive() && this.isConverting()) {
            int i = this.getConversionRate();
            this.conversionTimer -= i;
            if (this.conversionTimer <= 0) {
                this.finishConversion((ServerWorld)this.getWorld());
            }
        }
        super.tick();
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack lv = player.getStackInHand(hand);
        if (lv.isOf(Items.GOLDEN_APPLE)) {
            if (this.hasStatusEffect(StatusEffects.WEAKNESS)) {
                lv.decrementUnlessCreative(1, player);
                if (!this.getWorld().isClient) {
                    this.setConverting(player.getUuid(), this.random.nextInt(2401) + 3600);
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.CONSUME;
        }
        return super.interactMob(player, hand);
    }

    @Override
    protected boolean canConvertInWater() {
        return false;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return !this.isConverting() && this.xp == 0;
    }

    public boolean isConverting() {
        return this.getDataTracker().get(CONVERTING);
    }

    private void setConverting(@Nullable UUID uuid, int delay) {
        this.converter = uuid;
        this.conversionTimer = delay;
        this.getDataTracker().set(CONVERTING, true);
        this.removeStatusEffect(StatusEffects.WEAKNESS);
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, delay, Math.min(this.getWorld().getDifficulty().getId() - 1, 0)));
        this.getWorld().sendEntityStatus(this, EntityStatuses.PLAY_CURE_ZOMBIE_VILLAGER_SOUND);
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.PLAY_CURE_ZOMBIE_VILLAGER_SOUND) {
            if (!this.isSilent()) {
                this.getWorld().playSound(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, this.getSoundCategory(), 1.0f + this.random.nextFloat(), this.random.nextFloat() * 0.7f + 0.3f, false);
            }
            return;
        }
        super.handleStatus(status);
    }

    private void finishConversion(ServerWorld world) {
        PlayerEntity lv4;
        VillagerEntity lv = this.convertTo(EntityType.VILLAGER, false);
        if (lv == null) {
            return;
        }
        for (EquipmentSlot lv2 : this.dropEquipment(stack -> !EnchantmentHelper.hasAnyEnchantmentsWith(stack, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE))) {
            StackReference lv3 = lv.getStackReference(lv2.getEntitySlotId() + 300);
            lv3.set(this.getEquippedStack(lv2));
        }
        lv.setVillagerData(this.getVillagerData());
        if (this.gossipData != null) {
            lv.readGossipDataNbt(this.gossipData);
        }
        if (this.offerData != null) {
            lv.setOffers(this.offerData.copy());
        }
        lv.setExperience(this.xp);
        lv.initialize(world, world.getLocalDifficulty(lv.getBlockPos()), SpawnReason.CONVERSION, null);
        lv.reinitializeBrain(world);
        if (this.converter != null && (lv4 = world.getPlayerByUuid(this.converter)) instanceof ServerPlayerEntity) {
            Criteria.CURED_ZOMBIE_VILLAGER.trigger((ServerPlayerEntity)lv4, this, lv);
            world.handleInteraction(EntityInteraction.ZOMBIE_VILLAGER_CURED, lv4, lv);
        }
        lv.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0));
        if (!this.isSilent()) {
            world.syncWorldEvent(null, WorldEvents.ZOMBIE_VILLAGER_CURED, this.getBlockPos(), 0);
        }
    }

    private int getConversionRate() {
        int i = 1;
        if (this.random.nextFloat() < 0.01f) {
            int j = 0;
            BlockPos.Mutable lv = new BlockPos.Mutable();
            for (int k = (int)this.getX() - 4; k < (int)this.getX() + 4 && j < 14; ++k) {
                for (int l = (int)this.getY() - 4; l < (int)this.getY() + 4 && j < 14; ++l) {
                    for (int m = (int)this.getZ() - 4; m < (int)this.getZ() + 4 && j < 14; ++m) {
                        BlockState lv2 = this.getWorld().getBlockState(lv.set(k, l, m));
                        if (!lv2.isOf(Blocks.IRON_BARS) && !(lv2.getBlock() instanceof BedBlock)) continue;
                        if (this.random.nextFloat() < 0.3f) {
                            ++i;
                        }
                        ++j;
                    }
                }
            }
        }
        return i;
    }

    @Override
    public float getSoundPitch() {
        if (this.isBaby()) {
            return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 2.0f;
        }
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f;
    }

    @Override
    public SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_ZOMBIE_VILLAGER_AMBIENT;
    }

    @Override
    public SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ZOMBIE_VILLAGER_HURT;
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ZOMBIE_VILLAGER_DEATH;
    }

    @Override
    public SoundEvent getStepSound() {
        return SoundEvents.ENTITY_ZOMBIE_VILLAGER_STEP;
    }

    @Override
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }

    public void setOfferData(TradeOfferList offerData) {
        this.offerData = offerData;
    }

    public void setGossipData(NbtElement gossipData) {
        this.gossipData = gossipData;
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        this.setVillagerData(this.getVillagerData().withType(VillagerType.forBiome(world.getBiome(this.getBlockPos()))));
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    public void setVillagerData(VillagerData villagerData) {
        VillagerData lv = this.getVillagerData();
        if (lv.getProfession() != villagerData.getProfession()) {
            this.offerData = null;
        }
        this.dataTracker.set(VILLAGER_DATA, villagerData);
    }

    @Override
    public VillagerData getVillagerData() {
        return this.dataTracker.get(VILLAGER_DATA);
    }

    public int getXp() {
        return this.xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }
}

