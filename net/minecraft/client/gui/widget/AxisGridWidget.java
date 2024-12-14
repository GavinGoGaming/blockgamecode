/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.WrapperWidget;
import net.minecraft.util.Util;
import net.minecraft.util.math.Divider;

@Environment(value=EnvType.CLIENT)
public class AxisGridWidget
extends WrapperWidget {
    private final DisplayAxis axis;
    private final List<Element> elements = new ArrayList<Element>();
    private final Positioner mainPositioner = Positioner.create();

    public AxisGridWidget(int width, int height, DisplayAxis axis) {
        this(0, 0, width, height, axis);
    }

    public AxisGridWidget(int x, int y, int width, int height, DisplayAxis axis) {
        super(x, y, width, height);
        this.axis = axis;
    }

    @Override
    public void refreshPositions() {
        super.refreshPositions();
        if (this.elements.isEmpty()) {
            return;
        }
        int i = 0;
        int j = this.axis.getOtherAxisLength(this);
        for (Element lv : this.elements) {
            i += this.axis.getSameAxisLength(lv);
            j = Math.max(j, this.axis.getOtherAxisLength(lv));
        }
        int k = this.axis.getSameAxisLength(this) - i;
        int l = this.axis.getSameAxisCoordinate(this);
        Iterator<Element> iterator = this.elements.iterator();
        Element lv2 = iterator.next();
        this.axis.setSameAxisCoordinate(lv2, l);
        l += this.axis.getSameAxisLength(lv2);
        if (this.elements.size() >= 2) {
            Divider lv3 = new Divider(k, this.elements.size() - 1);
            while (lv3.hasNext()) {
                Element lv4 = iterator.next();
                this.axis.setSameAxisCoordinate(lv4, l += lv3.nextInt());
                l += this.axis.getSameAxisLength(lv4);
            }
        }
        int m = this.axis.getOtherAxisCoordinate(this);
        for (Element lv5 : this.elements) {
            this.axis.setOtherAxisCoordinate(lv5, m, j);
        }
        switch (this.axis.ordinal()) {
            case 0: {
                this.height = j;
                break;
            }
            case 1: {
                this.width = j;
            }
        }
    }

    @Override
    public void forEachElement(Consumer<Widget> consumer) {
        this.elements.forEach(element -> consumer.accept(element.widget));
    }

    public Positioner copyPositioner() {
        return this.mainPositioner.copy();
    }

    public Positioner getMainPositioner() {
        return this.mainPositioner;
    }

    public <T extends Widget> T add(T widget) {
        return this.add(widget, this.copyPositioner());
    }

    public <T extends Widget> T add(T widget, Positioner positioner) {
        this.elements.add(new Element(widget, positioner));
        return widget;
    }

    public <T extends Widget> T add(T widget, Consumer<Positioner> callback) {
        return this.add(widget, Util.make(this.copyPositioner(), callback));
    }

    @Environment(value=EnvType.CLIENT)
    public static enum DisplayAxis {
        HORIZONTAL,
        VERTICAL;


        int getSameAxisLength(Widget widget) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> widget.getWidth();
                case 1 -> widget.getHeight();
            };
        }

        int getSameAxisLength(Element element) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> element.getWidth();
                case 1 -> element.getHeight();
            };
        }

        int getOtherAxisLength(Widget widget) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> widget.getHeight();
                case 1 -> widget.getWidth();
            };
        }

        int getOtherAxisLength(Element element) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> element.getHeight();
                case 1 -> element.getWidth();
            };
        }

        void setSameAxisCoordinate(Element element, int low) {
            switch (this.ordinal()) {
                case 0: {
                    element.setX(low, element.getWidth());
                    break;
                }
                case 1: {
                    element.setY(low, element.getHeight());
                }
            }
        }

        void setOtherAxisCoordinate(Element element, int low, int high) {
            switch (this.ordinal()) {
                case 0: {
                    element.setY(low, high);
                    break;
                }
                case 1: {
                    element.setX(low, high);
                }
            }
        }

        int getSameAxisCoordinate(Widget widget) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> widget.getX();
                case 1 -> widget.getY();
            };
        }

        int getOtherAxisCoordinate(Widget widget) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> widget.getY();
                case 1 -> widget.getX();
            };
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Element
    extends WrapperWidget.WrappedElement {
        protected Element(Widget arg, Positioner arg2) {
            super(arg, arg2);
        }
    }
}

