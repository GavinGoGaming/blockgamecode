/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.dispenser;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.CandleCakeBlock;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.TntBlock;
import net.minecraft.block.WitherSkullBlock;
import net.minecraft.block.dispenser.BlockPlacementDispenserBehavior;
import net.minecraft.block.dispenser.BoatDispenserBehavior;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.dispenser.ShearsDispenserBehavior;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.ArmadilloEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potions;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationPropertyHelper;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.slf4j.Logger;

public interface DispenserBehavior {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DispenserBehavior NOOP = (pointer, stack) -> stack;

    public ItemStack dispense(BlockPointer var1, ItemStack var2);

    public static void registerDefaults() {
        DispenserBlock.registerProjectileBehavior(Items.ARROW);
        DispenserBlock.registerProjectileBehavior(Items.TIPPED_ARROW);
        DispenserBlock.registerProjectileBehavior(Items.SPECTRAL_ARROW);
        DispenserBlock.registerProjectileBehavior(Items.EGG);
        DispenserBlock.registerProjectileBehavior(Items.SNOWBALL);
        DispenserBlock.registerProjectileBehavior(Items.EXPERIENCE_BOTTLE);
        DispenserBlock.registerProjectileBehavior(Items.SPLASH_POTION);
        DispenserBlock.registerProjectileBehavior(Items.LINGERING_POTION);
        DispenserBlock.registerProjectileBehavior(Items.FIREWORK_ROCKET);
        DispenserBlock.registerProjectileBehavior(Items.FIRE_CHARGE);
        DispenserBlock.registerProjectileBehavior(Items.WIND_CHARGE);
        ItemDispenserBehavior lv = new ItemDispenserBehavior(){

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                Direction lv = pointer.state().get(DispenserBlock.FACING);
                EntityType<?> lv2 = ((SpawnEggItem)stack.getItem()).getEntityType(stack);
                try {
                    lv2.spawnFromItemStack(pointer.world(), stack, null, pointer.pos().offset(lv), SpawnReason.DISPENSER, lv != Direction.UP, false);
                } catch (Exception exception) {
                    LOGGER.error("Error while dispensing spawn egg from dispenser at {}", (Object)pointer.pos(), (Object)exception);
                    return ItemStack.EMPTY;
                }
                stack.decrement(1);
                pointer.world().emitGameEvent(null, GameEvent.ENTITY_PLACE, pointer.pos());
                return stack;
            }
        };
        for (SpawnEggItem lv2 : SpawnEggItem.getAll()) {
            DispenserBlock.registerBehavior(lv2, lv);
        }
        DispenserBlock.registerBehavior(Items.ARMOR_STAND, new ItemDispenserBehavior(){

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                Consumer<ArmorStandEntity> consumer;
                Direction lv = pointer.state().get(DispenserBlock.FACING);
                BlockPos lv2 = pointer.pos().offset(lv);
                ServerWorld lv3 = pointer.world();
                ArmorStandEntity lv4 = EntityType.ARMOR_STAND.spawn(lv3, consumer = EntityType.copier(armorStand -> armorStand.setYaw(lv.asRotation()), lv3, stack, null), lv2, SpawnReason.DISPENSER, false, false);
                if (lv4 != null) {
                    stack.decrement(1);
                }
                return stack;
            }
        });
        DispenserBlock.registerBehavior(Items.SADDLE, new FallibleItemDispenserBehavior(){

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                BlockPos lv = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
                List<LivingEntity> list = pointer.world().getEntitiesByClass(LivingEntity.class, new Box(lv), entity -> {
                    if (entity instanceof Saddleable) {
                        Saddleable lv = (Saddleable)((Object)entity);
                        return !lv.isSaddled() && lv.canBeSaddled();
                    }
                    return false;
                });
                if (!list.isEmpty()) {
                    ((Saddleable)((Object)list.get(0))).saddle(stack.split(1), SoundCategory.BLOCKS);
                    this.setSuccess(true);
                    return stack;
                }
                return super.dispenseSilently(pointer, stack);
            }
        });
        FallibleItemDispenserBehavior lv3 = new FallibleItemDispenserBehavior(){

            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                BlockPos lv = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
                List<AbstractHorseEntity> list = pointer.world().getEntitiesByClass(AbstractHorseEntity.class, new Box(lv), horse -> horse.isAlive() && horse.canUseSlot(EquipmentSlot.BODY));
                for (AbstractHorseEntity lv2 : list) {
                    if (!lv2.isHorseArmor(stack) || lv2.isWearingBodyArmor() || !lv2.isTame()) continue;
                    lv2.equipBodyArmor(stack.split(1));
                    this.setSuccess(true);
                    return stack;
                }
                return super.dispenseSilently(pointer, stack);
            }
        };
        DispenserBlock.registerBehavior(Items.LEATHER_HORSE_ARMOR, lv3);
        DispenserBlock.registerBehavior(Items.IRON_HORSE_ARMOR, lv3);
        DispenserBlock.registerBehavior(Items.GOLDEN_HORSE_ARMOR, lv3);
        DispenserBlock.registerBehavior(Items.DIAMOND_HORSE_ARMOR, lv3);
        DispenserBlock.registerBehavior(Items.WHITE_CARPET, lv3);
        DispenserBlock.registerBehavior(Items.ORANGE_CARPET, lv3);
        DispenserBlock.registerBehavior(Items.CYAN_CARPET, lv3);
        DispenserBlock.registerBehavior(Items.BLUE_CARPET, lv3);
        DispenserBlock.registerBehavior(Items.BROWN_CARPET, lv3);
        DispenserBlock.registerBehavior(Items.BLACK_CARPET, lv3);
        DispenserBlock.registerBehavior(Items.GRAY_CARPET, lv3);
        DispenserBlock.registerBehavior(Items.GREEN_CARPET, lv3);
        DispenserBlock.registerBehavior(Items.LIGHT_BLUE_CARPET, lv3);
        DispenserBlock.registerBehavior(Items.LIGHT_GRAY_CARPET, lv3);
        DispenserBlock.registerBehavior(Items.LIME_CARPET, lv3);
        DispenserBlock.registerBehavior(Items.MAGENTA_CARPET, lv3);
        DispenserBlock.registerBehavior(Items.PINK_CARPET, lv3);
        DispenserBlock.registerBehavior(Items.PURPLE_CARPET, lv3);
        DispenserBlock.registerBehavior(Items.RED_CARPET, lv3);
        DispenserBlock.registerBehavior(Items.YELLOW_CARPET, lv3);
        DispenserBlock.registerBehavior(Items.CHEST, new FallibleItemDispenserBehavior(){

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                BlockPos lv = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
                List<AbstractDonkeyEntity> list = pointer.world().getEntitiesByClass(AbstractDonkeyEntity.class, new Box(lv), donkey -> donkey.isAlive() && !donkey.hasChest());
                for (AbstractDonkeyEntity lv2 : list) {
                    if (!lv2.isTame() || !lv2.getStackReference(499).set(stack)) continue;
                    stack.decrement(1);
                    this.setSuccess(true);
                    return stack;
                }
                return super.dispenseSilently(pointer, stack);
            }
        });
        DispenserBlock.registerBehavior(Items.OAK_BOAT, new BoatDispenserBehavior(BoatEntity.Type.OAK));
        DispenserBlock.registerBehavior(Items.SPRUCE_BOAT, new BoatDispenserBehavior(BoatEntity.Type.SPRUCE));
        DispenserBlock.registerBehavior(Items.BIRCH_BOAT, new BoatDispenserBehavior(BoatEntity.Type.BIRCH));
        DispenserBlock.registerBehavior(Items.JUNGLE_BOAT, new BoatDispenserBehavior(BoatEntity.Type.JUNGLE));
        DispenserBlock.registerBehavior(Items.DARK_OAK_BOAT, new BoatDispenserBehavior(BoatEntity.Type.DARK_OAK));
        DispenserBlock.registerBehavior(Items.ACACIA_BOAT, new BoatDispenserBehavior(BoatEntity.Type.ACACIA));
        DispenserBlock.registerBehavior(Items.CHERRY_BOAT, new BoatDispenserBehavior(BoatEntity.Type.CHERRY));
        DispenserBlock.registerBehavior(Items.MANGROVE_BOAT, new BoatDispenserBehavior(BoatEntity.Type.MANGROVE));
        DispenserBlock.registerBehavior(Items.BAMBOO_RAFT, new BoatDispenserBehavior(BoatEntity.Type.BAMBOO));
        DispenserBlock.registerBehavior(Items.OAK_CHEST_BOAT, new BoatDispenserBehavior(BoatEntity.Type.OAK, true));
        DispenserBlock.registerBehavior(Items.SPRUCE_CHEST_BOAT, new BoatDispenserBehavior(BoatEntity.Type.SPRUCE, true));
        DispenserBlock.registerBehavior(Items.BIRCH_CHEST_BOAT, new BoatDispenserBehavior(BoatEntity.Type.BIRCH, true));
        DispenserBlock.registerBehavior(Items.JUNGLE_CHEST_BOAT, new BoatDispenserBehavior(BoatEntity.Type.JUNGLE, true));
        DispenserBlock.registerBehavior(Items.DARK_OAK_CHEST_BOAT, new BoatDispenserBehavior(BoatEntity.Type.DARK_OAK, true));
        DispenserBlock.registerBehavior(Items.ACACIA_CHEST_BOAT, new BoatDispenserBehavior(BoatEntity.Type.ACACIA, true));
        DispenserBlock.registerBehavior(Items.CHERRY_CHEST_BOAT, new BoatDispenserBehavior(BoatEntity.Type.CHERRY, true));
        DispenserBlock.registerBehavior(Items.MANGROVE_CHEST_BOAT, new BoatDispenserBehavior(BoatEntity.Type.MANGROVE, true));
        DispenserBlock.registerBehavior(Items.BAMBOO_CHEST_RAFT, new BoatDispenserBehavior(BoatEntity.Type.BAMBOO, true));
        ItemDispenserBehavior lv4 = new ItemDispenserBehavior(){
            private final ItemDispenserBehavior fallbackBehavior = new ItemDispenserBehavior();

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                FluidModificationItem lv = (FluidModificationItem)((Object)stack.getItem());
                BlockPos lv2 = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
                ServerWorld lv3 = pointer.world();
                if (lv.placeFluid(null, lv3, lv2, null)) {
                    lv.onEmptied(null, lv3, stack, lv2);
                    return this.decrementStackWithRemainder(pointer, stack, new ItemStack(Items.BUCKET));
                }
                return this.fallbackBehavior.dispense(pointer, stack);
            }
        };
        DispenserBlock.registerBehavior(Items.LAVA_BUCKET, lv4);
        DispenserBlock.registerBehavior(Items.WATER_BUCKET, lv4);
        DispenserBlock.registerBehavior(Items.POWDER_SNOW_BUCKET, lv4);
        DispenserBlock.registerBehavior(Items.SALMON_BUCKET, lv4);
        DispenserBlock.registerBehavior(Items.COD_BUCKET, lv4);
        DispenserBlock.registerBehavior(Items.PUFFERFISH_BUCKET, lv4);
        DispenserBlock.registerBehavior(Items.TROPICAL_FISH_BUCKET, lv4);
        DispenserBlock.registerBehavior(Items.AXOLOTL_BUCKET, lv4);
        DispenserBlock.registerBehavior(Items.TADPOLE_BUCKET, lv4);
        DispenserBlock.registerBehavior(Items.BUCKET, new ItemDispenserBehavior(){

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                ItemStack lv6;
                BlockPos lv2;
                ServerWorld lv = pointer.world();
                BlockState lv3 = lv.getBlockState(lv2 = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING)));
                Block lv4 = lv3.getBlock();
                if (lv4 instanceof FluidDrainable) {
                    FluidDrainable lv5 = (FluidDrainable)((Object)lv4);
                    lv6 = lv5.tryDrainFluid(null, lv, lv2, lv3);
                    if (lv6.isEmpty()) {
                        return super.dispenseSilently(pointer, stack);
                    }
                } else {
                    return super.dispenseSilently(pointer, stack);
                }
                lv.emitGameEvent(null, GameEvent.FLUID_PICKUP, lv2);
                Item lv7 = lv6.getItem();
                return this.decrementStackWithRemainder(pointer, stack, new ItemStack(lv7));
            }
        });
        DispenserBlock.registerBehavior(Items.FLINT_AND_STEEL, new FallibleItemDispenserBehavior(){

            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                ServerWorld lv = pointer.world();
                this.setSuccess(true);
                Direction lv2 = pointer.state().get(DispenserBlock.FACING);
                BlockPos lv3 = pointer.pos().offset(lv2);
                BlockState lv4 = lv.getBlockState(lv3);
                if (AbstractFireBlock.canPlaceAt(lv, lv3, lv2)) {
                    lv.setBlockState(lv3, AbstractFireBlock.getState(lv, lv3));
                    lv.emitGameEvent(null, GameEvent.BLOCK_PLACE, lv3);
                } else if (CampfireBlock.canBeLit(lv4) || CandleBlock.canBeLit(lv4) || CandleCakeBlock.canBeLit(lv4)) {
                    lv.setBlockState(lv3, (BlockState)lv4.with(Properties.LIT, true));
                    lv.emitGameEvent(null, GameEvent.BLOCK_CHANGE, lv3);
                } else if (lv4.getBlock() instanceof TntBlock) {
                    TntBlock.primeTnt(lv, lv3);
                    lv.removeBlock(lv3, false);
                } else {
                    this.setSuccess(false);
                }
                if (this.isSuccess()) {
                    stack.damage(1, lv, null, item -> {});
                }
                return stack;
            }
        });
        DispenserBlock.registerBehavior(Items.BONE_MEAL, new FallibleItemDispenserBehavior(){

            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                this.setSuccess(true);
                ServerWorld lv = pointer.world();
                BlockPos lv2 = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
                if (BoneMealItem.useOnFertilizable(stack, lv, lv2) || BoneMealItem.useOnGround(stack, lv, lv2, null)) {
                    if (!lv.isClient) {
                        lv.syncWorldEvent(WorldEvents.BONE_MEAL_USED, lv2, 15);
                    }
                } else {
                    this.setSuccess(false);
                }
                return stack;
            }
        });
        DispenserBlock.registerBehavior(Blocks.TNT, new ItemDispenserBehavior(){

            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                ServerWorld lv = pointer.world();
                BlockPos lv2 = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
                TntEntity lv3 = new TntEntity(lv, (double)lv2.getX() + 0.5, lv2.getY(), (double)lv2.getZ() + 0.5, null);
                lv.spawnEntity(lv3);
                lv.playSound(null, lv3.getX(), lv3.getY(), lv3.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0f, 1.0f);
                lv.emitGameEvent(null, GameEvent.ENTITY_PLACE, lv2);
                stack.decrement(1);
                return stack;
            }
        });
        FallibleItemDispenserBehavior lv5 = new FallibleItemDispenserBehavior(){

            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                this.setSuccess(ArmorItem.dispenseArmor(pointer, stack));
                return stack;
            }
        };
        DispenserBlock.registerBehavior(Items.CREEPER_HEAD, lv5);
        DispenserBlock.registerBehavior(Items.ZOMBIE_HEAD, lv5);
        DispenserBlock.registerBehavior(Items.DRAGON_HEAD, lv5);
        DispenserBlock.registerBehavior(Items.SKELETON_SKULL, lv5);
        DispenserBlock.registerBehavior(Items.PIGLIN_HEAD, lv5);
        DispenserBlock.registerBehavior(Items.PLAYER_HEAD, lv5);
        DispenserBlock.registerBehavior(Items.WITHER_SKELETON_SKULL, new FallibleItemDispenserBehavior(){

            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                ServerWorld lv = pointer.world();
                Direction lv2 = pointer.state().get(DispenserBlock.FACING);
                BlockPos lv3 = pointer.pos().offset(lv2);
                if (lv.isAir(lv3) && WitherSkullBlock.canDispense(lv, lv3, stack)) {
                    lv.setBlockState(lv3, (BlockState)Blocks.WITHER_SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, RotationPropertyHelper.fromDirection(lv2)), Block.NOTIFY_ALL);
                    lv.emitGameEvent(null, GameEvent.BLOCK_PLACE, lv3);
                    BlockEntity lv4 = lv.getBlockEntity(lv3);
                    if (lv4 instanceof SkullBlockEntity) {
                        WitherSkullBlock.onPlaced(lv, lv3, (SkullBlockEntity)lv4);
                    }
                    stack.decrement(1);
                    this.setSuccess(true);
                } else {
                    this.setSuccess(ArmorItem.dispenseArmor(pointer, stack));
                }
                return stack;
            }
        });
        DispenserBlock.registerBehavior(Blocks.CARVED_PUMPKIN, new FallibleItemDispenserBehavior(){

            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                ServerWorld lv = pointer.world();
                BlockPos lv2 = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
                CarvedPumpkinBlock lv3 = (CarvedPumpkinBlock)Blocks.CARVED_PUMPKIN;
                if (lv.isAir(lv2) && lv3.canDispense(lv, lv2)) {
                    if (!lv.isClient) {
                        lv.setBlockState(lv2, lv3.getDefaultState(), Block.NOTIFY_ALL);
                        lv.emitGameEvent(null, GameEvent.BLOCK_PLACE, lv2);
                    }
                    stack.decrement(1);
                    this.setSuccess(true);
                } else {
                    this.setSuccess(ArmorItem.dispenseArmor(pointer, stack));
                }
                return stack;
            }
        });
        DispenserBlock.registerBehavior(Blocks.SHULKER_BOX.asItem(), new BlockPlacementDispenserBehavior());
        for (DyeColor lv6 : DyeColor.values()) {
            DispenserBlock.registerBehavior(ShulkerBoxBlock.get(lv6).asItem(), new BlockPlacementDispenserBehavior());
        }
        DispenserBlock.registerBehavior(Items.GLASS_BOTTLE.asItem(), new FallibleItemDispenserBehavior(){

            private ItemStack replace(BlockPointer pointer, ItemStack oldStack, ItemStack newStack) {
                pointer.world().emitGameEvent(null, GameEvent.FLUID_PICKUP, pointer.pos());
                return this.decrementStackWithRemainder(pointer, oldStack, newStack);
            }

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                this.setSuccess(false);
                ServerWorld lv = pointer.world();
                BlockPos lv2 = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
                BlockState lv3 = lv.getBlockState(lv2);
                if (lv3.isIn(BlockTags.BEEHIVES, state -> state.contains(BeehiveBlock.HONEY_LEVEL) && state.getBlock() instanceof BeehiveBlock) && lv3.get(BeehiveBlock.HONEY_LEVEL) >= 5) {
                    ((BeehiveBlock)lv3.getBlock()).takeHoney(lv, lv3, lv2, null, BeehiveBlockEntity.BeeState.BEE_RELEASED);
                    this.setSuccess(true);
                    return this.replace(pointer, stack, new ItemStack(Items.HONEY_BOTTLE));
                }
                if (lv.getFluidState(lv2).isIn(FluidTags.WATER)) {
                    this.setSuccess(true);
                    return this.replace(pointer, stack, PotionContentsComponent.createStack(Items.POTION, Potions.WATER));
                }
                return super.dispenseSilently(pointer, stack);
            }
        });
        DispenserBlock.registerBehavior(Items.GLOWSTONE, new FallibleItemDispenserBehavior(){

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                Direction lv = pointer.state().get(DispenserBlock.FACING);
                BlockPos lv2 = pointer.pos().offset(lv);
                ServerWorld lv3 = pointer.world();
                BlockState lv4 = lv3.getBlockState(lv2);
                this.setSuccess(true);
                if (lv4.isOf(Blocks.RESPAWN_ANCHOR)) {
                    if (lv4.get(RespawnAnchorBlock.CHARGES) != 4) {
                        RespawnAnchorBlock.charge(null, lv3, lv2, lv4);
                        stack.decrement(1);
                    } else {
                        this.setSuccess(false);
                    }
                    return stack;
                }
                return super.dispenseSilently(pointer, stack);
            }
        });
        DispenserBlock.registerBehavior(Items.SHEARS.asItem(), new ShearsDispenserBehavior());
        DispenserBlock.registerBehavior(Items.BRUSH.asItem(), new FallibleItemDispenserBehavior(){

            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                BlockPos lv2;
                ServerWorld lv = pointer.world();
                List<Entity> list = lv.getEntitiesByClass(ArmadilloEntity.class, new Box(lv2 = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING))), EntityPredicates.EXCEPT_SPECTATOR);
                if (list.isEmpty()) {
                    this.setSuccess(false);
                    return stack;
                }
                for (ArmadilloEntity armadilloEntity : list) {
                    if (!armadilloEntity.brushScute()) continue;
                    stack.damage(16, lv, null, arg -> {});
                    return stack;
                }
                this.setSuccess(false);
                return stack;
            }
        });
        DispenserBlock.registerBehavior(Items.HONEYCOMB, new FallibleItemDispenserBehavior(){

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                BlockPos lv = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
                ServerWorld lv2 = pointer.world();
                BlockState lv3 = lv2.getBlockState(lv);
                Optional<BlockState> optional = HoneycombItem.getWaxedState(lv3);
                if (optional.isPresent()) {
                    lv2.setBlockState(lv, optional.get());
                    lv2.syncWorldEvent(WorldEvents.BLOCK_WAXED, lv, 0);
                    stack.decrement(1);
                    this.setSuccess(true);
                    return stack;
                }
                return super.dispenseSilently(pointer, stack);
            }
        });
        DispenserBlock.registerBehavior(Items.POTION, new ItemDispenserBehavior(){
            private final ItemDispenserBehavior fallbackBehavior = new ItemDispenserBehavior();

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                PotionContentsComponent lv = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
                if (!lv.matches(Potions.WATER)) {
                    return this.fallbackBehavior.dispense(pointer, stack);
                }
                ServerWorld lv2 = pointer.world();
                BlockPos lv3 = pointer.pos();
                BlockPos lv4 = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
                if (lv2.getBlockState(lv4).isIn(BlockTags.CONVERTABLE_TO_MUD)) {
                    if (!lv2.isClient) {
                        for (int i = 0; i < 5; ++i) {
                            lv2.spawnParticles(ParticleTypes.SPLASH, (double)lv3.getX() + lv2.random.nextDouble(), lv3.getY() + 1, (double)lv3.getZ() + lv2.random.nextDouble(), 1, 0.0, 0.0, 0.0, 1.0);
                        }
                    }
                    lv2.playSound(null, lv3, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    lv2.emitGameEvent(null, GameEvent.FLUID_PLACE, lv3);
                    lv2.setBlockState(lv4, Blocks.MUD.getDefaultState());
                    return this.decrementStackWithRemainder(pointer, stack, new ItemStack(Items.GLASS_BOTTLE));
                }
                return this.fallbackBehavior.dispense(pointer, stack);
            }
        });
    }
}

