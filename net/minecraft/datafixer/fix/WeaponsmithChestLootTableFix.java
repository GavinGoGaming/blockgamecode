/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;

public class WeaponsmithChestLootTableFix
extends ChoiceFix {
    public WeaponsmithChestLootTableFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType, "WeaponSmithChestLootTableFix", TypeReferences.BLOCK_ENTITY, "minecraft:chest");
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), dynamic -> {
            String string = dynamic.get("LootTable").asString("");
            return string.equals("minecraft:chests/village_blacksmith") ? dynamic.set("LootTable", dynamic.createString("minecraft:chests/village/village_weaponsmith")) : dynamic;
        });
    }
}

