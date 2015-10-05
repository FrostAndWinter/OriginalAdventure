package swen.adventure.engine.scenegraph;

import swen.adventure.engine.Event;
import swen.adventure.engine.datastorage.BundleObject;
import swen.adventure.engine.datastorage.BundleSerializable;
import swen.adventure.engine.rendering.maths.Matrix4;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 */
public abstract class SceneNode implements BundleSerializable {

    public interface NodeTraversalFunction {
        void visit(SceneNode node);
    }

    private Optional<TransformNode> _parent = Optional.empty();
    protected Set<SceneNode> _childNodes = new HashSet<>();

    /** Dynamic nodes are any nodes whose transforms may change during the execution of the game. */
    private boolean _isDynamic = false;

    public final String id;
    protected Map<String, SceneNode> _idsToNodesMap;
    protected Set<Light> _allLights;
    protected Set<CollisionNode> _allCollidables;

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
        _allLights = new HashSet<>();
        _allCollidables = new HashSet<>();

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
        _allCollidables = parent._allCollidables;

        this.id = id;
        _idsToNodesMap.put(id, this);

        _isDynamic = isDynamic || parent.isDynamic(); //a node is considered dynamic if it or any of its parents can have a changing transform.

        if (this instanceof Light) {
            _allLights.add((Light)this);
        } else if (this instanceof CollisionNode) {
            _allCollidables.add((CollisionNode)this);
        }
    }

    public Optional<TransformNode> parent() {
        return _parent;
    }

    public Set<SceneNode> siblings() {
        if (_parent.isPresent()) {
            Set<SceneNode> children = _parent.get()._childNodes;
            Set<SceneNode> siblings = new HashSet<>(children);
            siblings.remove(this);
            return siblings;
        }
        return Collections.emptySet();
    }

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
     * @return All of the lights in the scene.
     */
    public Set<Light> allLights() {
        return _allLights;
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


    /**
     * Change this node's parent to the given parent.
     *
     * @param newParent the new parent
     */
    public void changeParentTo(TransformNode newParent) {
        if (_parent.isPresent()) {
            _parent.get()._childNodes.remove(this);
        }

        if (newParent != null) {
            newParent._childNodes.add(this);
        }

        _parent = Optional.ofNullable(newParent);
    }

    public Set<SceneNode> getChildren() {
        return Collections.unmodifiableSet(_childNodes);
    }

}
