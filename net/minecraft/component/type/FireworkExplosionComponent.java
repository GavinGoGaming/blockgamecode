/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public record FireworkExplosionComponent(Type shape, IntList colors, IntList fadeColors, boolean hasTrail, boolean hasTwinkle) implements TooltipAppender
{
    public static final FireworkExplosionComponent DEFAULT = new FireworkExplosionComponent(Type.SMALL_BALL, IntList.of(), IntList.of(), false, false);
    public static final Codec<IntList> COLORS_CODEC = Codec.INT.listOf().xmap(IntArrayList::new, ArrayList::new);
    public static final Codec<FireworkExplosionComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Type.CODEC.fieldOf("shape")).forGetter(FireworkExplosionComponent::shape), COLORS_CODEC.optionalFieldOf("colors", IntList.of()).forGetter(FireworkExplosionComponent::colors), COLORS_CODEC.optionalFieldOf("fade_colors", IntList.of()).forGetter(FireworkExplosionComponent::fadeColors), Codec.BOOL.optionalFieldOf("has_trail", false).forGetter(FireworkExplosionComponent::hasTrail), Codec.BOOL.optionalFieldOf("has_twinkle", false).forGetter(FireworkExplosionComponent::hasTwinkle)).apply((Applicative<FireworkExplosionComponent, ?>)instance, FireworkExplosionComponent::new));
    private static final PacketCodec<ByteBuf, IntList> COLORS_PACKET_CODEC = PacketCodecs.INTEGER.collect(PacketCodecs.toList()).xmap(IntArrayList::new, ArrayList::new);
    public static final PacketCodec<ByteBuf, FireworkExplosionComponent> PACKET_CODEC = PacketCodec.tuple(Type.PACKET_CODEC, FireworkExplosionComponent::shape, COLORS_PACKET_CODEC, FireworkExplosionComponent::colors, COLORS_PACKET_CODEC, FireworkExplosionComponent::fadeColors, PacketCodecs.BOOL, FireworkExplosionComponent::hasTrail, PacketCodecs.BOOL, FireworkExplosionComponent::hasTwinkle, FireworkExplosionComponent::new);
    private static final Text CUSTOM_COLOR_TEXT = Text.translatable("item.minecraft.firework_star.custom_color");

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
        this.appendShapeTooltip(tooltip);
        this.appendOptionalTooltip(tooltip);
    }

    public void appendShapeTooltip(Consumer<Text> textConsumer) {
        textConsumer.accept(this.shape.getName().formatted(Formatting.GRAY));
    }

    public void appendOptionalTooltip(Consumer<Text> textConsumer) {
        if (!this.colors.isEmpty()) {
            textConsumer.accept(FireworkExplosionComponent.appendColorsTooltipText(Text.empty().formatted(Formatting.GRAY), this.colors));
        }
        if (!this.fadeColors.isEmpty()) {
            textConsumer.accept(FireworkExplosionComponent.appendColorsTooltipText(Text.translatable("item.minecraft.firework_star.fade_to").append(ScreenTexts.SPACE).formatted(Formatting.GRAY), this.fadeColors));
        }
        if (this.hasTrail) {
            textConsumer.accept(Text.translatable("item.minecraft.firework_star.trail").formatted(Formatting.GRAY));
        }
        if (this.hasTwinkle) {
            textConsumer.accept(Text.translatable("item.minecraft.firework_star.flicker").formatted(Formatting.GRAY));
        }
    }

    private static Text appendColorsTooltipText(MutableText text, IntList colors) {
        for (int i = 0; i < colors.size(); ++i) {
            if (i > 0) {
                text.append(", ");
            }
            text.append(FireworkExplosionComponent.getColorText(colors.getInt(i)));
        }
        return text;
    }

    private static Text getColorText(int color) {
        DyeColor lv = DyeColor.byFireworkColor(color);
        if (lv == null) {
            return CUSTOM_COLOR_TEXT;
        }
        return Text.translatable("item.minecraft.firework_star." + lv.getName());
    }

    public FireworkExplosionComponent withFadeColors(IntList fadeColors) {
        return new FireworkExplosionComponent(this.shape, this.colors, new IntArrayList(fadeColors), this.hasTrail, this.hasTwinkle);
    }

    public static enum Type implements StringIdentifiable
    {
        SMALL_BALL(0, "small_ball"),
        LARGE_BALL(1, "large_ball"),
        STAR(2, "star"),
        CREEPER(3, "creeper"),
        BURST(4, "burst");

        private static final IntFunction<Type> BY_ID;
        public static final PacketCodec<ByteBuf, Type> PACKET_CODEC;
        public static final Codec<Type> CODEC;
        private final int id;
        private final String name;

        private Type(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public MutableText getName() {
            return Text.translatable("item.minecraft.firework_star.shape." + this.name);
        }

        public int getId() {
            return this.id;
        }

        public static Type byId(int id) {
            return BY_ID.apply(id);
        }

        @Override
        public String asString() {
            return this.name;
        }

        static {
            BY_ID = ValueLists.createIdToValueFunction(Type::getId, Type.values(), ValueLists.OutOfBoundsHandling.ZERO);
            PACKET_CODEC = PacketCodecs.indexed(BY_ID, Type::getId);
            CODEC = StringIdentifiable.createBasicCodec(Type::values);
        }
    }
}

