/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.command;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import net.minecraft.command.CommandFunctionAction;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ControlFlowAware;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.ExecutionControl;
import net.minecraft.command.ExecutionFlags;
import net.minecraft.command.FallthroughCommandAction;
import net.minecraft.command.ReturnValueConsumer;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.command.AbstractServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.server.function.MacroException;
import net.minecraft.server.function.Procedure;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class FunctionCommand {
    private static final DynamicCommandExceptionType ARGUMENT_NOT_COMPOUND_EXCEPTION = new DynamicCommandExceptionType(argument -> Text.stringifiedTranslatable("commands.function.error.argument_not_compound", argument));
    static final DynamicCommandExceptionType NO_FUNCTIONS_EXCEPTION = new DynamicCommandExceptionType(argument -> Text.stringifiedTranslatable("commands.function.scheduled.no_functions", argument));
    @VisibleForTesting
    public static final Dynamic2CommandExceptionType INSTANTIATION_FAILURE_EXCEPTION = new Dynamic2CommandExceptionType((argument, argument2) -> Text.stringifiedTranslatable("commands.function.instantiationFailure", argument, argument2));
    public static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder) -> {
        CommandFunctionManager lv = ((ServerCommandSource)context.getSource()).getServer().getCommandFunctionManager();
        CommandSource.suggestIdentifiers(lv.getFunctionTags(), builder, "#");
        return CommandSource.suggestIdentifiers(lv.getAllFunctions(), builder);
    };
    static final ResultConsumer<ServerCommandSource> RESULT_REPORTER = new ResultConsumer<ServerCommandSource>(){

        @Override
        public void accept(ServerCommandSource arg, Identifier arg2, int i) {
            arg.sendFeedback(() -> Text.translatable("commands.function.result", Text.of(arg2), i), true);
        }
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("with");
        for (final DataCommand.ObjectType lv : DataCommand.SOURCE_OBJECT_TYPES) {
            lv.addArgumentsToBuilder(literalArgumentBuilder, builder -> ((ArgumentBuilder)builder.executes(new Command(){

                @Override
                protected NbtCompound getArguments(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                    return lv.getObject(context).getNbt();
                }
            })).then(CommandManager.argument("path", NbtPathArgumentType.nbtPath()).executes(new Command(){

                @Override
                protected NbtCompound getArguments(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                    return FunctionCommand.getArgument(NbtPathArgumentType.getNbtPath(context, "path"), lv.getObject(context));
                }
            })));
        }
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("function").requires(source -> source.hasPermissionLevel(2))).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("name", CommandFunctionArgumentType.commandFunction()).suggests(SUGGESTION_PROVIDER).executes(new Command(){

            @Override
            @Nullable
            protected NbtCompound getArguments(CommandContext<ServerCommandSource> context) {
                return null;
            }
        })).then(CommandManager.argument("arguments", NbtCompoundArgumentType.nbtCompound()).executes(new Command(){

            @Override
            protected NbtCompound getArguments(CommandContext<ServerCommandSource> context) {
                return NbtCompoundArgumentType.getNbtCompound(context, "arguments");
            }
        }))).then(literalArgumentBuilder)));
    }

    static NbtCompound getArgument(NbtPathArgumentType.NbtPath path, DataCommandObject object) throws CommandSyntaxException {
        NbtElement lv = DataCommand.getNbt(path, object);
        if (lv instanceof NbtCompound) {
            NbtCompound lv2 = (NbtCompound)lv;
            return lv2;
        }
        throw ARGUMENT_NOT_COMPOUND_EXCEPTION.create(lv.getNbtType().getCrashReportName());
    }

    public static ServerCommandSource createFunctionCommandSource(ServerCommandSource source) {
        return source.withSilent().withMaxLevel(2);
    }

    public static <T extends AbstractServerCommandSource<T>> void enqueueAction(Collection<CommandFunction<T>> commandFunctions, @Nullable NbtCompound args, T parentSource, T functionSource, ExecutionControl<T> control, ResultConsumer<T> resultConsumer, ExecutionFlags flags) throws CommandSyntaxException {
        if (flags.isInsideReturnRun()) {
            FunctionCommand.enqueueInReturnRun(commandFunctions, args, parentSource, functionSource, control, resultConsumer);
        } else {
            FunctionCommand.enqueueOutsideReturnRun(commandFunctions, args, parentSource, functionSource, control, resultConsumer);
        }
    }

    private static <T extends AbstractServerCommandSource<T>> void enqueueFunction(@Nullable NbtCompound args, ExecutionControl<T> control, CommandDispatcher<T> dispatcher, T source, CommandFunction<T> function, Identifier id, ReturnValueConsumer returnValueConsumer, boolean propagateReturn) throws CommandSyntaxException {
        try {
            Procedure<T> lv = function.withMacroReplaced(args, dispatcher);
            control.enqueueAction(new CommandFunctionAction<T>(lv, returnValueConsumer, propagateReturn).bind(source));
        } catch (MacroException lv2) {
            throw INSTANTIATION_FAILURE_EXCEPTION.create(id, lv2.getMessage());
        }
    }

    private static <T extends AbstractServerCommandSource<T>> ReturnValueConsumer wrapReturnValueConsumer(T flags, ResultConsumer<T> resultConsumer, Identifier id, ReturnValueConsumer wrapped) {
        if (flags.isSilent()) {
            return wrapped;
        }
        return (successful, returnValue) -> {
            resultConsumer.accept(flags, id, returnValue);
            wrapped.onResult(successful, returnValue);
        };
    }

    private static <T extends AbstractServerCommandSource<T>> void enqueueInReturnRun(Collection<CommandFunction<T>> functions, @Nullable NbtCompound args, T parentSource, T functionSource, ExecutionControl<T> control, ResultConsumer<T> resultConsumer) throws CommandSyntaxException {
        CommandDispatcher<T> commandDispatcher = parentSource.getDispatcher();
        T lv = functionSource.withDummyReturnValueConsumer();
        ReturnValueConsumer lv2 = ReturnValueConsumer.chain(parentSource.getReturnValueConsumer(), control.getFrame().returnValueConsumer());
        for (CommandFunction<T> lv3 : functions) {
            Identifier lv4 = lv3.id();
            ReturnValueConsumer lv5 = FunctionCommand.wrapReturnValueConsumer(parentSource, resultConsumer, lv4, lv2);
            FunctionCommand.enqueueFunction(args, control, commandDispatcher, lv, lv3, lv4, lv5, true);
        }
        control.enqueueAction(FallthroughCommandAction.getInstance());
    }

    private static <T extends AbstractServerCommandSource<T>> void enqueueOutsideReturnRun(Collection<CommandFunction<T>> functions, @Nullable NbtCompound args, T parentSource, T functionSource, ExecutionControl<T> control, ResultConsumer<T> resultConsumer) throws CommandSyntaxException {
        CommandDispatcher<T> commandDispatcher = parentSource.getDispatcher();
        T lv = functionSource.withDummyReturnValueConsumer();
        ReturnValueConsumer lv2 = parentSource.getReturnValueConsumer();
        if (functions.isEmpty()) {
            return;
        }
        if (functions.size() == 1) {
            CommandFunction<T> lv3 = functions.iterator().next();
            Identifier lv4 = lv3.id();
            ReturnValueConsumer lv5 = FunctionCommand.wrapReturnValueConsumer(parentSource, resultConsumer, lv4, lv2);
            FunctionCommand.enqueueFunction(args, control, commandDispatcher, lv, lv3, lv4, lv5, false);
        } else if (lv2 == ReturnValueConsumer.EMPTY) {
            for (CommandFunction<T> lv6 : functions) {
                Identifier lv7 = lv6.id();
                ReturnValueConsumer lv8 = FunctionCommand.wrapReturnValueConsumer(parentSource, resultConsumer, lv7, lv2);
                FunctionCommand.enqueueFunction(args, control, commandDispatcher, lv, lv6, lv7, lv8, false);
            }
        } else {
            class ReturnValueAdder {
                boolean successful;
                int returnValue;

                ReturnValueAdder() {
                }

                public void onSuccess(int returnValue) {
                    this.successful = true;
                    this.returnValue += returnValue;
                }
            }
            ReturnValueAdder lv9 = new ReturnValueAdder();
            ReturnValueConsumer lv10 = (successful, returnValue) -> lv9.onSuccess(returnValue);
            for (CommandFunction<T> lv11 : functions) {
                Identifier lv12 = lv11.id();
                ReturnValueConsumer lv13 = FunctionCommand.wrapReturnValueConsumer(parentSource, resultConsumer, lv12, lv10);
                FunctionCommand.enqueueFunction(args, control, commandDispatcher, lv, lv11, lv12, lv13, false);
            }
            control.enqueueAction((context, frame) -> {
                if (arg.successful) {
                    lv2.onSuccess(arg.returnValue);
                }
            });
        }
    }

    public static interface ResultConsumer<T> {
        public void accept(T var1, Identifier var2, int var3);
    }

    static abstract class Command
    extends ControlFlowAware.Helper<ServerCommandSource>
    implements ControlFlowAware.Command<ServerCommandSource> {
        Command() {
        }

        @Nullable
        protected abstract NbtCompound getArguments(CommandContext<ServerCommandSource> var1) throws CommandSyntaxException;

        @Override
        public void executeInner(ServerCommandSource arg, ContextChain<ServerCommandSource> contextChain, ExecutionFlags arg2, ExecutionControl<ServerCommandSource> arg3) throws CommandSyntaxException {
            CommandContext<ServerCommandSource> commandContext = contextChain.getTopContext().copyFor(arg);
            Pair<Identifier, Collection<CommandFunction<ServerCommandSource>>> pair = CommandFunctionArgumentType.getIdentifiedFunctions(commandContext, "name");
            Collection collection = pair.getSecond();
            if (collection.isEmpty()) {
                throw NO_FUNCTIONS_EXCEPTION.create(Text.of(pair.getFirst()));
            }
            NbtCompound lv = this.getArguments(commandContext);
            ServerCommandSource lv2 = FunctionCommand.createFunctionCommandSource(arg);
            if (collection.size() == 1) {
                arg.sendFeedback(() -> Text.translatable("commands.function.scheduled.single", Text.of(((CommandFunction)collection.iterator().next()).id())), true);
            } else {
                arg.sendFeedback(() -> Text.translatable("commands.function.scheduled.multiple", Texts.join(collection.stream().map(CommandFunction::id).toList(), Text::of)), true);
            }
            FunctionCommand.enqueueAction(collection, lv, arg, lv2, arg3, RESULT_REPORTER, arg2);
        }

        @Override
        public /* synthetic */ void executeInner(AbstractServerCommandSource source, ContextChain contextChain, ExecutionFlags flags, ExecutionControl control) throws CommandSyntaxException {
            this.executeInner((ServerCommandSource)source, (ContextChain<ServerCommandSource>)contextChain, flags, (ExecutionControl<ServerCommandSource>)control);
        }
    }
}

