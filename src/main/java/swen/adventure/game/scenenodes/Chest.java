package swen.adventure.game.scenenodes;

import swen.adventure.engine.Action;
import swen.adventure.engine.animation.AnimableProperty;
import swen.adventure.engine.animation.Animation;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.SceneNode;
import swen.adventure.engine.scenegraph.TransformNode;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 4/10/15.
 */
public class Chest extends GameObject {

    private boolean _isOpen = false;
    private TransformNode _hingeTransform;

    private static final float ClosedAngle = (float)(Math.PI / 180.f * 68.f);
    private static final float AnimationDuration = 0.6f;

    public final static Action<SceneNode, Player, Chest> actionToggleChest =
            (eventObject, player, chest, data) -> chest.toggle();

    private AnimableProperty _lidRotationProgress = new AnimableProperty(0);

    public Chest(String id, TransformNode parent) {
        super(id, parent);

        MeshNode chestMesh = new MeshNode(id + "ChestMesh", "Chest", "Chest.obj", parent);
        chestMesh.setCollidable(true);

        Vector3 hingeOffset = new Vector3(0.f, chestMesh.boundingBox().height() - 0.05f, 0.f);
        _hingeTransform = new TransformNode(id + "ChestHinge", parent, true, hingeOffset, new Quaternion(), Vector3.one);

        TransformNode lidTransform = new TransformNode(id + "ChestLid", _hingeTransform, true, hingeOffset.negate(), new Quaternion(), Vector3.one);
        MeshNode lidMesh = new MeshNode(id + "ChestLid", "Chest", "ChestLid.obj", lidTransform);

        chestMesh.eventMeshPressed.addAction(this, actionToggleChest);
        lidMesh.eventMeshPressed.addAction(this, actionToggleChest);

//        _hingeTransform.translateBy(new Vector3(-doorMesh.boundingBox().width() * 50 / 2, 0.f, 0.f));
//        body.translateBy(new Vector3(doorMesh.boundingBox().width()*50/2, 0.f, 0.f));

        _lidRotationProgress.eventValueChanged.addAction(this, (eventObject, triggeringObject, listener, data) ->  {
            listener._hingeTransform.setRotation(Quaternion.makeWithAngleAndAxis((float) (eventObject.value() * (ClosedAngle)), 1, 0, 0));
        });

        this.close();
    }

    public void toggle() {

        if (_isOpen) {
            this.close();
        } else {
            this.open();
        }
    }

    public void open() {
        _isOpen = true;
        new Animation(_lidRotationProgress, AnimationDuration * Math.abs(0.5f - _lidRotationProgress.value()), 1.0f);
    }

    public void close() {
        _isOpen = false;
        new Animation(_lidRotationProgress, AnimationDuration * Math.abs(0.5f - _lidRotationProgress.value()), 0.0f);
    }

    public boolean isOpen() {
        return _isOpen;
    }

    public void setOpen(boolean isOpen) {
        _isOpen = isOpen;
    }
}
