/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.IOException;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtEnd;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.nbt.scanner.NbtScanner;

public interface NbtType<T extends NbtElement> {
    public T read(DataInput var1, NbtSizeTracker var2) throws IOException;

    public NbtScanner.Result doAccept(DataInput var1, NbtScanner var2, NbtSizeTracker var3) throws IOException;

    default public void accept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
        switch (visitor.start(this)) {
            case CONTINUE: {
                this.doAccept(input, visitor, tracker);
                break;
            }
            case HALT: {
                break;
            }
            case BREAK: {
                this.skip(input, tracker);
            }
        }
    }

    public void skip(DataInput var1, int var2, NbtSizeTracker var3) throws IOException;

    public void skip(DataInput var1, NbtSizeTracker var2) throws IOException;

    default public boolean isImmutable() {
        return false;
    }

    public String getCrashReportName();

    public String getCommandFeedbackName();

    public static NbtType<NbtEnd> createInvalid(final int type) {
        return new NbtType<NbtEnd>(){

            private IOException createException() {
                return new IOException("Invalid tag id: " + type);
            }

            @Override
            public NbtEnd read(DataInput dataInput, NbtSizeTracker arg) throws IOException {
                throw this.createException();
            }

            @Override
            public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
                throw this.createException();
            }

            @Override
            public void skip(DataInput input, int count, NbtSizeTracker tracker) throws IOException {
                throw this.createException();
            }

            @Override
            public void skip(DataInput input, NbtSizeTracker tracker) throws IOException {
                throw this.createException();
            }

            @Override
            public String getCrashReportName() {
                return "INVALID[" + type + "]";
            }

            @Override
            public String getCommandFeedbackName() {
                return "UNKNOWN_" + type;
            }

            @Override
            public /* synthetic */ NbtElement read(DataInput input, NbtSizeTracker tracker) throws IOException {
                return this.read(input, tracker);
            }
        };
    }

    public static interface OfVariableSize<T extends NbtElement>
    extends NbtType<T> {
        @Override
        default public void skip(DataInput input, int count, NbtSizeTracker tracker) throws IOException {
            for (int j = 0; j < count; ++j) {
                this.skip(input, tracker);
            }
        }
    }

    public static interface OfFixedSize<T extends NbtElement>
    extends NbtType<T> {
        @Override
        default public void skip(DataInput input, NbtSizeTracker tracker) throws IOException {
            input.skipBytes(this.getSizeInBytes());
        }

        @Override
        default public void skip(DataInput input, int count, NbtSizeTracker tracker) throws IOException {
            input.skipBytes(this.getSizeInBytes() * count);
        }

        public int getSizeInBytes();
    }
}

