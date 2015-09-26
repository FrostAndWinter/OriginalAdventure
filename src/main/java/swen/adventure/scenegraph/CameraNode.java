package swen.adventure.scenegraph;

/**
 * Created by josephbennett on 19/09/15
 */
public class CameraNode extends GameObject {

    private float _fieldOfView = (float)Math.PI/3.f;
    private float _gamma = 2.2f;
    private float _hdrMaxIntensity = 100.f;

    public CameraNode(String id, TransformNode parent) {
        super(id, parent);
    }

    public float fieldOfView() {
        return _fieldOfView;
    }

    public void setFieldOfView(final float fieldOfView) {
        _fieldOfView = fieldOfView;
    }

    public float gamma() {
        return _gamma;
    }

    public void setGamma(final float gamma) {
        _gamma = gamma;
    }

    public float hdrMaxIntensity() {
        return _hdrMaxIntensity;
    }

    public void setHDRMaxIntensity(final float hdrMaxIntensity) {
        _hdrMaxIntensity = hdrMaxIntensity;
    }
}
