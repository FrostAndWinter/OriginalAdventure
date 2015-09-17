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

    private boolean _needsRecalculateModelWorldTransform = true;
    private boolean _needsRecalculateTransformWorldModelTransform = true;

    private Matrix4 _modelToWorldTransform; //formed by translating, scaling, then rotating.
    private Matrix4 _worldToModelTransform;

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
        _needsRecalculateModelWorldTransform = true;
        _needsRecalculateTransformWorldModelTransform = true;
        this.traverse((node) -> {
            if (node instanceof TransformNode) {
                ((TransformNode)node)._needsRecalculateTransformWorldModelTransform = true;
                ((TransformNode)node)._needsRecalculateModelWorldTransform = true;
            } else if (node instanceof GameObject) {
                ((GameObject)node).transformDidChange();
            }
        });
    }

    private Matrix4 calculateModelToWorldTransform() {
        Matrix4 transform = this.parent().isPresent() ? this.parent().get().modelToWorldSpaceTransform() : new Matrix4();

        transform = transform.translateWithVector3(_translation);
        transform = transform.rotate(_rotation);
        transform = transform.scaleWithVector3(_scale);

        return transform;
    }

    private Matrix4 calculateWorldToModelTransform() {
        Matrix4 transform = this.parent().isPresent() ? this.parent().get().worldToModelSpaceTransform() : new Matrix4();
        transform = transform.scaleWithVector3(new Vector3(1.f, 1.f, 1.f).divide(_scale));
        transform = transform.rotate(_rotation.conjugate());
        transform = transform.translateWithVector3(_translation.negate());
        return transform;
    }

    @Override
    public Matrix4 modelToWorldSpaceTransform() {
        if (_needsRecalculateModelWorldTransform) {
            _modelToWorldTransform = this.calculateModelToWorldTransform();
            _needsRecalculateModelWorldTransform = false;
        }
        return _modelToWorldTransform;
    }

    @Override
    public Matrix4 worldToModelSpaceTransform() {
        if (_needsRecalculateTransformWorldModelTransform) {
            _worldToModelTransform = this.calculateWorldToModelTransform();
            _needsRecalculateTransformWorldModelTransform = false;
        }
        return _worldToModelTransform;
    }

    private void checkForModificationOfStaticNode() {
        if (!this.isDynamic()) {
            throw new RuntimeException("Static node with id " + this.id + " cannot be transformed.");
        }
    }

    public void translateBy(Vector3 translation) {
        this.checkForModificationOfStaticNode();
        _translation = _translation.add(translation);
        this.setNeedsRecalculateTransform();
    }

    public void rotateBy(Quaternion rotation) {
        this.checkForModificationOfStaticNode();
        _rotation = _rotation.multiply(rotation);
        this.setNeedsRecalculateTransform();
    }

    public void rotateX(float xRotationRadians) {
        this.checkForModificationOfStaticNode();
        _rotation = _rotation.rotateByAngleX(xRotationRadians);
        this.setNeedsRecalculateTransform();
    }

    public void rotateY(float yRotationRadians) {
        this.checkForModificationOfStaticNode();
        _rotation = _rotation.rotateByAngleY(yRotationRadians);
        this.setNeedsRecalculateTransform();
    }

    public void rotateZ(float zRotationRadians) {
        this.checkForModificationOfStaticNode();
        _rotation = _rotation.rotateByAngleZ(zRotationRadians);
        this.setNeedsRecalculateTransform();
    }

    public void scaleBy(Vector3 scale) {
        this.checkForModificationOfStaticNode();
        _scale = _scale.multiply(scale);
        this.setNeedsRecalculateTransform();
    }

    public void scaleBy(float scale) {
        this.checkForModificationOfStaticNode();
        _scale = _scale.multiplyScalar(scale);
        this.setNeedsRecalculateTransform();
    }

}
