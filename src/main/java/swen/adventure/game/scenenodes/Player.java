package swen.adventure.game.scenenodes;

import swen.adventure.engine.Action;
import swen.adventure.engine.KeyInput;
import swen.adventure.engine.scenegraph.CollisionNode;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.TransformNode;
import swen.adventure.game.AdventureGame;
import swen.adventure.engine.Event;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.game.AdventureGameKeyInput;

import java.util.Collections;

/**
 * Created by josephbennett on 19/09/15
 */
public class Player extends GameObject {

    private float _playerSpeed = 3.f;

    private Inventory _inventory = new Inventory(id + "Inventory", parent().get());

    public static final Action<KeyInput, KeyInput, Player> actionPlayerMoveForward =
            (eventObject, triggeringObject, player, data) -> player.move(new Vector3(0, 0, -player._playerSpeed));

    public static final Action<KeyInput, KeyInput, Player> actionPlayerMoveBackward =
            (eventObject, triggeringObject, player, data) -> player.move(new Vector3(0, 0, player._playerSpeed));

    public static final Action<KeyInput, KeyInput, Player> actionPlayerMoveLeft =
            (eventObject, triggeringObject, player, data) -> player.move(new Vector3(-player._playerSpeed, 0, 0));

    public static final Action<KeyInput, KeyInput, Player> actionPlayerMoveRight=
            (eventObject, triggeringObject, player, data) -> player.move(new Vector3(player._playerSpeed, 0, 0));

    public final Event<Player> eventPlayerMoved = new Event<>("eventPlayerMoved", this);

    public Player(String id, TransformNode parent) {
        super(id, parent);
    }

    private void move(Vector3 vector) {
        TransformNode transformNode = this.parent().get();
        Vector3 translation = transformNode.rotation().rotateVector3(vector);
        Vector3 lateralTranslation = new Vector3(translation.x, 0, translation.z).normalise().multiplyScalar(vector.length());

        boolean successfullyMoved = this.attemptMoveDirect(new Vector3(lateralTranslation.x, 0.f, 0.f));
        successfullyMoved |= this.attemptMoveDirect(new Vector3(0.f, 0.f, lateralTranslation.z));

        if (successfullyMoved) {
            eventPlayerMoved.trigger(this, Collections.emptyMap());
        }
    }

    private boolean attemptMoveDirect(Vector3 translation) {
        TransformNode transformNode = this.parent().get();

        Vector3 startingTranslation = transformNode.translation();

        transformNode.translateBy(translation);

        for (CollisionNode otherCollisionNode : _allCollidables) {
            if (otherCollisionNode != this.collisionNode() && this.collisionNode().isCollidingWith(otherCollisionNode)) {
                transformNode.setTranslation(startingTranslation);
                return false;
            }
        }
        return true;
    }

    public Inventory getInventory() {
        return _inventory;
    }


}