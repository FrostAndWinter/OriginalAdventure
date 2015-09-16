package swen.adventure.utils;

import swen.adventure.rendering.maths.Vector3;
import swen.adventure.rendering.octree.Direction;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 */
public class BoundingBox {
    public final Vector3 minPoint, maxPoint;

    public BoundingBox(Vector3 minPoint, Vector3 maxPoint) {
        this.minPoint = minPoint;
        this.maxPoint = maxPoint;
    }

    public float width() {
        return this.maxPoint.x - this.minPoint.x;
    }

    public float depth() {
        return this.maxPoint.z - this.minPoint.z;
    }

    public float height() {
        return this.maxPoint.y - this.minPoint.y;
    }

    public float volume() {
        return this.depth() * this.width() * this.height();
    }

    public float centreX() {
        return (this.minPoint.x + this.maxPoint.x)/2.f;
    }

    public float centreY() {
        return (this.minPoint.y + this.maxPoint.y)/2.f;
    }

    public float centreZ() {
        return (this.minPoint.z + this.maxPoint.z)/2.f;
    }

    public Vector3 centre() {
        return new Vector3(this.centreX(), this.centreY(), this.centreZ());
    }

    public boolean containsPoint(Vector3 point) {
        return point.x >= this.minPoint.x &&
                point.x <= this.maxPoint.x &&
                point.y >= this.minPoint.y &&
                point.y <= this.maxPoint.y &&
                point.z >= this.minPoint.z &&
                point.z <= this.maxPoint.z;
    }

    public Vector3 vertexInDirection(Direction direction) {
        switch (direction) {
            case FrontUpLeft:
                return new Vector3(this.minPoint.x, this.maxPoint.y, this.maxPoint.z);
            case FrontUpRight:
                return new Vector3(this.maxPoint.x, this.maxPoint.y, this.maxPoint.z);
            case FrontDownLeft:
                return new Vector3(this.minPoint.x, this.minPoint.y, this.maxPoint.z);
            case FrontDownRight:
                return new Vector3(this.maxPoint.x, this.minPoint.y, this.maxPoint.z);
            case BackUpLeft:
                return new Vector3(this.minPoint.x, this.maxPoint.y, this.minPoint.z);
            case BackUpRight:
                return new Vector3(this.maxPoint.x, this.maxPoint.y, this.minPoint.z);
            case BackDownLeft:
                return new Vector3(this.minPoint.x, this.minPoint.y, this.minPoint.z);
            case BackDownRight:
                return new Vector3(this.maxPoint.x, this.minPoint.y, this.minPoint.z);
        }
        throw new RuntimeException("Not a valid direction: " + direction);
    }

    public boolean intersectsWith(BoundingBox otherBox) {

        //For an AABB defined by M,N against one defined by O,P they do not intersect if (Mx>Px) or (Ox>Nx) or (My>Py) or (Oy>Ny) or (Mz>Pz) or (Oz>Nz).
        return (this.maxPoint.x > otherBox.minPoint.x ||
                this.minPoint.x > otherBox.maxPoint.x ||
                this.maxPoint.y > otherBox.minPoint.y ||
                this.minPoint.y > otherBox.maxPoint.y ||
                this.maxPoint.z > otherBox.minPoint.z ||
                this.minPoint.z > otherBox.maxPoint.z);
    }
}
