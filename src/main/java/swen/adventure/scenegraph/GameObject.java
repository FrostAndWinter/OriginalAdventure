package swen.adventure.scenegraph;

import swen.adventure.utils.BoundingBox;
import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 */
public class GameObject extends SceneNode {

    private BoundingBox _boundingBox;

    public GameObject(String id, final SceneNode parent) {
        super(id, parent, true);
    }
}
