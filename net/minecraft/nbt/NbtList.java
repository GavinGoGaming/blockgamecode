/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.nbt;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.InvalidNbtException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.NbtTypes;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;

public class NbtList
extends AbstractNbtList<NbtElement> {
    private static final int SIZE = 37;
    public static final NbtType<NbtList> TYPE = new NbtType.OfVariableSize<NbtList>(){

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public NbtList read(DataInput dataInput, NbtSizeTracker arg) throws IOException {
            arg.pushStack();
            try {
                NbtList nbtList = 1.readList(dataInput, arg);
                return nbtList;
            } finally {
                arg.popStack();
            }
        }

        private static NbtList readList(DataInput input, NbtSizeTracker tracker) throws IOException {
            tracker.add(37L);
            byte b = input.readByte();
            int i = input.readInt();
            if (b == 0 && i > 0) {
                throw new InvalidNbtException("Missing type on ListTag");
            }
            tracker.add(4L, i);
            NbtType<?> lv = NbtTypes.byId(b);
            ArrayList<NbtElement> list = Lists.newArrayListWithCapacity(i);
            for (int j = 0; j < i; ++j) {
                list.add((NbtElement)lv.read(input, tracker));
            }
            return new NbtList(list, b);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
            tracker.pushStack();
            try {
                NbtScanner.Result result = 1.scanList(input, visitor, tracker);
                return result;
            } finally {
                tracker.popStack();
            }
        }

        /*
         * Exception decompiling
         */
        private static NbtScanner.Result scanList(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
            /*
             * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
             * 
             * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [4[SWITCH], 8[CASE]], but top level block is 9[SWITCH]
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
             *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
             *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:540)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
             *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:261)
             *     at org.benf.cfr.reader.Driver.doJar(Driver.java:143)
             *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
             *     at org.benf.cfr.reader.Main.main(Main.java:54)
             */
            throw new IllegalStateException("Decompilation failed");
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void skip(DataInput input, NbtSizeTracker tracker) throws IOException {
            tracker.pushStack();
            try {
                NbtType<?> lv = NbtTypes.byId(input.readByte());
                int i = input.readInt();
                lv.skip(input, i, tracker);
            } finally {
                tracker.popStack();
            }
        }

        @Override
        public String getCrashReportName() {
            return "LIST";
        }

        @Override
        public String getCommandFeedbackName() {
            return "TAG_List";
        }

        @Override
        public /* synthetic */ NbtElement read(DataInput input, NbtSizeTracker tracker) throws IOException {
            return this.read(input, tracker);
        }
    };
    private final List<NbtElement> value;
    private byte type;

    NbtList(List<NbtElement> list, byte type) {
        this.value = list;
        this.type = type;
    }

    public NbtList() {
        this(Lists.newArrayList(), NbtElement.END_TYPE);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        this.type = this.value.isEmpty() ? (byte)0 : this.value.get(0).getType();
        output.writeByte(this.type);
        output.writeInt(this.value.size());
        for (NbtElement lv : this.value) {
            lv.write(output);
        }
    }

    @Override
    public int getSizeInBytes() {
        int i = 37;
        i += 4 * this.value.size();
        for (NbtElement lv : this.value) {
            i += lv.getSizeInBytes();
        }
        return i;
    }

    @Override
    public byte getType() {
        return NbtElement.LIST_TYPE;
    }

    public NbtType<NbtList> getNbtType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return this.asString();
    }

    private void forgetTypeIfEmpty() {
        if (this.value.isEmpty()) {
            this.type = 0;
        }
    }

    @Override
    public NbtElement remove(int i) {
        NbtElement lv = this.value.remove(i);
        this.forgetTypeIfEmpty();
        return lv;
    }

    @Override
    public boolean isEmpty() {
        return this.value.isEmpty();
    }

    public NbtCompound getCompound(int index) {
        NbtElement lv;
        if (index >= 0 && index < this.value.size() && (lv = this.value.get(index)).getType() == NbtElement.COMPOUND_TYPE) {
            return (NbtCompound)lv;
        }
        return new NbtCompound();
    }

    public NbtList getList(int index) {
        NbtElement lv;
        if (index >= 0 && index < this.value.size() && (lv = this.value.get(index)).getType() == NbtElement.LIST_TYPE) {
            return (NbtList)lv;
        }
        return new NbtList();
    }

    public short getShort(int index) {
        NbtElement lv;
        if (index >= 0 && index < this.value.size() && (lv = this.value.get(index)).getType() == NbtElement.SHORT_TYPE) {
            return ((NbtShort)lv).shortValue();
        }
        return 0;
    }

    public int getInt(int index) {
        NbtElement lv;
        if (index >= 0 && index < this.value.size() && (lv = this.value.get(index)).getType() == NbtElement.INT_TYPE) {
            return ((NbtInt)lv).intValue();
        }
        return 0;
    }

    public int[] getIntArray(int index) {
        NbtElement lv;
        if (index >= 0 && index < this.value.size() && (lv = this.value.get(index)).getType() == NbtElement.INT_ARRAY_TYPE) {
            return ((NbtIntArray)lv).getIntArray();
        }
        return new int[0];
    }

    public long[] getLongArray(int index) {
        NbtElement lv;
        if (index >= 0 && index < this.value.size() && (lv = this.value.get(index)).getType() == NbtElement.LONG_ARRAY_TYPE) {
            return ((NbtLongArray)lv).getLongArray();
        }
        return new long[0];
    }

    public double getDouble(int index) {
        NbtElement lv;
        if (index >= 0 && index < this.value.size() && (lv = this.value.get(index)).getType() == NbtElement.DOUBLE_TYPE) {
            return ((NbtDouble)lv).doubleValue();
        }
        return 0.0;
    }

    public float getFloat(int index) {
        NbtElement lv;
        if (index >= 0 && index < this.value.size() && (lv = this.value.get(index)).getType() == NbtElement.FLOAT_TYPE) {
            return ((NbtFloat)lv).floatValue();
        }
        return 0.0f;
    }

    public String getString(int index) {
        if (index < 0 || index >= this.value.size()) {
            return "";
        }
        NbtElement lv = this.value.get(index);
        if (lv.getType() == NbtElement.STRING_TYPE) {
            return lv.asString();
        }
        return lv.toString();
    }

    @Override
    public int size() {
        return this.value.size();
    }

    @Override
    public NbtElement get(int i) {
        return this.value.get(i);
    }

    @Override
    public NbtElement set(int i, NbtElement arg) {
        NbtElement lv = this.get(i);
        if (!this.setElement(i, arg)) {
            throw new UnsupportedOperationException(String.format(Locale.ROOT, "Trying to add tag of type %d to list of %d", arg.getType(), this.type));
        }
        return lv;
    }

    @Override
    public void add(int i, NbtElement arg) {
        if (!this.addElement(i, arg)) {
            throw new UnsupportedOperationException(String.format(Locale.ROOT, "Trying to add tag of type %d to list of %d", arg.getType(), this.type));
        }
    }

    @Override
    public boolean setElement(int index, NbtElement element) {
        if (this.canAdd(element)) {
            this.value.set(index, element);
            return true;
        }
        return false;
    }

    @Override
    public boolean addElement(int index, NbtElement element) {
        if (this.canAdd(element)) {
            this.value.add(index, element);
            return true;
        }
        return false;
    }

    private boolean canAdd(NbtElement element) {
        if (element.getType() == 0) {
            return false;
        }
        if (this.type == 0) {
            this.type = element.getType();
            return true;
        }
        return this.type == element.getType();
    }

    @Override
    public NbtList copy() {
        List<NbtElement> iterable = NbtTypes.byId(this.type).isImmutable() ? this.value : Iterables.transform(this.value, NbtElement::copy);
        ArrayList<NbtElement> list = Lists.newArrayList(iterable);
        return new NbtList(list, this.type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof NbtList && Objects.equals(this.value, ((NbtList)o).value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public void accept(NbtElementVisitor visitor) {
        visitor.visitList(this);
    }

    @Override
    public byte getHeldType() {
        return this.type;
    }

    @Override
    public void clear() {
        this.value.clear();
        this.type = 0;
    }

    @Override
    public NbtScanner.Result doAccept(NbtScanner visitor) {
        switch (visitor.visitListMeta(NbtTypes.byId(this.type), this.value.size())) {
            case HALT: {
                return NbtScanner.Result.HALT;
            }
            case BREAK: {
                return visitor.endNested();
            }
        }
        block13: for (int i = 0; i < this.value.size(); ++i) {
            NbtElement lv = this.value.get(i);
            switch (visitor.startListItem(lv.getNbtType(), i)) {
                case HALT: {
                    return NbtScanner.Result.HALT;
                }
                case SKIP: {
                    continue block13;
                }
                case BREAK: {
                    return visitor.endNested();
                }
                default: {
                    switch (lv.doAccept(visitor)) {
                        case HALT: {
                            return NbtScanner.Result.HALT;
                        }
                        case BREAK: {
                            return visitor.endNested();
                        }
                    }
                }
            }
        }
        return visitor.endNested();
    }

    @Override
    public /* synthetic */ NbtElement copy() {
        return this.copy();
    }

    @Override
    public /* synthetic */ Object remove(int i) {
        return this.remove(i);
    }

    @Override
    public /* synthetic */ void add(int i, Object object) {
        this.add(i, (NbtElement)object);
    }

    @Override
    public /* synthetic */ Object set(int i, Object object) {
        return this.set(i, (NbtElement)object);
    }

    @Override
    public /* synthetic */ Object get(int index) {
        return this.get(index);
    }
}

