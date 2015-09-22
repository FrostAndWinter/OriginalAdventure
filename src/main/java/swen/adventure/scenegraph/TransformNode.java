package swen.adventure.scenegraph;

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

    public TransformNode(final String id, final SceneNode parent, boolean isDynamic, Vector3 translation, Quaternion rotation, Vector3 scale) {
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
}
