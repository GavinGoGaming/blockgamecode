/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.text;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.minecraft.text.Style;
import net.minecraft.util.Unit;

public interface StringVisitable {
    public static final Optional<Unit> TERMINATE_VISIT = Optional.of(Unit.INSTANCE);
    public static final StringVisitable EMPTY = new StringVisitable(){

        @Override
        public <T> Optional<T> visit(Visitor<T> visitor) {
            return Optional.empty();
        }

        @Override
        public <T> Optional<T> visit(StyledVisitor<T> styledVisitor, Style style) {
            return Optional.empty();
        }
    };

    public <T> Optional<T> visit(Visitor<T> var1);

    public <T> Optional<T> visit(StyledVisitor<T> var1, Style var2);

    public static StringVisitable plain(final String string) {
        return new StringVisitable(){

            @Override
            public <T> Optional<T> visit(Visitor<T> visitor) {
                return visitor.accept(string);
            }

            @Override
            public <T> Optional<T> visit(StyledVisitor<T> styledVisitor, Style style) {
                return styledVisitor.accept(style, string);
            }
        };
    }

    public static StringVisitable styled(final String string, final Style style) {
        return new StringVisitable(){

            @Override
            public <T> Optional<T> visit(Visitor<T> visitor) {
                return visitor.accept(string);
            }

            @Override
            public <T> Optional<T> visit(StyledVisitor<T> styledVisitor, Style style2) {
                return styledVisitor.accept(style.withParent(style2), string);
            }
        };
    }

    public static StringVisitable concat(StringVisitable ... visitables) {
        return StringVisitable.concat(ImmutableList.copyOf(visitables));
    }

    public static StringVisitable concat(final List<? extends StringVisitable> visitables) {
        return new StringVisitable(){

            @Override
            public <T> Optional<T> visit(Visitor<T> visitor) {
                for (StringVisitable lv : visitables) {
                    Optional<T> optional = lv.visit(visitor);
                    if (!optional.isPresent()) continue;
                    return optional;
                }
                return Optional.empty();
            }

            @Override
            public <T> Optional<T> visit(StyledVisitor<T> styledVisitor, Style style) {
                for (StringVisitable lv : visitables) {
                    Optional<T> optional = lv.visit(styledVisitor, style);
                    if (!optional.isPresent()) continue;
                    return optional;
                }
                return Optional.empty();
            }
        };
    }

    default public String getString() {
        StringBuilder stringBuilder = new StringBuilder();
        this.visit(string -> {
            stringBuilder.append(string);
            return Optional.empty();
        });
        return stringBuilder.toString();
    }

    public static interface Visitor<T> {
        public Optional<T> accept(String var1);
    }

    public static interface StyledVisitor<T> {
        public Optional<T> accept(Style var1, String var2);
    }
}

