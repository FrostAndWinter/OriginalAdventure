package swen.adventure.engine.scenegraph;

import swen.adventure.engine.Event;
import swen.adventure.engine.datastorage.BundleObject;
import swen.adventure.engine.rendering.GLMesh;
import swen.adventure.engine.rendering.Material;
import swen.adventure.engine.rendering.ObjMesh;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.rendering.shaders.MaterialShader;
import swen.adventure.engine.rendering.maths.BoundingBox;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 25/09/15.
 */
public class MeshNode extends SceneNode {

    private GLMesh<Float> _mesh;
    private Optional<Material> _materialOverride = Optional.empty();

    private BoundingBox _localSpaceBoundingBox;
    private Vector3 _textureScale = Vector3.one;

    public final Event<SceneNode> eventMeshClicked = new Event<>("eventMeshClicked", this);

    public MeshNode(final String directory, final String fileName, final TransformNode parent) {
        this("mesh" + fileName, directory, fileName, parent); //MeshNodes of the same file share ids.
    }

    public MeshNode(String id, final String directory, final String fileName, final TransformNode parent) {
        super(id, parent, false); //TODO discuss why mesh nodes need to have the same id

        try {
            _mesh = MeshNode.loadMeshWithFileName(directory, fileName);
            _localSpaceBoundingBox = _mesh.boundingBox();
        } catch (FileNotFoundException e) {
            System.err.println("Could not load mesh file " + fileName + ": " + e);
        }
    }

    @Override
    public BundleObject toBundle() {
        String fileName = id.substring("mesh".length()); // the id must equal "mesh" + fileName
        return super.toBundle()
                .put("fileName", fileName);
    }

    private static MeshNode createSceneNodeFromBundle(BundleObject bundle, Function<String, TransformNode> findParentFunction) {
        String id = bundle.getString("id");
        String parentId = bundle.getString("parentId");
        TransformNode parent = findParentFunction.apply(parentId);
        String fileName = bundle.getString("fileName");
        return new MeshNode(id, fileName, parent);
    }

    public Optional<Material> materialOverride() {
        return _materialOverride;
    }

    public void setMaterialOverride(final Material materialOverride) {
        _materialOverride = Optional.of(materialOverride);
    }

    public BoundingBox boundingBox() {
        return _localSpaceBoundingBox;
    }

    /**
     * Renders the mesh, applying its own material to the shader. If a material override is set, then that override is used.
     * @param shader The Material Shader on which to set the materials.
     */
    public void render(MaterialShader shader) {
        shader.setTextureScale(_textureScale);
        _materialOverride.ifPresent(material -> {
            shader.setMaterial(material.toBuffer());
            material.bindTextures();
            Material.bindSamplers();

            _mesh.render();

            Material.unbindSamplers();
            Material.unbindTextures();
        });
        if (!_materialOverride.isPresent()) {
            _mesh.render(shader);
        }
    }


    public void setTextureScale(Vector3 textureScale) {
        _textureScale = textureScale;
    }

    /** The texture scale is how much the textures should be scaled in each axis on this mesh. A scale less than 1 will tile the textures. */
    public Vector3 textureScale() {
        return _textureScale;
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

    private static GLMesh<Float> loadMeshWithFileName(String directory, String fileName) throws FileNotFoundException {
        GLMesh<Float> mesh = _loadedMeshes.get(fileName);

        if (mesh == null) {
            String[] fileNameComponents = fileName.split("\\.");
            String extension = fileNameComponents[fileNameComponents.length - 1];

            if (extension.equalsIgnoreCase("obj")) {
                mesh = ObjMesh.loadMesh(directory, fileName);
            } else {
                throw new RuntimeException("The file format " + extension + " is not supported.");
            }
            _loadedMeshes.put(fileName, mesh);
        }

        return mesh;
    }
}
