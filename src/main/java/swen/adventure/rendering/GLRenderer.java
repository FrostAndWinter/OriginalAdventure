package swen.adventure.rendering;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Util;
import swen.adventure.Utilities;
import swen.adventure.rendering.maths.Matrix4;
import swen.adventure.rendering.maths.Quaternion;
import swen.adventure.rendering.maths.Vector3;
import swen.adventure.rendering.maths.Vector4;
import swen.adventure.rendering.shaders.DirectionalLightShader;
import swen.adventure.scenegraph.CameraNode;
import swen.adventure.scenegraph.MeshNode;
import swen.adventure.scenegraph.SceneNode;
import swen.adventure.scenegraph.TransformNode;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 19/09/15.
 */
public class GLRenderer {

    private DirectionalLightShader _defaultProgram;
    private int _vertexArrayRef;

    private int _width, _height;

    public GLRenderer(int width, int height) {
        _width = width;
        _height = height;

        try {
            _defaultProgram = new DirectionalLightShader(Utilities.readFile(Utilities.pathForResource("VertexShader", "vert")), Utilities.readFile(Utilities.pathForResource("FragmentLighting", "frag")));
        } catch (IOException e) {
            e.printStackTrace();
        }

//        glEnable(GL_CULL_FACE);
//        glCullFace(GL_BACK);
//        glFrontFace(GL_CCW);

        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDepthFunc(GL_LEQUAL);
        glDepthRange(0.0f, 1.0f);
        glEnable(GL_DEPTH_CLAMP);
    }

    public Matrix4 perspectiveMatrix() {
        float cameraFOV = (float)Math.PI/3.f;
        float cameraNear = 1.f;
        float cameraFar = 10000.f;
        float cameraAspect = _width / (float) _height;

        Matrix4 perspectiveMatrix = Matrix4.makePerspective(cameraFOV, cameraAspect, cameraNear, cameraFar);

        return perspectiveMatrix;
    }



    public void render(SceneNode sceneGraph, CameraNode cameraNode) {

        glUseProgram(_defaultProgram.glProgramRef);

        Matrix4 cameraToClipMatrix = this.perspectiveMatrix();
        Matrix4 worldToCameraMatrix = cameraNode.worldToNodeSpaceTransform();

        final Vector3 lightPosition = new Vector3(30, 100, 0);
        Vector3 cameraSpaceLightPosition = worldToCameraMatrix.multiplyWithTranslation(sceneGraph.nodeToWorldSpaceTransform().multiplyWithTranslation(lightPosition));

        glUniformMatrix4(_defaultProgram.cameraToClipMatrixUniformRef, false, cameraToClipMatrix.asFloatBuffer());

        glUniform3f(_defaultProgram.cameraSpaceLightPositionUniformRef, cameraSpaceLightPosition.x, cameraSpaceLightPosition.y, cameraSpaceLightPosition.z);
        glUniform4f(_defaultProgram.ambientLightUniformRef, 0.2f, 0.2f, 0.2f, 1.f);
        glUniform4f(_defaultProgram.lightIntensityUniformRef, 0.8f, 0.8f, 0.8f, 1.f);


        sceneGraph.traverse((node) -> {
            if (node instanceof MeshNode) {
                MeshNode meshNode = (MeshNode)node;

                Matrix4 nodeToCameraSpaceTransform = worldToCameraMatrix.multiply(node.nodeToWorldSpaceTransform());
                Matrix4 normalModelToCameraSpaceTransform = nodeToCameraSpaceTransform.inverseTranspose();

                glUniformMatrix4(_defaultProgram.modelToCameraMatrixUniformRef, false, nodeToCameraSpaceTransform.asFloatBuffer());
                glUniformMatrix4(_defaultProgram.normalModelToCameraMatrixUniformRef, false, normalModelToCameraSpaceTransform.asFloatBuffer());

                Vector4 colour = meshNode.colour().orElse(new Vector4(1.f, 0.f, 0.f, 1.f));
                glUniform4(_defaultProgram.colourUniformRef, colour.asFloatBuffer());

                ((MeshNode) node).render();
            }
        });
    }
}
