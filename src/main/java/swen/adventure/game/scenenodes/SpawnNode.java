package swen.adventure.game.scenenodes;

import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.TransformNode;
import swen.adventure.game.scenenodes.Player;

/**
 * A spawn node is a location in the world where player's can be spawned in at.
 *
 * Created by drb on 06/10/15.
 */
public class SpawnNode extends GameObject {
    public static final String ID = "spawnPoint";

    public SpawnNode(String id, TransformNode parent) {
        super(ID, parent);
    }

    /**
     * Spawn a player at into the world at this spawn point.
     *
     * @param id id of player to spawn in
     */
    public void spawnPlayerWithId(String id) {
        new Player(id, new TransformNode(id + "Transform", parent().get(), true, new Vector3(0, 60, 0), new Quaternion(), Vector3.one));
    }
}
