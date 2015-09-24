package swen.adventure.scenegraph;

import swen.adventure.rendering.GLMesh;
import swen.adventure.rendering.ObjMesh;
import swen.adventure.rendering.maths.Vector4;

import javax.swing.text.html.Option;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 25/09/15.
 */
public class MeshNode extends SceneNode {

    private GLMesh<Float> _mesh;
    private Optional<Vector4> _colourOverride = Optional.empty();

    public MeshNode(final String fileName, final TransformNode parent) {
        super("mesh" + fileName, parent, false); //MeshNodes of the same file share ids.

        try {
            _mesh = MeshNode.loadMeshWithFileName(fileName);
        } catch (FileNotFoundException e) {
            System.err.println("Could not load mesh file " + fileName + ": " + e);
        }
    }

    public void setColour(Vector4 colour) {
        _colourOverride = Optional.of(colour);
    }

    public Optional<Vector4> colour() {
        return _colourOverride;
    }

    public void render() {
        _mesh.render();
    }

    public void render(String vertexArrayObjectName) {
        _mesh.render(vertexArrayObjectName);
    }

    private static Map<String, GLMesh<Float>> _loadedMeshes = new HashMap<>();

    private static GLMesh<Float> loadMeshWithFileName(String fileName) throws FileNotFoundException {
        GLMesh<Float> mesh = _loadedMeshes.get(fileName);

        if (mesh == null) {
            String[] fileNameComponents = fileName.split("\\.");
            String extension = fileNameComponents[fileNameComponents.length - 1];

            if (extension.equalsIgnoreCase("obj")) {
                mesh = ObjMesh.loadMesh(fileName);
            } else {
                throw new RuntimeException("The file format " + extension + " is not supported.");
            }
            _loadedMeshes.put(fileName, mesh);
        }

        return mesh;
    }
}
