/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.session.report;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public enum AbuseReportType {
    CHAT("chat"),
    SKIN("skin"),
    USERNAME("username");

    private final String name;

    private AbuseReportType(String name) {
        this.name = name.toUpperCase(Locale.ROOT);
    }

    public String getName() {
        return this.name;
    }
}

