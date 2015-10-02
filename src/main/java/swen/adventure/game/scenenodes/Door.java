package swen.adventure.game.scenenodes;

import swen.adventure.engine.Action;
import swen.adventure.engine.animation.AnimableProperty;
import swen.adventure.engine.animation.Animation;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.TransformNode;


public class Door extends GameObject {

    private final Quaternion doorOpenRotation = Quaternion.makeWithAngleAndAxis((float) (Math.PI/2), 0, 1, 0);
    private final Quaternion doorClosedRotation = Quaternion.makeWithAngleAndAxis(0, 0, 1, 0);

    private TransformNode _hingeTransform;

    private boolean open = false;

    public final static Action<MeshNode, Player, Door> actionToggleDoor =
            (eventObject, player, door, data) -> door.toggle();

    private AnimableProperty _doorRotationProgress = new AnimableProperty(0);

    public Door(String id, TransformNode parent) {
        super(id, parent);

        _hingeTransform = new TransformNode(id + "DoorHinge", parent, true, Vector3.zero, new Quaternion(), Vector3.one);

        TransformNode body = new TransformNode(id + "DoorBody", _hingeTransform, true, Vector3.zero, new Quaternion(), new Vector3(50, 100, 1));
        MeshNode doorMesh = new MeshNode(id + "DoorMesh", "box.obj", body);

        doorMesh.eventMeshClicked.addAction(this, actionToggleDoor);

        _hingeTransform.translateBy(new Vector3(-doorMesh.boundingBox().width() * 50 / 2, 0.f, 0.f));
        body.translateBy(new Vector3(doorMesh.boundingBox().width()*50/2, 0.f, 0.f));
        
        _doorRotationProgress.eventValueChanged.addAction(this, (eventObject, triggeringObject, listener, data) ->  {
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
        new Animation(_doorRotationProgress, Math.abs(0.5f - _doorRotationProgress.value()), 1.0f);
    }

    public void close() {
        open = false;
        new Animation(_doorRotationProgress, Math.abs(0.5f - _doorRotationProgress.value()), 0.0f);
    }

}
