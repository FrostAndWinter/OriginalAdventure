package swen.adventure.game.scenenodes;

import swen.adventure.engine.Action;
import swen.adventure.engine.animation.AnimableProperty;
import swen.adventure.engine.animation.Animation;
import swen.adventure.engine.animation.AnimationCurve;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.SceneNode;
import swen.adventure.engine.scenegraph.TransformNode;
import swen.adventure.game.Interaction;

import java.util.*;


public class Door extends AdventureGameObject {

    private boolean _isOpen = false;

    private static final float DoorAnimationDuration = 1.2f;

    private AnimableProperty _doorOpenPercentage = new AnimableProperty(0.f);

    private Set<Player> _playersThatCanOpenDoor = new HashSet<>();

    public static final Action<Item, Player, Door> actionAllowPlayerToOpenDoor = (item, player, door, data) -> {
        door._playersThatCanOpenDoor.add(player);
    };

    public static final Action<Item, Player, Door> actionDisallowPlayerFromOpeningDoor = (item, player, door, data) -> {
        door._playersThatCanOpenDoor.remove(player);
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

    public void toggle() {

        if (_isOpen) {
            close();
        } else {
            open();
        }
    }

    public void open() {
        _isOpen = true;
        new Animation(_doorOpenPercentage, AnimationCurve.Sine, DoorAnimationDuration, 0.9f);
    }

    public void close() {
        _isOpen = false;
        new Animation(_doorOpenPercentage, AnimationCurve.Sine, DoorAnimationDuration, 0.0f);
    }

    @Override
    public List<Interaction> possibleInteractions(final MeshNode meshNode, final Player player) {
        if (!_isOpen && _playersThatCanOpenDoor.contains(player)) {
            return Collections.singletonList(new Interaction(Interaction.InteractionType.Open, this, meshNode));
        }
        return Collections.emptyList();
    }

    @Override
    public void performInteraction(final Interaction interaction, final MeshNode meshNode, final Player player) {
        switch (interaction.interactionType) {
            case Open:
                this.open();
                break;
        }
    }
}
