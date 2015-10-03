package swen.adventure.game.scenenodes;

import org.lwjgl.Sys;
import swen.adventure.engine.Event;
import swen.adventure.engine.Game;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.Collections;

/**
 * Created by danielbraithwt on 10/3/15.
 */
public class Key extends GameObject {

    private boolean _pickupable;

    private MeshNode _mesh;

    public Key(String id, TransformNode parent) {
        super(id, parent);

        _mesh = new MeshNode("Key_B_02.obj", parent);
        _mesh.eventMeshClicked.addAction(this, (eventObject, player, listener, data) -> {
            if (_pickupable) {
                System.out.println("Key Can Be Picked Up");
            } else {
                System.out.println("Key Cant Be Picked Up");
            }
        });
    }

    public void setPickupable(boolean b) {
        _pickupable = b;
    }

}