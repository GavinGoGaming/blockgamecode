/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument.serialize;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ArgumentHelper;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;

public class FloatArgumentSerializer
implements ArgumentSerializer<FloatArgumentType, Properties> {
    @Override
    public void writePacket(Properties arg, PacketByteBuf arg2) {
        boolean bl = arg.min != -3.4028235E38f;
        boolean bl2 = arg.max != Float.MAX_VALUE;
        arg2.writeByte(ArgumentHelper.getMinMaxFlag(bl, bl2));
        if (bl) {
            arg2.writeFloat(arg.min);
        }
        if (bl2) {
            arg2.writeFloat(arg.max);
        }
    }

    @Override
    public Properties fromPacket(PacketByteBuf arg) {
        byte b = arg.readByte();
        float f = ArgumentHelper.hasMinFlag(b) ? arg.readFloat() : -3.4028235E38f;
        float g = ArgumentHelper.hasMaxFlag(b) ? arg.readFloat() : Float.MAX_VALUE;
        return new Properties(f, g);
    }

    @Override
    public void writeJson(Properties arg, JsonObject jsonObject) {
        if (arg.min != -3.4028235E38f) {
            jsonObject.addProperty("min", Float.valueOf(arg.min));
        }
        if (arg.max != Float.MAX_VALUE) {
            jsonObject.addProperty("max", Float.valueOf(arg.max));
        }
    }

    @Override
    public Properties getArgumentTypeProperties(FloatArgumentType floatArgumentType) {
        return new Properties(floatArgumentType.getMinimum(), floatArgumentType.getMaximum());
    }

    @Override
    public /* synthetic */ ArgumentSerializer.ArgumentTypeProperties fromPacket(PacketByteBuf buf) {
        return this.fromPacket(buf);
    }

    public final class Properties
    implements ArgumentSerializer.ArgumentTypeProperties<FloatArgumentType> {
        final float min;
        final float max;

        Properties(float min, float max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public FloatArgumentType createType(CommandRegistryAccess arg) {
            return FloatArgumentType.floatArg(this.min, this.max);
        }

        @Override
        public ArgumentSerializer<FloatArgumentType, ?> getSerializer() {
            return FloatArgumentSerializer.this;
        }

        @Override
        public /* synthetic */ ArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
            return this.createType(commandRegistryAccess);
        }
    }
}

