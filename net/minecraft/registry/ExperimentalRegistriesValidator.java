/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryCloner;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.PlacedFeature;

public class ExperimentalRegistriesValidator {
    public static CompletableFuture<RegistryBuilder.FullPatchesRegistriesPair> validate(CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture, RegistryBuilder builder) {
        return registriesFuture.thenApply(lookup -> {
            DynamicRegistryManager.Immutable lv = DynamicRegistryManager.of(Registries.REGISTRIES);
            RegistryCloner.CloneableRegistries lv2 = new RegistryCloner.CloneableRegistries();
            RegistryLoader.DYNAMIC_REGISTRIES.forEach(entry -> entry.addToCloner(lv2::add));
            RegistryBuilder.FullPatchesRegistriesPair lv3 = builder.createWrapperLookup(lv, (RegistryWrapper.WrapperLookup)lookup, lv2);
            RegistryWrapper.WrapperLookup lv4 = lv3.full();
            Optional<RegistryWrapper.Impl<Biome>> optional = lv4.getOptionalWrapper(RegistryKeys.BIOME);
            Optional<RegistryWrapper.Impl<PlacedFeature>> optional2 = lv4.getOptionalWrapper(RegistryKeys.PLACED_FEATURE);
            if (optional.isPresent() || optional2.isPresent()) {
                BuiltinRegistries.validate(optional2.orElseGet(() -> lookup.getWrapperOrThrow(RegistryKeys.PLACED_FEATURE)), optional.orElseGet(() -> lookup.getWrapperOrThrow(RegistryKeys.BIOME)));
            }
            return lv3;
        });
    }
}

