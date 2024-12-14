/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.village;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.TradedItem;

public class TradeOffer {
    public static final Codec<TradeOffer> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)TradedItem.CODEC.fieldOf("buy")).forGetter(tradeOffer -> tradeOffer.firstBuyItem), TradedItem.CODEC.lenientOptionalFieldOf("buyB").forGetter(tradeOffer -> tradeOffer.secondBuyItem), ((MapCodec)ItemStack.CODEC.fieldOf("sell")).forGetter(tradeOffer -> tradeOffer.sellItem), Codec.INT.lenientOptionalFieldOf("uses", 0).forGetter(tradeOffer -> tradeOffer.uses), Codec.INT.lenientOptionalFieldOf("maxUses", 4).forGetter(tradeOffer -> tradeOffer.maxUses), Codec.BOOL.lenientOptionalFieldOf("rewardExp", true).forGetter(tradeOffer -> tradeOffer.rewardingPlayerExperience), Codec.INT.lenientOptionalFieldOf("specialPrice", 0).forGetter(tradeOffer -> tradeOffer.specialPrice), Codec.INT.lenientOptionalFieldOf("demand", 0).forGetter(tradeOffer -> tradeOffer.demandBonus), Codec.FLOAT.lenientOptionalFieldOf("priceMultiplier", Float.valueOf(0.0f)).forGetter(tradeOffer -> Float.valueOf(tradeOffer.priceMultiplier)), Codec.INT.lenientOptionalFieldOf("xp", 1).forGetter(tradeOffer -> tradeOffer.merchantExperience)).apply((Applicative<TradeOffer, ?>)instance, TradeOffer::new));
    public static final PacketCodec<RegistryByteBuf, TradeOffer> PACKET_CODEC = PacketCodec.ofStatic(TradeOffer::write, TradeOffer::read);
    private final TradedItem firstBuyItem;
    private final Optional<TradedItem> secondBuyItem;
    private final ItemStack sellItem;
    private int uses;
    private final int maxUses;
    private final boolean rewardingPlayerExperience;
    private int specialPrice;
    private int demandBonus;
    private final float priceMultiplier;
    private final int merchantExperience;

    private TradeOffer(TradedItem firstBuyItem, Optional<TradedItem> secondBuyItem, ItemStack sellItem, int uses, int maxUses, boolean rewardingPlayerExperience, int specialPrice, int demandBonus, float priceMultiplier, int merchantExperience) {
        this.firstBuyItem = firstBuyItem;
        this.secondBuyItem = secondBuyItem;
        this.sellItem = sellItem;
        this.uses = uses;
        this.maxUses = maxUses;
        this.rewardingPlayerExperience = rewardingPlayerExperience;
        this.specialPrice = specialPrice;
        this.demandBonus = demandBonus;
        this.priceMultiplier = priceMultiplier;
        this.merchantExperience = merchantExperience;
    }

    public TradeOffer(TradedItem buyItem, ItemStack sellItem, int maxUses, int merchantExperience, float priceMultiplier) {
        this(buyItem, Optional.empty(), sellItem, maxUses, merchantExperience, priceMultiplier);
    }

    public TradeOffer(TradedItem firstBuyItem, Optional<TradedItem> secondBuyItem, ItemStack sellItem, int maxUses, int merchantExperience, float priceMultiplier) {
        this(firstBuyItem, secondBuyItem, sellItem, 0, maxUses, merchantExperience, priceMultiplier);
    }

    public TradeOffer(TradedItem firstBuyItem, Optional<TradedItem> secondBuyItem, ItemStack sellItem, int uses, int maxUses, int merchantExperience, float priceMultiplier) {
        this(firstBuyItem, secondBuyItem, sellItem, uses, maxUses, merchantExperience, priceMultiplier, 0);
    }

    public TradeOffer(TradedItem firstBuyItem, Optional<TradedItem> secondBuyItem, ItemStack sellItem, int uses, int maxUses, int merchantExperience, float priceMultiplier, int demandBonus) {
        this(firstBuyItem, secondBuyItem, sellItem, uses, maxUses, true, 0, demandBonus, priceMultiplier, merchantExperience);
    }

    private TradeOffer(TradeOffer offer) {
        this(offer.firstBuyItem, offer.secondBuyItem, offer.sellItem.copy(), offer.uses, offer.maxUses, offer.rewardingPlayerExperience, offer.specialPrice, offer.demandBonus, offer.priceMultiplier, offer.merchantExperience);
    }

    public ItemStack getOriginalFirstBuyItem() {
        return this.firstBuyItem.itemStack();
    }

    public ItemStack getDisplayedFirstBuyItem() {
        return this.firstBuyItem.itemStack().copyWithCount(this.getFirstBuyItemCount(this.firstBuyItem));
    }

    private int getFirstBuyItemCount(TradedItem firstBuyItem) {
        int i = firstBuyItem.count();
        int j = Math.max(0, MathHelper.floor((float)(i * this.demandBonus) * this.priceMultiplier));
        return MathHelper.clamp(i + j + this.specialPrice, 1, firstBuyItem.itemStack().getMaxCount());
    }

    public ItemStack getDisplayedSecondBuyItem() {
        return this.secondBuyItem.map(TradedItem::itemStack).orElse(ItemStack.EMPTY);
    }

    public TradedItem getFirstBuyItem() {
        return this.firstBuyItem;
    }

    public Optional<TradedItem> getSecondBuyItem() {
        return this.secondBuyItem;
    }

    public ItemStack getSellItem() {
        return this.sellItem;
    }

    public void updateDemandBonus() {
        this.demandBonus = this.demandBonus + this.uses - (this.maxUses - this.uses);
    }

    public ItemStack copySellItem() {
        return this.sellItem.copy();
    }

    public int getUses() {
        return this.uses;
    }

    public void resetUses() {
        this.uses = 0;
    }

    public int getMaxUses() {
        return this.maxUses;
    }

    public void use() {
        ++this.uses;
    }

    public int getDemandBonus() {
        return this.demandBonus;
    }

    public void increaseSpecialPrice(int increment) {
        this.specialPrice += increment;
    }

    public void clearSpecialPrice() {
        this.specialPrice = 0;
    }

    public int getSpecialPrice() {
        return this.specialPrice;
    }

    public void setSpecialPrice(int specialPrice) {
        this.specialPrice = specialPrice;
    }

    public float getPriceMultiplier() {
        return this.priceMultiplier;
    }

    public int getMerchantExperience() {
        return this.merchantExperience;
    }

    public boolean isDisabled() {
        return this.uses >= this.maxUses;
    }

    public void disable() {
        this.uses = this.maxUses;
    }

    public boolean hasBeenUsed() {
        return this.uses > 0;
    }

    public boolean shouldRewardPlayerExperience() {
        return this.rewardingPlayerExperience;
    }

    public boolean matchesBuyItems(ItemStack stack, ItemStack buyItem) {
        if (!this.firstBuyItem.matches(stack) || stack.getCount() < this.getFirstBuyItemCount(this.firstBuyItem)) {
            return false;
        }
        if (this.secondBuyItem.isPresent()) {
            return this.secondBuyItem.get().matches(buyItem) && buyItem.getCount() >= this.secondBuyItem.get().count();
        }
        return buyItem.isEmpty();
    }

    public boolean depleteBuyItems(ItemStack firstBuyStack, ItemStack secondBuyStack) {
        if (!this.matchesBuyItems(firstBuyStack, secondBuyStack)) {
            return false;
        }
        firstBuyStack.decrement(this.getDisplayedFirstBuyItem().getCount());
        if (!this.getDisplayedSecondBuyItem().isEmpty()) {
            secondBuyStack.decrement(this.getDisplayedSecondBuyItem().getCount());
        }
        return true;
    }

    public TradeOffer copy() {
        return new TradeOffer(this);
    }

    private static void write(RegistryByteBuf buf, TradeOffer offer) {
        TradedItem.PACKET_CODEC.encode(buf, offer.getFirstBuyItem());
        ItemStack.PACKET_CODEC.encode(buf, offer.getSellItem());
        TradedItem.OPTIONAL_PACKET_CODEC.encode(buf, offer.getSecondBuyItem());
        buf.writeBoolean(offer.isDisabled());
        buf.writeInt(offer.getUses());
        buf.writeInt(offer.getMaxUses());
        buf.writeInt(offer.getMerchantExperience());
        buf.writeInt(offer.getSpecialPrice());
        buf.writeFloat(offer.getPriceMultiplier());
        buf.writeInt(offer.getDemandBonus());
    }

    public static TradeOffer read(RegistryByteBuf buf) {
        TradedItem lv = (TradedItem)TradedItem.PACKET_CODEC.decode(buf);
        ItemStack lv2 = (ItemStack)ItemStack.PACKET_CODEC.decode(buf);
        Optional optional = (Optional)TradedItem.OPTIONAL_PACKET_CODEC.decode(buf);
        boolean bl = buf.readBoolean();
        int i = buf.readInt();
        int j = buf.readInt();
        int k = buf.readInt();
        int l = buf.readInt();
        float f = buf.readFloat();
        int m = buf.readInt();
        TradeOffer lv3 = new TradeOffer(lv, optional, lv2, i, j, k, f, m);
        if (bl) {
            lv3.disable();
        }
        lv3.setSpecialPrice(l);
        return lv3;
    }
}

