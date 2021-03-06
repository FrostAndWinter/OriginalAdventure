/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Liam O'Niell (oneilliam) (300312734) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 package swen.adventure.engine.rendering.maths;

import java.util.Arrays;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 17/09/15.
 * Methods adapted from and designed to emulate Apple's GLKit framework.
 * Note that no effort has been made to verify these methods' accuracy beyond recognition of the source as reliable
 * and testing that the results are as expected in use – in this regard, the classes Matrix3, Matrix4, Quaternion, Vector3, and Vector4 should be considered
 * to be part of a third-party library.
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

    public Quaternion slerpTo(Quaternion other, float t) {
        // Calculate angle between them.
        double cosHalfTheta = this.w * other.w + this.x * other.x + this.y * other.y + this.z * other.z;

        // if this == other or this == -other then theta = 0 and we can return this
        if (Math.abs(cosHalfTheta) >= 1.0){
            return this;
        }

        // Calculate temporary values.
        double halfTheta = Math.acos(cosHalfTheta);
        double sinHalfTheta = Math.sqrt(1.0 - cosHalfTheta * cosHalfTheta);

        double x, y, z, w;

        // if theta = 180 degrees then result is not fully defined
        // we could rotate around any axis normal to qa or qb
        if (Math.abs(sinHalfTheta) < 0.001){
            w = (this.w * 0.5 + other.w * 0.5);
            x = (this.x * 0.5 + other.x * 0.5);
            y = (this.y * 0.5 + other.y * 0.5);
            z = (this.z * 0.5 + other.z * 0.5);
        } else {
            double ratioA = Math.sin((1 - t) * halfTheta) / sinHalfTheta;
            double ratioB = Math.sin(t * halfTheta) / sinHalfTheta;

            //calculate Quaternion.
            w = (this.w * ratioA + other.w * ratioB);
            x = (this.x * ratioA + other.x * ratioB);
            y = (this.y * ratioA + other.y * ratioB);
            z = (this.z * ratioA + other.z * ratioB);
        }
        return new Quaternion((float)x, (float)y, (float)z, (float)w);
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