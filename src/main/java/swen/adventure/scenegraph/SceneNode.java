package swen.adventure.scenegraph;

import com.jogamp.opengl.math.Matrix4;
import java.util.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 */
abstract class SceneNode {

    public interface NodeTraversalFunction {
        void visit(SceneNode node);
    }

    private Optional<SceneNode> _parent = Optional.empty();
    private Set<SceneNode> _childNodes = new HashSet<>();
    private boolean _isDynamic = false;
    private Optional<String> _id;
    private Map<String, SceneNode> _idsToNodesMap;

    /**
     * Construct a new root SceneNode.
     */
    public SceneNode(Optional<String> id) {
        _idsToNodesMap = new HashMap<>();

        _id = id;
        if (id.isPresent()) {
            _idsToNodesMap.put(id.get(), this);
        }
    }

    /** Construct a SceneNode that is a child of parent. */
    public SceneNode(Optional<String> id, SceneNode parent) {

        //Add this node as a child of parent.
        parent._childNodes.add(this);
        _parent = Optional.of(parent);

        //Get a reference to the id-node dictionary.
        _idsToNodesMap = parent._idsToNodesMap;

        _id = id;
        if (id.isPresent()) {
            _idsToNodesMap.put(id.get(), this);
        }
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
    public Matrix4 worldSpaceTransform() {
        return _parent.isPresent() ? _parent.get().worldSpaceTransform() : new Matrix4();
    }
}
