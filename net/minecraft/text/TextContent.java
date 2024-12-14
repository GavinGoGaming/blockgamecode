/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

public interface TextContent {
    default public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> visitor, Style style) {
        return Optional.empty();
    }

    default public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
        return Optional.empty();
    }

    default public MutableText parse(@Nullable ServerCommandSource source, @Nullable Entity sender, int depth) throws CommandSyntaxException {
        return MutableText.of(this);
    }

    public Type<?> getType();

    public record Type<T extends TextContent>(MapCodec<T> codec, String id) implements StringIdentifiable
    {
        @Override
        public String asString() {
            return this.id;
        }
    }
}

