/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.scoreboard;

import com.mojang.authlib.GameProfile;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public interface ScoreHolder {
    public static final String WILDCARD_NAME = "*";
    public static final ScoreHolder WILDCARD = new ScoreHolder(){

        @Override
        public String getNameForScoreboard() {
            return ScoreHolder.WILDCARD_NAME;
        }
    };

    public String getNameForScoreboard();

    @Nullable
    default public Text getDisplayName() {
        return null;
    }

    default public Text getStyledDisplayName() {
        Text lv = this.getDisplayName();
        if (lv != null) {
            return lv.copy().styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(this.getNameForScoreboard()))));
        }
        return Text.literal(this.getNameForScoreboard());
    }

    public static ScoreHolder fromName(final String name) {
        if (name.equals(WILDCARD_NAME)) {
            return WILDCARD;
        }
        final MutableText lv = Text.literal(name);
        return new ScoreHolder(){

            @Override
            public String getNameForScoreboard() {
                return name;
            }

            @Override
            public Text getStyledDisplayName() {
                return lv;
            }
        };
    }

    public static ScoreHolder fromProfile(GameProfile gameProfile) {
        final String string = gameProfile.getName();
        return new ScoreHolder(){

            @Override
            public String getNameForScoreboard() {
                return string;
            }
        };
    }
}

