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
import swen.adventure.game.Interaction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 4/10/15.
 */
public class Lever extends AdventureGameObject {

    public final Event<SceneNode, Player> eventLeverToggled = new Event<>("eventLeverToggled", this);

    private TransformNode _hingeTransform;

    private boolean _isDown = true;

    private AnimableProperty _leverRotationProgress = new AnimableProperty(0);

    public Lever(String id, TransformNode parent) {
        super(id, parent);

        final String leverBaseMeshId = id + "LeverBase";
        final String leverHingeId = id + "LeverHinge";
        final String leverMeshId = id + "Lever";

        MeshNode leverBaseMesh = parent.findNodeWithIdOrCreate(leverBaseMeshId, () -> new MeshNode(leverBaseMeshId, "Lever", "LeverBase.obj", parent));
        this.registerMeshForInteraction(leverBaseMesh);

        _hingeTransform = parent.findNodeWithIdOrCreate(leverHingeId, () -> new TransformNode(leverHingeId, parent, true, Vector3.zero, new Quaternion(), Vector3.one));

        MeshNode leverMesh = parent.findNodeWithIdOrCreate(leverMeshId, () -> new MeshNode(id + "Lever", "Lever", "Lever.obj", _hingeTransform));
        this.registerMeshForInteraction(leverMesh);

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

    @Override
    public List<Interaction> possibleInteractions(final MeshNode meshNode, final Player player) {
        return Collections.singletonList(new Interaction(Interaction.InteractionType.Pull, this, meshNode));
    }

    @Override
    public void performInteraction(final Interaction interaction, final MeshNode meshNode, final Player player) {
        switch (interaction.interactionType) {
            case Pull:
                this.toggle(player);
                break;
        }
    }
}
