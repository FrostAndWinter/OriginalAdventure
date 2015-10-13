package swen.adventure.engine.rendering;

import swen.adventure.engine.rendering.maths.Matrix3;
import swen.adventure.engine.rendering.maths.Matrix4;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.rendering.shaders.deferredrendering.DirectionalLightPassShader;
import swen.adventure.engine.rendering.shaders.deferredrendering.GeometryPassShader;
import swen.adventure.engine.rendering.shaders.deferredrendering.NullShader;
import swen.adventure.engine.rendering.shaders.deferredrendering.PointLightPassShader;
import swen.adventure.engine.scenegraph.Light;
import swen.adventure.engine.scenegraph.MeshNode;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 11/10/15.
 *
 * Implementation adapted from http://ogldev.atspace.co.uk/www/tutorial37/tutorial37.html
 */
public class GLDeferredRenderer implements GLRenderer {

    private GeometryPassShader _geometryPassShader;
    private PointLightPassShader _pointLightPassShader;
    private DirectionalLightPassShader _directionalLightPassShader;
    private NullShader _nullShader;
    private int _width, _height, _widthPixels, _heightPixels;
    private float _currentFOV = (float)Math.PI/3.f;
    private Matrix4 _currentProjectionMatrix;

    private GBuffer _gBuffer;

    public GLDeferredRenderer(int width, int height) {

        _geometryPassShader = new GeometryPassShader();
        _pointLightPassShader = new PointLightPassShader();
        _directionalLightPassShader = new DirectionalLightPassShader();
        _nullShader = new NullShader();
        this.setSize(width, height);
    }

    private Matrix4 perspectiveMatrix(int width, int height, float fieldOfView) {;
        float cameraNear = 1.f;
        float cameraFar = 10000.f;
        float cameraAspect = width / (float) height;

        return Matrix4.makePerspective(fieldOfView, cameraAspect, cameraNear, cameraFar);
    }

    @Override
    public void setSize(int width, int height) {
        _width = width; _height = height;
        _currentProjectionMatrix = this.perspectiveMatrix(_width, _height, _currentFOV);
    }

    @Override
    public void setSizeInPixels(int width, int height) {
        _widthPixels = width;
        _heightPixels = height;

        if (_gBuffer != null) {
            _gBuffer.destroy(); //delete any old framebuffers.
        }

        _gBuffer = new GBuffer(width, height);

        _directionalLightPassShader.useProgram();
        _directionalLightPassShader.setScreenSize(width, height);
        _directionalLightPassShader.endUseProgram();

        _pointLightPassShader.useProgram();
        _pointLightPassShader.setScreenSize(width, height);
        _pointLightPassShader.endUseProgram();
    }

    /**
     * Renders the given nodes using the given lights and transformation matrix
     * @param nodes The nodes to render
     * @param lights The lights to use in lighting the nodes
     * @param worldToCameraMatrix A transformation to convert the node's position in world space to a position in camera space.
     * @param fieldOfView The field of view of the camera
     * @param hdrMaxIntensity The maximum light intensity in the scene.
     */
    @Override
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
    @Override
    public void render(List<MeshNode> nodes, List<Light> lights, Matrix4 worldToCameraMatrix, Matrix4 projectionMatrix, float hdrMaxIntensity) {
        if (_gBuffer == null) {
            return;
        }

        this.preRender();

        _gBuffer.startFrame();
        this.performGeometryPass(nodes, worldToCameraMatrix, projectionMatrix);

        // We need stencil to be enabled in the stencil pass to get the stencil buffer
        // updated and we also need it in the light pass because we render the light
        // only if the stencil passes.
        glEnable(GL_STENCIL_TEST);
        lights.stream().filter(light -> light.type == Light.LightType.Point).forEach(light -> {
            Matrix4 lightToCameraMatrix = this.calculatePointLightSphereNodeToCameraTransform(light, worldToCameraMatrix, hdrMaxIntensity);
            this.performStencilPass(lightToCameraMatrix, projectionMatrix);
            this.performPointLightPass(light, lightToCameraMatrix, projectionMatrix, hdrMaxIntensity);
        });

        // The directional light does not need a stencil test because its volume
        // is unlimited and the final pass simply copies the texture.
        glDisable(GL_STENCIL_TEST);

        this.performDirectionalLightPass(lights, worldToCameraMatrix, hdrMaxIntensity);

        this.performFinalPass();

        this.postRender();
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

        glDisable(GL_BLEND);
    }

    /**
     * Revert changed GL state.
     */
    private void postRender() {
        glDisable(GL_FRAMEBUFFER_SRGB);
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);

        glBlendEquationSeparate(GL_FUNC_ADD, GL_FUNC_ADD);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE);
    }

    private void performGeometryPass(List<MeshNode> nodes, Matrix4 worldToCameraMatrix, Matrix4 projectionMatrix) {
        _geometryPassShader.useProgram();

        _gBuffer.bindForGeometryPass();

        glDepthMask(true);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glEnable(GL_DEPTH_TEST);

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
    }

    private void performStencilPass(Matrix4 lightToCameraMatrix, Matrix4 projectionMatrix) {
        _nullShader.useProgram();

        _gBuffer.bindForStencilPass();

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);

        glClear(GL_STENCIL_BUFFER_BIT);

        // We need the stencil test to be enabled but we want it
        // to succeed always. Only the depth test matters.
        glStencilFunc(GL_ALWAYS, 0, 0);

        glStencilOpSeparate(GL_BACK, GL_KEEP, GL_INCR_WRAP, GL_KEEP);
        glStencilOpSeparate(GL_FRONT, GL_KEEP, GL_DECR_WRAP, GL_KEEP);

        final GLMesh<Float> sphereMesh = MeshNode.meshWithFileName(null, "sphere.obj");

        _nullShader.setCameraToClipMatrix(projectionMatrix);

        _nullShader.setModelToCameraMatrix(lightToCameraMatrix);

        sphereMesh.render();

    }

    private float calculatePointLightSphereRadius(Light pointLight, float hdrMaxIntensity) {
        Vector3 colourVector = pointLight.colourVector();
        float maxChannel = Math.max(Math.max(colourVector.x, colourVector.y), colourVector.z) * 256 / hdrMaxIntensity;

        float exponential = pointLight.falloff == Light.LightFalloff.Quadratic ? Light.LightAttenuationFactor : 0;
        float linear = pointLight.falloff == Light.LightFalloff.Linear ? Light.LightAttenuationFactor : 0;
        float constant = 1.f;

        float radius = (-linear + (float)Math.sqrt(linear * linear - 4 * exponential * (constant - maxChannel)))/(2*exponential);
        return radius;
    }

    private Matrix4 calculatePointLightSphereNodeToCameraTransform(Light light, Matrix4 worldToCameraMatrix, float hdrMaxIntensity) {
        Matrix4 nodeToCameraSpaceTransform = worldToCameraMatrix.multiply(light.nodeToWorldSpaceTransform());

        Vector3 translationInWorldSpace = nodeToCameraSpaceTransform.multiplyWithTranslation(Vector3.zero);

        float scale = this.calculatePointLightSphereRadius(light, hdrMaxIntensity);
        Matrix4 scaleMatrix = Matrix4.makeScale(scale, scale, scale);
        return Matrix4.makeTranslation(translationInWorldSpace.x, translationInWorldSpace.y, translationInWorldSpace.z).multiply(scaleMatrix);
    }

    private void performPointLightPass(Light light, Matrix4 lightToCameraMatrix, Matrix4 projectionMatrix, float hdrMaxIntensity) {

        final GLMesh<Float> sphereMesh = MeshNode.meshWithFileName(null, "sphere.obj");

        _gBuffer.bindForLightPass();

        _pointLightPassShader.useProgram();

        glStencilFunc(GL_NOTEQUAL, 0, 0xFF);

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_ONE, GL_ONE);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);

        _pointLightPassShader.setCameraToClipMatrix(projectionMatrix);

        _pointLightPassShader.setPointLightData(light.pointLightDataBuffer(lightToCameraMatrix, hdrMaxIntensity));

        _pointLightPassShader.setModelToCameraMatrix(lightToCameraMatrix);

        sphereMesh.render();

        glCullFace(GL_BACK);
        glDisable(GL_BLEND);
    }

    private void performDirectionalLightPass(List<Light> lights, Matrix4 worldToCameraMatrix, float hdrMaxIntensity) {
        if (lights.isEmpty()) { return; }

        _gBuffer.bindForLightPass();
        _directionalLightPassShader.useProgram();

        final GLMesh<Float> quadMesh = MeshNode.meshWithFileName(null, "Plane.obj");

        List<Light> filteredLights = lights.stream()
                .filter(light -> light.type == Light.LightType.Directional || light.type == Light.LightType.Ambient)
                .collect(Collectors.toList());

        _directionalLightPassShader.setLightData(Light.toLightBlock(filteredLights, worldToCameraMatrix, hdrMaxIntensity));

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_ONE, GL_ONE);

        quadMesh.render();

        glDisable(GL_BLEND);

        _directionalLightPassShader.endUseProgram();
    }

    private void performFinalPass() {
        _gBuffer.bindForFinalPass();

        glBlitFramebuffer(0, 0, _widthPixels, _heightPixels,
                0, 0, _widthPixels, _heightPixels, GL_COLOR_BUFFER_BIT, GL_LINEAR);
        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
    }

}
