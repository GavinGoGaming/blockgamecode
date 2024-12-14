/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
class WorldScreenOptionGrid {
    private static final int BUTTON_WIDTH = 44;
    private final List<Option> options;

    WorldScreenOptionGrid(List<Option> options) {
        this.options = options;
    }

    public void refresh() {
        this.options.forEach(Option::refresh);
    }

    public static Builder builder(int width) {
        return new Builder(width);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        final int width;
        private final List<OptionBuilder> options = new ArrayList<OptionBuilder>();
        int marginLeft;
        int rowSpacing = 4;
        int rows;
        Optional<TooltipBoxDisplay> tooltipBoxDisplay = Optional.empty();

        public Builder(int width) {
            this.width = width;
        }

        void incrementRows() {
            ++this.rows;
        }

        public OptionBuilder add(Text text, BooleanSupplier getter, Consumer<Boolean> setter) {
            OptionBuilder lv = new OptionBuilder(text, getter, setter, 44);
            this.options.add(lv);
            return lv;
        }

        public Builder marginLeft(int marginLeft) {
            this.marginLeft = marginLeft;
            return this;
        }

        public Builder setRowSpacing(int rowSpacing) {
            this.rowSpacing = rowSpacing;
            return this;
        }

        public WorldScreenOptionGrid build(Consumer<Widget> widgetConsumer) {
            GridWidget lv = new GridWidget().setRowSpacing(this.rowSpacing);
            lv.add(EmptyWidget.ofWidth(this.width - 44), 0, 0);
            lv.add(EmptyWidget.ofWidth(44), 0, 1);
            ArrayList<Option> list = new ArrayList<Option>();
            this.rows = 0;
            for (OptionBuilder lv2 : this.options) {
                list.add(lv2.build(this, lv, 0));
            }
            lv.refreshPositions();
            widgetConsumer.accept(lv);
            WorldScreenOptionGrid lv3 = new WorldScreenOptionGrid(list);
            lv3.refresh();
            return lv3;
        }

        public Builder withTooltipBox(int maxInfoRows, boolean alwaysMaxHeight) {
            this.tooltipBoxDisplay = Optional.of(new TooltipBoxDisplay(maxInfoRows, alwaysMaxHeight));
            return this;
        }
    }

    @Environment(value=EnvType.CLIENT)
    record TooltipBoxDisplay(int maxInfoRows, boolean alwaysMaxHeight) {
    }

    @Environment(value=EnvType.CLIENT)
    record Option(CyclingButtonWidget<Boolean> button, BooleanSupplier getter, @Nullable BooleanSupplier toggleable) {
        public void refresh() {
            this.button.setValue(this.getter.getAsBoolean());
            if (this.toggleable != null) {
                this.button.active = this.toggleable.getAsBoolean();
            }
        }

        @Nullable
        public BooleanSupplier toggleable() {
            return this.toggleable;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class OptionBuilder {
        private final Text text;
        private final BooleanSupplier getter;
        private final Consumer<Boolean> setter;
        @Nullable
        private Text tooltip;
        @Nullable
        private BooleanSupplier toggleable;
        private final int buttonWidth;

        OptionBuilder(Text text, BooleanSupplier getter, Consumer<Boolean> setter, int buttonWidth) {
            this.text = text;
            this.getter = getter;
            this.setter = setter;
            this.buttonWidth = buttonWidth;
        }

        public OptionBuilder toggleable(BooleanSupplier toggleable) {
            this.toggleable = toggleable;
            return this;
        }

        public OptionBuilder tooltip(Text tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        Option build(Builder gridBuilder, GridWidget gridWidget, int row) {
            boolean bl;
            gridBuilder.incrementRows();
            TextWidget lv = new TextWidget(this.text, MinecraftClient.getInstance().textRenderer).alignLeft();
            gridWidget.add(lv, gridBuilder.rows, row, gridWidget.copyPositioner().relative(0.0f, 0.5f).marginLeft(gridBuilder.marginLeft));
            Optional<TooltipBoxDisplay> optional = gridBuilder.tooltipBoxDisplay;
            CyclingButtonWidget.Builder<Boolean> lv2 = CyclingButtonWidget.onOffBuilder(this.getter.getAsBoolean());
            lv2.omitKeyText();
            boolean bl2 = bl = this.tooltip != null && optional.isEmpty();
            if (bl) {
                Tooltip lv3 = Tooltip.of(this.tooltip);
                lv2.tooltip((T value) -> lv3);
            }
            if (this.tooltip != null && !bl) {
                lv2.narration(button -> ScreenTexts.joinSentences(this.text, button.getGenericNarrationMessage(), this.tooltip));
            } else {
                lv2.narration(button -> ScreenTexts.joinSentences(this.text, button.getGenericNarrationMessage()));
            }
            CyclingButtonWidget<Boolean> lv4 = lv2.build(0, 0, this.buttonWidth, 20, Text.empty(), (button, value) -> this.setter.accept((Boolean)value));
            if (this.toggleable != null) {
                lv4.active = this.toggleable.getAsBoolean();
            }
            gridWidget.add(lv4, gridBuilder.rows, row + 1, gridWidget.copyPositioner().alignRight());
            if (this.tooltip != null) {
                optional.ifPresent(tooltipBoxDisplay -> {
                    MutableText lv = this.tooltip.copy().formatted(Formatting.GRAY);
                    TextRenderer lv2 = MinecraftClient.getInstance().textRenderer;
                    MultilineTextWidget lv3 = new MultilineTextWidget(lv, lv2);
                    lv3.setMaxWidth(arg.width - arg.marginLeft - this.buttonWidth);
                    lv3.setMaxRows(tooltipBoxDisplay.maxInfoRows());
                    gridBuilder.incrementRows();
                    int j = tooltipBoxDisplay.alwaysMaxHeight ? lv2.fontHeight * tooltipBoxDisplay.maxInfoRows - lv3.getHeight() : 0;
                    gridWidget.add(lv3, arg.rows, row, gridWidget.copyPositioner().marginTop(-arg.rowSpacing).marginBottom(j));
                });
            }
            return new Option(lv4, this.getter, this.toggleable);
        }
    }
}

