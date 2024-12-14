/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Instrument;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class GoatHornItem
extends Item {
    private final TagKey<Instrument> instrumentTag;

    public GoatHornItem(Item.Settings settings, TagKey<Instrument> instrumentTag) {
        super(settings);
        this.instrumentTag = instrumentTag;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        Optional optional = this.getInstrument(stack).flatMap(RegistryEntry::getKey);
        if (optional.isPresent()) {
            MutableText lv = Text.translatable(Util.createTranslationKey("instrument", ((RegistryKey)optional.get()).getValue()));
            tooltip.add(lv.formatted(Formatting.GRAY));
        }
    }

    public static ItemStack getStackForInstrument(Item item, RegistryEntry<Instrument> instrument) {
        ItemStack lv = new ItemStack(item);
        lv.set(DataComponentTypes.INSTRUMENT, instrument);
        return lv;
    }

    public static void setRandomInstrumentFromTag(ItemStack stack, TagKey<Instrument> instrumentTag, Random random) {
        Optional<RegistryEntry<Instrument>> optional = Registries.INSTRUMENT.getRandomEntry(instrumentTag, random);
        optional.ifPresent(instrument -> stack.set(DataComponentTypes.INSTRUMENT, instrument));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        Optional<RegistryEntry<Instrument>> optional = this.getInstrument(lv);
        if (optional.isPresent()) {
            Instrument lv2 = optional.get().value();
            user.setCurrentHand(hand);
            GoatHornItem.playSound(world, user, lv2);
            user.getItemCooldownManager().set(this, lv2.useDuration());
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            return TypedActionResult.consume(lv);
        }
        return TypedActionResult.fail(lv);
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        Optional<RegistryEntry<Instrument>> optional = this.getInstrument(stack);
        return optional.map(instrument -> ((Instrument)instrument.value()).useDuration()).orElse(0);
    }

    private Optional<RegistryEntry<Instrument>> getInstrument(ItemStack stack) {
        RegistryEntry<Instrument> lv = stack.get(DataComponentTypes.INSTRUMENT);
        if (lv != null) {
            return Optional.of(lv);
        }
        Iterator<RegistryEntry<Instrument>> iterator = Registries.INSTRUMENT.iterateEntries(this.instrumentTag).iterator();
        if (iterator.hasNext()) {
            return Optional.of(iterator.next());
        }
        return Optional.empty();
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.TOOT_HORN;
    }

    private static void playSound(World world, PlayerEntity player, Instrument instrument) {
        SoundEvent lv = instrument.soundEvent().value();
        float f = instrument.range() / 16.0f;
        world.playSoundFromEntity(player, player, lv, SoundCategory.RECORDS, f, 1.0f);
        world.emitGameEvent(GameEvent.INSTRUMENT_PLAY, player.getPos(), GameEvent.Emitter.of(player));
    }
}

