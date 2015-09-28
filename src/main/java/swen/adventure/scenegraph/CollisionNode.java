package swen.adventure.scenegraph;

import swen.adventure.utils.BoundingBox;

/**
 * Created by josephbennett on 25/09/15
 */
public class CollisionNode extends SceneNode {

    private boolean _needsRecalculateBoundingBox = true;
    private BoundingBox _boundingBox;
    private BoundingBox _transformedBoundingBox;

    public CollisionNode(String id, TransformNode parent, boolean isDynamic, BoundingBox boundingBox) {
        super(id, parent, isDynamic);
        _boundingBox = boundingBox;
    }

    public BoundingBox boundingBox() {
        if (_needsRecalculateBoundingBox) {
            _transformedBoundingBox = _boundingBox.transformByMatrix(this.nodeToWorldSpaceTransform());
            _needsRecalculateBoundingBox = false;
        }
        return _transformedBoundingBox;
    }

    public void transformDidChange() {
        _needsRecalculateBoundingBox = true;
    }

}
