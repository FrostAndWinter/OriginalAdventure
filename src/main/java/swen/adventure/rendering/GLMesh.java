package swen.adventure.rendering;

import com.jogamp.opengl.GL3;
import javafx.util.Pair;
import swen.adventure.datastorage.WavefrontParser;
import swen.adventure.rendering.maths.Vector;
import swen.adventure.rendering.maths.Vector3;
import swen.adventure.scenegraph.SceneNode;
import swen.adventure.scenegraph.TransformNode;

import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 20/09/15.
 * Adapted from the ArcSynthesis GL Tutorials (https://bitbucket.org/alfonse/gltut/wiki/Home)
 */
public abstract class GLMesh<T> extends SceneNode {

    static class RenderCommand {
        public final boolean isIndexedCommand;
        public final int primitiveType;
        private int _startIndex;
        private int _elementCount;
        private int _indexDataType; //only applies if isIndexedCommand is true
        private int _primitiveRestart; //only applies if isIndexedCommand is true

        /**
         * Constructs a new non-indexed command.
         * @param primitiveType Either GL_UNSIGNED_BYTE or GL_UNSIGNED_SHORT
         * @param startIndex
         */
        public RenderCommand(int primitiveType, int startIndex, int elementCount) {
            this.isIndexedCommand = false;
            this.primitiveType = primitiveType;
            this._startIndex = startIndex;
            this._elementCount = elementCount;

            if (this._startIndex < 0) {
                throw new RuntimeException("The array start index must be 0 or greater");
            }
            if (this._elementCount < 1) {
                throw new RuntimeException("The array count must be 1 or greater.");
            }
        }

        /**
         * Constructs a new indexed command. The _startIndex, _elementCount, _indexDataType, and _primitiveRestart fields need to be filled in before use.
         * @param primitiveType Either GL_UNSIGNED_BYTE or GL_UNSIGNED_SHORT
         * @param primitiveRestart
         */
        public RenderCommand(int primitiveType, int primitiveRestart) {

            this.primitiveType = primitiveType;
            this.isIndexedCommand = true;
            _primitiveRestart = primitiveRestart;
        }

        public void render(GL3 gl) {
            if (this.isIndexedCommand) {
                gl.glDrawElements(primitiveType, _elementCount, _indexDataType, _startIndex);
            } else {
                gl.glDrawArrays(primitiveType, _startIndex, _elementCount);
            }

        }

        public void setStartIndex(final int startIndex) {
            _startIndex = startIndex;
        }

        public void setElementCount(final int elementCount) {
            _elementCount = elementCount;
        }

        public void setIndexDataType(final int indexDataType) {
            _indexDataType = indexDataType;
        }
    }

    /**
     * An attribute is an abstraction around an array of primitives.
     */
     class Attribute {
            public final int attributeIndex;
            public final AttributeType attributeType;
            public final int numberOfComponents;
            public final boolean isIntegral;
            public final List<T> data;

        public Attribute(int attributeIndex, int numberOfComponents, AttributeType attributeType, boolean isIntegral, List<T> data) {
            this.attributeIndex = attributeIndex;

            if(!((0 < numberOfComponents) && (numberOfComponents < 5))) {
                throw new RuntimeException("Attribute size must be between 1 and 4.");
            }

            this.numberOfComponents = numberOfComponents;
            this.attributeType = attributeType;

            this.isIntegral = isIntegral;
            if (isIntegral) {

                if (this.attributeType.isNormalised) {
                    throw new RuntimeException("Attribute cannot be both 'integral' and a normalized 'type'.");
                }
            }

            this.data = data;

            if (data.isEmpty())
                throw new RuntimeException("The attribute must have an array of values.");
            if (data.size() % this.numberOfComponents != 0)
                throw new RuntimeException("The attribute's data must be a multiple of its size in elements.");
        }

            public Attribute(final Attribute rhs) {
                this.attributeIndex = rhs.attributeIndex;
                this.attributeType = rhs.attributeType;
                this.numberOfComponents = rhs.numberOfComponents;
                this.isIntegral = rhs.isIntegral;
                this.data = new ArrayList<>(rhs.data);
            }


            public int sizeInBytes() {
                return this.data.size() * this.attributeType.sizeInBytes;
            }

            public int sizePerElement() {
                return this.numberOfComponents * this.attributeType.sizeInBytes;
            }

            public void fillBoundBufferObject(GL3 gl, int offset) {
                this.attributeType.writeToBuffer(gl, GL3.GL_ARRAY_BUFFER, this.data, offset);
            }

            public void setupAttributeArray(GL3 gl, int offset)  {
                gl.glEnableVertexAttribArray(this.attributeIndex); //TODO This should really be interleaved data (see Tut 13 - Purloined Primitives in the Modern Graphics Programming Book).
                if (this.isIntegral)  {
                    gl.glVertexAttribIPointer(this.attributeIndex, this.numberOfComponents, this.attributeType.glType,
                            0, offset);
                } else {
                    gl.glVertexAttribPointer(this.attributeIndex, this.numberOfComponents,
                            this.attributeType.glType, this.attributeType.isNormalised,
                            0, offset);
                }
            }

        }

    static class IndexData<U> {
        public final AttributeType attributeType;
        public final List<U> data;

        public IndexData(List<U> data, AttributeType attributeType) {
            this.attributeType = attributeType;
            this.data = data;
        }

        public int sizeInBytes() {
            return data.size() * attributeType.sizeInBytes;
        }

        public void fillBoundBufferObject(GL3 gl, int offset) {
            this.attributeType.writeToBuffer(gl, GL3.GL_ELEMENT_ARRAY_BUFFER, this.data, offset);

        }
    }

    static class NamedVertexArrayObject {
        public final String name;
        public final List<Integer> attributeIndices;

        public NamedVertexArrayObject(final String name, final List<Integer> attributeIndices) {
            this.name = name;
            this.attributeIndices = attributeIndices;
        }
    }

    private int _attributeArrraysBufferRef = 0;
    private int _indexBufferRef = 0;
    private int _vertexArrayObjectRef = 0;

    private List<RenderCommand> _primitives;
    private Map<String, Integer> _namedVAOs = new HashMap<>();

    public GLMesh(String id, TransformNode parent) {
        super(id, parent, false);
    }

    protected void initialise(GL3 gl, List<Attribute> attributes, List<IndexData<?>> indexData, List<NamedVertexArrayObject> namedVAOList, List<RenderCommand> primitives) {

        _primitives = primitives;

        //Figure out how big of a buffer object for the attribute data we need.
        int attributeBufferSize = 0;
        int[] attribStartLocs = new int[attributes.size()];


        int numberOfComponents = 0;
        for(int i = 0; i < attributes.size(); i++) {
            attributeBufferSize = attributeBufferSize % 16 != 0 ?
                    (attributeBufferSize + (16 - attributeBufferSize % 16)) : attributeBufferSize; //Make sure that the buffer is a workable number of bytes.

            attribStartLocs[i] = attributeBufferSize;

            Attribute attribute = attributes.get(i);
            attributeBufferSize += attribute.sizeInBytes();

            if (numberOfComponents != 0) {
                if(numberOfComponents != attribute.numberOfComponents)
                    throw new RuntimeException("Some of the attribute arrays have different element counts.");
            } else {
                numberOfComponents = attribute.numberOfComponents;
            }
        }

        IntBuffer buffer = IntBuffer.allocate(1);

        //Create the "Everything" VAO.
        gl.glGenVertexArrays(1, buffer);
        _vertexArrayObjectRef = buffer.get(0);
        gl.glBindVertexArray(_vertexArrayObjectRef);

        //Create the buffer object.
        gl.glGenBuffers(1, buffer);
        _attributeArrraysBufferRef = buffer.get(0);
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, _attributeArrraysBufferRef);
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, attributeBufferSize, null, GL3.GL_STATIC_DRAW);

        //Fill in our data and set up the attribute arrays.
        for(int i = 0; i < attributes.size(); i++) {
            Attribute attribute = attributes.get(i);
            attribute.fillBoundBufferObject(gl, attribStartLocs[i]);
            attribute.setupAttributeArray(gl, attribStartLocs[i]);
        }

        //Fill the named VAOs.
        for (NamedVertexArrayObject namedVao : namedVAOList) {

            int vao = -1;
            gl.glGenVertexArrays(1, buffer);
            vao = buffer.get(0);
            gl.glBindVertexArray(vao);

            for (int attributeIndex = 0; attributeIndex < namedVao.attributeIndices.size(); attributeIndex++) {
                int attributeRef = namedVao.attributeIndices.get(attributeIndex);
                int attributeOffset = -1;
                for (int count = 0; count < attributes.size(); count++) {
                    if (attributes.get(count).attributeIndex == attributeRef) {
                        attributeOffset = count;
                        break;
                    }
                }

                Attribute attribute = attributes.get(attributeOffset);
                attribute.setupAttributeArray(gl, attribStartLocs[attributeOffset]);
            }

            _namedVAOs.put(namedVao.name, vao);
        }

        gl.glBindVertexArray(0);

        //Get the size of our index buffer data.
        int indexBufferSize = 0;
        int[] indexStartLocs = new int[indexData.size()];

        for (int i = 0; i < indexData.size(); i++) {
            indexBufferSize = indexBufferSize % 16 != 0 ?
                    (indexBufferSize + (16 - indexBufferSize % 16)) : indexBufferSize; //Again, make sure we're aligned to boundaries.

            indexStartLocs[i] = indexBufferSize;
            IndexData data = indexData.get(i);

            indexBufferSize += data.sizeInBytes();
        }

        //Create the index buffer object.
        if (indexBufferSize > 0) {
            gl.glBindVertexArray(_vertexArrayObjectRef);

            gl.glGenBuffers(1, buffer);
            _indexBufferRef = buffer.get(0);
            gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, _indexBufferRef);
            gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, indexBufferSize, null, GL3.GL_STATIC_DRAW);

            //Fill with data.
            for (int i = 0; i < indexData.size(); i++) {
                IndexData data = indexData.get(i);
                data.fillBoundBufferObject(gl, indexStartLocs[i]);
            }

            //Fill in indexed rendering commands.
            int currentIndexed = 0;
            for (RenderCommand primitive : _primitives) {
                if(primitive.isIndexedCommand) {
                    primitive.setStartIndex(indexStartLocs[currentIndexed]);
                    primitive.setElementCount(indexData.get(currentIndexed).data.size());
                    primitive.setIndexDataType(indexData.get(currentIndexed).attributeType.glType);
                    currentIndexed++;
                }
            }

            for (int vao : _namedVAOs.values()) {
                gl.glBindVertexArray(vao);
                gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, _indexBufferRef);
            }

            gl.glBindVertexArray(0);
        }

    }

    public void render(GL3 gl) {
        if (_vertexArrayObjectRef == 0) {
            return;
        }

        gl.glBindVertexArray(_vertexArrayObjectRef);

        for (RenderCommand primitive : _primitives) {
            primitive.render(gl);
        }

        gl.glBindVertexArray(0);
    }

    public void render(GL3 gl, String vertexArrayObjectName) {
        Integer vaoObj = _namedVAOs.get(vertexArrayObjectName);
        if (vaoObj == null) {
            return;
        }
        int vao = vaoObj;

        gl.glBindVertexArray(vao);

        for (RenderCommand primitive : _primitives) {
            primitive.render(gl);
        }

        gl.glBindVertexArray(0);
    }


}
