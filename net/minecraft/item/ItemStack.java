/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BlockPredicatesChecker;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Unit;
import net.minecraft.util.UseAction;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.dynamic.NullOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class ItemStack
implements ComponentHolder {
    public static final Codec<RegistryEntry<Item>> ITEM_CODEC = Registries.ITEM.getEntryCodec().validate(entry -> entry.matches(Items.AIR.getRegistryEntry()) ? DataResult.error(() -> "Item must not be minecraft:air") : DataResult.success(entry));
    public static final Codec<ItemStack> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ITEM_CODEC.fieldOf("id")).forGetter(ItemStack::getRegistryEntry), ((MapCodec)Codecs.rangedInt(1, 99).fieldOf("count")).orElse(1).forGetter(ItemStack::getCount), ComponentChanges.CODEC.optionalFieldOf("components", ComponentChanges.EMPTY).forGetter(stack -> stack.components.getChanges())).apply((Applicative<ItemStack, ?>)instance, ItemStack::new)));
    public static final Codec<ItemStack> UNCOUNTED_CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ITEM_CODEC.fieldOf("id")).forGetter(ItemStack::getRegistryEntry), ComponentChanges.CODEC.optionalFieldOf("components", ComponentChanges.EMPTY).forGetter(stack -> stack.components.getChanges())).apply((Applicative<ItemStack, ?>)instance, (item, components) -> new ItemStack((RegistryEntry<Item>)item, 1, (ComponentChanges)components))));
    public static final Codec<ItemStack> VALIDATED_CODEC = CODEC.validate(ItemStack::validate);
    public static final Codec<ItemStack> VALIDATED_UNCOUNTED_CODEC = UNCOUNTED_CODEC.validate(ItemStack::validate);
    public static final Codec<ItemStack> OPTIONAL_CODEC = Codecs.optional(CODEC).xmap(optional -> optional.orElse(EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));
    public static final Codec<ItemStack> REGISTRY_ENTRY_CODEC = ITEM_CODEC.xmap(ItemStack::new, ItemStack::getRegistryEntry);
    public static final PacketCodec<RegistryByteBuf, ItemStack> OPTIONAL_PACKET_CODEC = new PacketCodec<RegistryByteBuf, ItemStack>(){
        private static final PacketCodec<RegistryByteBuf, RegistryEntry<Item>> ITEM_PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.ITEM);

        @Override
        public ItemStack decode(RegistryByteBuf arg) {
            int i = arg.readVarInt();
            if (i <= 0) {
                return EMPTY;
            }
            RegistryEntry lv = (RegistryEntry)ITEM_PACKET_CODEC.decode(arg);
            ComponentChanges lv2 = (ComponentChanges)ComponentChanges.PACKET_CODEC.decode(arg);
            return new ItemStack(lv, i, lv2);
        }

        @Override
        public void encode(RegistryByteBuf arg, ItemStack arg2) {
            if (arg2.isEmpty()) {
                arg.writeVarInt(0);
                return;
            }
            arg.writeVarInt(arg2.getCount());
            ITEM_PACKET_CODEC.encode(arg, arg2.getRegistryEntry());
            ComponentChanges.PACKET_CODEC.encode(arg, arg2.components.getChanges());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((RegistryByteBuf)object, (ItemStack)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((RegistryByteBuf)object);
        }
    };
    public static final PacketCodec<RegistryByteBuf, ItemStack> PACKET_CODEC = new PacketCodec<RegistryByteBuf, ItemStack>(){

        @Override
        public ItemStack decode(RegistryByteBuf arg) {
            ItemStack lv = (ItemStack)OPTIONAL_PACKET_CODEC.decode(arg);
            if (lv.isEmpty()) {
                throw new DecoderException("Empty ItemStack not allowed");
            }
            return lv;
        }

        @Override
        public void encode(RegistryByteBuf arg, ItemStack arg2) {
            if (arg2.isEmpty()) {
                throw new EncoderException("Empty ItemStack not allowed");
            }
            OPTIONAL_PACKET_CODEC.encode(arg, arg2);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((RegistryByteBuf)object, (ItemStack)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((RegistryByteBuf)object);
        }
    };
    public static final PacketCodec<RegistryByteBuf, List<ItemStack>> OPTIONAL_LIST_PACKET_CODEC = OPTIONAL_PACKET_CODEC.collect(PacketCodecs.toCollection(DefaultedList::ofSize));
    public static final PacketCodec<RegistryByteBuf, List<ItemStack>> LIST_PACKET_CODEC = PACKET_CODEC.collect(PacketCodecs.toCollection(DefaultedList::ofSize));
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ItemStack EMPTY = new ItemStack((Void)null);
    private static final Text DISABLED_TEXT = Text.translatable("item.disabled").formatted(Formatting.RED);
    private int count;
    private int bobbingAnimationTime;
    @Deprecated
    @Nullable
    private final Item item;
    final ComponentMapImpl components;
    @Nullable
    private Entity holder;

    private static DataResult<ItemStack> validate(ItemStack stack) {
        DataResult<Unit> dataResult = ItemStack.validateComponents(stack.getComponents());
        if (dataResult.isError()) {
            return dataResult.map(v -> stack);
        }
        if (stack.getCount() > stack.getMaxCount()) {
            return DataResult.error(() -> "Item stack with stack size of " + stack.getCount() + " was larger than maximum: " + stack.getMaxCount());
        }
        return DataResult.success(stack);
    }

    public static PacketCodec<RegistryByteBuf, ItemStack> createExtraValidatingPacketCodec(final PacketCodec<RegistryByteBuf, ItemStack> basePacketCodec) {
        return new PacketCodec<RegistryByteBuf, ItemStack>(){

            @Override
            public ItemStack decode(RegistryByteBuf arg) {
                ItemStack lv = (ItemStack)basePacketCodec.decode(arg);
                if (!lv.isEmpty()) {
                    RegistryOps<Unit> lv2 = arg.getRegistryManager().getOps(NullOps.INSTANCE);
                    CODEC.encodeStart(lv2, lv).getOrThrow(DecoderException::new);
                }
                return lv;
            }

            @Override
            public void encode(RegistryByteBuf arg, ItemStack arg2) {
                basePacketCodec.encode(arg, arg2);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryByteBuf)object, (ItemStack)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryByteBuf)object);
            }
        };
    }

    public Optional<TooltipData> getTooltipData() {
        return this.getItem().getTooltipData(this);
    }

    @Override
    public ComponentMap getComponents() {
        return !this.isEmpty() ? this.components : ComponentMap.EMPTY;
    }

    public ComponentMap getDefaultComponents() {
        return !this.isEmpty() ? this.getItem().getComponents() : ComponentMap.EMPTY;
    }

    public ComponentChanges getComponentChanges() {
        return !this.isEmpty() ? this.components.getChanges() : ComponentChanges.EMPTY;
    }

    public ItemStack(ItemConvertible item) {
        this(item, 1);
    }

    public ItemStack(RegistryEntry<Item> entry) {
        this(entry.value(), 1);
    }

    public ItemStack(RegistryEntry<Item> item, int count, ComponentChanges changes) {
        this(item.value(), count, ComponentMapImpl.create(item.value().getComponents(), changes));
    }

    public ItemStack(RegistryEntry<Item> itemEntry, int count) {
        this(itemEntry.value(), count);
    }

    public ItemStack(ItemConvertible item, int count) {
        this(item, count, new ComponentMapImpl(item.asItem().getComponents()));
    }

    private ItemStack(ItemConvertible item, int count, ComponentMapImpl components) {
        this.item = item.asItem();
        this.count = count;
        this.components = components;
        this.getItem().postProcessComponents(this);
    }

    private ItemStack(@Nullable Void v) {
        this.item = null;
        this.components = new ComponentMapImpl(ComponentMap.EMPTY);
    }

    public static DataResult<Unit> validateComponents(ComponentMap components) {
        if (components.contains(DataComponentTypes.MAX_DAMAGE) && components.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1) > 1) {
            return DataResult.error(() -> "Item cannot be both damageable and stackable");
        }
        ContainerComponent lv = components.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
        for (ItemStack lv2 : lv.iterateNonEmpty()) {
            int j;
            int i = lv2.getCount();
            if (i <= (j = lv2.getMaxCount())) continue;
            return DataResult.error(() -> "Item stack with count of " + i + " was larger than maximum: " + j);
        }
        return DataResult.success(Unit.INSTANCE);
    }

    public static Optional<ItemStack> fromNbt(RegistryWrapper.WrapperLookup registries, NbtElement nbt) {
        return CODEC.parse(registries.getOps(NbtOps.INSTANCE), nbt).resultOrPartial(error -> LOGGER.error("Tried to load invalid item: '{}'", error));
    }

    public static ItemStack fromNbtOrEmpty(RegistryWrapper.WrapperLookup registries, NbtCompound nbt) {
        if (nbt.isEmpty()) {
            return EMPTY;
        }
        return ItemStack.fromNbt(registries, nbt).orElse(EMPTY);
    }

    public boolean isEmpty() {
        return this == EMPTY || this.item == Items.AIR || this.count <= 0;
    }

    public boolean isItemEnabled(FeatureSet enabledFeatures) {
        return this.isEmpty() || this.getItem().isEnabled(enabledFeatures);
    }

    public ItemStack split(int amount) {
        int j = Math.min(amount, this.getCount());
        ItemStack lv = this.copyWithCount(j);
        this.decrement(j);
        return lv;
    }

    public ItemStack copyAndEmpty() {
        if (this.isEmpty()) {
            return EMPTY;
        }
        ItemStack lv = this.copy();
        this.setCount(0);
        return lv;
    }

    public Item getItem() {
        return this.isEmpty() ? Items.AIR : this.item;
    }

    public RegistryEntry<Item> getRegistryEntry() {
        return this.getItem().getRegistryEntry();
    }

    public boolean isIn(TagKey<Item> tag) {
        return this.getItem().getRegistryEntry().isIn(tag);
    }

    public boolean isOf(Item item) {
        return this.getItem() == item;
    }

    public boolean itemMatches(Predicate<RegistryEntry<Item>> predicate) {
        return predicate.test(this.getItem().getRegistryEntry());
    }

    public boolean itemMatches(RegistryEntry<Item> itemEntry) {
        return this.getItem().getRegistryEntry() == itemEntry;
    }

    public boolean isIn(RegistryEntryList<Item> registryEntryList) {
        return registryEntryList.contains(this.getRegistryEntry());
    }

    public Stream<TagKey<Item>> streamTags() {
        return this.getItem().getRegistryEntry().streamTags();
    }

    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity lv = context.getPlayer();
        BlockPos lv2 = context.getBlockPos();
        if (lv != null && !lv.getAbilities().allowModifyWorld && !this.canPlaceOn(new CachedBlockPosition(context.getWorld(), lv2, false))) {
            return ActionResult.PASS;
        }
        Item lv3 = this.getItem();
        ActionResult lv4 = lv3.useOnBlock(context);
        if (lv != null && lv4.shouldIncrementStat()) {
            lv.incrementStat(Stats.USED.getOrCreateStat(lv3));
        }
        return lv4;
    }

    public float getMiningSpeedMultiplier(BlockState state) {
        return this.getItem().getMiningSpeed(this, state);
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return this.getItem().use(world, user, hand);
    }

    public ItemStack finishUsing(World world, LivingEntity user) {
        return this.getItem().finishUsing(this, world, user);
    }

    public NbtElement encode(RegistryWrapper.WrapperLookup registries, NbtElement prefix) {
        if (this.isEmpty()) {
            throw new IllegalStateException("Cannot encode empty ItemStack");
        }
        return CODEC.encode(this, registries.getOps(NbtOps.INSTANCE), prefix).getOrThrow();
    }

    public NbtElement encode(RegistryWrapper.WrapperLookup registries) {
        if (this.isEmpty()) {
            throw new IllegalStateException("Cannot encode empty ItemStack");
        }
        return CODEC.encodeStart(registries.getOps(NbtOps.INSTANCE), this).getOrThrow();
    }

    public NbtElement encodeAllowEmpty(RegistryWrapper.WrapperLookup registries) {
        if (this.isEmpty()) {
            return new NbtCompound();
        }
        return this.encode(registries, new NbtCompound());
    }

    public int getMaxCount() {
        return this.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1);
    }

    public boolean isStackable() {
        return this.getMaxCount() > 1 && (!this.isDamageable() || !this.isDamaged());
    }

    public boolean isDamageable() {
        return this.contains(DataComponentTypes.MAX_DAMAGE) && !this.contains(DataComponentTypes.UNBREAKABLE) && this.contains(DataComponentTypes.DAMAGE);
    }

    public boolean isDamaged() {
        return this.isDamageable() && this.getDamage() > 0;
    }

    public int getDamage() {
        return MathHelper.clamp(this.getOrDefault(DataComponentTypes.DAMAGE, 0), 0, this.getMaxDamage());
    }

    public void setDamage(int damage) {
        this.set(DataComponentTypes.DAMAGE, MathHelper.clamp(damage, 0, this.getMaxDamage()));
    }

    public int getMaxDamage() {
        return this.getOrDefault(DataComponentTypes.MAX_DAMAGE, 0);
    }

    public void damage(int amount, ServerWorld world, @Nullable ServerPlayerEntity player, Consumer<Item> breakCallback) {
        if (!this.isDamageable()) {
            return;
        }
        if (player != null && player.isInCreativeMode()) {
            return;
        }
        if (amount > 0 && (amount = EnchantmentHelper.getItemDamage(world, this, amount)) <= 0) {
            return;
        }
        if (player != null && amount != 0) {
            Criteria.ITEM_DURABILITY_CHANGED.trigger(player, this, this.getDamage() + amount);
        }
        int j = this.getDamage() + amount;
        this.setDamage(j);
        if (j >= this.getMaxDamage()) {
            Item lv = this.getItem();
            this.decrement(1);
            breakCallback.accept(lv);
        }
    }

    public void damage(int amount, LivingEntity entity, EquipmentSlot slot) {
        World world = entity.getWorld();
        if (world instanceof ServerWorld) {
            ServerPlayerEntity lv2;
            ServerWorld lv = (ServerWorld)world;
            this.damage(amount, lv, entity instanceof ServerPlayerEntity ? (lv2 = (ServerPlayerEntity)entity) : null, (Item item) -> entity.sendEquipmentBreakStatus((Item)item, slot));
        }
    }

    public ItemStack damage(int amount, ItemConvertible itemAfterBreaking, LivingEntity entity, EquipmentSlot slot) {
        this.damage(amount, entity, slot);
        if (this.isEmpty()) {
            ItemStack lv = this.copyComponentsToNewStackIgnoreEmpty(itemAfterBreaking, 1);
            if (lv.isDamageable()) {
                lv.setDamage(0);
            }
            return lv;
        }
        return this;
    }

    public boolean isItemBarVisible() {
        return this.getItem().isItemBarVisible(this);
    }

    public int getItemBarStep() {
        return this.getItem().getItemBarStep(this);
    }

    public int getItemBarColor() {
        return this.getItem().getItemBarColor(this);
    }

    public boolean onStackClicked(Slot slot, ClickType clickType, PlayerEntity player) {
        return this.getItem().onStackClicked(this, slot, clickType, player);
    }

    public boolean onClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        return this.getItem().onClicked(this, stack, slot, clickType, player, cursorStackReference);
    }

    public boolean postHit(LivingEntity target, PlayerEntity player) {
        Item lv = this.getItem();
        if (lv.postHit(this, target, player)) {
            player.incrementStat(Stats.USED.getOrCreateStat(lv));
            return true;
        }
        return false;
    }

    public void postDamageEntity(LivingEntity target, PlayerEntity player) {
        this.getItem().postDamageEntity(this, target, player);
    }

    public void postMine(World world, BlockState state, BlockPos pos, PlayerEntity miner) {
        Item lv = this.getItem();
        if (lv.postMine(this, world, state, pos, miner)) {
            miner.incrementStat(Stats.USED.getOrCreateStat(lv));
        }
    }

    public boolean isSuitableFor(BlockState state) {
        return this.getItem().isCorrectForDrops(this, state);
    }

    public ActionResult useOnEntity(PlayerEntity user, LivingEntity entity, Hand hand) {
        return this.getItem().useOnEntity(this, user, entity, hand);
    }

    public ItemStack copy() {
        if (this.isEmpty()) {
            return EMPTY;
        }
        ItemStack lv = new ItemStack(this.getItem(), this.count, this.components.copy());
        lv.setBobbingAnimationTime(this.getBobbingAnimationTime());
        return lv;
    }

    public ItemStack copyWithCount(int count) {
        if (this.isEmpty()) {
            return EMPTY;
        }
        ItemStack lv = this.copy();
        lv.setCount(count);
        return lv;
    }

    public ItemStack withItem(ItemConvertible item) {
        return this.copyComponentsToNewStack(item, this.getCount());
    }

    public ItemStack copyComponentsToNewStack(ItemConvertible item, int count) {
        if (this.isEmpty()) {
            return EMPTY;
        }
        return this.copyComponentsToNewStackIgnoreEmpty(item, count);
    }

    private ItemStack copyComponentsToNewStackIgnoreEmpty(ItemConvertible item, int count) {
        return new ItemStack(item.asItem().getRegistryEntry(), count, this.components.getChanges());
    }

    public static boolean areEqual(ItemStack left, ItemStack right) {
        if (left == right) {
            return true;
        }
        if (left.getCount() != right.getCount()) {
            return false;
        }
        return ItemStack.areItemsAndComponentsEqual(left, right);
    }

    @Deprecated
    public static boolean stacksEqual(List<ItemStack> left, List<ItemStack> right) {
        if (left.size() != right.size()) {
            return false;
        }
        for (int i = 0; i < left.size(); ++i) {
            if (ItemStack.areEqual(left.get(i), right.get(i))) continue;
            return false;
        }
        return true;
    }

    public static boolean areItemsEqual(ItemStack left, ItemStack right) {
        return left.isOf(right.getItem());
    }

    public static boolean areItemsAndComponentsEqual(ItemStack stack, ItemStack otherStack) {
        if (!stack.isOf(otherStack.getItem())) {
            return false;
        }
        if (stack.isEmpty() && otherStack.isEmpty()) {
            return true;
        }
        return Objects.equals(stack.components, otherStack.components);
    }

    public static MapCodec<ItemStack> createOptionalCodec(String fieldName) {
        return CODEC.lenientOptionalFieldOf(fieldName).xmap(optional -> optional.orElse(EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));
    }

    public static int hashCode(@Nullable ItemStack stack) {
        if (stack != null) {
            int i = 31 + stack.getItem().hashCode();
            return 31 * i + stack.getComponents().hashCode();
        }
        return 0;
    }

    @Deprecated
    public static int listHashCode(List<ItemStack> stacks) {
        int i = 0;
        for (ItemStack lv : stacks) {
            i = i * 31 + ItemStack.hashCode(lv);
        }
        return i;
    }

    public String getTranslationKey() {
        return this.getItem().getTranslationKey(this);
    }

    public String toString() {
        return this.getCount() + " " + String.valueOf(this.getItem());
    }

    public void inventoryTick(World world, Entity entity, int slot, boolean selected) {
        if (this.bobbingAnimationTime > 0) {
            --this.bobbingAnimationTime;
        }
        if (this.getItem() != null) {
            this.getItem().inventoryTick(this, world, entity, slot, selected);
        }
    }

    public void onCraftByPlayer(World world, PlayerEntity player, int amount) {
        player.increaseStat(Stats.CRAFTED.getOrCreateStat(this.getItem()), amount);
        this.getItem().onCraftByPlayer(this, world, player);
    }

    public void onCraftByCrafter(World world) {
        this.getItem().onCraft(this, world);
    }

    public int getMaxUseTime(LivingEntity user) {
        return this.getItem().getMaxUseTime(this, user);
    }

    public UseAction getUseAction() {
        return this.getItem().getUseAction(this);
    }

    public void onStoppedUsing(World world, LivingEntity user, int remainingUseTicks) {
        this.getItem().onStoppedUsing(this, world, user, remainingUseTicks);
    }

    public boolean isUsedOnRelease() {
        return this.getItem().isUsedOnRelease(this);
    }

    @Nullable
    public <T> T set(ComponentType<? super T> type, @Nullable T value) {
        return this.components.set(type, value);
    }

    @Nullable
    public <T, U> T apply(ComponentType<T> type, T defaultValue, U change, BiFunction<T, U, T> applier) {
        return this.set(type, applier.apply(this.getOrDefault(type, defaultValue), change));
    }

    @Nullable
    public <T> T apply(ComponentType<T> type, T defaultValue, UnaryOperator<T> applier) {
        T object2 = this.getOrDefault(type, defaultValue);
        return this.set(type, applier.apply(object2));
    }

    @Nullable
    public <T> T remove(ComponentType<? extends T> type) {
        return this.components.remove(type);
    }

    public void applyChanges(ComponentChanges changes) {
        ComponentChanges lv = this.components.getChanges();
        this.components.applyChanges(changes);
        Optional<DataResult.Error<ItemStack>> optional = ItemStack.validate(this).error();
        if (optional.isPresent()) {
            LOGGER.error("Failed to apply component patch '{}' to item: '{}'", (Object)changes, (Object)optional.get().message());
            this.components.setChanges(lv);
            return;
        }
        this.getItem().postProcessComponents(this);
    }

    public void applyUnvalidatedChanges(ComponentChanges changes) {
        this.components.applyChanges(changes);
        this.getItem().postProcessComponents(this);
    }

    public void applyComponentsFrom(ComponentMap components) {
        this.components.setAll(components);
        this.getItem().postProcessComponents(this);
    }

    public Text getName() {
        Text lv = this.get(DataComponentTypes.CUSTOM_NAME);
        if (lv != null) {
            return lv;
        }
        Text lv2 = this.get(DataComponentTypes.ITEM_NAME);
        if (lv2 != null) {
            return lv2;
        }
        return this.getItem().getName(this);
    }

    private <T extends TooltipAppender> void appendTooltip(ComponentType<T> componentType, Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type) {
        TooltipAppender lv = (TooltipAppender)this.get(componentType);
        if (lv != null) {
            lv.appendTooltip(context, textConsumer, type);
        }
    }

    public List<Text> getTooltip(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type) {
        BlockPredicatesChecker lv4;
        MapIdComponent lv2;
        if (!type.isCreative() && this.contains(DataComponentTypes.HIDE_TOOLTIP)) {
            return List.of();
        }
        ArrayList<Text> list = Lists.newArrayList();
        MutableText lv = Text.empty().append(this.getName()).formatted(this.getRarity().getFormatting());
        if (this.contains(DataComponentTypes.CUSTOM_NAME)) {
            lv.formatted(Formatting.ITALIC);
        }
        list.add(lv);
        if (!type.isAdvanced() && !this.contains(DataComponentTypes.CUSTOM_NAME) && this.isOf(Items.FILLED_MAP) && (lv2 = this.get(DataComponentTypes.MAP_ID)) != null) {
            list.add(FilledMapItem.getIdText(lv2));
        }
        Consumer<Text> consumer = list::add;
        if (!this.contains(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP)) {
            this.getItem().appendTooltip(this, context, list, type);
        }
        this.appendTooltip(DataComponentTypes.JUKEBOX_PLAYABLE, context, consumer, type);
        this.appendTooltip(DataComponentTypes.TRIM, context, consumer, type);
        this.appendTooltip(DataComponentTypes.STORED_ENCHANTMENTS, context, consumer, type);
        this.appendTooltip(DataComponentTypes.ENCHANTMENTS, context, consumer, type);
        this.appendTooltip(DataComponentTypes.DYED_COLOR, context, consumer, type);
        this.appendTooltip(DataComponentTypes.LORE, context, consumer, type);
        this.appendAttributeModifiersTooltip(consumer, player);
        this.appendTooltip(DataComponentTypes.UNBREAKABLE, context, consumer, type);
        BlockPredicatesChecker lv3 = this.get(DataComponentTypes.CAN_BREAK);
        if (lv3 != null && lv3.showInTooltip()) {
            consumer.accept(ScreenTexts.EMPTY);
            consumer.accept(BlockPredicatesChecker.CAN_BREAK_TEXT);
            lv3.addTooltips(consumer);
        }
        if ((lv4 = this.get(DataComponentTypes.CAN_PLACE_ON)) != null && lv4.showInTooltip()) {
            consumer.accept(ScreenTexts.EMPTY);
            consumer.accept(BlockPredicatesChecker.CAN_PLACE_TEXT);
            lv4.addTooltips(consumer);
        }
        if (type.isAdvanced()) {
            if (this.isDamaged()) {
                list.add(Text.translatable("item.durability", this.getMaxDamage() - this.getDamage(), this.getMaxDamage()));
            }
            list.add(Text.literal(Registries.ITEM.getId(this.getItem()).toString()).formatted(Formatting.DARK_GRAY));
            int i = this.components.size();
            if (i > 0) {
                list.add(Text.translatable("item.components", i).formatted(Formatting.DARK_GRAY));
            }
        }
        if (player != null && !this.getItem().isEnabled(player.getWorld().getEnabledFeatures())) {
            list.add(DISABLED_TEXT);
        }
        return list;
    }

    private void appendAttributeModifiersTooltip(Consumer<Text> textConsumer, @Nullable PlayerEntity player) {
        AttributeModifiersComponent lv = this.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        if (!lv.showInTooltip()) {
            return;
        }
        for (AttributeModifierSlot lv2 : AttributeModifierSlot.values()) {
            MutableBoolean mutableBoolean = new MutableBoolean(true);
            this.applyAttributeModifier(lv2, (attribute, modifier) -> {
                if (mutableBoolean.isTrue()) {
                    textConsumer.accept(ScreenTexts.EMPTY);
                    textConsumer.accept(Text.translatable("item.modifiers." + lv2.asString()).formatted(Formatting.GRAY));
                    mutableBoolean.setFalse();
                }
                this.appendAttributeModifierTooltip(textConsumer, player, (RegistryEntry<EntityAttribute>)attribute, (EntityAttributeModifier)modifier);
            });
        }
    }

    private void appendAttributeModifierTooltip(Consumer<Text> textConsumer, @Nullable PlayerEntity player, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier) {
        double d = modifier.value();
        boolean bl = false;
        if (player != null) {
            if (modifier.idMatches(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID)) {
                d += player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                bl = true;
            } else if (modifier.idMatches(Item.BASE_ATTACK_SPEED_MODIFIER_ID)) {
                d += player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED);
                bl = true;
            }
        }
        double e = modifier.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE || modifier.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL ? d * 100.0 : (attribute.matches(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) ? d * 10.0 : d);
        if (bl) {
            textConsumer.accept(ScreenTexts.space().append(Text.translatable("attribute.modifier.equals." + modifier.operation().getId(), AttributeModifiersComponent.DECIMAL_FORMAT.format(e), Text.translatable(attribute.value().getTranslationKey()))).formatted(Formatting.DARK_GREEN));
        } else if (d > 0.0) {
            textConsumer.accept(Text.translatable("attribute.modifier.plus." + modifier.operation().getId(), AttributeModifiersComponent.DECIMAL_FORMAT.format(e), Text.translatable(attribute.value().getTranslationKey())).formatted(attribute.value().getFormatting(true)));
        } else if (d < 0.0) {
            textConsumer.accept(Text.translatable("attribute.modifier.take." + modifier.operation().getId(), AttributeModifiersComponent.DECIMAL_FORMAT.format(-e), Text.translatable(attribute.value().getTranslationKey())).formatted(attribute.value().getFormatting(false)));
        }
    }

    public boolean hasGlint() {
        Boolean boolean_ = this.get(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        if (boolean_ != null) {
            return boolean_;
        }
        return this.getItem().hasGlint(this);
    }

    public Rarity getRarity() {
        Rarity lv = this.getOrDefault(DataComponentTypes.RARITY, Rarity.COMMON);
        if (!this.hasEnchantments()) {
            return lv;
        }
        return switch (lv) {
            case Rarity.COMMON, Rarity.UNCOMMON -> Rarity.RARE;
            case Rarity.RARE -> Rarity.EPIC;
            default -> lv;
        };
    }

    public boolean isEnchantable() {
        if (this.getItem().isEnchantable(this)) {
            ItemEnchantmentsComponent lv = this.get(DataComponentTypes.ENCHANTMENTS);
            return lv != null && lv.isEmpty();
        }
        return false;
    }

    public void addEnchantment(RegistryEntry<Enchantment> enchantment, int level) {
        EnchantmentHelper.apply(this, builder -> builder.add(enchantment, level));
    }

    public boolean hasEnchantments() {
        return !this.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT).isEmpty();
    }

    public ItemEnchantmentsComponent getEnchantments() {
        return this.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
    }

    public boolean isInFrame() {
        return this.holder instanceof ItemFrameEntity;
    }

    public void setHolder(@Nullable Entity holder) {
        if (!this.isEmpty()) {
            this.holder = holder;
        }
    }

    @Nullable
    public ItemFrameEntity getFrame() {
        return this.holder instanceof ItemFrameEntity ? (ItemFrameEntity)this.getHolder() : null;
    }

    @Nullable
    public Entity getHolder() {
        return !this.isEmpty() ? this.holder : null;
    }

    public void applyAttributeModifier(AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer) {
        AttributeModifiersComponent lv = this.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        if (!lv.modifiers().isEmpty()) {
            lv.applyModifiers(slot, attributeModifierConsumer);
        } else {
            this.getItem().getAttributeModifiers().applyModifiers(slot, attributeModifierConsumer);
        }
        EnchantmentHelper.applyAttributeModifiers(this, slot, attributeModifierConsumer);
    }

    public void applyAttributeModifiers(EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer) {
        AttributeModifiersComponent lv = this.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        if (!lv.modifiers().isEmpty()) {
            lv.applyModifiers(slot, attributeModifierConsumer);
        } else {
            this.getItem().getAttributeModifiers().applyModifiers(slot, attributeModifierConsumer);
        }
        EnchantmentHelper.applyAttributeModifiers(this, slot, attributeModifierConsumer);
    }

    public Text toHoverableText() {
        MutableText lv = Text.empty().append(this.getName());
        if (this.contains(DataComponentTypes.CUSTOM_NAME)) {
            lv.formatted(Formatting.ITALIC);
        }
        MutableText lv2 = Texts.bracketed(lv);
        if (!this.isEmpty()) {
            lv2.formatted(this.getRarity().getFormatting()).styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(this))));
        }
        return lv2;
    }

    public boolean canPlaceOn(CachedBlockPosition pos) {
        BlockPredicatesChecker lv = this.get(DataComponentTypes.CAN_PLACE_ON);
        return lv != null && lv.check(pos);
    }

    public boolean canBreak(CachedBlockPosition pos) {
        BlockPredicatesChecker lv = this.get(DataComponentTypes.CAN_BREAK);
        return lv != null && lv.check(pos);
    }

    public int getBobbingAnimationTime() {
        return this.bobbingAnimationTime;
    }

    public void setBobbingAnimationTime(int bobbingAnimationTime) {
        this.bobbingAnimationTime = bobbingAnimationTime;
    }

    public int getCount() {
        return this.isEmpty() ? 0 : this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void capCount(int maxCount) {
        if (!this.isEmpty() && this.getCount() > maxCount) {
            this.setCount(maxCount);
        }
    }

    public void increment(int amount) {
        this.setCount(this.getCount() + amount);
    }

    public void decrement(int amount) {
        this.increment(-amount);
    }

    public void decrementUnlessCreative(int amount, @Nullable LivingEntity entity) {
        if (entity == null || !entity.isInCreativeMode()) {
            this.decrement(amount);
        }
    }

    public ItemStack splitUnlessCreative(int amount, @Nullable LivingEntity entity) {
        ItemStack lv = this.copyWithCount(amount);
        this.decrementUnlessCreative(amount, entity);
        return lv;
    }

    public void usageTick(World world, LivingEntity user, int remainingUseTicks) {
        this.getItem().usageTick(world, user, this, remainingUseTicks);
    }

    public void onItemEntityDestroyed(ItemEntity entity) {
        this.getItem().onItemEntityDestroyed(entity);
    }

    public SoundEvent getDrinkSound() {
        return this.getItem().getDrinkSound();
    }

    public SoundEvent getEatSound() {
        return this.getItem().getEatSound();
    }

    public SoundEvent getBreakSound() {
        return this.getItem().getBreakSound();
    }

    public boolean takesDamageFrom(DamageSource source) {
        return !this.contains(DataComponentTypes.FIRE_RESISTANT) || !source.isIn(DamageTypeTags.IS_FIRE);
    }
}

