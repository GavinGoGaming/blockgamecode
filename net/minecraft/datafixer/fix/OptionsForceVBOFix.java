/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.TypeReferences;

public class OptionsForceVBOFix
extends DataFix {
    public OptionsForceVBOFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("OptionsForceVBOFix", this.getInputSchema().getType(TypeReferences.OPTIONS), optionsTyped -> optionsTyped.update(DSL.remainderFinder(), optionsDynamic -> optionsDynamic.set("useVbo", optionsDynamic.createString("true"))));
    }
}

