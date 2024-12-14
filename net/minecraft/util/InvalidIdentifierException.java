/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import org.apache.commons.lang3.StringEscapeUtils;

public class InvalidIdentifierException
extends RuntimeException {
    public InvalidIdentifierException(String message) {
        super(StringEscapeUtils.escapeJava(message));
    }

    public InvalidIdentifierException(String message, Throwable throwable) {
        super(StringEscapeUtils.escapeJava(message), throwable);
    }
}

