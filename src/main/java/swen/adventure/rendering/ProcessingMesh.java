package swen.adventure.rendering;

import com.jogamp.opengl.GL3;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;
import swen.adventure.rendering.maths.Matrix4;
import swen.adventure.rendering.maths.Vector3;
import swen.adventure.rendering.maths.Vector4;
import swen.adventure.scenegraph.SceneNode;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 */
public class ProcessingMesh extends SceneNode {

    private PShape _mesh;
    private int _vertexArrayObjectRef;
    private int _vertexPositionBufferRef;
    private boolean _glInitialised = false;

    public ProcessingMesh(String id, final SceneNode parent, PShape mesh) {
        super(id, parent, false);
        _mesh = mesh;
    }

    private void setupGL(GL3 gl) {

        IntBuffer arrayObjectRefBuffer = IntBuffer.allocate(1);
        gl.glGenVertexArrays(1, arrayObjectRefBuffer);
        _vertexArrayObjectRef = arrayObjectRefBuffer.get();
        gl.glBindVertexArray(_vertexArrayObjectRef);


        IntBuffer vertexPositionBuffer = IntBuffer.allocate(1);
        gl.glGenBuffers(1, vertexPositionBuffer);
        _vertexPositionBufferRef = vertexPositionBuffer.get();

        float[] vertexPositions = this.verticesFromPShape(_mesh);
        FloatBuffer positionBuffer = FloatBuffer.wrap(vertexPositions);

        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, _vertexPositionBufferRef); //bind our vertex buffer
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, vertexPositions.length * 4, positionBuffer, this.isDynamic() ? GL3.GL_STREAM_DRAW : GL3.GL_STATIC_DRAW); //FIXME these probably aren't the right draw modes.
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0); //unbind the buffer

        gl.glBindVertexArray(0);

        _glInitialised = true;
    }

    private float[] verticesFromPShape(PShape shape) {
        float[] vertices = new float[shape.getVertexCount() * 3];
        PVector vector = new PVector();
        for (int i = 0; i < shape.getVertexCount(); i++) {
            shape.getVertex(i, vector);
            vertices[i * 3] = vector.x;
            vertices[i * 3 + 1] = vector.y;
            vertices[i * 3 + 2] = vector.z;
        }

        return vertices;
    }

    public PShape mesh() {
        return _mesh;
    }

    public void render(GL3 gl) {
        if (!_glInitialised) {
            this.setupGL(gl);
        }

        gl.glBindVertexArray(_vertexArrayObjectRef);
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, _vertexPositionBufferRef);

        gl.glEnableVertexAttribArray(0);

        gl.glVertexAttribPointer(0, 3, GL3.GL_FLOAT, false, 0, 0);

        gl.glDrawArrays(GL3.GL_TRIANGLE_FAN, 0, _mesh.getVertexCount());

        gl.glDisableVertexAttribArray(0);
        gl.glBindVertexArray(0);

    }
}
