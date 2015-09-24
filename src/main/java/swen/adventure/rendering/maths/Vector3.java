package swen.adventure.rendering.maths;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 * Methods adapted from and designed to emulate Apple's GLKit framework.
 */
public class Vector3 implements Vector {
    public final float[] v;
    public final float x, y, z; //I'd rather duplicate the data than use a getter since it's guaranteed not to change.


    public Vector3(float x, float y, float z) {
        this(new float[]{ x, y, z });
    }

    public Vector3(float[] values) {
        this.v = values;

        this.x = values[0];
        this.y = values[1];
        this.z = values[2];
    }

    public Vector3 negate() {
        return new Vector3(-this.v[0], -this.v[1], -this.v[2] );
    }

    public Vector3 add(Vector3 vectorRight) {
        return new Vector3(this.v[0] + vectorRight.v[0],
                this.v[1] + vectorRight.v[1],
                this.v[2] + vectorRight.v[2]);
    }

    public Vector3 subtract(Vector3 vectorRight) {
        return new Vector3(this.v[0] - vectorRight.v[0],
                this.v[1] - vectorRight.v[1],
                this.v[2] - vectorRight.v[2] );
    }

    public Vector3 multiply(Vector3 vectorRight) {
        return new Vector3(this.v[0] * vectorRight.v[0],
                this.v[1] * vectorRight.v[1],
                this.v[2] * vectorRight.v[2] );
        
    }

    public Vector3 divide(Vector3 vectorRight)
    {
        return new Vector3(this.v[0] / vectorRight.v[0],
                this.v[1] / vectorRight.v[1],
                this.v[2] / vectorRight.v[2] );
        
    }

    public Vector3 addScalar(float value) {
        return new Vector3(this.v[0] + value,
                this.v[1] + value,
                this.v[2] + value);
        
    }

    public Vector3 subtractScalar(float value) {
        return new Vector3(this.v[0] - value,
                this.v[1] - value,
                this.v[2] - value);
        
    }

    public Vector3 multiplyScalar(float value) {
        return new Vector3(this.v[0] * value,
                this.v[1] * value,
                this.v[2] * value);
        
    }

    public Vector3 divideScalar(float value) {
        return new Vector3(this.v[0] / value,
                this.v[1] / value,
                this.v[2] / value);
        
    }

    public Vector3 maximum(Vector3 vectorRight) {
        Vector3 max = this;
        if (vectorRight.v[0] > this.v[0])
            max.v[0] = vectorRight.v[0];
        if (vectorRight.v[1] > this.v[1])
            max.v[1] = vectorRight.v[1];
        if (vectorRight.v[2] > this.v[2])
            max.v[2] = vectorRight.v[2];
        return max;
    }

    public Vector3 minimum(Vector3 vectorRight) {
        Vector3 min = this;
        if (vectorRight.v[0] < this.v[0])
            min.v[0] = vectorRight.v[0];
        if (vectorRight.v[1] < this.v[1])
            min.v[1] = vectorRight.v[1];
        if (vectorRight.v[2] < this.v[2])
            min.v[2] = vectorRight.v[2];
        return min;
    }

    public boolean equals(Vector3 vectorRight) {
        return (this.v[0] == vectorRight.v[0] &&
                this.v[1] == vectorRight.v[1] &&
                this.v[2] == vectorRight.v[2]);
    }

    public boolean allComponentsEqualScalar(float value) {
        return (this.v[0] == value &&
                this.v[1] == value &&
                this.v[2] == value);
    }

    public boolean allComponentsGreaterThanVector3(Vector3 vectorRight) {
        return (this.v[0] > vectorRight.v[0] &&
                this.v[1] > vectorRight.v[1] &&
                this.v[2] > vectorRight.v[2]);

    }

    public boolean allComponentsGreaterThanScalar(Vector3 vector, float value) {
        return (vector.v[0] > value &&
                vector.v[1] > value &&
                vector.v[2] > value);
    }

    public boolean allComponentsGreaterThanOrEqualToVector3(Vector3 vectorRight) {
        return (this.v[0] >= vectorRight.v[0] &&
                this.v[1] >= vectorRight.v[1] &&
                this.v[2] >= vectorRight.v[2]);
    }

    public boolean allComponentsGreaterThanOrEqualToScalar(float value) {
        return (this.v[0] >= value &&
                this.v[1] >= value &&
                this.v[2] >= value);
    }

    public Vector3 normalise() {
        float scale = 1.0f / this.length();
        return new Vector3(this.v[0] * scale, this.v[1] * scale, this.v[2] * scale);
        
    }

    public float dotProduct(Vector3 vectorRight) {
        return this.v[0] * vectorRight.v[0] + this.v[1] * vectorRight.v[1] + this.v[2] * vectorRight.v[2];
    }

    public float length() {
        return (float)Math.sqrt(this.v[0] * this.v[0] + this.v[1] * this.v[1] + this.v[2] * this.v[2]);
    }

    public float distance(Vector3 vectorEnd) {
        return vectorEnd.subtract(this).length();
    }

    public Vector3 lerpTo(Vector3 vectorEnd, float t) {
        return new Vector3(this.v[0] + ((vectorEnd.v[0] - this.v[0]) * t),
                this.v[1] + ((vectorEnd.v[1] - this.v[1]) * t),
                this.v[2] + ((vectorEnd.v[2] - this.v[2]) * t) );
        
    }

    public Vector3 crossProduct(Vector3 vectorRight) {
        return new Vector3(this.v[1] * vectorRight.v[2] - this.v[2] * vectorRight.v[1],
                this.v[2] * vectorRight.v[0] - this.v[0] * vectorRight.v[2],
                this.v[0] * vectorRight.v[1] - this.v[1] * vectorRight.v[0] );
        
    }

    public static Vector3 project(Vector3 vectorToProject, Vector3 projectionVector) {
        float scale = projectionVector.dotProduct(vectorToProject) / projectionVector.dotProduct(projectionVector);
        return projectionVector.multiplyScalar(scale);
    }

    public String toString() {
        return String.format("(%f, %f, %f)", this.x, this.y, this.z);
    }

    @Override
    public int numberOfComponents() {
        return 3;
    }

    @Override
    public float[] data() {
        return this.v;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Vector3 vector3 = (Vector3) o;

        if (Float.compare(vector3.x, x) != 0) return false;
        if (Float.compare(vector3.y, y) != 0) return false;
        return Float.compare(vector3.z, z) == 0;

    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        result = 31 * result + (z != +0.0f ? Float.floatToIntBits(z) : 0);
        return result;
    }

    public FloatBuffer asFloatBuffer() {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
        buffer.put(this.v);
        buffer.flip();
        return buffer;
    }
}
