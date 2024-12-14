/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.text;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import org.jetbrains.annotations.Nullable;

public class ScoreTextContent
implements TextContent {
    public static final MapCodec<ScoreTextContent> INNER_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("name")).forGetter(ScoreTextContent::getName), ((MapCodec)Codec.STRING.fieldOf("objective")).forGetter(ScoreTextContent::getObjective)).apply((Applicative<ScoreTextContent, ?>)instance, ScoreTextContent::new));
    public static final MapCodec<ScoreTextContent> CODEC = INNER_CODEC.fieldOf("score");
    public static final TextContent.Type<ScoreTextContent> TYPE = new TextContent.Type<ScoreTextContent>(CODEC, "score");
    private final String name;
    @Nullable
    private final EntitySelector selector;
    private final String objective;

    @Nullable
    private static EntitySelector parseEntitySelector(String name) {
        try {
            return new EntitySelectorReader(new StringReader(name)).read();
        } catch (CommandSyntaxException commandSyntaxException) {
            return null;
        }
    }

    public ScoreTextContent(String name, String objective) {
        this.name = name;
        this.selector = ScoreTextContent.parseEntitySelector(name);
        this.objective = objective;
    }

    @Override
    public TextContent.Type<?> getType() {
        return TYPE;
    }

    public String getName() {
        return this.name;
    }

    @Nullable
    public EntitySelector getSelector() {
        return this.selector;
    }

    public String getObjective() {
        return this.objective;
    }

    private ScoreHolder getScoreHolder(ServerCommandSource source) throws CommandSyntaxException {
        List<? extends Entity> list;
        if (this.selector != null && !(list = this.selector.getEntities(source)).isEmpty()) {
            if (list.size() != 1) {
                throw EntityArgumentType.TOO_MANY_ENTITIES_EXCEPTION.create();
            }
            return list.get(0);
        }
        return ScoreHolder.fromName(this.name);
    }

    private MutableText getScore(ScoreHolder scoreHolder, ServerCommandSource source) {
        ReadableScoreboardScore lv3;
        ServerScoreboard lv;
        ScoreboardObjective lv2;
        MinecraftServer minecraftServer = source.getServer();
        if (minecraftServer != null && (lv2 = (lv = minecraftServer.getScoreboard()).getNullableObjective(this.objective)) != null && (lv3 = lv.getScore(scoreHolder, lv2)) != null) {
            return lv3.getFormattedScore(lv2.getNumberFormatOr(StyledNumberFormat.EMPTY));
        }
        return Text.empty();
    }

    @Override
    public MutableText parse(@Nullable ServerCommandSource source, @Nullable Entity sender, int depth) throws CommandSyntaxException {
        if (source == null) {
            return Text.empty();
        }
        ScoreHolder lv = this.getScoreHolder(source);
        ScoreHolder lv2 = sender != null && lv.equals(ScoreHolder.WILDCARD) ? sender : lv;
        return this.getScore(lv2, source);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ScoreTextContent)) return false;
        ScoreTextContent lv = (ScoreTextContent)o;
        if (!this.name.equals(lv.name)) return false;
        if (!this.objective.equals(lv.objective)) return false;
        return true;
    }

    public int hashCode() {
        int i = this.name.hashCode();
        i = 31 * i + this.objective.hashCode();
        return i;
    }

    public String toString() {
        return "score{name='" + this.name + "', objective='" + this.objective + "'}";
    }
}

