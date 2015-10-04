package swen.adventure.game.scenenodes;

import swen.adventure.engine.Event;
import swen.adventure.engine.datastorage.BundleObject;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.Collections;
import java.util.function.Function;

/**
 * Created by danielbraithwt on 10/2/15.
 */
public class Button extends GameObject {

    public final Event<Button> eventButtonPressed = new Event<>("eventButtonPressed", this);

    private MeshNode _mesh = null;

    public Button(String id, TransformNode parent) {
        super(id, parent);

        //TransformNode body = new TransformNode(id + "ButtonBody", parent, true, new Vector3(100, 100, 0), new Quaternion(), new Vector3(40, 40, 40));
        _mesh = new MeshNode(id + "ButtonMesh", "box.obj", parent);

        _mesh.eventMeshClicked.addAction(this, (eventObject, player, listener, data) -> this.eventButtonPressed.trigger(player, Collections.emptyMap()));
    }

    public MeshNode mesh() {
        return _mesh;
    }

    @SuppressWarnings("unused")
    private static Button createNodeFromBundle(BundleObject bundle,
                                                   Function<String, TransformNode> findParentFunction) {
        String id = bundle.getString("id");
        String parentId = bundle.getString("parentId");
        TransformNode parent = findParentFunction.apply(parentId);
        return new Button(id, parent);
    }
}
