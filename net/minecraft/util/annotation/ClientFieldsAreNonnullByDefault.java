/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.NotNull
 */
package net.minecraft.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.meta.TypeQualifierDefault;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;

@NotNull
@TypeQualifierDefault(value={ElementType.FIELD})
@Retention(value=RetentionPolicy.RUNTIME)
@Environment(value=EnvType.CLIENT)
public @interface ClientFieldsAreNonnullByDefault {
}

