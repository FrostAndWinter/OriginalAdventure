package swen.adventure.engine.ui.clickable;

/**
 * Created by danielbraithwt on 9/15/15.
 */
public interface Clickable {
    public void addClickListener(OnClickListener c);
    public void removeClickListener(OnClickListener c);
    public void clicked(int x, int y);
}
