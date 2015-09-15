package swen.adventure.scenegraph;

import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 */
public class GameObject extends SceneNode {

    public GameObject(final Optional<String> id, final SceneNode parent) {
        super(id, parent);
    }
}
