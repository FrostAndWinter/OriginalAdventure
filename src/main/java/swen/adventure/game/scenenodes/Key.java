package swen.adventure.game.scenenodes;

import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.TransformNode;

/**
 * Daniel Braithwate, 300313770
 * Thomas Roughton, 300313924
 * Joseph Bennett, 300319773
 */
public class Key extends Item {

    private static final String KeyDescription = "A key. Perhaps it opens a door?";

    public Key(String id, TransformNode parent) {
        super(id, parent, "Key", KeyDescription);

        final String keyMeshId = id + "KeyMesh";

        MeshNode keyMesh = parent.findNodeWithIdOrCreate(keyMeshId, () -> new MeshNode(keyMeshId, null, "Key_B_02.obj", parent));
        this.setMesh(keyMesh);
    }

}
