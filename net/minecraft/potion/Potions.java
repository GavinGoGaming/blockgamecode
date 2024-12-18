/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.potion;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class Potions {
    public static final RegistryEntry<Potion> WATER = Potions.register("water", new Potion(new StatusEffectInstance[0]));
    public static final RegistryEntry<Potion> MUNDANE = Potions.register("mundane", new Potion(new StatusEffectInstance[0]));
    public static final RegistryEntry<Potion> THICK = Potions.register("thick", new Potion(new StatusEffectInstance[0]));
    public static final RegistryEntry<Potion> AWKWARD = Potions.register("awkward", new Potion(new StatusEffectInstance[0]));
    public static final RegistryEntry<Potion> NIGHT_VISION = Potions.register("night_vision", new Potion(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 3600)));
    public static final RegistryEntry<Potion> LONG_NIGHT_VISION = Potions.register("long_night_vision", new Potion("night_vision", new StatusEffectInstance(StatusEffects.NIGHT_VISION, 9600)));
    public static final RegistryEntry<Potion> INVISIBILITY = Potions.register("invisibility", new Potion(new StatusEffectInstance(StatusEffects.INVISIBILITY, 3600)));
    public static final RegistryEntry<Potion> LONG_INVISIBILITY = Potions.register("long_invisibility", new Potion("invisibility", new StatusEffectInstance(StatusEffects.INVISIBILITY, 9600)));
    public static final RegistryEntry<Potion> LEAPING = Potions.register("leaping", new Potion(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 3600)));
    public static final RegistryEntry<Potion> LONG_LEAPING = Potions.register("long_leaping", new Potion("leaping", new StatusEffectInstance(StatusEffects.JUMP_BOOST, 9600)));
    public static final RegistryEntry<Potion> STRONG_LEAPING = Potions.register("strong_leaping", new Potion("leaping", new StatusEffectInstance(StatusEffects.JUMP_BOOST, 1800, 1)));
    public static final RegistryEntry<Potion> FIRE_RESISTANCE = Potions.register("fire_resistance", new Potion(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 3600)));
    public static final RegistryEntry<Potion> LONG_FIRE_RESISTANCE = Potions.register("long_fire_resistance", new Potion("fire_resistance", new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 9600)));
    public static final RegistryEntry<Potion> SWIFTNESS = Potions.register("swiftness", new Potion(new StatusEffectInstance(StatusEffects.SPEED, 3600)));
    public static final RegistryEntry<Potion> LONG_SWIFTNESS = Potions.register("long_swiftness", new Potion("swiftness", new StatusEffectInstance(StatusEffects.SPEED, 9600)));
    public static final RegistryEntry<Potion> STRONG_SWIFTNESS = Potions.register("strong_swiftness", new Potion("swiftness", new StatusEffectInstance(StatusEffects.SPEED, 1800, 1)));
    public static final RegistryEntry<Potion> SLOWNESS = Potions.register("slowness", new Potion(new StatusEffectInstance(StatusEffects.SLOWNESS, 1800)));
    public static final RegistryEntry<Potion> LONG_SLOWNESS = Potions.register("long_slowness", new Potion("slowness", new StatusEffectInstance(StatusEffects.SLOWNESS, 4800)));
    public static final RegistryEntry<Potion> STRONG_SLOWNESS = Potions.register("strong_slowness", new Potion("slowness", new StatusEffectInstance(StatusEffects.SLOWNESS, 400, 3)));
    public static final RegistryEntry<Potion> TURTLE_MASTER = Potions.register("turtle_master", new Potion("turtle_master", new StatusEffectInstance(StatusEffects.SLOWNESS, 400, 3), new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 2)));
    public static final RegistryEntry<Potion> LONG_TURTLE_MASTER = Potions.register("long_turtle_master", new Potion("turtle_master", new StatusEffectInstance(StatusEffects.SLOWNESS, 800, 3), new StatusEffectInstance(StatusEffects.RESISTANCE, 800, 2)));
    public static final RegistryEntry<Potion> STRONG_TURTLE_MASTER = Potions.register("strong_turtle_master", new Potion("turtle_master", new StatusEffectInstance(StatusEffects.SLOWNESS, 400, 5), new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 3)));
    public static final RegistryEntry<Potion> WATER_BREATHING = Potions.register("water_breathing", new Potion(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 3600)));
    public static final RegistryEntry<Potion> LONG_WATER_BREATHING = Potions.register("long_water_breathing", new Potion("water_breathing", new StatusEffectInstance(StatusEffects.WATER_BREATHING, 9600)));
    public static final RegistryEntry<Potion> HEALING = Potions.register("healing", new Potion(new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 1)));
    public static final RegistryEntry<Potion> STRONG_HEALING = Potions.register("strong_healing", new Potion("healing", new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 1, 1)));
    public static final RegistryEntry<Potion> HARMING = Potions.register("harming", new Potion(new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1)));
    public static final RegistryEntry<Potion> STRONG_HARMING = Potions.register("strong_harming", new Potion("harming", new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 1)));
    public static final RegistryEntry<Potion> POISON = Potions.register("poison", new Potion(new StatusEffectInstance(StatusEffects.POISON, 900)));
    public static final RegistryEntry<Potion> LONG_POISON = Potions.register("long_poison", new Potion("poison", new StatusEffectInstance(StatusEffects.POISON, 1800)));
    public static final RegistryEntry<Potion> STRONG_POISON = Potions.register("strong_poison", new Potion("poison", new StatusEffectInstance(StatusEffects.POISON, 432, 1)));
    public static final RegistryEntry<Potion> REGENERATION = Potions.register("regeneration", new Potion(new StatusEffectInstance(StatusEffects.REGENERATION, 900)));
    public static final RegistryEntry<Potion> LONG_REGENERATION = Potions.register("long_regeneration", new Potion("regeneration", new StatusEffectInstance(StatusEffects.REGENERATION, 1800)));
    public static final RegistryEntry<Potion> STRONG_REGENERATION = Potions.register("strong_regeneration", new Potion("regeneration", new StatusEffectInstance(StatusEffects.REGENERATION, 450, 1)));
    public static final RegistryEntry<Potion> STRENGTH = Potions.register("strength", new Potion(new StatusEffectInstance(StatusEffects.STRENGTH, 3600)));
    public static final RegistryEntry<Potion> LONG_STRENGTH = Potions.register("long_strength", new Potion("strength", new StatusEffectInstance(StatusEffects.STRENGTH, 9600)));
    public static final RegistryEntry<Potion> STRONG_STRENGTH = Potions.register("strong_strength", new Potion("strength", new StatusEffectInstance(StatusEffects.STRENGTH, 1800, 1)));
    public static final RegistryEntry<Potion> WEAKNESS = Potions.register("weakness", new Potion(new StatusEffectInstance(StatusEffects.WEAKNESS, 1800)));
    public static final RegistryEntry<Potion> LONG_WEAKNESS = Potions.register("long_weakness", new Potion("weakness", new StatusEffectInstance(StatusEffects.WEAKNESS, 4800)));
    public static final RegistryEntry<Potion> LUCK = Potions.register("luck", new Potion("luck", new StatusEffectInstance(StatusEffects.LUCK, 6000)));
    public static final RegistryEntry<Potion> SLOW_FALLING = Potions.register("slow_falling", new Potion(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 1800)));
    public static final RegistryEntry<Potion> LONG_SLOW_FALLING = Potions.register("long_slow_falling", new Potion("slow_falling", new StatusEffectInstance(StatusEffects.SLOW_FALLING, 4800)));
    public static final RegistryEntry<Potion> WIND_CHARGED = Potions.register("wind_charged", new Potion("wind_charged", new StatusEffectInstance(StatusEffects.WIND_CHARGED, 3600)));
    public static final RegistryEntry<Potion> WEAVING = Potions.register("weaving", new Potion("weaving", new StatusEffectInstance(StatusEffects.WEAVING, 3600)));
    public static final RegistryEntry<Potion> OOZING = Potions.register("oozing", new Potion("oozing", new StatusEffectInstance(StatusEffects.OOZING, 3600)));
    public static final RegistryEntry<Potion> INFESTED = Potions.register("infested", new Potion("infested", new StatusEffectInstance(StatusEffects.INFESTED, 3600)));

    private static RegistryEntry<Potion> register(String name, Potion potion) {
        return Registry.registerReference(Registries.POTION, Identifier.ofVanilla(name), potion);
    }

    public static RegistryEntry<Potion> registerAndGetDefault(Registry<Potion> registry) {
        return WATER;
    }
}

