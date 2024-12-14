/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.message;

import java.util.Map;
import net.minecraft.network.message.SignedMessage;
import org.jetbrains.annotations.Nullable;

public interface SignedCommandArguments {
    public static final SignedCommandArguments EMPTY = new SignedCommandArguments(){

        @Override
        @Nullable
        public SignedMessage getMessage(String argumentName) {
            return null;
        }
    };

    @Nullable
    public SignedMessage getMessage(String var1);

    public record Impl(Map<String, SignedMessage> arguments) implements SignedCommandArguments
    {
        @Override
        @Nullable
        public SignedMessage getMessage(String argumentName) {
            return this.arguments.get(argumentName);
        }
    }
}

