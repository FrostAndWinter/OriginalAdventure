package swen.adventure.scenegraph;

import swen.adventure.rendering.maths.Matrix4;

import java.util.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 */
public abstract class SceneNode {

    public interface NodeTraversalFunction {
        void visit(SceneNode node);
    }

    private Optional<SceneNode> _parent = Optional.empty();
    private Set<SceneNode> _childNodes = new HashSet<>();

    /** Dynamic nodes are any nodes whose transforms may change during the execution of the game. */
    private boolean _isDynamic = false;

    public final String id;
    private Map<String, SceneNode> _idsToNodesMap;

    /**
     * Construct a new root SceneNode.
     */
    public SceneNode(String id) {
        _idsToNodesMap = new HashMap<>();

        this.id = id;
        _idsToNodesMap.put(id, this);

        //The root node must be static.
    }

    /** Construct a SceneNode that is a child of parent. */
    public SceneNode(String id, SceneNode parent, boolean isDynamic) {

        //Add this node as a child of parent.
        parent._childNodes.add(this);
        _parent = Optional.of(parent);

        //Get a reference to the id-node dictionary.
        _idsToNodesMap = parent._idsToNodesMap;

        this.id = id;
        _idsToNodesMap.put(id, this);

        _isDynamic = isDynamic || parent.isDynamic(); //a node is considered dynamic if it or any of its parents can have a changing transform.
    }

    public Optional<SceneNode> parent() {
        return _parent;
    }

    public boolean isDynamic() {
        return _isDynamic;
    }

    public Optional<SceneNode> nodeWithID(String nodeID) {
        return Optional.ofNullable(_idsToNodesMap.get(nodeID));
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
