package swen.adventure.engine.scenegraph;

import swen.adventure.engine.rendering.maths.BoundingBox;

import java.util.Optional;

/**
 * Created by josephbennett on 29/09/15
 *
 * A CollisionNode represents an object that can be collided with.
 * The most common usage is to initialise it with a MeshNode from which to take its bounding box.
 */
public final class CollisionNode extends SceneNode {

    private BoundingBox _worldSpaceBoundingBox = null;
    private BoundingBox _localSpaceBoundingBox = null;

    /**
     * Constructs a new CollisionNode from a given MeshNode.
     * It will have the same parent and bounding box as the mesh.
     * @param meshNode The MeshNode to use in the construction.
     */
    public CollisionNode(MeshNode meshNode) {
        super(meshNode.id + "Collider", meshNode.parent().get(), meshNode.isDynamic());
        _localSpaceBoundingBox = meshNode.boundingBox();
    }

    /**
     * Constructs a new CollisionNode with a specific parent and bounding box.
     * @param id The id the collision node should take.
     * @param parent The collision nodes parent, specifying its transform.
     * @param boundingBox The bounding box of the collision node.
     */
    public CollisionNode(String id, TransformNode parent, BoundingBox boundingBox) {
        super(id, parent, false);
        _localSpaceBoundingBox = boundingBox;
    }

    /**
     * Performs an intersection test against the other node.
     * @param node The node to test against.
     * @return Whether this node intersects with node.
     */
    public boolean isCollidingWith(CollisionNode node) {
        return node != this &&
                this.worldSpaceBoundingBox().intersectsWith(node.worldSpaceBoundingBox());
    }

    /**
     * @return This node's bounding box in world space.
     */
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