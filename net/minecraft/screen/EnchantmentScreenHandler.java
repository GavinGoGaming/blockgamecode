/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Blocks;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class EnchantmentScreenHandler
extends ScreenHandler {
    static final Identifier EMPTY_LAPIS_SLOT_TEXTURE = Identifier.ofVanilla("item/empty_slot_lapis_lazuli");
    private final Inventory inventory = new SimpleInventory(2){

        @Override
        public void markDirty() {
            super.markDirty();
            EnchantmentScreenHandler.this.onContentChanged(this);
        }
    };
    private final ScreenHandlerContext context;
    private final Random random = Random.create();
    private final Property seed = Property.create();
    public final int[] enchantmentPower = new int[3];
    public final int[] enchantmentId = new int[]{-1, -1, -1};
    public final int[] enchantmentLevel = new int[]{-1, -1, -1};

    public EnchantmentScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public EnchantmentScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ScreenHandlerType.ENCHANTMENT, syncId);
        int j;
        this.context = context;
        this.addSlot(new Slot(this, this.inventory, 0, 15, 47){

            @Override
            public int getMaxItemCount() {
                return 1;
            }
        });
        this.addSlot(new Slot(this, this.inventory, 1, 35, 47){

            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.LAPIS_LAZULI);
            }

            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, EMPTY_LAPIS_SLOT_TEXTURE);
            }
        });
        for (j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
            }
        }
        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 142));
        }
        this.addProperty(Property.create(this.enchantmentPower, 0));
        this.addProperty(Property.create(this.enchantmentPower, 1));
        this.addProperty(Property.create(this.enchantmentPower, 2));
        this.addProperty(this.seed).set(playerInventory.player.getEnchantmentTableSeed());
        this.addProperty(Property.create(this.enchantmentId, 0));
        this.addProperty(Property.create(this.enchantmentId, 1));
        this.addProperty(Property.create(this.enchantmentId, 2));
        this.addProperty(Property.create(this.enchantmentLevel, 0));
        this.addProperty(Property.create(this.enchantmentLevel, 1));
        this.addProperty(Property.create(this.enchantmentLevel, 2));
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        if (inventory == this.inventory) {
            ItemStack lv = inventory.getStack(0);
            if (lv.isEmpty() || !lv.isEnchantable()) {
                for (int i = 0; i < 3; ++i) {
                    this.enchantmentPower[i] = 0;
                    this.enchantmentId[i] = -1;
                    this.enchantmentLevel[i] = -1;
                }
            } else {
                this.context.run((world, pos) -> {
                    int j;
                    IndexedIterable<RegistryEntry<Enchantment>> lv = world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getIndexedEntries();
                    int i = 0;
                    for (BlockPos lv2 : EnchantingTableBlock.POWER_PROVIDER_OFFSETS) {
                        if (!EnchantingTableBlock.canAccessPowerProvider(world, pos, lv2)) continue;
                        ++i;
                    }
                    this.random.setSeed(this.seed.get());
                    for (j = 0; j < 3; ++j) {
                        this.enchantmentPower[j] = EnchantmentHelper.calculateRequiredExperienceLevel(this.random, j, i, lv);
                        this.enchantmentId[j] = -1;
                        this.enchantmentLevel[j] = -1;
                        if (this.enchantmentPower[j] >= j + 1) continue;
                        this.enchantmentPower[j] = 0;
                    }
                    for (j = 0; j < 3; ++j) {
                        List<EnchantmentLevelEntry> list;
                        if (this.enchantmentPower[j] <= 0 || (list = this.generateEnchantments(world.getRegistryManager(), lv, j, this.enchantmentPower[j])) == null || list.isEmpty()) continue;
                        EnchantmentLevelEntry lv3 = list.get(this.random.nextInt(list.size()));
                        this.enchantmentId[j] = lv.getRawId(lv3.enchantment);
                        this.enchantmentLevel[j] = lv3.level;
                    }
                    this.sendContentUpdates();
                });
            }
        }
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id < 0 || id >= this.enchantmentPower.length) {
            Util.error(String.valueOf(player.getName()) + " pressed invalid button id: " + id);
            return false;
        }
        ItemStack lv = this.inventory.getStack(0);
        ItemStack lv2 = this.inventory.getStack(1);
        int j = id + 1;
        if ((lv2.isEmpty() || lv2.getCount() < j) && !player.isInCreativeMode()) {
            return false;
        }
        if (this.enchantmentPower[id] > 0 && !lv.isEmpty() && (player.experienceLevel >= j && player.experienceLevel >= this.enchantmentPower[id] || player.getAbilities().creativeMode)) {
            this.context.run((world, pos) -> {
                ItemStack lv = lv;
                List<EnchantmentLevelEntry> list = this.generateEnchantments(world.getRegistryManager(), lv, id, this.enchantmentPower[id]);
                if (!list.isEmpty()) {
                    player.applyEnchantmentCosts(lv, j);
                    if (lv.isOf(Items.BOOK)) {
                        lv = lv.withItem(Items.ENCHANTED_BOOK);
                        this.inventory.setStack(0, lv);
                    }
                    for (EnchantmentLevelEntry lv2 : list) {
                        lv.addEnchantment(lv2.enchantment, lv2.level);
                    }
                    lv2.decrementUnlessCreative(j, player);
                    if (lv2.isEmpty()) {
                        this.inventory.setStack(1, ItemStack.EMPTY);
                    }
                    player.incrementStat(Stats.ENCHANT_ITEM);
                    if (player instanceof ServerPlayerEntity) {
                        Criteria.ENCHANTED_ITEM.trigger((ServerPlayerEntity)player, lv, j);
                    }
                    this.inventory.markDirty();
                    this.seed.set(player.getEnchantmentTableSeed());
                    this.onContentChanged(this.inventory);
                    world.playSound(null, (BlockPos)pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0f, world.random.nextFloat() * 0.1f + 0.9f);
                }
            });
            return true;
        }
        return false;
    }

    private List<EnchantmentLevelEntry> generateEnchantments(DynamicRegistryManager registryManager, ItemStack stack, int slot, int level) {
        this.random.setSeed(this.seed.get() + slot);
        Optional<RegistryEntryList.Named<Enchantment>> optional = registryManager.get(RegistryKeys.ENCHANTMENT).getEntryList(EnchantmentTags.IN_ENCHANTING_TABLE);
        if (optional.isEmpty()) {
            return List.of();
        }
        List<EnchantmentLevelEntry> list = EnchantmentHelper.generateEnchantments(this.random, stack, level, optional.get().stream());
        if (stack.isOf(Items.BOOK) && list.size() > 1) {
            list.remove(this.random.nextInt(list.size()));
        }
        return list;
    }

    public int getLapisCount() {
        ItemStack lv = this.inventory.getStack(1);
        if (lv.isEmpty()) {
            return 0;
        }
        return lv.getCount();
    }

    public int getSeed() {
        return this.seed.get();
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> this.dropInventory(player, this.inventory));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return EnchantmentScreenHandler.canUse(this.context, player, Blocks.ENCHANTING_TABLE);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack lv = ItemStack.EMPTY;
        Slot lv2 = (Slot)this.slots.get(slot);
        if (lv2 != null && lv2.hasStack()) {
            ItemStack lv3 = lv2.getStack();
            lv = lv3.copy();
            if (slot == 0) {
                if (!this.insertItem(lv3, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slot == 1) {
                if (!this.insertItem(lv3, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (lv3.isOf(Items.LAPIS_LAZULI)) {
                if (!this.insertItem(lv3, 1, 2, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!((Slot)this.slots.get(0)).hasStack() && ((Slot)this.slots.get(0)).canInsert(lv3)) {
                ItemStack lv4 = lv3.copyWithCount(1);
                lv3.decrement(1);
                ((Slot)this.slots.get(0)).setStack(lv4);
            } else {
                return ItemStack.EMPTY;
            }
            if (lv3.isEmpty()) {
                lv2.setStack(ItemStack.EMPTY);
            } else {
                lv2.markDirty();
            }
            if (lv3.getCount() == lv.getCount()) {
                return ItemStack.EMPTY;
            }
            lv2.onTakeItem(player, lv3);
        }
        return lv;
    }
}

