/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.server.function.CommandFunction;

public record MacroInvocation(List<String> segments, List<String> variables) {
    public static MacroInvocation parse(String command, int lineNumber) {
        ImmutableList.Builder builder = ImmutableList.builder();
        ImmutableList.Builder builder2 = ImmutableList.builder();
        int j = command.length();
        int k = 0;
        int l = command.indexOf(36);
        while (l != -1) {
            if (l == j - 1 || command.charAt(l + 1) != '(') {
                l = command.indexOf(36, l + 1);
                continue;
            }
            builder.add(command.substring(k, l));
            int m = command.indexOf(41, l + 1);
            if (m == -1) {
                throw new IllegalArgumentException("Unterminated macro variable in macro '" + command + "' on line " + lineNumber);
            }
            String string2 = command.substring(l + 2, m);
            if (!MacroInvocation.isValidMacroName(string2)) {
                throw new IllegalArgumentException("Invalid macro variable name '" + string2 + "' on line " + lineNumber);
            }
            builder2.add(string2);
            k = m + 1;
            l = command.indexOf(36, k);
        }
        if (k == 0) {
            throw new IllegalArgumentException("Macro without variables on line " + lineNumber);
        }
        if (k != j) {
            builder.add(command.substring(k));
        }
        return new MacroInvocation((List<String>)((Object)builder.build()), (List<String>)((Object)builder2.build()));
    }

    private static boolean isValidMacroName(String name) {
        for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_') continue;
            return false;
        }
        return true;
    }

    public String apply(List<String> arguments) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < this.variables.size(); ++i) {
            stringBuilder.append(this.segments.get(i)).append(arguments.get(i));
            CommandFunction.validateCommandLength(stringBuilder);
        }
        if (this.segments.size() > this.variables.size()) {
            stringBuilder.append(this.segments.get(this.segments.size() - 1));
        }
        CommandFunction.validateCommandLength(stringBuilder);
        return stringBuilder.toString();
    }
}

