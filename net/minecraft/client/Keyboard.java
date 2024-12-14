/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.platform.TextureUtil;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.navigation.GuiNavigationType;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.GameModeSelectionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.NarratorMode;
import net.minecraft.client.util.Clipboard;
import net.minecraft.client.util.GlfwUtil;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.WinNativeModuleUtil;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(value=EnvType.CLIENT)
public class Keyboard {
    public static final int DEBUG_CRASH_TIME = 10000;
    private final MinecraftClient client;
    private final Clipboard clipboard = new Clipboard();
    private long debugCrashStartTime = -1L;
    private long debugCrashLastLogTime = -1L;
    private long debugCrashElapsedTime = -1L;
    private boolean switchF3State;

    public Keyboard(MinecraftClient client) {
        this.client = client;
    }

    private boolean processDebugKeys(int key) {
        switch (key) {
            case 69: {
                this.client.debugChunkInfo = !this.client.debugChunkInfo;
                this.debugFormattedLog("SectionPath: {0}", this.client.debugChunkInfo ? "shown" : "hidden");
                return true;
            }
            case 76: {
                this.client.chunkCullingEnabled = !this.client.chunkCullingEnabled;
                this.debugFormattedLog("SmartCull: {0}", this.client.chunkCullingEnabled ? "enabled" : "disabled");
                return true;
            }
            case 85: {
                if (Screen.hasShiftDown()) {
                    this.client.worldRenderer.killFrustum();
                    this.debugFormattedLog("Killed frustum", new Object[0]);
                } else {
                    this.client.worldRenderer.captureFrustum();
                    this.debugFormattedLog("Captured frustum", new Object[0]);
                }
                return true;
            }
            case 86: {
                this.client.debugChunkOcclusion = !this.client.debugChunkOcclusion;
                this.debugFormattedLog("SectionVisibility: {0}", this.client.debugChunkOcclusion ? "enabled" : "disabled");
                return true;
            }
            case 87: {
                this.client.wireFrame = !this.client.wireFrame;
                this.debugFormattedLog("WireFrame: {0}", this.client.wireFrame ? "enabled" : "disabled");
                return true;
            }
        }
        return false;
    }

    private void addDebugMessage(Formatting formatting, Text text) {
        this.client.inGameHud.getChatHud().addMessage(Text.empty().append(Text.translatable("debug.prefix").formatted(formatting, Formatting.BOLD)).append(ScreenTexts.SPACE).append(text));
    }

    private void debugLog(Text text) {
        this.addDebugMessage(Formatting.YELLOW, text);
    }

    private void debugLog(String key, Object ... args) {
        this.debugLog(Text.stringifiedTranslatable(key, args));
    }

    private void debugError(String key, Object ... args) {
        this.addDebugMessage(Formatting.RED, Text.stringifiedTranslatable(key, args));
    }

    private void debugFormattedLog(String pattern, Object ... args) {
        this.debugLog(Text.literal(MessageFormat.format(pattern, args)));
    }

    private boolean processF3(int key) {
        if (this.debugCrashStartTime > 0L && this.debugCrashStartTime < Util.getMeasuringTimeMs() - 100L) {
            return true;
        }
        switch (key) {
            case 65: {
                this.client.worldRenderer.reload();
                this.debugLog("debug.reload_chunks.message", new Object[0]);
                return true;
            }
            case 66: {
                boolean bl = !this.client.getEntityRenderDispatcher().shouldRenderHitboxes();
                this.client.getEntityRenderDispatcher().setRenderHitboxes(bl);
                this.debugLog(bl ? "debug.show_hitboxes.on" : "debug.show_hitboxes.off", new Object[0]);
                return true;
            }
            case 68: {
                if (this.client.inGameHud != null) {
                    this.client.inGameHud.getChatHud().clear(false);
                }
                return true;
            }
            case 71: {
                boolean bl2 = this.client.debugRenderer.toggleShowChunkBorder();
                this.debugLog(bl2 ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off", new Object[0]);
                return true;
            }
            case 72: {
                this.client.options.advancedItemTooltips = !this.client.options.advancedItemTooltips;
                this.debugLog(this.client.options.advancedItemTooltips ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off", new Object[0]);
                this.client.options.write();
                return true;
            }
            case 73: {
                if (!this.client.player.hasReducedDebugInfo()) {
                    this.copyLookAt(this.client.player.hasPermissionLevel(2), !Screen.hasShiftDown());
                }
                return true;
            }
            case 78: {
                if (!this.client.player.hasPermissionLevel(2)) {
                    this.debugLog("debug.creative_spectator.error", new Object[0]);
                } else if (!this.client.player.isSpectator()) {
                    this.client.player.networkHandler.sendCommand("gamemode spectator");
                } else {
                    this.client.player.networkHandler.sendCommand("gamemode " + MoreObjects.firstNonNull(this.client.interactionManager.getPreviousGameMode(), GameMode.CREATIVE).getName());
                }
                return true;
            }
            case 293: {
                if (!this.client.player.hasPermissionLevel(2)) {
                    this.debugLog("debug.gamemodes.error", new Object[0]);
                } else {
                    this.client.setScreen(new GameModeSelectionScreen());
                }
                return true;
            }
            case 80: {
                this.client.options.pauseOnLostFocus = !this.client.options.pauseOnLostFocus;
                this.client.options.write();
                this.debugLog(this.client.options.pauseOnLostFocus ? "debug.pause_focus.on" : "debug.pause_focus.off", new Object[0]);
                return true;
            }
            case 81: {
                this.debugLog("debug.help.message", new Object[0]);
                ChatHud lv = this.client.inGameHud.getChatHud();
                lv.addMessage(Text.translatable("debug.reload_chunks.help"));
                lv.addMessage(Text.translatable("debug.show_hitboxes.help"));
                lv.addMessage(Text.translatable("debug.copy_location.help"));
                lv.addMessage(Text.translatable("debug.clear_chat.help"));
                lv.addMessage(Text.translatable("debug.chunk_boundaries.help"));
                lv.addMessage(Text.translatable("debug.advanced_tooltips.help"));
                lv.addMessage(Text.translatable("debug.inspect.help"));
                lv.addMessage(Text.translatable("debug.profiling.help"));
                lv.addMessage(Text.translatable("debug.creative_spectator.help"));
                lv.addMessage(Text.translatable("debug.pause_focus.help"));
                lv.addMessage(Text.translatable("debug.help.help"));
                lv.addMessage(Text.translatable("debug.dump_dynamic_textures.help"));
                lv.addMessage(Text.translatable("debug.reload_resourcepacks.help"));
                lv.addMessage(Text.translatable("debug.pause.help"));
                lv.addMessage(Text.translatable("debug.gamemodes.help"));
                return true;
            }
            case 83: {
                Path path = this.client.runDirectory.toPath().toAbsolutePath();
                Path path2 = TextureUtil.getDebugTexturePath(path);
                this.client.getTextureManager().dumpDynamicTextures(path2);
                MutableText lv2 = Text.literal(path.relativize(path2).toString()).formatted(Formatting.UNDERLINE).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path2.toFile().toString())));
                this.debugLog("debug.dump_dynamic_textures", lv2);
                return true;
            }
            case 84: {
                this.debugLog("debug.reload_resourcepacks.message", new Object[0]);
                this.client.reloadResources();
                return true;
            }
            case 76: {
                if (this.client.toggleDebugProfiler(this::debugLog)) {
                    this.debugLog("debug.profiling.start", 10);
                }
                return true;
            }
            case 67: {
                if (this.client.player.hasReducedDebugInfo()) {
                    return false;
                }
                ClientPlayNetworkHandler lv3 = this.client.player.networkHandler;
                if (lv3 == null) {
                    return false;
                }
                this.debugLog("debug.copy_location.message", new Object[0]);
                this.setClipboard(String.format(Locale.ROOT, "/execute in %s run tp @s %.2f %.2f %.2f %.2f %.2f", this.client.player.getWorld().getRegistryKey().getValue(), this.client.player.getX(), this.client.player.getY(), this.client.player.getZ(), Float.valueOf(this.client.player.getYaw()), Float.valueOf(this.client.player.getPitch())));
                return true;
            }
            case 49: {
                this.client.getDebugHud().toggleRenderingChart();
                return true;
            }
            case 50: {
                this.client.getDebugHud().toggleRenderingAndTickCharts();
                return true;
            }
            case 51: {
                this.client.getDebugHud().togglePacketSizeAndPingCharts();
                return true;
            }
        }
        return false;
    }

    private void copyLookAt(boolean hasQueryPermission, boolean queryServer) {
        HitResult lv = this.client.crosshairTarget;
        if (lv == null) {
            return;
        }
        switch (lv.getType()) {
            case BLOCK: {
                BlockPos lv2 = ((BlockHitResult)lv).getBlockPos();
                World lv3 = this.client.player.getWorld();
                BlockState lv4 = lv3.getBlockState(lv2);
                if (hasQueryPermission) {
                    if (queryServer) {
                        this.client.player.networkHandler.getDataQueryHandler().queryBlockNbt(lv2, nbt -> {
                            this.copyBlock(lv4, lv2, (NbtCompound)nbt);
                            this.debugLog("debug.inspect.server.block", new Object[0]);
                        });
                        break;
                    }
                    BlockEntity lv5 = lv3.getBlockEntity(lv2);
                    NbtCompound lv6 = lv5 != null ? lv5.createNbt(lv3.getRegistryManager()) : null;
                    this.copyBlock(lv4, lv2, lv6);
                    this.debugLog("debug.inspect.client.block", new Object[0]);
                    break;
                }
                this.copyBlock(lv4, lv2, null);
                this.debugLog("debug.inspect.client.block", new Object[0]);
                break;
            }
            case ENTITY: {
                Entity lv7 = ((EntityHitResult)lv).getEntity();
                Identifier lv8 = Registries.ENTITY_TYPE.getId(lv7.getType());
                if (hasQueryPermission) {
                    if (queryServer) {
                        this.client.player.networkHandler.getDataQueryHandler().queryEntityNbt(lv7.getId(), nbt -> {
                            this.copyEntity(lv8, lv7.getPos(), (NbtCompound)nbt);
                            this.debugLog("debug.inspect.server.entity", new Object[0]);
                        });
                        break;
                    }
                    NbtCompound lv9 = lv7.writeNbt(new NbtCompound());
                    this.copyEntity(lv8, lv7.getPos(), lv9);
                    this.debugLog("debug.inspect.client.entity", new Object[0]);
                    break;
                }
                this.copyEntity(lv8, lv7.getPos(), null);
                this.debugLog("debug.inspect.client.entity", new Object[0]);
                break;
            }
        }
    }

    private void copyBlock(BlockState state, BlockPos pos, @Nullable NbtCompound nbt) {
        StringBuilder stringBuilder = new StringBuilder(BlockArgumentParser.stringifyBlockState(state));
        if (nbt != null) {
            stringBuilder.append(nbt);
        }
        String string = String.format(Locale.ROOT, "/setblock %d %d %d %s", pos.getX(), pos.getY(), pos.getZ(), stringBuilder);
        this.setClipboard(string);
    }

    private void copyEntity(Identifier id, Vec3d pos, @Nullable NbtCompound nbt) {
        String string2;
        if (nbt != null) {
            nbt.remove("UUID");
            nbt.remove("Pos");
            nbt.remove("Dimension");
            String string = NbtHelper.toPrettyPrintedText(nbt).getString();
            string2 = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f %s", id, pos.x, pos.y, pos.z, string);
        } else {
            string2 = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f", id, pos.x, pos.y, pos.z);
        }
        this.setClipboard(string2);
    }

    public void onKey(long window, int key, int scancode, int action, int modifiers) {
        GameMenuScreen lv3;
        Screen screen;
        boolean bl4;
        Screen lv;
        if (window != this.client.getWindow().getHandle()) {
            return;
        }
        boolean bl = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_F3);
        if (this.debugCrashStartTime > 0L) {
            if (!InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_C) || !bl) {
                this.debugCrashStartTime = -1L;
            }
        } else if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_C) && bl) {
            this.switchF3State = true;
            this.debugCrashStartTime = Util.getMeasuringTimeMs();
            this.debugCrashLastLogTime = Util.getMeasuringTimeMs();
            this.debugCrashElapsedTime = 0L;
        }
        if ((lv = this.client.currentScreen) != null) {
            switch (key) {
                case 262: 
                case 263: 
                case 264: 
                case 265: {
                    this.client.setNavigationType(GuiNavigationType.KEYBOARD_ARROW);
                    break;
                }
                case 258: {
                    this.client.setNavigationType(GuiNavigationType.KEYBOARD_TAB);
                }
            }
        }
        if (!(action != 1 || this.client.currentScreen instanceof KeybindsScreen && ((KeybindsScreen)lv).lastKeyCodeUpdateTime > Util.getMeasuringTimeMs() - 20L)) {
            if (this.client.options.fullscreenKey.matchesKey(key, scancode)) {
                this.client.getWindow().toggleFullscreen();
                this.client.options.getFullscreen().setValue(this.client.getWindow().isFullscreen());
                return;
            }
            if (this.client.options.screenshotKey.matchesKey(key, scancode)) {
                if (Screen.hasControlDown()) {
                    // empty if block
                }
                ScreenshotRecorder.saveScreenshot(this.client.runDirectory, this.client.getFramebuffer(), message -> this.client.execute(() -> this.client.inGameHud.getChatHud().addMessage((Text)message)));
                return;
            }
        }
        if (action != 0) {
            boolean bl2;
            boolean bl3 = bl2 = lv == null || !(lv.getFocused() instanceof TextFieldWidget) || !((TextFieldWidget)lv.getFocused()).isActive();
            if (bl2) {
                if (Screen.hasControlDown() && key == GLFW.GLFW_KEY_B && this.client.getNarratorManager().isActive() && this.client.options.getNarratorHotkey().getValue().booleanValue()) {
                    boolean bl32 = this.client.options.getNarrator().getValue() == NarratorMode.OFF;
                    this.client.options.getNarrator().setValue(NarratorMode.byId(this.client.options.getNarrator().getValue().getId() + 1));
                    this.client.options.write();
                    if (lv != null) {
                        lv.refreshNarrator(bl32);
                    }
                }
                ClientPlayerEntity bl32 = this.client.player;
            }
        }
        if (lv != null) {
            boolean[] bls = new boolean[]{false};
            Screen.wrapScreenError(() -> {
                if (action == 1 || action == 2) {
                    lv.applyKeyPressNarratorDelay();
                    bls[0] = lv.keyPressed(key, scancode, modifiers);
                } else if (action == 0) {
                    bls[0] = lv.keyReleased(key, scancode, modifiers);
                }
            }, "keyPressed event handler", lv.getClass().getCanonicalName());
            if (bls[0]) {
                return;
            }
        }
        InputUtil.Key lv2 = InputUtil.fromKeyCode(key, scancode);
        boolean bl3 = this.client.currentScreen == null;
        boolean bl5 = bl4 = bl3 || (screen = this.client.currentScreen) instanceof GameMenuScreen && !(lv3 = (GameMenuScreen)screen).shouldShowMenu();
        if (action == 0) {
            KeyBinding.setKeyPressed(lv2, false);
            if (bl4 && key == GLFW.GLFW_KEY_F3) {
                if (this.switchF3State) {
                    this.switchF3State = false;
                } else {
                    this.client.getDebugHud().toggleDebugHud();
                }
            }
            return;
        }
        boolean bl52 = false;
        if (bl4) {
            if (key == GLFW.GLFW_KEY_F4 && this.client.gameRenderer != null) {
                this.client.gameRenderer.togglePostProcessorEnabled();
            }
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                this.client.openGameMenu(bl);
                bl52 |= bl;
            }
            this.switchF3State |= (bl52 |= bl && this.processF3(key));
            if (key == GLFW.GLFW_KEY_F1) {
                boolean bl6 = this.client.options.hudHidden = !this.client.options.hudHidden;
            }
            if (this.client.getDebugHud().shouldShowRenderingChart() && !bl && key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9) {
                this.client.handleProfilerKeyPress(key - GLFW.GLFW_KEY_0);
            }
        }
        if (bl3) {
            if (bl52) {
                KeyBinding.setKeyPressed(lv2, false);
            } else {
                KeyBinding.setKeyPressed(lv2, true);
                KeyBinding.onKeyPressed(lv2);
            }
        }
    }

    private void onChar(long window, int codePoint, int modifiers) {
        if (window != this.client.getWindow().getHandle()) {
            return;
        }
        Screen lv = this.client.currentScreen;
        if (lv == null || this.client.getOverlay() != null) {
            return;
        }
        if (Character.charCount(codePoint) == 1) {
            Screen.wrapScreenError(() -> lv.charTyped((char)codePoint, modifiers), "charTyped event handler", lv.getClass().getCanonicalName());
        } else {
            for (char c : Character.toChars(codePoint)) {
                Screen.wrapScreenError(() -> lv.charTyped(c, modifiers), "charTyped event handler", lv.getClass().getCanonicalName());
            }
        }
    }

    public void setup(long window2) {
        InputUtil.setKeyboardCallbacks(window2, (window, key, scancode, action, modifiers) -> this.client.execute(() -> this.onKey(window, key, scancode, action, modifiers)), (window, codePoint, modifiers) -> this.client.execute(() -> this.onChar(window, codePoint, modifiers)));
    }

    public String getClipboard() {
        return this.clipboard.getClipboard(this.client.getWindow().getHandle(), (error, description) -> {
            if (error != 65545) {
                this.client.getWindow().logGlError(error, description);
            }
        });
    }

    public void setClipboard(String clipboard) {
        if (!clipboard.isEmpty()) {
            this.clipboard.setClipboard(this.client.getWindow().getHandle(), clipboard);
        }
    }

    public void pollDebugCrash() {
        if (this.debugCrashStartTime > 0L) {
            long l = Util.getMeasuringTimeMs();
            long m = 10000L - (l - this.debugCrashStartTime);
            long n = l - this.debugCrashLastLogTime;
            if (m < 0L) {
                if (Screen.hasControlDown()) {
                    GlfwUtil.makeJvmCrash();
                }
                String string = "Manually triggered debug crash";
                CrashReport lv = new CrashReport("Manually triggered debug crash", new Throwable("Manually triggered debug crash"));
                CrashReportSection lv2 = lv.addElement("Manual crash details");
                WinNativeModuleUtil.addDetailTo(lv2);
                throw new CrashException(lv);
            }
            if (n >= 1000L) {
                if (this.debugCrashElapsedTime == 0L) {
                    this.debugLog("debug.crash.message", new Object[0]);
                } else {
                    this.debugError("debug.crash.warning", MathHelper.ceil((float)m / 1000.0f));
                }
                this.debugCrashLastLogTime = l;
                ++this.debugCrashElapsedTime;
            }
        }
    }
}

