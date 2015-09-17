package swen.adventure.rendering;

import processing.core.PGraphics;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.opengl.PGraphics3D;
import processing.opengl.PGraphicsOpenGL;
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

    public Matrix4 perspectiveMatrix() {
        float cameraFOV = (float)Math.PI/3.f;
        float cameraX = _graphicsContext.width / 2.0f;
        float cameraY = _graphicsContext.height / 2.0f;
        float cameraZ = cameraY / ((float) Math.tan(cameraFOV / 2.0f));
        float cameraNear = 1.f;
        float cameraFar = 600.f;
        float cameraAspect = (float) _graphicsContext.width / (float) _graphicsContext.height;

        Matrix4 perspectiveMatrix = Matrix4.makePerspective((float)Math.PI/9, cameraAspect, cameraNear, cameraFar);

        return perspectiveMatrix;
    }


    public PGraphics render(SceneNode sceneGraph) {
        _graphicsContext.beginDraw();
        _graphicsContext.clear();

        _graphicsContext.camera = new PMatrix3D();
        _graphicsContext.cameraInv = new PMatrix3D();

        Matrix4 projectionMatrix = this.perspectiveMatrix();
        _graphicsContext.projection = projectionMatrix.toPMatrix();

        Matrix4 worldToCameraMatrix = Matrix4.makeTranslation(0, 0, 0.f);
        Matrix4 cameraToWorldMatrix = Matrix4.makeTranslation(0, 0, 0.f);

        sceneGraph.traverse((node) -> {
            if (node instanceof MeshNode) {
                PShape shape = ((MeshNode) node).mesh();

                _graphicsContext.modelview = worldToCameraMatrix.multiply(node.modelToWorldSpaceTransform()).toPMatrix();
                _graphicsContext.modelviewInv = node.worldToModelSpaceTransform().multiply(cameraToWorldMatrix).toPMatrix();
                _graphicsContext.projmodelview.set(_graphicsContext.projection);
                _graphicsContext.projmodelview.apply(_graphicsContext.modelview);
                _graphicsContext.shape(shape);
            }
        });

        System.out.println("Camera matrix is:");
        _graphicsContext.camera.print();
        System.out.println("Camera inv matrix is:");
        _graphicsContext.cameraInv.print();
        System.out.println("Modelview matrix is:");
        _graphicsContext.modelview.print();
        System.out.println("Modelview inv matrix is:");
        _graphicsContext.modelviewInv.print();
        System.out.println("Projection matrix is:");
        _graphicsContext.projection.print();
        System.out.println("Projmodelview matrix is:");
        _graphicsContext.projmodelview.print();

        _graphicsContext.endDraw();

        return _graphicsContext;
    }
}