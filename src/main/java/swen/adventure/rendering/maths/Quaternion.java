package swen.adventure.rendering.maths;

import java.util.Arrays;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 17/09/15.
 * Methods adapted from and designed to emulate Apple's GLKit framework.
 */
public class Quaternion {
    public final float[] q;
    public final float x, y, z, w; //I'd rather duplicate the data than use a getter since it's guaranteed not to change.

    public Quaternion() {
        this(new float[]{ 0.f, 0.f, 0.f, 1.f});
    }

    public Quaternion(float x, float y, float z, float w)
    {
        this(new float[]{ x, y, z, w });
    }

    public Quaternion(Vector3 vector, float scalar)
    {
        this(new float[]{ vector.v[0], vector.v[1], vector.v[2], scalar });
    }

    public Quaternion(float[] values)
    {
        this.q = values;
        this.x = values[0]; this.y = values[1]; this.z = values[2]; this.w = values[3];
    }

    public static Quaternion makeWithAngleAndAxis(float radians, float x, float y, float z)
    {
        float halfAngle = radians * 0.5f;
        float scale = (float)Math.sin(halfAngle);
        return new Quaternion(scale * x, scale * y, scale * z, (float)Math.cos(halfAngle));
    }

    public static Quaternion makeWithAngleAndAxis(float radians, Vector3 axisVector)
    {
        return Quaternion.makeWithAngleAndAxis(radians, axisVector.v[0], axisVector.v[1], axisVector.v[2]);
    }

    public Quaternion add(Quaternion quaternionRight)
    {
        return new Quaternion(this.q[0] + quaternionRight.q[0],
                this.q[1] + quaternionRight.q[1],
                this.q[2] + quaternionRight.q[2],
                this.q[3] + quaternionRight.q[3]);
    }

    public Quaternion subtract(Quaternion quaternionRight)
    {
        return new Quaternion(this.q[0] - quaternionRight.q[0],
                this.q[1] - quaternionRight.q[1],
                this.q[2] - quaternionRight.q[2],
                this.q[3] - quaternionRight.q[3]);
    }

    public Quaternion multiply(Quaternion quaternionRight)
    {
        return new Quaternion(this.q[3] * quaternionRight.q[0] +
                this.q[0] * quaternionRight.q[3] +
                this.q[1] * quaternionRight.q[2] -
                this.q[2] * quaternionRight.q[1],

                this.q[3] * quaternionRight.q[1] +
                        this.q[1] * quaternionRight.q[3] +
                        this.q[2] * quaternionRight.q[0] -
                        this.q[0] * quaternionRight.q[2],

                this.q[3] * quaternionRight.q[2] +
                        this.q[2] * quaternionRight.q[3] +
                        this.q[0] * quaternionRight.q[1] -
                        this.q[1] * quaternionRight.q[0],

                this.q[3] * quaternionRight.q[3] -
                        this.q[0] * quaternionRight.q[0] -
                        this.q[1] * quaternionRight.q[1] -
                        this.q[2] * quaternionRight.q[2]);
    }

    public float length()
    {
        return (float)Math.sqrt(this.q[0] * this.q[0] +
                this.q[1] * this.q[1] +
                this.q[2] * this.q[2] +
                this.q[3] * this.q[3]);
    }

    public Quaternion conjugate()
    {
        return new Quaternion(-this.q[0], -this.q[1], -this.q[2], this.q[3]);
    }

    public Quaternion invert()
    {
        float scale = 1.0f / (this.q[0] * this.q[0] +
                this.q[1] * this.q[1] +
                this.q[2] * this.q[2] +
                this.q[3] * this.q[3]);
        return new Quaternion(-this.q[0] * scale, -this.q[1] * scale, -this.q[2] * scale, this.q[3] * scale);
    }

    public Quaternion normalize()
    {
        float scale = 1.0f / this.length();
        return new Quaternion(this.q[0] * scale, this.q[1] * scale, this.q[2] * scale, this.q[3] * scale);
    }

    public Vector3 rotateVector3(Vector3 vector)
    {
        Quaternion rotatedQuaternion = new Quaternion(vector.v[0], vector.v[1], vector.v[2], 0.0f);
        rotatedQuaternion = this.multiply(rotatedQuaternion).multiply(this.invert());

        return new Vector3(rotatedQuaternion.q[0], rotatedQuaternion.q[1], rotatedQuaternion.q[2]);
    }

    public Vector4 rotateVector4(Vector4 vector)
    {
        Quaternion rotatedQuaternion = new Quaternion(vector.v[0], vector.v[1], vector.v[2], 0.0f);
        rotatedQuaternion = this.multiply(rotatedQuaternion).multiply(this.invert());

        return new Vector4(rotatedQuaternion.q[0], rotatedQuaternion.q[1], rotatedQuaternion.q[2], vector.v[3]);
    }

    public Quaternion rotateByAngleX(float theta) {
        float halfAngle = 0.5f * theta;
        float sinHalfAngle = (float)Math.sin(halfAngle);
        float cosHalfAngle = (float)Math.cos(halfAngle);
        return new Quaternion(this.x * cosHalfAngle + this.w * sinHalfAngle, this.y * cosHalfAngle + this.z * sinHalfAngle, -this.y * sinHalfAngle + this.z * cosHalfAngle, -this.x * sinHalfAngle + this.w * cosHalfAngle);
    }

    public Quaternion rotateByAngleY(float theta) {
        float halfAngle = 0.5f * theta;
        float sinHalfAngle = (float)Math.sin(halfAngle);
        float cosHalfAngle = (float)Math.cos(halfAngle);
        return new Quaternion(this.x * cosHalfAngle - this.z * sinHalfAngle, this.y * cosHalfAngle + this.w * sinHalfAngle, this.x * sinHalfAngle + this.z * cosHalfAngle, -this.y * sinHalfAngle + this.w * cosHalfAngle);
    }

    public Quaternion rotateByAngleZ(float theta) {
        float halfAngle = 0.5f * theta;
        float sinHalfAngle = (float)Math.sin(halfAngle);
        float cosHalfAngle = (float)Math.cos(halfAngle);
        return new Quaternion(this.x * cosHalfAngle + this.y * sinHalfAngle, -this.x * sinHalfAngle + this.y * cosHalfAngle, this.z * cosHalfAngle + this.w * sinHalfAngle, -this.z * sinHalfAngle + this.w * cosHalfAngle);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Quaternion that = (Quaternion) o;

        if (Float.compare(that.x, x) != 0) return false;
        if (Float.compare(that.y, y) != 0) return false;
        if (Float.compare(that.z, z) != 0) return false;
        if (Float.compare(that.w, w) != 0) return false;
        return Arrays.equals(q, that.q);

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(q);
        result = 31 * result + (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        result = 31 * result + (z != +0.0f ? Float.floatToIntBits(z) : 0);
        result = 31 * result + (w != +0.0f ? Float.floatToIntBits(w) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Quaternion{" +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", w=" + w +
                '}';
    }
}
