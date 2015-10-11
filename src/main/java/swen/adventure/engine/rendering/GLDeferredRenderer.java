package swen.adventure.engine.rendering;

import swen.adventure.engine.rendering.maths.Matrix3;
import swen.adventure.engine.rendering.maths.Matrix4;
import swen.adventure.engine.rendering.shaders.deferredrendering.GeometryPassShader;
import swen.adventure.engine.scenegraph.Light;
import swen.adventure.engine.scenegraph.MeshNode;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 11/10/15.
 */
public class GLDeferredRenderer {

    private GeometryPassShader _geometryPassShader;
    private int _width, _height;
    private float _currentFOV = (float)Math.PI/3.f;
    private Matrix4 _currentProjectionMatrix;

    private GBuffer _gBuffer;

    public GLDeferredRenderer(int width, int height) {

        _geometryPassShader = new GeometryPassShader();
        _gBuffer = new GBuffer(width, height);
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

        this.performGeometryPass(nodes, worldToCameraMatrix, projectionMatrix);
        this.performLightPass();

    }


    private void performGeometryPass(List<MeshNode> nodes, Matrix4 worldToCameraMatrix, Matrix4 projectionMatrix) {
        _geometryPassShader.useProgram();

        _gBuffer.bindForWriting();

        glDepthMask(true);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glEnable(GL_DEPTH_TEST);

        glDisable(GL_BLEND);

        _geometryPassShader.setCameraToClipMatrix(projectionMatrix);

        nodes.forEach(node -> {
            Matrix4 nodeToCameraSpaceTransform = worldToCameraMatrix.multiply(node.nodeToWorldSpaceTransform());
            Matrix3 normalModelToCameraSpaceTransform = nodeToCameraSpaceTransform.getMatrix3().inverse().transpose();

            _geometryPassShader.setModelToCameraMatrix(nodeToCameraSpaceTransform);
            _geometryPassShader.setNormalModelToCameraMatrix(normalModelToCameraSpaceTransform);

            node.render(_geometryPassShader);
        });


        _geometryPassShader.endUseProgram();

        // When we get here the depth buffer is already populated and the stencil pass
        // depends on it, but it does not write to it.
        glDepthMask(false);

        glDisable(GL_DEPTH_TEST);

    }

    private void beginLightPasses() {
        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_ONE, GL_ONE);

        _gBuffer.bindForReading();
        glClear(GL_COLOR_BUFFER_BIT);
    }

    private void performPointLightPass() {

    }

    private void performLightPass() {
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        _gBuffer.bindForReading();

        int halfWidth = (int)(_width / 2.0f);
        int halfHeight = (int)(_height / 2.0f);

        _gBuffer.setReadBuffer(TextureUnit.PositionUnit);
        glBlitFramebuffer(0, 0, _width, _height, 0, 0, halfWidth, halfHeight, GL_COLOR_BUFFER_BIT, GL_LINEAR);

        _gBuffer.setReadBuffer(TextureUnit.DiffuseColourUnit);
        glBlitFramebuffer(0, 0, _width, _height, 0, halfHeight, halfWidth, _height, GL_COLOR_BUFFER_BIT, GL_LINEAR);

        _gBuffer.setReadBuffer(TextureUnit.VertexNormalUnit);
        glBlitFramebuffer(0, 0, _width, _height, halfWidth, halfHeight, _width, _height, GL_COLOR_BUFFER_BIT, GL_LINEAR);

        _gBuffer.setReadBuffer(TextureUnit.TextureCoordinateUnit);
        glBlitFramebuffer(0, 0, _width, _height, halfWidth, 0, _width, halfHeight, GL_COLOR_BUFFER_BIT, GL_LINEAR);
    }
}
