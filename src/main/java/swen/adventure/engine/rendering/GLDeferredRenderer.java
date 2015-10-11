package swen.adventure.engine.rendering;

import swen.adventure.engine.rendering.maths.Matrix3;
import swen.adventure.engine.rendering.maths.Matrix4;
import swen.adventure.engine.rendering.shaders.GaussianMaterialsNormalMapsShader;
import swen.adventure.engine.scenegraph.Light;
import swen.adventure.engine.scenegraph.MeshNode;

import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;

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
     * Renders the given nodes using the given lights and transformation matrix, overriding the projection matrix.
     * @param nodes The nodes to render
     * @param lights The lights to use in lighting the nodes
     * @param worldToCameraMatrix A transformation to convert the node's position in world space to a position in camera space.
     * @param projectionMatrix The projection matrix to use
     * @param hdrMaxIntensity The maximum light intensity in the scene.
     */
    public void render(List<MeshNode> nodes, List<Light> lights, Matrix4 worldToCameraMatrix, Matrix4 projectionMatrix, float hdrMaxIntensity) {

        this.preRender();



        this.postRender();
    }

    private void performGeometryPass(List<MeshNode> nodes) {
        _geometryPassShader.useProgram();

        _gBuffer.bindForWriting();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        nodes.forEach(node -> {
            node.render(_geometryPassShader);
        });
    }

    private void performLightPass() {
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        _gBuffer.bindForReading();

        int halfWidth = (int)(_width / 2.0f);
        int halfHeight = (int)(_height / 2.0f);

        _gBuffer.setReadBuffer(GBuffer.TextureType.Position);
        glBlitFramebuffer(0, 0, _width, _height, 0, 0, halfWidth, halfHeight, GL_COLOR_BUFFER_BIT, GL_LINEAR);

        _gBuffer.setReadBuffer(GBuffer.TextureType.Diffuse);
        glBlitFramebuffer(0, 0, _width, _height, 0, halfHeight, halfWidth, _height, GL_COLOR_BUFFER_BIT, GL_LINEAR);

        _gBuffer.setReadBuffer(GBuffer.TextureType.Normal);
        glBlitFramebuffer(0, 0, _width, _height, halfWidth, halfHeight, _width, _height, GL_COLOR_BUFFER_BIT, GL_LINEAR);

        _gBuffer.setReadBuffer(GBuffer.TextureType.TextureCoordinate);
        glBlitFramebuffer(0, 0, _width, _height, halfWidth, 0, _width, halfHeight, GL_COLOR_BUFFER_BIT, GL_LINEAR);
    }
}
