/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.listener;

import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;

public interface PacketListener {
    public NetworkSide getSide();

    public NetworkPhase getPhase();

    public void onDisconnected(DisconnectionInfo var1);

    default public void onPacketException(Packet packet, Exception exception) throws CrashException {
        throw NetworkThreadUtils.createCrashException(exception, packet, this);
    }

    default public DisconnectionInfo createDisconnectionInfo(Text reason, Throwable exception) {
        return new DisconnectionInfo(reason);
    }

    public boolean isConnectionOpen();

    default public boolean accepts(Packet<?> packet) {
        return this.isConnectionOpen();
    }

    default public void fillCrashReport(CrashReport report) {
        CrashReportSection lv = report.addElement("Connection");
        lv.add("Protocol", () -> this.getPhase().getId());
        lv.add("Flow", () -> this.getSide().toString());
        this.addCustomCrashReportInfo(report, lv);
    }

    default public void addCustomCrashReportInfo(CrashReport report, CrashReportSection section) {
    }
}

