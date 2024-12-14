/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Narratable;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class AlwaysSelectedEntryListWidget<E extends Entry<E>>
extends EntryListWidget<E> {
    private static final Text SELECTION_USAGE_TEXT = Text.translatable("narration.selection.usage");

    public AlwaysSelectedEntryListWidget(MinecraftClient arg, int i, int j, int k, int l) {
        super(arg, i, j, k, l);
    }

    @Override
    @Nullable
    public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        if (this.getEntryCount() == 0) {
            return null;
        }
        if (this.isFocused() && navigation instanceof GuiNavigation.Arrow) {
            GuiNavigation.Arrow lv = (GuiNavigation.Arrow)navigation;
            Entry lv2 = (Entry)this.getNeighboringEntry(lv.direction());
            if (lv2 != null) {
                return GuiNavigationPath.of(this, GuiNavigationPath.of(lv2));
            }
            return null;
        }
        if (!this.isFocused()) {
            Entry lv3 = (Entry)this.getSelectedOrNull();
            if (lv3 == null) {
                lv3 = (Entry)this.getNeighboringEntry(navigation.getDirection());
            }
            if (lv3 == null) {
                return null;
            }
            return GuiNavigationPath.of(this, GuiNavigationPath.of(lv3));
        }
        return null;
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        Entry lv = (Entry)this.getHoveredEntry();
        if (lv != null) {
            this.appendNarrations(builder.nextMessage(), lv);
            lv.appendNarrations(builder);
        } else {
            Entry lv2 = (Entry)this.getSelectedOrNull();
            if (lv2 != null) {
                this.appendNarrations(builder.nextMessage(), lv2);
                lv2.appendNarrations(builder);
            }
        }
        if (this.isFocused()) {
            builder.put(NarrationPart.USAGE, SELECTION_USAGE_TEXT);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class Entry<E extends Entry<E>>
    extends EntryListWidget.Entry<E>
    implements Narratable {
        public abstract Text getNarration();

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return true;
        }

        @Override
        public void appendNarrations(NarrationMessageBuilder builder) {
            builder.put(NarrationPart.TITLE, this.getNarration());
        }
    }
}

