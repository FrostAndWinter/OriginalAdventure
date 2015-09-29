package swen.adventure.utils;

import swen.adventure.rendering.maths.Matrix4;
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
        return !(this.maxPoint.x < otherBox.minPoint.x ||
                this.minPoint.x > otherBox.maxPoint.x ||
                this.maxPoint.y < otherBox.minPoint.y ||
                this.minPoint.y > otherBox.maxPoint.y ||
                this.maxPoint.z < otherBox.minPoint.z ||
                this.minPoint.z > otherBox.maxPoint.z);
    }
    

    public BoundingBox transformByMatrix(Matrix4 matrix) {
        return new BoundingBox(matrix.multiplyWithTranslation(this.minPoint), matrix.multiplyWithTranslation(this.maxPoint));
    }

    @Override
    public String toString() {
        return "BoundingBox{" +
                "minPoint=" + minPoint +
                ", maxPoint=" + maxPoint +
                '}';
    }

    public BoundingBox axisAlignedBoundingBoxInSpace(Matrix4 nodeToSpaceTransform) {

        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

        //Compute all the vertices for the box.
        for (int xToggle = 0; xToggle < 2; xToggle++) {
            for (int yToggle = 0; yToggle < 2; yToggle++) {
                for (int zToggle = 0; zToggle < 2; zToggle++) {
                    float x = xToggle == 0 ? minPoint.x : maxPoint.x;
                    float y = yToggle == 0 ? minPoint.y : maxPoint.y;
                    float z = zToggle == 0 ? minPoint.z : maxPoint.z;
                    Vector3 vertex = new Vector3(x, y, z);
                    Vector3 transformedVertex = nodeToSpaceTransform.multiplyWithTranslation(vertex);

                    if (transformedVertex.x < minX) { minX = transformedVertex.x; }
                    if (transformedVertex.y < minY) { minY = transformedVertex.y; }
                    if (transformedVertex.z < minZ) { minZ = transformedVertex.z; }
                    if (transformedVertex.x > maxX) { maxX = transformedVertex.x; }
                    if (transformedVertex.y > maxY) { maxY = transformedVertex.y; }
                    if (transformedVertex.z > maxZ) { maxZ = transformedVertex.z; }
                }
            }
        }

        return new BoundingBox(new Vector3(minX, minY, minZ), new Vector3(maxX, maxY, maxZ));

    }
}
