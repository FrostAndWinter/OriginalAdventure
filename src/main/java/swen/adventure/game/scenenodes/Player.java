package swen.adventure.game.scenenodes;

import swen.adventure.engine.Action;
import swen.adventure.engine.KeyInput;
import swen.adventure.engine.scenegraph.CameraNode;
import swen.adventure.engine.scenegraph.CollisionNode;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.TransformNode;
import swen.adventure.engine.Event;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.game.EventDataKeys;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by josephbennett on 19/09/15
 */
public class Player extends AdventureGameObject {

    private float _playerSpeed = 600.f; //units per second

    private Optional<CameraNode> _camera = Optional.empty();

    public static final Action<KeyInput, KeyInput, Player> actionMoveInDirection =
            (eventObject, triggeringObject, player, data) -> {
                Vector3 direction = (Vector3)data.get(EventDataKeys.Direction);
                long elapsedMillis = (Long)data.get(EventDataKeys.ElapsedMillis);
                player.move(direction.multiplyScalar(player._playerSpeed * elapsedMillis / 1000.f));
            };

    public static final Action<Player, Player, Player> actionMoveToLocation =
            (player, triggeringPlayer, ignored, data) -> {
                Vector3 location = (Vector3) data.get(EventDataKeys.Location);
                player.move(location);
            };

    public final Event<Player, Player> eventPlayerMoved = new Event<>("PlayerMoved", this);

    public Player(String id, TransformNode parent) {
        super(id, parent, id);

        this.setContainer(new Inventory(this));
    }

    public void setCamera(CameraNode camera) {
        _camera = Optional.of(camera);
    }

    private void move(Vector3 vector) {
        TransformNode transformNode = this.parent().get();
        Vector3 translation = transformNode.rotation().rotateVector3(vector);
        Vector3 lateralTranslation = new Vector3(translation.x, 0, translation.z).normalise().multiplyScalar(vector.length());

        boolean successfullyMoved = this.attemptMoveDirect(new Vector3(lateralTranslation.x, 0.f, 0.f));
        successfullyMoved |= this.attemptMoveDirect(new Vector3(0.f, 0.f, lateralTranslation.z));

        if (successfullyMoved) {
            eventPlayerMoved.trigger(this, Collections.singletonMap(EventDataKeys.Location, transformNode.translation()));
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

    public Inventory inventory() {
        return (Inventory)this.container().get();
    }

    public Optional<CameraNode> camera() {
        return _camera;
    }
}
