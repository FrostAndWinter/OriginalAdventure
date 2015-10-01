package swen.adventure.engine.rendering;

import swen.adventure.engine.rendering.maths.Matrix3;
import swen.adventure.engine.rendering.maths.Matrix4;
import swen.adventure.engine.rendering.shaders.GaussianPerObjectMaterialShader;
import swen.adventure.engine.scenegraph.CameraNode;
import swen.adventure.engine.scenegraph.Light;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.SceneNode;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 19/09/15.
 */
public class GLRenderer {

    private GaussianPerObjectMaterialShader _defaultShader;
    private int _width, _height;
    private float _currentFOV = (float)Math.PI/3.f;

    public GLRenderer(int width, int height) {
        _defaultShader = new GaussianPerObjectMaterialShader();

        this.setSize(width, height);
    }

    public Matrix4 perspectiveMatrix(int width, int height, float fieldOfView) {;
        float cameraNear = 1.f;
        float cameraFar = 10000.f;
        float cameraAspect = width / (float) height;

        return Matrix4.makePerspective(fieldOfView, cameraAspect, cameraNear, cameraFar);
    }

    private void setProjectionMatrix() {

        _defaultShader.useProgram();
        _defaultShader.setCameraToClipMatrix(this.perspectiveMatrix(_width, _height, _currentFOV));
        _defaultShader.endUseProgram();
    }

    public void setSize(int width, int height) {
        _width = width; _height = height;
        this.setProjectionMatrix();
    }

    /**
     * Setup GL state for rendering.
     */
    private void preRender() {
        glEnable(GL_FRAMEBUFFER_SRGB);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);

        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDepthFunc(GL_LEQUAL);
        glDepthRange(0.0f, 1.0f);
        glEnable(GL_DEPTH_CLAMP);
    }

    /**
     * Revert changed GL state.
     */
    private void postRender() {
        glDisable(GL_FRAMEBUFFER_SRGB);
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
    }

    public void render(SceneNode sceneGraph, CameraNode cameraNode) {

        if (cameraNode.fieldOfView() != _currentFOV) {
            _currentFOV = cameraNode.fieldOfView();
            this.setProjectionMatrix();
        }

        this.preRender();

        _defaultShader.useProgram();

        _defaultShader.setMaxIntensity(cameraNode.hdrMaxIntensity());
        _defaultShader.setLightData(Light.toLightBlock(sceneGraph.allLights(), cameraNode.worldToNodeSpaceTransform()));

        Matrix4 worldToCameraMatrix = cameraNode.worldToNodeSpaceTransform();

        sceneGraph.traverse((node) -> {
            if (node instanceof MeshNode) {
                MeshNode meshNode = (MeshNode)node;

                Matrix4 nodeToCameraSpaceTransform = worldToCameraMatrix.multiply(node.nodeToWorldSpaceTransform());
                Matrix3 normalModelToCameraSpaceTransform = nodeToCameraSpaceTransform.getMatrix3().inverse().transpose();

                _defaultShader.setModelToCameraMatrix(nodeToCameraSpaceTransform);
                _defaultShader.setNormalModelToCameraMatrix(normalModelToCameraSpaceTransform);

                meshNode.render(_defaultShader);
            }
        });

        _defaultShader.endUseProgram();

        this.postRender();
    }
}
