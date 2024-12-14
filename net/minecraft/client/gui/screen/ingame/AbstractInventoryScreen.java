/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.Ordering;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractInventoryScreen<T extends ScreenHandler>
extends HandledScreen<T> {
    private static final Identifier EFFECT_BACKGROUND_LARGE_TEXTURE = Identifier.ofVanilla("container/inventory/effect_background_large");
    private static final Identifier EFFECT_BACKGROUND_SMALL_TEXTURE = Identifier.ofVanilla("container/inventory/effect_background_small");

    public AbstractInventoryScreen(T arg, PlayerInventory arg2, Text arg3) {
        super(arg, arg2, arg3);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawStatusEffects(context, mouseX, mouseY);
    }

    public boolean hideStatusEffectHud() {
        int i = this.x + this.backgroundWidth + 2;
        int j = this.width - i;
        return j >= 32;
    }

    private void drawStatusEffects(DrawContext context, int mouseX, int mouseY) {
        int k = this.x + this.backgroundWidth + 2;
        int l = this.width - k;
        Collection<StatusEffectInstance> collection = this.client.player.getStatusEffects();
        if (collection.isEmpty() || l < 32) {
            return;
        }
        boolean bl = l >= 120;
        int m = 33;
        if (collection.size() > 5) {
            m = 132 / (collection.size() - 1);
        }
        List<StatusEffectInstance> iterable = Ordering.natural().sortedCopy(collection);
        this.drawStatusEffectBackgrounds(context, k, m, iterable, bl);
        this.drawStatusEffectSprites(context, k, m, iterable, bl);
        if (bl) {
            this.drawStatusEffectDescriptions(context, k, m, iterable);
        } else if (mouseX >= k && mouseX <= k + 33) {
            int n = this.y;
            StatusEffectInstance lv = null;
            for (StatusEffectInstance lv2 : iterable) {
                if (mouseY >= n && mouseY <= n + m) {
                    lv = lv2;
                }
                n += m;
            }
            if (lv != null) {
                List<Text> list = List.of(this.getStatusEffectDescription(lv), StatusEffectUtil.getDurationText(lv, 1.0f, this.client.world.getTickManager().getTickRate()));
                context.drawTooltip(this.textRenderer, list, Optional.empty(), mouseX, mouseY);
            }
        }
    }

    private void drawStatusEffectBackgrounds(DrawContext context, int x, int height, Iterable<StatusEffectInstance> statusEffects, boolean wide) {
        int k = this.y;
        for (StatusEffectInstance lv : statusEffects) {
            if (wide) {
                context.drawGuiTexture(EFFECT_BACKGROUND_LARGE_TEXTURE, x, k, 120, 32);
            } else {
                context.drawGuiTexture(EFFECT_BACKGROUND_SMALL_TEXTURE, x, k, 32, 32);
            }
            k += height;
        }
    }

    private void drawStatusEffectSprites(DrawContext context, int x, int height, Iterable<StatusEffectInstance> statusEffects, boolean wide) {
        StatusEffectSpriteManager lv = this.client.getStatusEffectSpriteManager();
        int k = this.y;
        for (StatusEffectInstance lv2 : statusEffects) {
            RegistryEntry<StatusEffect> lv3 = lv2.getEffectType();
            Sprite lv4 = lv.getSprite(lv3);
            context.drawSprite(x + (wide ? 6 : 7), k + 7, 0, 18, 18, lv4);
            k += height;
        }
    }

    private void drawStatusEffectDescriptions(DrawContext context, int x, int height, Iterable<StatusEffectInstance> statusEffects) {
        int k = this.y;
        for (StatusEffectInstance lv : statusEffects) {
            Text lv2 = this.getStatusEffectDescription(lv);
            context.drawTextWithShadow(this.textRenderer, lv2, x + 10 + 18, k + 6, 0xFFFFFF);
            Text lv3 = StatusEffectUtil.getDurationText(lv, 1.0f, this.client.world.getTickManager().getTickRate());
            context.drawTextWithShadow(this.textRenderer, lv3, x + 10 + 18, k + 6 + 10, 0x7F7F7F);
            k += height;
        }
    }

    private Text getStatusEffectDescription(StatusEffectInstance statusEffect) {
        MutableText lv = statusEffect.getEffectType().value().getName().copy();
        if (statusEffect.getAmplifier() >= 1 && statusEffect.getAmplifier() <= 9) {
            lv.append(ScreenTexts.SPACE).append(Text.translatable("enchantment.level." + (statusEffect.getAmplifier() + 1)));
        }
        return lv;
    }
}

