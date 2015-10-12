package swen.adventure.engine.rendering;

import javafx.scene.shape.Mesh;
import swen.adventure.engine.rendering.maths.Matrix3;
import swen.adventure.engine.rendering.maths.Matrix4;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.rendering.shaders.deferredrendering.DirectionalLightPassShader;
import swen.adventure.engine.rendering.shaders.deferredrendering.GeometryPassShader;
import swen.adventure.engine.rendering.shaders.deferredrendering.LightPassShader;
import swen.adventure.engine.rendering.shaders.deferredrendering.PointLightPassShader;
import swen.adventure.engine.scenegraph.Light;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.SceneNode;
import swen.adventure.engine.scenegraph.TransformNode;

import java.awt.*;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 11/10/15.
 */
public class GLDeferredRenderer {

    private GeometryPassShader _geometryPassShader;
    private PointLightPassShader _pointLightPassShader;
    private DirectionalLightPassShader _directionalLightPassShader;
    private int _width, _height;
    private float _currentFOV = (float)Math.PI/3.f;
    private Matrix4 _currentProjectionMatrix;

    private GBuffer _gBuffer;

    public GLDeferredRenderer(int width, int height) {

        _geometryPassShader = new GeometryPassShader();
        _pointLightPassShader = new PointLightPassShader();
        _directionalLightPassShader = new DirectionalLightPassShader();
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
        this.beginLightPasses();
        this.performPointLightPass(lights, worldToCameraMatrix, projectionMatrix, hdrMaxIntensity);
        this.performDirectionalLightPasses(lights, worldToCameraMatrix, hdrMaxIntensity);
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

    private float calculatePointLightSphereRadius(Light pointLight, float hdrMaxIntensity) {
        Vector3 colourVector = pointLight.colourVector();
        float maxChannel = Math.max(Math.max(colourVector.x, colourVector.y), colourVector.z) * 256 / hdrMaxIntensity;

        float exponential = pointLight.falloff == Light.LightFalloff.Quadratic ? Light.LightAttenuationFactor : 0;
        float linear = pointLight.falloff == Light.LightFalloff.Linear ? Light.LightAttenuationFactor : 0;
        float constant = 0;

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

    private void performPointLightPass(List<Light> lights, Matrix4 worldToCameraMatrix, Matrix4 projectionMatrix, float hdrMaxIntensity) {
        if (lights.isEmpty()) { return; }

        GLMesh<Float> sphereMesh = null;
        try {
            sphereMesh = MeshNode.loadMeshWithFileName(null, "sphere.obj");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Missing sphere mesh: " + e);
        }

        _pointLightPassShader.useProgram();

        _pointLightPassShader.setCameraToClipMatrix(projectionMatrix);
        _pointLightPassShader.setMaxIntensity(hdrMaxIntensity);

        final GLMesh<Float> finalSphereMesh = sphereMesh;
        lights.stream().filter(light -> light.type == Light.LightType.Point).forEach(light -> {
            _pointLightPassShader.setPointLightData(light.pointLightDataBuffer(worldToCameraMatrix));

            Matrix4 nodeToCameraSpaceTransform = this.calculatePointLightSphereNodeToCameraTransform(light, worldToCameraMatrix, hdrMaxIntensity);

            _pointLightPassShader.setModelToCameraMatrix(nodeToCameraSpaceTransform);

            finalSphereMesh.render();
        });
    }

    private void performDirectionalLightPasses(List<Light> lights, Matrix4 worldToCameraMatrix, float hdrMaxIntensity) {
        if (lights.isEmpty()) { return; }

        _directionalLightPassShader.useProgram();

        GLMesh<Float> quadMesh = null;
        try {
            quadMesh = MeshNode.loadMeshWithFileName(null, "Plane.obj");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Missing quad mesh: " + e);
        }

        List<Light> filteredLights = lights.stream()
                .filter(light -> light.type == Light.LightType.Directional || light.type == Light.LightType.Ambient)
                .collect(Collectors.toList());

        _directionalLightPassShader.setMaxIntensity(hdrMaxIntensity);
        _directionalLightPassShader.setLightData(Light.toLightBlock(filteredLights, worldToCameraMatrix));

        quadMesh.render();

        _directionalLightPassShader.endUseProgram();
    }
}
