/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import java.util.function.Supplier;
import org.apache.commons.lang3.ObjectUtils;

public record ModStatus(Confidence confidence, String description) {
    public static ModStatus check(String vanillaBrand, Supplier<String> brandSupplier, String environment, Class<?> clazz) {
        String string3 = brandSupplier.get();
        if (!vanillaBrand.equals(string3)) {
            return new ModStatus(Confidence.DEFINITELY, environment + " brand changed to '" + string3 + "'");
        }
        if (clazz.getSigners() == null) {
            return new ModStatus(Confidence.VERY_LIKELY, environment + " jar signature invalidated");
        }
        return new ModStatus(Confidence.PROBABLY_NOT, environment + " jar signature and brand is untouched");
    }

    public boolean isModded() {
        return this.confidence.modded;
    }

    public ModStatus combine(ModStatus brand) {
        return new ModStatus((Confidence)((Object)ObjectUtils.max((Comparable[])new Confidence[]{this.confidence, brand.confidence})), this.description + "; " + brand.description);
    }

    public String getMessage() {
        return this.confidence.description + " " + this.description;
    }

    public static enum Confidence {
        PROBABLY_NOT("Probably not.", false),
        VERY_LIKELY("Very likely;", true),
        DEFINITELY("Definitely;", true);

        final String description;
        final boolean modded;

        private Confidence(String description, boolean modded) {
            this.description = description;
            this.modded = modded;
        }
    }
}

