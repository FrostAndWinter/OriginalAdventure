package swen.adventure.game.scenenodes;

import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.TransformNode;

/**
 * Created by danielbraithwt on 10/3/15.
 * Modified by Thomas Roughton, Student ID 300313924
 */
public class Key extends Item {

    public Key(String id, TransformNode parent) {
        super(id, parent);

        MeshNode keyMesh = new MeshNode(null, "Key_B_02.obj", parent);
        this.setMesh(keyMesh);
    }

}
