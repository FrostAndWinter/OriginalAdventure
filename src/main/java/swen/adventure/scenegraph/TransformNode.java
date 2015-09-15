package swen.adventure.scenegraph;

import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.math.Quaternion;
import swen.adventure.rendering.maths.Vector3;

import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 */
public class TransformNode extends SceneNode {
    private Vector3 _translation;
    private Quaternion _rotation;
    private Vector3 _scale;

    private boolean _needsRecalculateTransform = true;

    private Matrix4 _worldSpaceTransform; //formed by translating, scaling, then rotating.

    public TransformNode(final Optional<String> id, final SceneNode parent, Vector3 translation, Quaternion rotation, Vector3 scale) {
        super(id, parent);
        _translation = translation;
        _rotation = rotation;
        _scale = scale;
    }

    private void setNeedsRecalculateTransform() {
        _needsRecalculateTransform = true;
        this.traverse((node) -> {
            if (node instanceof TransformNode) {
                ((TransformNode)node)._needsRecalculateTransform = true;
            }
        });
    }

    private Matrix4 calculateTransform() {
        Matrix4 transform = this.parent().isPresent() ? this.parent().get().worldSpaceTransform() : new Matrix4();
        transform.scale(_scale.x, _scale.y, _scale.z);
        transform.rotate(_rotation);
        transform.translate(_translation.x, _translation.y, _translation.z);
        return transform;
    }

    @Override
    public Matrix4 worldSpaceTransform() {
        if (_needsRecalculateTransform) {
            _worldSpaceTransform = this.calculateTransform();
            _needsRecalculateTransform = false;
        }
        return _worldSpaceTransform;
    }

}
