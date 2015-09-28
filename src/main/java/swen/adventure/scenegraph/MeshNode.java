package swen.adventure.scenegraph;

import swen.adventure.rendering.GLMesh;
import swen.adventure.rendering.Material;
import swen.adventure.rendering.ObjMesh;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 25/09/15.
 */
public class MeshNode extends SceneNode {

    private GLMesh<Float> _mesh;
    private Material _material = Material.DefaultMaterial;

    public MeshNode(final String fileName, final TransformNode parent) {
        this("mesh" + fileName, fileName, parent); //MeshNodes of the same file share ids.
    }

    public MeshNode(String id, final String fileName, final TransformNode parent) {
        super(id, parent, false); //TODO discuss why mesh nodes need to have the same id

        try {
            _mesh = MeshNode.loadMeshWithFileName(fileName);
        } catch (FileNotFoundException e) {
            System.err.println("Could not load mesh file " + fileName + ": " + e);
        }
    }

    public Material material() {
        return _material;
    }

    public void setMaterial(final Material material) {
        _material = material;
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
