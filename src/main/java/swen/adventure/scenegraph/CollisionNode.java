package swen.adventure.scenegraph;

import swen.adventure.utils.BoundingBox;

import javax.swing.text.html.Option;
import java.util.Optional;

/**
 * Created by josephbennett on 29/09/15
 */
public class CollisionNode extends SceneNode {

    private GameObject _gameObject;

    private Optional<BoundingBox> _worldSpaceBoundingBox = Optional.empty();
    private boolean _needsRecalculateWorldSpaceBoundingBox = true;
    private Optional<BoundingBox> _localSpaceBoundingBox = Optional.empty();

    public CollisionNode(GameObject gameObject) {
        this(null, gameObject);
    }

    public CollisionNode(BoundingBox boundingBox, GameObject gameObject) {
        super(gameObject.id + "Collider", gameObject.parent().get(), gameObject.isDynamic());
        _gameObject = gameObject;
        _localSpaceBoundingBox = Optional.ofNullable(boundingBox);
    }

    public boolean isCollidingWith(CollisionNode node) {
        return this.worldSpaceBoundingBox().get().intersectsWith(node.worldSpaceBoundingBox().get());
    }

    public Optional<BoundingBox> worldSpaceBoundingBox() {
        if (_needsRecalculateWorldSpaceBoundingBox) {
            _worldSpaceBoundingBox = this.boundingBox()
                    .map(boundingBox -> boundingBox.axisAlignedBoundingBoxInSpace(this.nodeToWorldSpaceTransform()));
            _needsRecalculateWorldSpaceBoundingBox = false;
        }
        return _worldSpaceBoundingBox;
    }

    @Override
    public void transformDidChange() {
        super.transformDidChange();
        _needsRecalculateWorldSpaceBoundingBox = true;
    }

    /**
     * @return this object's bounding box.
     * If it does not have one, it searches in its siblings for a MeshNode and returns its bounding box.
     */
    public Optional<BoundingBox> boundingBox() {
        if (!_localSpaceBoundingBox.isPresent()) {
            for (SceneNode sibling : this.siblings()) {
                if (sibling instanceof MeshNode) {
                    _localSpaceBoundingBox = ((MeshNode) sibling).boundingBox();
                    break;
                }
            }
        }
        return _localSpaceBoundingBox;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        _localSpaceBoundingBox = Optional.of(boundingBox);
    }
}