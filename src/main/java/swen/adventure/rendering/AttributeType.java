package swen.adventure.rendering;

import java.nio.ByteBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.GL_HALF_FLOAT;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 20/09/15.
 */
public enum AttributeType {

    Float(false, GL_FLOAT, 4),
    Half(false, GL_HALF_FLOAT, 2),
    Int(false, GL_INT, 4),
    UInt(false, GL_UNSIGNED_INT, 4),
    NormalisedInt(true, GL_INT, 4),
    NormalisedUInt(true, GL_UNSIGNED_INT, 4),
    Short(false, GL_SHORT, 2),
    UShort(false, GL_UNSIGNED_SHORT, 2),
    NormalisedShort(true, GL_SHORT, 2),
    NormalisedUShort(true, GL_UNSIGNED_SHORT, 2),
    Byte(false, GL_BYTE, 1),
    UnsignedByte(false, GL_UNSIGNED_BYTE, 1),
    NormalisedByte(true, GL_BYTE, 1),
    NormalisedUnsignedByte(true, GL_UNSIGNED_BYTE, 1);

    public final boolean isNormalised;
    public final int glType;
    public final int sizeInBytes;

    AttributeType(boolean isNormalised, int glType, int sizeInBytes) {
        this.isNormalised = isNormalised;
        this.glType = glType;
        this.sizeInBytes = sizeInBytes;
    }

    @SuppressWarnings("unchecked")
    public void writeToBuffer(ByteBuffer buffer, List<?> dataArray, int componentsPerStride, int offset, int stride, AttributeType type) {
        switch (type) {
            case Float:
                this.writeToFloatBuffer(buffer, (List<Float>)dataArray, componentsPerStride, offset, stride);
                break;
            case Half:
            case Short:
            case UShort:
            case NormalisedShort:
            case NormalisedUShort:
                this.writeToShortBuffer(buffer, (List<Short>)dataArray, componentsPerStride, offset, stride);
                break;
            case Int:
            case UInt:
            case NormalisedInt:
            case NormalisedUInt:
                this.writeToIntBuffer(buffer, (List<Integer>)dataArray, componentsPerStride, offset, stride);
                break;
            case Byte:
            case UnsignedByte:
            case NormalisedByte:
            case NormalisedUnsignedByte:
                this.writeToByteBuffer(buffer, (List<Byte>)dataArray, componentsPerStride, offset, stride);
                break;
        }
    }

    private void writeToByteBuffer(ByteBuffer buffer, List<Byte> dataArray, int componentsPerStride, int offset, int stride) {
        int component = 0;
        int strideIndex = 0;

        for (Byte element : dataArray) {
            int index = offset + component + stride * strideIndex;
            buffer.put(index, element);

            component++;
            if (component == componentsPerStride) {
                component = 0;
                strideIndex++;
            }
        }
    }

    private void writeToIntBuffer(ByteBuffer buffer, List<Integer> dataArray, int componentsPerStride, int offset, int stride) {
        int component = 0;
        int strideIndex = 0;

        for (Integer element : dataArray) {
            int index = offset + 4 * component + stride * strideIndex;
            buffer.putInt(index, element);

            component++;
            if (component == componentsPerStride) {
                component = 0;
                strideIndex++;
            }
        }
    }

    private void writeToShortBuffer(ByteBuffer buffer, List<Short> dataArray, int componentsPerStride, int offset, int stride) {
        int component = 0;
        int strideIndex = 0;

        for (Short element : dataArray) {
            int index = offset + 2 * component + stride * strideIndex;
            buffer.putShort(index, element);

            component++;
            if (component == componentsPerStride) {
                component = 0;
                strideIndex++;
            }
        }
    }

    private void writeToFloatBuffer(ByteBuffer buffer, List<Float> dataArray, int componentsPerStride, int offset, int stride) {
        int component = 0;
        int strideIndex = 0;

        for (Float element : dataArray) {
            int index = offset + 4 * component + stride * strideIndex;
            buffer.putFloat(index, element);

            component++;
            if (component == componentsPerStride) {
                component = 0;
                strideIndex++;
            }
        }
    }
}
