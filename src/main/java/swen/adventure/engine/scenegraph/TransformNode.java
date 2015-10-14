package swen.adventure.engine.scenegraph;

import swen.adventure.engine.datastorage.BundleObject;
import swen.adventure.engine.datastorage.BundleSerializable;
import swen.adventure.engine.rendering.maths.Matrix4;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;

import java.util.Objects;
import java.util.function.Function;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 *
 * A TransformNode represents a transform between some local space and world space.
 */
public final class TransformNode extends SceneNode {
    private Vector3 _translation;
    private Quaternion _rotation;
    private Vector3 _scale;

    private boolean _needsRecalculateNodeWorldTransform = true;
    private boolean _needsRecalculateTransformWorldNodeTransform = true;

    private Matrix4 _nodeToWorldTransform; //formed by translating, scaling, then rotating.
    private Matrix4 _worldToNodeTransform;

    /**
     * Constructs a new root transform node with the specified rotation, translation and scale.
     * @param id The id of the transform.
     * @param translation The translation, in its parent's coordinate system, of this node.
     * @param rotation The rotation of this transform about its own axis.
     * @param scale The scale of this object in relation to its own axes, before rotation.
     */
    public TransformNode(final String id, Vector3 translation, Quaternion rotation, Vector3 scale) {
        super(id);
        _translation = Objects.requireNonNull(translation);
        _rotation = Objects.requireNonNull(rotation);
        _scale = Objects.requireNonNull(scale);
    }

    /**
     * Constructs a new transform node with the specified rotation, translation and scale.
     * @param id The id of the transform.
     * @param parent The parent transform to this transform.
     * @param translation The translation, in its parent's coordinate system, of this node.
     * @param rotation The rotation of this transform about its own axis.
     * @param scale The scale of this object in relation to its own axes, before rotation.
     */
    public TransformNode(final String id, final TransformNode parent, boolean isDynamic, Vector3 translation, Quaternion rotation, Vector3 scale) {
        super(id, parent, isDynamic);
        _translation = Objects.requireNonNull(translation);
        _rotation = Objects.requireNonNull(rotation);
        _scale = Objects.requireNonNull(scale);
    }

    @Override
    public void transformDidChange() {
        super.transformDidChange();
        _needsRecalculateNodeWorldTransform = true;
        _needsRecalculateTransformWorldNodeTransform = true;
    }

    /**
     * Causes this transform node to regenerate its transformation matrix and call transformDidChange on all of its children.
     */
    private void setNeedsRecalculateTransform() {
        this.traverse(SceneNode::transformDidChange);
    }

    /**
     * @return a matrix that converts from local space to world space (i.e. the space of the root node.)
     */
    private Matrix4 calculateNodeToWorldTransform() {
        Matrix4 transform = this.parent().isPresent() ? this.parent().get().nodeToWorldSpaceTransform() : new Matrix4();

        transform = transform.translate(_translation);
        transform = transform.rotate(_rotation);
        transform = transform.scale(_scale);

        return transform;
    }

    /**
     * @return a matrix that converts from world space (i.e. the space of the root node) to local space.
     */
    private Matrix4 calculateWorldToNodeTransform() {
        Matrix4 parentTransform = this.parent().isPresent() ? this.parent().get().worldToNodeSpaceTransform() : new Matrix4();

        Matrix4 transform = Matrix4.makeScale(Vector3.one.divide(_scale));
        transform = transform.rotate(_rotation.conjugate());
        transform = transform.translate(_translation.negate());

        return transform.multiply(parentTransform);
    }

    @Override
    public Matrix4 nodeToWorldSpaceTransform() {
        if (_needsRecalculateNodeWorldTransform) {
            _nodeToWorldTransform = this.calculateNodeToWorldTransform();
            _needsRecalculateNodeWorldTransform = false;
        }
        return _nodeToWorldTransform;
    }

    @Override
    public Matrix4 worldToNodeSpaceTransform() {
        if (_needsRecalculateTransformWorldNodeTransform) {
            _worldToNodeTransform = this.calculateWorldToNodeTransform();
            _needsRecalculateTransformWorldNodeTransform = false;
        }
        return _worldToNodeTransform;
    }

    private void checkForModificationOfStaticNode() {
        if (!this.isDynamic()) {
            throw new RuntimeException("Static node with id " + this.id + " cannot be transformed.");
        }
    }

    /**
     * @param translation This node's translation, specified in its parent's coordinate space.
     */
    public void setTranslation(Vector3 translation) {
        this.checkForModificationOfStaticNode();
        _translation = translation;
        this.setNeedsRecalculateTransform();
    }

    /**
     * Translates this node by translation, specified in its parent's coordinate space.
     * @param translation the translation, specified in this node's parent's coordinate space.
     */
    public void translateBy(Vector3 translation) {
        this.setTranslation(_translation.add(translation));
    }

    /**
     * @param rotation The rotation of this transform about its own axis.
     */
    public void setRotation(Quaternion rotation) {
        this.checkForModificationOfStaticNode();
        _rotation = rotation;
        this.setNeedsRecalculateTransform();
    }

    /**
     * Rotates the node by rotation (performs quaternion multiplication with rotation on the right).
     * @param rotation The rotation to rotate by.
     */
    public void rotateBy(Quaternion rotation) {
        this.setRotation(_rotation.multiply(rotation));
    }

    /**
     * Rotates by an angle about the x axis.
     * @param xRotationRadians The angle to rotate by.
     */
    public void rotateX(float xRotationRadians) {
        this.setRotation(_rotation.rotateByAngleX(xRotationRadians));
    }

    /**
     * Rotates by an angle about the y axis.
     * @param yRotationRadians The angle to rotate by.
     */
    public void rotateY(float yRotationRadians) {
        this.setRotation(_rotation.rotateByAngleY(yRotationRadians));
    }

    /**
     * Rotates by an angle about the z axis.
     * @param zRotationRadians The angle to rotate by.
     */
    public void rotateZ(float zRotationRadians) {
        this.setRotation(_rotation.rotateByAngleZ(zRotationRadians));
    }

    public void setScale(Vector3 scale) {
        this.checkForModificationOfStaticNode();
        _scale = scale;
        this.setNeedsRecalculateTransform();
    }

    public void scaleBy(Vector3 scale) {
        this.setScale(_scale.multiply(scale));
    }

    public void scaleBy(float scale) {
        this.setScale(_scale.multiplyScalar(scale));
    }

    /**
     * @return This node's translation, specified in its parent's coordinate space.
     */
    public Vector3 translation() {
        return _translation;
    }

    /**
     * @return The rotation of this transform about its own axis.
     */
    public Quaternion rotation() {
        return _rotation;
    }

    /**
     * @return The scale of this object in relation to its own axes, before rotation.
     */
    public Vector3 scale() {
        return _scale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransformNode that = (TransformNode) o;

        if (_needsRecalculateNodeWorldTransform != that._needsRecalculateNodeWorldTransform) return false;
        if (_needsRecalculateTransformWorldNodeTransform != that._needsRecalculateTransformWorldNodeTransform)
            return false;
        if (_translation != null ? !_translation.equals(that._translation) : that._translation != null) return false;
        if (_rotation != null ? !_rotation.equals(that._rotation) : that._rotation != null) return false;
        if (_scale != null ? !_scale.equals(that._scale) : that._scale != null) return false;
        if (_nodeToWorldTransform != null ? !_nodeToWorldTransform.equals(that._nodeToWorldTransform) : that._nodeToWorldTransform != null)
            return false;
        return !(_worldToNodeTransform != null ? !_worldToNodeTransform.equals(that._worldToNodeTransform) : that._worldToNodeTransform != null);

    }

    @Override
    public void setParent(TransformNode newParent) {
        super.setParent(newParent);
        this.setNeedsRecalculateTransform();
    }

    @Override
    public int hashCode() {
        int result = _translation != null ? _translation.hashCode() : 0;
        result = 31 * result + (_rotation != null ? _rotation.hashCode() : 0);
        result = 31 * result + (_scale != null ? _scale.hashCode() : 0);
        result = 31 * result + (_needsRecalculateNodeWorldTransform ? 1 : 0);
        result = 31 * result + (_needsRecalculateTransformWorldNodeTransform ? 1 : 0);
        result = 31 * result + (_nodeToWorldTransform != null ? _nodeToWorldTransform.hashCode() : 0);
        result = 31 * result + (_worldToNodeTransform != null ? _worldToNodeTransform.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TransformNode{" +
                "_needsRecalculateNodeWorldTransform=" + _needsRecalculateNodeWorldTransform +
                ", _translation=" + _translation +
                ", _rotation=" + _rotation +
                ", _scale=" + _scale +
                ", _needsRecalculateTransformWorldNodeTransform=" + _needsRecalculateTransformWorldNodeTransform +
                ", _nodeToWorldTransform=" + _nodeToWorldTransform +
                ", _worldToNodeTransform=" + _worldToNodeTransform +
                '}';
    }
}
