package swen.adventure.rendering.maths;

import processing.core.PMatrix3D;

import java.util.Arrays;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 17/09/15.
 * Methods adapted from and designed to emulate Apple's GLKit framework.
 */
public class Matrix4 {

    public final float[] m;

    public Matrix4() {
        this.m = new float[]{1, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, 1, 0,
                    0, 0, 0, 1};
    }

    public Matrix4(float m00, float m01, float m02, float m03,
                                         float m10, float m11, float m12, float m13,
                                         float m20, float m21, float m22, float m23,
                                         float m30, float m31, float m32, float m33) {
        this(new float[]{ m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33 });
    }

    public static Matrix4 makeAndTranspose(float m00, float m01, float m02, float m03,
                                                     float m10, float m11, float m12, float m13,
                                                     float m20, float m21, float m22, float m23,
                                                     float m30, float m31, float m32, float m33) {
        return new Matrix4(m00, m10, m20, m30,
                m01, m11, m21, m31,
                m02, m12, m22, m32,
                m03, m13, m23, m33);
    }

    public Matrix4(float[] values) {
        this.m = values;
    }

    public static Matrix4 makeWithArrayAndTranspose(float[] values) {
        return new Matrix4(values[0], values[4], values[8], values[12],
                values[1], values[5], values[9], values[13],
                values[2], values[6], values[10], values[14],
                values[3], values[7], values[11], values[15]);
    }

    public static Matrix4 makeWithRows(Vector4 row0,
                                                 Vector4 row1,
                                                 Vector4 row2,
                                                 Vector4 row3) {
        return new Matrix4(row0.v[0], row1.v[0], row2.v[0], row3.v[0],
                row0.v[1], row1.v[1], row2.v[1], row3.v[1],
                row0.v[2], row1.v[2], row2.v[2], row3.v[2],
                row0.v[3], row1.v[3], row2.v[3], row3.v[3]);
    }

    public static Matrix4 makeWithColumns(Vector4 column0,
                                                    Vector4 column1,
                                                    Vector4 column2,
                                                    Vector4 column3) {
        return new Matrix4(column0.v[0], column0.v[1], column0.v[2], column0.v[3],
                column1.v[0], column1.v[1], column1.v[2], column1.v[3],
                column2.v[0], column2.v[1], column2.v[2], column2.v[3],
                column3.v[0], column3.v[1], column3.v[2], column3.v[3]);
    }

    public Matrix4(Quaternion quaternion) {
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
                0.0f,
                _2x * y - _2w * z,
                1.0f - _2x * x - _2z * z,
                _2y * z + _2w * x,
                0.0f,
                _2x * z + _2w * y,
                _2y * z - _2w * x,
                1.0f - _2x * x - _2y * y,
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                1.0f };
    }

    public static Matrix4 makeTranslation(float tx, float ty, float tz) {
        Matrix4 m = new Matrix4();
        m.m[12] = tx;
        m.m[13] = ty;
        m.m[14] = tz;
        return m;
    }

    public static Matrix4 makeScale(float sx, float sy, float sz) {
        Matrix4 m = new Matrix4();
        m.m[0] = sx;
        m.m[5] = sy;
        m.m[10] = sz;
        return m;
    }

    public static Matrix4 makeRotation(float radians, float x, float y, float z) {
        Vector3 v = new Vector3(x, y, z).normalise();
        float cos = (float)Math.cos(radians);
        float cosp = 1.0f - cos;
        float sin = (float)Math.sin(radians);

        return new Matrix4(cos + cosp * v.v[0] * v.v[0],
                cosp * v.v[0] * v.v[1] + v.v[2] * sin,
                cosp * v.v[0] * v.v[2] - v.v[1] * sin,
                0.0f,
                cosp * v.v[0] * v.v[1] - v.v[2] * sin,
                cos + cosp * v.v[1] * v.v[1],
                cosp * v.v[1] * v.v[2] + v.v[0] * sin,
                0.0f,
                cosp * v.v[0] * v.v[2] + v.v[1] * sin,
                cosp * v.v[1] * v.v[2] - v.v[0] * sin,
                cos + cosp * v.v[2] * v.v[2],
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                1.0f);
    }

    public static Matrix4 makeXRotation(float radians) {
        float cos = (float)Math.cos(radians);
        float sin = (float)Math.sin(radians);

        return new Matrix4(1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, cos, sin, 0.0f,
                0.0f, -sin, cos, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f);
    }

    public static Matrix4 makeYRotation(float radians) {
        float cos = (float)Math.cos(radians);
        float sin = (float)Math.sin(radians);

        return new Matrix4( cos, 0.0f, -sin, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                sin, 0.0f, cos, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f );
    }

    public static Matrix4 makeZRotation(float radians) {
        float cos = (float)Math.cos(radians);
        float sin = (float)Math.sin(radians);

        return new Matrix4( cos, sin, 0.0f, 0.0f,
                -sin, cos, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f );
    }

    public static Matrix4 makePerspective(float fovyRadians, float aspect, float nearZ, float farZ) {
        float cotan = 1.0f / (float)Math.tan(fovyRadians / 2.0f);

        return new Matrix4(
                cotan / aspect, 0.0f, 0.0f, 0.0f,
                0.0f, cotan, 0.0f, 0.0f,
                0.0f, 0.0f, (farZ + nearZ) / (nearZ - farZ), -1.0f,
                0.0f, 0.0f, (2.0f * farZ * nearZ) / (nearZ - farZ), 0.0f );
    }

    public static Matrix4 makeFrustum(float left, float right,
                                                float bottom, float top,
                                                float nearZ, float farZ) {
        float ral = right + left;
        float rsl = right - left;
        float tsb = top - bottom;
        float tab = top + bottom;
        float fan = farZ + nearZ;
        float fsn = farZ - nearZ;

        return new Matrix4( 2.0f * nearZ / rsl, 0.0f, 0.0f, 0.0f,
                0.0f, 2.0f * nearZ / tsb, 0.0f, 0.0f,
                ral / rsl, tab / tsb, -fan / fsn, -1.0f,
                0.0f, 0.0f, (-2.0f * farZ * nearZ) / fsn, 0.0f );
    }

    public static Matrix4 makeOrtho(float left, float right,
                                              float bottom, float top,
                                              float nearZ, float farZ) {
        float ral = right + left;
        float rsl = right - left;
        float tab = top + bottom;
        float tsb = top - bottom;
        float fan = farZ + nearZ;
        float fsn = farZ - nearZ;

        return new Matrix4( 2.0f / rsl, 0.0f, 0.0f, 0.0f,
                0.0f, 2.0f / tsb, 0.0f, 0.0f,
                0.0f, 0.0f, -2.0f / fsn, 0.0f,
                -ral / rsl, -tab / tsb, -fan / fsn, 1.0f );
    }

    public static Matrix4 makeLookAt(float eyeX, float eyeY, float eyeZ,
                                               float centerX, float centerY, float centerZ,
                                               float upX, float upY, float upZ) {
        Vector3 ev = new Vector3(eyeX, eyeY, eyeZ);
        Vector3 cv = new Vector3(centerX, centerY, centerZ);
        Vector3 uv = new Vector3(upX, upY, upZ);
        Vector3 n = ev.subtract(cv).normalise();
        Vector3 u = uv.crossProduct(n).normalise();
        Vector3 v = n.crossProduct(u);

        return new Matrix4(u.v[0], v.v[0], n.v[0], 0.0f,
                u.v[1], v.v[1], n.v[1], 0.0f,
                u.v[2], v.v[2], n.v[2], 0.0f,
                u.negate().dotProduct(ev),
                v.negate().dotProduct(ev),
                n.negate().dotProduct(ev),
                1.0f);
    }

    Matrix3 getMatrix3(Matrix4 matrix) {
        return new Matrix3(this.m[0], this.m[1], this.m[2],
                this.m[4], this.m[5], this.m[6],
                this.m[8], this.m[9], this.m[10]);
    }

    public Vector4 getRow(int row) {
        Vector4 v = new Vector4(this.m[row], this.m[4 + row], this.m[8 + row], this.m[12 + row]);
        return v;
    }

    public Vector4 getColumn(int column) {
        Vector4 v = new Vector4(this.m[column * 4 + 0], this.m[column * 4 + 1], this.m[column * 4 + 2], this.m[column * 4 + 3]);
        return v;
    }

    public void setRow(int row, Vector4 vector) {
        this.m[row] = vector.v[0];
        this.m[row + 4] = vector.v[1];
        this.m[row + 8] = vector.v[2];
        this.m[row + 12] = vector.v[3];
    }

    public void setColumn(int column, Vector4 vector) {
        this.m[column * 4 + 0] = vector.v[0];
        this.m[column * 4 + 1] = vector.v[1];
        this.m[column * 4 + 2] = vector.v[2];
        this.m[column * 4 + 3] = vector.v[3];
    }

    public Matrix4 transpose() {
        return new Matrix4(this.m[0], this.m[4], this.m[8], this.m[12],
                this.m[1], this.m[5], this.m[9], this.m[13],
                this.m[2], this.m[6], this.m[10], this.m[14],
                this.m[3], this.m[7], this.m[11], this.m[15] );
    }

    public Matrix4 multiply(Matrix4 matrixRight) {
        Matrix4 m = new Matrix4();

        m.m[0]  = this.m[0] * matrixRight.m[0]  + this.m[4] * matrixRight.m[1]  + this.m[8] * matrixRight.m[2]   + this.m[12] * matrixRight.m[3];
        m.m[4]  = this.m[0] * matrixRight.m[4]  + this.m[4] * matrixRight.m[5]  + this.m[8] * matrixRight.m[6]   + this.m[12] * matrixRight.m[7];
        m.m[8]  = this.m[0] * matrixRight.m[8]  + this.m[4] * matrixRight.m[9]  + this.m[8] * matrixRight.m[10]  + this.m[12] * matrixRight.m[11];
        m.m[12] = this.m[0] * matrixRight.m[12] + this.m[4] * matrixRight.m[13] + this.m[8] * matrixRight.m[14]  + this.m[12] * matrixRight.m[15];

        m.m[1]  = this.m[1] * matrixRight.m[0]  + this.m[5] * matrixRight.m[1]  + this.m[9] * matrixRight.m[2]   + this.m[13] * matrixRight.m[3];
        m.m[5]  = this.m[1] * matrixRight.m[4]  + this.m[5] * matrixRight.m[5]  + this.m[9] * matrixRight.m[6]   + this.m[13] * matrixRight.m[7];
        m.m[9]  = this.m[1] * matrixRight.m[8]  + this.m[5] * matrixRight.m[9]  + this.m[9] * matrixRight.m[10]  + this.m[13] * matrixRight.m[11];
        m.m[13] = this.m[1] * matrixRight.m[12] + this.m[5] * matrixRight.m[13] + this.m[9] * matrixRight.m[14]  + this.m[13] * matrixRight.m[15];

        m.m[2]  = this.m[2] * matrixRight.m[0]  + this.m[6] * matrixRight.m[1]  + this.m[10] * matrixRight.m[2]  + this.m[14] * matrixRight.m[3];
        m.m[6]  = this.m[2] * matrixRight.m[4]  + this.m[6] * matrixRight.m[5]  + this.m[10] * matrixRight.m[6]  + this.m[14] * matrixRight.m[7];
        m.m[10] = this.m[2] * matrixRight.m[8]  + this.m[6] * matrixRight.m[9]  + this.m[10] * matrixRight.m[10] + this.m[14] * matrixRight.m[11];
        m.m[14] = this.m[2] * matrixRight.m[12] + this.m[6] * matrixRight.m[13] + this.m[10] * matrixRight.m[14] + this.m[14] * matrixRight.m[15];

        m.m[3]  = this.m[3] * matrixRight.m[0]  + this.m[7] * matrixRight.m[1]  + this.m[11] * matrixRight.m[2]  + this.m[15] * matrixRight.m[3];
        m.m[7]  = this.m[3] * matrixRight.m[4]  + this.m[7] * matrixRight.m[5]  + this.m[11] * matrixRight.m[6]  + this.m[15] * matrixRight.m[7];
        m.m[11] = this.m[3] * matrixRight.m[8]  + this.m[7] * matrixRight.m[9]  + this.m[11] * matrixRight.m[10] + this.m[15] * matrixRight.m[11];
        m.m[15] = this.m[3] * matrixRight.m[12] + this.m[7] * matrixRight.m[13] + this.m[11] * matrixRight.m[14] + this.m[15] * matrixRight.m[15];

        return m;
    }

    public Matrix4 add(Matrix4 matrixRight) {
        Matrix4 m = new Matrix4();

        m.m[0] = this.m[0] + matrixRight.m[0];
        m.m[1] = this.m[1] + matrixRight.m[1];
        m.m[2] = this.m[2] + matrixRight.m[2];
        m.m[3] = this.m[3] + matrixRight.m[3];

        m.m[4] = this.m[4] + matrixRight.m[4];
        m.m[5] = this.m[5] + matrixRight.m[5];
        m.m[6] = this.m[6] + matrixRight.m[6];
        m.m[7] = this.m[7] + matrixRight.m[7];

        m.m[8] = this.m[8] + matrixRight.m[8];
        m.m[9] = this.m[9] + matrixRight.m[9];
        m.m[10] = this.m[10] + matrixRight.m[10];
        m.m[11] = this.m[11] + matrixRight.m[11];

        m.m[12] = this.m[12] + matrixRight.m[12];
        m.m[13] = this.m[13] + matrixRight.m[13];
        m.m[14] = this.m[14] + matrixRight.m[14];
        m.m[15] = this.m[15] + matrixRight.m[15];

        return m;
    }

    public Matrix4 subtract(Matrix4 matrixRight) {
        Matrix4 m = new Matrix4();

        m.m[0] = this.m[0] - matrixRight.m[0];
        m.m[1] = this.m[1] - matrixRight.m[1];
        m.m[2] = this.m[2] - matrixRight.m[2];
        m.m[3] = this.m[3] - matrixRight.m[3];

        m.m[4] = this.m[4] - matrixRight.m[4];
        m.m[5] = this.m[5] - matrixRight.m[5];
        m.m[6] = this.m[6] - matrixRight.m[6];
        m.m[7] = this.m[7] - matrixRight.m[7];

        m.m[8] = this.m[8] - matrixRight.m[8];
        m.m[9] = this.m[9] - matrixRight.m[9];
        m.m[10] = this.m[10] - matrixRight.m[10];
        m.m[11] = this.m[11] - matrixRight.m[11];

        m.m[12] = this.m[12] - matrixRight.m[12];
        m.m[13] = this.m[13] - matrixRight.m[13];
        m.m[14] = this.m[14] - matrixRight.m[14];
        m.m[15] = this.m[15] - matrixRight.m[15];

        return m;
    }

    public Matrix4 translate(float tx, float ty, float tz) {
        return new Matrix4(this.m[0], this.m[1], this.m[2], this.m[3],
                this.m[4], this.m[5], this.m[6], this.m[7],
                this.m[8], this.m[9], this.m[10], this.m[11],
                this.m[0] * tx + this.m[4] * ty + this.m[8] * tz + this.m[12],
                this.m[1] * tx + this.m[5] * ty + this.m[9] * tz + this.m[13],
                this.m[2] * tx + this.m[6] * ty + this.m[10] * tz + this.m[14],
                this.m[3] * tx + this.m[7] * ty + this.m[11] * tz + this.m[15]);
    }

    public Matrix4 translate(Vector3 translationVector) {
        return new Matrix4(this.m[0], this.m[1], this.m[2], this.m[3],
                this.m[4], this.m[5], this.m[6], this.m[7],
                this.m[8], this.m[9], this.m[10], this.m[11],
                this.m[0] * translationVector.v[0] + this.m[4] * translationVector.v[1] + this.m[8] * translationVector.v[2] + this.m[12],
                this.m[1] * translationVector.v[0] + this.m[5] * translationVector.v[1] + this.m[9] * translationVector.v[2] + this.m[13],
                this.m[2] * translationVector.v[0] + this.m[6] * translationVector.v[1] + this.m[10] * translationVector.v[2] + this.m[14],
                this.m[3] * translationVector.v[0] + this.m[7] * translationVector.v[1] + this.m[11] * translationVector.v[2] + this.m[15]);
    }

    public Matrix4 translate(Vector4 translationVector) {
        return new Matrix4(this.m[0], this.m[1], this.m[2], this.m[3],
                this.m[4], this.m[5], this.m[6], this.m[7],
                this.m[8], this.m[9], this.m[10], this.m[11],
                this.m[0] * translationVector.v[0] + this.m[4] * translationVector.v[1] + this.m[8] * translationVector.v[2] + this.m[12],
                this.m[1] * translationVector.v[0] + this.m[5] * translationVector.v[1] + this.m[9] * translationVector.v[2] + this.m[13],
                this.m[2] * translationVector.v[0] + this.m[6] * translationVector.v[1] + this.m[10] * translationVector.v[2] + this.m[14],
                this.m[3] * translationVector.v[0] + this.m[7] * translationVector.v[1] + this.m[11] * translationVector.v[2] + this.m[15]);
    }

    public Matrix4 scale(float sx, float sy, float sz) {
        return new Matrix4(this.m[0] * sx, this.m[1] * sx, this.m[2] * sx, this.m[3] * sx,
                this.m[4] * sy, this.m[5] * sy, this.m[6] * sy, this.m[7] * sy,
                this.m[8] * sz, this.m[9] * sz, this.m[10] * sz, this.m[11] * sz,
                this.m[12], this.m[13], this.m[14], this.m[15]);
    }

    public Matrix4 scale(Vector3 scaleVector) {
        return new Matrix4(this.m[0] * scaleVector.v[0], this.m[1] * scaleVector.v[0], this.m[2] * scaleVector.v[0], this.m[3] * scaleVector.v[0],
                this.m[4] * scaleVector.v[1], this.m[5] * scaleVector.v[1], this.m[6] * scaleVector.v[1], this.m[7] * scaleVector.v[1],
                this.m[8] * scaleVector.v[2], this.m[9] * scaleVector.v[2], this.m[10] * scaleVector.v[2], this.m[11] * scaleVector.v[2],
                this.m[12], this.m[13], this.m[14], this.m[15]);
    }

    public Matrix4 scale(Vector4 scaleVector) {
        return new Matrix4(this.m[0] * scaleVector.v[0], this.m[1] * scaleVector.v[0], this.m[2] * scaleVector.v[0], this.m[3] * scaleVector.v[0],
                this.m[4] * scaleVector.v[1], this.m[5] * scaleVector.v[1], this.m[6] * scaleVector.v[1], this.m[7] * scaleVector.v[1],
                this.m[8] * scaleVector.v[2], this.m[9] * scaleVector.v[2], this.m[10] * scaleVector.v[2], this.m[11] * scaleVector.v[2],
                this.m[12], this.m[13], this.m[14], this.m[15]);
    }

    public Matrix4 rotate(float radians, float x, float y, float z) {
        Matrix4 rm = Matrix4.makeRotation(radians, x, y, z);
        return this.multiply(rm);
    }

    public Matrix4 rotate(Quaternion quaternion) {
        return this.multiply(new Matrix4(quaternion));
    }

    public Matrix4 rotate3(float radians, Vector3 axisVector) {
        Matrix4 rm = Matrix4.makeRotation(radians, axisVector.v[0], axisVector.v[1], axisVector.v[2]);
        return this.multiply(rm);
    }

    public Matrix4 rotate(float radians, Vector4 axisVector) {
        Matrix4 rm = Matrix4.makeRotation(radians, axisVector.v[0], axisVector.v[1], axisVector.v[2]);
        return this.multiply(rm);
    }

    public Matrix4 rotateX(float radians) {
        Matrix4 rm = Matrix4.makeXRotation(radians);
        return this.multiply(rm);
    }

    public Matrix4 rotateY(float radians) {
        Matrix4 rm = Matrix4.makeYRotation(radians);
        return this.multiply(rm);
    }

    public Matrix4 rotateZ(float radians) {
        Matrix4 rm = Matrix4.makeZRotation(radians);
        return this.multiply(rm);
    }

    public Vector3 multiply(Vector3 vectorRight) {
        Vector4 v4 = this.multiply(new Vector4(vectorRight.v[0], vectorRight.v[1], vectorRight.v[2], 0.0f));
        return new Vector3(v4.v[0], v4.v[1], v4.v[2]);
    }

    public Vector3 multiplyWithTranslation(Vector3 vectorRight) {
        Vector4 v4 = this.multiply(new Vector4(vectorRight.v[0], vectorRight.v[1], vectorRight.v[2], 1.0f));
        return new Vector3(v4.v[0], v4.v[1], v4.v[2]);
    }

    public Vector3 multiplyAndProjectVector3(Vector3 vectorRight) {
        Vector4 v4 = this.multiply(new Vector4(vectorRight.v[0], vectorRight.v[1], vectorRight.v[2], 1.0f));
        return new Vector3(v4.v[0], v4.v[1], v4.v[2]).multiplyScalar(1.0f / v4.v[3]);
    }

    public void multiply(Vector3[] vectors) {
        for (int i = 0; i < vectors.length; i++)
            vectors[i] = this.multiply(vectors[i]);
    }

    public void multiplyArrayWithTranslation(Vector3[] vectors) {
        for (int i=0; i < vectors.length; i++)
            vectors[i] = this.multiplyWithTranslation(vectors[i]);
    }

    public void multiplyAndProjectVector3Array(Vector3[] vectors) {
        for (int i = 0; i < vectors.length; i++)
            vectors[i] = this.multiplyAndProjectVector3(vectors[i]);
    }

    public Vector4 multiply(Vector4 vectorRight) {
        Vector4 v = new Vector4(this.m[0] * vectorRight.v[0] + this.m[4] * vectorRight.v[1] + this.m[8] * vectorRight.v[2] + this.m[12] * vectorRight.v[3],
                this.m[1] * vectorRight.v[0] + this.m[5] * vectorRight.v[1] + this.m[9] * vectorRight.v[2] + this.m[13] * vectorRight.v[3],
                this.m[2] * vectorRight.v[0] + this.m[6] * vectorRight.v[1] + this.m[10] * vectorRight.v[2] + this.m[14] * vectorRight.v[3],
                this.m[3] * vectorRight.v[0] + this.m[7] * vectorRight.v[1] + this.m[11] * vectorRight.v[2] + this.m[15] * vectorRight.v[3]);
        return v;
    }

    public void multiply(Vector4[] vectors) {
        for (int i=0; i < vectors.length; i++)
            vectors[i] = this.multiply(vectors[i]);
    }

    /**
     * Calculates the inverse transpose of the matrix. Code adapted from PMatrix3D's invert function.
     * @return the inverse transpose of the matrix.
     */
    public Matrix4 inverseTranspose() {
        float determinant = determinant();
        if (determinant == 0) {
            throw new RuntimeException("The matrix " + this + " is not invertible.");
        }

        Matrix4 result = new Matrix4();

    // first row
        float t00 =  determinant3x3(m[5], m[9], m[13], m[6], m[10], m[14], m[7], m[11], m[15]);
        float t01 = -determinant3x3(m[1], m[9], m[13], m[2], m[10], m[14], m[3], m[11], m[15]);
        float t02 =  determinant3x3(m[1], m[5], m[13], m[2], m[6], m[14], m[3], m[7], m[15]);
        float t03 = -determinant3x3(m[1], m[5], m[9], m[2], m[6], m[10], m[3], m[7], m[11]);

// second row
        float t10 = -determinant3x3(m[4], m[8], m[12], m[6], m[10], m[14], m[7], m[11], m[15]);
        float t11 =  determinant3x3(m[0], m[8], m[12], m[2], m[10], m[14], m[3], m[11], m[15]);
        float t12 = -determinant3x3(m[0], m[4], m[12], m[2], m[6], m[14], m[3], m[7], m[15]);
        float t13 =  determinant3x3(m[0], m[4], m[8], m[2], m[6], m[10], m[3], m[7], m[11]);

// third row
        float t20 =  determinant3x3(m[4], m[8], m[12], m[5], m[9], m[13], m[7], m[11], m[15]);
        float t21 = -determinant3x3(m[0], m[8], m[12], m[1], m[9], m[13], m[3], m[11], m[15]);
        float t22 =  determinant3x3(m[0], m[4], m[12], m[1], m[5], m[13], m[3], m[7], m[15]);
        float t23 = -determinant3x3(m[0], m[4], m[8], m[1], m[5], m[9], m[3], m[7], m[11]);

// fourth row
        float t30 = -determinant3x3(m[4], m[8], m[12], m[5], m[9], m[13], m[6], m[10], m[14]);
        float t31 =  determinant3x3(m[0], m[8], m[12], m[1], m[9], m[13], m[2], m[10], m[14]);
        float t32 = -determinant3x3(m[0], m[4], m[12], m[1], m[5], m[13], m[2], m[6], m[14]);
        float t33 =  determinant3x3(m[0], m[4], m[8], m[1], m[5], m[9], m[2], m[6], m[10]);

        // divide by the determinant
        result.m[0] = t00 / determinant;
        result.m[4] = t10 / determinant;
        result.m[8] = t20 / determinant;
        result.m[12] = t30 / determinant;

        result.m[1] = t01 / determinant;
        result.m[5] = t11 / determinant;
        result.m[9] = t21 / determinant;
        result.m[13] = t31 / determinant;

        result.m[2] = t02 / determinant;
        result.m[6] = t12 / determinant;
        result.m[10] = t22 / determinant;
        result.m[14] = t32 / determinant;

        result.m[3] = t03 / determinant;
        result.m[7] = t13 / determinant;
        result.m[11] = t23 / determinant;
        result.m[15] = t33 / determinant;

        return result;
    }


    /**
     * Calculate the determinant of a 3x3 matrix.
     * @return result
     */
    private float determinant3x3(float t00, float t01, float t02,
                                 float t10, float t11, float t12,
                                 float t20, float t21, float t22) {
        return (t00 * (t11 * t22 - t12 * t21) +
                t01 * (t12 * t20 - t10 * t22) +
                t02 * (t10 * t21 - t11 * t20));
    }


    /**
     * @return the determinant of the matrix
     */public float determinant() {
        float f =
            m[0]
                    * ((m[5] * m[10] * m[15] + m[9] * m[14] * m[7] + m[13] * m[6] * m[11])
                    - m[13] * m[10] * m[7]
                    - m[5] * m[14] * m[11]
                    - m[9] * m[6] * m[15]);
        f -= m[4]
                * ((m[1] * m[10] * m[15] + m[9] * m[14] * m[3] + m[13] * m[2] * m[11])
                - m[13] * m[10] * m[3]
                - m[1] * m[14] * m[11]
                - m[9] * m[2] * m[15]);
        f += m[8]
                * ((m[1] * m[6] * m[15] + m[5] * m[14] * m[3] + m[13] * m[2] * m[7])
                - m[13] * m[6] * m[3]
                - m[1] * m[14] * m[7]
                - m[5] * m[2] * m[15]);
        f -= m[12]
                * ((m[1] * m[6] * m[11] + m[5] * m[10] * m[3] + m[9] * m[2] * m[7])
                - m[9] * m[6] * m[3]
                - m[1] * m[10] * m[7]
                - m[5] * m[2] * m[11]);
        return f;
    }

    /**
     * Returns the PMatrix representation of this matrix
     * Note: PMatrix is row-major, whereas this is column major. Therefore, we want to pass the transpose.
     * @return a PMatrix3D version of this matrix.
     */
    public PMatrix3D toPMatrix() {
        return new PMatrix3D(
                this.m[0], this.m[4], this.m[8], this.m[12],
                this.m[1], this.m[5], this.m[9], this.m[13],
                this.m[2], this.m[6], this.m[10], this.m[14],
                this.m[3], this.m[7], this.m[11], this.m[15]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Matrix4 matrix4 = (Matrix4) o;

        return Arrays.equals(m, matrix4.m);

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
        for (int i = 0; i < 16; i++) {
            stringBuilder.append(this.m[column * 4 + row] + ", ");
            column++;
            if (column == 4) {
                column = 0;
                row++;
                stringBuilder.append("\n");
            }
        }
        stringBuilder.append(")\n");
        return stringBuilder.toString();
    }
}
