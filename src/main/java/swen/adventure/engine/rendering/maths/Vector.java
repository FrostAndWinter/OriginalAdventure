/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 package swen.adventure.engine.rendering.maths;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 20/09/15.
 */
public interface Vector {
    int numberOfComponents();
    float[] data();
    Vector3 asVector3();
}