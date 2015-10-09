package swen.adventure.engine.scenegraph;

/**
 * Created by josephbennett on 19/09/15
 *
 * Represents a camera in the world that can be used for rendering.
 */
public final class CameraNode extends SceneNode {

    private float _fieldOfView = (float)Math.PI/3.f;
    private float _hdrMaxIntensity = 12.f;

    public CameraNode(String id, TransformNode parent) {
        super(id, parent, false);
    }

    public float fieldOfView() {
        return _fieldOfView;
    }

    public void setFieldOfView(final float fieldOfView) {
        _fieldOfView = fieldOfView;
    }

    public float hdrMaxIntensity() {
        return _hdrMaxIntensity;
    }

    public void setHDRMaxIntensity(final float hdrMaxIntensity) {
        _hdrMaxIntensity = hdrMaxIntensity;
    }
}
