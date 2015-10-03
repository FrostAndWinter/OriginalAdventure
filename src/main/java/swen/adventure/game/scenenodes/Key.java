package swen.adventure.game.scenenodes;

import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.TransformNode;

/**
 * Created by danielbraithwt on 10/3/15.
 */
public class Key extends GameObject {

    private boolean _interactionEnabled;

    private MeshNode _mesh;

    public Key(String id, TransformNode parent) {
        super(id, parent);

        _mesh = new MeshNode("Key_B_02.obj", parent);
        _mesh.eventMeshClicked.addAction(this, (eventObject, player, listener, data) -> {
            if (_interactionEnabled) {
                System.out.println("Key can be picked up");
            } else {
                System.out.println("Key can't be picked up");
            }
        });
    }

    public void setInteractionEnabled(boolean b) {
        _interactionEnabled = b;
    }

}
