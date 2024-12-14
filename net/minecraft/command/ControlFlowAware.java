/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.ExecutionControl;
import net.minecraft.command.ExecutionFlags;
import net.minecraft.server.command.AbstractServerCommandSource;
import net.minecraft.server.function.Tracer;
import org.jetbrains.annotations.Nullable;

public interface ControlFlowAware<T> {
    public void execute(T var1, ContextChain<T> var2, ExecutionFlags var3, ExecutionControl<T> var4);

    public static abstract class Helper<T extends AbstractServerCommandSource<T>>
    implements ControlFlowAware<T> {
        @Override
        public final void execute(T arg, ContextChain<T> contextChain, ExecutionFlags arg2, ExecutionControl<T> arg3) {
            try {
                this.executeInner(arg, contextChain, arg2, arg3);
            } catch (CommandSyntaxException commandSyntaxException) {
                this.sendError(commandSyntaxException, arg, arg2, arg3.getTracer());
                arg.getReturnValueConsumer().onFailure();
            }
        }

        protected void sendError(CommandSyntaxException exception, T source, ExecutionFlags flags, @Nullable Tracer tracer) {
            source.handleException(exception, flags.isSilent(), tracer);
        }

        protected abstract void executeInner(T var1, ContextChain<T> var2, ExecutionFlags var3, ExecutionControl<T> var4) throws CommandSyntaxException;
    }

    public static interface Command<T>
    extends com.mojang.brigadier.Command<T>,
    ControlFlowAware<T> {
        @Override
        default public int run(CommandContext<T> context) throws CommandSyntaxException {
            throw new UnsupportedOperationException("This function should not run");
        }
    }
}

