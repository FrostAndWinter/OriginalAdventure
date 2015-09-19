package swen.adventure.rendering.maths;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 17/09/15.
 * Methods adapted from and designed to emulate Apple's GLKit framework.
 */
public class Vector4 {
    public final float[] v;
    public final float x, y, z, w; //I'd rather duplicate the data than use a getter since it's guaranteed not to change.

    public Vector4(float x, float y, float z, float w)
    {
        this(new float[]{ x, y, z, w });
    }

    public Vector4(float[] values)
    {
        this.v = values;
        this.x = values[0]; this.y = values[1]; this.z = values[2]; this.w = values[3];
    }

    public Vector4(Vector3 vector, float w)
    {
        this(new float[]{ vector.v[0], vector.v[1], vector.v[2], w });
    }

    public Vector4 negate()
    {
        return new Vector4(-this.v[0], -this.v[1], -this.v[2], -this.v[3]);
    }

    public Vector4 add(Vector4 vectorRight) {
        return new Vector4(this.v[0] + vectorRight.v[0],
                this.v[1] + vectorRight.v[1],
                this.v[2] + vectorRight.v[2],
                this.v[3] + vectorRight.v[3]);
    }

    public Vector4 subtract(Vector4 vectorRight)
    {
        return new Vector4(this.v[0] - vectorRight.v[0],
                this.v[1] - vectorRight.v[1],
                this.v[2] - vectorRight.v[2],
                this.v[3] - vectorRight.v[3]);
    }

    public Vector4 multiply(Vector4 vectorRight)
    {
        return new Vector4(this.v[0] * vectorRight.v[0],
                this.v[1] * vectorRight.v[1],
                this.v[2] * vectorRight.v[2],
                this.v[3] * vectorRight.v[3]);
    }

    public Vector4 divide(Vector4 vectorRight)
    {
        return new Vector4(this.v[0] / vectorRight.v[0],
                this.v[1] / vectorRight.v[1],
                this.v[2] / vectorRight.v[2],
                this.v[3] / vectorRight.v[3]);
    }

    public Vector4 addScalar(float value)
    {
        return new Vector4(this.v[0] + value,
                this.v[1] + value,
                this.v[2] + value,
                this.v[3] + value);
    }

    public Vector4 subtractScalar(float value)
    {
        return new Vector4(this.v[0] - value,
                this.v[1] - value,
                this.v[2] - value,
                this.v[3] - value);
    }

    public Vector4 multiplyScalar(float value)
    {
        return new Vector4(this.v[0] * value,
                this.v[1] * value,
                this.v[2] * value,
                this.v[3] * value);
    }

    public Vector4 divideScalar(float value)
    {
        return new Vector4(this.v[0] / value,
                this.v[1] / value,
                this.v[2] / value,
                this.v[3] / value);
    }

    public Vector4 maximum(Vector4 vectorRight)
    {
        Vector4 max = new Vector4(this.x, this.y, this.z, this.w);
        if (vectorRight.v[0] > this.v[0])
            max.v[0] = vectorRight.v[0];
        if (vectorRight.v[1] > this.v[1])
            max.v[1] = vectorRight.v[1];
        if (vectorRight.v[2] > this.v[2])
            max.v[2] = vectorRight.v[2];
        if (vectorRight.v[3] > this.v[3])
            max.v[3] = vectorRight.v[3];
        return max;
    }

    public Vector4 minimum(Vector4 vectorRight)
    {
        Vector4 min = new Vector4(this.x, this.y, this.z, this.w);
        if (vectorRight.v[0] < this.v[0])
            min.v[0] = vectorRight.v[0];
        if (vectorRight.v[1] < this.v[1])
            min.v[1] = vectorRight.v[1];
        if (vectorRight.v[2] < this.v[2])
            min.v[2] = vectorRight.v[2];
        if (vectorRight.v[3] < this.v[3])
            min.v[3] = vectorRight.v[3];
        return min;
    }

    public boolean allEqualToVector4(Vector4 vectorRight)
    {
        boolean compare = false;
        if (this.v[0] == vectorRight.v[0] &&
                this.v[1] == vectorRight.v[1] &&
                this.v[2] == vectorRight.v[2] &&
                this.v[3] == vectorRight.v[3])
            compare = true;
        return compare;
    }

    public boolean allEqualToScalar(float value)
    {
        boolean compare = false;
        if (this.v[0] == value &&
                this.v[1] == value &&
                this.v[2] == value &&
                this.v[3] == value)
            compare = true;
        return compare;
    }

    public boolean allGreaterThanVector4(Vector4 vectorRight)
    {
        boolean compare = false;
        if (this.v[0] > vectorRight.v[0] &&
                this.v[1] > vectorRight.v[1] &&
                this.v[2] > vectorRight.v[2] &&
                this.v[3] > vectorRight.v[3])
            compare = true;
        return compare;
    }

    public boolean allGreaterThanScalar(float value)
    {
        boolean compare = false;
        if (this.v[0] > value &&
                this.v[1] > value &&
                this.v[2] > value &&
                this.v[3] > value)
            compare = true;
        return compare;
    }

    public boolean allGreaterThanOrEqualToVector4(Vector4 vectorRight)
    {
        boolean compare = false;
        if (this.v[0] >= vectorRight.v[0] &&
                this.v[1] >= vectorRight.v[1] &&
                this.v[2] >= vectorRight.v[2] &&
                this.v[3] >= vectorRight.v[3])
            compare = true;
        return compare;
    }

    public boolean allGreaterThanOrEqualToScalar(float value)
    {
        boolean compare = false;
        if (this.v[0] >= value &&
                this.v[1] >= value &&
                this.v[2] >= value &&
                this.v[3] >= value)
            compare = true;
        return compare;
    }

    public Vector4 normalize()
    {
        float scale = 1.0f / this.length();
        return this.multiplyScalar(scale);
    }

    public float dotProduct(Vector4 vectorRight)
    {
        return this.v[0] * vectorRight.v[0] +
                this.v[1] * vectorRight.v[1] +
                this.v[2] * vectorRight.v[2] +
                this.v[3] * vectorRight.v[3];
    }

    public float length()
    {
        return (float)Math.sqrt(this.dotProduct(this));
    }

    public float distance(Vector4 vectorEnd)
    {
        return vectorEnd.subtract(this).length();
    }

    public Vector4 lerp(Vector4 vectorEnd, float t)
    {
        return new Vector4(this.v[0] + ((vectorEnd.v[0] - this.v[0]) * t),
                this.v[1] + ((vectorEnd.v[1] - this.v[1]) * t),
                this.v[2] + ((vectorEnd.v[2] - this.v[2]) * t),
                this.v[3] + ((vectorEnd.v[3] - this.v[3]) * t));
    }

    public Vector4 crossProduct(Vector4 vectorRight) {
        return new Vector4(this.v[1] * vectorRight.v[2] - this.v[2] * vectorRight.v[1],
                this.v[2] * vectorRight.v[0] - this.v[0] * vectorRight.v[2],
                this.v[0] * vectorRight.v[1] - this.v[1] * vectorRight.v[0],
                0.0f);
    }

    public Vector4 project(Vector4 projectionVector)
    {
        float scale = projectionVector.dotProduct(this) / projectionVector.dotProduct(projectionVector);
        return projectionVector.multiplyScalar(scale);
    }

    public String toString() {
        return String.format("(%f, %f, %f, %f)", this.x, this.y, this.z, this.w);
    }

}
