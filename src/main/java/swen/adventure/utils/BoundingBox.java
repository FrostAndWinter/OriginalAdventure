package swen.adventure.utils;

import swen.adventure.rendering.maths.Vector3;
import swen.adventure.rendering.octree.Direction;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 */
public class BoundingBox {
    public final float minX, minY, minZ, maxX, maxY, maxZ;

    public BoundingBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public float width() {
        return maxX - minX;
    }

    public float depth() {
        return maxZ - minZ;
    }

    public float height() {
        return maxY - minY;
    }

    public float volume() {
        return this.depth() * this.width() * this.height();
    }

    public float centreX() {
        return (minX + maxX)/2.f;
    }

    public float centreY() {
        return (minY + maxY)/2.f;
    }

    public float centreZ() {
        return (minZ + maxZ)/2.f;
    }

    public Vector3 centre() {
        return new Vector3(this.centreX(), this.centreY(), this.centreZ());
    }

    public boolean containsPoint(Vector3 point) {
        return point.x >= this.minX &&
                point.x <= this.maxX &&
                point.y >= this.minY &&
                point.y <= this.maxY &&
                point.z >= this.minZ &&
                point.z <= this.maxZ;
    }

    public Vector3 vertexInDirection(Direction direction) {
        switch (direction) {
            case FrontUpLeft:
                return new Vector3(this.minX, this.maxY, this.maxZ);
            case FrontUpRight:
                return new Vector3(this.maxX, this.maxY, this.maxZ);
            case FrontDownLeft:
                return new Vector3(this.minX, this.minY, this.maxZ);
            case FrontDownRight:
                return new Vector3(this.maxX, this.minY, this.maxZ);
            case BackUpLeft:
                return new Vector3(this.minX, this.maxY, this.minZ);
            case BackUpRight:
                return new Vector3(this.maxX, this.maxY, this.minZ);
            case BackDownLeft:
                return new Vector3(this.minX, this.minY, this.minZ);
            case BackDownRight:
                return new Vector3(this.maxX, this.minY, this.minZ);
        }
        throw new RuntimeException("Not a valid direction: " + direction);
    }
}
