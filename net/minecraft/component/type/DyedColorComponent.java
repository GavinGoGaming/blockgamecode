/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ColorHelper;

public record DyedColorComponent(int rgb, boolean showInTooltip) implements TooltipAppender
{
    private static final Codec<DyedColorComponent> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("rgb")).forGetter(DyedColorComponent::rgb), Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter(DyedColorComponent::showInTooltip)).apply((Applicative<DyedColorComponent, ?>)instance, DyedColorComponent::new));
    public static final Codec<DyedColorComponent> CODEC = Codec.withAlternative(BASE_CODEC, Codec.INT, rgb -> new DyedColorComponent((int)rgb, true));
    public static final PacketCodec<ByteBuf, DyedColorComponent> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, DyedColorComponent::rgb, PacketCodecs.BOOL, DyedColorComponent::showInTooltip, DyedColorComponent::new);
    public static final int DEFAULT_COLOR = -6265536;

    public static int getColor(ItemStack stack, int defaultColor) {
        DyedColorComponent lv = stack.get(DataComponentTypes.DYED_COLOR);
        return lv != null ? ColorHelper.Argb.fullAlpha(lv.rgb()) : defaultColor;
    }

    public static ItemStack setColor(ItemStack stack, List<DyeItem> dyes) {
        int s;
        int p;
        int o;
        if (!stack.isIn(ItemTags.DYEABLE)) {
            return ItemStack.EMPTY;
        }
        ItemStack lv = stack.copyWithCount(1);
        int i = 0;
        int j = 0;
        int k = 0;
        int l = 0;
        int m = 0;
        DyedColorComponent lv2 = lv.get(DataComponentTypes.DYED_COLOR);
        if (lv2 != null) {
            int n = ColorHelper.Argb.getRed(lv2.rgb());
            o = ColorHelper.Argb.getGreen(lv2.rgb());
            p = ColorHelper.Argb.getBlue(lv2.rgb());
            l += Math.max(n, Math.max(o, p));
            i += n;
            j += o;
            k += p;
            ++m;
        }
        for (DyeItem lv3 : dyes) {
            p = lv3.getColor().getEntityColor();
            int q = ColorHelper.Argb.getRed(p);
            int r = ColorHelper.Argb.getGreen(p);
            s = ColorHelper.Argb.getBlue(p);
            l += Math.max(q, Math.max(r, s));
            i += q;
            j += r;
            k += s;
            ++m;
        }
        int n = i / m;
        o = j / m;
        p = k / m;
        float f = (float)l / (float)m;
        float g = Math.max(n, Math.max(o, p));
        n = (int)((float)n * f / g);
        o = (int)((float)o * f / g);
        p = (int)((float)p * f / g);
        s = ColorHelper.Argb.getArgb(0, n, o, p);
        boolean bl = lv2 == null || lv2.showInTooltip();
        lv.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(s, bl));
        return lv;
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
        if (!this.showInTooltip) {
            return;
        }
        if (type.isAdvanced()) {
            tooltip.accept(Text.translatable("item.color", String.format(Locale.ROOT, "#%06X", this.rgb)).formatted(Formatting.GRAY));
        } else {
            tooltip.accept(Text.translatable("item.dyed").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
    }

    public DyedColorComponent withShowInTooltip(boolean showInTooltip) {
        return new DyedColorComponent(this.rgb, showInTooltip);
    }
}

