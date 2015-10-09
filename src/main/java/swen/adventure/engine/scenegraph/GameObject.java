package swen.adventure.engine.scenegraph;

import javafx.scene.shape.Mesh;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 * Implements a variant of the composite/decorator pattern – a game object can hold references to different 'component' nodes, and can be treated as the combination of those nodes.
 * A game object can be 'decorated' with different component nodes.
 */
public class GameObject extends SceneNode {

    private Optional<CollisionNode> _collisionNode = Optional.empty();

    private Optional<Light> _light = Optional.empty();
    private Optional<MeshNode> _mainMesh = Optional.empty();

    /**
     * Game objects can have more than one mesh. A game object usually has
     * more than one mesh if animations are needed, as you can only animate meshes as a whole.
     *
     * TODO Revisit how this is done.
     */
    private List<MeshNode> _meshes = new ArrayList<>();

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

    public void setMainMesh(final MeshNode mesh) {
        _mainMesh = Optional.of(mesh);
        addMesh(mesh);
    }

    public Optional<MeshNode> mesh() {
        return _mainMesh;
    }

    protected void addMesh(MeshNode meshNode) {
        if (_meshes.contains(meshNode)) {
            return;
        }

        _meshes.add(meshNode);
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        super.setEnabled(isEnabled);
        if (_collisionNode.isPresent()) {
            _collisionNode.get().setEnabled(isEnabled);
        }

        for (MeshNode mesh : _meshes) {
            mesh.setEnabled(isEnabled);
        }

        if (_light.isPresent()) {
            _light.get().setEnabled(isEnabled);
        }
    }
}
