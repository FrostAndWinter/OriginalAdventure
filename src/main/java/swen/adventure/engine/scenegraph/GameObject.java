/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Liam O'Niell (oneilliam) (300312734) */ 
 /* Liam O'Niell (oneilliam) (300312734) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 package swen.adventure.engine.scenegraph;

import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 * Implements a variant of the composite/decorator pattern – a game object can hold references to different 'component' nodes, and can be treated as the combination of those nodes.
 * A game object can be 'decorated' with different component nodes.
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

    @Override
    public void setEnabled(boolean isEnabled) {
        super.setEnabled(isEnabled);

        _mesh.ifPresent(mesh -> mesh.setEnabled(isEnabled));

        _collisionNode.ifPresent(collisionNode -> collisionNode.setEnabled(isEnabled));

        _light.ifPresent(light -> light.setEnabled(isEnabled));
    }
}