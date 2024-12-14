/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.command.BlockDataObject;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.EntityDataObject;
import net.minecraft.command.StorageDataObject;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class DataCommand {
    private static final SimpleCommandExceptionType MERGE_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.data.merge.failed"));
    private static final DynamicCommandExceptionType GET_INVALID_EXCEPTION = new DynamicCommandExceptionType(path -> Text.stringifiedTranslatable("commands.data.get.invalid", path));
    private static final DynamicCommandExceptionType GET_UNKNOWN_EXCEPTION = new DynamicCommandExceptionType(path -> Text.stringifiedTranslatable("commands.data.get.unknown", path));
    private static final SimpleCommandExceptionType GET_MULTIPLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.data.get.multiple"));
    private static final DynamicCommandExceptionType MODIFY_EXPECTED_OBJECT_EXCEPTION = new DynamicCommandExceptionType(nbt -> Text.stringifiedTranslatable("commands.data.modify.expected_object", nbt));
    private static final DynamicCommandExceptionType MODIFY_EXPECTED_VALUE_EXCEPTION = new DynamicCommandExceptionType(nbt -> Text.stringifiedTranslatable("commands.data.modify.expected_value", nbt));
    private static final Dynamic2CommandExceptionType MODIFY_INVALID_SUBSTRING_EXCEPTION = new Dynamic2CommandExceptionType((startIndex, endIndex) -> Text.stringifiedTranslatable("commands.data.modify.invalid_substring", startIndex, endIndex));
    public static final List<Function<String, ObjectType>> OBJECT_TYPE_FACTORIES = ImmutableList.of(EntityDataObject.TYPE_FACTORY, BlockDataObject.TYPE_FACTORY, StorageDataObject.TYPE_FACTORY);
    public static final List<ObjectType> TARGET_OBJECT_TYPES = OBJECT_TYPE_FACTORIES.stream().map(factory -> (ObjectType)factory.apply("target")).collect(ImmutableList.toImmutableList());
    public static final List<ObjectType> SOURCE_OBJECT_TYPES = OBJECT_TYPE_FACTORIES.stream().map(factory -> (ObjectType)factory.apply("source")).collect(ImmutableList.toImmutableList());

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder literalArgumentBuilder = (LiteralArgumentBuilder)CommandManager.literal("data").requires(source -> source.hasPermissionLevel(2));
        for (ObjectType lv : TARGET_OBJECT_TYPES) {
            ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder.then(lv.addArgumentsToBuilder(CommandManager.literal("merge"), builder -> builder.then(CommandManager.argument("nbt", NbtCompoundArgumentType.nbtCompound()).executes(context -> DataCommand.executeMerge((ServerCommandSource)context.getSource(), lv.getObject(context), NbtCompoundArgumentType.getNbtCompound(context, "nbt"))))))).then(lv.addArgumentsToBuilder(CommandManager.literal("get"), builder -> ((ArgumentBuilder)builder.executes(context -> DataCommand.executeGet((ServerCommandSource)context.getSource(), lv.getObject(context)))).then(((RequiredArgumentBuilder)CommandManager.argument("path", NbtPathArgumentType.nbtPath()).executes(context -> DataCommand.executeGet((ServerCommandSource)context.getSource(), lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path")))).then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).executes(context -> DataCommand.executeGet((ServerCommandSource)context.getSource(), lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path"), DoubleArgumentType.getDouble(context, "scale")))))))).then(lv.addArgumentsToBuilder(CommandManager.literal("remove"), builder -> builder.then(CommandManager.argument("path", NbtPathArgumentType.nbtPath()).executes(context -> DataCommand.executeRemove((ServerCommandSource)context.getSource(), lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path"))))))).then(DataCommand.addModifyArgument((builder, modifier) -> ((ArgumentBuilder)((ArgumentBuilder)((ArgumentBuilder)((ArgumentBuilder)builder.then(CommandManager.literal("insert").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("index", IntegerArgumentType.integer()).then(modifier.create((context, sourceNbt, path, elements) -> path.insert(IntegerArgumentType.getInteger(context, "index"), sourceNbt, elements)))))).then(CommandManager.literal("prepend").then(modifier.create((context, sourceNbt, path, elements) -> path.insert(0, sourceNbt, elements))))).then(CommandManager.literal("append").then(modifier.create((context, sourceNbt, path, elements) -> path.insert(-1, sourceNbt, elements))))).then(CommandManager.literal("set").then(modifier.create((context, sourceNbt, path, elements) -> path.put(sourceNbt, (NbtElement)Iterables.getLast(elements)))))).then(CommandManager.literal("merge").then(modifier.create((context, element, path, elements) -> {
                NbtCompound lv = new NbtCompound();
                for (NbtElement lv2 : elements) {
                    if (NbtPathArgumentType.NbtPath.isTooDeep(lv2, 0)) {
                        throw NbtPathArgumentType.TOO_DEEP_EXCEPTION.create();
                    }
                    if (lv2 instanceof NbtCompound) {
                        NbtCompound lv3 = (NbtCompound)lv2;
                        lv.copyFrom(lv3);
                        continue;
                    }
                    throw MODIFY_EXPECTED_OBJECT_EXCEPTION.create(lv2);
                }
                List<NbtElement> collection = path.getOrInit(element, NbtCompound::new);
                int i = 0;
                for (NbtElement lv4 : collection) {
                    if (!(lv4 instanceof NbtCompound)) {
                        throw MODIFY_EXPECTED_OBJECT_EXCEPTION.create(lv4);
                    }
                    NbtCompound lv5 = (NbtCompound)lv4;
                    NbtCompound lv6 = lv5.copy();
                    lv5.copyFrom(lv);
                    i += lv6.equals(lv5) ? 0 : 1;
                }
                return i;
            })))));
        }
        dispatcher.register(literalArgumentBuilder);
    }

    private static String asString(NbtElement nbt) throws CommandSyntaxException {
        if (nbt.getNbtType().isImmutable()) {
            return nbt.asString();
        }
        throw MODIFY_EXPECTED_VALUE_EXCEPTION.create(nbt);
    }

    private static List<NbtElement> mapValues(List<NbtElement> list, Processor processor) throws CommandSyntaxException {
        ArrayList<NbtElement> list2 = new ArrayList<NbtElement>(list.size());
        for (NbtElement lv : list) {
            String string = DataCommand.asString(lv);
            list2.add(NbtString.of(processor.process(string)));
        }
        return list2;
    }

    private static ArgumentBuilder<ServerCommandSource, ?> addModifyArgument(BiConsumer<ArgumentBuilder<ServerCommandSource, ?>, ModifyArgumentCreator> subArgumentAdder) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("modify");
        for (ObjectType lv : TARGET_OBJECT_TYPES) {
            lv.addArgumentsToBuilder(literalArgumentBuilder, builder -> {
                RequiredArgumentBuilder<ServerCommandSource, NbtPathArgumentType.NbtPath> argumentBuilder2 = CommandManager.argument("targetPath", NbtPathArgumentType.nbtPath());
                for (ObjectType lv : SOURCE_OBJECT_TYPES) {
                    subArgumentAdder.accept(argumentBuilder2, operation -> lv.addArgumentsToBuilder(CommandManager.literal("from"), builderx -> ((ArgumentBuilder)builderx.executes(context -> DataCommand.executeModify(context, lv, operation, DataCommand.getValues(context, lv)))).then(CommandManager.argument("sourcePath", NbtPathArgumentType.nbtPath()).executes(context -> DataCommand.executeModify(context, lv, operation, DataCommand.getValuesByPath(context, lv))))));
                    subArgumentAdder.accept(argumentBuilder2, operation -> lv.addArgumentsToBuilder(CommandManager.literal("string"), builderx -> ((ArgumentBuilder)builderx.executes(context -> DataCommand.executeModify(context, lv, operation, DataCommand.mapValues(DataCommand.getValues(context, lv), value -> value)))).then(((RequiredArgumentBuilder)CommandManager.argument("sourcePath", NbtPathArgumentType.nbtPath()).executes(context -> DataCommand.executeModify(context, lv, operation, DataCommand.mapValues(DataCommand.getValuesByPath(context, lv), value -> value)))).then(((RequiredArgumentBuilder)CommandManager.argument("start", IntegerArgumentType.integer()).executes(context -> DataCommand.executeModify(context, lv, operation, DataCommand.mapValues(DataCommand.getValuesByPath(context, lv), value -> DataCommand.substring(value, IntegerArgumentType.getInteger(context, "start")))))).then(CommandManager.argument("end", IntegerArgumentType.integer()).executes(context -> DataCommand.executeModify(context, lv, operation, DataCommand.mapValues(DataCommand.getValuesByPath(context, lv), value -> DataCommand.substring(value, IntegerArgumentType.getInteger(context, "start"), IntegerArgumentType.getInteger(context, "end"))))))))));
                }
                subArgumentAdder.accept(argumentBuilder2, modifier -> CommandManager.literal("value").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("value", NbtElementArgumentType.nbtElement()).executes(context -> {
                    List<NbtElement> list = Collections.singletonList(NbtElementArgumentType.getNbtElement(context, "value"));
                    return DataCommand.executeModify(context, lv, modifier, list);
                })));
                return builder.then(argumentBuilder2);
            });
        }
        return literalArgumentBuilder;
    }

    private static String substringInternal(String string, int startIndex, int endIndex) throws CommandSyntaxException {
        if (startIndex < 0 || endIndex > string.length() || startIndex > endIndex) {
            throw MODIFY_INVALID_SUBSTRING_EXCEPTION.create(startIndex, endIndex);
        }
        return string.substring(startIndex, endIndex);
    }

    private static String substring(String string, int startIndex, int endIndex) throws CommandSyntaxException {
        int k = string.length();
        int l = DataCommand.getSubstringIndex(startIndex, k);
        int m = DataCommand.getSubstringIndex(endIndex, k);
        return DataCommand.substringInternal(string, l, m);
    }

    private static String substring(String string, int startIndex) throws CommandSyntaxException {
        int j = string.length();
        return DataCommand.substringInternal(string, DataCommand.getSubstringIndex(startIndex, j), j);
    }

    private static int getSubstringIndex(int index, int length) {
        return index >= 0 ? index : length + index;
    }

    private static List<NbtElement> getValues(CommandContext<ServerCommandSource> context, ObjectType objectType) throws CommandSyntaxException {
        DataCommandObject lv = objectType.getObject(context);
        return Collections.singletonList(lv.getNbt());
    }

    private static List<NbtElement> getValuesByPath(CommandContext<ServerCommandSource> context, ObjectType objectType) throws CommandSyntaxException {
        DataCommandObject lv = objectType.getObject(context);
        NbtPathArgumentType.NbtPath lv2 = NbtPathArgumentType.getNbtPath(context, "sourcePath");
        return lv2.get(lv.getNbt());
    }

    private static int executeModify(CommandContext<ServerCommandSource> context, ObjectType objectType, ModifyOperation modifier, List<NbtElement> elements) throws CommandSyntaxException {
        DataCommandObject lv = objectType.getObject(context);
        NbtPathArgumentType.NbtPath lv2 = NbtPathArgumentType.getNbtPath(context, "targetPath");
        NbtCompound lv3 = lv.getNbt();
        int i = modifier.modify(context, lv3, lv2, elements);
        if (i == 0) {
            throw MERGE_FAILED_EXCEPTION.create();
        }
        lv.setNbt(lv3);
        context.getSource().sendFeedback(() -> lv.feedbackModify(), true);
        return i;
    }

    private static int executeRemove(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
        NbtCompound lv = object.getNbt();
        int i = path.remove(lv);
        if (i == 0) {
            throw MERGE_FAILED_EXCEPTION.create();
        }
        object.setNbt(lv);
        source.sendFeedback(() -> object.feedbackModify(), true);
        return i;
    }

    public static NbtElement getNbt(NbtPathArgumentType.NbtPath path, DataCommandObject object) throws CommandSyntaxException {
        List<NbtElement> collection = path.get(object.getNbt());
        Iterator iterator = collection.iterator();
        NbtElement lv = (NbtElement)iterator.next();
        if (iterator.hasNext()) {
            throw GET_MULTIPLE_EXCEPTION.create();
        }
        return lv;
    }

    private static int executeGet(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
        int i;
        NbtElement lv = DataCommand.getNbt(path, object);
        if (lv instanceof AbstractNbtNumber) {
            i = MathHelper.floor(((AbstractNbtNumber)lv).doubleValue());
        } else if (lv instanceof AbstractNbtList) {
            i = ((AbstractNbtList)lv).size();
        } else if (lv instanceof NbtCompound) {
            i = ((NbtCompound)lv).getSize();
        } else if (lv instanceof NbtString) {
            i = lv.asString().length();
        } else {
            throw GET_UNKNOWN_EXCEPTION.create(path.toString());
        }
        source.sendFeedback(() -> object.feedbackQuery(lv), false);
        return i;
    }

    private static int executeGet(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path, double scale) throws CommandSyntaxException {
        NbtElement lv = DataCommand.getNbt(path, object);
        if (!(lv instanceof AbstractNbtNumber)) {
            throw GET_INVALID_EXCEPTION.create(path.toString());
        }
        int i = MathHelper.floor(((AbstractNbtNumber)lv).doubleValue() * scale);
        source.sendFeedback(() -> object.feedbackGet(path, scale, i), false);
        return i;
    }

    private static int executeGet(ServerCommandSource source, DataCommandObject object) throws CommandSyntaxException {
        NbtCompound lv = object.getNbt();
        source.sendFeedback(() -> object.feedbackQuery(lv), false);
        return 1;
    }

    private static int executeMerge(ServerCommandSource source, DataCommandObject object, NbtCompound nbt) throws CommandSyntaxException {
        NbtCompound lv = object.getNbt();
        if (NbtPathArgumentType.NbtPath.isTooDeep(nbt, 0)) {
            throw NbtPathArgumentType.TOO_DEEP_EXCEPTION.create();
        }
        NbtCompound lv2 = lv.copy().copyFrom(nbt);
        if (lv.equals(lv2)) {
            throw MERGE_FAILED_EXCEPTION.create();
        }
        object.setNbt(lv2);
        source.sendFeedback(() -> object.feedbackModify(), true);
        return 1;
    }

    public static interface ObjectType {
        public DataCommandObject getObject(CommandContext<ServerCommandSource> var1) throws CommandSyntaxException;

        public ArgumentBuilder<ServerCommandSource, ?> addArgumentsToBuilder(ArgumentBuilder<ServerCommandSource, ?> var1, Function<ArgumentBuilder<ServerCommandSource, ?>, ArgumentBuilder<ServerCommandSource, ?>> var2);
    }

    @FunctionalInterface
    static interface Processor {
        public String process(String var1) throws CommandSyntaxException;
    }

    @FunctionalInterface
    static interface ModifyOperation {
        public int modify(CommandContext<ServerCommandSource> var1, NbtCompound var2, NbtPathArgumentType.NbtPath var3, List<NbtElement> var4) throws CommandSyntaxException;
    }

    @FunctionalInterface
    static interface ModifyArgumentCreator {
        public ArgumentBuilder<ServerCommandSource, ?> create(ModifyOperation var1);
    }
}

