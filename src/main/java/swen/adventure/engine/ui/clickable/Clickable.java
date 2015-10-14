/* Contributor List  */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* Daniel Braithwaite (braithdani) (300313770) */ 
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
     * Called when the component is clicked
     * @param x x position of the click
     * @param y y position of the click
     */
    public void clicked(int x, int y);
}