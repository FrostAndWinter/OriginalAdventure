package swen.adventure.engine.ui.clickable;

/**
 * Created by danielbraithwt on 9/15/15.
 */
public interface Clickable {
    /**
     * @param c listener to add
     */
    public void addClickListener(OnClickListener c);

    /**
     * @param c listener to remove
     */
    public void removeClickListener(OnClickListener c);

    /**
     *
     * @param x
     * @param y
     */
    public void clicked(int x, int y);
}
