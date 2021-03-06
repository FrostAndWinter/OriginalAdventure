/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 package swen.adventure.engine.rendering;

import swen.adventure.engine.rendering.maths.Matrix3;
import swen.adventure.engine.rendering.maths.Matrix4;
import swen.adventure.engine.rendering.shaders.GaussianMaterialsNormalMapsShader;
import swen.adventure.engine.scenegraph.Light;
import swen.adventure.engine.scenegraph.MeshNode;

import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_SRGB;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 19/09/15.
 *
 * GLForwardRenderer provides mechanisms to render MeshNodes under lighting defined by a set of Lights and using a given camera matrix.
 * It performs lighting and geometry in a single pass.
 */
public class GLForwardRenderer implements GLRenderer {

    private GaussianMaterialsNormalMapsShader _defaultShader;
    private int _width, _height;
    private float _currentFOV = (float)Math.PI/3.f;
    private Matrix4 _currentProjectionMatrix;

    public GLForwardRenderer(int width, int height) {
        _defaultShader = new GaussianMaterialsNormalMapsShader();

        this.setSize(width, height);
    }

    private Matrix4 perspectiveMatrix(int width, int height, float fieldOfView) {;
        float cameraNear = 1.f;
        float cameraFar = 10000.f;
        float cameraAspect = width / (float) height;

        return Matrix4.makePerspective(fieldOfView, cameraAspect, cameraNear, cameraFar);
    }

    public void setSize(int width, int height) {
        _width = width; _height = height;
        _currentProjectionMatrix = this.perspectiveMatrix(_width, _height, _currentFOV);
    }

    @Override
    public void setSizeInPixels(final int width, final int height) {

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

        glClear(GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Revert changed GL state.
     */
    private void postRender() {
        glDisable(GL_FRAMEBUFFER_SRGB);
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
    }

    /**
     * Renders the given nodes using the given lights and transformation matrix
     * @param nodes The nodes to render
     * @param lights The lights to use in lighting the nodes
     * @param worldToCameraMatrix A transformation to convert the node's position in world space to a position in camera space.
     * @param fieldOfView The field of view of the camera
     * @param hdrMaxIntensity The maximum light intensity in the scene.
     */
    public void render(List<MeshNode> nodes, List<Light> lights, Matrix4 worldToCameraMatrix, float fieldOfView, float hdrMaxIntensity) {

        if (fieldOfView != _currentFOV) {
            _currentFOV = fieldOfView;
            _currentProjectionMatrix = this.perspectiveMatrix(_width, _height, fieldOfView);
        }

        this.render(nodes, lights, worldToCameraMatrix, _currentProjectionMatrix, hdrMaxIntensity);
    }

    /**
     * Renders the given nodes using the given lights and transformation matrix, overriding the projection matrix.
     * @param nodes The nodes to render
     * @param lights The lights to use in lighting the nodes
     * @param worldToCameraMatrix A transformation to convert the node's position in world space to a position in camera space.
     * @param projectionMatrix The projection matrix to use
     * @param hdrMaxIntensity The maximum light intensity in the scene.
     */
    public void render(List<MeshNode> nodes, List<Light> lights, Matrix4 worldToCameraMatrix, Matrix4 projectionMatrix, float hdrMaxIntensity) {

        this.preRender();

        _defaultShader.useProgram();

        _defaultShader.setCameraToClipMatrix(projectionMatrix);

        _defaultShader.setLightData(Light.toLightBlock(lights.stream().filter(Light::isOn).collect(Collectors.toList()), worldToCameraMatrix, hdrMaxIntensity));

        nodes.forEach(node -> {
            Matrix4 nodeToCameraSpaceTransform = worldToCameraMatrix.multiply(node.nodeToWorldSpaceTransform());
            Matrix3 normalModelToCameraSpaceTransform = nodeToCameraSpaceTransform.getMatrix3().inverse().transpose();

            _defaultShader.setModelToCameraMatrix(nodeToCameraSpaceTransform);
            _defaultShader.setNormalModelToCameraMatrix(normalModelToCameraSpaceTransform);

            node.render(_defaultShader, hdrMaxIntensity);
        });


        _defaultShader.endUseProgram();

        this.postRender();
    }
}