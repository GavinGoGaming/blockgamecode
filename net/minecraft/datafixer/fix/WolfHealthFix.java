/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class WolfHealthFix
extends ChoiceFix {
    private static final String WOLF_ENTITY_ID = "minecraft:wolf";
    private static final String MAX_HEALTH_ATTRIBUTE_ID = "minecraft:generic.max_health";

    public WolfHealthFix(Schema outputSchema) {
        super(outputSchema, false, "FixWolfHealth", TypeReferences.ENTITY, WOLF_ENTITY_ID);
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), wolfDynamic -> {
            MutableBoolean mutableBoolean = new MutableBoolean(false);
            wolfDynamic = wolfDynamic.update("Attributes", attributesDynamic -> attributesDynamic.createList(attributesDynamic.asStream().map(attributeDynamic -> {
                if (MAX_HEALTH_ATTRIBUTE_ID.equals(IdentifierNormalizingSchema.normalize(attributeDynamic.get("Name").asString("")))) {
                    return attributeDynamic.update("Base", baseDynamic -> {
                        if (baseDynamic.asDouble(0.0) == 20.0) {
                            mutableBoolean.setTrue();
                            return baseDynamic.createDouble(40.0);
                        }
                        return baseDynamic;
                    });
                }
                return attributeDynamic;
            })));
            if (mutableBoolean.isTrue()) {
                wolfDynamic = wolfDynamic.update("Health", healthDynamic -> healthDynamic.createFloat(healthDynamic.asFloat(0.0f) * 2.0f));
            }
            return wolfDynamic;
        });
    }
}

