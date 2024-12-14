/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command;

import java.util.List;
import net.minecraft.command.CommandExecutionContext;
import net.minecraft.command.CommandQueueEntry;
import net.minecraft.command.Frame;
import net.minecraft.command.ReturnValueConsumer;
import net.minecraft.command.SourcedCommandAction;
import net.minecraft.command.SteppedCommandAction;
import net.minecraft.server.command.AbstractServerCommandSource;
import net.minecraft.server.function.Procedure;
import net.minecraft.server.function.Tracer;

public class CommandFunctionAction<T extends AbstractServerCommandSource<T>>
implements SourcedCommandAction<T> {
    private final Procedure<T> function;
    private final ReturnValueConsumer returnValueConsumer;
    private final boolean propagateReturn;

    public CommandFunctionAction(Procedure<T> function, ReturnValueConsumer returnValueConsumer, boolean propagateReturn) {
        this.function = function;
        this.returnValueConsumer = returnValueConsumer;
        this.propagateReturn = propagateReturn;
    }

    @Override
    public void execute(T arg, CommandExecutionContext<T> arg2, Frame arg3) {
        arg2.decrementCommandQuota();
        List<SourcedCommandAction<T>> list = this.function.entries();
        Tracer lv = arg2.getTracer();
        if (lv != null) {
            lv.traceFunctionCall(arg3.depth(), this.function.id(), this.function.entries().size());
        }
        int i = arg3.depth() + 1;
        Frame.Control lv2 = this.propagateReturn ? arg3.frameControl() : arg2.getEscapeControl(i);
        Frame lv3 = new Frame(i, this.returnValueConsumer, lv2);
        SteppedCommandAction.enqueueCommands(arg2, lv3, list, (frame, action) -> new CommandQueueEntry<AbstractServerCommandSource>(frame, action.bind(arg)));
    }

    @Override
    public /* synthetic */ void execute(Object object, CommandExecutionContext arg, Frame arg2) {
        this.execute((AbstractServerCommandSource)object, arg, arg2);
    }
}

