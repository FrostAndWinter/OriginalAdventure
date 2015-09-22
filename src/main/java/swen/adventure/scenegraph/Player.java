package swen.adventure.scenegraph;

import swen.adventure.rendering.maths.Vector;
import swen.adventure.rendering.maths.Vector3;

/**
 * Created by josephbennett on 19/09/15
 */
public class Player extends GameObject {

    public Player(String id, SceneNode parent) {
        super(id, parent);
    }

    public void move(Vector3 vector) {
        TransformNode transformNode = (TransformNode) parent().get();
        transformNode.translateBy(transformNode.rotation().rotateVector3(vector));
    }

}
