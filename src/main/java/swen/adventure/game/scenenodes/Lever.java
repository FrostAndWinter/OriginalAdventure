package swen.adventure.game.scenenodes;

import swen.adventure.engine.Action;
import swen.adventure.engine.Event;
import swen.adventure.engine.animation.AnimableProperty;
import swen.adventure.engine.animation.Animation;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.SceneNode;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.Collections;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 4/10/15.
 */
public class Lever extends GameObject{

    public final Event<SceneNode> eventLeverToggled = new Event<>("eventLeverToggled", this);

    private TransformNode _hingeTransform;

    private boolean _isDown = true;

    public final static Action<MeshNode, Player, Lever> actionToggleLever =
            (eventObject, player, lever, data) -> lever.toggle(player);

    private AnimableProperty _leverRotationProgress = new AnimableProperty(0);

    public Lever(String id, TransformNode parent) {
        super(id, parent);

        MeshNode leverBaseMesh = new MeshNode(id + "LeverBase", "Lever", "LeverBase.obj", parent);

        _hingeTransform = new TransformNode(id + "LeverHinge", parent, true, Vector3.zero, new Quaternion(), Vector3.one);

        MeshNode leverMesh = new MeshNode(id + "Lever", "Lever", "Lever.obj", _hingeTransform);

        leverBaseMesh.eventMeshClicked.addAction(this, actionToggleLever);
        leverMesh.eventMeshClicked.addAction(this, actionToggleLever);

        _leverRotationProgress.eventValueChanged.addAction(this, (eventObject, triggeringObject, listener, data) ->  {
            listener._hingeTransform.setRotation(Quaternion.makeWithAngleAndAxis((float) (eventObject.value() * (-Math.PI/3)), 0, 0, 1));
        });
    }

    public void toggle(Player player) {
        if (_isDown) {
            this.moveUp();
        } else {
            this.moveDown();
        }

        this.eventLeverToggled.trigger(player, Collections.emptyMap());
    }

    public void moveUp() {
        _isDown = false;
        new Animation(_leverRotationProgress, Math.abs(0.5f - _leverRotationProgress.value()), 1.0f);
    }

    public void moveDown() {
        _isDown = true;
        new Animation(_leverRotationProgress, Math.abs(0.5f - _leverRotationProgress.value()), 0.0f);
    }
}
