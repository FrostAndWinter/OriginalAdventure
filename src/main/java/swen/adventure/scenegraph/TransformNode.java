package swen.adventure.scenegraph;

import processing.core.PMatrix3D;
import swen.adventure.rendering.maths.Matrix4;
import swen.adventure.rendering.maths.Quaternion;
import swen.adventure.rendering.maths.Vector3;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 */
public class TransformNode extends SceneNode {
    private Vector3 _translation;
    private Quaternion _rotation;
    private Vector3 _scale;

    private boolean _needsRecalculateNodeWorldTransform = true;
    private boolean _needsRecalculateTransformWorldNodeTransform = true;

    private Matrix4 _nodeToWorldTransform; //formed by translating, scaling, then rotating.
    private Matrix4 _worldToNodeTransform;

    public TransformNode(final String id, Vector3 translation, Quaternion rotation, Vector3 scale) {
        super(id);
        _translation = translation;
        _rotation = rotation;
        _scale = scale;
    }

    public TransformNode(final String id, final TransformNode parent, boolean isDynamic, Vector3 translation, Quaternion rotation, Vector3 scale) {
        super(id, parent, isDynamic);
        _translation = translation;
        _rotation = rotation;
        _scale = scale;
    }

    private void setNeedsRecalculateTransform() {
        _needsRecalculateNodeWorldTransform = true;
        _needsRecalculateTransformWorldNodeTransform = true;
        this.traverse((node) -> {
            if (node instanceof TransformNode) {
                ((TransformNode)node)._needsRecalculateTransformWorldNodeTransform = true;
                ((TransformNode)node)._needsRecalculateNodeWorldTransform = true;
            } else if (node instanceof GameObject) {
                ((GameObject)node).transformDidChange();
            }
        });
    }

    private Matrix4 calculateNodeToWorldTransform() {
        Matrix4 transform = this.parent().isPresent() ? this.parent().get().nodeToWorldSpaceTransform() : new Matrix4();

        transform = transform.translate(_translation);
        transform = transform.rotate(_rotation);
        transform = transform.scale(_scale);

        return transform;
    }

    private Matrix4 calculateWorldToNodeTransform() {
        Matrix4 transform = this.parent().isPresent() ? this.parent().get().worldToNodeSpaceTransform() : new Matrix4();
        transform = transform.scale(new Vector3(1.f, 1.f, 1.f).divide(_scale));
        transform = transform.rotate(_rotation.conjugate());
        transform = transform.translate(_translation.negate());

        return transform;
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

    public void setTranslation(Vector3 translation) {
        this.checkForModificationOfStaticNode();
        _translation = translation;
        this.setNeedsRecalculateTransform();
    }

    public void translateBy(Vector3 translation) {
        this.setTranslation(_translation.add(translation));
    }

    public void setRotation(Quaternion rotation) {
        this.checkForModificationOfStaticNode();
        _rotation = rotation;
        this.setNeedsRecalculateTransform();
    }

    public void rotateBy(Quaternion rotation) {
        this.setRotation(_rotation.multiply(rotation));
    }

    public void rotateX(float xRotationRadians) {
        this.setRotation(_rotation.rotateByAngleX(xRotationRadians));
    }

    public void rotateY(float yRotationRadians) {
        this.setRotation(_rotation.rotateByAngleY(yRotationRadians));
    }

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

    public Quaternion rotation() {
        return _rotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransformNode that = (TransformNode) o;

        if (_needsRecalculateNodeWorldTransform != that._needsRecalculateNodeWorldTransform) return false;
        if (_needsRecalculateTransformWorldNodeTransform != that._needsRecalculateTransformWorldNodeTransform)
            return false;
        if (!_translation.equals(that._translation)) return false;
        if (!_rotation.equals(that._rotation)) return false;
        return _scale.equals(that._scale);

    }

    @Override
    public int hashCode() {
        int result = _translation.hashCode();
        result = 31 * result + _rotation.hashCode();
        result = 31 * result + _scale.hashCode();
        result = 31 * result + (_needsRecalculateNodeWorldTransform ? 1 : 0);
        result = 31 * result + (_needsRecalculateTransformWorldNodeTransform ? 1 : 0);
        result = 31 * result + _nodeToWorldTransform.hashCode();
        result = 31 * result + _worldToNodeTransform.hashCode();
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
