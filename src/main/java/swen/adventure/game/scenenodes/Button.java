package swen.adventure.game.scenenodes;

import swen.adventure.engine.Action;
import swen.adventure.engine.Event;
import swen.adventure.engine.MouseInput;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.Collections;

/**
 * Created by danielbraithwt on 10/2/15.
 */
public class Button extends GameObject {

    public final Event<Button> eventButtonPressed = new Event<>("eventButtonPressed", this);

    public final static Action<MeshNode, MouseInput, Button> actionButtonPressed =
            (eventObject, triggeringObject, button, data) -> button.eventButtonPressed.trigger(triggeringObject, Collections.emptyMap());

    public Button(String id, TransformNode parent) {
        super(id, parent);

        TransformNode body = new TransformNode(id + "ButtonBody", parent, true, new Vector3(100, 100, 0), new Quaternion(), new Vector3(40, 40, 40));
        MeshNode doorMesh = new MeshNode(id + "DoorMesh", "box.obj", body);

        doorMesh.eventMeshClicked.addAction(this, actionButtonPressed);
    }
}
