package swen.adventure.scenegraph;

import swen.adventure.rendering.maths.Matrix4;
import swen.adventure.utils.BoundingBox;

import java.util.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 */
public abstract class SceneNode {

    public interface NodeTraversalFunction {
        void visit(SceneNode node);
    }

    private Optional<TransformNode> _parent = Optional.empty();
    protected Set<SceneNode> _childNodes = new HashSet<>();

    /** Dynamic nodes are any nodes whose transforms may change during the execution of the game. */
    private boolean _isDynamic = false;

    public final String id;
    protected Map<String, SceneNode> _idsToNodesMap;
    protected Set<Light> _allLights; //FIXME maybe this should be in a separate SceneGraph wrapper class?

    private Optional<BoundingBox> _worldSpaceBoundingBox = Optional.empty();
    private boolean _needsRecalculateWorldSpaceBoundingBox = true;

    /**
     * Construct a new root SceneNode.
     */
    public SceneNode(String id) {
        _idsToNodesMap = new HashMap<>();
        _allLights = new HashSet<>();

        this.id = id;
        _idsToNodesMap.put(id, this);

        //The root node must be static.
    }

    /** Construct a SceneNode that is a child of parent. */
    public SceneNode(String id, TransformNode parent, boolean isDynamic) {

        //Add this node as a child of parent.
        parent._childNodes.add(this);
        _parent = Optional.of(parent);

        //Get a reference to the id-node dictionary, and pass along a reference to the lights set.
        _idsToNodesMap = parent._idsToNodesMap;
        _allLights = parent._allLights;

        this.id = id;
        _idsToNodesMap.put(id, this);

        _isDynamic = isDynamic || parent.isDynamic(); //a node is considered dynamic if it or any of its parents can have a changing transform.

        if (this instanceof Light) {
            _allLights.add((Light)this);
        }
    }

    public Optional<TransformNode> parent() {
        return _parent;
    }

    public boolean isDynamic() {
        return _isDynamic;
    }

    /**
     * Called on every SceneNode when their transformation matrices change.
     */
    public void transformDidChange() {
        _needsRecalculateWorldSpaceBoundingBox = true;
    }

    /**
     * Finds a bounding box by searching through its children.
     * Prioritises GameObjects, and then MeshNodes.
     * If this node has multiple GameObject or MeshNode children, then it uses the bounding box of the first one it finds.
     * @return The bounding box of this object, if present.
     */
    public Optional<BoundingBox> boundingBox() {
        Optional<BoundingBox> boundingBox = Optional.empty();

        for (SceneNode node : _childNodes) {
            if (node instanceof GameObject) {
                boundingBox = node.boundingBox();
                if (boundingBox.isPresent()) {
                    return boundingBox;
                }
            } else if (!boundingBox.isPresent()) {
                boundingBox = node.boundingBox();
            }
        }
        return boundingBox;
    }

    public Optional<BoundingBox> worldSpaceBoundingBox() {
        if (_needsRecalculateWorldSpaceBoundingBox) {
            _worldSpaceBoundingBox = this.boundingBox()
                    .map(boundingBox -> boundingBox.axisAlignedBoundingBoxInSpace(this.nodeToWorldSpaceTransform()));
            _needsRecalculateWorldSpaceBoundingBox = false;
        }
        return _worldSpaceBoundingBox;
    }

    /**
     * @param nodeID The id of the node to fetch.
     * @return The node in this node's graph with nodeID as its id, or Optional.empty if it can't be found.
     */
    public Optional<SceneNode> nodeWithID(String nodeID) {
        return Optional.ofNullable(_idsToNodesMap.get(nodeID));
    }

    /**
     * @return All of the lights in the scene.
     */
    public Set<Light> allLights() {
        return _allLights;
    }

    /**
     * Recursively applies a function to every node and then its children.
     * @param traversalFunction The function to apply to each node.
     */
    public void traverse(NodeTraversalFunction traversalFunction) {
        traversalFunction.visit(this);
        for (SceneNode child : _childNodes) {
            child.traverse(traversalFunction);
        }
    }

    /**
     * Calculates if necessary and returns the world space transform of the nearest transform node, looking towards the root.
     * @return The world space transform of the nearest transform node.
     */
    public Matrix4 nodeToWorldSpaceTransform() {
        return _parent.isPresent() ? _parent.get().nodeToWorldSpaceTransform() : new Matrix4();
    }

    /**
     * Calculates if necessary and returns the world space transform of the nearest transform node, looking towards the root.
     * @return The world space transform of the nearest transform node.
     */
    public Matrix4 worldToNodeSpaceTransform() {
        return _parent.isPresent() ? _parent.get().worldToNodeSpaceTransform() : new Matrix4();
    }
}
