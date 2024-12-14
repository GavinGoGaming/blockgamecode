/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.timer;

import java.util.Collection;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.timer.Timer;
import net.minecraft.world.timer.TimerCallback;

public class FunctionTagTimerCallback
implements TimerCallback<MinecraftServer> {
    final Identifier name;

    public FunctionTagTimerCallback(Identifier name) {
        this.name = name;
    }

    @Override
    public void call(MinecraftServer minecraftServer, Timer<MinecraftServer> arg, long l) {
        CommandFunctionManager lv = minecraftServer.getCommandFunctionManager();
        Collection<CommandFunction<ServerCommandSource>> collection = lv.getTag(this.name);
        for (CommandFunction<ServerCommandSource> lv2 : collection) {
            lv.execute(lv2, lv.getScheduledCommandSource());
        }
    }

    @Override
    public /* synthetic */ void call(Object server, Timer events, long time) {
        this.call((MinecraftServer)server, (Timer<MinecraftServer>)events, time);
    }

    public static class Serializer
    extends TimerCallback.Serializer<MinecraftServer, FunctionTagTimerCallback> {
        public Serializer() {
            super(Identifier.ofVanilla("function_tag"), FunctionTagTimerCallback.class);
        }

        @Override
        public void serialize(NbtCompound arg, FunctionTagTimerCallback arg2) {
            arg.putString("Name", arg2.name.toString());
        }

        @Override
        public FunctionTagTimerCallback deserialize(NbtCompound arg) {
            Identifier lv = Identifier.of(arg.getString("Name"));
            return new FunctionTagTimerCallback(lv);
        }

        @Override
        public /* synthetic */ TimerCallback deserialize(NbtCompound nbt) {
            return this.deserialize(nbt);
        }
    }
}

