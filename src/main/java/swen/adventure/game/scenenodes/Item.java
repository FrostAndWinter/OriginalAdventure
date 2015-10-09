package swen.adventure.game.scenenodes;

import swen.adventure.engine.Event;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.Collections;

/**
 * Created by josephbennett on 6/10/15
 * Modified By Daniel Braithwate ID: 300313770
 */
public class Item extends AdventureGameObject {

    public final Event<Item, Player> eventItemPickup = new Event<>("eventItemPickup", this);

    private boolean _interactionEnabled = true;

    private String _description;

    /** An Item's parent transform must directly bring the item into world space (including centering the mesh at the origin); any extra translations must be done in a separate transform. */
    public Item(String id, TransformNode parent, String description) {
        super(id, parent);
        _description = description;
    }

    @Override
    public void setMainMesh(MeshNode mesh) {
        super.setMainMesh(mesh);
        mesh.eventMeshPressed.addAction(this, (eventObject, player, listener, data) -> {
            if (_interactionEnabled) {
                eventItemPickup.trigger(player, Collections.emptyMap());
            }
        });
    }

    public void setInteractionEnabled(boolean interactionEnabled) {
        _interactionEnabled = interactionEnabled;
    }

    public String getDescription() {
        return _description;
    }

    @Override
    public void setContainer(Container container) {
        super.setContainer(container);
        if (container != null) {
            container.push(this);
        }
    }
}
