package swen.adventure.scenegraph;

import processing.core.PShape;

import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 */
public class MeshNode extends SceneNode {

    private PShape _mesh;

    public MeshNode(String id, final SceneNode parent, PShape mesh) {
        super(id, parent, false);
        _mesh = mesh;
    }

    public PShape mesh() {
        return _mesh;
    }
}
