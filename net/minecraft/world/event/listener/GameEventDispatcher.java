/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.event.listener;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;

public interface GameEventDispatcher {
    public static final GameEventDispatcher EMPTY = new GameEventDispatcher(){

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void addListener(GameEventListener listener) {
        }

        @Override
        public void removeListener(GameEventListener listener) {
        }

        @Override
        public boolean dispatch(RegistryEntry<GameEvent> event, Vec3d pos, GameEvent.Emitter emitter, DispatchCallback callback) {
            return false;
        }
    };

    public boolean isEmpty();

    public void addListener(GameEventListener var1);

    public void removeListener(GameEventListener var1);

    public boolean dispatch(RegistryEntry<GameEvent> var1, Vec3d var2, GameEvent.Emitter var3, DispatchCallback var4);

    @FunctionalInterface
    public static interface DispatchCallback {
        public void visit(GameEventListener var1, Vec3d var2);
    }
}

