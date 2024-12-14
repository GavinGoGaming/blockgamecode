/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public enum SpawnGroup implements StringIdentifiable
{
    MONSTER("monster", 70, false, false, 128),
    CREATURE("creature", 10, true, true, 128),
    AMBIENT("ambient", 15, true, false, 128),
    AXOLOTLS("axolotls", 5, true, false, 128),
    UNDERGROUND_WATER_CREATURE("underground_water_creature", 5, true, false, 128),
    WATER_CREATURE("water_creature", 5, true, false, 128),
    WATER_AMBIENT("water_ambient", 20, true, false, 64),
    MISC("misc", -1, true, true, 128);

    public static final Codec<SpawnGroup> CODEC;
    private final int capacity;
    private final boolean peaceful;
    private final boolean rare;
    private final String name;
    private final int despawnStartRange = 32;
    private final int immediateDespawnRange;

    private SpawnGroup(String name, int spawnCap, boolean peaceful, boolean rare, int immediateDespawnRange) {
        this.name = name;
        this.capacity = spawnCap;
        this.peaceful = peaceful;
        this.rare = rare;
        this.immediateDespawnRange = immediateDespawnRange;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public boolean isPeaceful() {
        return this.peaceful;
    }

    public boolean isRare() {
        return this.rare;
    }

    public int getImmediateDespawnRange() {
        return this.immediateDespawnRange;
    }

    public int getDespawnStartRange() {
        return 32;
    }

    static {
        CODEC = StringIdentifiable.createCodec(SpawnGroup::values);
    }
}

