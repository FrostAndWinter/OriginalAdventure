package swen.adventure.rendering;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.glBufferSubData;
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
    public void writeToBuffer(int glBuffer, List<?> dataArray, int offset, AttributeType type) {
        switch (type) {
            case Float:
                this.writeToFloatBuffer(glBuffer, (List<Float>)dataArray, offset);
                break;
            case Half:
            case Short:
            case UShort:
            case NormalisedShort:
            case NormalisedUShort:
                this.writeToShortBuffer(glBuffer, (List<Short>)dataArray, offset);
                break;
            case Int:
            case UInt:
            case NormalisedInt:
            case NormalisedUInt:
                this.writeToIntBuffer(glBuffer, (List<Integer>)dataArray, offset);
                break;
            case Byte:
            case UnsignedByte:
            case NormalisedByte:
            case NormalisedUnsignedByte:
                this.writeToByteBuffer(glBuffer, (List<Byte>)dataArray, offset);
                break;
        }
    }

    private void writeToByteBuffer(int glBuffer, List<Byte> dataArray, int offset) {

        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(dataArray.size());
        for (Byte element : dataArray) {
            byteBuffer.put(element);
        }
        byteBuffer.flip();

        glBufferSubData(glBuffer, offset, byteBuffer);
    }

    private void writeToIntBuffer(int glBuffer, List<Integer> dataArray, int offset) {

        IntBuffer intBuffer = BufferUtils.createIntBuffer(dataArray.size());

        for (Integer element : dataArray) {
            intBuffer.put(element);
        }
        intBuffer.flip();

        glBufferSubData(glBuffer, offset, intBuffer);
    }

    private void writeToShortBuffer(int glBuffer, List<Short> dataArray, int offset) {

        ShortBuffer shortBuffer = BufferUtils.createShortBuffer(dataArray.size());

        for (Short element : dataArray) {
            shortBuffer.put(element);
        }
        shortBuffer.flip();

        glBufferSubData(glBuffer, offset, shortBuffer);
    }

    private void writeToFloatBuffer(int glBuffer, List<Float> dataArray, int offset) {

        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(dataArray.size());

        for (Float element : dataArray) {
            floatBuffer.put(element);
        }
        floatBuffer.flip();

        glBufferSubData(glBuffer, offset, floatBuffer);
    }
}
