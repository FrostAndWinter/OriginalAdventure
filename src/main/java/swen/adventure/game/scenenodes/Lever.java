/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Liam O'Niell (oneilliam) (300312734) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* David Barnett (barnetdavi) (300313764) */ 
 /* Daniel Braithwaite (braithdani) (300313770) */ 
 package swen.adventure.game.scenenodes;

import swen.adventure.engine.Action;
import swen.adventure.engine.Event;
import swen.adventure.engine.animation.AnimableProperty;
import swen.adventure.engine.animation.Animation;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.SceneNode;
import swen.adventure.engine.scenegraph.TransformNode;
import swen.adventure.game.Interaction;
import swen.adventure.game.InteractionType;

import java.util.Collections;
import java.util.List;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 4/10/15.
 * Modified by Daniel Braithwaite, ID: 300313770
 */
public class Lever extends AdventureGameObject {

    public final Event<SceneNode, Player> eventLeverMovedUp = new Event<>("LeverMovedUp", this);
    public final Event<SceneNode, Player> eventLeverMovedDown = new Event<>("LeverMovedDown", this);

    public static final Action<SceneNode, Player, Lever> actionLeverMoveUp = (sceneNode, player, lever, data) ->  {
        lever.moveUp(player);
    };

    public static final Action<SceneNode, Player, Lever> actionLeverMoveDown = (sceneNode, player, lever, data) ->  {
        lever.moveDown(player);
    };

    private TransformNode _hingeTransform;

    private boolean _isDown = false;

    private AnimableProperty _leverRotationProgress = new AnimableProperty(0);

    public Lever(String id, TransformNode parent) {
        super(id, parent, "lever");

        final String leverBaseMeshId = id + "LeverBase";
        final String leverHingeId = id + "LeverHinge";
        final String leverMeshId = id + "Lever";

        MeshNode leverBaseMesh = parent.findNodeWithIdOrCreate(leverBaseMeshId, () -> new MeshNode(leverBaseMeshId, "Lever", "LeverBase.obj", parent));
        this.registerMeshForInteraction(leverBaseMesh);

        _hingeTransform = parent.findNodeWithIdOrCreate(leverHingeId, () -> new TransformNode(leverHingeId, parent, true, Vector3.zero, new Quaternion(), Vector3.one));

        MeshNode leverMesh = parent.findNodeWithIdOrCreate(leverMeshId, () -> new MeshNode(id + "Lever", "Lever", "Lever.obj", _hingeTransform));
        this.registerMeshForInteraction(leverMesh);

        _leverRotationProgress.eventValueChanged.addAction(this, (eventObject, triggeringObject, listener, data) -> {
            listener._hingeTransform.setRotation(Quaternion.makeWithAngleAndAxis((float) (eventObject.value() * (-Math.PI / 3)), 0, 0, 1));
        });
    }

    /**
     * Changes the state of the lever, if its down then
     * this will set it to up and visa versa
     * @param player player that is toggling it
     */
    public void toggle(Player player) {
        if (_isDown) {
            this.moveUp(player);
        } else {
            this.moveDown(player);
        }
    }

    /**
     * Sets the lever to be up and animates the lever to that position
     * @param player player making the interaction
     */
    public void moveUp(Player player) {
        _isDown = false;
        new Animation(_leverRotationProgress, Math.abs(0.5f - _leverRotationProgress.value()), 0.0f);
        this.eventLeverMovedUp.trigger(player, Collections.emptyMap());
    }

    /**
     * Sets the lever to be down and animates the lever to that position
     * @param player player making the interaction
     */
    public void moveDown(Player player) {
        _isDown = true;
        new Animation(_leverRotationProgress, Math.abs(0.5f - _leverRotationProgress.value()), 1.0f);
        this.eventLeverMovedDown.trigger(player, Collections.emptyMap());
    }

    public boolean isDown() {
        return _isDown;
    }

    public void setIsDown(boolean isDown) {
        _isDown = isDown;
    }

    @Override
    public List<Interaction> possibleInteractions(final MeshNode meshNode, final Player player) {
        return Collections.singletonList(new Interaction(InteractionType.Pull, this, meshNode));
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