package swen.adventure.game.scenenodes;

import swen.adventure.engine.Game;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.TransformNode;

/**
 * Created by danielbraithwt on 10/3/15.
 */
public class Key extends GameObject {

    private MeshNode _mesh;

    public Key(String id, TransformNode parent) {
        super(id, parent);

        new MeshNode("Key_B_02.obj", parent);
    }
}
