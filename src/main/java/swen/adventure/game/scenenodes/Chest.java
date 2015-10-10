package swen.adventure.game.scenenodes;

import swen.adventure.engine.animation.AnimableProperty;
import swen.adventure.engine.animation.Animation;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.TransformNode;
import swen.adventure.game.Interaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 4/10/15.
 */
public class Chest extends AdventureGameObject {

    private boolean _isOpen = false;
    private TransformNode _hingeTransform;

    private static final float ClosedAngle = (float)(Math.PI / 180.f * 68.f);
    private static final float AnimationDuration = 0.6f;

    private AnimableProperty _lidRotationProgress = new AnimableProperty(ClosedAngle);

    public Chest(String id, TransformNode parent) {
        super(id, parent);

        final String chestMeshId = id + "ChestMesh";

        MeshNode chestMesh = parent.findNodeWithIdOrCreate(chestMeshId, () -> new MeshNode(chestMeshId, "Chest", "Chest.obj", parent));

        this.registerMeshForInteraction(chestMesh);

        chestMesh.setCollidable(true);
        chestMesh.setParent(parent);

        Vector3 hingeOffset = new Vector3(0.f, chestMesh.boundingBox().height() - 0.05f, 0.f);

        final String hingeId = id + "ChestHinge";

        _hingeTransform = parent.findNodeWithIdOrCreate(hingeId, () -> new TransformNode(hingeId, parent, true, hingeOffset, Quaternion.makeWithAngleAndAxis(ClosedAngle, 1, 0, 0), Vector3.one));

        final String lidTransformId = id + "ChestLidT";

        TransformNode lidTransform = parent.findNodeWithIdOrCreate(lidTransformId, () -> new TransformNode(lidTransformId, _hingeTransform, true, hingeOffset.negate(), new Quaternion(), Vector3.one));

        final String lidMeshId = id + "LidMesh";

        MeshNode lidMesh = parent.findNodeWithIdOrCreate(lidMeshId, () -> new MeshNode(id + "ChestLid", "Chest", "ChestLid.obj", lidTransform));
        this.registerMeshForInteraction(lidMesh);

        _lidRotationProgress.eventValueChanged.addAction(this, (eventObject, triggeringObject, listener, data) ->  {
            listener._hingeTransform.setRotation(Quaternion.makeWithAngleAndAxis(eventObject.value() * (ClosedAngle), 1, 0, 0));
        });

        this.close(false);
    }

    public void open(boolean animate) {
        _isOpen = true;
        if (animate) {
            new Animation(_lidRotationProgress, AnimationDuration * Math.abs(0.5f - _lidRotationProgress.value()), 0.0f);
        } else {
            _lidRotationProgress.stopAnimating();
            _lidRotationProgress.setValue(0.f);
        }
    }

    public void close(boolean animate) {
        _isOpen = false;
        if (animate) {
            new Animation(_lidRotationProgress, AnimationDuration * Math.abs(0.5f - _lidRotationProgress.value()), 1.0f);
        } else {
            _lidRotationProgress.stopAnimating();
            _lidRotationProgress.setValue(1.f);
        }
    }

    public boolean isOpen() {
        return _isOpen;
    }

    @Override
    public void setContainer(Container container) {
        super.setContainer(container);
        if (container != null) {
            //Set up the interaction connections between this object's mesh being interacted with and the container.
        }
    }

    @Override
    public List<Interaction> possibleInteractions(MeshNode meshNode, Player player) {
        List<Interaction> possibleInteractions = new ArrayList<>();
        possibleInteractions.add(new Interaction(this.isOpen() ? Interaction.InteractionType.Close : Interaction.InteractionType.Open, this, meshNode));

        container().ifPresent(container -> {
            // if the container isn't full, allow placing items in
            if (!container.isFull()) {
                possibleInteractions.add(new Interaction(Interaction.InteractionType.PlaceIn, this, meshNode));
            }
        });

        return possibleInteractions;
    }

    @Override
    public void performInteraction(Interaction interaction, MeshNode meshNode, Player player) {
        super.performInteraction(interaction, meshNode, player);
        switch (interaction.interactionType) {
            case Open:
                this.open(true);
                break;
            case Close:
                this.close(true);
                break;
            default:
                break;
        }
    }
}
