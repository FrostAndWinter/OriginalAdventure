package swen.adventure.game.scenenodes;

import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.TransformNode;
import swen.adventure.game.scenenodes.Player;

/**
 * Created by drb on 06/10/15.
 */
public class SpawnNode extends GameObject {
    public static final String ID = "spawnPoint";

    public SpawnNode(String id, TransformNode parent) {
        super(ID, parent);
    }

    public void spawnPlayerWithId(String id) {
        new Player(id, parent().get());
    }
}
