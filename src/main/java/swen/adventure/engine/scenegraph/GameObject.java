package swen.adventure.engine.scenegraph;

import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 */
public class GameObject extends SceneNode {

    private Optional<CollisionNode> _collisionNode = Optional.empty();

    public GameObject(String id, final TransformNode parent) {
        super(id, parent, true);
    }

    public Optional<CollisionNode> collisionNode() {
        return _collisionNode;
    }

    public void setCollisionNode(CollisionNode collisionNode) {
        _collisionNode = Optional.of(collisionNode);
    }
}
