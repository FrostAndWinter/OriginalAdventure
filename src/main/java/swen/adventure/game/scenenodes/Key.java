package swen.adventure.game.scenenodes;

import swen.adventure.engine.Action;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.Puzzle;
import swen.adventure.engine.scenegraph.TransformNode;

/**
 * Created by danielbraithwt on 10/3/15.
 * Modified by Thomas Roughton, Student ID 300313924
 */
public class Key extends Item {

    private static final String KeyDescription = "A key. Perhaps it opens a door?";

    public Action<Puzzle, Puzzle, Key> actionSetEnabled = (puzzle, ignored, key, data) -> {
        this.setEnabled(true);
    };

    public Action<Puzzle, Puzzle, Key> actionSetDisabled = (puzzle, ignored, key, data) -> {
        this.setEnabled(false);
    };

    public Key(String id, TransformNode parent) {
        super(id, parent, KeyDescription);

        final String keyMeshId = id + "KeyMesh";

        MeshNode keyMesh = parent.findNodeWithIdOrCreate(keyMeshId, () -> new MeshNode(keyMeshId, null, "Key_B_02.obj", parent));
        this.setMesh(keyMesh);
    }

}
