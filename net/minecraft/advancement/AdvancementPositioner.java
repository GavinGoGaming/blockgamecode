/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.advancement;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.advancement.PlacedAdvancement;
import org.jetbrains.annotations.Nullable;

public class AdvancementPositioner {
    private final PlacedAdvancement advancement;
    @Nullable
    private final AdvancementPositioner parent;
    @Nullable
    private final AdvancementPositioner previousSibling;
    private final int childrenSize;
    private final List<AdvancementPositioner> children = Lists.newArrayList();
    private AdvancementPositioner optionalLast;
    @Nullable
    private AdvancementPositioner substituteChild;
    private int depth;
    private float row;
    private float relativeRowInSiblings;
    private float field_1266;
    private float field_1265;

    public AdvancementPositioner(PlacedAdvancement advancement, @Nullable AdvancementPositioner parent, @Nullable AdvancementPositioner previousSibling, int childrenSize, int depth) {
        if (advancement.getAdvancement().display().isEmpty()) {
            throw new IllegalArgumentException("Can't position an invisible advancement!");
        }
        this.advancement = advancement;
        this.parent = parent;
        this.previousSibling = previousSibling;
        this.childrenSize = childrenSize;
        this.optionalLast = this;
        this.depth = depth;
        this.row = -1.0f;
        AdvancementPositioner lv = null;
        for (PlacedAdvancement lv2 : advancement.getChildren()) {
            lv = this.findChildrenRecursively(lv2, lv);
        }
    }

    @Nullable
    private AdvancementPositioner findChildrenRecursively(PlacedAdvancement advancement, @Nullable AdvancementPositioner lastChild) {
        if (advancement.getAdvancement().display().isPresent()) {
            lastChild = new AdvancementPositioner(advancement, this, lastChild, this.children.size() + 1, this.depth + 1);
            this.children.add(lastChild);
        } else {
            for (PlacedAdvancement lv : advancement.getChildren()) {
                lastChild = this.findChildrenRecursively(lv, lastChild);
            }
        }
        return lastChild;
    }

    private void calculateRecursively() {
        if (this.children.isEmpty()) {
            this.row = this.previousSibling != null ? this.previousSibling.row + 1.0f : 0.0f;
            return;
        }
        AdvancementPositioner lv = null;
        for (AdvancementPositioner lv2 : this.children) {
            lv2.calculateRecursively();
            lv = lv2.onFinishCalculation(lv == null ? lv2 : lv);
        }
        this.onFinishChildrenCalculation();
        float f = (this.children.get((int)0).row + this.children.get((int)(this.children.size() - 1)).row) / 2.0f;
        if (this.previousSibling != null) {
            this.row = this.previousSibling.row + 1.0f;
            this.relativeRowInSiblings = this.row - f;
        } else {
            this.row = f;
        }
    }

    private float findMinRowRecursively(float deltaRow, int depth, float minRow) {
        this.row += deltaRow;
        this.depth = depth;
        if (this.row < minRow) {
            minRow = this.row;
        }
        for (AdvancementPositioner lv : this.children) {
            minRow = lv.findMinRowRecursively(deltaRow + this.relativeRowInSiblings, depth + 1, minRow);
        }
        return minRow;
    }

    private void increaseRowRecursively(float deltaRow) {
        this.row += deltaRow;
        for (AdvancementPositioner lv : this.children) {
            lv.increaseRowRecursively(deltaRow);
        }
    }

    private void onFinishChildrenCalculation() {
        float f = 0.0f;
        float g = 0.0f;
        for (int i = this.children.size() - 1; i >= 0; --i) {
            AdvancementPositioner lv = this.children.get(i);
            lv.row += f;
            lv.relativeRowInSiblings += f;
            f += lv.field_1265 + (g += lv.field_1266);
        }
    }

    @Nullable
    private AdvancementPositioner getFirstChild() {
        if (this.substituteChild != null) {
            return this.substituteChild;
        }
        if (!this.children.isEmpty()) {
            return this.children.get(0);
        }
        return null;
    }

    @Nullable
    private AdvancementPositioner getLastChild() {
        if (this.substituteChild != null) {
            return this.substituteChild;
        }
        if (!this.children.isEmpty()) {
            return this.children.get(this.children.size() - 1);
        }
        return null;
    }

    private AdvancementPositioner onFinishCalculation(AdvancementPositioner last) {
        if (this.previousSibling == null) {
            return last;
        }
        AdvancementPositioner lv = this;
        AdvancementPositioner lv2 = this;
        AdvancementPositioner lv3 = this.previousSibling;
        AdvancementPositioner lv4 = this.parent.children.get(0);
        float f = this.relativeRowInSiblings;
        float g = this.relativeRowInSiblings;
        float h = lv3.relativeRowInSiblings;
        float i = lv4.relativeRowInSiblings;
        while (lv3.getLastChild() != null && lv.getFirstChild() != null) {
            lv3 = lv3.getLastChild();
            lv = lv.getFirstChild();
            lv4 = lv4.getFirstChild();
            lv2 = lv2.getLastChild();
            lv2.optionalLast = this;
            float j = lv3.row + h - (lv.row + f) + 1.0f;
            if (j > 0.0f) {
                lv3.getLast(this, last).pushDown(this, j);
                f += j;
                g += j;
            }
            h += lv3.relativeRowInSiblings;
            f += lv.relativeRowInSiblings;
            i += lv4.relativeRowInSiblings;
            g += lv2.relativeRowInSiblings;
        }
        if (lv3.getLastChild() != null && lv2.getLastChild() == null) {
            lv2.substituteChild = lv3.getLastChild();
            lv2.relativeRowInSiblings += h - g;
        } else {
            if (lv.getFirstChild() != null && lv4.getFirstChild() == null) {
                lv4.substituteChild = lv.getFirstChild();
                lv4.relativeRowInSiblings += f - i;
            }
            last = this;
        }
        return last;
    }

    private void pushDown(AdvancementPositioner positioner, float extraRowDistance) {
        float g = positioner.childrenSize - this.childrenSize;
        if (g != 0.0f) {
            positioner.field_1266 -= extraRowDistance / g;
            this.field_1266 += extraRowDistance / g;
        }
        positioner.field_1265 += extraRowDistance;
        positioner.row += extraRowDistance;
        positioner.relativeRowInSiblings += extraRowDistance;
    }

    private AdvancementPositioner getLast(AdvancementPositioner arg, AdvancementPositioner arg2) {
        if (this.optionalLast != null && arg.parent.children.contains(this.optionalLast)) {
            return this.optionalLast;
        }
        return arg2;
    }

    private void apply() {
        this.advancement.getAdvancement().display().ifPresent(display -> display.setPos(this.depth, this.row));
        if (!this.children.isEmpty()) {
            for (AdvancementPositioner lv : this.children) {
                lv.apply();
            }
        }
    }

    public static void arrangeForTree(PlacedAdvancement root) {
        if (root.getAdvancement().display().isEmpty()) {
            throw new IllegalArgumentException("Can't position children of an invisible root!");
        }
        AdvancementPositioner lv = new AdvancementPositioner(root, null, null, 1, 0);
        lv.calculateRecursively();
        float f = lv.findMinRowRecursively(0.0f, 0, lv.row);
        if (f < 0.0f) {
            lv.increaseRowRecursively(-f);
        }
        lv.apply();
    }
}

