package swen.adventure.engine.scenegraph;

import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 * Implements a variant of the component pattern – a game object can hold references to different 'component' nodes.
 */
public class GameObject extends SceneNode {

    private Optional<CollisionNode> _collisionNode = Optional.empty();

    private Optional<Light> _light = Optional.empty();
    private Optional<MeshNode> _mesh = Optional.empty();

    public GameObject(String id, final TransformNode parent) {
        super(id, parent, true);
    }

    public Optional<CollisionNode> collisionNode() {
        return _collisionNode;
    }

    public void setCollisionNode(CollisionNode collisionNode) {
        _collisionNode = Optional.of(collisionNode);
    }

    public void setLight(final Light light) {
        _light = Optional.of(light);
    }

    public Optional<Light> light() {
        return _light;
    }

    public void setMesh(final MeshNode mesh) {
        _mesh = Optional.of(mesh);
    }

    public Optional<MeshNode> mesh() {
        return _mesh;
    }

    public Optional<CollisionNode> getCollisionNode() {
        return _collisionNode;
    }
}
