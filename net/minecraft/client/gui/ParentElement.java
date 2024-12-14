/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.navigation.NavigationAxis;
import net.minecraft.client.gui.navigation.NavigationDirection;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

@Environment(value=EnvType.CLIENT)
public interface ParentElement
extends Element {
    public List<? extends Element> children();

    default public Optional<Element> hoveredElement(double mouseX, double mouseY) {
        for (Element element : this.children()) {
            if (!element.isMouseOver(mouseX, mouseY)) continue;
            return Optional.of(element);
        }
        return Optional.empty();
    }

    @Override
    default public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Element element : this.children()) {
            if (!element.mouseClicked(mouseX, mouseY, button)) continue;
            this.setFocused(element);
            if (button == 0) {
                this.setDragging(true);
            }
            return true;
        }
        return false;
    }

    @Override
    default public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.isDragging()) {
            this.setDragging(false);
            if (this.getFocused() != null) {
                return this.getFocused().mouseReleased(mouseX, mouseY, button);
            }
        }
        return this.hoveredElement(mouseX, mouseY).filter(element -> element.mouseReleased(mouseX, mouseY, button)).isPresent();
    }

    @Override
    default public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.getFocused() != null && this.isDragging() && button == 0) {
            return this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return false;
    }

    public boolean isDragging();

    public void setDragging(boolean var1);

    @Override
    default public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return this.hoveredElement(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent();
    }

    @Override
    default public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.getFocused() != null && this.getFocused().keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    default public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return this.getFocused() != null && this.getFocused().keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    default public boolean charTyped(char chr, int modifiers) {
        return this.getFocused() != null && this.getFocused().charTyped(chr, modifiers);
    }

    @Nullable
    public Element getFocused();

    public void setFocused(@Nullable Element var1);

    @Override
    default public void setFocused(boolean focused) {
    }

    @Override
    default public boolean isFocused() {
        return this.getFocused() != null;
    }

    @Override
    @Nullable
    default public GuiNavigationPath getFocusedPath() {
        Element lv = this.getFocused();
        if (lv != null) {
            return GuiNavigationPath.of(this, lv.getFocusedPath());
        }
        return null;
    }

    @Override
    @Nullable
    default public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        GuiNavigationPath lv2;
        Element lv = this.getFocused();
        if (lv != null && (lv2 = lv.getNavigationPath(navigation)) != null) {
            return GuiNavigationPath.of(this, lv2);
        }
        if (navigation instanceof GuiNavigation.Tab) {
            GuiNavigation.Tab lv3 = (GuiNavigation.Tab)navigation;
            return this.computeNavigationPath(lv3);
        }
        if (navigation instanceof GuiNavigation.Arrow) {
            GuiNavigation.Arrow lv4 = (GuiNavigation.Arrow)navigation;
            return this.computeNavigationPath(lv4);
        }
        return null;
    }

    @Nullable
    private GuiNavigationPath computeNavigationPath(GuiNavigation.Tab navigation) {
        Supplier<Element> supplier;
        BooleanSupplier booleanSupplier;
        boolean bl = navigation.forward();
        Element lv = this.getFocused();
        ArrayList<? extends Element> list = new ArrayList<Element>(this.children());
        Collections.sort(list, Comparator.comparingInt(arg -> arg.getNavigationOrder()));
        int i = list.indexOf(lv);
        int j = lv != null && i >= 0 ? i + (bl ? 1 : 0) : (bl ? 0 : list.size());
        ListIterator listIterator = list.listIterator(j);
        BooleanSupplier booleanSupplier2 = bl ? listIterator::hasNext : (booleanSupplier = listIterator::hasPrevious);
        Supplier<Element> supplier2 = bl ? listIterator::next : (supplier = listIterator::previous);
        while (booleanSupplier.getAsBoolean()) {
            Element lv2 = supplier.get();
            GuiNavigationPath lv3 = lv2.getNavigationPath(navigation);
            if (lv3 == null) continue;
            return GuiNavigationPath.of(this, lv3);
        }
        return null;
    }

    @Nullable
    private GuiNavigationPath computeNavigationPath(GuiNavigation.Arrow navigation) {
        Element lv = this.getFocused();
        if (lv == null) {
            NavigationDirection lv2 = navigation.direction();
            ScreenRect lv3 = this.getNavigationFocus().getBorder(lv2.getOpposite());
            return GuiNavigationPath.of(this, this.computeChildPath(lv3, lv2, null, navigation));
        }
        ScreenRect lv4 = lv.getNavigationFocus();
        return GuiNavigationPath.of(this, this.computeChildPath(lv4, navigation.direction(), lv, navigation));
    }

    @Nullable
    private GuiNavigationPath computeChildPath(ScreenRect focus, NavigationDirection direction, @Nullable Element focused, GuiNavigation navigation) {
        NavigationAxis lv = direction.getAxis();
        NavigationAxis lv2 = lv.getOther();
        NavigationDirection lv3 = lv2.getPositiveDirection();
        int i = focus.getBoundingCoordinate(direction.getOpposite());
        ArrayList<Element> list = new ArrayList<Element>();
        for (Element element2 : this.children()) {
            ScreenRect lv5;
            if (element2 == focused || !(lv5 = element2.getNavigationFocus()).overlaps(focus, lv2)) continue;
            int j = lv5.getBoundingCoordinate(direction.getOpposite());
            if (direction.isAfter(j, i)) {
                list.add(element2);
                continue;
            }
            if (j != i || !direction.isAfter(lv5.getBoundingCoordinate(direction), focus.getBoundingCoordinate(direction))) continue;
            list.add(element2);
        }
        Comparator<Element> comparator = Comparator.comparing(element -> element.getNavigationFocus().getBoundingCoordinate(direction.getOpposite()), direction.getComparator());
        Comparator<Element> comparator2 = Comparator.comparing(element -> element.getNavigationFocus().getBoundingCoordinate(lv3.getOpposite()), lv3.getComparator());
        list.sort(comparator.thenComparing(comparator2));
        for (Element lv6 : list) {
            GuiNavigationPath lv7 = lv6.getNavigationPath(navigation);
            if (lv7 == null) continue;
            return lv7;
        }
        return this.computeInitialChildPath(focus, direction, focused, navigation);
    }

    @Nullable
    private GuiNavigationPath computeInitialChildPath(ScreenRect focus, NavigationDirection direction, @Nullable Element focused, GuiNavigation navigation) {
        NavigationAxis lv = direction.getAxis();
        NavigationAxis lv2 = lv.getOther();
        ArrayList<Pair> list = new ArrayList<Pair>();
        ScreenPos lv3 = ScreenPos.of(lv, focus.getBoundingCoordinate(direction), focus.getCenter(lv2));
        for (Element element : this.children()) {
            ScreenRect lv5;
            ScreenPos lv6;
            if (element == focused || !direction.isAfter((lv6 = ScreenPos.of(lv, (lv5 = element.getNavigationFocus()).getBoundingCoordinate(direction.getOpposite()), lv5.getCenter(lv2))).getComponent(lv), lv3.getComponent(lv))) continue;
            long l = Vector2i.distanceSquared(lv3.x(), lv3.y(), lv6.x(), lv6.y());
            list.add(Pair.of(element, l));
        }
        list.sort(Comparator.comparingDouble(Pair::getSecond));
        for (Pair pair : list) {
            GuiNavigationPath lv7 = ((Element)pair.getFirst()).getNavigationPath(navigation);
            if (lv7 == null) continue;
            return lv7;
        }
        return null;
    }
}

