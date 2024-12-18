/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;
import net.minecraft.datafixer.fix.ItemIdFix;
import net.minecraft.datafixer.fix.ItemInstanceTheFlatteningFix;

public class BlockEntityJukeboxFix
extends ChoiceFix {
    public BlockEntityJukeboxFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType, "BlockEntityJukeboxFix", TypeReferences.BLOCK_ENTITY, "minecraft:jukebox");
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        Type<?> type = this.getInputSchema().getChoiceType(TypeReferences.BLOCK_ENTITY, "minecraft:jukebox");
        Type<?> type2 = type.findFieldType("RecordItem");
        OpticFinder<?> opticFinder = DSL.fieldFinder("RecordItem", type2);
        Dynamic<?> dynamic = inputTyped.get(DSL.remainderFinder());
        int i = dynamic.get("Record").asInt(0);
        if (i > 0) {
            dynamic.remove("Record");
            String string = ItemInstanceTheFlatteningFix.getItem(ItemIdFix.fromId(i), 0);
            if (string != null) {
                Dynamic dynamic2 = dynamic.emptyMap();
                dynamic2 = dynamic2.set("id", dynamic2.createString(string));
                dynamic2 = dynamic2.set("Count", dynamic2.createByte((byte)1));
                return inputTyped.set(opticFinder, type2.readTyped(dynamic2).result().orElseThrow(() -> new IllegalStateException("Could not create record item stack.")).getFirst()).set(DSL.remainderFinder(), dynamic);
            }
        }
        return inputTyped;
    }
}

