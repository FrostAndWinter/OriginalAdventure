package swen.adventure.ui.LayoutManagers;

import processing.core.PGraphics;
import swen.adventure.ui.components.UIComponent;

import java.util.List;

/**
 * Created by danielbraithwt on 9/19/15.
 */
public class LinearLayout extends LayoutManager {

    public static final int LINEAR_LAYOUT_VERTICAL = 0;
    public static final int LINEAR_LAYOUT_HORIZONTAL = 1;

    private int orientation;

    public LinearLayout(int o) {
        switch (o) {
            case LINEAR_LAYOUT_HORIZONTAL:
                orientation = LINEAR_LAYOUT_HORIZONTAL;
                break;

            case LINEAR_LAYOUT_VERTICAL:
                orientation = LINEAR_LAYOUT_VERTICAL;
                break;

            default:
                throw new IllegalArgumentException("Invalid orientation");
        }
    }

    @Override
    public void applyLayout(PGraphics g) {
        height = 0;
        width = 0;

        int currentX = padding / 2;
        int currentY = padding / 2;

        if (orientation == LINEAR_LAYOUT_VERTICAL) {
            for (int i = 0; i < components.size(); i++) {
                UIComponent c = components.get(i);

                if (width < c.getWidth(g)) {
                    width = c.getWidth(g);
                }

                height += c.getHeight(g);

                c.setX(currentX);
                c.setY(currentY);

                currentY += c.getHeight(g) + padding / 2;
                if (i != 0) {
                    height += padding / 2;
                }
            }
        } else if (orientation == LINEAR_LAYOUT_HORIZONTAL) {
            for (int i = 0; i < components.size(); i++) {
                UIComponent c = components.get(i);

                if (height < c.getHeight(g)) {
                    height = c.getHeight(g);
                }

                width += c.getWidth(g);

                c.setX(currentX);
                c.setY(currentY);

                currentX += c.getWidth(g) + padding / 2;
                if (i != 0) {
                    width += padding / 2;
                }
            }
        }
    }
}
