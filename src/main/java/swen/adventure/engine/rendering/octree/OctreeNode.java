package swen.adventure.engine.rendering.octree;

import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.rendering.maths.BoundingBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 * An Octree partitions points spatially to allow for efficient lookup of what points are in an area.
 */
class OctreeNode<T> {

    private Set<T> _values = new HashSet<>();
    private OctreeNode[] _children =  new OctreeNode[8];
    public final Vector3 point;

    public OctreeNode(Vector3 point) {
        this.point = point;
    }

    private Direction directionForPoint(Vector3 point) {
        boolean toRight = point.x > this.point.x;
        boolean above = point.y > this.point.y;
        boolean inFront = point.z > this.point.z;

        if (toRight && above && inFront) { return Direction.FrontUpRight; }
        if (toRight && above) { return Direction.BackUpRight; }
        if (toRight && inFront) { return Direction.FrontDownRight; }
        if (toRight) { return Direction.BackDownRight; }
        if (above && inFront) { return Direction.FrontUpLeft; }
        if (above) { return Direction.BackUpLeft; }
        if (inFront) { return Direction.FrontDownLeft; }
        return Direction.BackDownLeft;

    }

    public void addValue(T object, BoundingBox boundingBox) {

        if (boundingBox.containsPoint(this.point)) { //then this point is contained within this object's bounding box
            _values.add(object);
        } else {
            int index = this.directionForPoint(this.point).index;
            OctreeNode<T> child = _children[index];
            if (child == null) {
                child = new OctreeNode(boundingBox.centre());
                _children[index] = child;
            }
            child.addValue(object, boundingBox);
        }
    }

    private void addValuesInRegionToList(BoundingBox region, List<Set<T>> list) {

        //check each point of the bounding box and find the region that point is in
        //then call add on that part.

        if (region.containsPoint(this.point)) {
            list.add(_values);
        }

        boolean[] visitedChildren = new boolean[8];

        for (Direction direction : Direction.values()) {
            Vector3 vertex = region.vertexInDirection(direction);
            int directionFromThisTree = this.directionForPoint(vertex).index;

            if (!visitedChildren[directionFromThisTree]) {
                _children[directionFromThisTree].addValuesInRegionToList(region, list);
                visitedChildren[directionFromThisTree] = true;
            }

        }
    }

    /**
     * Returns a list of sets of elements that may be within the region.
     * May also return objects that are outside of the region: these may need to be discarded by whatever processes the objects.
     * The list of sets is a performance consideration (i.e. we're only generating a small list instead of a massive set or adding a large number of elements.)
     */
    public List<Set<T>> valuesInRegion(BoundingBox region) {
        List<Set<T>> list = new ArrayList<Set<T>>();
        this.addValuesInRegionToList(region, list);
        return list;
    }
}