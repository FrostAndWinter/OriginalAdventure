package swen.adventure.rendering;

import swen.adventure.rendering.maths.Matrix3;
import swen.adventure.rendering.maths.Matrix4;
import swen.adventure.rendering.shaders.GaussianPerObjectMaterialShader;
import swen.adventure.scenegraph.CameraNode;
import swen.adventure.scenegraph.Light;
import swen.adventure.scenegraph.MeshNode;
import swen.adventure.scenegraph.SceneNode;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 19/09/15.
 */
public class GLRenderer {

    private GaussianPerObjectMaterialShader _defaultShader;
    private int _vertexArrayRef;

    private Matrix4 _perspectiveMatrix;

    public GLRenderer(int width, int height) {
        _defaultShader = new GaussianPerObjectMaterialShader();

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);

        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDepthFunc(GL_LEQUAL);
        glDepthRange(0.0f, 1.0f);
        glEnable(GL_DEPTH_CLAMP);

        this.setSize(width, height);
    }

    public Matrix4 perspectiveMatrix(int width, int height) {
        float cameraFOV = (float)Math.PI/3.f;
        float cameraNear = 1.f;
        float cameraFar = 10000.f;
        float cameraAspect = width / (float) height;

        return Matrix4.makePerspective(cameraFOV, cameraAspect, cameraNear, cameraFar);
    }

    public void setSize(int width, int height) {
        _perspectiveMatrix = this.perspectiveMatrix(width, height);

        _defaultShader.useProgram();
        _defaultShader.setCameraToClipMatrix(_perspectiveMatrix);
        _defaultShader.endUseProgram();
    }


    public void render(SceneNode sceneGraph, CameraNode cameraNode) {
        _defaultShader.useProgram();

        _defaultShader.setGamma(2.2f);
        _defaultShader.setMaxIntensity(12.f);
        _defaultShader.setLightData(Light.toLightBlock(sceneGraph.allLights(), cameraNode.worldToNodeSpaceTransform()));

        Matrix4 worldToCameraMatrix = cameraNode.worldToNodeSpaceTransform();

        sceneGraph.traverse((node) -> {
            if (node instanceof MeshNode) {
                MeshNode meshNode = (MeshNode)node;

                Matrix4 nodeToCameraSpaceTransform = worldToCameraMatrix.multiply(node.nodeToWorldSpaceTransform());
                Matrix3 normalModelToCameraSpaceTransform = nodeToCameraSpaceTransform.getMatrix3().inverse().transpose();

                _defaultShader.setModelToCameraMatrix(nodeToCameraSpaceTransform);
                _defaultShader.setNormalModelToCameraMatrix(normalModelToCameraSpaceTransform);

                _defaultShader.setMaterial(meshNode.material().toFloatBuffer());

                meshNode.render();
            }
        });

        _defaultShader.endUseProgram();
    }
}
