package swen.adventure.scenegraph;

import swen.adventure.rendering.GLMesh;
import swen.adventure.rendering.Material;
import swen.adventure.rendering.ObjMesh;
import swen.adventure.rendering.shaders.MaterialShader;
import swen.adventure.utils.BoundingBox;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 25/09/15.
 */
public class MeshNode extends SceneNode {

    private GLMesh<Float> _mesh;
    private Optional<Material> _materialOverride = Optional.empty();

    private BoundingBox _localSpaceBoundingBox;

    public MeshNode(final String fileName, final TransformNode parent) {
        this("mesh" + fileName, fileName, parent); //MeshNodes of the same file share ids.
    }

    public MeshNode(String id, final String fileName, final TransformNode parent) {
        super(id, parent, false); //TODO discuss why mesh nodes need to have the same id

        try {
            _mesh = MeshNode.loadMeshWithFileName(fileName);
            _localSpaceBoundingBox = _mesh.boundingBox();
        } catch (FileNotFoundException e) {
            System.err.println("Could not load mesh file " + fileName + ": " + e);
        }
    }

    public Optional<Material> materialOverride() {
        return _materialOverride;
    }

    public void setMaterialOverride(final Material materialOverride) {
        _materialOverride = Optional.of(materialOverride);
    }

    public Optional<BoundingBox> boundingBox() {
        return Optional.ofNullable(_localSpaceBoundingBox);
    }

    /**
     * Renders the mesh, applying its own material to the shader. If a material override is set, then that override is used.
     * @param shader The Material Shader on which to set the materials.
     */
    public void render(MaterialShader shader) {
        if (_materialOverride.isPresent()) {
            shader.setMaterial(_materialOverride.get().toFloatBuffer());
            _mesh.render();
        } else {
            _mesh.render(shader);
        }
    }

    /**
     * Renders the mesh using the currently bound shader and materials.
     */
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
