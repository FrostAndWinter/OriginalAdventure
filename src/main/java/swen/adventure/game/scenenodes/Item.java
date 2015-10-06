package swen.adventure.game.scenenodes;

import swen.adventure.engine.Event;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.Collections;

/**
 * Created by josephbennett on 6/10/15
 */
public class Item extends GameObject {

    public final Event<Item, Player> eventItemPickup = new Event<>("eventItemPickup", this);

    private boolean _interactionEnabled = true;

    public Item(String id, TransformNode parent) {
        super(id, parent);
    }

    @Override
    public void setMesh(MeshNode mesh) {
        super.setMesh(mesh);
        mesh.eventMeshClicked.addAction(this, (eventObject, player, listener, data) -> {
            if (_interactionEnabled) {
                eventItemPickup.trigger(player, Collections.emptyMap());
            }
        });
    }

    public void setInteractionEnabled(boolean interactionEnabled) {
        _interactionEnabled = interactionEnabled;
    }
}
