package swen.adventure.engine.scenegraph;

import javafx.scene.Scene;
import swen.adventure.engine.Action;
import swen.adventure.engine.Event;
import swen.adventure.engine.datastorage.BundleObject;
import swen.adventure.engine.datastorage.BundleSerializable;
import swen.adventure.engine.rendering.maths.Matrix4;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 *
 * SceneNode is the root class for any object that exists within the game world.
 * When chained together, SceneNodes implement a recursive tree structure called a scene graph.
 * The use of this structure has correlations with the Composite design pattern – any scene node can be treated as a singular node,
 * as it encapsulates the behaviour of its children.
 *
 */
public abstract class SceneNode implements BundleSerializable {

    public interface NodeTraversalFunction {
        void visit(SceneNode node);
    }

    private Optional<TransformNode> _parent = Optional.empty();
    protected Set<SceneNode> _childNodes = new HashSet<>();

    /** Dynamic nodes are any nodes whose transforms may change during the execution of the game. */
    private boolean _isDynamic = false;

    private boolean _isEnabled = true;

    public final String id;
    protected Map<String, SceneNode> _idsToNodesMap;
    protected Map<Class<? extends SceneNode>, List<? extends SceneNode>> _nodesOfTypeMap;

    public static Action<SceneNode, SceneNode, SceneNode> actionSetEnabled = (ignored, ignored1, sceneNode, data) -> {
        sceneNode.setEnabled(true);
    };

    public Action<SceneNode, SceneNode, SceneNode> actionSetDisabled = (ignored, ignored1, sceneNode, data) -> {
        sceneNode.setEnabled(false);
    };

    @Override
    public BundleObject toBundle() {
        BundleObject out = new BundleObject();

        out.put("id", id);

        if(_parent.isPresent()) {
            out.put("parentId", _parent.get().id);
            out.put("isDynamic", _isDynamic);
        }

        return out;
    }

    /**
     * Construct a new root SceneNode.
     */
    public SceneNode(String id) {
        _idsToNodesMap = new HashMap<>();
        _nodesOfTypeMap = new HashMap<>();

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
        _nodesOfTypeMap = parent._nodesOfTypeMap;

        this.id = id;
        _idsToNodesMap.put(id, this);

        _isDynamic = isDynamic || parent.isDynamic(); //a node is considered dynamic if it or any of its parents can have a changing transform.

        this.addNodeWithTypeToMap(this.getClass(), this);
    }


    private <T extends SceneNode> void addNodeWithTypeToMap(Class<T> type, SceneNode node) {
        List<T> list = this.allNodesOfType(type);
        list.add((T)node);
    }

    /**
     * @return This node's parent, if it is not a root node.
     */
    public Optional<TransformNode> parent() {
        return _parent;
    }

    /**
     * @return All SceneNodes that sit alongside this node in the scene graph.
     */
    public Set<SceneNode> siblings() {
        if (_parent.isPresent()) {
            Set<SceneNode> children = _parent.get()._childNodes;
            Set<SceneNode> siblings = new HashSet<>(children);
            siblings.remove(this);
            return siblings;
        }
        return Collections.emptySet();
    }

    /**
     * @return whether the node is dynamic i.e. whether any of its properties can change; for instance, this must be true if a node can be moved or reparented.
     */
    public boolean isDynamic() {
        return _isDynamic;
    }

    /**
     * Called on every SceneNode when their transformation matrices change.
     */
    public void transformDidChange() {}

    /**
     * Given an event name in UpperCamelCase, finds and returns the event instance associated with that name on this object.
     * The field is expected to be named in the form event{eventName}.
     * @param eventName The name of the event e.g. DoorOpened.
     * @return The event for that name on this object.
     * @throws RuntimeException if the event does not exist on this object.
     */
    @SuppressWarnings("unchecked")
    public Event<? extends GameObject, ?> eventWithName(String eventName) {
        try {
            Field field = this.getClass().getField("event" + eventName);
            return (Event<? extends GameObject, ?>) field.get(this);
        } catch (IllegalAccessException e) {
            System.err.println("Error accessing event with name " + eventName + ": " + e);
        } catch (NoSuchFieldException ignored) {
        }

        throw new RuntimeException("Could not find an event of name " + eventName + " on " + this);
    }

    /**
     * @param nodeID The id of the node to fetch.
     * @return The node in this node's graph with nodeID as its id, or Optional.empty if it can't be found.
     */
    public Optional<SceneNode> nodeWithID(String nodeID) {
        return Optional.ofNullable(_idsToNodesMap.get(nodeID));
    }

    /**
     * Either fetches a node with a particular id, or creates it if it doesn't already exist.
     * @param nodeID The id of the node to fetch.
     * @return The node in this node's graph with nodeID as its id, or Optional.empty if it can't be found.
     */
    public <T extends SceneNode> T findNodeWithIdOrCreate(String nodeID, Supplier<T> supplier) {
        T node = (T)_idsToNodesMap.get(nodeID);

        return (node != null) ? node : supplier.get();
    }

    /**
     * @return All of the lights in the scene.
     */
    public <T extends SceneNode> List<T> allNodesOfType(Class<T> type) {
        List<T> nodes = (List<T>)_nodesOfTypeMap.get(type);
        if (nodes == null) {
            nodes = new ArrayList<>();
            _nodesOfTypeMap.put(type, nodes);
        }
        return nodes;
    }

    /**
     * Recursively applies a function to this node and then its children.
     * @param traversalFunction The function to apply to each node.
     */
    public void traverse(NodeTraversalFunction traversalFunction) {
        traversalFunction.visit(this);
        for (SceneNode child : _childNodes) {
            child.traverse(traversalFunction);
        }
    }

    /**
     * Calculates if necessary and returns a matrix that converts from local space to world space (the space of the root node).
     * @return The world space transform of the nearest transform node.
     */
    public Matrix4 nodeToWorldSpaceTransform() {
        return _parent.isPresent() ? _parent.get().nodeToWorldSpaceTransform() : new Matrix4();
    }

    /**
     * Calculates if necessary and returns a matrix that converts from world space (the space of the root node) to local space.
     * @return The world space transform of the nearest transform node.
     */
    public Matrix4 worldToNodeSpaceTransform() {
        return _parent.isPresent() ? _parent.get().worldToNodeSpaceTransform() : new Matrix4();
    }


    /**
     * Change this node's parent to the given parent.
     *
     * @param newParent the new parent
     */
    public void setParent(TransformNode newParent) {
        if (_parent.isPresent()) {
            if (_parent.get() == newParent) {
                return;
            } else if (!this.isDynamic()) {
                throw new RuntimeException("Static node with id " + this.id + " cannot be re-parented.");
            }
            _parent.get()._childNodes.remove(this);
        }

        if (newParent != null) {
            newParent._childNodes.add(this);
        }

        _parent = Optional.ofNullable(newParent);
    }

    public Set<SceneNode> children() {
        return Collections.unmodifiableSet(_childNodes);
    }

    public boolean isEnabled() {
        return _isEnabled;
    }

    /** Recursively sets isEnabled on this node's children and itself. */
    public void setEnabled(boolean isEnabled) {
        this.children().forEach(node -> node.setEnabled(isEnabled));
        _isEnabled = isEnabled;
    }

}
