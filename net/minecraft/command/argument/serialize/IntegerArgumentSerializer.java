/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument.serialize;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ArgumentHelper;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;

public class IntegerArgumentSerializer
implements ArgumentSerializer<IntegerArgumentType, Properties> {
    @Override
    public void writePacket(Properties arg, PacketByteBuf arg2) {
        boolean bl = arg.min != Integer.MIN_VALUE;
        boolean bl2 = arg.max != Integer.MAX_VALUE;
        arg2.writeByte(ArgumentHelper.getMinMaxFlag(bl, bl2));
        if (bl) {
            arg2.writeInt(arg.min);
        }
        if (bl2) {
            arg2.writeInt(arg.max);
        }
    }

    @Override
    public Properties fromPacket(PacketByteBuf arg) {
        byte b = arg.readByte();
        int i = ArgumentHelper.hasMinFlag(b) ? arg.readInt() : Integer.MIN_VALUE;
        int j = ArgumentHelper.hasMaxFlag(b) ? arg.readInt() : Integer.MAX_VALUE;
        return new Properties(i, j);
    }

    @Override
    public void writeJson(Properties arg, JsonObject jsonObject) {
        if (arg.min != Integer.MIN_VALUE) {
            jsonObject.addProperty("min", arg.min);
        }
        if (arg.max != Integer.MAX_VALUE) {
            jsonObject.addProperty("max", arg.max);
        }
    }

    @Override
    public Properties getArgumentTypeProperties(IntegerArgumentType integerArgumentType) {
        return new Properties(integerArgumentType.getMinimum(), integerArgumentType.getMaximum());
    }

    @Override
    public /* synthetic */ ArgumentSerializer.ArgumentTypeProperties fromPacket(PacketByteBuf buf) {
        return this.fromPacket(buf);
    }

    public final class Properties
    implements ArgumentSerializer.ArgumentTypeProperties<IntegerArgumentType> {
        final int min;
        final int max;

        Properties(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public IntegerArgumentType createType(CommandRegistryAccess arg) {
            return IntegerArgumentType.integer(this.min, this.max);
        }

        @Override
        public ArgumentSerializer<IntegerArgumentType, ?> getSerializer() {
            return IntegerArgumentSerializer.this;
        }

        @Override
        public /* synthetic */ ArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
            return this.createType(commandRegistryAccess);
        }
    }
}

