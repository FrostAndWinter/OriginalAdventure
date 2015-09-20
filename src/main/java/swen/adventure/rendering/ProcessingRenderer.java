package swen.adventure.rendering;

import processing.core.PGraphics;
import processing.core.PShape;
import processing.opengl.PGraphics3D;
import swen.adventure.rendering.maths.Matrix4;
import swen.adventure.scenegraph.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 17/09/15.
 */
public class ProcessingRenderer {

    private PGraphics3D _graphicsContext;

    public ProcessingRenderer(PGraphics3D graphicsContext) {
        _graphicsContext = graphicsContext;
    }

    public Matrix4 perspectiveMatrix() {
        float cameraFOV = (float)Math.PI/3.f;
        float cameraNear = 1.f;
        float cameraFar = 10000.f;
        float cameraAspect = (float) _graphicsContext.width / (float) _graphicsContext.height;

        Matrix4 perspectiveMatrix = Matrix4.makePerspective((float)Math.PI/3, cameraAspect, cameraNear, cameraFar);

        return perspectiveMatrix;
    }


    public PGraphics render(SceneNode sceneGraph) {
        _graphicsContext.beginDraw();
        _graphicsContext.clear();

        _graphicsContext.directionalLight(150f, 60f, 83f, -0.6f, 0.3f, 0.f);
        _graphicsContext.ambientLight(100.f, 110.f, 120.f);

//        _graphicsContext.camera = new PMatrix3D();
//        _graphicsContext.cameraInv = new PMatrix3D();

        Matrix4 projectionMatrix = this.perspectiveMatrix();
        _graphicsContext.projection = projectionMatrix.toPMatrix();

        Matrix4 worldToCameraMatrix = Matrix4.makeTranslation(0, -30.f, 0.f);
        Matrix4 cameraToWorldMatrix = Matrix4.makeTranslation(0, 30.f, 0.f);

        sceneGraph.traverse((node) -> {
            if (node instanceof ProcessingMesh) {
                PShape shape = ((ProcessingMesh) node).mesh();

                _graphicsContext.modelview = worldToCameraMatrix.multiply(node.nodeToWorldSpaceTransform()).toPMatrix();
                _graphicsContext.modelviewInv = node.worldToNodeSpaceTransform().multiply(cameraToWorldMatrix).toPMatrix();
                _graphicsContext.projmodelview.set(_graphicsContext.projection);
                _graphicsContext.projmodelview.apply(_graphicsContext.modelview);

                _graphicsContext.shape(shape);
            }
        });

        _graphicsContext.endDraw();

        return _graphicsContext;
    }
}