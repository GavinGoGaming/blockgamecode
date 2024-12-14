/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.world;

import java.util.Objects;
import net.minecraft.server.world.ChunkTicketType;

public final class ChunkTicket<T>
implements Comparable<ChunkTicket<?>> {
    private final ChunkTicketType<T> type;
    private final int level;
    private final T argument;
    private long tickCreated;

    protected ChunkTicket(ChunkTicketType<T> type, int level, T argument) {
        this.type = type;
        this.level = level;
        this.argument = argument;
    }

    @Override
    public int compareTo(ChunkTicket<?> arg) {
        int i = Integer.compare(this.level, arg.level);
        if (i != 0) {
            return i;
        }
        int j = Integer.compare(System.identityHashCode(this.type), System.identityHashCode(arg.type));
        if (j != 0) {
            return j;
        }
        return this.type.getArgumentComparator().compare(this.argument, arg.argument);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChunkTicket)) {
            return false;
        }
        ChunkTicket lv = (ChunkTicket)o;
        return this.level == lv.level && Objects.equals(this.type, lv.type) && Objects.equals(this.argument, lv.argument);
    }

    public int hashCode() {
        return Objects.hash(this.type, this.level, this.argument);
    }

    public String toString() {
        return "Ticket[" + String.valueOf(this.type) + " " + this.level + " (" + String.valueOf(this.argument) + ")] at " + this.tickCreated;
    }

    public ChunkTicketType<T> getType() {
        return this.type;
    }

    public int getLevel() {
        return this.level;
    }

    protected void setTickCreated(long tickCreated) {
        this.tickCreated = tickCreated;
    }

    protected boolean isExpired(long currentTick) {
        long m = this.type.getExpiryTicks();
        return m != 0L && currentTick - this.tickCreated > m;
    }

    @Override
    public /* synthetic */ int compareTo(Object that) {
        return this.compareTo((ChunkTicket)that);
    }
}

