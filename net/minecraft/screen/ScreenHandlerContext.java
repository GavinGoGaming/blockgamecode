/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ScreenHandlerContext {
    public static final ScreenHandlerContext EMPTY = new ScreenHandlerContext(){

        @Override
        public <T> Optional<T> get(BiFunction<World, BlockPos, T> getter) {
            return Optional.empty();
        }
    };

    public static ScreenHandlerContext create(final World world, final BlockPos pos) {
        return new ScreenHandlerContext(){

            @Override
            public <T> Optional<T> get(BiFunction<World, BlockPos, T> getter) {
                return Optional.of(getter.apply(world, pos));
            }
        };
    }

    public <T> Optional<T> get(BiFunction<World, BlockPos, T> var1);

    default public <T> T get(BiFunction<World, BlockPos, T> getter, T defaultValue) {
        return this.get(getter).orElse(defaultValue);
    }

    default public void run(BiConsumer<World, BlockPos> function) {
        this.get((world, pos) -> {
            function.accept((World)world, (BlockPos)pos);
            return Optional.empty();
        });
    }
}

