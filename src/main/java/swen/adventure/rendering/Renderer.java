package swen.adventure.rendering;

import processing.core.PGraphics;
import processing.core.PShape;
import processing.opengl.PGraphics3D;
import swen.adventure.rendering.maths.Matrix4;
import swen.adventure.scenegraph.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 17/09/15.
 */
public class Renderer {

    private PGraphics3D _graphicsContext;

    public Renderer(PGraphics3D graphicsContext) {
        _graphicsContext = graphicsContext;
    }

    public PGraphics render(SceneNode sceneGraph) {
        _graphicsContext.beginDraw();
        _graphicsContext.clear();

        _graphicsContext.resetProjection();
        _graphicsContext.resetMatrix();

        _graphicsContext.lights();

        Matrix4 worldToCameraMatrix = new Matrix4();

        sceneGraph.traverse((node) -> {
            if (node instanceof MeshNode) {
                PShape shape = ((MeshNode) node).mesh();
                _graphicsContext.setMatrix(node.worldSpaceTransform().multiply(worldToCameraMatrix).toPMatrix());
                _graphicsContext.shape(shape);
            }
        });

        _graphicsContext.endDraw();

        return _graphicsContext;
    }
}
