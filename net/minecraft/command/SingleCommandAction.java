/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.command.CommandAction;
import net.minecraft.command.CommandExecutionContext;
import net.minecraft.command.CommandQueueEntry;
import net.minecraft.command.ControlFlowAware;
import net.minecraft.command.ExecutionControl;
import net.minecraft.command.ExecutionFlags;
import net.minecraft.command.FallthroughCommandAction;
import net.minecraft.command.FixedCommandAction;
import net.minecraft.command.Forkable;
import net.minecraft.command.Frame;
import net.minecraft.command.ReturnValueConsumer;
import net.minecraft.command.SourcedCommandAction;
import net.minecraft.command.SteppedCommandAction;
import net.minecraft.server.command.AbstractServerCommandSource;
import net.minecraft.server.function.Tracer;
import net.minecraft.text.Text;

public class SingleCommandAction<T extends AbstractServerCommandSource<T>> {
    @VisibleForTesting
    public static final DynamicCommandExceptionType FORK_LIMIT_EXCEPTION = new DynamicCommandExceptionType(count -> Text.stringifiedTranslatable("command.forkLimit", count));
    private final String command;
    private final ContextChain<T> contextChain;

    public SingleCommandAction(String command, ContextChain<T> contextChain) {
        this.command = command;
        this.contextChain = contextChain;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void execute(T baseSource, List<T> sources, CommandExecutionContext<T> context2, Frame frame2, ExecutionFlags flags) {
        ContextChain<T> contextChain = this.contextChain;
        ExecutionFlags lv = flags;
        List<Object> list2 = sources;
        if (contextChain.getStage() != ContextChain.Stage.EXECUTE) {
            context2.getProfiler().push(() -> "prepare " + this.command);
            try {
                int i = context2.getForkLimit();
                while (contextChain.getStage() != ContextChain.Stage.EXECUTE) {
                    RedirectModifier<T> redirectModifier;
                    CommandContext<T> commandContext = contextChain.getTopContext();
                    if (commandContext.isForked()) {
                        lv = lv.setSilent();
                    }
                    if ((redirectModifier = commandContext.getRedirectModifier()) instanceof Forkable) {
                        Forkable lv2 = (Forkable)((Object)redirectModifier);
                        lv2.execute(baseSource, list2, contextChain, lv, ExecutionControl.of(context2, frame2));
                        return;
                    }
                    if (redirectModifier != null) {
                        context2.decrementCommandQuota();
                        boolean bl = lv.isSilent();
                        ObjectArrayList list3 = new ObjectArrayList();
                        for (AbstractServerCommandSource abstractServerCommandSource : list2) {
                            Collection<AbstractServerCommandSource> collection;
                            block21: {
                                try {
                                    collection = ContextChain.runModifier(commandContext, abstractServerCommandSource, (context, successful, returnValue) -> {}, bl);
                                    if (list3.size() + collection.size() < i) break block21;
                                    baseSource.handleException(FORK_LIMIT_EXCEPTION.create(i), bl, context2.getTracer());
                                    return;
                                } catch (CommandSyntaxException commandSyntaxException) {
                                    abstractServerCommandSource.handleException(commandSyntaxException, bl, context2.getTracer());
                                    if (bl) continue;
                                    context2.getProfiler().pop();
                                    return;
                                }
                            }
                            list3.addAll(collection);
                        }
                        list2 = list3;
                    }
                    contextChain = contextChain.nextStage();
                }
            } finally {
                context2.getProfiler().pop();
            }
        }
        if (list2.isEmpty()) {
            if (lv.isInsideReturnRun()) {
                context2.enqueueCommand(new CommandQueueEntry(frame2, FallthroughCommandAction.getInstance()));
            }
            return;
        }
        CommandContext<T> commandContext2 = contextChain.getTopContext();
        Command<T> command = commandContext2.getCommand();
        if (command instanceof ControlFlowAware) {
            ControlFlowAware lv4 = (ControlFlowAware)((Object)command);
            ExecutionControl lv5 = ExecutionControl.of(context2, frame2);
            for (AbstractServerCommandSource abstractServerCommandSource : list2) {
                lv4.execute(abstractServerCommandSource, contextChain, lv, lv5);
            }
        } else {
            if (lv.isInsideReturnRun()) {
                AbstractServerCommandSource lv7 = (AbstractServerCommandSource)list2.get(0);
                lv7 = lv7.withReturnValueConsumer(ReturnValueConsumer.chain(lv7.getReturnValueConsumer(), frame2.returnValueConsumer()));
                list2 = List.of(lv7);
            }
            FixedCommandAction<T> lv8 = new FixedCommandAction<T>(this.command, lv, commandContext2);
            SteppedCommandAction.enqueueCommands(context2, frame2, list2, (frame, source) -> new CommandQueueEntry<AbstractServerCommandSource>(frame, lv8.bind(source)));
        }
    }

    protected void traceCommandStart(CommandExecutionContext<T> context, Frame frame) {
        Tracer lv = context.getTracer();
        if (lv != null) {
            lv.traceCommandStart(frame.depth(), this.command);
        }
    }

    public String toString() {
        return this.command;
    }

    public static class SingleSource<T extends AbstractServerCommandSource<T>>
    extends SingleCommandAction<T>
    implements CommandAction<T> {
        private final T source;

        public SingleSource(String command, ContextChain<T> contextChain, T source) {
            super(command, contextChain);
            this.source = source;
        }

        @Override
        public void execute(CommandExecutionContext<T> arg, Frame arg2) {
            this.traceCommandStart(arg, arg2);
            this.execute(this.source, List.of(this.source), arg, arg2, ExecutionFlags.NONE);
        }
    }

    public static class MultiSource<T extends AbstractServerCommandSource<T>>
    extends SingleCommandAction<T>
    implements CommandAction<T> {
        private final ExecutionFlags flags;
        private final T baseSource;
        private final List<T> sources;

        public MultiSource(String command, ContextChain<T> contextChain, ExecutionFlags flags, T baseSource, List<T> sources) {
            super(command, contextChain);
            this.baseSource = baseSource;
            this.sources = sources;
            this.flags = flags;
        }

        @Override
        public void execute(CommandExecutionContext<T> arg, Frame arg2) {
            this.execute(this.baseSource, this.sources, arg, arg2, this.flags);
        }
    }

    public static class Sourced<T extends AbstractServerCommandSource<T>>
    extends SingleCommandAction<T>
    implements SourcedCommandAction<T> {
        public Sourced(String string, ContextChain<T> contextChain) {
            super(string, contextChain);
        }

        @Override
        public void execute(T arg, CommandExecutionContext<T> arg2, Frame arg3) {
            this.traceCommandStart(arg2, arg3);
            this.execute(arg, List.of(arg), arg2, arg3, ExecutionFlags.NONE);
        }

        @Override
        public /* synthetic */ void execute(Object object, CommandExecutionContext arg, Frame arg2) {
            this.execute((AbstractServerCommandSource)object, arg, arg2);
        }
    }
}

