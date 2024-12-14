/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.text;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.message.MessageType;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public record Decoration(String translationKey, List<Parameter> parameters, Style style) {
    public static final Codec<Decoration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("translation_key")).forGetter(Decoration::translationKey), ((MapCodec)Parameter.CODEC.listOf().fieldOf("parameters")).forGetter(Decoration::parameters), Style.Codecs.CODEC.optionalFieldOf("style", Style.EMPTY).forGetter(Decoration::style)).apply((Applicative<Decoration, ?>)instance, Decoration::new));
    public static final PacketCodec<RegistryByteBuf, Decoration> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.STRING, Decoration::translationKey, Parameter.PACKET_CODEC.collect(PacketCodecs.toList()), Decoration::parameters, Style.Codecs.PACKET_CODEC, Decoration::style, Decoration::new);

    public static Decoration ofChat(String translationKey) {
        return new Decoration(translationKey, List.of(Parameter.SENDER, Parameter.CONTENT), Style.EMPTY);
    }

    public static Decoration ofIncomingMessage(String translationKey) {
        Style lv = Style.EMPTY.withColor(Formatting.GRAY).withItalic(true);
        return new Decoration(translationKey, List.of(Parameter.SENDER, Parameter.CONTENT), lv);
    }

    public static Decoration ofOutgoingMessage(String translationKey) {
        Style lv = Style.EMPTY.withColor(Formatting.GRAY).withItalic(true);
        return new Decoration(translationKey, List.of(Parameter.TARGET, Parameter.CONTENT), lv);
    }

    public static Decoration ofTeamMessage(String translationKey) {
        return new Decoration(translationKey, List.of(Parameter.TARGET, Parameter.SENDER, Parameter.CONTENT), Style.EMPTY);
    }

    public Text apply(Text content, MessageType.Parameters params) {
        Object[] objects = this.collectArguments(content, params);
        return Text.translatable(this.translationKey, objects).fillStyle(this.style);
    }

    private Text[] collectArguments(Text content, MessageType.Parameters params) {
        Text[] lvs = new Text[this.parameters.size()];
        for (int i = 0; i < lvs.length; ++i) {
            Parameter lv = this.parameters.get(i);
            lvs[i] = lv.apply(content, params);
        }
        return lvs;
    }

    public static enum Parameter implements StringIdentifiable
    {
        SENDER(0, "sender", (content, params) -> params.name()),
        TARGET(1, "target", (content, params) -> params.targetName().orElse(ScreenTexts.EMPTY)),
        CONTENT(2, "content", (content, params) -> content);

        private static final IntFunction<Parameter> BY_ID;
        public static final Codec<Parameter> CODEC;
        public static final PacketCodec<ByteBuf, Parameter> PACKET_CODEC;
        private final int id;
        private final String name;
        private final Selector selector;

        private Parameter(int id, String name, Selector selector) {
            this.id = id;
            this.name = name;
            this.selector = selector;
        }

        public Text apply(Text content, MessageType.Parameters params) {
            return this.selector.select(content, params);
        }

        @Override
        public String asString() {
            return this.name;
        }

        static {
            BY_ID = ValueLists.createIdToValueFunction(parameter -> parameter.id, Parameter.values(), ValueLists.OutOfBoundsHandling.ZERO);
            CODEC = StringIdentifiable.createCodec(Parameter::values);
            PACKET_CODEC = PacketCodecs.indexed(BY_ID, parameter -> parameter.id);
        }

        public static interface Selector {
            public Text select(Text var1, MessageType.Parameters var2);
        }
    }
}

