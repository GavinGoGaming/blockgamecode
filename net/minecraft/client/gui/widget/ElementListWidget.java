/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.widget;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.navigation.NavigationAxis;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class ElementListWidget<E extends Entry<E>>
extends EntryListWidget<E> {
    public ElementListWidget(MinecraftClient arg, int i, int j, int k, int l) {
        super(arg, i, j, k, l);
    }

    @Override
    @Nullable
    public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        if (this.getEntryCount() == 0) {
            return null;
        }
        if (navigation instanceof GuiNavigation.Arrow) {
            GuiNavigationPath lv5;
            GuiNavigation.Arrow lv = (GuiNavigation.Arrow)navigation;
            Entry lv2 = (Entry)this.getFocused();
            if (lv.direction().getAxis() == NavigationAxis.HORIZONTAL && lv2 != null) {
                return GuiNavigationPath.of(this, lv2.getNavigationPath(navigation));
            }
            int i = -1;
            NavigationDirection lv3 = lv.direction();
            if (lv2 != null) {
                i = lv2.children().indexOf(lv2.getFocused());
            }
            if (i == -1) {
                switch (lv3) {
                    case LEFT: {
                        i = Integer.MAX_VALUE;
                        lv3 = NavigationDirection.DOWN;
                        break;
                    }
                    case RIGHT: {
                        i = 0;
                        lv3 = NavigationDirection.DOWN;
                        break;
                    }
                    default: {
                        i = 0;
                    }
                }
            }
            Entry lv4 = lv2;
            do {
                if ((lv4 = this.getNeighboringEntry(lv3, element -> !element.children().isEmpty(), lv4)) != null) continue;
                return null;
            } while ((lv5 = lv4.getNavigationPath(lv, i)) == null);
            return GuiNavigationPath.of(this, lv5);
        }
        return super.getNavigationPath(navigation);
    }

    @Override
    public void setFocused(@Nullable Element focused) {
        if (this.getFocused() == focused) {
            return;
        }
        super.setFocused(focused);
        if (focused == null) {
            this.setSelected(null);
        }
    }

    @Override
    public Selectable.SelectionType getType() {
        if (this.isFocused()) {
            return Selectable.SelectionType.FOCUSED;
        }
        return super.getType();
    }

    @Override
    protected boolean isSelectedEntry(int index) {
        return false;
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        Entry lv = (Entry)this.getHoveredEntry();
        if (lv != null) {
            lv.appendNarrations(builder.nextMessage());
            this.appendNarrations(builder, lv);
        } else {
            Entry lv2 = (Entry)this.getFocused();
            if (lv2 != null) {
                lv2.appendNarrations(builder.nextMessage());
                this.appendNarrations(builder, lv2);
            }
        }
        builder.put(NarrationPart.USAGE, (Text)Text.translatable("narration.component_list.usage"));
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class Entry<E extends Entry<E>>
    extends EntryListWidget.Entry<E>
    implements ParentElement {
        @Nullable
        private Element focused;
        @Nullable
        private Selectable focusedSelectable;
        private boolean dragging;

        @Override
        public boolean isDragging() {
            return this.dragging;
        }

        @Override
        public void setDragging(boolean dragging) {
            this.dragging = dragging;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return ParentElement.super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void setFocused(@Nullable Element focused) {
            if (this.focused != null) {
                this.focused.setFocused(false);
            }
            if (focused != null) {
                focused.setFocused(true);
            }
            this.focused = focused;
        }

        @Override
        @Nullable
        public Element getFocused() {
            return this.focused;
        }

        @Nullable
        public GuiNavigationPath getNavigationPath(GuiNavigation navigation, int index) {
            if (this.children().isEmpty()) {
                return null;
            }
            GuiNavigationPath lv = this.children().get(Math.min(index, this.children().size() - 1)).getNavigationPath(navigation);
            return GuiNavigationPath.of(this, lv);
        }

        @Override
        @Nullable
        public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
            if (navigation instanceof GuiNavigation.Arrow) {
                int j;
                int i;
                GuiNavigation.Arrow lv = (GuiNavigation.Arrow)navigation;
                switch (lv.direction()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case UP: 
                    case DOWN: {
                        int n = 0;
                        break;
                    }
                    case LEFT: {
                        int n = -1;
                        break;
                    }
                    case RIGHT: {
                        int n = i = 1;
                    }
                }
                if (i == 0) {
                    return null;
                }
                for (int k = j = MathHelper.clamp(i + this.children().indexOf(this.getFocused()), 0, this.children().size() - 1); k >= 0 && k < this.children().size(); k += i) {
                    Element lv2 = this.children().get(k);
                    GuiNavigationPath lv3 = lv2.getNavigationPath(navigation);
                    if (lv3 == null) continue;
                    return GuiNavigationPath.of(this, lv3);
                }
            }
            return ParentElement.super.getNavigationPath(navigation);
        }

        public abstract List<? extends Selectable> selectableChildren();

        void appendNarrations(NarrationMessageBuilder builder) {
            List<Selectable> list = this.selectableChildren();
            Screen.SelectedElementNarrationData lv = Screen.findSelectedElementData(list, this.focusedSelectable);
            if (lv != null) {
                if (lv.selectType.isFocused()) {
                    this.focusedSelectable = lv.selectable;
                }
                if (list.size() > 1) {
                    builder.put(NarrationPart.POSITION, (Text)Text.translatable("narrator.position.object_list", lv.index + 1, list.size()));
                    if (lv.selectType == Selectable.SelectionType.FOCUSED) {
                        builder.put(NarrationPart.USAGE, (Text)Text.translatable("narration.component_list.usage"));
                    }
                }
                lv.selectable.appendNarrations(builder.nextMessage());
            }
        }
    }
}

