/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.option;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ControlsListWidget
extends ElementListWidget<Entry> {
    private static final int field_49533 = 20;
    final KeybindsScreen parent;
    private int maxKeyNameLength;

    public ControlsListWidget(KeybindsScreen parent, MinecraftClient client) {
        super(client, parent.width, parent.layout.getContentHeight(), parent.layout.getHeaderHeight(), 20);
        this.parent = parent;
        Object[] lvs = ArrayUtils.clone(client.options.allKeys);
        Arrays.sort(lvs);
        String string = null;
        for (Object lv : lvs) {
            MutableText lv2;
            int i;
            String string2 = ((KeyBinding)lv).getCategory();
            if (!string2.equals(string)) {
                string = string2;
                this.addEntry(new CategoryEntry(Text.translatable(string2)));
            }
            if ((i = client.textRenderer.getWidth(lv2 = Text.translatable(((KeyBinding)lv).getTranslationKey()))) > this.maxKeyNameLength) {
                this.maxKeyNameLength = i;
            }
            this.addEntry(new KeyBindingEntry((KeyBinding)lv, lv2));
        }
    }

    public void update() {
        KeyBinding.updateKeysByCode();
        this.updateChildren();
    }

    public void updateChildren() {
        this.children().forEach(Entry::update);
    }

    @Override
    public int getRowWidth() {
        return 340;
    }

    @Environment(value=EnvType.CLIENT)
    public class CategoryEntry
    extends Entry {
        final Text text;
        private final int textWidth;

        public CategoryEntry(Text text) {
            this.text = text;
            this.textWidth = ((ControlsListWidget)ControlsListWidget.this).client.textRenderer.getWidth(this.text);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawText(((ControlsListWidget)ControlsListWidget.this).client.textRenderer, this.text, ControlsListWidget.this.width / 2 - this.textWidth / 2, y + entryHeight - ((ControlsListWidget)ControlsListWidget.this).client.textRenderer.fontHeight - 1, Colors.WHITE, false);
        }

        @Override
        @Nullable
        public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
            return null;
        }

        @Override
        public List<? extends Element> children() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return ImmutableList.of(new Selectable(){

                @Override
                public Selectable.SelectionType getType() {
                    return Selectable.SelectionType.HOVERED;
                }

                @Override
                public void appendNarrations(NarrationMessageBuilder builder) {
                    builder.put(NarrationPart.TITLE, CategoryEntry.this.text);
                }
            });
        }

        @Override
        protected void update() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class KeyBindingEntry
    extends Entry {
        private static final Text RESET_TEXT = Text.translatable("controls.reset");
        private static final int field_49535 = 10;
        private final KeyBinding binding;
        private final Text bindingName;
        private final ButtonWidget editButton;
        private final ButtonWidget resetButton;
        private boolean duplicate = false;

        KeyBindingEntry(KeyBinding binding, Text bindingName) {
            this.binding = binding;
            this.bindingName = bindingName;
            this.editButton = ButtonWidget.builder(bindingName, button -> {
                ControlsListWidget.this.parent.selectedKeyBinding = binding;
                ControlsListWidget.this.update();
            }).dimensions(0, 0, 75, 20).narrationSupplier(textSupplier -> {
                if (binding.isUnbound()) {
                    return Text.translatable("narrator.controls.unbound", bindingName);
                }
                return Text.translatable("narrator.controls.bound", bindingName, textSupplier.get());
            }).build();
            this.resetButton = ButtonWidget.builder(RESET_TEXT, button -> {
                ((ControlsListWidget)ControlsListWidget.this).client.options.setKeyCode(binding, binding.getDefaultKey());
                ControlsListWidget.this.update();
            }).dimensions(0, 0, 50, 20).narrationSupplier(textSupplier -> Text.translatable("narrator.controls.reset", bindingName)).build();
            this.update();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int p = ControlsListWidget.this.getScrollbarX() - this.resetButton.getWidth() - 10;
            int q = y - 2;
            this.resetButton.setPosition(p, q);
            this.resetButton.render(context, mouseX, mouseY, tickDelta);
            int r = p - 5 - this.editButton.getWidth();
            this.editButton.setPosition(r, q);
            this.editButton.render(context, mouseX, mouseY, tickDelta);
            context.drawTextWithShadow(((ControlsListWidget)ControlsListWidget.this).client.textRenderer, this.bindingName, x, y + entryHeight / 2 - ((ControlsListWidget)ControlsListWidget.this).client.textRenderer.fontHeight / 2, Colors.WHITE);
            if (this.duplicate) {
                int s = 3;
                int t = this.editButton.getX() - 6;
                context.fill(t, y - 1, t + 3, y + entryHeight, Colors.RED);
            }
        }

        @Override
        public List<? extends Element> children() {
            return ImmutableList.of(this.editButton, this.resetButton);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return ImmutableList.of(this.editButton, this.resetButton);
        }

        @Override
        protected void update() {
            this.editButton.setMessage(this.binding.getBoundKeyLocalizedText());
            this.resetButton.active = !this.binding.isDefault();
            this.duplicate = false;
            MutableText lv = Text.empty();
            if (!this.binding.isUnbound()) {
                for (KeyBinding lv2 : ((ControlsListWidget)ControlsListWidget.this).client.options.allKeys) {
                    if (lv2 == this.binding || !this.binding.equals(lv2)) continue;
                    if (this.duplicate) {
                        lv.append(", ");
                    }
                    this.duplicate = true;
                    lv.append(Text.translatable(lv2.getTranslationKey()));
                }
            }
            if (this.duplicate) {
                this.editButton.setMessage(Text.literal("[ ").append(this.editButton.getMessage().copy().formatted(Formatting.WHITE)).append(" ]").formatted(Formatting.RED));
                this.editButton.setTooltip(Tooltip.of(Text.translatable("controls.keybinds.duplicateKeybinds", lv)));
            } else {
                this.editButton.setTooltip(null);
            }
            if (ControlsListWidget.this.parent.selectedKeyBinding == this.binding) {
                this.editButton.setMessage(Text.literal("> ").append(this.editButton.getMessage().copy().formatted(Formatting.WHITE, Formatting.UNDERLINE)).append(" <").formatted(Formatting.YELLOW));
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class Entry
    extends ElementListWidget.Entry<Entry> {
        abstract void update();
    }
}

