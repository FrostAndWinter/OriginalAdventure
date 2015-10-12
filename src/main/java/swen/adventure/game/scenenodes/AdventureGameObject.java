package swen.adventure.game.scenenodes;

import swen.adventure.engine.Action;
import swen.adventure.engine.Event;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.SceneNode;
import swen.adventure.engine.scenegraph.TransformNode;
import swen.adventure.game.EventDataKeys;
import swen.adventure.game.Interaction;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 9/10/15.
 *
 * An AdventureGameObject is a specialisation of GameObject allowing for game-specific components.
 */
public class AdventureGameObject extends GameObject {

    public static final Action<SceneNode, Player, AdventureGameObject> actionEnableInteractions = (sceneNode, player, gameObject, data) -> {
        MeshNode mesh = (MeshNode)data.get(EventDataKeys.Mesh);
        for (Interaction interaction : gameObject.possibleInteractions(mesh, player)) {
            gameObject.eventShouldProvideInteraction.trigger(player, Collections.singletonMap(EventDataKeys.Interaction, interaction));
        }
    };

    public final Event<AdventureGameObject, Player> eventShouldProvideInteraction = new Event<>("ShouldProvideInteraction", this);
    public final Event<AdventureGameObject, Player> eventInteractionEnded = new Event<>("InteractionEnded", this);

    public final String name;
    private Optional<Container> _container = Optional.empty();

    public AdventureGameObject(final String id, final TransformNode parent, final String name) {
        super(id, parent);
        this.name = name;
    }

    /**
     * @return The container associated with this object, if it exists.
     *         This is used to either link a game object as being a container, or, in the case of an item, specify the container the item is in.
     */
    public Optional<Container> container() {
        return _container;
    }

    protected void registerMeshForInteraction(MeshNode mesh) {
        mesh.eventMeshLookedAt.addAction(this, actionEnableInteractions);
    }

    /**
     * Sets this object's container to be the specified container.
     * In the case of GameObjects that are represent container meshes, this should be overridden to
     * set up the connections required for the container to be modified when the object is interacted with.
     * In the case of items, this should call pushItem on the container with the item as an argument.
     * @param container
     */
    public void setContainer(Container container) {
        _container = Optional.ofNullable(container);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    public List<Interaction> possibleInteractions(MeshNode meshNode, Player player) {
        return Collections.emptyList();
    }

    public void performInteraction(Interaction interaction, MeshNode meshNode, Player player) {}
}
