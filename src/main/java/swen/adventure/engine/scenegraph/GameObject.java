package swen.adventure.engine.scenegraph;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 */
public class GameObject extends SceneNode {

    private CollisionNode _collisionNode = new CollisionNode(this);

    public GameObject(String id, final TransformNode parent) {
        super(id, parent, true);
    }

    public CollisionNode collisionNode() {
        return _collisionNode;
    }
}