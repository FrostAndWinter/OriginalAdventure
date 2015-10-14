package swen.adventure.game.scenenodes;

import javafx.scene.shape.Mesh;
import swen.adventure.engine.Action;
import swen.adventure.engine.KeyInput;
import swen.adventure.engine.rendering.maths.BoundingBox;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.scenegraph.*;
import swen.adventure.engine.Event;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.game.EventDataKeys;
import swen.adventure.game.Interaction;
import swen.adventure.game.InteractionType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by josephbennett on 19/09/15
 * Modified by Thomas Roughton, Student ID 300313924.
 */
public class Player extends AdventureGameObject {

    private static final String PlayerMeshName = "Knight.obj";
    private static final Vector3 PlayerMeshOffset = new Vector3(0, -60, 0);
    private static final float PlayerMeshScale = 4;

    private static final BoundingBox PlayerBoundingBox = new BoundingBox(new Vector3(-30, -60, -10) , new Vector3(30, 60, 10));
    private float _playerSpeed = 600.f; //units per second

    private final CameraNode _camera;
    private final TransformNode _meshRotationTransform;

    private static final Vector3 CameraTranslation = new Vector3(0, 40, 0);

    /**
     * Moves the player in the direction specified by the EventDataKeys.Direction key in the data dictionary.
     */
    public static final Action<KeyInput, KeyInput, Player> actionMoveInDirection =
            (eventObject, triggeringObject, player, data) -> {
                Vector3 direction = (Vector3)data.get(EventDataKeys.Direction);
                long elapsedMillis = (Long)data.get(EventDataKeys.ElapsedMillis);
                player.move(direction.multiplyScalar(player._playerSpeed * elapsedMillis / 1000.f));
            };

    /**
     * Directly sets the location on a player.
     */
    public static final Action<Player, Player, Player> actionMoveToLocation =
            (player, triggeringPlayer, ignored, data) -> {
                Vector3 location = (Vector3) data.get(EventDataKeys.Location);
                player.move(location);
            };

    public final Event<Player, Player> eventPlayerMoved = new Event<>("PlayerMoved", this);
    public final Event<Player, Player> eventPlayerSlotSelected = new Event<>("PlayerSlotSelected", this);


    public Player(String id, TransformNode parent) {
        super(id, parent, id);

        final String inventoryId = id + "Inventory";

        Inventory inventory = parent.findNodeWithIdOrCreate(inventoryId, () -> new Inventory(inventoryId, parent));
        this.setContainer(inventory);

        final String colliderID = id + "Collider";
        CollisionNode collider = parent.findNodeWithIdOrCreate(colliderID, () -> new CollisionNode(colliderID, parent, PlayerBoundingBox, CollisionNode.CollisionFlag.Player));
        this.setCollisionNode(collider);

        String cameraTranslationID = id + "CameraTranslation";

        TransformNode cameraTransform = parent.findNodeWithIdOrCreate(cameraTranslationID, () ->
            new TransformNode(cameraTranslationID, parent, false, CameraTranslation, new Quaternion(), Vector3.one)
        );

        final String cameraID = id + "Camera";
        _camera = parent.findNodeWithIdOrCreate(cameraID, () -> new CameraNode(id + "Camera", cameraTransform));


        final String meshRotationTransformID = id + "MeshRotationTransform";
        _meshRotationTransform = parent.findNodeWithIdOrCreate(meshRotationTransformID, () ->
            new TransformNode(meshRotationTransformID, parent, true, Vector3.zero, new Quaternion(), Vector3.one)
        );

        final String meshTransformID = id + "MeshTransform";

        TransformNode meshTransform = parent.findNodeWithIdOrCreate(meshTransformID, () ->
                new TransformNode(meshTransformID, _meshRotationTransform, false, PlayerMeshOffset, new Quaternion(0, 1, 0, 0), new Vector3(PlayerMeshScale, PlayerMeshScale, PlayerMeshScale))
        );

        final String meshID = id + "Mesh";

        MeshNode mesh = parent.findNodeWithIdOrCreate(meshID, () -> new MeshNode(meshID, null, PlayerMeshName, meshTransform));
        this.registerMeshForInteraction(mesh);
        this.setMesh(mesh);

        this.eventPlayerSlotSelected.addAction(this, (eventObject, triggeringObject, listener, data) ->
            this.inventory().selectSlot((int)data.get(EventDataKeys.Slot))
        );
    }

    private void move(Vector3 vector) {
        TransformNode transformNode = this.parent().get();
        Vector3 translation = transformNode.rotation().rotateVector3(vector);
        Vector3 lateralTranslation = new Vector3(translation.x, 0, translation.z).normalise().multiplyScalar(vector.length());

        boolean successfullyMoved = this.attemptMoveDirect(new Vector3(lateralTranslation.x, 0.f, 0.f));
        successfullyMoved |= this.attemptMoveDirect(new Vector3(0.f, 0.f, lateralTranslation.z));

        if (successfullyMoved) {
            eventPlayerMoved.trigger(this,
                    Collections.singletonMap(EventDataKeys.Location, transformNode.translation()));
        }
    }

    private boolean attemptMoveDirect(Vector3 translation) {
        TransformNode transformNode = this.parent().get();

        Vector3 startingTranslation = transformNode.translation();

        transformNode.translateBy(translation);


        if (!this.collisionNode().isPresent())
            return true;

        CollisionNode selfCollisionNode = collisionNode().get();

        boolean canMove = allNodesOfType(CollisionNode.class).stream()
                .filter(otherCollisionNode -> selfCollisionNode != otherCollisionNode)
                .noneMatch(selfCollisionNode::isCollidingWith);

        if(!canMove)
            transformNode.setTranslation(startingTranslation);

        return canMove;
    }

    /**
     * Sets the player to look in a direction (i.e. sets the rotation)
     * @param angleX The rotation about the x axis.
     * @param angleY The rotation about the y axis.
     */
    public void setLookDirection(float angleX, float angleY) {
        TransformNode rotatedNode = this.parent().get();
        Quaternion rotation = Quaternion.makeWithAngleAndAxis(angleX, 0, -1, 0)
                   .multiply(Quaternion.makeWithAngleAndAxis(angleY, -1, 0, 0));
        rotatedNode.setRotation(rotation);
        _meshRotationTransform.setRotation(rotation.invert().multiply(Quaternion.makeWithAngleAndAxis(angleY, -1, 0, 0)));

        eventPlayerMoved.trigger(this,
                Collections.singletonMap(EventDataKeys.Quaternion, rotatedNode.rotation()));
    }

    public Inventory inventory() {
        return (Inventory)this.container().get();
    }

    public CameraNode camera() {
        return _camera;
    }

    @Override
    public List<Interaction> possibleInteractions(final MeshNode meshNode, final Player otherPlayer) {
        final Interaction[] interaction = {null};
        if (otherPlayer.inventory().selectedItem().isPresent() && !this.inventory().isFull()) {
            return Collections.singletonList(new Interaction(InteractionType.Give, this, meshNode));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void performInteraction(final Interaction interaction, final MeshNode meshNode, final Player player) {
        switch (interaction.interactionType) {
            case Give:
                player.inventory().selectedItem().ifPresent(item -> {
                    item.moveToContainer(this.inventory());
                    item.eventPlayerDroppedItem.trigger(player, Collections.emptyMap());
                });
                break;
        }
    }
}
