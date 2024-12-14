/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.JukeboxPlayableComponent;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryPair;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.resource.featuretoggle.ToggleableFeature;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Unit;
import net.minecraft.util.UseAction;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class Item
implements ToggleableFeature,
ItemConvertible {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Map<Block, Item> BLOCK_ITEMS = Maps.newHashMap();
    public static final Identifier BASE_ATTACK_DAMAGE_MODIFIER_ID = Identifier.ofVanilla("base_attack_damage");
    public static final Identifier BASE_ATTACK_SPEED_MODIFIER_ID = Identifier.ofVanilla("base_attack_speed");
    public static final int DEFAULT_MAX_COUNT = 64;
    public static final int MAX_MAX_COUNT = 99;
    public static final int ITEM_BAR_STEPS = 13;
    private final RegistryEntry.Reference<Item> registryEntry = Registries.ITEM.createEntry(this);
    private final ComponentMap components;
    @Nullable
    private final Item recipeRemainder;
    @Nullable
    private String translationKey;
    private final FeatureSet requiredFeatures;

    public static int getRawId(Item item) {
        return item == null ? 0 : Registries.ITEM.getRawId(item);
    }

    public static Item byRawId(int id) {
        return Registries.ITEM.get(id);
    }

    @Deprecated
    public static Item fromBlock(Block block) {
        return BLOCK_ITEMS.getOrDefault(block, Items.AIR);
    }

    public Item(Settings settings) {
        String string;
        this.components = settings.getValidatedComponents();
        this.recipeRemainder = settings.recipeRemainder;
        this.requiredFeatures = settings.requiredFeatures;
        if (SharedConstants.isDevelopment && !(string = this.getClass().getSimpleName()).endsWith("Item")) {
            LOGGER.error("Item classes should end with Item and {} doesn't.", (Object)string);
        }
    }

    @Deprecated
    public RegistryEntry.Reference<Item> getRegistryEntry() {
        return this.registryEntry;
    }

    public ComponentMap getComponents() {
        return this.components;
    }

    public int getMaxCount() {
        return this.components.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1);
    }

    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
    }

    public void onItemEntityDestroyed(ItemEntity entity) {
    }

    public void postProcessComponents(ItemStack stack) {
    }

    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return true;
    }

    @Override
    public Item asItem() {
        return this;
    }

    public ActionResult useOnBlock(ItemUsageContext context) {
        return ActionResult.PASS;
    }

    public float getMiningSpeed(ItemStack stack, BlockState state) {
        ToolComponent lv = stack.get(DataComponentTypes.TOOL);
        return lv != null ? lv.getSpeed(state) : 1.0f;
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        FoodComponent lv2 = lv.get(DataComponentTypes.FOOD);
        if (lv2 != null) {
            if (user.canConsume(lv2.canAlwaysEat())) {
                user.setCurrentHand(hand);
                return TypedActionResult.consume(lv);
            }
            return TypedActionResult.fail(lv);
        }
        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        FoodComponent lv = stack.get(DataComponentTypes.FOOD);
        if (lv != null) {
            return user.eatFood(world, stack, lv);
        }
        return stack;
    }

    public boolean isItemBarVisible(ItemStack stack) {
        return stack.isDamaged();
    }

    public int getItemBarStep(ItemStack stack) {
        return MathHelper.clamp(Math.round(13.0f - (float)stack.getDamage() * 13.0f / (float)stack.getMaxDamage()), 0, 13);
    }

    public int getItemBarColor(ItemStack stack) {
        int i = stack.getMaxDamage();
        float f = Math.max(0.0f, ((float)i - (float)stack.getDamage()) / (float)i);
        return MathHelper.hsvToRgb(f / 3.0f, 1.0f, 1.0f);
    }

    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        return false;
    }

    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        return false;
    }

    public float getBonusAttackDamage(Entity target, float baseAttackDamage, DamageSource damageSource) {
        return 0.0f;
    }

    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return false;
    }

    public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
    }

    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        ToolComponent lv = stack.get(DataComponentTypes.TOOL);
        if (lv == null) {
            return false;
        }
        if (!world.isClient && state.getHardness(world, pos) != 0.0f && lv.damagePerBlock() > 0) {
            stack.damage(lv.damagePerBlock(), miner, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    public boolean isCorrectForDrops(ItemStack stack, BlockState state) {
        ToolComponent lv = stack.get(DataComponentTypes.TOOL);
        return lv != null && lv.isCorrectForDrops(state);
    }

    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        return ActionResult.PASS;
    }

    public Text getName() {
        return Text.translatable(this.getTranslationKey());
    }

    public String toString() {
        return Registries.ITEM.getEntry(this).getIdAsString();
    }

    protected String getOrCreateTranslationKey() {
        if (this.translationKey == null) {
            this.translationKey = Util.createTranslationKey("item", Registries.ITEM.getId(this));
        }
        return this.translationKey;
    }

    public String getTranslationKey() {
        return this.getOrCreateTranslationKey();
    }

    public String getTranslationKey(ItemStack stack) {
        return this.getTranslationKey();
    }

    @Nullable
    public final Item getRecipeRemainder() {
        return this.recipeRemainder;
    }

    public boolean hasRecipeRemainder() {
        return this.recipeRemainder != null;
    }

    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
    }

    public void onCraftByPlayer(ItemStack stack, World world, PlayerEntity player) {
        this.onCraft(stack, world);
    }

    public void onCraft(ItemStack stack, World world) {
    }

    public boolean isNetworkSynced() {
        return false;
    }

    public UseAction getUseAction(ItemStack stack) {
        return stack.contains(DataComponentTypes.FOOD) ? UseAction.EAT : UseAction.NONE;
    }

    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        FoodComponent lv = stack.get(DataComponentTypes.FOOD);
        if (lv != null) {
            return lv.getEatTicks();
        }
        return 0;
    }

    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
    }

    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
    }

    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        return Optional.empty();
    }

    public Text getName(ItemStack stack) {
        return Text.translatable(this.getTranslationKey(stack));
    }

    public boolean hasGlint(ItemStack stack) {
        return stack.hasEnchantments();
    }

    public boolean isEnchantable(ItemStack stack) {
        return stack.getMaxCount() == 1 && stack.contains(DataComponentTypes.MAX_DAMAGE);
    }

    protected static BlockHitResult raycast(World world, PlayerEntity player, RaycastContext.FluidHandling fluidHandling) {
        Vec3d lv = player.getEyePos();
        Vec3d lv2 = lv.add(player.getRotationVector(player.getPitch(), player.getYaw()).multiply(player.getBlockInteractionRange()));
        return world.raycast(new RaycastContext(lv, lv2, RaycastContext.ShapeType.OUTLINE, fluidHandling, player));
    }

    public int getEnchantability() {
        return 0;
    }

    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return false;
    }

    @Deprecated
    public AttributeModifiersComponent getAttributeModifiers() {
        return AttributeModifiersComponent.DEFAULT;
    }

    public boolean isUsedOnRelease(ItemStack stack) {
        return false;
    }

    public ItemStack getDefaultStack() {
        return new ItemStack(this);
    }

    public SoundEvent getDrinkSound() {
        return SoundEvents.ENTITY_GENERIC_DRINK;
    }

    public SoundEvent getEatSound() {
        return SoundEvents.ENTITY_GENERIC_EAT;
    }

    public SoundEvent getBreakSound() {
        return SoundEvents.ENTITY_ITEM_BREAK;
    }

    public boolean canBeNested() {
        return true;
    }

    @Override
    public FeatureSet getRequiredFeatures() {
        return this.requiredFeatures;
    }

    public static class Settings {
        private static final Interner<ComponentMap> COMPONENT_MAP_INTERNER = Interners.newStrongInterner();
        @Nullable
        private ComponentMap.Builder components;
        @Nullable
        Item recipeRemainder;
        FeatureSet requiredFeatures = FeatureFlags.VANILLA_FEATURES;

        public Settings food(FoodComponent foodComponent) {
            return this.component(DataComponentTypes.FOOD, foodComponent);
        }

        public Settings maxCount(int maxCount) {
            return this.component(DataComponentTypes.MAX_STACK_SIZE, maxCount);
        }

        public Settings maxDamage(int maxDamage) {
            this.component(DataComponentTypes.MAX_DAMAGE, maxDamage);
            this.component(DataComponentTypes.MAX_STACK_SIZE, 1);
            this.component(DataComponentTypes.DAMAGE, 0);
            return this;
        }

        public Settings recipeRemainder(Item recipeRemainder) {
            this.recipeRemainder = recipeRemainder;
            return this;
        }

        public Settings rarity(Rarity rarity) {
            return this.component(DataComponentTypes.RARITY, rarity);
        }

        public Settings fireproof() {
            return this.component(DataComponentTypes.FIRE_RESISTANT, Unit.INSTANCE);
        }

        public Settings jukeboxPlayable(RegistryKey<JukeboxSong> songKey) {
            return this.component(DataComponentTypes.JUKEBOX_PLAYABLE, new JukeboxPlayableComponent(new RegistryPair<JukeboxSong>(songKey), true));
        }

        public Settings requires(FeatureFlag ... features) {
            this.requiredFeatures = FeatureFlags.FEATURE_MANAGER.featureSetOf(features);
            return this;
        }

        public <T> Settings component(ComponentType<T> type, T value) {
            if (this.components == null) {
                this.components = ComponentMap.builder().addAll(DataComponentTypes.DEFAULT_ITEM_COMPONENTS);
            }
            this.components.add(type, value);
            return this;
        }

        public Settings attributeModifiers(AttributeModifiersComponent attributeModifiersComponent) {
            return this.component(DataComponentTypes.ATTRIBUTE_MODIFIERS, attributeModifiersComponent);
        }

        ComponentMap getValidatedComponents() {
            ComponentMap lv = this.getComponents();
            if (lv.contains(DataComponentTypes.DAMAGE) && lv.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1) > 1) {
                throw new IllegalStateException("Item cannot have both durability and be stackable");
            }
            return lv;
        }

        private ComponentMap getComponents() {
            if (this.components == null) {
                return DataComponentTypes.DEFAULT_ITEM_COMPONENTS;
            }
            return COMPONENT_MAP_INTERNER.intern(this.components.build());
        }
    }

    public static interface TooltipContext {
        public static final TooltipContext DEFAULT = new TooltipContext(){

            @Override
            @Nullable
            public RegistryWrapper.WrapperLookup getRegistryLookup() {
                return null;
            }

            @Override
            public float getUpdateTickRate() {
                return 20.0f;
            }

            @Override
            @Nullable
            public MapState getMapState(MapIdComponent mapIdComponent) {
                return null;
            }
        };

        @Nullable
        public RegistryWrapper.WrapperLookup getRegistryLookup();

        public float getUpdateTickRate();

        @Nullable
        public MapState getMapState(MapIdComponent var1);

        public static TooltipContext create(final @Nullable World world) {
            if (world == null) {
                return DEFAULT;
            }
            return new TooltipContext(){

                @Override
                public RegistryWrapper.WrapperLookup getRegistryLookup() {
                    return world.getRegistryManager();
                }

                @Override
                public float getUpdateTickRate() {
                    return world.getTickManager().getTickRate();
                }

                @Override
                public MapState getMapState(MapIdComponent mapIdComponent) {
                    return world.getMapState(mapIdComponent);
                }
            };
        }

        public static TooltipContext create(final RegistryWrapper.WrapperLookup registryLookup) {
            return new TooltipContext(){

                @Override
                public RegistryWrapper.WrapperLookup getRegistryLookup() {
                    return registryLookup;
                }

                @Override
                public float getUpdateTickRate() {
                    return 20.0f;
                }

                @Override
                @Nullable
                public MapState getMapState(MapIdComponent mapIdComponent) {
                    return null;
                }
            };
        }
    }
}

