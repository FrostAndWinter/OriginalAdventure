package swen.adventure.rendering;

import com.jogamp.opengl.GL3;
import javafx.scene.shape.Mesh;
import processing.opengl.PGraphics3D;
import processing.opengl.PJOGL;
import swen.adventure.rendering.maths.Matrix4;
import swen.adventure.rendering.maths.Vector4;
import swen.adventure.scenegraph.MeshNode;
import swen.adventure.scenegraph.SceneNode;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 19/09/15.
 */
public class GLRenderer {

    private static final String FragmentShader = "#version 330\n" +
                        "smooth in vec4 interpColor;\n" +
                        "out vec4 outputColor;\n" +
                        "void main() {\n" +
                        "outputColor = interpColor;\n" +
                        "}";

    private static final String VertexShader = "#version 330\n" +
            "\n" +
            "layout(location = 0) in vec4 position;\n" +
            "\n" +
            "smooth out vec4 interpColor;\n" +
            "\n" +
            "uniform mat4 modelToClipMatrix;\n" +
            "uniform vec4 colour;\n" +
            "\n" +
            "void main() {\n" +
            "\tgl_Position = modelToClipMatrix * position;\n" +
            "\tinterpColor = colour;\n" +
            "}\n";


    private PGraphics3D _graphicsContext;
    private PJOGL _glContext;
    private GL3 _gl;

    private ShaderProgram _defaultProgram;
    private int _vertexArrayRef;

    public GLRenderer(PGraphics3D graphicsContext) {
        _graphicsContext = graphicsContext;
        _glContext = (PJOGL)graphicsContext.pgl;
        _gl = (GL3)_glContext.gl;

        _defaultProgram = new ShaderProgram(_gl, VertexShader, FragmentShader);

        _gl.glDisable(GL3.GL_CULL_FACE);
        //_gl.glCullFace(GL3.GL_BACK);
      // _gl.glFrontFace(GL3.GL_CCW);
      _gl.glDisable(GL3.GL_DEPTH_TEST);
      //  _gl.glDepthMask(true);
     //   _gl.glDepthFunc(GL3.GL_LEQUAL);
     // _gl.glDepthRange(0.0f, 1.0f);
     //_gl.glEnable(GL3.GL_DEPTH_CLAMP);
    }

    public Matrix4 perspectiveMatrix() {
        float cameraFOV = (float)Math.PI/3.f;
        float cameraNear = 1.f;
        float cameraFar = 10000.f;
        float cameraAspect = (float) _graphicsContext.width / (float) _graphicsContext.height;

        Matrix4 perspectiveMatrix = Matrix4.makePerspective(cameraFOV, cameraAspect, cameraNear, cameraFar);

        return perspectiveMatrix;
    }

    public void render(SceneNode sceneGraph) {

        _graphicsContext.background(0);
        _graphicsContext.beginPGL();

        _gl.glUseProgram(_defaultProgram.glProgramRef);


        Matrix4 cameraToClipMatrix = this.perspectiveMatrix();
        Matrix4 worldToCameraMatrix = Matrix4.makeTranslation(0, -30.f, 0.f);

        sceneGraph.traverse((node) -> {
            if (node instanceof MeshNode) {
              Matrix4 nodeToClipSpaceTransform = cameraToClipMatrix.multiply(worldToCameraMatrix).multiply(node.nodeToWorldSpaceTransform());

                _gl.glUniformMatrix4fv(_defaultProgram.modelToClipMatrixUniformRef, 1, false, FloatBuffer.wrap(nodeToClipSpaceTransform.m));

                int rgb = ((MeshNode)node).mesh().getFill(0);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                _gl.glUniform4f(_defaultProgram.colourUniformRef, red/255.f, green/255.f, blue/255.f, 1.f);

                ((MeshNode)node).render(_gl);

            }
        });

        _graphicsContext.endPGL();
    }
}
