package swen.adventure.engine.scenegraph;

import swen.adventure.engine.datastorage.BundleObject;

import java.util.function.Function;

/**
 * Created by josephbennett on 19/09/15
 */
public class CameraNode extends SceneNode {

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

    /*@Override
    public BundleObject toBundle() {
        return super.toBundle()
                .put("fieldOfView", _fieldOfView)
                .put("hdrMaxIntensity", _hdrMaxIntensity);
    }*/

    @SuppressWarnings("unused")
    private static CameraNode createNodeFromBundle(BundleObject bundle,
                                                   Function<String, TransformNode> findParentFunction) {
        String id = bundle.getString("id");
        String parentId = bundle.getString("parentId");
        TransformNode parent = findParentFunction.apply(parentId);
        return new CameraNode(id, parent);
    }
}
