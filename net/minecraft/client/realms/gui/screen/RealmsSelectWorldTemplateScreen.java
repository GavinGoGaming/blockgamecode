/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.realms.gui.screen;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.RealmsObjectSelectionList;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.WorldTemplate;
import net.minecraft.client.realms.dto.WorldTemplatePaginatedList;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.realms.util.RealmsTextureManager;
import net.minecraft.client.realms.util.TextRenderingUtils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.Urls;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsSelectWorldTemplateScreen
extends RealmsScreen {
    static final Logger LOGGER = LogUtils.getLogger();
    static final Identifier SLOT_FRAME_TEXTURE = Identifier.ofVanilla("widget/slot_frame");
    private static final Text SELECT_TEXT = Text.translatable("mco.template.button.select");
    private static final Text TRAILER_TEXT = Text.translatable("mco.template.button.trailer");
    private static final Text PUBLISHER_TEXT = Text.translatable("mco.template.button.publisher");
    private static final int field_45974 = 100;
    private static final int field_45975 = 10;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    final Consumer<WorldTemplate> callback;
    WorldTemplateObjectSelectionList templateList;
    private final RealmsServer.WorldType worldType;
    private ButtonWidget selectButton;
    private ButtonWidget trailerButton;
    private ButtonWidget publisherButton;
    @Nullable
    WorldTemplate selectedTemplate = null;
    @Nullable
    String currentLink;
    @Nullable
    private Text[] warning;
    @Nullable
    List<TextRenderingUtils.Line> noTemplatesMessage;

    public RealmsSelectWorldTemplateScreen(Text title, Consumer<WorldTemplate> callback, RealmsServer.WorldType worldType) {
        this(title, callback, worldType, null);
    }

    public RealmsSelectWorldTemplateScreen(Text title, Consumer<WorldTemplate> callback, RealmsServer.WorldType worldType, @Nullable WorldTemplatePaginatedList templateList) {
        super(title);
        this.callback = callback;
        this.worldType = worldType;
        if (templateList == null) {
            this.templateList = new WorldTemplateObjectSelectionList();
            this.setPagination(new WorldTemplatePaginatedList(10));
        } else {
            this.templateList = new WorldTemplateObjectSelectionList(Lists.newArrayList(templateList.templates));
            this.setPagination(templateList);
        }
    }

    public void setWarning(Text ... warning) {
        this.warning = warning;
    }

    @Override
    public void init() {
        this.layout.addHeader(this.title, this.textRenderer);
        this.templateList = this.layout.addBody(new WorldTemplateObjectSelectionList(this.templateList.getValues()));
        DirectionalLayoutWidget lv = this.layout.addFooter(DirectionalLayoutWidget.horizontal().spacing(10));
        lv.getMainPositioner().alignHorizontalCenter();
        this.trailerButton = lv.add(ButtonWidget.builder(TRAILER_TEXT, button -> this.onTrailer()).width(100).build());
        this.selectButton = lv.add(ButtonWidget.builder(SELECT_TEXT, button -> this.selectTemplate()).width(100).build());
        lv.add(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.close()).width(100).build());
        this.publisherButton = lv.add(ButtonWidget.builder(PUBLISHER_TEXT, button -> this.onPublish()).width(100).build());
        this.updateButtonStates();
        this.layout.forEachChild(child -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
        });
        this.initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        this.templateList.setDimensions(this.width, this.height - this.layout.getFooterHeight() - this.getTemplateListTop());
        this.layout.refreshPositions();
    }

    @Override
    public Text getNarratedTitle() {
        ArrayList<Text> list = Lists.newArrayListWithCapacity(2);
        list.add(this.title);
        if (this.warning != null) {
            list.addAll(Arrays.asList(this.warning));
        }
        return ScreenTexts.joinLines(list);
    }

    void updateButtonStates() {
        this.publisherButton.visible = this.selectedTemplate != null && !this.selectedTemplate.link.isEmpty();
        this.trailerButton.visible = this.selectedTemplate != null && !this.selectedTemplate.trailer.isEmpty();
        this.selectButton.active = this.selectedTemplate != null;
    }

    @Override
    public void close() {
        this.callback.accept(null);
    }

    private void selectTemplate() {
        if (this.selectedTemplate != null) {
            this.callback.accept(this.selectedTemplate);
        }
    }

    private void onTrailer() {
        if (this.selectedTemplate != null && !this.selectedTemplate.trailer.isBlank()) {
            ConfirmLinkScreen.open((Screen)this, this.selectedTemplate.trailer);
        }
    }

    private void onPublish() {
        if (this.selectedTemplate != null && !this.selectedTemplate.link.isBlank()) {
            ConfirmLinkScreen.open((Screen)this, this.selectedTemplate.link);
        }
    }

    private void setPagination(final WorldTemplatePaginatedList templateList) {
        new Thread("realms-template-fetcher"){

            @Override
            public void run() {
                WorldTemplatePaginatedList lv = templateList;
                RealmsClient lv2 = RealmsClient.create();
                while (lv != null) {
                    Either<WorldTemplatePaginatedList, Exception> either = RealmsSelectWorldTemplateScreen.this.fetchWorldTemplates(lv, lv2);
                    lv = RealmsSelectWorldTemplateScreen.this.client.submit(() -> {
                        if (either.right().isPresent()) {
                            LOGGER.error("Couldn't fetch templates", (Throwable)either.right().get());
                            if (RealmsSelectWorldTemplateScreen.this.templateList.isEmpty()) {
                                RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(I18n.translate("mco.template.select.failure", new Object[0]), new TextRenderingUtils.LineSegment[0]);
                            }
                            return null;
                        }
                        WorldTemplatePaginatedList lv = (WorldTemplatePaginatedList)either.left().get();
                        for (WorldTemplate lv2 : lv.templates) {
                            RealmsSelectWorldTemplateScreen.this.templateList.addEntry(lv2);
                        }
                        if (lv.templates.isEmpty()) {
                            if (RealmsSelectWorldTemplateScreen.this.templateList.isEmpty()) {
                                String string = I18n.translate("mco.template.select.none", "%link");
                                TextRenderingUtils.LineSegment lv3 = TextRenderingUtils.LineSegment.link(I18n.translate("mco.template.select.none.linkTitle", new Object[0]), Urls.REALMS_CONTENT_CREATOR.toString());
                                RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(string, lv3);
                            }
                            return null;
                        }
                        return lv;
                    }).join();
                }
            }
        }.start();
    }

    Either<WorldTemplatePaginatedList, Exception> fetchWorldTemplates(WorldTemplatePaginatedList templateList, RealmsClient realms) {
        try {
            return Either.left(realms.fetchWorldTemplates(templateList.page + 1, templateList.size, this.worldType));
        } catch (RealmsServiceException lv) {
            return Either.right(lv);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.currentLink = null;
        if (this.noTemplatesMessage != null) {
            this.renderMessages(context, mouseX, mouseY, this.noTemplatesMessage);
        }
        if (this.warning != null) {
            for (int k = 0; k < this.warning.length; ++k) {
                Text lv = this.warning[k];
                context.drawCenteredTextWithShadow(this.textRenderer, lv, this.width / 2, RealmsSelectWorldTemplateScreen.row(-1 + k), Colors.LIGHT_GRAY);
            }
        }
    }

    private void renderMessages(DrawContext context, int x, int y, List<TextRenderingUtils.Line> messages) {
        for (int k = 0; k < messages.size(); ++k) {
            TextRenderingUtils.Line lv = messages.get(k);
            int l = RealmsSelectWorldTemplateScreen.row(4 + k);
            int m = lv.segments.stream().mapToInt(segment -> this.textRenderer.getWidth(segment.renderedText())).sum();
            int n = this.width / 2 - m / 2;
            for (TextRenderingUtils.LineSegment lv2 : lv.segments) {
                int o = lv2.isLink() ? 0x3366BB : Colors.WHITE;
                int p = context.drawTextWithShadow(this.textRenderer, lv2.renderedText(), n, l, o);
                if (lv2.isLink() && x > n && x < p && y > l - 3 && y < l + 8) {
                    this.setTooltip(Text.literal(lv2.getLinkUrl()));
                    this.currentLink = lv2.getLinkUrl();
                }
                n = p;
            }
        }
    }

    int getTemplateListTop() {
        return this.warning != null ? RealmsSelectWorldTemplateScreen.row(1) : 33;
    }

    @Environment(value=EnvType.CLIENT)
    class WorldTemplateObjectSelectionList
    extends RealmsObjectSelectionList<WorldTemplateObjectSelectionListEntry> {
        public WorldTemplateObjectSelectionList() {
            this(Collections.emptyList());
        }

        public WorldTemplateObjectSelectionList(Iterable<WorldTemplate> templates) {
            super(RealmsSelectWorldTemplateScreen.this.width, RealmsSelectWorldTemplateScreen.this.height - 33 - RealmsSelectWorldTemplateScreen.this.getTemplateListTop(), RealmsSelectWorldTemplateScreen.this.getTemplateListTop(), 46);
            templates.forEach(this::addEntry);
        }

        public void addEntry(WorldTemplate template) {
            this.addEntry(new WorldTemplateObjectSelectionListEntry(template));
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (RealmsSelectWorldTemplateScreen.this.currentLink != null) {
                ConfirmLinkScreen.open((Screen)RealmsSelectWorldTemplateScreen.this, RealmsSelectWorldTemplateScreen.this.currentLink);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void setSelected(@Nullable WorldTemplateObjectSelectionListEntry arg) {
            super.setSelected(arg);
            RealmsSelectWorldTemplateScreen.this.selectedTemplate = arg == null ? null : arg.mTemplate;
            RealmsSelectWorldTemplateScreen.this.updateButtonStates();
        }

        @Override
        public int getMaxPosition() {
            return this.getEntryCount() * 46;
        }

        @Override
        public int getRowWidth() {
            return 300;
        }

        public boolean isEmpty() {
            return this.getEntryCount() == 0;
        }

        public List<WorldTemplate> getValues() {
            return this.children().stream().map(child -> child.mTemplate).collect(Collectors.toList());
        }
    }

    @Environment(value=EnvType.CLIENT)
    class WorldTemplateObjectSelectionListEntry
    extends AlwaysSelectedEntryListWidget.Entry<WorldTemplateObjectSelectionListEntry> {
        private static final ButtonTextures LINK_TEXTURES = new ButtonTextures(Identifier.ofVanilla("icon/link"), Identifier.ofVanilla("icon/link_highlighted"));
        private static final ButtonTextures VIDEO_LINK_TEXTURES = new ButtonTextures(Identifier.ofVanilla("icon/video_link"), Identifier.ofVanilla("icon/video_link_highlighted"));
        private static final Text INFO_TOOLTIP_TEXT = Text.translatable("mco.template.info.tooltip");
        private static final Text TRAILER_TOOLTIP_TEXT = Text.translatable("mco.template.trailer.tooltip");
        public final WorldTemplate mTemplate;
        private long prevClickTime;
        @Nullable
        private TexturedButtonWidget infoButton;
        @Nullable
        private TexturedButtonWidget trailerButton;

        public WorldTemplateObjectSelectionListEntry(WorldTemplate template) {
            this.mTemplate = template;
            if (!template.link.isBlank()) {
                this.infoButton = new TexturedButtonWidget(15, 15, LINK_TEXTURES, ConfirmLinkScreen.opening((Screen)RealmsSelectWorldTemplateScreen.this, template.link), INFO_TOOLTIP_TEXT);
                this.infoButton.setTooltip(Tooltip.of(INFO_TOOLTIP_TEXT));
            }
            if (!template.trailer.isBlank()) {
                this.trailerButton = new TexturedButtonWidget(15, 15, VIDEO_LINK_TEXTURES, ConfirmLinkScreen.opening((Screen)RealmsSelectWorldTemplateScreen.this, template.trailer), TRAILER_TOOLTIP_TEXT);
                this.trailerButton.setTooltip(Tooltip.of(TRAILER_TOOLTIP_TEXT));
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            RealmsSelectWorldTemplateScreen.this.selectedTemplate = this.mTemplate;
            RealmsSelectWorldTemplateScreen.this.updateButtonStates();
            if (Util.getMeasuringTimeMs() - this.prevClickTime < 250L && this.isFocused()) {
                RealmsSelectWorldTemplateScreen.this.callback.accept(this.mTemplate);
            }
            this.prevClickTime = Util.getMeasuringTimeMs();
            if (this.infoButton != null) {
                this.infoButton.mouseClicked(mouseX, mouseY, button);
            }
            if (this.trailerButton != null) {
                this.trailerButton.mouseClicked(mouseX, mouseY, button);
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawTexture(RealmsTextureManager.getTextureId(this.mTemplate.id, this.mTemplate.image), x + 1, y + 1 + 1, 0.0f, 0.0f, 38, 38, 38, 38);
            context.drawGuiTexture(SLOT_FRAME_TEXTURE, x, y + 1, 40, 40);
            int p = 5;
            int q = RealmsSelectWorldTemplateScreen.this.textRenderer.getWidth(this.mTemplate.version);
            if (this.infoButton != null) {
                this.infoButton.setPosition(x + entryWidth - q - this.infoButton.getWidth() - 10, y);
                this.infoButton.render(context, mouseX, mouseY, tickDelta);
            }
            if (this.trailerButton != null) {
                this.trailerButton.setPosition(x + entryWidth - q - this.trailerButton.getWidth() * 2 - 15, y);
                this.trailerButton.render(context, mouseX, mouseY, tickDelta);
            }
            int r = x + 45 + 20;
            int s = y + 5;
            context.drawText(RealmsSelectWorldTemplateScreen.this.textRenderer, this.mTemplate.name, r, s, Colors.WHITE, false);
            context.drawText(RealmsSelectWorldTemplateScreen.this.textRenderer, this.mTemplate.version, x + entryWidth - q - 5, s, 0x6C6C6C, false);
            context.drawText(RealmsSelectWorldTemplateScreen.this.textRenderer, this.mTemplate.author, r, s + ((RealmsSelectWorldTemplateScreen)RealmsSelectWorldTemplateScreen.this).textRenderer.fontHeight + 5, Colors.LIGHT_GRAY, false);
            if (!this.mTemplate.recommendedPlayers.isBlank()) {
                context.drawText(RealmsSelectWorldTemplateScreen.this.textRenderer, this.mTemplate.recommendedPlayers, r, y + entryHeight - ((RealmsSelectWorldTemplateScreen)RealmsSelectWorldTemplateScreen.this).textRenderer.fontHeight / 2 - 5, 0x4C4C4C, false);
            }
        }

        @Override
        public Text getNarration() {
            Text lv = ScreenTexts.joinLines(Text.literal(this.mTemplate.name), Text.translatable("mco.template.select.narrate.authors", this.mTemplate.author), Text.literal(this.mTemplate.recommendedPlayers), Text.translatable("mco.template.select.narrate.version", this.mTemplate.version));
            return Text.translatable("narrator.select", lv);
        }
    }
}

