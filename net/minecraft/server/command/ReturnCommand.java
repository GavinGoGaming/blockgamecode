/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.ContextChain;
import java.util.List;
import net.minecraft.command.ControlFlowAware;
import net.minecraft.command.ExecutionControl;
import net.minecraft.command.ExecutionFlags;
import net.minecraft.command.FallthroughCommandAction;
import net.minecraft.command.Forkable;
import net.minecraft.command.Frame;
import net.minecraft.command.SingleCommandAction;
import net.minecraft.server.command.AbstractServerCommandSource;

public class ReturnCommand {
    public static <T extends AbstractServerCommandSource<T>> void register(CommandDispatcher<T> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("return").requires(source -> source.hasPermissionLevel(2))).then(RequiredArgumentBuilder.argument("value", IntegerArgumentType.integer()).executes(new ValueCommand()))).then(LiteralArgumentBuilder.literal("fail").executes(new FailCommand()))).then(LiteralArgumentBuilder.literal("run").forward(dispatcher.getRoot(), new ReturnRunRedirector(), false)));
    }

    static class ValueCommand<T extends AbstractServerCommandSource<T>>
    implements ControlFlowAware.Command<T> {
        ValueCommand() {
        }

        @Override
        public void execute(T arg, ContextChain<T> contextChain, ExecutionFlags arg2, ExecutionControl<T> arg3) {
            int i = IntegerArgumentType.getInteger(contextChain.getTopContext(), "value");
            arg.getReturnValueConsumer().onSuccess(i);
            Frame lv = arg3.getFrame();
            lv.succeed(i);
            lv.doReturn();
        }
    }

    static class FailCommand<T extends AbstractServerCommandSource<T>>
    implements ControlFlowAware.Command<T> {
        FailCommand() {
        }

        @Override
        public void execute(T arg, ContextChain<T> contextChain, ExecutionFlags arg2, ExecutionControl<T> arg3) {
            arg.getReturnValueConsumer().onFailure();
            Frame lv = arg3.getFrame();
            lv.fail();
            lv.doReturn();
        }
    }

    static class ReturnRunRedirector<T extends AbstractServerCommandSource<T>>
    implements Forkable.RedirectModifier<T> {
        ReturnRunRedirector() {
        }

        @Override
        public void execute(T arg, List<T> list, ContextChain<T> contextChain, ExecutionFlags arg2, ExecutionControl<T> arg3) {
            if (list.isEmpty()) {
                if (arg2.isInsideReturnRun()) {
                    arg3.enqueueAction(FallthroughCommandAction.getInstance());
                }
                return;
            }
            arg3.getFrame().doReturn();
            ContextChain<T> contextChain2 = contextChain.nextStage();
            String string = contextChain2.getTopContext().getInput();
            arg3.enqueueAction(new SingleCommandAction.MultiSource<T>(string, contextChain2, arg2.setInsideReturnRun(), arg, list));
        }
    }
}

