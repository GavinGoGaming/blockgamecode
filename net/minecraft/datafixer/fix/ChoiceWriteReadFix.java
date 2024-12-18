/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.View;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.BitSet;
import net.minecraft.util.Util;

public abstract class ChoiceWriteReadFix
extends DataFix {
    private final String name;
    private final String choiceName;
    private final DSL.TypeReference type;

    public ChoiceWriteReadFix(Schema outputSchema, boolean changesType, String name, DSL.TypeReference type, String choiceName) {
        super(outputSchema, changesType);
        this.name = name;
        this.type = type;
        this.choiceName = choiceName;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(this.type);
        Type<?> type2 = this.getInputSchema().getChoiceType(this.type, this.choiceName);
        Type<?> type3 = this.getOutputSchema().getType(this.type);
        Type<?> type4 = this.getOutputSchema().getChoiceType(this.type, this.choiceName);
        OpticFinder<?> opticFinder = DSL.namedChoice(this.choiceName, type2);
        Type<?> type5 = type2.all(ChoiceWriteReadFix.substitutionRewriteResult(type, type3), true, false).view().newType();
        return this.method_56641(type, type3, opticFinder, type4, type5);
    }

    private <S, T, A, B> TypeRewriteRule method_56641(Type<S> inputType, Type<T> outputType, OpticFinder<A> opticFinder, Type<B> outputSubtype, Type<?> rewrittenType) {
        return this.fixTypeEverywhere(this.name, inputType, outputType, dynamicOps -> input -> {
            Typed<Object> typed = new Typed<Object>(inputType, (DynamicOps<?>)dynamicOps, input);
            return typed.update(opticFinder, outputSubtype, object -> {
                Typed<Object> typed = new Typed<Object>((Type<Object>)rewrittenType, (DynamicOps<?>)dynamicOps, object);
                return Util.apply(typed, outputSubtype, this::transform).getValue();
            }).getValue();
        });
    }

    private static <A, B> TypeRewriteRule substitutionRewriteResult(Type<A> inputSubtype, Type<B> outputSubtype) {
        RewriteResult<A, B> rewriteResult = RewriteResult.create(View.create("Patcher", inputSubtype, outputSubtype, dynamicOps -> object -> {
            throw new UnsupportedOperationException();
        }), new BitSet());
        return TypeRewriteRule.everywhere(TypeRewriteRule.ifSame(inputSubtype, rewriteResult), PointFreeRule.nop(), true, true);
    }

    protected abstract <T> Dynamic<T> transform(Dynamic<T> var1);
}

