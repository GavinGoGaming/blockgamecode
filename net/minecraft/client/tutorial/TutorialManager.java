/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.tutorial;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.toast.TutorialToast;
import net.minecraft.client.tutorial.BundleTutorial;
import net.minecraft.client.tutorial.TutorialStep;
import net.minecraft.client.tutorial.TutorialStepHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TutorialManager {
    private final MinecraftClient client;
    @Nullable
    private TutorialStepHandler currentHandler;
    private final List<Entry> entries = Lists.newArrayList();
    private final BundleTutorial bundleTutorial;

    public TutorialManager(MinecraftClient client, GameOptions options) {
        this.client = client;
        this.bundleTutorial = new BundleTutorial(this, options);
    }

    public void onMovement(Input input) {
        if (this.currentHandler != null) {
            this.currentHandler.onMovement(input);
        }
    }

    public void onUpdateMouse(double deltaX, double deltaY) {
        if (this.currentHandler != null) {
            this.currentHandler.onMouseUpdate(deltaX, deltaY);
        }
    }

    public void tick(@Nullable ClientWorld world, @Nullable HitResult hitResult) {
        if (this.currentHandler != null && hitResult != null && world != null) {
            this.currentHandler.onTarget(world, hitResult);
        }
    }

    public void onBlockBreaking(ClientWorld world, BlockPos pos, BlockState state, float progress) {
        if (this.currentHandler != null) {
            this.currentHandler.onBlockBreaking(world, pos, state, progress);
        }
    }

    public void onInventoryOpened() {
        if (this.currentHandler != null) {
            this.currentHandler.onInventoryOpened();
        }
    }

    public void onSlotUpdate(ItemStack stack) {
        if (this.currentHandler != null) {
            this.currentHandler.onSlotUpdate(stack);
        }
    }

    public void destroyHandler() {
        if (this.currentHandler == null) {
            return;
        }
        this.currentHandler.destroy();
        this.currentHandler = null;
    }

    public void createHandler() {
        if (this.currentHandler != null) {
            this.destroyHandler();
        }
        this.currentHandler = this.client.options.tutorialStep.createHandler(this);
    }

    public void add(TutorialToast toast, int ticks) {
        this.entries.add(new Entry(toast, ticks));
        this.client.getToastManager().add(toast);
    }

    public void remove(TutorialToast toast) {
        this.entries.removeIf(entry -> entry.toast == toast);
        toast.hide();
    }

    public void tick() {
        this.entries.removeIf(Entry::tick);
        if (this.currentHandler != null) {
            if (this.client.world != null) {
                this.currentHandler.tick();
            } else {
                this.destroyHandler();
            }
        } else if (this.client.world != null) {
            this.createHandler();
        }
    }

    public void setStep(TutorialStep step) {
        this.client.options.tutorialStep = step;
        this.client.options.write();
        if (this.currentHandler != null) {
            this.currentHandler.destroy();
            this.currentHandler = step.createHandler(this);
        }
    }

    public MinecraftClient getClient() {
        return this.client;
    }

    public boolean isInSurvival() {
        if (this.client.interactionManager == null) {
            return false;
        }
        return this.client.interactionManager.getCurrentGameMode() == GameMode.SURVIVAL;
    }

    public static Text keyToText(String name) {
        return Text.keybind("key." + name).formatted(Formatting.BOLD);
    }

    public void onPickupSlotClick(ItemStack cursorStack, ItemStack slotStack, ClickType clickType) {
        this.bundleTutorial.onPickupSlotClick(cursorStack, slotStack, clickType);
    }

    @Environment(value=EnvType.CLIENT)
    static final class Entry {
        final TutorialToast toast;
        private final int expiry;
        private int age;

        Entry(TutorialToast toast, int expiry) {
            this.toast = toast;
            this.expiry = expiry;
        }

        private boolean tick() {
            this.toast.setProgress(Math.min((float)(++this.age) / (float)this.expiry, 1.0f));
            if (this.age > this.expiry) {
                this.toast.hide();
                return true;
            }
            return false;
        }
    }
}

