package swen.adventure.engine.rendering;

import swen.adventure.engine.rendering.maths.Matrix4;
import swen.adventure.engine.scenegraph.Light;
import swen.adventure.engine.scenegraph.MeshNode;

import java.util.List;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 13/10/15.
 *
 * A GLRenderer allows rendering meshes to the screen with particular materials, lights, and transforms.
 */
public interface GLRenderer {
    void setSize(int width, int height);

    void setSizeInPixels(int width, int height);

    /**
     * Renders the given nodes using the given lights and transformation matrix
     * @param nodes The nodes to render
     * @param lights The lights to use in lighting the nodes
     * @param worldToCameraMatrix A transformation to convert the node's position in world space to a position in camera space.
     * @param fieldOfView The field of view of the camera
     * @param hdrMaxIntensity The maximum light intensity in the scene.
     */
    void render(List<MeshNode> nodes, List<Light> lights, Matrix4 worldToCameraMatrix, float fieldOfView, float hdrMaxIntensity);

    /**
     * Renders the given nodes using the given lights and transformation matrix, overriding the projection matrix.
     * @param nodes The nodes to render
     * @param lights The lights to use in lighting the nodes
     * @param worldToCameraMatrix A transformation to convert the node's position in world space to a position in camera space.
     * @param projectionMatrix The projection matrix to use
     * @param hdrMaxIntensity The maximum light intensity in the scene.
     */
    void render(List<MeshNode> nodes, List<Light> lights, Matrix4 worldToCameraMatrix, Matrix4 projectionMatrix, float hdrMaxIntensity);
}
