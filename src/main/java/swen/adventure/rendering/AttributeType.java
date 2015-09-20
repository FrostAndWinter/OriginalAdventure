package swen.adventure.rendering;

import com.jogamp.opengl.GL3;

import java.nio.*;
import java.util.List;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 20/09/15.
 */
public enum AttributeType {
    
    Float(false, GL3.GL_FLOAT, 4, FloatBuffer::allocate, ((object, buffer) -> ((FloatBuffer)buffer).put((float)object))),
    Half(false, GL3.GL_HALF_FLOAT, 2, ShortBuffer::allocate, ((object, buffer) -> ((ShortBuffer)buffer).put((short)object))),
    Int(false, GL3.GL_INT, 4, IntBuffer::allocate, ((object, buffer) -> ((IntBuffer)buffer).put((int)object))),
    UInt(false, GL3.GL_UNSIGNED_INT, 4, IntBuffer::allocate, ((object, buffer) -> ((IntBuffer)buffer).put((int)object))),
    NormalisedInt(true, GL3.GL_INT, 4, IntBuffer::allocate, ((object, buffer) -> ((IntBuffer)buffer).put((int)object))),
    NormalisedUInt(true, GL3.GL_UNSIGNED_INT, 4, IntBuffer::allocate, ((object, buffer) -> ((IntBuffer)buffer).put((int)object))),
    Short(false, GL3.GL_SHORT, 2, ShortBuffer::allocate, ((object, buffer) -> ((ShortBuffer)buffer).put((short)object))),
    UShort(false, GL3.GL_UNSIGNED_SHORT, 2, ShortBuffer::allocate, ((object, buffer) -> ((ShortBuffer)buffer).put((short)object))),
    NormalisedShort(true, GL3.GL_SHORT, 2, ShortBuffer::allocate, ((object, buffer) -> ((ShortBuffer)buffer).put((short)object))),
    NormalisedUShort(true, GL3.GL_UNSIGNED_SHORT, 2, ShortBuffer::allocate, ((object, buffer) -> ((ShortBuffer)buffer).put((short)object))),
    Byte(false, GL3.GL_BYTE, 1, ByteBuffer::allocate, ((object, buffer) -> ((ByteBuffer)buffer).put((byte)object))),
    UnsignedByte(false, GL3.GL_UNSIGNED_BYTE, 1, ByteBuffer::allocate, ((object, buffer) -> ((ByteBuffer)buffer).put((byte)object))),
    NormalisedByte(true, GL3.GL_BYTE, 1, ByteBuffer::allocate, ((object, buffer) -> ((ByteBuffer)buffer).put((byte)object))),
    NormalisedUnsignedByte(true, GL3.GL_UNSIGNED_BYTE, 1, ByteBuffer::allocate, ((object, buffer) -> ((ByteBuffer)buffer).put((byte)object)));

    interface BufferAllocationMethod {
        Buffer allocate(int capacity);
    }

    interface BufferAdditionMethod<T> {
        void addToBuffer(T object, Buffer buffer);
    }

    public final boolean isNormalised;
    public final int glType;
    public final int sizeInBytes;
    public final BufferAllocationMethod bufferAllocationMethod;
    public final BufferAdditionMethod<?> bufferAdditionMethod;

    AttributeType(boolean isNormalised, int glType, int sizeInBytes, BufferAllocationMethod allocationMethod, BufferAdditionMethod<?> additionMethod) {
        this.isNormalised = isNormalised;
        this.glType = glType;
        this.sizeInBytes = sizeInBytes;
        this.bufferAllocationMethod = allocationMethod;
        this.bufferAdditionMethod = additionMethod;
    }

    public <T> void writeToBuffer(GL3 gl, int glBuffer, List<T> dataArray, int offset) {
        Buffer buffer = this.bufferAllocationMethod.allocate(dataArray.size());
        BufferAdditionMethod<T> additionMethod = (BufferAdditionMethod<T>)this.bufferAdditionMethod;
        for (T element : dataArray) {
            additionMethod.addToBuffer(element, buffer);
        }
        buffer.rewind();

        gl.glBufferSubData(glBuffer, offset, dataArray.size() * this.sizeInBytes, buffer);
    }
}
