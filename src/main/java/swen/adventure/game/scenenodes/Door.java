package swen.adventure.game.scenenodes;

import swen.adventure.engine.Action;
import swen.adventure.engine.MouseInput;
import swen.adventure.engine.animation.AnimableProperty;
import swen.adventure.engine.animation.Animation;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.Map;


public class Door extends GameObject {

    private final Quaternion doorOpenRotation = Quaternion.makeWithAngleAndAxis((float) (Math.PI/2), 0, 1, 0);
    private final Quaternion doorClosedRotation = Quaternion.makeWithAngleAndAxis(0, 0, 1, 0);

    private TransformNode _hingeTransform;

    private boolean open = false;

    public final static Action<MeshNode, MouseInput, Door> actionToggleDoor =
            (eventObject, triggeringObject, door, data) -> door.toggle();

    private AnimableProperty doorRotationProgress = new AnimableProperty(0);

    public Door(String id, TransformNode parent) {
        super(id, parent);

        _hingeTransform = new TransformNode(id + "DoorHinge", parent, true, Vector3.zero, new Quaternion(), Vector3.one);

        TransformNode body = new TransformNode(id + "DoorBody", _hingeTransform, true, Vector3.zero, new Quaternion(), new Vector3(50, 100, 1));
        MeshNode doorMesh = new MeshNode(id + "DoorMesh", "box.obj", body);

        doorMesh.eventMeshClicked.addAction(this, actionToggleDoor);

        _hingeTransform.translateBy(new Vector3(-doorMesh.boundingBox().width() * 50 / 2, 0.f, 0.f));
        body.translateBy(new Vector3(doorMesh.boundingBox().width()*50/2, 0.f, 0.f));
        
        doorRotationProgress.eventValueChanged.addAction(this, (eventObject, triggeringObject, listener, data) ->  {
            listener._hingeTransform.setRotation(Quaternion.makeWithAngleAndAxis((float) (eventObject.value() * (Math.PI/2)), 0, 1, 0));
        });
    }

    public void toggle() {

        if (open) {
            close();
        } else {
            open();
        }
    }

    public void open() {
        open = true;
        new Animation(doorRotationProgress, 0.5f, 1.0f);
    }

    public void close() {
        open = false;
        new Animation(doorRotationProgress, 0.5f, 0.0f);
    }

}
