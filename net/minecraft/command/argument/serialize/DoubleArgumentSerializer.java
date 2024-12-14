/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument.serialize;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ArgumentHelper;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;

public class DoubleArgumentSerializer
implements ArgumentSerializer<DoubleArgumentType, Properties> {
    @Override
    public void writePacket(Properties arg, PacketByteBuf arg2) {
        boolean bl = arg.min != -1.7976931348623157E308;
        boolean bl2 = arg.max != Double.MAX_VALUE;
        arg2.writeByte(ArgumentHelper.getMinMaxFlag(bl, bl2));
        if (bl) {
            arg2.writeDouble(arg.min);
        }
        if (bl2) {
            arg2.writeDouble(arg.max);
        }
    }

    @Override
    public Properties fromPacket(PacketByteBuf arg) {
        byte b = arg.readByte();
        double d = ArgumentHelper.hasMinFlag(b) ? arg.readDouble() : -1.7976931348623157E308;
        double e = ArgumentHelper.hasMaxFlag(b) ? arg.readDouble() : Double.MAX_VALUE;
        return new Properties(d, e);
    }

    @Override
    public void writeJson(Properties arg, JsonObject jsonObject) {
        if (arg.min != -1.7976931348623157E308) {
            jsonObject.addProperty("min", arg.min);
        }
        if (arg.max != Double.MAX_VALUE) {
            jsonObject.addProperty("max", arg.max);
        }
    }

    @Override
    public Properties getArgumentTypeProperties(DoubleArgumentType doubleArgumentType) {
        return new Properties(doubleArgumentType.getMinimum(), doubleArgumentType.getMaximum());
    }

    @Override
    public /* synthetic */ ArgumentSerializer.ArgumentTypeProperties fromPacket(PacketByteBuf buf) {
        return this.fromPacket(buf);
    }

    public final class Properties
    implements ArgumentSerializer.ArgumentTypeProperties<DoubleArgumentType> {
        final double min;
        final double max;

        Properties(double min, double max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public DoubleArgumentType createType(CommandRegistryAccess arg) {
            return DoubleArgumentType.doubleArg(this.min, this.max);
        }

        @Override
        public ArgumentSerializer<DoubleArgumentType, ?> getSerializer() {
            return DoubleArgumentSerializer.this;
        }

        @Override
        public /* synthetic */ ArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
            return this.createType(commandRegistryAccess);
        }
    }
}

