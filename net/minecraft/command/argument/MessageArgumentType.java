/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.command.argument.SignedArgumentType;
import net.minecraft.network.message.MessageDecorator;
import net.minecraft.network.message.SignedCommandArguments;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class MessageArgumentType
implements SignedArgumentType<MessageFormat> {
    private static final Collection<String> EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");
    static final Dynamic2CommandExceptionType MESSAGE_TOO_LONG_EXCEPTION = new Dynamic2CommandExceptionType((length, maxLength) -> Text.stringifiedTranslatable("argument.message.too_long", length, maxLength));

    public static MessageArgumentType message() {
        return new MessageArgumentType();
    }

    public static Text getMessage(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        MessageFormat lv = context.getArgument(name, MessageFormat.class);
        return lv.format(context.getSource());
    }

    public static void getSignedMessage(CommandContext<ServerCommandSource> context, String name, Consumer<SignedMessage> callback) throws CommandSyntaxException {
        MessageFormat lv = context.getArgument(name, MessageFormat.class);
        ServerCommandSource lv2 = context.getSource();
        Text lv3 = lv.format(lv2);
        SignedCommandArguments lv4 = lv2.getSignedArguments();
        SignedMessage lv5 = lv4.getMessage(name);
        if (lv5 != null) {
            MessageArgumentType.chain(callback, lv2, lv5.withUnsignedContent(lv3));
        } else {
            MessageArgumentType.chainUnsigned(callback, lv2, SignedMessage.ofUnsigned(lv.contents).withUnsignedContent(lv3));
        }
    }

    private static void chain(Consumer<SignedMessage> callback, ServerCommandSource source, SignedMessage message) {
        MinecraftServer minecraftServer = source.getServer();
        CompletableFuture<FilteredMessage> completableFuture = MessageArgumentType.filterText(source, message);
        Text lv = minecraftServer.getMessageDecorator().decorate(source.getPlayer(), message.getContent());
        source.getMessageChainTaskQueue().append(completableFuture, filtered -> {
            SignedMessage lv = message.withUnsignedContent(lv).withFilterMask(filtered.mask());
            callback.accept(lv);
        });
    }

    private static void chainUnsigned(Consumer<SignedMessage> callback, ServerCommandSource source, SignedMessage message) {
        MessageDecorator lv = source.getServer().getMessageDecorator();
        Text lv2 = lv.decorate(source.getPlayer(), message.getContent());
        callback.accept(message.withUnsignedContent(lv2));
    }

    private static CompletableFuture<FilteredMessage> filterText(ServerCommandSource source, SignedMessage message) {
        ServerPlayerEntity lv = source.getPlayer();
        if (lv != null && message.canVerifyFrom(lv.getUuid())) {
            return lv.getTextStream().filterText(message.getSignedContent());
        }
        return CompletableFuture.completedFuture(FilteredMessage.permitted(message.getSignedContent()));
    }

    @Override
    public MessageFormat parse(StringReader stringReader) throws CommandSyntaxException {
        return MessageFormat.parse(stringReader, true);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }

    public record MessageFormat(String contents, MessageSelector[] selectors) {
        Text format(ServerCommandSource source) throws CommandSyntaxException {
            return this.format(source, source.hasPermissionLevel(2));
        }

        public Text format(ServerCommandSource source, boolean canUseSelectors) throws CommandSyntaxException {
            if (this.selectors.length == 0 || !canUseSelectors) {
                return Text.literal(this.contents);
            }
            MutableText lv = Text.literal(this.contents.substring(0, this.selectors[0].start()));
            int i = this.selectors[0].start();
            for (MessageSelector lv2 : this.selectors) {
                Text lv3 = lv2.format(source);
                if (i < lv2.start()) {
                    lv.append(this.contents.substring(i, lv2.start()));
                }
                lv.append(lv3);
                i = lv2.end();
            }
            if (i < this.contents.length()) {
                lv.append(this.contents.substring(i));
            }
            return lv;
        }

        public static MessageFormat parse(StringReader reader, boolean canUseSelectors) throws CommandSyntaxException {
            if (reader.getRemainingLength() > 256) {
                throw MESSAGE_TOO_LONG_EXCEPTION.create(reader.getRemainingLength(), 256);
            }
            String string = reader.getRemaining();
            if (!canUseSelectors) {
                reader.setCursor(reader.getTotalLength());
                return new MessageFormat(string, new MessageSelector[0]);
            }
            ArrayList<MessageSelector> list = Lists.newArrayList();
            int i = reader.getCursor();
            while (reader.canRead()) {
                if (reader.peek() == '@') {
                    EntitySelector lv2;
                    int j = reader.getCursor();
                    try {
                        EntitySelectorReader lv = new EntitySelectorReader(reader);
                        lv2 = lv.read();
                    } catch (CommandSyntaxException commandSyntaxException) {
                        if (commandSyntaxException.getType() == EntitySelectorReader.MISSING_EXCEPTION || commandSyntaxException.getType() == EntitySelectorReader.UNKNOWN_SELECTOR_EXCEPTION) {
                            reader.setCursor(j + 1);
                            continue;
                        }
                        throw commandSyntaxException;
                    }
                    list.add(new MessageSelector(j - i, reader.getCursor() - i, lv2));
                    continue;
                }
                reader.skip();
            }
            return new MessageFormat(string, list.toArray(new MessageSelector[0]));
        }
    }

    public record MessageSelector(int start, int end, EntitySelector selector) {
        public Text format(ServerCommandSource source) throws CommandSyntaxException {
            return EntitySelector.getNames(this.selector.getEntities(source));
        }
    }
}

