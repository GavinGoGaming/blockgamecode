/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.pack;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Colors;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ExperimentalWarningScreen
extends Screen {
    private static final Text TITLE = Text.translatable("selectWorld.experimental.title");
    private static final Text MESSAGE = Text.translatable("selectWorld.experimental.message");
    private static final Text DETAILS = Text.translatable("selectWorld.experimental.details");
    private static final int field_42498 = 10;
    private static final int field_42499 = 100;
    private final BooleanConsumer callback;
    final Collection<ResourcePackProfile> enabledProfiles;
    private final GridWidget grid = new GridWidget().setColumnSpacing(10).setRowSpacing(20);

    public ExperimentalWarningScreen(Collection<ResourcePackProfile> enabledProfiles, BooleanConsumer callback) {
        super(TITLE);
        this.enabledProfiles = enabledProfiles;
        this.callback = callback;
    }

    @Override
    public Text getNarratedTitle() {
        return ScreenTexts.joinSentences(super.getNarratedTitle(), MESSAGE);
    }

    @Override
    protected void init() {
        super.init();
        GridWidget.Adder lv = this.grid.createAdder(2);
        Positioner lv2 = lv.copyPositioner().alignHorizontalCenter();
        lv.add(new TextWidget(this.title, this.textRenderer), 2, lv2);
        MultilineTextWidget lv3 = lv.add(new MultilineTextWidget(MESSAGE, this.textRenderer).setCentered(true), 2, lv2);
        lv3.setMaxWidth(310);
        lv.add(ButtonWidget.builder(DETAILS, button -> this.client.setScreen(new DetailsScreen())).width(100).build(), 2, lv2);
        lv.add(ButtonWidget.builder(ScreenTexts.PROCEED, button -> this.callback.accept(true)).build());
        lv.add(ButtonWidget.builder(ScreenTexts.BACK, button -> this.callback.accept(false)).build());
        this.grid.forEachChild(child -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
        });
        this.grid.refreshPositions();
        this.initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        SimplePositioningWidget.setPos(this.grid, 0, 0, this.width, this.height, 0.5f, 0.5f);
    }

    @Override
    public void close() {
        this.callback.accept(false);
    }

    @Environment(value=EnvType.CLIENT)
    class DetailsScreen
    extends Screen {
        private static final Text TITLE = Text.translatable("selectWorld.experimental.details.title");
        final ThreePartsLayoutWidget layout;
        @Nullable
        private PackListWidget packListWidget;

        DetailsScreen() {
            super(TITLE);
            this.layout = new ThreePartsLayoutWidget(this);
        }

        @Override
        protected void init() {
            this.layout.addHeader(TITLE, this.textRenderer);
            this.packListWidget = this.layout.addBody(new PackListWidget(this, this.client, ExperimentalWarningScreen.this.enabledProfiles));
            this.layout.addFooter(ButtonWidget.builder(ScreenTexts.BACK, button -> this.close()).build());
            this.layout.forEachChild(child -> {
                ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
            });
            this.initTabNavigation();
        }

        @Override
        protected void initTabNavigation() {
            if (this.packListWidget != null) {
                this.packListWidget.position(this.width, this.layout);
            }
            this.layout.refreshPositions();
        }

        @Override
        public void close() {
            this.client.setScreen(ExperimentalWarningScreen.this);
        }

        @Environment(value=EnvType.CLIENT)
        class PackListWidget
        extends AlwaysSelectedEntryListWidget<PackListWidgetEntry> {
            public PackListWidget(DetailsScreen arg, MinecraftClient client, Collection<ResourcePackProfile> enabledProfiles) {
                super(client, arg.width, arg.layout.getContentHeight(), arg.layout.getHeaderHeight(), (client.textRenderer.fontHeight + 2) * 3);
                for (ResourcePackProfile lv : enabledProfiles) {
                    String string = FeatureFlags.printMissingFlags(FeatureFlags.VANILLA_FEATURES, lv.getRequestedFeatures());
                    if (string.isEmpty()) continue;
                    MutableText lv2 = Texts.setStyleIfAbsent(lv.getDisplayName().copy(), Style.EMPTY.withBold(true));
                    MutableText lv3 = Text.translatable("selectWorld.experimental.details.entry", string);
                    this.addEntry(arg.new PackListWidgetEntry(lv2, lv3, MultilineText.create(arg.textRenderer, (Text)lv3, this.getRowWidth())));
                }
            }

            @Override
            public int getRowWidth() {
                return this.width * 3 / 4;
            }
        }

        @Environment(value=EnvType.CLIENT)
        class PackListWidgetEntry
        extends AlwaysSelectedEntryListWidget.Entry<PackListWidgetEntry> {
            private final Text displayName;
            private final Text details;
            private final MultilineText multilineDetails;

            PackListWidgetEntry(Text displayName, Text details, MultilineText multilineDetails) {
                this.displayName = displayName;
                this.details = details;
                this.multilineDetails = multilineDetails;
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                context.drawTextWithShadow(((DetailsScreen)DetailsScreen.this).client.textRenderer, this.displayName, x, y, Colors.WHITE);
                this.multilineDetails.drawWithShadow(context, x, y + 12, ((DetailsScreen)DetailsScreen.this).textRenderer.fontHeight, -1);
            }

            @Override
            public Text getNarration() {
                return Text.translatable("narrator.select", ScreenTexts.joinSentences(this.displayName, this.details));
            }
        }
    }
}

