/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Liam O'Niell (oneilliam) (300312734) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 package swen.adventure.engine.rendering.maths;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.Arrays;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 17/09/15.
 * Methods adapted from and designed to emulate Apple's GLKit framework.
 * Note that no effort has been made to verify these methods' accuracy beyond recognition of the source as reliable
 * and testing that the results are as expected in use – in this regard, the classes Matrix3, Matrix4, Quaternion, Vector3, and Vector4 should be considered
 * to be part of a third-party library.
 */
public class Matrix3 {
    public final float[] m;
    
    public Matrix3() {
        this(   1, 0, 0,
                0, 1, 0,
                0, 0, 1);
    }

    public Matrix3(float m00, float m01, float m02,
                                         float m10, float m11, float m12,
                                         float m20, float m21, float m22)
    {
        this(new float[]{ m00, m01, m02,
                m10, m11, m12,
                m20, m21, m22 });
        
    }

    public static Matrix3 makeAndTranspose(float m00, float m01, float m02,
                                                     float m10, float m11, float m12,
                                                     float m20, float m21, float m22)
    {
        return new Matrix3(m00, m10, m20,
                m01, m11, m21,
                m02, m12, m22);
        
    }

    public Matrix3(float[] values)
    {
        this.m = values;
    }

    public static Matrix3 makeWithArrayAndTranspose(float[] values)
    {
        return new Matrix3(values[0], values[3], values[6],
                values[1], values[4], values[7],
                values[2], values[5], values[8]);
        
    }

    public static Matrix3 makeWithRows(Vector3 row0, Vector3 row1, Vector3 row2)
    {
        return new Matrix3(row0.v[0], row1.v[0], row2.v[0],
                row0.v[1], row1.v[1], row2.v[1],
                row0.v[2], row1.v[2], row2.v[2]);
        
    }

    public static Matrix3 makeWithColumns(Vector3 column0, Vector3 column1, Vector3 column2)
    {
        return new Matrix3(column0.v[0], column0.v[1], column0.v[2],
                column1.v[0], column1.v[1], column1.v[2],
                column2.v[0], column2.v[1], column2.v[2]);
        
    }

    public Matrix3(Quaternion quaternion)
    {
        quaternion = quaternion.normalize();

        float x = quaternion.q[0];
        float y = quaternion.q[1];
        float z = quaternion.q[2];
        float w = quaternion.q[3];

        float _2x = x + x;
        float _2y = y + y;
        float _2z = z + z;
        float _2w = w + w;

        this.m = new float[]{ 1.0f - _2y * y - _2z * z,
                _2x * y + _2w * z,
                _2x * z - _2w * y,

                _2x * y - _2w * z,
                1.0f - _2x * x - _2z * z,
                _2y * z + _2w * x,

                _2x * z + _2w * y,
                _2y * z - _2w * x,
                1.0f - _2x * x - _2y * y };

        
    }

    public static Matrix3 makeScale(float sx, float sy, float sz)
    {
        Matrix3 m = new Matrix3();
        m.m[0] = sx;
        m.m[4] = sy;
        m.m[8] = sz;

        return m;
    }

    public static Matrix3 makeRotation(float radians, float x, float y, float z)
    {
        Vector3 v = new Vector3(x, y, z).normalise();
        float cos = (float)Math.cos(radians);
        float cosp = 1.0f - cos;
        float sin = (float)Math.sin(radians);

        return new Matrix3(cos + cosp * v.v[0] * v.v[0],
                cosp * v.v[0] * v.v[1] + v.v[2] * sin,
                cosp * v.v[0] * v.v[2] - v.v[1] * sin,

                cosp * v.v[0] * v.v[1] - v.v[2] * sin,
                cos + cosp * v.v[1] * v.v[1],
                cosp * v.v[1] * v.v[2] + v.v[0] * sin,

                cosp * v.v[0] * v.v[2] + v.v[1] * sin,
                cosp * v.v[1] * v.v[2] - v.v[0] * sin,
                cos + cosp * v.v[2] * v.v[2]);

        
    }

    public static Matrix3 makeXRotation(float radians)
    {
        float cos = (float)Math.cos(radians);
        float sin = (float)Math.sin(radians);

        return new Matrix3(1.0f, 0.0f, 0.0f,
                0.0f, cos, sin,
                0.0f, -sin, cos);

        
    }

    public static Matrix3 makeYRotation(float radians)
    {
        float cos = (float)Math.cos(radians);
        float sin = (float)Math.sin(radians);

        return new Matrix3(cos, 0.0f, -sin,
                0.0f, 1.0f, 0.0f,
                sin, 0.0f, cos);

        
    }

    public static Matrix3 makeZRotation(float radians)
    {
        float cos = (float)Math.cos(radians);
        float sin = (float)Math.sin(radians);

        return new Matrix3(cos, sin, 0.0f,
                -sin, cos, 0.0f,
                0.0f, 0.0f, 1.0f);

        
    }

    public Vector3 getRow(int row)
    {
        return new Vector3(this.m[row], this.m[3 + row], this.m[6 + row]);
    }

    public Vector3 getColumn(int column)
    {
        return new Vector3(this.m[column * 3 + 0], this.m[column * 3 + 1], this.m[column * 3 + 2]);
    }

    public void setRow(int row, Vector3 vector)
    {
        this.m[row] = vector.v[0];
        this.m[row + 3] = vector.v[1];
        this.m[row + 6] = vector.v[2];
    }

    public void setColumn(int column, Vector3 vector)
    {
        this.m[column * 3 + 0] = vector.v[0];
        this.m[column * 3 + 1] = vector.v[1];
        this.m[column * 3 + 2] = vector.v[2];
    }

    public Matrix3 transpose()
    {
        return new Matrix3(this.m[0], this.m[3], this.m[6],
                this.m[1], this.m[4], this.m[7],
                this.m[2], this.m[5], this.m[8]);
        
    }

    public Matrix3 multiply(Matrix3 thisRight)
    {
        Matrix3 m = new Matrix3();

        m.m[0] = this.m[0] * thisRight.m[0] + this.m[3] * thisRight.m[1] + this.m[6] * thisRight.m[2];
        m.m[3] = this.m[0] * thisRight.m[3] + this.m[3] * thisRight.m[4] + this.m[6] * thisRight.m[5];
        m.m[6] = this.m[0] * thisRight.m[6] + this.m[3] * thisRight.m[7] + this.m[6] * thisRight.m[8];

        m.m[1] = this.m[1] * thisRight.m[0] + this.m[4] * thisRight.m[1] + this.m[7] * thisRight.m[2];
        m.m[4] = this.m[1] * thisRight.m[3] + this.m[4] * thisRight.m[4] + this.m[7] * thisRight.m[5];
        m.m[7] = this.m[1] * thisRight.m[6] + this.m[4] * thisRight.m[7] + this.m[7] * thisRight.m[8];

        m.m[2] = this.m[2] * thisRight.m[0] + this.m[5] * thisRight.m[1] + this.m[8] * thisRight.m[2];
        m.m[5] = this.m[2] * thisRight.m[3] + this.m[5] * thisRight.m[4] + this.m[8] * thisRight.m[5];
        m.m[8] = this.m[2] * thisRight.m[6] + this.m[5] * thisRight.m[7] + this.m[8] * thisRight.m[8];
        
        return m;
    }

    public Matrix3 add(Matrix3 thisRight)
    {
        Matrix3 m = new Matrix3();

        m.m[0] = this.m[0] + thisRight.m[0];
        m.m[1] = this.m[1] + thisRight.m[1];
        m.m[2] = this.m[2] + thisRight.m[2];

        m.m[3] = this.m[3] + thisRight.m[3];
        m.m[4] = this.m[4] + thisRight.m[4];
        m.m[5] = this.m[5] + thisRight.m[5];

        m.m[6] = this.m[6] + thisRight.m[6];
        m.m[7] = this.m[7] + thisRight.m[7];
        m.m[8] = this.m[8] + thisRight.m[8];
        
        return m;
    }

    public Matrix3 subtract(Matrix3 thisRight)
    {
        Matrix3 m = new Matrix3();

        m.m[0] = this.m[0] - thisRight.m[0];
        m.m[1] = this.m[1] - thisRight.m[1];
        m.m[2] = this.m[2] - thisRight.m[2];

        m.m[3] = this.m[3] - thisRight.m[3];
        m.m[4] = this.m[4] - thisRight.m[4];
        m.m[5] = this.m[5] - thisRight.m[5];

        m.m[6] = this.m[6] - thisRight.m[6];
        m.m[7] = this.m[7] - thisRight.m[7];
        m.m[8] = this.m[8] - thisRight.m[8];
        
        return m;
    }

    public Matrix3 scale(float sx, float sy, float sz)
    {
        return new Matrix3(this.m[0] * sx, this.m[1] * sx, this.m[2] * sx,
                this.m[3] * sy, this.m[4] * sy, this.m[5] * sy,
                this.m[6] * sz, this.m[7] * sz, this.m[8] * sz);
        
    }

    public Matrix3 scale(Vector3 scaleVector)
    {
        return new Matrix3(this.m[0] * scaleVector.v[0], this.m[1] * scaleVector.v[0], this.m[2] * scaleVector.v[0],
                this.m[3] * scaleVector.v[1], this.m[4] * scaleVector.v[1], this.m[5] * scaleVector.v[1],
                this.m[6] * scaleVector.v[2], this.m[7] * scaleVector.v[2], this.m[8] * scaleVector.v[2]);
        
    }

    public Matrix3 scale(Vector4 scaleVector)
    {
        return new Matrix3(this.m[0] * scaleVector.v[0], this.m[1] * scaleVector.v[0], this.m[2] * scaleVector.v[0],
                this.m[3] * scaleVector.v[1], this.m[4] * scaleVector.v[1], this.m[5] * scaleVector.v[1],
                this.m[6] * scaleVector.v[2], this.m[7] * scaleVector.v[2], this.m[8] * scaleVector.v[2]);
        
    }

    public Matrix3 rotate(float radians, float x, float y, float z)
    {
        Matrix3 rm = Matrix3.makeRotation(radians, x, y, z);
        return this.multiply(rm);
    }

    public Matrix3 rotate(float radians, Vector3 axisVector)
    {
        Matrix3 rm = Matrix3.makeRotation(radians, axisVector.v[0], axisVector.v[1], axisVector.v[2]);
        return this.multiply(rm);
    }

    public Matrix3 rotate(float radians, Vector4 axisVector)
    {
        Matrix3 rm = Matrix3.makeRotation(radians, axisVector.v[0], axisVector.v[1], axisVector.v[2]);
        return this.multiply(rm);
    }

    public Matrix3 rotateX(float radians)
    {
        Matrix3 rm = Matrix3.makeXRotation(radians);
        return this.multiply(rm);
    }

    public Matrix3 rotateY(float radians)
    {
        Matrix3 rm = Matrix3.makeYRotation(radians);
        return this.multiply(rm);
    }

    public Matrix3 rotateZ(float radians)
    {
        Matrix3 rm = Matrix3.makeZRotation(radians);
        return this.multiply(rm);
    }

    public Vector3 multiply(Vector3 vectorRight)
    {
        return new Vector3(this.m[0] * vectorRight.v[0] + this.m[3] * vectorRight.v[1] + this.m[6] * vectorRight.v[2],
                this.m[1] * vectorRight.v[0] + this.m[4] * vectorRight.v[1] + this.m[7] * vectorRight.v[2],
                this.m[2] * vectorRight.v[0] + this.m[5] * vectorRight.v[1] + this.m[8] * vectorRight.v[2]);
    }

    public void multiplyArray(Vector3[] vectors)
    {
        for (int i=0; i < vectors.length; i++)
            vectors[i] = this.multiply(vectors[i]);
    }

    public float determinant() {
        return
                        + m[0] * (m[4] * m[8] - m[7] * m[5])
                        - m[3] * (m[1] * m[8] - m[7] * m[2])
                        + m[6] * (m[1] * m[5] - m[4] * m[2]);
    }

    public Matrix3 inverse() {
        float determinant = this.determinant();

        Matrix3 result = new Matrix3();

        result.m[0] = + (m[4] * m[8] - m[7] * m[5]) / determinant;
        result.m[3] = - (m[3] * m[8] - m[6] * m[5]) / determinant;
        result.m[6] = + (m[3] * m[7] - m[6] * m[4]) / determinant;
        result.m[1] = - (m[1] * m[8] - m[7] * m[2]) / determinant;
        result.m[4] = + (m[0] * m[8] - m[6] * m[2]) / determinant;
        result.m[7] = - (m[0] * m[7] - m[6] * m[1]) / determinant;
        result.m[2] = + (m[1] * m[5] - m[4] * m[2]) / determinant;
        result.m[5] = - (m[0] * m[5] - m[3] * m[2]) / determinant;
        result.m[8] = + (m[0] * m[4] - m[3] * m[1]) / determinant;

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Matrix3 matrix3 = (Matrix3) o;

        return Arrays.equals(m, matrix3.m);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(m);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("(");
        int column = 0;
        int row = 0;
        for (int i = 0; i < 9; i++) {
            stringBuilder.append(this.m[column * 3 + row]).append(", ");
            column++;
            if (column == 3) {
                column = 0;
                row++;
                stringBuilder.append("\n");
            }
        }
        stringBuilder.append(")\n");
        return stringBuilder.toString();
    }

    public FloatBuffer toFloatBuffer() {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(9);
        buffer.put(this.m);
        buffer.flip();
        return buffer;
    }
}