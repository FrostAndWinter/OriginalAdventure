package swen.adventure.game.scenenodes;

import swen.adventure.engine.Action;
import swen.adventure.engine.Game;
import swen.adventure.engine.animation.AnimableProperty;
import swen.adventure.engine.animation.Animation;
import swen.adventure.engine.animation.AnimationCurve;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.SceneNode;
import swen.adventure.engine.scenegraph.TransformNode;
import swen.adventure.game.Interaction;
import swen.adventure.game.InteractionType;

import java.util.*;

/**
 * A door that can be opened and closed. Amazing.
 *
 * It can also require a key to be opened, at this stage there is no link between doors and keys. This can be setup
 * with event connections though by allowing a player open the door when they have picked up a key.
 *
 * Joseph Bennett, 300319773
 */
public class Door extends AdventureGameObject {
    private static final float DoorAnimationDuration = 1.2f;

    private boolean _isOpen = false;
    private boolean _requiresKey = false;

    /**
     * Whether or not a player can directly interact with this door
     */
    private boolean _canDirectlyInteractWith = true;

    private AnimableProperty _doorOpenPercentage = new AnimableProperty(0.f);

    private Set<Player> _playersThatCanOpenDoor = new HashSet<>();

    // DO NOT REMOVE. This action is unused within the Java code base but is still used in the event connections.
    public static final Action<Item, Player, Door> actionAllowPlayerToOpenDoor = (item, player, door, data) -> {
        door._playersThatCanOpenDoor.add(player);
    };

    // DO NOT REMOVE. This action is unused within the Java code base but is still used in the event connections.
    public static final Action<Item, Player, Door> actionDisallowPlayerFromOpeningDoor = (item, player, door, data) -> {
        door._playersThatCanOpenDoor.remove(player);
    };

    // DO NOT REMOVE. This action is unused within the Java code base but is still used in the event connections.
    public static final Action<GameObject, Player, Door> actionOpenDoor = (item, player, door, data) -> {
        door.open();
    };

    // DO NOT REMOVE. This action is unused within the Java code base but is still used in the event connections.
    public static final Action<GameObject, Player, Door> actionCloseDoor = (item, player, door, data) -> {
        door.close();
    };

    public Door(String id, TransformNode parent) {
        super(id, parent, "door");

        final String frameId = id + "DoorFrame";
        final String bodyTransformId = id + "DoorBody";
        final String meshId = id + "DoorMesh";

        MeshNode doorFrame = parent.findNodeWithIdOrCreate(frameId, () -> new MeshNode(frameId, "MedievalModels", "DoorSetting.obj", parent));
        doorFrame.setCollidable(false);

        TransformNode body = parent.findNodeWithIdOrCreate(bodyTransformId, () -> new TransformNode(bodyTransformId, parent, true, Vector3.zero, new Quaternion(), Vector3.one));
        MeshNode doorMesh = parent.findNodeWithIdOrCreate(meshId, () -> new MeshNode(meshId, "MedievalModels", "Door.obj", body));
        doorMesh.setCollidable(true);
        this.registerMeshForInteraction(doorMesh);
        
        _doorOpenPercentage.eventValueChanged.addAction(this, (eventObject, triggeringObject, listener, data) ->  {
            body.setTranslation(new Vector3(-_doorOpenPercentage.value() * doorMesh.boundingBox().width(), 0.f, 0.f));
        });
    }

    /**
     * Sets the door to open and starts the animation
     */
    public void open() {
        _isOpen = true;
        new Animation(_doorOpenPercentage, AnimationCurve.Sine, DoorAnimationDuration, 0.9f);
    }

    /**
     * Sets the door to be closed and starts the animation
     */
    public void close() {
        _isOpen = false;
        new Animation(_doorOpenPercentage, AnimationCurve.Sine, DoorAnimationDuration, 0.0f);
    }


    @Override
    public List<Interaction> possibleInteractions(final MeshNode meshNode, final Player player) {

        // a player can interact with the door if it doesn't require a key or that player has permission to open it.
        boolean canInteract = _canDirectlyInteractWith && (!_requiresKey || _playersThatCanOpenDoor.contains(player));

        if (canInteract) {
            InteractionType interactionType;

            if (_isOpen) {
                interactionType = InteractionType.Close;
            } else {
                interactionType = InteractionType.Open;
            }

            return Collections.singletonList(new Interaction(interactionType, this, meshNode));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void performInteraction(final Interaction interaction, final MeshNode meshNode, final Player player) {
        switch (interaction.interactionType) {
            case Open:
                this.open();
                break;
            case Close:
                this.close();
                break;
        }
    }

    /**
     * Set whether this door is open or closed.
     *
     * @param isOpen true to set the door as open, false to set door as closed
     */
    public void setIsOpen(boolean isOpen) {
        _isOpen = isOpen;
    }

    /**
     * Whether or not a player can directly interact with this door. i.e can interact by using a primary
     * or secondary interacition
     *
     * @param canDirectlyInteractWith whether or not a player can directly interact with this door.
     */
    public void setCanDirectlyInteractWith(boolean canDirectlyInteractWith) {
        this._canDirectlyInteractWith = canDirectlyInteractWith;
    }

    /**
     * Set whether or not this door needs a key to be opened
     *
     * @param requiresKey true if the door requires a key to be opened, false otherwise.
     */
    public void setRequiresKey(boolean requiresKey) {
        _requiresKey = requiresKey;
    }
}
