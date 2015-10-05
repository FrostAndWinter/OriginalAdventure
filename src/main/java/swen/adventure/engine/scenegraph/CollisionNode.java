package swen.adventure.engine.scenegraph;

import swen.adventure.engine.rendering.maths.BoundingBox;

import java.util.Optional;

/**
 * Created by josephbennett on 29/09/15
 */
public class CollisionNode extends SceneNode {

    private Optional<MeshNode> _meshNode = Optional.empty();

    private BoundingBox _worldSpaceBoundingBox = null;
    private BoundingBox _localSpaceBoundingBox;

    public CollisionNode(MeshNode meshNode) {
        super(meshNode.id + "Collider", meshNode.parent().get(), meshNode.isDynamic());
        _meshNode = Optional.of(meshNode);
        _localSpaceBoundingBox = meshNode.boundingBox();
    }

    public CollisionNode(String id, TransformNode parent, BoundingBox boundingBox) {
        super(id, parent, false);
        _localSpaceBoundingBox = boundingBox;
    }

    public boolean isCollidingWith(CollisionNode node) {
        return this.worldSpaceBoundingBox().intersectsWith(node.worldSpaceBoundingBox());
    }

    public BoundingBox worldSpaceBoundingBox() {
        if (_worldSpaceBoundingBox == null) {
            _worldSpaceBoundingBox = this.boundingBox().axisAlignedBoundingBoxInSpace(this.nodeToWorldSpaceTransform());
        }
        return _worldSpaceBoundingBox;
    }

    @Override
    public void transformDidChange() {
        super.transformDidChange();
        _worldSpaceBoundingBox = null;
    }

    /**
     * @return this object's bounding box.
     */
    public BoundingBox boundingBox() {
        return _localSpaceBoundingBox;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        _localSpaceBoundingBox = boundingBox;
    }
}