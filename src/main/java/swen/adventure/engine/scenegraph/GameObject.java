package swen.adventure.engine.scenegraph;

import swen.adventure.engine.datastorage.BundleObject;

import java.util.function.Function;

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

    @SuppressWarnings("unused")
    private static GameObject createNodeFromBundle(BundleObject bundle,
                                                   Function<String, TransformNode> findParentFunction) {
        String id = bundle.getString("id");
        String parentId = bundle.getString("parentId");
        TransformNode parent = findParentFunction.apply(parentId);
        return new GameObject(id, parent);
    }
}
