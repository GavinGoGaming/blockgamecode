/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.widget;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class OptionListWidget
extends ElementListWidget<WidgetEntry> {
    private static final int field_49481 = 310;
    private static final int field_49482 = 25;
    private final GameOptionsScreen optionsScreen;

    public OptionListWidget(MinecraftClient client, int width, GameOptionsScreen optionsScreen) {
        super(client, width, optionsScreen.layout.getContentHeight(), optionsScreen.layout.getHeaderHeight(), 25);
        this.centerListVertically = false;
        this.optionsScreen = optionsScreen;
    }

    public void addSingleOptionEntry(SimpleOption<?> option) {
        this.addEntry(OptionWidgetEntry.create(this.client.options, option, this.optionsScreen));
    }

    public void addAll(SimpleOption<?> ... options) {
        for (int i = 0; i < options.length; i += 2) {
            SimpleOption<?> lv = i < options.length - 1 ? options[i + 1] : null;
            this.addEntry(OptionWidgetEntry.create(this.client.options, options[i], lv, this.optionsScreen));
        }
    }

    public void addAll(List<ClickableWidget> widgets) {
        for (int i = 0; i < widgets.size(); i += 2) {
            this.addWidgetEntry(widgets.get(i), i < widgets.size() - 1 ? widgets.get(i + 1) : null);
        }
    }

    public void addWidgetEntry(ClickableWidget firstWidget, @Nullable ClickableWidget secondWidget) {
        this.addEntry(WidgetEntry.create(firstWidget, secondWidget, this.optionsScreen));
    }

    @Override
    public int getRowWidth() {
        return 310;
    }

    @Nullable
    public ClickableWidget getWidgetFor(SimpleOption<?> option) {
        for (WidgetEntry lv : this.children()) {
            if (!(lv instanceof OptionWidgetEntry)) continue;
            OptionWidgetEntry lv2 = (OptionWidgetEntry)lv;
            ClickableWidget lv3 = lv2.optionWidgets.get(option);
            if (lv3 == null) continue;
            return lv3;
        }
        return null;
    }

    public void applyAllPendingValues() {
        for (WidgetEntry lv : this.children()) {
            if (!(lv instanceof OptionWidgetEntry)) continue;
            OptionWidgetEntry lv2 = (OptionWidgetEntry)lv;
            for (ClickableWidget lv3 : lv2.optionWidgets.values()) {
                if (!(lv3 instanceof SimpleOption.OptionSliderWidgetImpl)) continue;
                SimpleOption.OptionSliderWidgetImpl lv4 = (SimpleOption.OptionSliderWidgetImpl)lv3;
                lv4.applyPendingValue();
            }
        }
    }

    public Optional<Element> getHoveredWidget(double mouseX, double mouseY) {
        for (WidgetEntry lv : this.children()) {
            for (Element element : lv.children()) {
                if (!element.isMouseOver(mouseX, mouseY)) continue;
                return Optional.of(element);
            }
        }
        return Optional.empty();
    }

    @Environment(value=EnvType.CLIENT)
    protected static class OptionWidgetEntry
    extends WidgetEntry {
        final Map<SimpleOption<?>, ClickableWidget> optionWidgets;

        private OptionWidgetEntry(Map<SimpleOption<?>, ClickableWidget> widgets, GameOptionsScreen optionsScreen) {
            super(ImmutableList.copyOf(widgets.values()), optionsScreen);
            this.optionWidgets = widgets;
        }

        public static OptionWidgetEntry create(GameOptions gameOptions, SimpleOption<?> option, GameOptionsScreen optionsScreen) {
            return new OptionWidgetEntry(ImmutableMap.of(option, option.createWidget(gameOptions, 0, 0, 310)), optionsScreen);
        }

        public static OptionWidgetEntry create(GameOptions gameOptions, SimpleOption<?> firstOption, @Nullable SimpleOption<?> secondOption, GameOptionsScreen optionsScreen) {
            ClickableWidget lv = firstOption.createWidget(gameOptions);
            if (secondOption == null) {
                return new OptionWidgetEntry(ImmutableMap.of(firstOption, lv), optionsScreen);
            }
            return new OptionWidgetEntry(ImmutableMap.of(firstOption, lv, secondOption, secondOption.createWidget(gameOptions)), optionsScreen);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class WidgetEntry
    extends ElementListWidget.Entry<WidgetEntry> {
        private final List<ClickableWidget> widgets;
        private final Screen screen;
        private static final int WIDGET_X_SPACING = 160;

        WidgetEntry(List<ClickableWidget> widgets, Screen screen) {
            this.widgets = ImmutableList.copyOf(widgets);
            this.screen = screen;
        }

        public static WidgetEntry create(List<ClickableWidget> widgets, Screen screen) {
            return new WidgetEntry(widgets, screen);
        }

        public static WidgetEntry create(ClickableWidget firstWidget, @Nullable ClickableWidget secondWidget, Screen screen) {
            if (secondWidget == null) {
                return new WidgetEntry(ImmutableList.of(firstWidget), screen);
            }
            return new WidgetEntry(ImmutableList.of(firstWidget, secondWidget), screen);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int p = 0;
            int q = this.screen.width / 2 - 155;
            for (ClickableWidget lv : this.widgets) {
                lv.setPosition(q + p, y);
                lv.render(context, mouseX, mouseY, tickDelta);
                p += 160;
            }
        }

        @Override
        public List<? extends Element> children() {
            return this.widgets;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return this.widgets;
        }
    }
}

