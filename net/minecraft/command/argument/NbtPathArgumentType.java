/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NbtPathArgumentType
implements ArgumentType<NbtPath> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo.bar", "foo[0]", "[0]", "[]", "{foo=bar}");
    public static final SimpleCommandExceptionType INVALID_PATH_NODE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("arguments.nbtpath.node.invalid"));
    public static final SimpleCommandExceptionType TOO_DEEP_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("arguments.nbtpath.too_deep"));
    public static final DynamicCommandExceptionType NOTHING_FOUND_EXCEPTION = new DynamicCommandExceptionType(path -> Text.stringifiedTranslatable("arguments.nbtpath.nothing_found", path));
    static final DynamicCommandExceptionType EXPECTED_LIST_EXCEPTION = new DynamicCommandExceptionType(nbt -> Text.stringifiedTranslatable("commands.data.modify.expected_list", nbt));
    static final DynamicCommandExceptionType INVALID_INDEX_EXCEPTION = new DynamicCommandExceptionType(index -> Text.stringifiedTranslatable("commands.data.modify.invalid_index", index));
    private static final char LEFT_SQUARE_BRACKET = '[';
    private static final char RIGHT_SQUARE_BRACKET = ']';
    private static final char LEFT_CURLY_BRACKET = '{';
    private static final char RIGHT_CURLY_BRACKET = '}';
    private static final char DOUBLE_QUOTE = '\"';
    private static final char SINGLE_QUOTE = '\'';

    public static NbtPathArgumentType nbtPath() {
        return new NbtPathArgumentType();
    }

    public static NbtPath getNbtPath(CommandContext<ServerCommandSource> context, String name) {
        return context.getArgument(name, NbtPath.class);
    }

    @Override
    public NbtPath parse(StringReader stringReader) throws CommandSyntaxException {
        ArrayList<PathNode> list = Lists.newArrayList();
        int i = stringReader.getCursor();
        Object2IntOpenHashMap<PathNode> object2IntMap = new Object2IntOpenHashMap<PathNode>();
        boolean bl = true;
        while (stringReader.canRead() && stringReader.peek() != ' ') {
            char c;
            PathNode lv = NbtPathArgumentType.parseNode(stringReader, bl);
            list.add(lv);
            object2IntMap.put(lv, stringReader.getCursor() - i);
            bl = false;
            if (!stringReader.canRead() || (c = stringReader.peek()) == ' ' || c == '[' || c == '{') continue;
            stringReader.expect('.');
        }
        return new NbtPath(stringReader.getString().substring(i, stringReader.getCursor()), list.toArray(new PathNode[0]), object2IntMap);
    }

    private static PathNode parseNode(StringReader reader, boolean root) throws CommandSyntaxException {
        return switch (reader.peek()) {
            case '{' -> {
                if (!root) {
                    throw INVALID_PATH_NODE_EXCEPTION.createWithContext(reader);
                }
                NbtCompound lv = new StringNbtReader(reader).parseCompound();
                yield new FilteredRootNode(lv);
            }
            case '[' -> {
                reader.skip();
                char i = reader.peek();
                if (i == '{') {
                    NbtCompound lv2 = new StringNbtReader(reader).parseCompound();
                    reader.expect(']');
                    yield new FilteredListElementNode(lv2);
                }
                if (i == ']') {
                    reader.skip();
                    yield AllListElementNode.INSTANCE;
                }
                int j = reader.readInt();
                reader.expect(']');
                yield new IndexedListElementNode(j);
            }
            case '\"', '\'' -> NbtPathArgumentType.readCompoundChildNode(reader, reader.readString());
            default -> NbtPathArgumentType.readCompoundChildNode(reader, NbtPathArgumentType.readName(reader));
        };
    }

    private static PathNode readCompoundChildNode(StringReader reader, String name) throws CommandSyntaxException {
        if (reader.canRead() && reader.peek() == '{') {
            NbtCompound lv = new StringNbtReader(reader).parseCompound();
            return new FilteredNamedNode(name, lv);
        }
        return new NamedNode(name);
    }

    private static String readName(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        while (reader.canRead() && NbtPathArgumentType.isNameCharacter(reader.peek())) {
            reader.skip();
        }
        if (reader.getCursor() == i) {
            throw INVALID_PATH_NODE_EXCEPTION.createWithContext(reader);
        }
        return reader.getString().substring(i, reader.getCursor());
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private static boolean isNameCharacter(char c) {
        return c != ' ' && c != '\"' && c != '\'' && c != '[' && c != ']' && c != '.' && c != '{' && c != '}';
    }

    static Predicate<NbtElement> getPredicate(NbtCompound filter) {
        return nbt -> NbtHelper.matches(filter, nbt, true);
    }

    @Override
    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }

    public static class NbtPath {
        private final String string;
        private final Object2IntMap<PathNode> nodeEndIndices;
        private final PathNode[] nodes;
        public static final Codec<NbtPath> CODEC = Codec.STRING.comapFlatMap(path -> {
            try {
                NbtPath lv = new NbtPathArgumentType().parse(new StringReader((String)path));
                return DataResult.success(lv);
            } catch (CommandSyntaxException commandSyntaxException) {
                return DataResult.error(() -> "Failed to parse path " + path + ": " + commandSyntaxException.getMessage());
            }
        }, NbtPath::getString);

        public static NbtPath parse(String path) throws CommandSyntaxException {
            return new NbtPathArgumentType().parse(new StringReader(path));
        }

        public NbtPath(String string, PathNode[] nodes, Object2IntMap<PathNode> nodeEndIndices) {
            this.string = string;
            this.nodes = nodes;
            this.nodeEndIndices = nodeEndIndices;
        }

        public List<NbtElement> get(NbtElement element) throws CommandSyntaxException {
            List<NbtElement> list = Collections.singletonList(element);
            for (PathNode lv : this.nodes) {
                if (!(list = lv.get(list)).isEmpty()) continue;
                throw this.createNothingFoundException(lv);
            }
            return list;
        }

        public int count(NbtElement element) {
            List<NbtElement> list = Collections.singletonList(element);
            for (PathNode lv : this.nodes) {
                if (!(list = lv.get(list)).isEmpty()) continue;
                return 0;
            }
            return list.size();
        }

        private List<NbtElement> getTerminals(NbtElement start) throws CommandSyntaxException {
            List<NbtElement> list = Collections.singletonList(start);
            for (int i = 0; i < this.nodes.length - 1; ++i) {
                PathNode lv = this.nodes[i];
                int j = i + 1;
                if (!(list = lv.getOrInit(list, this.nodes[j]::init)).isEmpty()) continue;
                throw this.createNothingFoundException(lv);
            }
            return list;
        }

        public List<NbtElement> getOrInit(NbtElement element, Supplier<NbtElement> source) throws CommandSyntaxException {
            List<NbtElement> list = this.getTerminals(element);
            PathNode lv = this.nodes[this.nodes.length - 1];
            return lv.getOrInit(list, source);
        }

        private static int forEach(List<NbtElement> elements, Function<NbtElement, Integer> operation) {
            return elements.stream().map(operation).reduce(0, (a, b) -> a + b);
        }

        public static boolean isTooDeep(NbtElement element, int depth) {
            block4: {
                block3: {
                    if (depth >= 512) {
                        return true;
                    }
                    if (!(element instanceof NbtCompound)) break block3;
                    NbtCompound lv = (NbtCompound)element;
                    for (String string : lv.getKeys()) {
                        NbtElement lv2 = lv.get(string);
                        if (lv2 == null || !NbtPath.isTooDeep(lv2, depth + 1)) continue;
                        return true;
                    }
                    break block4;
                }
                if (!(element instanceof NbtList)) break block4;
                NbtList lv3 = (NbtList)element;
                for (NbtElement lv4 : lv3) {
                    if (!NbtPath.isTooDeep(lv4, depth + 1)) continue;
                    return true;
                }
            }
            return false;
        }

        public int put(NbtElement element, NbtElement source) throws CommandSyntaxException {
            if (NbtPath.isTooDeep(source, this.getDepth())) {
                throw TOO_DEEP_EXCEPTION.create();
            }
            NbtElement lv = source.copy();
            List<NbtElement> list = this.getTerminals(element);
            if (list.isEmpty()) {
                return 0;
            }
            PathNode lv2 = this.nodes[this.nodes.length - 1];
            MutableBoolean mutableBoolean = new MutableBoolean(false);
            return NbtPath.forEach(list, nbt -> lv2.set((NbtElement)nbt, () -> {
                if (mutableBoolean.isFalse()) {
                    mutableBoolean.setTrue();
                    return lv;
                }
                return lv.copy();
            }));
        }

        private int getDepth() {
            return this.nodes.length;
        }

        public int insert(int index, NbtCompound compound, List<NbtElement> elements) throws CommandSyntaxException {
            ArrayList<NbtElement> list2 = new ArrayList<NbtElement>(elements.size());
            for (NbtElement lv : elements) {
                NbtElement lv2 = lv.copy();
                list2.add(lv2);
                if (!NbtPath.isTooDeep(lv2, this.getDepth())) continue;
                throw TOO_DEEP_EXCEPTION.create();
            }
            List<NbtElement> collection = this.getOrInit(compound, NbtList::new);
            int j = 0;
            boolean bl = false;
            for (NbtElement lv3 : collection) {
                if (!(lv3 instanceof AbstractNbtList)) {
                    throw EXPECTED_LIST_EXCEPTION.create(lv3);
                }
                AbstractNbtList lv4 = (AbstractNbtList)lv3;
                boolean bl2 = false;
                int k = index < 0 ? lv4.size() + index + 1 : index;
                for (NbtElement lv5 : list2) {
                    try {
                        if (!lv4.addElement(k, bl ? lv5.copy() : lv5)) continue;
                        ++k;
                        bl2 = true;
                    } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                        throw INVALID_INDEX_EXCEPTION.create(k);
                    }
                }
                bl = true;
                j += bl2 ? 1 : 0;
            }
            return j;
        }

        public int remove(NbtElement element) {
            List<NbtElement> list = Collections.singletonList(element);
            for (int i = 0; i < this.nodes.length - 1; ++i) {
                list = this.nodes[i].get(list);
            }
            PathNode lv = this.nodes[this.nodes.length - 1];
            return NbtPath.forEach(list, lv::clear);
        }

        private CommandSyntaxException createNothingFoundException(PathNode node) {
            int i = this.nodeEndIndices.getInt(node);
            return NOTHING_FOUND_EXCEPTION.create(this.string.substring(0, i));
        }

        public String toString() {
            return this.string;
        }

        public String getString() {
            return this.string;
        }
    }

    static interface PathNode {
        public void get(NbtElement var1, List<NbtElement> var2);

        public void getOrInit(NbtElement var1, Supplier<NbtElement> var2, List<NbtElement> var3);

        public NbtElement init();

        public int set(NbtElement var1, Supplier<NbtElement> var2);

        public int clear(NbtElement var1);

        default public List<NbtElement> get(List<NbtElement> elements) {
            return this.process(elements, this::get);
        }

        default public List<NbtElement> getOrInit(List<NbtElement> elements, Supplier<NbtElement> supplier) {
            return this.process(elements, (current, results) -> this.getOrInit((NbtElement)current, supplier, (List<NbtElement>)results));
        }

        default public List<NbtElement> process(List<NbtElement> elements, BiConsumer<NbtElement, List<NbtElement>> action) {
            ArrayList<NbtElement> list2 = Lists.newArrayList();
            for (NbtElement lv : elements) {
                action.accept(lv, list2);
            }
            return list2;
        }
    }

    static class FilteredRootNode
    implements PathNode {
        private final Predicate<NbtElement> matcher;

        public FilteredRootNode(NbtCompound filter) {
            this.matcher = NbtPathArgumentType.getPredicate(filter);
        }

        @Override
        public void get(NbtElement current, List<NbtElement> results) {
            if (current instanceof NbtCompound && this.matcher.test(current)) {
                results.add(current);
            }
        }

        @Override
        public void getOrInit(NbtElement current, Supplier<NbtElement> source, List<NbtElement> results) {
            this.get(current, results);
        }

        @Override
        public NbtElement init() {
            return new NbtCompound();
        }

        @Override
        public int set(NbtElement current, Supplier<NbtElement> source) {
            return 0;
        }

        @Override
        public int clear(NbtElement current) {
            return 0;
        }
    }

    static class FilteredListElementNode
    implements PathNode {
        private final NbtCompound filter;
        private final Predicate<NbtElement> predicate;

        public FilteredListElementNode(NbtCompound filter) {
            this.filter = filter;
            this.predicate = NbtPathArgumentType.getPredicate(filter);
        }

        @Override
        public void get(NbtElement current, List<NbtElement> results) {
            if (current instanceof NbtList) {
                NbtList lv = (NbtList)current;
                lv.stream().filter(this.predicate).forEach(results::add);
            }
        }

        @Override
        public void getOrInit(NbtElement current, Supplier<NbtElement> source, List<NbtElement> results) {
            MutableBoolean mutableBoolean = new MutableBoolean();
            if (current instanceof NbtList) {
                NbtList lv = (NbtList)current;
                lv.stream().filter(this.predicate).forEach(nbt -> {
                    results.add((NbtElement)nbt);
                    mutableBoolean.setTrue();
                });
                if (mutableBoolean.isFalse()) {
                    NbtCompound lv2 = this.filter.copy();
                    lv.add(lv2);
                    results.add(lv2);
                }
            }
        }

        @Override
        public NbtElement init() {
            return new NbtList();
        }

        @Override
        public int set(NbtElement current, Supplier<NbtElement> source) {
            int i = 0;
            if (current instanceof NbtList) {
                NbtList lv = (NbtList)current;
                int j = lv.size();
                if (j == 0) {
                    lv.add(source.get());
                    ++i;
                } else {
                    for (int k = 0; k < j; ++k) {
                        NbtElement lv3;
                        NbtElement lv2 = lv.get(k);
                        if (!this.predicate.test(lv2) || (lv3 = source.get()).equals(lv2) || !lv.setElement(k, lv3)) continue;
                        ++i;
                    }
                }
            }
            return i;
        }

        @Override
        public int clear(NbtElement current) {
            int i = 0;
            if (current instanceof NbtList) {
                NbtList lv = (NbtList)current;
                for (int j = lv.size() - 1; j >= 0; --j) {
                    if (!this.predicate.test(lv.get(j))) continue;
                    lv.remove(j);
                    ++i;
                }
            }
            return i;
        }
    }

    static class AllListElementNode
    implements PathNode {
        public static final AllListElementNode INSTANCE = new AllListElementNode();

        private AllListElementNode() {
        }

        @Override
        public void get(NbtElement current, List<NbtElement> results) {
            if (current instanceof AbstractNbtList) {
                results.addAll((AbstractNbtList)current);
            }
        }

        @Override
        public void getOrInit(NbtElement current, Supplier<NbtElement> source, List<NbtElement> results) {
            if (current instanceof AbstractNbtList) {
                AbstractNbtList lv = (AbstractNbtList)current;
                if (lv.isEmpty()) {
                    NbtElement lv2 = source.get();
                    if (lv.addElement(0, lv2)) {
                        results.add(lv2);
                    }
                } else {
                    results.addAll(lv);
                }
            }
        }

        @Override
        public NbtElement init() {
            return new NbtList();
        }

        @Override
        public int set(NbtElement current, Supplier<NbtElement> source) {
            if (current instanceof AbstractNbtList) {
                AbstractNbtList lv = (AbstractNbtList)current;
                int i = lv.size();
                if (i == 0) {
                    lv.addElement(0, source.get());
                    return 1;
                }
                NbtElement lv2 = source.get();
                int j = i - (int)lv.stream().filter((Predicate<NbtElement>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Z, equals(java.lang.Object ), (Lnet/minecraft/nbt/NbtElement;)Z)((NbtElement)lv2)).count();
                if (j == 0) {
                    return 0;
                }
                lv.clear();
                if (!lv.addElement(0, lv2)) {
                    return 0;
                }
                for (int k = 1; k < i; ++k) {
                    lv.addElement(k, source.get());
                }
                return j;
            }
            return 0;
        }

        @Override
        public int clear(NbtElement current) {
            AbstractNbtList lv;
            int i;
            if (current instanceof AbstractNbtList && (i = (lv = (AbstractNbtList)current).size()) > 0) {
                lv.clear();
                return i;
            }
            return 0;
        }
    }

    static class IndexedListElementNode
    implements PathNode {
        private final int index;

        public IndexedListElementNode(int index) {
            this.index = index;
        }

        @Override
        public void get(NbtElement current, List<NbtElement> results) {
            if (current instanceof AbstractNbtList) {
                int j;
                AbstractNbtList lv = (AbstractNbtList)current;
                int i = lv.size();
                int n = j = this.index < 0 ? i + this.index : this.index;
                if (0 <= j && j < i) {
                    results.add((NbtElement)lv.get(j));
                }
            }
        }

        @Override
        public void getOrInit(NbtElement current, Supplier<NbtElement> source, List<NbtElement> results) {
            this.get(current, results);
        }

        @Override
        public NbtElement init() {
            return new NbtList();
        }

        @Override
        public int set(NbtElement current, Supplier<NbtElement> source) {
            if (current instanceof AbstractNbtList) {
                int j;
                AbstractNbtList lv = (AbstractNbtList)current;
                int i = lv.size();
                int n = j = this.index < 0 ? i + this.index : this.index;
                if (0 <= j && j < i) {
                    NbtElement lv2 = (NbtElement)lv.get(j);
                    NbtElement lv3 = source.get();
                    if (!lv3.equals(lv2) && lv.setElement(j, lv3)) {
                        return 1;
                    }
                }
            }
            return 0;
        }

        @Override
        public int clear(NbtElement current) {
            if (current instanceof AbstractNbtList) {
                int j;
                AbstractNbtList lv = (AbstractNbtList)current;
                int i = lv.size();
                int n = j = this.index < 0 ? i + this.index : this.index;
                if (0 <= j && j < i) {
                    lv.remove(j);
                    return 1;
                }
            }
            return 0;
        }
    }

    static class FilteredNamedNode
    implements PathNode {
        private final String name;
        private final NbtCompound filter;
        private final Predicate<NbtElement> predicate;

        public FilteredNamedNode(String name, NbtCompound filter) {
            this.name = name;
            this.filter = filter;
            this.predicate = NbtPathArgumentType.getPredicate(filter);
        }

        @Override
        public void get(NbtElement current, List<NbtElement> results) {
            NbtElement lv;
            if (current instanceof NbtCompound && this.predicate.test(lv = ((NbtCompound)current).get(this.name))) {
                results.add(lv);
            }
        }

        @Override
        public void getOrInit(NbtElement current, Supplier<NbtElement> source, List<NbtElement> results) {
            if (current instanceof NbtCompound) {
                NbtCompound lv = (NbtCompound)current;
                NbtElement lv2 = lv.get(this.name);
                if (lv2 == null) {
                    lv2 = this.filter.copy();
                    lv.put(this.name, lv2);
                    results.add(lv2);
                } else if (this.predicate.test(lv2)) {
                    results.add(lv2);
                }
            }
        }

        @Override
        public NbtElement init() {
            return new NbtCompound();
        }

        @Override
        public int set(NbtElement current, Supplier<NbtElement> source) {
            NbtElement lv3;
            NbtCompound lv;
            NbtElement lv2;
            if (current instanceof NbtCompound && this.predicate.test(lv2 = (lv = (NbtCompound)current).get(this.name)) && !(lv3 = source.get()).equals(lv2)) {
                lv.put(this.name, lv3);
                return 1;
            }
            return 0;
        }

        @Override
        public int clear(NbtElement current) {
            NbtCompound lv;
            NbtElement lv2;
            if (current instanceof NbtCompound && this.predicate.test(lv2 = (lv = (NbtCompound)current).get(this.name))) {
                lv.remove(this.name);
                return 1;
            }
            return 0;
        }
    }

    static class NamedNode
    implements PathNode {
        private final String name;

        public NamedNode(String name) {
            this.name = name;
        }

        @Override
        public void get(NbtElement current, List<NbtElement> results) {
            NbtElement lv;
            if (current instanceof NbtCompound && (lv = ((NbtCompound)current).get(this.name)) != null) {
                results.add(lv);
            }
        }

        @Override
        public void getOrInit(NbtElement current, Supplier<NbtElement> source, List<NbtElement> results) {
            if (current instanceof NbtCompound) {
                NbtElement lv2;
                NbtCompound lv = (NbtCompound)current;
                if (lv.contains(this.name)) {
                    lv2 = lv.get(this.name);
                } else {
                    lv2 = source.get();
                    lv.put(this.name, lv2);
                }
                results.add(lv2);
            }
        }

        @Override
        public NbtElement init() {
            return new NbtCompound();
        }

        @Override
        public int set(NbtElement current, Supplier<NbtElement> source) {
            if (current instanceof NbtCompound) {
                NbtElement lv3;
                NbtCompound lv = (NbtCompound)current;
                NbtElement lv2 = source.get();
                if (!lv2.equals(lv3 = lv.put(this.name, lv2))) {
                    return 1;
                }
            }
            return 0;
        }

        @Override
        public int clear(NbtElement current) {
            NbtCompound lv;
            if (current instanceof NbtCompound && (lv = (NbtCompound)current).contains(this.name)) {
                lv.remove(this.name);
                return 1;
            }
            return 0;
        }
    }
}

