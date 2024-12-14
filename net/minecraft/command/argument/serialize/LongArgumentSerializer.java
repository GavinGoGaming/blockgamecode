/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument.serialize;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ArgumentHelper;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;

public class LongArgumentSerializer
implements ArgumentSerializer<LongArgumentType, Properties> {
    @Override
    public void writePacket(Properties arg, PacketByteBuf arg2) {
        boolean bl = arg.min != Long.MIN_VALUE;
        boolean bl2 = arg.max != Long.MAX_VALUE;
        arg2.writeByte(ArgumentHelper.getMinMaxFlag(bl, bl2));
        if (bl) {
            arg2.writeLong(arg.min);
        }
        if (bl2) {
            arg2.writeLong(arg.max);
        }
    }

    @Override
    public Properties fromPacket(PacketByteBuf arg) {
        byte b = arg.readByte();
        long l = ArgumentHelper.hasMinFlag(b) ? arg.readLong() : Long.MIN_VALUE;
        long m = ArgumentHelper.hasMaxFlag(b) ? arg.readLong() : Long.MAX_VALUE;
        return new Properties(l, m);
    }

    @Override
    public void writeJson(Properties arg, JsonObject jsonObject) {
        if (arg.min != Long.MIN_VALUE) {
            jsonObject.addProperty("min", arg.min);
        }
        if (arg.max != Long.MAX_VALUE) {
            jsonObject.addProperty("max", arg.max);
        }
    }

    @Override
    public Properties getArgumentTypeProperties(LongArgumentType longArgumentType) {
        return new Properties(longArgumentType.getMinimum(), longArgumentType.getMaximum());
    }

    @Override
    public /* synthetic */ ArgumentSerializer.ArgumentTypeProperties fromPacket(PacketByteBuf buf) {
        return this.fromPacket(buf);
    }

    public final class Properties
    implements ArgumentSerializer.ArgumentTypeProperties<LongArgumentType> {
        final long min;
        final long max;

        Properties(long min, long max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public LongArgumentType createType(CommandRegistryAccess arg) {
            return LongArgumentType.longArg(this.min, this.max);
        }

        @Override
        public ArgumentSerializer<LongArgumentType, ?> getSerializer() {
            return LongArgumentSerializer.this;
        }

        @Override
        public /* synthetic */ ArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
            return this.createType(commandRegistryAccess);
        }
    }
}

