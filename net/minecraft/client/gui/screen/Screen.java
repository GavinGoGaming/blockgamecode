/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.navigation.Navigable;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.screen.narration.ScreenNarrator;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.option.NarratorMode;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.MusicSound;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public abstract class Screen
extends AbstractParentElement
implements Drawable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Text SCREEN_USAGE_TEXT = Text.translatable("narrator.screen.usage");
    protected static final CubeMapRenderer PANORAMA_RENDERER = new CubeMapRenderer(Identifier.ofVanilla("textures/gui/title/background/panorama"));
    protected static final RotatingCubeMapRenderer ROTATING_PANORAMA_RENDERER = new RotatingCubeMapRenderer(PANORAMA_RENDERER);
    public static final Identifier MENU_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/menu_background.png");
    public static final Identifier HEADER_SEPARATOR_TEXTURE = Identifier.ofVanilla("textures/gui/header_separator.png");
    public static final Identifier FOOTER_SEPARATOR_TEXTURE = Identifier.ofVanilla("textures/gui/footer_separator.png");
    private static final Identifier INWORLD_MENU_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/inworld_menu_background.png");
    public static final Identifier INWORLD_HEADER_SEPARATOR_TEXTURE = Identifier.ofVanilla("textures/gui/inworld_header_separator.png");
    public static final Identifier INWORLD_FOOTER_SEPARATOR_TEXTURE = Identifier.ofVanilla("textures/gui/inworld_footer_separator.png");
    protected final Text title;
    private final List<Element> children = Lists.newArrayList();
    private final List<Selectable> selectables = Lists.newArrayList();
    @Nullable
    protected MinecraftClient client;
    private boolean screenInitialized;
    public int width;
    public int height;
    private final List<Drawable> drawables = Lists.newArrayList();
    protected TextRenderer textRenderer;
    private static final long SCREEN_INIT_NARRATION_DELAY;
    private static final long NARRATOR_MODE_CHANGE_DELAY;
    private static final long MOUSE_MOVE_NARRATION_DELAY = 750L;
    private static final long MOUSE_PRESS_SCROLL_NARRATION_DELAY = 200L;
    private static final long KEY_PRESS_NARRATION_DELAY = 200L;
    private final ScreenNarrator narrator = new ScreenNarrator();
    private long elementNarrationStartTime = Long.MIN_VALUE;
    private long screenNarrationStartTime = Long.MAX_VALUE;
    @Nullable
    protected CyclingButtonWidget<NarratorMode> narratorToggleButton;
    @Nullable
    private Selectable selected;
    @Nullable
    private PositionedTooltip tooltip;
    protected final Executor executor = runnable -> this.client.execute(() -> {
        if (this.client.currentScreen == this) {
            runnable.run();
        }
    });

    protected Screen(Text title) {
        this.title = title;
    }

    public Text getTitle() {
        return this.title;
    }

    public Text getNarratedTitle() {
        return this.getTitle();
    }

    public final void renderWithTooltip(DrawContext context, int mouseX, int mouseY, float delta) {
        this.render(context, mouseX, mouseY, delta);
        if (this.tooltip != null) {
            context.drawTooltip(this.textRenderer, this.tooltip.tooltip(), this.tooltip.positioner(), mouseX, mouseY);
            this.tooltip = null;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        for (Drawable lv : this.drawables) {
            lv.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        GuiNavigation.Tab lv;
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()) {
            this.close();
            return true;
        }
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        switch (keyCode) {
            case 263: {
                Record record = this.getArrowNavigation(NavigationDirection.LEFT);
                break;
            }
            case 262: {
                Record record = this.getArrowNavigation(NavigationDirection.RIGHT);
                break;
            }
            case 265: {
                Record record = this.getArrowNavigation(NavigationDirection.UP);
                break;
            }
            case 264: {
                Record record = this.getArrowNavigation(NavigationDirection.DOWN);
                break;
            }
            case 258: {
                Record record = this.getTabNavigation();
                break;
            }
            default: {
                Record record = lv = null;
            }
        }
        if (lv != null) {
            GuiNavigationPath lv2 = super.getNavigationPath(lv);
            if (lv2 == null && lv instanceof GuiNavigation.Tab) {
                this.blur();
                lv2 = super.getNavigationPath(lv);
            }
            if (lv2 != null) {
                this.switchFocus(lv2);
            }
        }
        return false;
    }

    private GuiNavigation.Tab getTabNavigation() {
        boolean bl = !Screen.hasShiftDown();
        return new GuiNavigation.Tab(bl);
    }

    private GuiNavigation.Arrow getArrowNavigation(NavigationDirection direction) {
        return new GuiNavigation.Arrow(direction);
    }

    protected void setInitialFocus() {
        GuiNavigation.Tab lv;
        GuiNavigationPath lv2;
        if (this.client.getNavigationType().isKeyboard() && (lv2 = super.getNavigationPath(lv = new GuiNavigation.Tab(true))) != null) {
            this.switchFocus(lv2);
        }
    }

    protected void setInitialFocus(Element element) {
        GuiNavigationPath lv = GuiNavigationPath.of(this, element.getNavigationPath(new GuiNavigation.Down()));
        if (lv != null) {
            this.switchFocus(lv);
        }
    }

    public void blur() {
        GuiNavigationPath lv = this.getFocusedPath();
        if (lv != null) {
            lv.setFocused(false);
        }
    }

    @VisibleForTesting
    protected void switchFocus(GuiNavigationPath path) {
        this.blur();
        path.setFocused(true);
    }

    public boolean shouldCloseOnEsc() {
        return true;
    }

    public void close() {
        this.client.setScreen(null);
    }

    protected <T extends Element & Drawable> T addDrawableChild(T drawableElement) {
        this.drawables.add(drawableElement);
        return this.addSelectableChild(drawableElement);
    }

    protected <T extends Drawable> T addDrawable(T drawable) {
        this.drawables.add(drawable);
        return drawable;
    }

    protected <T extends Element & Selectable> T addSelectableChild(T child) {
        this.children.add(child);
        this.selectables.add(child);
        return child;
    }

    protected void remove(Element child) {
        if (child instanceof Drawable) {
            this.drawables.remove((Drawable)((Object)child));
        }
        if (child instanceof Selectable) {
            this.selectables.remove((Selectable)((Object)child));
        }
        this.children.remove(child);
    }

    protected void clearChildren() {
        this.drawables.clear();
        this.children.clear();
        this.selectables.clear();
    }

    public static List<Text> getTooltipFromItem(MinecraftClient client, ItemStack stack) {
        return stack.getTooltip(Item.TooltipContext.create(client.world), client.player, client.options.advancedItemTooltips ? TooltipType.Default.ADVANCED : TooltipType.Default.BASIC);
    }

    protected void insertText(String text, boolean override) {
    }

    public boolean handleTextClick(@Nullable Style style) {
        if (style == null) {
            return false;
        }
        ClickEvent lv = style.getClickEvent();
        if (Screen.hasShiftDown()) {
            if (style.getInsertion() != null) {
                this.insertText(style.getInsertion(), false);
            }
        } else if (lv != null) {
            block22: {
                if (lv.getAction() == ClickEvent.Action.OPEN_URL) {
                    if (!this.client.options.getChatLinks().getValue().booleanValue()) {
                        return false;
                    }
                    try {
                        URI uRI = Util.validateUri(lv.getValue());
                        if (this.client.options.getChatLinksPrompt().getValue().booleanValue()) {
                            this.client.setScreen(new ConfirmLinkScreen(confirmed -> {
                                if (confirmed) {
                                    Util.getOperatingSystem().open(uRI);
                                }
                                this.client.setScreen(this);
                            }, lv.getValue(), false));
                            break block22;
                        }
                        Util.getOperatingSystem().open(uRI);
                    } catch (URISyntaxException uRISyntaxException) {
                        LOGGER.error("Can't open url for {}", (Object)lv, (Object)uRISyntaxException);
                    }
                } else if (lv.getAction() == ClickEvent.Action.OPEN_FILE) {
                    Util.getOperatingSystem().open(new File(lv.getValue()));
                } else if (lv.getAction() == ClickEvent.Action.SUGGEST_COMMAND) {
                    this.insertText(StringHelper.stripInvalidChars(lv.getValue()), true);
                } else if (lv.getAction() == ClickEvent.Action.RUN_COMMAND) {
                    String string = StringHelper.stripInvalidChars(lv.getValue());
                    if (string.startsWith("/")) {
                        if (!this.client.player.networkHandler.sendCommand(string.substring(1))) {
                            LOGGER.error("Not allowed to run command with signed argument from click event: '{}'", (Object)string);
                        }
                    } else {
                        LOGGER.error("Failed to run command without '/' prefix from click event: '{}'", (Object)string);
                    }
                } else if (lv.getAction() == ClickEvent.Action.COPY_TO_CLIPBOARD) {
                    this.client.keyboard.setClipboard(lv.getValue());
                } else {
                    LOGGER.error("Don't know how to handle {}", (Object)lv);
                }
            }
            return true;
        }
        return false;
    }

    public final void init(MinecraftClient client, int width, int height) {
        this.client = client;
        this.textRenderer = client.textRenderer;
        this.width = width;
        this.height = height;
        if (!this.screenInitialized) {
            this.init();
            this.setInitialFocus();
        } else {
            this.initTabNavigation();
        }
        this.screenInitialized = true;
        this.narrateScreenIfNarrationEnabled(false);
        this.setElementNarrationDelay(SCREEN_INIT_NARRATION_DELAY);
    }

    protected void clearAndInit() {
        this.clearChildren();
        this.blur();
        this.init();
        this.setInitialFocus();
    }

    @Override
    public List<? extends Element> children() {
        return this.children;
    }

    protected void init() {
    }

    public void tick() {
    }

    public void removed() {
    }

    public void onDisplayed() {
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client.world == null) {
            this.renderPanoramaBackground(context, delta);
        }
        this.applyBlur(delta);
        this.renderDarkening(context);
    }

    protected void applyBlur(float delta) {
        this.client.gameRenderer.renderBlur(delta);
        this.client.getFramebuffer().beginWrite(false);
    }

    protected void renderPanoramaBackground(DrawContext context, float delta) {
        ROTATING_PANORAMA_RENDERER.render(context, this.width, this.height, 1.0f, delta);
    }

    protected void renderDarkening(DrawContext context) {
        this.renderDarkening(context, 0, 0, this.width, this.height);
    }

    protected void renderDarkening(DrawContext context, int x, int y, int width, int height) {
        Screen.renderBackgroundTexture(context, this.client.world == null ? MENU_BACKGROUND_TEXTURE : INWORLD_MENU_BACKGROUND_TEXTURE, x, y, 0.0f, 0.0f, width, height);
    }

    public static void renderBackgroundTexture(DrawContext context, Identifier texture, int x, int y, float u, float v, int width, int height) {
        int m = 32;
        RenderSystem.enableBlend();
        context.drawTexture(texture, x, y, 0, u, v, width, height, 32, 32);
        RenderSystem.disableBlend();
    }

    public void renderInGameBackground(DrawContext context) {
        context.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
    }

    public boolean shouldPause() {
        return true;
    }

    public static boolean hasControlDown() {
        if (MinecraftClient.IS_SYSTEM_MAC) {
            return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SUPER) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SUPER);
        }
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    public static boolean hasShiftDown() {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    public static boolean hasAltDown() {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_ALT) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_ALT);
    }

    public static boolean isCut(int code) {
        return code == 88 && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
    }

    public static boolean isPaste(int code) {
        return code == 86 && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
    }

    public static boolean isCopy(int code) {
        return code == 67 && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
    }

    public static boolean isSelectAll(int code) {
        return code == 65 && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
    }

    protected void initTabNavigation() {
        this.clearAndInit();
    }

    public void resize(MinecraftClient client, int width, int height) {
        this.width = width;
        this.height = height;
        this.initTabNavigation();
    }

    public static void wrapScreenError(Runnable task, String errorTitle, String screenName) {
        try {
            task.run();
        } catch (Throwable throwable) {
            CrashReport lv = CrashReport.create(throwable, errorTitle);
            CrashReportSection lv2 = lv.addElement("Affected screen");
            lv2.add("Screen name", () -> screenName);
            throw new CrashException(lv);
        }
    }

    protected boolean isValidCharacterForName(String name, char character, int cursorPos) {
        int j = name.indexOf(58);
        int k = name.indexOf(47);
        if (character == ':') {
            return (k == -1 || cursorPos <= k) && j == -1;
        }
        if (character == '/') {
            return cursorPos > j;
        }
        return character == '_' || character == '-' || character >= 'a' && character <= 'z' || character >= '0' && character <= '9' || character == '.';
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return true;
    }

    public void filesDragged(List<Path> paths) {
    }

    private void setScreenNarrationDelay(long delayMs, boolean restartElementNarration) {
        this.screenNarrationStartTime = Util.getMeasuringTimeMs() + delayMs;
        if (restartElementNarration) {
            this.elementNarrationStartTime = Long.MIN_VALUE;
        }
    }

    private void setElementNarrationDelay(long delayMs) {
        this.elementNarrationStartTime = Util.getMeasuringTimeMs() + delayMs;
    }

    public void applyMouseMoveNarratorDelay() {
        this.setScreenNarrationDelay(750L, false);
    }

    public void applyMousePressScrollNarratorDelay() {
        this.setScreenNarrationDelay(200L, true);
    }

    public void applyKeyPressNarratorDelay() {
        this.setScreenNarrationDelay(200L, true);
    }

    private boolean isNarratorActive() {
        return this.client.getNarratorManager().isActive();
    }

    public void updateNarrator() {
        long l;
        if (this.isNarratorActive() && (l = Util.getMeasuringTimeMs()) > this.screenNarrationStartTime && l > this.elementNarrationStartTime) {
            this.narrateScreen(true);
            this.screenNarrationStartTime = Long.MAX_VALUE;
        }
    }

    public void narrateScreenIfNarrationEnabled(boolean onlyChangedNarrations) {
        if (this.isNarratorActive()) {
            this.narrateScreen(onlyChangedNarrations);
        }
    }

    private void narrateScreen(boolean onlyChangedNarrations) {
        this.narrator.buildNarrations(this::addScreenNarrations);
        String string = this.narrator.buildNarratorText(!onlyChangedNarrations);
        if (!string.isEmpty()) {
            this.client.getNarratorManager().narrate(string);
        }
    }

    protected boolean hasUsageText() {
        return true;
    }

    protected void addScreenNarrations(NarrationMessageBuilder messageBuilder) {
        messageBuilder.put(NarrationPart.TITLE, this.getNarratedTitle());
        if (this.hasUsageText()) {
            messageBuilder.put(NarrationPart.USAGE, SCREEN_USAGE_TEXT);
        }
        this.addElementNarrations(messageBuilder);
    }

    protected void addElementNarrations(NarrationMessageBuilder builder) {
        List<Selectable> list = this.selectables.stream().filter(Selectable::isNarratable).sorted(Comparator.comparingInt(Navigable::getNavigationOrder)).toList();
        SelectedElementNarrationData lv = Screen.findSelectedElementData(list, this.selected);
        if (lv != null) {
            if (lv.selectType.isFocused()) {
                this.selected = lv.selectable;
            }
            if (list.size() > 1) {
                builder.put(NarrationPart.POSITION, (Text)Text.translatable("narrator.position.screen", lv.index + 1, list.size()));
                if (lv.selectType == Selectable.SelectionType.FOCUSED) {
                    builder.put(NarrationPart.USAGE, this.getUsageNarrationText());
                }
            }
            lv.selectable.appendNarrations(builder.nextMessage());
        }
    }

    protected Text getUsageNarrationText() {
        return Text.translatable("narration.component_list.usage");
    }

    @Nullable
    public static SelectedElementNarrationData findSelectedElementData(List<? extends Selectable> selectables, @Nullable Selectable selectable) {
        SelectedElementNarrationData lv = null;
        SelectedElementNarrationData lv2 = null;
        int j = selectables.size();
        for (int i = 0; i < j; ++i) {
            Selectable lv3 = selectables.get(i);
            Selectable.SelectionType lv4 = lv3.getType();
            if (lv4.isFocused()) {
                if (lv3 == selectable) {
                    lv2 = new SelectedElementNarrationData(lv3, i, lv4);
                    continue;
                }
                return new SelectedElementNarrationData(lv3, i, lv4);
            }
            if (lv4.compareTo(lv != null ? lv.selectType : Selectable.SelectionType.NONE) <= 0) continue;
            lv = new SelectedElementNarrationData(lv3, i, lv4);
        }
        return lv != null ? lv : lv2;
    }

    public void refreshNarrator(boolean previouslyDisabled) {
        if (previouslyDisabled) {
            this.setScreenNarrationDelay(NARRATOR_MODE_CHANGE_DELAY, false);
        }
        if (this.narratorToggleButton != null) {
            this.narratorToggleButton.setValue(this.client.options.getNarrator().getValue());
        }
    }

    protected void clearTooltip() {
        this.tooltip = null;
    }

    public void setTooltip(List<OrderedText> tooltip) {
        this.setTooltip(tooltip, HoveredTooltipPositioner.INSTANCE, true);
    }

    public void setTooltip(List<OrderedText> tooltip, TooltipPositioner positioner, boolean focused) {
        if (this.tooltip == null || focused) {
            this.tooltip = new PositionedTooltip(tooltip, positioner);
        }
    }

    public void setTooltip(Text tooltip) {
        this.setTooltip(Tooltip.wrapLines(this.client, tooltip));
    }

    public void setTooltip(Tooltip tooltip, TooltipPositioner positioner, boolean focused) {
        this.setTooltip(tooltip.getLines(this.client), positioner, focused);
    }

    @Override
    public ScreenRect getNavigationFocus() {
        return new ScreenRect(0, 0, this.width, this.height);
    }

    @Nullable
    public MusicSound getMusic() {
        return null;
    }

    static {
        NARRATOR_MODE_CHANGE_DELAY = SCREEN_INIT_NARRATION_DELAY = TimeUnit.SECONDS.toMillis(2L);
    }

    @Environment(value=EnvType.CLIENT)
    record PositionedTooltip(List<OrderedText> tooltip, TooltipPositioner positioner) {
    }

    @Environment(value=EnvType.CLIENT)
    public static class SelectedElementNarrationData {
        public final Selectable selectable;
        public final int index;
        public final Selectable.SelectionType selectType;

        public SelectedElementNarrationData(Selectable selectable, int index, Selectable.SelectionType selectType) {
            this.selectable = selectable;
            this.index = index;
            this.selectType = selectType;
        }
    }
}

