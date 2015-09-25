package swen.adventure.scenegraph;

import swen.adventure.rendering.maths.Vector3;

/**
 * Created by josephbennett on 19/09/15
 */
public class Player extends GameObject {

    public Player(String id, TransformNode parent) {
        super(id, parent);
    }

    public void move(Vector3 vector) {
        TransformNode transformNode = parent().get();
        Vector3 translation = transformNode.rotation().rotateVector3(vector);
        Vector3 lateralTranslation = new Vector3(translation.x, 0, translation.z).normalise().multiplyScalar(vector.length());
        this.parent().get().translateBy(lateralTranslation);
    }

}
