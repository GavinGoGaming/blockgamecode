/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.narration;

import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.narration.Narration;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;

@Environment(value=EnvType.CLIENT)
public class ScreenNarrator {
    int currentMessageIndex;
    final Map<PartIndex, Message> narrations = Maps.newTreeMap(Comparator.comparing(partIndex -> partIndex.part).thenComparing(partIndex -> partIndex.depth));

    public void buildNarrations(Consumer<NarrationMessageBuilder> builderConsumer) {
        ++this.currentMessageIndex;
        builderConsumer.accept(new MessageBuilder(0));
    }

    public String buildNarratorText(boolean includeUnchanged) {
        final StringBuilder stringBuilder = new StringBuilder();
        Consumer<String> consumer = new Consumer<String>(){
            private boolean first = true;

            @Override
            public void accept(String string) {
                if (!this.first) {
                    stringBuilder.append(". ");
                }
                this.first = false;
                stringBuilder.append(string);
            }

            @Override
            public /* synthetic */ void accept(Object sentence) {
                this.accept((String)sentence);
            }
        };
        this.narrations.forEach((partIndex, message) -> {
            if (message.index == this.currentMessageIndex && (includeUnchanged || !message.used)) {
                message.narration.forEachSentence(consumer);
                message.used = true;
            }
        });
        return stringBuilder.toString();
    }

    @Environment(value=EnvType.CLIENT)
    class MessageBuilder
    implements NarrationMessageBuilder {
        private final int depth;

        MessageBuilder(int depth) {
            this.depth = depth;
        }

        @Override
        public void put(NarrationPart part, Narration<?> narration) {
            ScreenNarrator.this.narrations.computeIfAbsent(new PartIndex(part, this.depth), partIndex -> new Message()).setNarration(ScreenNarrator.this.currentMessageIndex, narration);
        }

        @Override
        public NarrationMessageBuilder nextMessage() {
            return new MessageBuilder(this.depth + 1);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Message {
        Narration<?> narration = Narration.EMPTY;
        int index = -1;
        boolean used;

        Message() {
        }

        public Message setNarration(int index, Narration<?> narration) {
            if (!this.narration.equals(narration)) {
                this.narration = narration;
                this.used = false;
            } else if (this.index + 1 != index) {
                this.used = false;
            }
            this.index = index;
            return this;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class PartIndex {
        final NarrationPart part;
        final int depth;

        PartIndex(NarrationPart part, int depth) {
            this.part = part;
            this.depth = depth;
        }
    }
}

